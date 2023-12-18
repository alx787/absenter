package ru.sds.plugialo.absenter.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;
import net.java.ao.RawEntity;
import ru.sds.plugialo.absenter.ao.AbsenceEntity;
import ru.sds.plugialo.absenter.model.UserAbsence;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class UserAbsenceAO {
    protected ActiveObjects ao;

    public UserAbsenceAO(ActiveObjects ao) {
        this.ao = ao;
    }

    protected <T extends RawEntity<K>, K> T getFirstByField(Class<T> type, String field, Object value) {
        if (value == null)
            return null;
        RawEntity[] arrayOfRawEntity = this.ao.find(type, Query.select().where(field + " = ?", new Object[]{value}));
        if (arrayOfRawEntity.length > 0)
            return (T) arrayOfRawEntity[0];
        return null;
    }

    public UserAbsence getById(int id) {
        AbsenceEntity resultEntity = getFirstByField(AbsenceEntity.class, "ID", Integer.valueOf(id));
        if (resultEntity == null)
            return null;
        return new UserAbsence(resultEntity);
    }

    public boolean isEntryExist(String user, String absenceId) {
        AbsenceEntity[] resultEntity = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class,
                Query.select().where("USER = ? AND ABSENCE_ID = ?", new Object[]{user, absenceId}));
        return (resultEntity != null);
    }

    public UserAbsence getByAbsenceId(String user, String absenceId) {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class,
                Query.select().where("USER = ? AND ABSENCE_ID = ?", new Object[]{user, absenceId}));
        if (absences.length > 0)
            return new UserAbsence(absences[0]);
        return null;
    }

    public void deleteUserAbsenceByAbsenceId(String user, String absenceId) {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class,
                Query.select().where("USER = ? AND ABSENCE_ID = ?", new Object[]{user, absenceId}));
        this.ao.delete((RawEntity[]) absences);
    }

    public UserAbsence createUserAbsence(String absenceId, String user, String summary, String type, String message, long startDate, long endDate, String reportedBy) {
        AbsenceEntity entity = (AbsenceEntity) this.ao.create(AbsenceEntity.class, new net.java.ao.DBParam[0]);
        entity.setAbsenceId(Integer.parseInt(absenceId));
        entity.setUser(user);
        entity.setSummary(summary);
        entity.setType(type);
        entity.setMessage(message);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setReportedBy(reportedBy);
        entity.setComplete(false);
        long year = LocalDateTime.now().getLong(ChronoField.YEAR);
        entity.setDate(year);
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence updateUserAbsence(int id, long startDate, long endDate, boolean complete) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", Integer.valueOf(id));
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setComplete(complete);
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence breakUserAbsence(int id, boolean complete, String reportedBy) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", Integer.valueOf(id));
        entity.setComplete(complete);
        entity.setReportedBy(reportedBy);
        entity.save();
        return new UserAbsence(entity);
    }

    public List<UserAbsence> getAllUserAbsencesForUser(String userName) {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class,
                Query.select().where("USER = ?", new Object[]{userName}));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absences)
            resultList.add(new UserAbsence(entity));
        return resultList;
    }

    public List<UserAbsence> getAllUserAbsences() {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class);
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absences)
            resultList.add(new UserAbsence(entity));
        return resultList;
    }

    public void clear(String userName) {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class, Query.select().where("USER = ?", new Object[]{userName}));
        this.ao.delete((RawEntity[]) absences);
    }

    public List<UserAbsence> getAllUserAbsencesForDates(long startDate, long endDate) {
        AbsenceEntity[] absences = (AbsenceEntity[]) this.ao.find(AbsenceEntity.class, Query.select().where("START_DATE <= ? AND END_DATE >= ?", new Object[]{Long.valueOf(endDate), Long.valueOf(startDate)}));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absences)
            resultList.add(new UserAbsence(entity));
        return resultList;
    }

    public void test() {
    }
}
