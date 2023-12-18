package ru.sds.plugialo.absenter.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ru.sds.plugialo.absenter.model.PlannerConfig;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;

import java.util.Map;

public class AccessCondition extends AbstractWebCondition {
    @ComponentImport
    PluginSettingsFactory pluginSettingsFactory;

    public AccessCondition(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public PluginSettingsFactory getPluginSettingsFactory() {
        return this.pluginSettingsFactory;
    }

    public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public boolean shouldDisplay(Map<String, Object> context) {
        ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        PlannerConfig plannerConfig = PlannerSecurityService.getCurrentSettings(this.pluginSettingsFactory);
        boolean hasAccess = PlannerSecurityService.isMemberOfAccessGroups(applicationUser, plannerConfig.getAccessGroups(), true);
        return hasAccess;
    }

    public boolean shouldDisplay(ApplicationUser arg0, JiraHelper arg1) {
        return false;
    }
}
