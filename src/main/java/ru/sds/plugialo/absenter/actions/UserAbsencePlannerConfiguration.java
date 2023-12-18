package ru.sds.plugialo.absenter.actions;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ru.sds.plugialo.absenter.model.PlannerConfig;
import ru.sds.plugialo.absenter.model.User;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

//@Scanned
@Named
@SupportedMethods({RequestMethod.GET})
public class UserAbsencePlannerConfiguration extends JiraWebActionSupport {
    final PluginSettingsFactory pluginSettingsFactory;

    private static final long serialVersionUID = 1L;

    @Inject
    public UserAbsencePlannerConfiguration(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public String doDefault() throws Exception {
        return "input";
    }

    protected String doExecute() throws Exception {
        return "input";
    }

    public PlannerConfig getCurrentSecuritySettings() {
        return PlannerSecurityService.getCurrentSettings(this.pluginSettingsFactory);
    }

    protected void doValidation() {
        super.doValidation();
    }

    public ArrayList<String> getGroups() {
        GroupPickerSearchService gpss = (GroupPickerSearchService) ComponentAccessor.getComponent(GroupPickerSearchService.class);
        List<Group> groups = gpss.findGroups("");
        ArrayList<String> groupNames = new ArrayList<>();
        for (Group group : groups)
            groupNames.add(group.getName());
        return groupNames;
    }

    public String getIconURL() {
        String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
        return baseUrl;
    }

    public User getPermittedUser(String userName) {
        UserManager um = ComponentAccessor.getUserManager();
        User plannerUser = null;
        ApplicationUser appUser = um.getUserByName(userName);
        if (appUser != null) {
            plannerUser = new User();
            plannerUser.setUserKey(appUser.getKey());
            plannerUser.setName(appUser.getUsername());
            plannerUser.setDisplayName(appUser.getDisplayName());
        }
        return plannerUser;
    }
}
