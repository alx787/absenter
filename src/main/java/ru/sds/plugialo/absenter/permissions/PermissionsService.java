package ru.sds.plugialo.absenter.permissions;

import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import ru.sds.plugialo.absenter.model.User;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsService {
    @ComponentImport
    private final UserManager userManager;

    public PermissionsService(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean isSystemAdmin() {
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        UserKey userKey = new UserKey(currentUser.getKey());
        return this.userManager.isSystemAdmin(userKey);
    }

    public boolean isEditor(String profileUser, PluginSettingsFactory pluginSettingsFactory) {
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        UserKey userKey = new UserKey(currentUser.getKey());

        if (PlannerSecurityService.isAbsencePlannerEditor(currentUser.getUsername(), pluginSettingsFactory))
            return true;
        return (this.userManager.isSystemAdmin(userKey) || profileUser.equals(currentUser.getUsername()));
    }

    public boolean hasCreatePermission(ApplicationUser applicationUser, Project project) {
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(ProjectPermissions.CREATE_ISSUES, project, applicationUser);
    }

    public boolean isAssignableUser(ApplicationUser applicationUser, Project project) {
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(ProjectPermissions.CREATE_ISSUES, project, applicationUser);
    }

    public boolean isProjectAdmin(ApplicationUser applicationUser, Project project) {
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, applicationUser);
    }

    public boolean isSystemAdmin(ApplicationUser applicationUser) {
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

    public boolean isMemberOfDefinedGroups(ApplicationUser applicationUser, String groupNames, boolean allowEmptyGroups) {
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        if (groupNames == null || (groupNames.trim().length() <= 0 && allowEmptyGroups))
            return true;
        for (String groupName : groupNames.split(",")) {
            if (groupManager.isUserInGroup(applicationUser, groupName))
                return true;
        }
        return false;
    }

    public boolean hasBrowsePermission(ApplicationUser applicationUser, Project project) {
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, project, applicationUser);
    }

    public ArrayList<Project> getUserProjects(UserManager userManager, ApplicationUser applicationUser) {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        ArrayList<Project> userProjects = new ArrayList<>();
        for (Project project : projectManager.getProjects()) {
            if (isAssignableUser(applicationUser, project) &&
                    hasBrowsePermission(applicationUser, project))
                userProjects.add(project);
        }
        return userProjects;
    }

    public List<User> getRepresentatives(ApplicationUser applicationUser, String userName) {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        AssigneeService assigneeService = (AssigneeService) ComponentAccessor.getComponent(AssigneeService.class);
        ArrayList<User> representatives = new ArrayList<>();
        for (Project project : projectManager.getProjects()) {
            if (isAssignableUser(applicationUser, project) &&
                    hasBrowsePermission(applicationUser, project)) {
                Collection<ApplicationUser> users = assigneeService.findAssignableUsers(userName, project);
                ArrayList<User> usersList = new ArrayList<>();
                for (ApplicationUser user : users) {
                    User plannerUser = new User();
                    plannerUser.setUserKey(user.getKey());
                    plannerUser.setName(user.getUsername());
                    plannerUser.setDisplayName(user.getDisplayName());
                    usersList.add(plannerUser);
                }
                representatives.addAll(usersList);
            }
        }
        Set<String> nameSet = new HashSet<>();
        List<User> representativesFiltered = (List<User>) representatives.stream().filter(e -> nameSet.add(e.getName())).collect(Collectors.toList());
        return representativesFiltered;
    }

    public List<User> getAbsencesPossibleEditors(ApplicationUser applicationUser, String userName) {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        AssigneeService assigneeService = (AssigneeService) ComponentAccessor.getComponent(AssigneeService.class);
        ArrayList<User> absencesPossibleEditors = new ArrayList<>();
        for (Project project : projectManager.getProjects()) {
            Collection<ApplicationUser> users = assigneeService.findAssignableUsers(userName, project);
            ArrayList<User> usersList = new ArrayList<>();
            for (ApplicationUser user : users) {
                User plannerUser = new User();
                plannerUser.setUserKey(user.getKey());
                plannerUser.setName(user.getUsername());
                plannerUser.setDisplayName(user.getDisplayName());
                usersList.add(plannerUser);
            }
            absencesPossibleEditors.addAll(usersList);
        }
        Set<String> nameSet = new HashSet<>();
        List<User> absencesPossibleEditorsFilltered = (List<User>) absencesPossibleEditors.stream().filter(e -> nameSet.add(e.getName())).collect(Collectors.toList());
        return absencesPossibleEditorsFilltered;
    }
}
