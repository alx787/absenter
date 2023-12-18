package ru.sds.plugialo.absenter.services;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.user.ApplicationUser;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.model.UserAbsence;
import ru.sds.plugialo.absenter.utils.Utils;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class AbsenceService {
    public boolean isAbsenceOn(long onDate, UserAbsence userAbsence) {
        LocalDate now = userAbsence.getPlannerDate(onDate);
        LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
        LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());
        if ((start.getYear() == now.getYear() && now.getYear() < end.getYear()) || (now
                .getDayOfYear() >= start.getDayOfYear() && now.getDayOfYear() <= end.getDayOfYear()))
            return true;
        return false;
    }

    public boolean isAbsenceBetween(long startDate, long endDate, UserAbsence userAbsence) {
        LocalDate startOn = userAbsence.getPlannerDate(startDate);
        LocalDate endOn = userAbsence.getPlannerDate(startDate);
        LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
        LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());
        if ((start.getYear() == startOn.getYear() && startOn.getYear() < end.getYear()) || (startOn
                .getDayOfYear() >= start.getDayOfYear() && startOn.getDayOfYear() <= end.getDayOfYear()))
            if ((end.getYear() == endOn.getYear() && endOn.getYear() > start.getYear()) || (endOn
                    .getDayOfYear() >= end.getDayOfYear() && endOn.getDayOfYear() <= start.getDayOfYear()))
                return true;
        return false;
    }

    public static boolean isUserAbsenceToday(AbsencePlannerService absencePlannerService, String userName) {
        ApplicationUser applicationUser = Utils.getApplicationUser(userName);
        if (applicationUser != null) {
            for (UserAbsence userAbsence : absencePlannerService.getAllUserAbsencesForUser(userName)) {
                LocalDate now = LocalDate.now();
                LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
                LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());
                if (now.compareTo(start) >= 0 && now.compareTo(end) <= 0)
                    return true;
            }
            List<UserAbsence> userAbsences = absencePlannerService.getAllUserRegularAbsencesForUser(applicationUser.getUsername());
            for (UserAbsence userAbsence : userAbsences) {
                if (userAbsence.isRecurring()) {
                    String days = userAbsence.getRecurringDays();
                    if (days != null && days.contains(Utils.getTodayName()))
                        return true;
                }
            }
        }
        return false;
    }

    public static void commentOnIssue(ApplicationUser applicationUser, Issue issue, String body) {
        CommentManager commentManager = ComponentAccessor.getCommentManager();
        commentManager.create(issue, applicationUser, body, null, null, new Date(), null, true);
    }

    public static String getAbsencesMessage(AbsencePlannerService absencePlannerService, String userName, long date) {
        List<UserAbsence> absencesList = absencePlannerService.getAllUserAbsencesForUser(userName);
        for (UserAbsence userAbsence : absencesList) {
            LocalDate now = LocalDate.now();
            LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
            LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());
            if (start.getYear() == now.getYear() && now.getYear() <= end.getYear() && now
                    .getDayOfYear() >= start.getDayOfYear() && now.getDayOfYear() <= end.getDayOfYear()) {
                if (userAbsence.getMessage() != null && userAbsence.getMessage().length() > 2)
                    return userAbsence.getMessage();
                return " I am out of office";
            }
        }
        for (UserAbsence userAbsence : absencePlannerService.getAllUserRegularAbsencesForUser(userName)) {
            if (userAbsence.isRecurring()) {
                String days = userAbsence.getRecurringDays();
                if (days != null && days.contains(Utils.getTodayName())) {
                    if (userAbsence.getMessage() != null && userAbsence.getMessage().length() > 2)
                        return userAbsence.getMessage();
                    return " I am out of office";
                }
            }
        }
        return " I am out of office";
    }
}
