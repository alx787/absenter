package ru.sds.plugialo.absenter.services;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class PlannerDateComparator implements Comparator<String> {
    private final DateTimeFormatter dateFormatter;

    public PlannerDateComparator(String dateFormatPattern) {
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();
        this.dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern, i18nHelper.getLocale());
    }

    public int compare(String dateString1, String dateString2) {
        LocalDate date1 = LocalDate.parse(dateString1, this.dateFormatter);
        LocalDate date2 = LocalDate.parse(dateString2, this.dateFormatter);
        return date1.compareTo(date2);
    }
}
