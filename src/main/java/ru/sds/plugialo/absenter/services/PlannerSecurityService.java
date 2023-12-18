package ru.sds.plugialo.absenter.services;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.codehaus.jackson.map.ObjectMapper;
import ru.sds.plugialo.absenter.logger.LogService;
import ru.sds.plugialo.absenter.model.PlannerConfig;

import java.io.IOException;

public class PlannerSecurityService {

    public static boolean isSystemAdmin(ApplicationUser applicationUser) {
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        for (ApplicationUser appUser : userUtil.getJiraSystemAdministrators()) {
            if (appUser.getKey().equals(applicationUser.getKey()))
                return true;
        }
        for (ApplicationUser appUser : userUtil.getJiraAdministrators()) {
            if (appUser.getKey().equals(applicationUser.getKey()))
                return true;
        }
        return false;
    }

    public static boolean isMemberOfDefinedGroups(ApplicationUser applicationUser, String groupNames, boolean allowEmptyGroups) {
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        if (groupNames == null || (groupNames.trim().length() <= 0 && allowEmptyGroups))
            return true;
        for (String groupName : groupNames.split(",")) {
            if (groupManager.isUserInGroup(applicationUser, groupName))
                return true;
        }
        return false;
    }

    public static boolean isMemberOfAccessGroups(ApplicationUser applicationUser, String groupNames, boolean allowEmptyGroups) {
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        if (groupNames == null || (groupNames.trim().length() <= 0 && allowEmptyGroups))
            return true;
        for (String groupName : groupNames.split(",")) {
            if (groupManager.isUserInGroup(applicationUser, groupName))
                return true;
        }
        return false;
    }

    public static boolean hasAccess(PluginSettingsFactory pluginSettingsFactory, ApplicationUser applicationUser) {
        PlannerConfig plannerConfig = getCurrentSettings(pluginSettingsFactory);
        boolean hasAccess = isMemberOfAccessGroups(applicationUser, plannerConfig.getAccessGroups(), true);
        return hasAccess;
    }

    public static PlannerConfig getCurrentSettings(PluginSettingsFactory pluginSettingsFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        PlannerConfig plannerConfig = new PlannerConfig();
        Object settingsObject = pluginSettingsFactory.createGlobalSettings().get("sds-planner-admin-sec-settings");
        if (settingsObject != null) {
            String retJson = (String) pluginSettingsFactory.createGlobalSettings().get("sds-planner-admin-sec-settings");
            try {
                plannerConfig = (PlannerConfig) objectMapper.readValue(retJson, PlannerConfig.class);
            } catch (IOException e) {
                LogService.error("[Absence Planner-ERROR]--- Error in retriving the PlannerSettings");
                LogService.error(e.getMessage());
            }
        }
        return plannerConfig;
    }

    public static void setPlannerSecuritySettings(PlannerConfig plannerConfig, PluginSettingsFactory pluginSettingsFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonValue = objectMapper.writeValueAsString(plannerConfig);
            pluginSettingsFactory.createGlobalSettings().put("sds-planner-admin-sec-settings", jsonValue);
            plannerConfig.toString();
            PlannerConfig plannerConfig2 = getCurrentSettings(pluginSettingsFactory);
            plannerConfig2.toString();
        } catch (IOException e) {
            LogService.error("ABSENCE-PLANNER Error in Converting settings to Json:" + e.getMessage());
        }
    }

    public static boolean isAbsencePlannerEditor(String userName, PluginSettingsFactory pluginSettingsFactory) {
        PlannerConfig plannerConfig = getCurrentSettings(pluginSettingsFactory);
        if (plannerConfig != null && plannerConfig.getUsers() != null)
            for (String user : plannerConfig.getUsers().split(",")) {
                if (user.equalsIgnoreCase(userName))
                    return true;
            }
        return false;
    }

    public static boolean isUserAbsencePlannerEditor(ApplicationUser applicationUser, PluginSettingsFactory pluginSettingsFactory, boolean isSysAdmin) {
        PlannerConfig plannerConfig = getCurrentSettings(pluginSettingsFactory);
        String userName = applicationUser.getUsername();
        if (plannerConfig != null && plannerConfig.getUsers() != null) {
            if (plannerConfig.isEnabledForSystemAdmins() && isSysAdmin)
                return true;
            if (plannerConfig.getGroups() != null && (plannerConfig.getGroups().split(",")).length > 0 &&
                    isMemberOfDefinedGroups(applicationUser, plannerConfig.getGroups(), false))
                return true;
            for (String user : plannerConfig.getUsers().split(",")) {
                if (user.equalsIgnoreCase(userName))
                    return true;
            }
        }
        return false;
    }
}
