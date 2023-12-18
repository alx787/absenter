package ru.sds.plugialo.absenter.events;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.user.LoginEvent;
import com.atlassian.jira.event.user.LogoutEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.events.SessionCreatedEvent;
import com.atlassian.sal.api.events.SessionDestroyedEvent;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.logger.LogService;
import ru.sds.plugialo.absenter.model.PlannerConfig;
import ru.sds.plugialo.absenter.services.AbsenceService;
import ru.sds.plugialo.absenter.services.PlannerIssueService;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;
import ru.sds.plugialo.absenter.utils.Utils;

import java.time.LocalDate;

@Component
public class UserAbsenceLogoutEvent implements InitializingBean, DisposableBean {
    @JiraImport
    private final EventPublisher eventPublisher;

    @JiraImport
    private final PluginSettingsFactory pluginSettingsFactory;

//    @JiraImport
//    private ActiveObjects ao;

    private final AbsencePlannerService absencePlannerService;


    @Autowired
    public UserAbsenceLogoutEvent(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory, AbsencePlannerService absencePlannerService) {
        this.eventPublisher = eventPublisher;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.absencePlannerService = absencePlannerService;
    }

    public void afterPropertiesSet() throws Exception {
        this.eventPublisher.register(this);
    }

    public void destroy() throws Exception {
        this.eventPublisher.unregister(this);
    }

    @EventListener
    public void onUserLogoutEvent(LogoutEvent logoutEvent) {
        ApplicationUser applicationUser = logoutEvent.getUser();
        UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
        PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
        ptSet.setBoolean(Utils.userPropIsOnline, false);
    }

    @EventListener
    public void onUserLogInEvent(LoginEvent loginEvent) {
        ApplicationUser applicationUser = loginEvent.getUser();
        UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
        PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
        ptSet.setBoolean(Utils.userPropIsOnline, true);
    }

    @EventListener
    public void onUserSessionTimeoutEvent(SessionDestroyedEvent sessionDestroyedEvent) {
        UserManager userManager = ComponentAccessor.getUserManager();
        if (sessionDestroyedEvent != null && sessionDestroyedEvent.getUserName() != null) {
            ApplicationUser applicationUser = userManager.getUserByName(sessionDestroyedEvent.getUserName());
            UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
            PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
            ptSet.setBoolean(Utils.userPropIsOnline, false);
        }
    }

    @EventListener
    public void onUserSessionCreatedEvent(SessionCreatedEvent sessionCreatedEvent) {
        UserManager userManager = ComponentAccessor.getUserManager();
        if (sessionCreatedEvent != null && sessionCreatedEvent.getUserName() != null) {
            ApplicationUser applicationUser = userManager.getUserByName(sessionCreatedEvent.getUserName());
            UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
            PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
            ptSet.setBoolean(Utils.userPropIsOnline, true);
        }
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();
        if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID) || eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID) || eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            boolean assigneeChanged = false;
            try {
                for (Object item : issueEvent.getChangeLog().getRelated("ChildChangeItem")) {
                    if (item.toString().contains("[field,assignee]"))
                        assigneeChanged = true;
                }
            } catch (GenericEntityException e) {
                LogService.error("Error getting change log" + e.getMessage());
            }
            if (assigneeChanged || eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)) {
//                AbsenceService absenceService = new AbsenceService();
                try {
                    PlannerConfig plannerConfig = PlannerSecurityService.getCurrentSettings(this.pluginSettingsFactory);
                    boolean isCommentDisabled = false;
                    if (plannerConfig != null)
                        isCommentDisabled = plannerConfig.isDisableAutoComment();

                    if (issue.getAssignee() != null) {
                        boolean isUserAbsent = AbsenceService.isUserAbsenceToday(absencePlannerService, issue.getAssignee().getUsername());

                        if (isUserAbsent) {
                            ApplicationUser applicationUser = issue.getAssignee();
                            String alternate = Utils.getSelectedAssignee(issue.getAssignee());
                            if (alternate != null && alternate.contains(":")) {
                                String isAssignable = alternate.split(":")[1];
                                boolean addComment = false;
                                if ((alternate.split(":")).length == 3 && alternate.split(":")[2].equals("true") && !isCommentDisabled)
                                    addComment = true;

                                String message = AbsenceService.getAbsencesMessage(absencePlannerService, applicationUser.getUsername(), LocalDate.now().toEpochDay());

                                if (isAssignable.equals("true")) {
//
                                    ApplicationUser alternateUser = Utils.getAlternateUser(applicationUser);
                                    if (PlannerIssueService.updateIssueAssignee(issueEvent.getUser(), issue, alternateUser) && addComment)
                                        AbsenceService.commentOnIssue(applicationUser, issueEvent.getIssue(), message + "\n  Issue is reassigned to: " + alternateUser.getDisplayName());
                                } else if (addComment) {
                                    AbsenceService.commentOnIssue(applicationUser, issueEvent.getIssue(), message);
                                }
                            }
                        }
                    } else {
                        LogService.error(" ASSIGNEE OF THE ISSUE IS NULL");
                    }
                } catch (Exception e) {
                    LogService.error("ERROR IN CATCHING ISSUE EVENTS");
                }
            }
        }
    }
}
