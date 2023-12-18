package ru.sds.plugialo.absenter.rest;

import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sds.plugialo.absenter.api.AbsencePlannerService;
import ru.sds.plugialo.absenter.logger.LogService;
import ru.sds.plugialo.absenter.model.*;
import ru.sds.plugialo.absenter.permissions.PermissionsService;
import ru.sds.plugialo.absenter.services.PlannerSecurityService;
import ru.sds.plugialo.absenter.utils.Utils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
//import com.atlassian.jira.user.util.UserManager;

//@UnrestrictedAccess
@Path("/")
public class UserAbsencePlannerRest {

    static Logger log = LoggerFactory.getLogger(UserAbsencePlannerRest.class);

    //    @ComponentImport
    private I18nResolver i18n;

    private final AbsencePlannerService absencePlannerService;

    @ComponentImport
    private final UserManager userManager;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

//    @ComponentImport
//    private final ActiveObjects ao;

    //    @Autowired
    @Inject
    public void setI18nResolver(I18nResolver i18n) {
        this.i18n = i18n;
    }

    @Inject
    public UserAbsencePlannerRest(AbsencePlannerService absencePlannerService, UserManager userManager, PluginSettingsFactory pluginSettingsFactory) {
        this.absencePlannerService = absencePlannerService;
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @POST
//    @UnrestrictedAccess
    @Path("refreshAllUsersAbsence")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response refreshAllUsersAbsence(List<UserAbsence2> userAbsenceList) {

        // на входе будет такой объект, его нужно будет преобразовать в UserAbsence
        // изменить только поля date startDate endDate
//        {
//            "user": "admin"
//            "reportedBy": "admin",
//            "summary": "отсутствие какое то",
//            "type": "Business Trip",
//            "message": "сообщение",
//            "recurring": false,
//            "date": "2023-12-15",
//            "startDate": "2023-12-15",
//            "endDate": "2023-12-15",
//        },

        absencePlannerService.clearAll();

        String resultString = "";

        for (UserAbsence2 userAbsence2 : userAbsenceList) {

//            log.warn(userAbsence2.toString());

            UserAbsence userAbsence = new UserAbsence();
            userAbsence.setUser(userAbsence2.getUser());
            userAbsence.setReportedBy(userAbsence2.getReportedBy());
            userAbsence.setSummary(userAbsence2.getSummary());
            userAbsence.setType(userAbsence2.getType());
            userAbsence.setMessage(userAbsence2.getType());
            userAbsence.setMessage(userAbsence2.getMessage());
            userAbsence.setRecurring(false);

            long dateMls = Utils.convertStringToDate2(userAbsence2.getDate());
            long startDateMls = Utils.convertStringToDate2(userAbsence2.getStartDate());
            long endDateMls = Utils.convertStringToDate2(userAbsence2.getEndDate());

            userAbsence.setDate(dateMls);
            userAbsence.setStartDate(startDateMls);
            userAbsence.setEndDate(endDateMls);

            UserAbsence userAbsenceResult = absencePlannerService.createUserAbsence(userAbsence);

//            log.warn(userAbsenceResult.toString());

            if (userAbsenceResult == null) {
                resultString = resultString + "Error when loading absence on " + userAbsence2.getUser()
                                            + " from " + userAbsence2.getStartDate()
                                            + " to "  + userAbsence2.getEndDate()
                                            + " type "  + userAbsence2.getType()
                                            + "::::";
            } else {

            }

        }

        String jsonString = "";
        if (resultString.equals("")) {
            jsonString = "{\"status\":\"ok\", \"description\":\"load " + String.valueOf(userAbsenceList.size()) + " absences\"}";
        } else {
            jsonString = "{\"status\":\"error\", \"description\":\"" + resultString + "\"}";
        }

//        return Response.ok("{\"status\":\"ok\", \"description\":\"task deleted\"}").build();
        return Response.ok(jsonString).build();
    }


    @GET
    @Path("isUserAvailable")
    public Response isUserAvailable(@QueryParam("from") String from, @QueryParam("to") String to) {
        return Response.ok("No :)").build();
    }

    @POST
    @Path("setUserAvailability")
    public Response setUserAvailability(@QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("type") String type, @QueryParam("message") String userMessage, @Nullable @QueryParam("user") String userName) {
        return Response.ok("No :)").build();
    }

    @POST
    @Path("addUserAbsence")
    @Consumes({"application/json"})
    public Response addUserAbsence(UserAbsence userAbsence) {
        String result = "";
        if (userAbsence.isRecurring()) {
            absencePlannerService.createRecurringUserAbsence(userAbsence);
            result = "Success";
        } else {
            absencePlannerService.createUserAbsence(userAbsence);
            result = "Success";
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("addNewAbsence")
    @Consumes({"application/json"})
    public Response addNewAbsence(UserAbsence2 userAbsenceModel) {
        String result = "";
        String startDate = userAbsenceModel.getStartDate();
        String endDate = userAbsenceModel.getEndDate();
        long startDateMls = Utils.convertStringToDate(startDate);
        long endDateMls = Utils.convertStringToDate(endDate);
        UserAbsence userAbsence = new UserAbsence();
        userAbsence.setEndDate(endDateMls);
        userAbsence.setRecurring(false);
        userAbsence.setStartDate(startDateMls);
        userAbsence.setAbsenceId(userAbsenceModel.getAbsenceId());
        userAbsence.setSummary(userAbsenceModel.getSummary());
        userAbsence.setDate((new Date()).getTime());
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        userAbsence.setReportedBy(currentUser.getName());
        userAbsence.setType(userAbsenceModel.getType());
        userAbsence.setUser(userAbsenceModel.getUser());
        userAbsence.setMessage(userAbsenceModel.getMessage());
        UserAbsence userAbsenceResult = absencePlannerService.createUserAbsence(userAbsence);
        if (userAbsenceResult != null) {
            LogService.error(userAbsenceResult.toString());
            result = "Success";
        } else {
            result = "failed";
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("updateUserAvailability")
    @Consumes({"application/json"})
    public Response updateUserAvailability(UserAbsence userAbsence) {
        String result = "";
        if (userAbsence.isRecurring()) {
            UserAbsence userAbsence1 = absencePlannerService.updateRecurrentUserAbsence(userAbsence);
        } else {
            UserAbsence userAbsence1 = absencePlannerService.updateUserAbsence(userAbsence);
        }
        result = "Success";
        return Response.ok(result).build();
    }

    @DELETE
    @Path("cancelAbsenceEntry")
    public Response cancelAbsenceEntry(@QueryParam("user") String user, @QueryParam("absenceId") String absenceId) {
        absencePlannerService.deleteUserAbsenceByAbsenceId(user, absenceId);
        return Response.ok().build();
    }

    @GET
    @Path("loadAbsenceHistory")
    public Response loadAbsenceHistory(@QueryParam("userName") String userName) {
        VelocityParamFactory velocityParamFactory = ComponentAccessor.getVelocityParamFactory();
        Map<String, Object> contextParameters = velocityParamFactory.getDefaultVelocityParams();
        contextParameters.put("userAbsencesList", absencePlannerService.getAllUserAbsencesForUser(userName));

        List<UserAbsence> regularAbsences = absencePlannerService.getAllUserRegularAbsencesForUser(userName);
        if (regularAbsences != null && regularAbsences.size() > 0)
            contextParameters.put("userRegularAbsencesList", regularAbsences);

        PermissionsService permissionsService = new PermissionsService(this.userManager);
        boolean isEditor = permissionsService.isEditor(userName, this.pluginSettingsFactory);
        contextParameters.put("isEditor", isEditor);
        contextParameters.put("i18n", this.i18n);
        contextParameters.put("plannerUtils", new Utils());
        String dialogHtml = ComponentAccessor.getVelocityManager().getBody("/templates/", "UserAbsenceHistory.vm", contextParameters);

        return Response.ok(dialogHtml).build();
    }

    @GET
    @Path("plannedAbsencesSimple")
    @Produces({"application/json"})
    public Response plannedAbsencesSimple(@QueryParam("userName") String userName, @QueryParam("start") String start, @QueryParam("end") String end) {
        List<UserAbsence> userAbsences = absencePlannerService.getAllUserAbsencesForUser(userName);
        ArrayList<CalAbsence> calAbsences = new ArrayList<>();
        for (UserAbsence ua : userAbsences) {
            CalAbsence calAbsence = new CalAbsence();
            calAbsence.setTitle(ua.getType());
            calAbsence.setId(ua.getAbsenceId());
            calAbsence.setStart(ua.getStartDate());
            calAbsence.setEnd(ua.getEndDate());
            calAbsences.add(calAbsence);
        }
        return Response.ok(calAbsences).build();
    }

    @GET
    @Path("plannedAbsences")
    @Produces({"application/json"})
    public Response plannedAbsences(@QueryParam("userName") String userName, @QueryParam("start") String start, @QueryParam("end") String end) {
        List<UserAbsence> userAbsences = absencePlannerService.getAllUserAbsencesForUser(userName);
        List<UserAbsence> userRegularAbsences = absencePlannerService.getAllUserRegularAbsencesForUser(userName);
        ArrayList<CalAbsence> calAbsences = new ArrayList<>();
        for (UserAbsence ua : userAbsences) {
            CalAbsence calAbsence = new CalAbsence();
            calAbsence.setTitle(ua.getType());
            calAbsence.setId(ua.getAbsenceId());
            calAbsence.setStart(ua.getStartDate());
            calAbsence.setEnd(ua.getEndDate());
            calAbsences.add(calAbsence);
        }
        for (UserAbsence ua : userRegularAbsences) {
            String regularDays = ua.getRecurringDays();
            if (regularDays != null) {
                String[] recurringDays = Utils.getRecurringDays(regularDays);
                CalAbsence calAbsence = new CalAbsence();
                calAbsence.setTitle(ua.getSummary());
                calAbsence.setId(ua.getAbsenceId());
                calAbsence.setDaysOfWeek(recurringDays);
                calAbsences.add(calAbsence);
            }
        }
        return Response.ok(calAbsences).build();
    }

    @GET
    @Path("getAbsencesForUser")
    @Produces({"application/json"})
    public Response getAbsencesForUser(@QueryParam("userName") String userName, @QueryParam("start") String start, @QueryParam("end") String end) {
        List<UserAbsence> userAbsences = absencePlannerService.getAllUserAbsencesForUser(userName);
        List<UserAbsence> userRegularAbsences = absencePlannerService.getAllUserRegularAbsencesForUser(userName);
        ArrayList<CalAbsence> calAbsences = new ArrayList<>();
        for (UserAbsence ua : userAbsences) {
            CalAbsence calAbsence = new CalAbsence();
            calAbsence.setTitle(ua.getType());
            calAbsence.setId(ua.getAbsenceId());
            calAbsence.setStart(ua.getStartDate());
            calAbsence.setEnd(ua.getEndDate());
            calAbsences.add(calAbsence);
        }
        for (UserAbsence ua : userRegularAbsences) {
            String regularDays = ua.getRecurringDays();
            if (regularDays != null) {
                String[] recurringDays = Utils.getRecurringDays(regularDays);
                CalAbsence calAbsence = new CalAbsence();
                calAbsence.setTitle(ua.getSummary());
                calAbsence.setId(ua.getAbsenceId());
                calAbsence.setDaysOfWeek(recurringDays);
                calAbsences.add(calAbsence);
            }
        }
        return Response.ok(calAbsences).build();
    }

    CalAbsence getAfterWeekCalAbsence(LocalDate startDate, String title, String id, int i) {
        CalAbsence calAbsence = new CalAbsence();
        startDate = startDate.plusWeeks(i);
        calAbsence.setTitle(title);
        calAbsence.setId(id);
        calAbsence.setStart(startDate.toEpochDay() * 24L * 60L * 60L * 1000L);
        return calAbsence;
    }

    @GET
    @Path("absenceCheck")
    public Response absenceCheck(@QueryParam("userName") String userName, @QueryParam("context") String context, @QueryParam("date") long date) {
        VelocityParamFactory velocityParamFactory = ComponentAccessor.getVelocityParamFactory();
        Map<String, Object> contextParameters = velocityParamFactory.getDefaultVelocityParams();
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        ApplicationUser currentUser = jiraAuthenticationContext.getLoggedInUser();

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, currentUser);
        if (!hasPlannerAccess)
            return Response.ok("NoAccess").build();

        LoginService ls = (LoginService) ComponentAccessor.getComponent(LoginService.class);
        LoginInfo loginInfo = ls.getLoginInfo(userName);

//        Utils plannerUtils = new Utils();

        com.atlassian.jira.user.util.UserManager userManager2 = ComponentAccessor.getUserManager();
        ApplicationUser profileUser = userManager2.getUserByName(userName);

        if (userName != null && userName.equals(currentUser.getUsername()))
            Utils.setUserAvailability(currentUser, true);

        contextParameters.put("jira_base_url", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
        boolean isUserOnlineNow = Utils.isUserOnline(profileUser);
        contextParameters.put("i18n", this.i18n);

        String templateName = "AbsenceInfo.vm";

        if (context != null && context.contains("issue"))
            templateName = "IssueAbsenceInfo.vm";

        if (loginInfo != null && loginInfo.getLastLoginTime() != null) {
            long lastLoginTime = loginInfo.getLastLoginTime().longValue();
            LocalDateTime localDateTime = Utils.getLocalDateTime(lastLoginTime);
            if (isUserOnlineNow) {
                contextParameters.put("isLoggedIn", true);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Utils.getJiraFormat() + " HH:mm");
                contextParameters.put("lastLoginTime", localDateTime.format(formatter));
            } else {
                contextParameters.put("isLoggedIn", false);
            }
        } else {
            isUserOnlineNow = Utils.isUserOnline(profileUser);
            contextParameters.put("isLoggedIn", false);
        }

        for (UserAbsence userAbsence : absencePlannerService.getAllUserAbsencesForUser(userName)) {
            LocalDate now = userAbsence.getPlannerDate(date);
            LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
            LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());

            if (now.compareTo(start) >= 0 && now.compareTo(end) <= 0) {
                contextParameters.put("userAbsenceEntry", userAbsence);
                String str = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
                return Response.ok(str).build();
            }

            if (userAbsence.isRecurring()) {
                String days = userAbsence.getRecurringDays();
                if (days != null && days.contains(Utils.getTodayName())) {
                    contextParameters.put("userAbsenceEntry", userAbsence);
                    String str = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
                }
            }
        }

        for (UserAbsence userAbsence : absencePlannerService.getAllUserRegularAbsencesForUser(userName)) {
            if (userAbsence.isRecurring()) {
                String days = userAbsence.getRecurringDays();
                if (days != null && days.contains(Utils.getTodayName())) {
                    contextParameters.put("userAbsenceEntry", userAbsence);
                    String str = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
                }
            }
        }

        String dialogHtml = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
        return Response.ok(dialogHtml).build();
    }

    @GET
    @Path("checkAbsence")
    public Response checkAbsence(@QueryParam("userName") String userName, @QueryParam("context") String context, @QueryParam("date") long date) {

        ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, applicationUser);
        if (!hasPlannerAccess)
            return Response.ok("NoAccess").build();

        VelocityParamFactory velocityParamFactory = ComponentAccessor.getVelocityParamFactory();
        Map<String, Object> contextParameters = velocityParamFactory.getDefaultVelocityParams();
        LoginService ls = (LoginService) ComponentAccessor.getComponent(LoginService.class);
        LoginInfo loginInfo = ls.getLoginInfo(userName);

        com.atlassian.jira.user.util.UserManager userManager2 = ComponentAccessor.getUserManager();

        ApplicationUser profileUser = userManager2.getUserByName(userName);

        contextParameters.put("jira_base_url", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));
        boolean isUserOnlineNow = Utils.isUserOnline(profileUser);
        contextParameters.put("i18n", this.i18n);
        contextParameters.put("userName", userName);
        String templateName = "UserAbseneceIcon.vm";
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        ApplicationUser currentUser = jiraAuthenticationContext.getLoggedInUser();
        if (currentUser != null && currentUser.getUsername().equals(userName))
            Utils.setUserAvailability(currentUser, true);
        if (loginInfo != null && loginInfo.getLastLoginTime() != null) {
            long lastLoginTime = loginInfo.getLastLoginTime().longValue();
            LocalDateTime localDateTime = Utils.getLocalDateTime(lastLoginTime);
            if (isUserOnlineNow) {
                contextParameters.put("isLoggedIn", true);
                contextParameters.put("absenceClass", "userOnline-IssueLed");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Utils.getJiraFormat() + " HH:mm");
                contextParameters.put("lastLoginTime", localDateTime.format(formatter));
            } else {
                contextParameters.put("isLoggedIn", false);
                contextParameters.put("absenceClass", "userOffline-IssueLed");
            }
        } else {
            contextParameters.put("isLoggedIn", false);
        }
        for (UserAbsence userAbsence : absencePlannerService.getAllUserAbsencesForUser(userName)) {
            LocalDate now = userAbsence.getPlannerDate(date);
            LocalDate start = userAbsence.getPlannerDate(userAbsence.getStartDate());
            LocalDate end = userAbsence.getPlannerDate(userAbsence.getEndDate());
            if (now.compareTo(start) >= 0 && now.compareTo(end) <= 0) {
                contextParameters.put("absenceClass", "userAbsent-IssueLed");
                contextParameters.put("userAbsenceEntry", userAbsence);
                String str = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
                return Response.ok(str).build();
            }
        }
        for (UserAbsence userAbsence : absencePlannerService.getAllUserRegularAbsencesForUser(userName)) {
            if (userAbsence.isRecurring()) {
                String days = userAbsence.getRecurringDays();
                if (days != null && days.contains(Utils.getTodayName())) {
                    contextParameters.put("userAbsenceEntry", userAbsence);
                    contextParameters.put("absenceClass", "userAbsent-IssueLed");
                    String str = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
                    return Response.ok(str).build();
                }
            }
        }
        String dialogHtml = ComponentAccessor.getVelocityManager().getBody("/templates/", templateName, contextParameters);
        return Response.ok(dialogHtml).build();
    }

