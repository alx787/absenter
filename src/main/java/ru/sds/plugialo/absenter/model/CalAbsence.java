package ru.sds.plugialo.absenter.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class CalAbsence {
    @JsonProperty
    String id;

    @JsonProperty
    String title;

    @JsonProperty
    long start;

    @JsonProperty
    long end;

    @JsonProperty
    String[] daysOfWeek;

    public void setEnd(long end) {
        this.end = end;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getEnd() {
        return this.end;
    }

    public long getStart() {
        return this.start;
    }

    public String getTitle() {
        return this.title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setDaysOfWeek(String[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String[] getDaysOfWeek() {
        return this.daysOfWeek;
    }
}
