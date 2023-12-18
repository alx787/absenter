package ru.sds.plugialo.absenter.jql;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.google.common.base.Strings;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.model.UserAbsence;
import ru.sds.plugialo.absenter.utils.Utils;

import javax.annotation.Nonnull;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class AbsentUsersJQL extends AbstractJqlFunction {
    private final AbsencePlannerService absencePlannerService;

    private final SimpleDateFormat simpleDateFormat;

    private final SimpleDateFormat simpleDateFormat2;

    @Nonnull
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSetImpl messageSetImpl = new MessageSetImpl();
        if (functionOperand.getArgs().size() == 1) {
            String startDateAsString = functionOperand.getArgs().get(0);
            if (Strings.isNullOrEmpty(startDateAsString)) {
                messageSetImpl.addErrorMessage(
                        formatMessage("Date format in '%s' is empty. Should be a date in format 'yyyy/MM/dd' or 'yyyy-MM-dd'", new String[]{getFunctionName()}));
                return (MessageSet) messageSetImpl;
            }
            Date startDate = convertStringToDate(startDateAsString);
            if (startDate == null) {
                messageSetImpl
                        .addErrorMessage(formatMessage("Date format in '%s' is invalid. Should be 'yyyy/MM/dd' or 'yyyy-MM-dd'", new String[]{getFunctionName()}));
                return (MessageSet) messageSetImpl;
            }
        }
        if (functionOperand.getArgs().size() == 2) {
            String startDateAsString = functionOperand.getArgs().get(0);
            String endDateAsString = functionOperand.getArgs().get(1);
            if (Strings.isNullOrEmpty(startDateAsString) || Strings.isNullOrEmpty(startDateAsString)) {
                messageSetImpl.addErrorMessage(
                        formatMessage("Date format in '%s' is empty. Should be a date in format 'yyyy/MM/dd' or 'yyyy-MM-dd'", new String[]{getFunctionName()}));
                return (MessageSet) messageSetImpl;
            }
            Date startDate = convertStringToDate(startDateAsString);
            Date endDate = convertStringToDate(endDateAsString);
            if (startDate == null || endDate == null) {
                messageSetImpl
                        .addErrorMessage(formatMessage("Date format in '%s' is invalid. Should be 'yyyy/MM/dd' or 'yyyy-MM-dd'", new String[]{getFunctionName()}));
                return (MessageSet) messageSetImpl;
            }
            if (startDate.after(endDate))
                messageSetImpl
                        .addErrorMessage(formatMessage("Range of Date is reversed!", new String[]{getFunctionName()}));
        }
        if (functionOperand.getArgs().size() > 2) {
            messageSetImpl.addErrorMessage(
                    formatMessage("Too many operands in '%s'. Should be %s('yyyy/MM/dd') or %s('yyyy/MM/dd', 'yyyy/MM/dd')", new String[]{getFunctionName(), getFunctionName(), getFunctionName()}));
            return (MessageSet) messageSetImpl;
        }
        return (MessageSet) messageSetImpl;
    }

    @Nonnull
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        long startArgInSeconds = 0L;
        long endArgInSeconds = 0L;
        List<QueryLiteral> literals = new LinkedList<>();
        List<UserAbsence> userAbsences = null;
        long startDate = getStartOfDay(new Date()).getTime() / 1000L;
        long endDate = getEndOfDay(new Date()).getTime() / 1000L;
        if (functionOperand.getArgs().size() == 1) {
            String startDateAsString = functionOperand.getArgs().get(0);
            startDate = getStartOfDay(convertStringToDate(startDateAsString)).getTime() / 1000L;
            startArgInSeconds = getStartOfDay(convertStringToDate(startDateAsString)).getTime();
            endDate = getEndOfDay(convertStringToDate(startDateAsString)).getTime() / 1000L;
            endArgInSeconds = getEndOfDayInSeconds(convertStringToDate(startDateAsString)) * 1000L;
        }
        if (functionOperand.getArgs().size() == 0) {
            LocalDateTime localTime = LocalDateTime.now();
            ZoneId zoneId = ZoneId.systemDefault();
            long epochStart = localTime.atZone(zoneId).toEpochSecond();
            LocalDateTime endOfDay = localTime.with(LocalTime.NOON);
            long endOfTheDay = endOfDay.toEpochSecond(ZoneOffset.MAX);
            startArgInSeconds = epochStart * 1000L;
            endArgInSeconds = endOfTheDay * 1000L;
        }
        if (functionOperand.getArgs().size() == 2) {
            String startDateAsString = functionOperand.getArgs().get(0);
            String endDateAsString = functionOperand.getArgs().get(1);
            startArgInSeconds = getStartOfDay(convertStringToDate(startDateAsString)).getTime();
            endArgInSeconds = getEndOfDayInSeconds(convertStringToDate(endDateAsString)) * 1000L;
            startDate = getStartOfDay(convertStringToDate(startDateAsString)).getTime() / 1000L;
            endArgInSeconds = getEndOfDay(convertStringToDate(endDateAsString)).getTime();
        }
        userAbsences = this.absencePlannerService.getAllUserAbsencesForJQL(startArgInSeconds, endArgInSeconds);
        HashSet<String> userNames = new HashSet<>();

        for (UserAbsence userAbsence : userAbsences) {
            if (userAbsence.isRecurring()) {
                if (functionOperand.getArgs().size() == 0) {
                    LocalDate currentDate = LocalDate.now();
                    DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                    String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    LocalDate date = LocalDate.now();
                    Format f = new SimpleDateFormat("EEEE");
                    String str = f.format(new Date());
                    ZoneId zoneId = ZoneId.systemDefault();
                    long epoch = date.atStartOfDay(zoneId).toEpochSecond();
                    if (userAbsence.getRecurringDays().contains(dayName)) {
                        userNames.add(userAbsence.getUser());
                        literals.add(new QueryLiteral((Operand) functionOperand, userAbsence.getUser()));
                    }
                    continue;
                }
                if (Utils.isQueryInRegularAbsence(userAbsence, startDate, endDate)) {
                    userNames.add(userAbsence.getUser());
                    literals.add(new QueryLiteral((Operand) functionOperand, userAbsence.getUser()));
                }
                continue;
            }
            int compStart = Long.compare(userAbsence.getStartDate() / 1000L, startDate);
            int compEnd = Long.compare(endDate, userAbsence.getEndDate() / 1000L);
            userNames.add(userAbsence.getUser());
            literals.add(new QueryLiteral((Operand) functionOperand, userAbsence.getUser()));
            if (functionOperand.getArgs().size() == 0) {
                LocalDate date = LocalDate.now();
                ZoneId zoneId = ZoneId.systemDefault();
                long epoch = date.atStartOfDay(zoneId).toEpochSecond();
                compEnd = Long.compare(userAbsence.getEndDate() / 1000L, epoch);
                compStart = Long.compare(epoch, userAbsence.getStartDate() / 1000L);
                if (compStart >= 0 && compEnd >= 0) {
                    userNames.add(userAbsence.getUser());
                    literals.add(new QueryLiteral((Operand) functionOperand, userAbsence.getUser()));
                }
            }
        }
        return literals;
    }

    private String formatMessage(String message, String... args) {
        String result = String.format("[%s]: ", new Object[]{getFunctionName()});
        return result + String.format(message, (Object[]) args);
    }

    private Date convertStringToDate(String date) {
        try {
            if (isThisDateValid(this.simpleDateFormat, date))
                return this.simpleDateFormat.parse(date);
            if (isThisDateValid(this.simpleDateFormat2, date))
                return this.simpleDateFormat2.parse(date);
        } catch (Exception exception) {
        }
        return null;
    }

    private boolean isThisDateValid(SimpleDateFormat sdf, String dateToValidate) {
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

    public int getMinimumNumberOfExpectedArguments() {
        return 0;
    }

    @Nonnull
    public JiraDataType getDataType() {
        return JiraDataTypes.USER;
    }

    public AbsentUsersJQL(AbsencePlannerService absencePlannerService) {
        this.simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        this.simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        this.absencePlannerService = absencePlannerService;
    }

    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return getDate(endOfDay);
    }

    public static long getEndOfDayInSeconds(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.NOON);
        return endOfDay.toEpochSecond(ZoneOffset.MAX);
    }

    public static long getEndOfDayInSeconds2(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return endOfDay.toEpochSecond(ZoneOffset.MAX);
    }

    public static long getStartfDayInSeconds(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MIN);
        return endOfDay.toEpochSecond(ZoneOffset.MAX);
    }

    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = getLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return getDate(startOfDay);
    }

    private static Date getDate(LocalDateTime startOfDay) {
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDateTime getLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }
}