    @POST
    @Path("setLocum")
    public Response setLocum(@QueryParam("userName") String userName, @QueryParam("locum") String locum) {

        com.atlassian.jira.user.util.UserManager userManager2 = ComponentAccessor.getUserManager();
        ApplicationUser profileUser = userManager2.getUserByName(userName);
        if (profileUser != null) {
            Utils.setSelectedAssignee(profileUser, locum);
        } else {
            LogService.error("User absence-ERROR: no user found with name:" + userName);
        }
        return Response.ok().build();
    }

    @GET
    @Path("findAssignableUsers")
    @Produces({"application/json"})
    public Response findAssignableUsers(@QueryParam("profileUser") String profileUser, @QueryParam("userName") String userName) {
        com.atlassian.jira.user.util.UserManager userManager2 = ComponentAccessor.getUserManager();
        ApplicationUser profileApplicationUser = userManager2.getUserByName(profileUser);
        List<User> usersList = (new PermissionsService(this.userManager)).getRepresentatives(profileApplicationUser, userName);
        return Response.ok(usersList).build();
    }

    @GET
    @Path("findPermittableUsers")
    @Produces({"application/json"})
    public Response findPermittableUsers(@QueryParam("searchToken") String searchToken) {
        com.atlassian.jira.user.util.UserManager userManagerUtil = ComponentAccessor.getUserManager();
        ApplicationUser profileApplicationUser = userManagerUtil.getUserByName(searchToken);
        List<User> usersList = (new PermissionsService(this.userManager)).getAbsencesPossibleEditors(profileApplicationUser, searchToken);
        return Response.ok(usersList).build();
    }

    @POST
    @Path("updatePlannerSettings")
    @Consumes({"application/json"})
    public Response updatePlannerSettings(PlannerConfig plannerConfig) {
        PlannerSecurityService.setPlannerSecuritySettings(plannerConfig, this.pluginSettingsFactory);
        return Response.ok("Sucess").build();
    }

    @GET
    @Path("hasAccess")
    public Response hasAccess(@QueryParam("context") String context) {

        ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        boolean hasPlannerAccess = PlannerSecurityService.hasAccess(this.pluginSettingsFactory, applicationUser);

        return Response.ok(String.valueOf(hasPlannerAccess)).build();
    }
}
