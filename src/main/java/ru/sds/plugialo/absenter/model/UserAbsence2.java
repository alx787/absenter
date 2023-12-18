package ru.sds.plugialo.absenter.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.codehaus.jackson.annotate.JsonProperty;
import ru.sds.plugialo.absenter.utils.Utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class UserAbsence2 {
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
    private String startDate;

    @JsonProperty
    private String endDate;

    @JsonProperty
    private String user;

    @JsonProperty
    private String reportedBy;

    @JsonProperty
    private String date;

    @JsonProperty
    private boolean complete;

    @JsonProperty
    private boolean recurring;

    @JsonProperty
    private String recurringDays;

    public String getAbsenceId() {
        return this.absenceId;
    }

    public String getDate() {
        return this.date;
    }

    public int getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public String getReportedBy() {
        return this.reportedBy;
    }

    public String getSummary() {
        return this.summary;
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

    public LocalDate getUADate(long milliseconds) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.of(timeZone.getID()));
        return zDateTime.toLocalDate();
    }

    public String getUIFormattedSate(long milliseconds) {
        return Utils.getLocalDateFromEpoch(milliseconds);
    }
}
