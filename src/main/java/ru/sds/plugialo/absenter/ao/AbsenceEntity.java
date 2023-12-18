package ru.sds.plugialo.absenter.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("AbsencePlan")
public interface AbsenceEntity extends Entity {
    void setAbsenceId(int paramInt);
    int getAbsenceId();

    void setSummary(String paramString);
    String getSummary();

    void setMessage(String paramString);
    String getMessage();

    void setType(String paramString);
    String getType();

    void setStartDate(long paramLong);
    long getStartDate();

    void setEndDate(long paramLong);
    long getEndDate();

    String getUser();
    void setUser(String paramString);

    String getReportedBy();
    void setReportedBy(String paramString);

    void setDate(long paramLong);
    long getDate();

    void setComplete(boolean paramBoolean);
    boolean isComplete();

    boolean isRecurring();
    void setRecurring(boolean paramBoolean);

    String getRecurringDays();
    void setRecurringDays(String paramString);

    void clearAll();
}
