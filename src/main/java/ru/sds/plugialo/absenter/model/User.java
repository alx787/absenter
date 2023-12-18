package ru.sds.plugialo.absenter.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class User {
    @JsonProperty
    String userKey;

    @JsonProperty
    String name;

    @JsonProperty
    String displayName;

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return this.userKey;
    }

    public void setName(String userName) {
        this.name = userName;
    }

    public String getName() {
        return this.name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
