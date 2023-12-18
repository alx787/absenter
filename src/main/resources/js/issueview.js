AJS.toInit(function () {

    var isUserAbsent = function (userName, context, contorl) {
        AJS.$.ajax({
            type: "GET",
            url: AJS.contextPath() + '/rest/absenter/1.0/checkAbsence?userName=' + userName + '&context=' + context + '&date=' + Date.parse(new Date()),
            contentType: 'application/json; charset=utf-8',
            dataType: "html",
            success: function (absenseStatus) {
                if (absenseStatus === "NoAccess")
                    return "";
                if (AJS.$(contorl).find('.aui-avatar-small').length == 0 || AJS.$(contorl).find('.aui-avatar-xsmall').length == 0) {
                    if (AJS.$(contorl).closest(".editable-field").attr("data-fieldtype") == "multiuserpicker") {
                        if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0)
                            AJS.$(contorl).parent().prepend(absenseStatus);
                    } else if (AJS.$(contorl).parent().hasClass("cell-type-user")) {
                        if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                            AJS.$(contorl).prepend(absenseStatus);
                        }
                    } else if (AJS.$(contorl).parent().hasClass("action-details")) {
                        if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                            //AJS.$(contorl).prepend(absenseStatus);
                        }
                    } else {
                        if (AJS.$(contorl).parent().find(".sg_planner_status").length < AJS.$(contorl).parent().find(".user-hover").length) {
                            AJS.$(contorl).prepend(absenseStatus);
                        }
                    }
                } else
                    AJS.$(contorl).prepend(absenseStatus);
            },
            error: function (error) {
                AJS.$('#sg-planner-absence-status-loader').hide();
            }
        });

    }
    AJS.$('.user-hover').each(function () {
        var userName = AJS.$(this).attr("rel");

        isUserAbsent(userName, "profile", AJS.$(this));

    });
    var sgCounter = 0;
    var assignEventAfterInlineEdit = function () {
        JIRA.bind(JIRA.Events.INLINE_EDIT_SAVE_COMPLETE, function (e, context, reason) {
            if (AJS.$('.sg_planner_status').length == 0) {
                sgCounter += 1;
                AJS.$('.user-hover').each(function () {
                    var userName = AJS.$(this).attr("rel");
                    if (AJS.$(this).find(".sg_planner_status").length == 0) {
                        var contorl = AJS.$(this);
                        AJS.$.ajax({
                            type: "GET",
                            url: AJS.contextPath() + '/rest/absenter/1.0/checkAbsence?userName=' + userName + '&context=' + context + '&date=' + Date.parse(new Date()),
                            contentType: 'application/json; charset=utf-8',
                            dataType: "html",
                            success: function (absenseStatus) {
                                if (absenseStatus === "NoAccess")
                                    return "";
                                /* if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0)
                                   if (AJS.$(contorl).find('.aui-avatar-small').length == 0) {
                                     if (AJS.$(contorl).parent(".editable-field").attr("data-fieldtype") == "multiuserpicker")
                                       AJS.$(contorl).parent().prepend(absenseStatus);
                                     else
                                       AJS.$(contorl).parent().parent().prepend(absenseStatus);
                                   }
                                   else
                                     AJS.$(contorl).prepend(absenseStatus);*/

                                if (AJS.$(contorl).find('.aui-avatar-small').length == 0) {
                                    if (AJS.$(contorl).closest(".editable-field").attr("data-fieldtype") == "multiuserpicker")
                                        if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {

                                            AJS.$(contorl).parent().prepend(absenseStatus);
                                        } else if (AJS.$(contorl).parent().hasClass("cell-type-user")) {
                                            if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                                                AJS.$(contorl).prepend(absenseStatus);
                                            }

                                        } else if (AJS.$(contorl).parent().hasClass("action-details")) {
                                            if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                                                // AJS.$(contorl).prepend(absenseStatus);
                                            }
                                        } else {
                                            if (AJS.$(contorl).parent().find(".sg_planner_status").length < AJS.$(contorl).parent().find(".user-hover").length) {
                                                AJS.$(contorl).prepend(absenseStatus);
                                            }
                                        }

                                } else if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                                    AJS.$(contorl).prepend(absenseStatus);
                                }
                            },
                            error: function (error) {
                                AJS.$('#sg-planner-absence-status-loader').hide();
                            }
                        });
                    }
                });

            }
        });
    }
    assignEventAfterInlineEdit();

    var hasAccessToUAP = false;
    var hasAccess = function () {

        var context = "admin";
        AJS.$.ajax({
            type: "GET",
            url: AJS.contextPath() + '/rest/absenter/1.0/hasAccess?context=' + context,
            contentType: 'application/json; charset=utf-8',
            dataType: "html",
            success: function (hasAccess) {
                hasAccessToUAP = hasAccess;
            },
            error: function () {


            }
        });
    }

    hasAccess();
    console.log(hasAccessToUAP);

    var assignEventWhenIssueRefresshed = function () {

        JIRA.bind(JIRA.Events.ISSUE_REFRESHED, function (e, context, reason) {
            if (AJS.$('.sg_planner_status').length == 0) {
                AJS.$('.user-hover').each(function () {
                    var userName = AJS.$(this).attr("rel");
                    var contorl = AJS.$(this);
                    AJS.$.ajax({
                        type: "GET",
                        url: AJS.contextPath() + '/rest/absenter/1.0/checkAbsence?userName=' + userName + '&context=' + context + '&date=' + Date.parse(new Date()),
                        contentType: 'application/json; charset=utf-8',
                        dataType: "html",
                        success: function (absenseStatus) {
                            if (absenseStatus === "NoAccess")
                                return "";
                            /* if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0)
                               if (AJS.$(contorl).find('.aui-avatar-small').length == 0) {
                                 if (AJS.$(contorl).closest(".editable-field").attr("data-fieldtype") == "multiuserpicker")
                                   AJS.$(contorl).parent().prepend(absenseStatus);
                                 else
                                   AJS.$(contorl).parent().parent().prepend(absenseStatus);
                               }
                               else
                                 AJS.$(contorl).prepend(absenseStatus);*/
                            if (AJS.$(contorl).find('.aui-avatar-small').length == 0) {
                                if (AJS.$(contorl).closest(".editable-field").attr("data-fieldtype") == "multiuserpicker")
                                    AJS.$(contorl).parent().prepend(absenseStatus);
                                else if (AJS.$(contorl).parent().hasClass("cell-type-user")) {
                                    if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                                        AJS.$(contorl).prepend(absenseStatus);
                                    }
                                } else if (AJS.$(contorl).parent().hasClass("action-details")) {
                                    if (AJS.$(contorl).parent().find(".sg_planner_status").length == 0) {
                                        //AJS.$(contorl).prepend(absenseStatus);
                                    }
                                } else {
                                    if (AJS.$(contorl).parent().find(".sg_planner_status").length < AJS.$(contorl).parent().find(".user-hover").length) {
                                        AJS.$(contorl).prepend(absenseStatus);
                                    }
                                }

                            } else
                                AJS.$(contorl).prepend(absenseStatus);
                        },
                        error: function (error) {
                            AJS.$('#sg-planner-absence-status-loader').hide();

                        }
                    });
                });

            }
        });
    }
    assignEventWhenIssueRefresshed();

});