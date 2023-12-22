package ru.sds.plugialo.absenter.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import ru.sds.plugialo.absenter.logger.LogService;
import ru.sds.plugialo.absenter.model.UserAbsence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

//import com.atlassian.jira.datetime.DateTimeFormatter;

public class Utils {
//    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
//    private final SimpleDateFormat simpleDateFormatStr2 = new SimpleDateFormat("yyyy-MM-dd");

//    ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

    public static String userPropIsOnline = "SG-PLANNER-ISONLINE";
    public static String userPropIsAssignee = "SG-PLANNER-ASSIGNEE";


    public static LocalDateTime getLocalDateTime(long milliseconds) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.of(timeZone.getID()));
        return zDateTime.toLocalDateTime();
    }

    public static LocalDateTime getLocalDateTimeNow() {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        LocalDateTime ldt = LocalDateTime.now(timeZone.toZoneId());
        return ldt;
    }

    public static String getDayOfJQL(long epochSecond) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochSecond(epochSecond).atZone(ZoneId.of(timeZone.getID()));
        return zDateTime.toLocalDateTime().getDayOfWeek().name();
    }

    public String getDayOfJQLForTest(long epochSecond) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochSecond(epochSecond).atZone(ZoneId.of(timeZone.getID()));
        JqlDateSupport jqld = (JqlDateSupport) ComponentAccessor.getComponent(JqlDateSupport.class);
        Date date = jqld.convertToDate(Long.valueOf(epochSecond));
        return zDateTime.toLocalDateTime().getDayOfWeek().toString();
    }

    public static LocalDate getLocalDate(long milliseconds) {
        TimeZoneManager userTimeZoneValue = (TimeZoneManager) ComponentAccessor.getComponent(TimeZoneManager.class);
        TimeZone timeZone = userTimeZoneValue.getLoggedInUserTimeZone();
        ZonedDateTime zDateTime = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.of(timeZone.getID()));
        return zDateTime.toLocalDate();
    }

    public static Instant getInstant(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        return instant;
    }

    public static boolean isUserOnline(ApplicationUser applicationUser) {
//        if (applicationUser != null) {
//            UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
//            PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
//            return ptSet.getBoolean(userPropIsOnline);
//        }
        return false;
    }

    public static boolean isUserAvailabilitySet(ApplicationUser applicationUser) {
//        if (applicationUser != null) {
//            UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
//            PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
//            if (ptSet.exists(userPropIsOnline))
//                return true;
//        }
        return false;
    }

    public static String getLocalDateFromEpoch(long date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
        String fortmattedDate = getJiraDateFormat(localDateTime.toLocalDate().toString());
        return fortmattedDate;
    }

    public static String getJiraDateFormat(String formattedDate) {
        com.atlassian.jira.datetime.DateTimeFormatter dateTimeFormatter = (ComponentAccessor.getComponent(com.atlassian.jira.datetime.DateTimeFormatter.class)).forLoggedInUser();
        LocalDate localDate = LocalDate.parse(formattedDate);
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        I18nHelper i18n = jiraAuthenticationContext.getI18nHelper();
        com.atlassian.jira.datetime.DateTimeFormatter dmyFormatter = dateTimeFormatter.withLocale(i18n.getLocale()).withStyle(DateTimeStyle.DATE);
        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        String format = applicationProperties.getDefaultBackedString("jira.date.picker.java.format");
        formattedDate = localDate.format(DateTimeFormatter.ofPattern(format, dmyFormatter.getLocale()));
        return formattedDate;
    }

    public static Date getDateFromLocalDate(LocalDate localDate) {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        return date;
    }

    public static String getJiraFormat() {
        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        String format = applicationProperties.getDefaultBackedString("jira.date.picker.java.format");
        return format;
    }

    public static void setUserAvailability(ApplicationUser applicationUser, boolean status) {
//        UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
//        PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
//        ptSet.setBoolean(userPropIsOnline, status);
    }

    public static String[] getRecurringDays(String days) {
        Calendar c = Calendar.getInstance();
        String recurringDays = "";
        for (String day : days.split(",")) {
            switch (day.toLowerCase()) {
                case "monday":
                    recurringDays = recurringDays + ",1";
                    break;
                case "tuesday":
                    recurringDays = recurringDays + ",2";
                    break;
                case "wednesday":
                    recurringDays = recurringDays + ",3";
                    break;
                case "thursday":
                    recurringDays = recurringDays + ",4";
                    break;
                case "friday":
                    recurringDays = recurringDays + ",5";
                    break;
                case "saturday":
                    recurringDays = recurringDays + ",6";
                    break;
                case "sunday":
                    recurringDays = recurringDays + ",7";
                    break;
            }
        }
        return recurringDays.split(",");
    }

    public static boolean isQueryInRegularAbsence(UserAbsence userAbsence, long jqlStart, long jqlEnd) {
        String recDays = userAbsence.getRecurringDays();
        for (String regDay : recDays.split(",")) {
            LocalDateTime ldt = LocalDateTime.ofEpochSecond(jqlStart, 0, ZoneOffset.of(ZoneOffset.UTC.getId()));
            LocalDateTime ldt2 = LocalDateTime.ofEpochSecond(jqlEnd, 0, ZoneOffset.of(ZoneOffset.UTC.getId()));
            if (jqlStart == jqlEnd &&
                    regDay.equalsIgnoreCase(ldt.getDayOfWeek().name()))
                return true;
            while (ldt.isBefore(ldt2)) {
                if (regDay.equalsIgnoreCase(ldt.getDayOfWeek().name()))
                    return true;
                ldt = ldt.plusDays(1L);
            }
        }
        return false;
    }

    public static String getTodayName() {
        LocalDate date = LocalDate.now();
        DayOfWeek dow = date.getDayOfWeek();
        return dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static LocalDate getRecurringDate(String day, int startEnd) {
        Calendar c = Calendar.getInstance();
        c.set(LocalDate.now().getYear(),
                LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
        switch (day.toLowerCase()) {
            case "monday":
                c.set(7, 2);
                break;
            case "tuesday":
                c.set(7, 3);
                break;
            case "wednesday":
                c.set(7, 4);
                break;
            case "thursday":
                c.set(7, 5);
                break;
            case "friday":
                c.set(7, 6);
                break;
            case "saturday":
                c.set(7, 7);
                break;
            case "sunday":
                c.set(7, 1);
                break;
        }
        if (startEnd == 1)
            return getLocalDate(getStartOfDay(c.getTime()).getTime());
        return getLocalDate(getEndOfDay(c.getTime()).getTime());
    }

    public static Date getRecurringEndDate(String day) {
        return null;
    }

    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.withHour(12);
        return getDate(endOfDay);
    }

    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return getDate(startOfDay);
    }

    public static LocalDateTime getStartOfDayAsLocalTime(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return startOfDay;
    }

    private static Date getDate(LocalDateTime startOfDay) {
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDateTime getLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    public static String getSelectedAssignee(ApplicationUser applicationUser) {
//        if (applicationUser != null)
//            try {
//                UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
//                PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
//                return ptSet.getString(userPropIsAssignee);
//            } catch (PropertyException pe) {
//                LogService.error(pe.getMessage());
//            }
        return null;
    }

    public static void setSelectedAssignee(ApplicationUser applicationUser, String userName) {
//        if (applicationUser != null)
//            try {
//                UserPropertyManager userPropertyManager = ComponentAccessor.getUserPropertyManager();
//                PropertySet ptSet = userPropertyManager.getPropertySetForUserKey(applicationUser.getKey());
//                ptSet.setString(userPropIsAssignee, userName);
//            } catch (PropertyException pe) {
//                LogService.error(pe.getMessage());
//            }
    }

    public static ApplicationUser getAlternateUser(ApplicationUser applicationUser) {
        String AlternateToken = getSelectedAssignee(applicationUser);
        String Alternate = null;
        if (AlternateToken != null && AlternateToken.contains(":")) {
            Alternate = AlternateToken.split(":")[0];
            ApplicationUser AlternateUser = getApplicationUser(Alternate);
            return AlternateUser;
        }
        return null;
    }

    public static boolean automaticAssignmentEnabled(ApplicationUser applicationUser) {
        String AlternateToken = getSelectedAssignee(applicationUser);
        if (AlternateToken != null && AlternateToken.contains(":") &&
                AlternateToken.split(":")[1].equals("true"))
            return true;
        return false;
    }

    public static boolean addCommentEnabled(ApplicationUser applicationUser) {
        String AlternateToken = getSelectedAssignee(applicationUser);
        if (AlternateToken != null && AlternateToken.contains(":") && (AlternateToken.split(":")).length == 3 &&
                AlternateToken.split(":")[2].equals("true"))
            return true;
        return false;
    }

    public static ApplicationUser getApplicationUser(String userName) {
        UserManager userManager = ComponentAccessor.getUserManager();
        ApplicationUser profileUser = userManager.getUserByName(userName);
        return profileUser;
    }

    public static long convertStringToDate(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            if (isThisDateValid(simpleDateFormat, date))
                return simpleDateFormat.parse(date).getTime();
//            if (isThisDateValid(this.simpleDateFormatStr2, date))
//                return this.simpleDateFormatStr2.parse(date).getTime();
        } catch (Exception exception) {
        }
        return 0L;
    }

    public static long convertStringToDate2(String date) {
        SimpleDateFormat simpleDateFormatStr2 = new SimpleDateFormat("yyyy-MM-dd");

        try {
            if (isThisDateValid(simpleDateFormatStr2, date))
                return simpleDateFormatStr2.parse(date).getTime();
        } catch (Exception exception) {
        }
        return 0L;
    }


    private static boolean isThisDateValid(SimpleDateFormat sdf, String dateToValidate) {
        if (dateToValidate == null)
            return false;
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateToValidate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
