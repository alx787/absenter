package ru.sds.plugialo.absenter.model;

import ru.sds.plugialo.absenter.logger.LogService;
import org.codehaus.jackson.annotate.JsonProperty;

public class PlannerConfig {
    @JsonProperty
    boolean diableAutoComment = false;

    @JsonProperty
    boolean enabledForSystemAdmins = false;

    @JsonProperty
    String groups;

    @JsonProperty
    String users;

    @JsonProperty
    String accessGroups;

    @JsonProperty
    String accessUsers;

    public PlannerConfig() {
    }

    public PlannerConfig(boolean diableAutoComment, boolean enabledForSystemAdmins) {
        this.diableAutoComment = diableAutoComment;
        this.enabledForSystemAdmins = enabledForSystemAdmins;
    }

    public void setDisableAutoComment(boolean enableAutoComment) {
        this.diableAutoComment = enableAutoComment;
    }

    public boolean isDisableAutoComment() {
        return this.diableAutoComment;
    }

    public void setEnabledForSystemAdmins(boolean enabledForSystemAdmins) {
        this.enabledForSystemAdmins = enabledForSystemAdmins;
    }

    public boolean isEnabledForSystemAdmins() {
        return this.enabledForSystemAdmins;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getGroups() {
        return this.groups;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getUsers() {
        return this.users;
    }

    public void setAccessGroups(String accessGroups) {
        this.accessGroups = accessGroups;
    }

    public void setAccessUsers(String accessUsers) {
        this.accessUsers = accessUsers;
    }

    public String getAccessGroups() {
        return this.accessGroups;
    }

    public String getAccessUsers() {
        return this.accessUsers;
    }

    public String toString() {
        String print = "UAP: Settings of Admin Page:  -> disableAutoComment:" + this.diableAutoComment + " \n SysAdmins:" + this.enabledForSystemAdmins + "| Groups:" + this.groups + " | AccessGroups:" + this.accessGroups;
        print = print + " Users:" + this.users;
        LogService.error(print);
        return print;
    }
}
