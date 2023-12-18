package ru.sds.plugialo.absenter.api;

import com.atlassian.activeobjects.tx.Transactional;
import ru.sds.plugialo.absenter.ao.AbsenceEntity;
import ru.sds.plugialo.absenter.model.UserAbsence;

import java.util.List;

//@Transactional
public interface AbsencePlannerService {
    AbsenceEntity add(UserAbsence paramUserAbsence);
    UserAbsence getById(int paramInt);
    boolean isEntryExist(String paramString1, String paramString2);
    UserAbsence getByAbsenceId(String paramString1, String paramString2);
    void deleteUserAbsenceByAbsenceId(String paramString1, String paramString2);
    UserAbsence createUserAbsence(UserAbsence paramUserAbsence);
    UserAbsence createNewAbsence(UserAbsence paramUserAbsence);
    UserAbsence createRecurringUserAbsence(UserAbsence paramUserAbsence);
    UserAbsence updateUserAbsence(int paramInt, long paramLong1, long paramLong2, boolean paramBoolean);
    UserAbsence updateRecurrentUserAbsence(UserAbsence paramUserAbsence);
    UserAbsence updateUserAbsence(UserAbsence paramUserAbsence);
    UserAbsence breakUserAbsence(int paramInt, boolean paramBoolean, String paramString);
    List<UserAbsence> getAllUserAbsencesForUser(String paramString);
    List<UserAbsence> getAllUserAbsences();
    List<UserAbsence> getAllUserAbsencesForDates(long paramLong1, long paramLong2);
    List<UserAbsence> getAllUserAbsencesForJQL(long paramLong1, long paramLong2);
    void clear(String paramString);
    List<AbsenceEntity> all();
    List<UserAbsence> getAllUserRegularAbsences();

    List<UserAbsence> getAllUserRegularAbsencesForUser(String paramString);
    void clearAll();
}
