package ru.sds.plugialo.absenter.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.codehaus.jackson.annotate.JsonProperty;
import ru.sds.plugialo.absenter.ao.AbsenceEntity;
import ru.sds.plugialo.absenter.utils.Utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class UserAbsence {
    @JsonProperty
    private int id;

    @JsonProperty
    private String absenceId;

    @JsonProperty
    private String summary;

    @JsonProperty
    private String type;

    @JsonProperty
    private String message;

    @JsonProperty
    private long startDate;

    @JsonProperty
    private long endDate;

    @JsonProperty
    private String user;

    @JsonProperty
    private String reportedBy;

    @JsonProperty
    private long date;

    @JsonProperty
    private boolean complete;

    @JsonProperty
    private boolean recurring;

    @JsonProperty
    private String recurringDays;

    public UserAbsence() {
    }

    public UserAbsence(AbsenceEntity absenceEntity) {
        this.id = absenceEntity.getID();
        this.absenceId = String.valueOf(absenceEntity.getAbsenceId());
        this.summary = absenceEntity.getSummary();
        this.type = absenceEntity.getType();
        this.message = absenceEntity.getMessage();
        this.date = absenceEntity.getDate();
        this.startDate = absenceEntity.getStartDate();
        this.endDate = absenceEntity.getEndDate();
        this.user = absenceEntity.getUser();
        this.reportedBy = absenceEntity.getReportedBy();
        this.complete = absenceEntity.isComplete();
        if (absenceEntity.isRecurring()) {
            this.recurringDays = absenceEntity.getRecurringDays();
            setRecurring(true);
        }
    }

    public String getAbsenceId() {
        return this.absenceId;
    }

    public long getDate() {
        return this.date;
    }

    public long getEndDate() {
        return this.endDate;
    }

    public int getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getReportedBy() {
        return this.reportedBy;
    }

    public long getStartDate() {
        return this.startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAbsenceId(String absenceId) {
        this.absenceId = absenceId;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return this.type;
    }

    public String getUser() {
        return this.user;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isRecurring() {
        return this.recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurringDays() {
        return this.recurringDays;
    }

    public void setRecurringDays(String recurringDays) {
        this.recurringDays = recurringDays;
    }

    public String toString() {
        return "UserAbsence [absenceId=" + this.absenceId + ", complete=" + this.complete + ", date=" + this.date + ", endDate=" + this.endDate + ", id=" + this.id + " -Message=" + this.message + ", reportedBy=" + this.reportedBy + ", startDate=" + this.startDate + " -Summary=" + this.summary + ", type=" + this.type + ", user=" + this.user + "]";
    }

    public LocalDate getPlannerDate(long milliseconds) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.of(timeZone.getID()));
        return zDateTime.toLocalDate();
    }

    public String getUIFormattedSate(long milliseconds) {
        return Utils.getLocalDateFromEpoch(milliseconds);
    }
}
