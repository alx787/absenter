package ru.sds.plugialo.absenter.ui;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class UserAbsenceStatusPanel implements WebPanel {
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    static String userOfProfile = "";

    public UserAbsenceStatusPanel(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public boolean showPanel(ApplicationUser applicationUser, ApplicationUser applicationUser1) {
        userOfProfile = applicationUser.getDisplayName() + " : " + applicationUser1.getDisplayName();

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, applicationUser);
        return hasPlannerAccess;
    }

    public void init(ViewProfilePanelModuleDescriptor viewProfilePanelModuleDescriptor) {
    }

    public String getHtml(Map<String, Object> context) {
        VelocityParamFactory velocityParamFactory = ComponentAccessor.getVelocityParamFactory();
        Map<String, Object> contextParameters = velocityParamFactory.getDefaultVelocityParams();
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        boolean isAdmin = true;
        ApplicationUser profileUser = (ApplicationUser) context.get("profileUser");
        contextParameters.put("profileUser", profileUser.getName());
        contextParameters.put("profileUserFullName", profileUser.getDisplayName());
        contextParameters.put("jira_base_url",
                ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
        contextParameters.put("isAdmin", isAdmin);
        contextParameters.put("userFullName", currentUser.getDisplayName());
        contextParameters.put("currentUser", currentUser.getUsername());
        String dialogHtml = ComponentAccessor.getVelocityManager().getBody("/templates/", "UserAbsenceStatus.vm", contextParameters);
        return dialogHtml;
    }

    public void writeHtml(Writer writer, Map<String, Object> context) throws IOException {
    }
}
