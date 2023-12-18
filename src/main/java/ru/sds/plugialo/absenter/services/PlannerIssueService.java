package ru.sds.plugialo.absenter.services;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import ru.sds.plugialo.absenter.logger.LogService;

public class PlannerIssueService {
    public static boolean updateIssueAssignee(ApplicationUser applicationUser, Issue issue, ApplicationUser represetativeUser) {
        IssueService issueService = ComponentAccessor.getIssueService();
        IssueService.AssignValidationResult assignValidationResult = issueService.validateAssign(applicationUser, issue.getId(), represetativeUser.getUsername());
        if (assignValidationResult.isValid()) {
            IssueService.IssueResult issueResult = issueService.assign(applicationUser, assignValidationResult);
            if (issueResult != null)
                return true;
        } else {
            LogService.error("Assignment failed, we are not getting better with this function");
        }
        return false;
    }
}
