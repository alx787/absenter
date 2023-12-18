package ru.sds.plugialo.absenter.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.java.ao.Query;
import net.java.ao.RawEntity;
import ru.sds.plugialo.absenter.ao.AbsenceEntity;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.logger.LogService;
import ru.sds.plugialo.absenter.model.UserAbsence;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

//import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
//import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

//@Scanned
@Transactional
@Named
public class AbsencePlannerServiceImpl implements AbsencePlannerService {

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public AbsencePlannerServiceImpl(ActiveObjects ao) {
//        this.ao = (ActiveObjects) Preconditions.checkNotNull(ao);
        this.ao = ao;
    }

    public AbsenceEntity add(UserAbsence userAbsence) {
        AbsenceEntity userAvailability = ao.create(AbsenceEntity.class, new net.java.ao.DBParam[0]);
        userAvailability.save();
        return userAvailability;
    }

    public List<AbsenceEntity> all() {
//        return Lists.newArrayList((Object[]) this.ao.find(AbsenceEntity.class));
        return Lists.newArrayList(ao.find(AbsenceEntity.class));
    }

    public UserAbsence getById(int id) {
        return null;
    }

    public boolean isEntryExist(String user, String absenceId) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("USER = ? AND ABSENCE_ID = ?", user, absenceId)));
        if (absenceEntities != null)
            return true;
        return false;
    }

    public UserAbsence getByAbsenceId(String user, String absenceId) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("USER = ? AND ABSENCE_ID = ?", user, absenceId)));
        if (absenceEntities != null)
            return new UserAbsence(absenceEntities.get(0));
        return null;
    }

    public UserAbsence updateUserAbsence(int id, long startDate, long endDate, boolean complete) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", id);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setComplete(complete);
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence breakUserAbsence(int id, boolean complete, String reportedBy) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", id);
        entity.setComplete(complete);
        entity.setReportedBy(reportedBy);
        entity.save();
        return new UserAbsence(entity);
    }

    public List<UserAbsence> getAllUserAbsencesForUser(String userName) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("USER = ?", userName).order("END_DATE DESC ")));
        List<UserAbsence> resultList = new ArrayList<>();
        int count = 0;
        for (AbsenceEntity entity : absenceEntities) {
            if (!entity.isRecurring() && count < 20) {
                resultList.add(new UserAbsence(entity));
                count++;
            }
        }
        return resultList;
    }

    public int getMaxForUSer(String userName) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("USER = ?", userName)));
        List<UserAbsence> resultList = new ArrayList<>();
        int max = 0;
        for (AbsenceEntity entity : absenceEntities) {
            resultList.add(new UserAbsence(entity));
            if (max < entity.getAbsenceId())
                max = entity.getAbsenceId();
        }
        return max;
    }

    public List<UserAbsence> getAllUserAbsences() {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absenceEntities)
            resultList.add(new UserAbsence(entity));
        return resultList;
    }

    public List<UserAbsence> getAllUserAbsencesForDates(long startDate, long endDate) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("START_DATE >= ? AND END_DATE <= ?", startDate, endDate)));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absenceEntities)
            resultList.add(new UserAbsence(entity));
        return resultList;
    }

    public List<UserAbsence> getAllUserAbsencesForJQL(long startDate, long endDate) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absenceEntities) {
            if (!entity.isRecurring()) {
                if (startDate / 1000L >= entity.getStartDate() / 1000L && endDate / 1000L <= entity.getEndDate() / 1000L)
                    resultList.add(new UserAbsence(entity));
                if (startDate / 1000L <= entity.getStartDate() / 1000L && endDate / 1000L >= entity.getEndDate() / 1000L)
                    resultList.add(new UserAbsence(entity));
                continue;
            }
            resultList.add(new UserAbsence(entity));
        }
        return resultList;
    }

    public List<UserAbsence> getAllUserRegularAbsences() {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("RECURRING=true")));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absenceEntities) {
            if (entity.isRecurring() == true) {
                UserAbsence userAbsence = new UserAbsence(entity);
                resultList.add(userAbsence);
            }
        }
        return resultList;
    }

    public List<UserAbsence> getAllUserRegularAbsencesForUser(String userName) {
        ArrayList<AbsenceEntity> absenceEntities = Lists.newArrayList(ao.find(AbsenceEntity.class, Query.select().where("USER like '" + userName + "'")));
        List<UserAbsence> resultList = new ArrayList<>();
        for (AbsenceEntity entity : absenceEntities) {
            if (entity.isRecurring() == true)
                resultList.add(new UserAbsence(entity));
        }
        return resultList;
    }

    public void deleteUserAbsenceByAbsenceId(String user, String absenceId) {
        AbsenceEntity[] absenceEntities = ao.find(AbsenceEntity.class, Query.select().where("USER = ? AND ABSENCE_ID = ?", user, absenceId));
        ao.delete(absenceEntities);
    }

    public void clear(String userName) {
        AbsenceEntity[] absences = ao.find(AbsenceEntity.class, Query.select().where("USER = ?", userName));
        ao.delete(absences);
    }

    protected <T extends RawEntity<K>, K> T getFirstByField(Class<T> type, String field, Object value) {
        if (value == null)
            return null;
        RawEntity[] arrayOfRawEntity = ao.find(type, Query.select().where(field + " = ?", value));
        if (arrayOfRawEntity.length > 0)
            return (T) arrayOfRawEntity[0];
        return null;
    }

    public UserAbsence createUserAbsence(UserAbsence userAbsence) {
        int absenceId = getMaxForUSer(userAbsence.getUser());
        AbsenceEntity entity = ao.create(AbsenceEntity.class, new net.java.ao.DBParam[0]);
        entity.setAbsenceId(absenceId + 1);
        entity.setUser(userAbsence.getUser());
        entity.setSummary(userAbsence.getSummary());
        entity.setType(userAbsence.getType());
        entity.setMessage(userAbsence.getMessage());
        entity.setStartDate(userAbsence.getStartDate());
        entity.setEndDate(userAbsence.getEndDate());
        entity.setReportedBy(userAbsence.getReportedBy());
        entity.setComplete(false);
        entity.setDate(userAbsence.getDate());
        entity.save();
        return new UserAbsence(entity);
    }

    long getMilliSeconds(String dateStr) {
        String[] formatStrings = {"yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy"};
        LocalDate date = null;
        for (String formatString : formatStrings) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
                date = LocalDate.parse(dateStr, formatter);
                break;
            } catch (DateTimeParseException e) {
                LogService.error(e.getMessage());
            }
        }
        if (date == null)
            throw new IllegalArgumentException("No known date format found: " + dateStr);
        Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long milliseconds = instant.toEpochMilli();
        LogService.error(String.valueOf(milliseconds));
        return milliseconds;
    }

    public UserAbsence createNewAbsence(UserAbsence userAbsence) {
        int absenceId = getMaxForUSer(userAbsence.getUser());
        AbsenceEntity entity = ao.create(AbsenceEntity.class, new net.java.ao.DBParam[0]);
        entity.setAbsenceId(absenceId + 1);
        entity.setUser(userAbsence.getUser());
        entity.setSummary(userAbsence.getSummary());
        entity.setType(userAbsence.getType());
        entity.setMessage(userAbsence.getMessage());
        entity.setStartDate(getMilliSeconds(String.valueOf(userAbsence.getStartDate())));
        entity.setEndDate(userAbsence.getEndDate());
        entity.setReportedBy(userAbsence.getReportedBy());
        entity.setComplete(false);
        entity.setDate(userAbsence.getDate());
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence createRecurringUserAbsence(UserAbsence userAbsence) {
        int absenceId = getMaxForUSer(userAbsence.getUser());
        AbsenceEntity entity = this.ao.create(AbsenceEntity.class, new net.java.ao.DBParam[0]);
        entity.setAbsenceId(absenceId + 1);
        entity.setUser(userAbsence.getUser());
        entity.setSummary(userAbsence.getSummary());
        entity.setType("recurrent");
        entity.setMessage(userAbsence.getMessage());
        entity.setStartDate(userAbsence.getStartDate());
        entity.setEndDate(userAbsence.getEndDate());
        entity.setReportedBy(userAbsence.getReportedBy());
        entity.setComplete(false);
        entity.setDate(userAbsence.getDate());
        entity.setRecurring(true);
        entity.setRecurringDays(userAbsence.getRecurringDays());
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence updateUserAbsence(UserAbsence userAbsence) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", Integer.valueOf(userAbsence.getId()));
        entity.setStartDate(userAbsence.getStartDate());
        entity.setEndDate(userAbsence.getEndDate());
        entity.setComplete(false);
        entity.setSummary(userAbsence.getSummary());
        entity.setMessage(userAbsence.getMessage());
        entity.setType(userAbsence.getType());
        entity.setReportedBy(userAbsence.getReportedBy());
        entity.save();
        return new UserAbsence(entity);
    }

    public UserAbsence updateRecurrentUserAbsence(UserAbsence userAbsence) {
        AbsenceEntity entity = getFirstByField(AbsenceEntity.class, "ID", Integer.valueOf(userAbsence.getId()));
        entity.setStartDate(userAbsence.getStartDate());
        entity.setEndDate(userAbsence.getEndDate());
        entity.setComplete(false);
        entity.setSummary(userAbsence.getSummary());
        entity.setMessage(userAbsence.getMessage());
        entity.setType(userAbsence.getType());
        entity.setReportedBy(userAbsence.getReportedBy());
        entity.setRecurring(true);
        entity.setRecurringDays(userAbsence.getRecurringDays());
        entity.save();
        return new UserAbsence(entity);
    }

    public void clearAll() {
        ao.deleteWithSQL(AbsenceEntity.class, "ID > ?", 0);
    }
}
