package ru.sds.plugialo.absenter.ui;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.model.UserAbsence;
import ru.sds.plugialo.absenter.permissions.PermissionsService;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;
import ru.sds.plugialo.absenter.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
public class UserAbsencePanel implements ViewProfilePanel {

    //    @ComponentImport
//    private final ActiveObjects ao;
    private final AbsencePlannerService absencePlannerService;

    @ComponentImport
    private final UserManager userManager;

    @ComponentImport
    private final LoginUriProvider loginUriProvider;

    @ComponentImport
    private final TemplateRenderer renderer;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @ComponentImport
    private final I18nResolver i18n;


    @Inject
    public UserAbsencePanel(AbsencePlannerService absencePlannerService, UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer, PluginSettingsFactory pluginSettingsFactory, I18nResolver i18n) {
        this.absencePlannerService = absencePlannerService;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.i18n = i18n;
    }

    public boolean showPanel(ApplicationUser applicationUser, ApplicationUser applicationUser1) {

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, applicationUser1);
        return hasPlannerAccess;
    }

    public void init(ViewProfilePanelModuleDescriptor viewProfilePanelModuleDescriptor) {

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser());
        if (!hasPlannerAccess)
            viewProfilePanelModuleDescriptor.setBroken();
    }

    public String getHtml(ApplicationUser applicationUser) {

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, applicationUser);

        if (!hasPlannerAccess)
            return "<div class=\"aui-message\">\n        <p class=\"title\">\n            <strong>Limited Access</strong>\n        </p>\n        <p>You don't have access to the user absence planner as it has been restricted by a Jira administrator</p>\n    </div>";

        VelocityParamFactory velocityParamFactory = ComponentAccessor.getVelocityParamFactory();

        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        boolean theSameUser = Objects.equals(applicationUser.getName(), currentUser.getName());

        Map<String, Object> contextParameters = velocityParamFactory.getDefaultVelocityParams();
        contextParameters.put("theSameUser", theSameUser);
        contextParameters.put("userOfProfile", applicationUser.getName());
        contextParameters.put("currentUser", currentUser.getUsername());

        ApplicationUser representative = Utils.getAlternateUser(applicationUser);
        contextParameters.put("representative", representative);
        contextParameters.put("assignable", Utils.automaticAssignmentEnabled(applicationUser));
        contextParameters.put("commentable", Utils.addCommentEnabled(applicationUser));

        contextParameters.put("i18n", this.i18n);

        PermissionsService permissionsService = new PermissionsService(this.userManager);
        boolean isAdmin = permissionsService.isSystemAdmin();
        boolean isEditor = applicationUser.getName().equalsIgnoreCase(currentUser.getName());
        if (!isEditor)
            isEditor = PlannerSecurityService.isUserAbsencePlannerEditor(currentUser, this.pluginSettingsFactory, isAdmin);

        contextParameters.put("commentDisabled", PlannerSecurityService.getCurrentSettings(this.pluginSettingsFactory).isDisableAutoComment());
        contextParameters.put("isEditor", isEditor);
        contextParameters.put("userFullName", currentUser.getDisplayName());
        contextParameters.put("jira_base_url", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));

        contextParameters.put("userAbsencesList", absencePlannerService.getAllUserAbsencesForUser(applicationUser.getName()));
        List<UserAbsence> regularAbsences = absencePlannerService.getAllUserRegularAbsencesForUser(applicationUser.getName());

        if (regularAbsences != null && regularAbsences.size() > 0)
            contextParameters.put("userRegularAbsencesList", regularAbsences);

        String dialogHtml = ComponentAccessor.getVelocityManager().getBody("/templates/", "UserAbsencePlanner.vm", contextParameters);
        return dialogHtml;
    }
}
