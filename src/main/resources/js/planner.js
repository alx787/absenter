
AJS.toInit(function () {

  var dialogClean = function () {

    $('#sg-planner-from').val('');
    $('#sg-planner-to').val('');
    AJS.$('#sg-planner-type').val('-1').trigger("change");
    AJS.$('#sg-planner-message').val('');
    AJS.$('#sg-planner-summary').val('');
    AJS.$('#sg-planner-from').datePicker({ 'overrideBrowserDefault': true });
    AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true });
    AJS.$('#sg-planner-from').datePicker().setMax('');
    AJS.$('#sg-planner-to').datePicker().setMin('');


  }
  // Shows the dialog when the "Show dialog" button is clicked
  AJS.$("#sg-add-new-absence-dialog").click(function (e) {
    e.preventDefault();
    dialogClean();
    resetDialog();

    AJS.dialog2("#sg-planner-add-absence-dialog").show();
    AJS.$('#sg-regDaysContainer').hide();
    AJS.$('#sg-absenceTypeContainer').show();
    AJS.$('#sg-fromToContainer').show();
  });

  AJS.$("#sg-add-new-reg-absence-dialog").click(function (e) {

    e.preventDefault();
    resetDialog();

    AJS.dialog2("#sg-planner-add-absence-dialog").show();
    AJS.$('#sg-regDaysContainer').show();
    AJS.$('#sg-absenceTypeContainer').hide();
    AJS.$('#sg-fromToContainer').hide();
    // AJS.$('.sg_recurring').attr("disabled",false);

  });
  // Hides the dialog
  AJS.$("#sg-planner-addAbsenceEntry").click(function (e) {
    e.preventDefault();
    // AJS.dialog2("#sg-planner-add-absence-dialog").hide();
  });
  AJS.$("#sg-planner-close-dialog, #sg-planner-cancel-dialog").click(function (e) {
    e.preventDefault();
    resetDialog();
    AJS.dialog2("#sg-planner-add-absence-dialog").hide();
  });
  AJS.$('#sg-planner-userAbsenceHistory-loader').hide();
  var LoadUserAbsenceHistory = function (userName) {
    AJS.$.ajax({
      type: "GET",
      url: AJS.contextPath() + '/rest/absenter/1.0/loadAbsenceHistory?userName=' + userName,
      contentType: 'application/json; charset=utf-8',
      dataType: "html",
      success: function (absenceHistory) {
        AJS.$('#sg-planner-userAbsenceHistory-loader').hide();
        AJS.$('#sg-planner-userAbsenceHistory').html(absenceHistory);
        AJS.$('.sg-confirm-yes').each(function () {
          AJS.$(this).click(function () {
            var absenceId = AJS.$(this).attr("sg-absenceId");
            var user = AJS.$('#profileUserName').val();
            AJS.$.ajax({
              type: "DELETE",
              url: AJS.contextPath() + '/rest/absenter/1.0/cancelAbsenceEntry?user=' + user + '&absenceId=' + absenceId,
              contentType: 'application/json; charset=utf-8',
              dataType: "html",
              success: function (absenceHistory) {
                AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
                var myFlag = AJS.flag({
                  type: 'success',
                  body: 'Absence removed!',
                  close: 'auto',
                });
                LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
              },
              error: function (error) {
                AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
                var myFlag = AJS.flag({
                  type: 'error',
                  body: 'Error occurs!',
                  close: 'auto',
                });
              }
            });
          });
        });
        AJS.tabs.setup();
        var calendarEl = document.getElementById('calendar');
        if (AJS.$(calendarEl).length > 0) {
          var userName = AJS.$('#profileUserName').val();
          var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            initialDate: '2021-03-07',
            eventClassNames: 'sg-calView sg-calView2',
            headerToolbar: {
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            eventSources: [
              {
                url: AJS.contextPath() + '/rest/absenter/1.0/plannedAbsences?userName=' + userName,
                method: 'GET',
                /* extraParams: {
                   custom_param1: 'something',
                   custom_param2: 'somethingelse'
                 },*/
                failure: function () {
                  alert('there was an error while fetching events!');
                },
                color: '#8E1010',   // a non-ajax option
                textColor: 'white' // a non-ajax option
              }


            ]
          });
          calendar.render();
          AJS.$('#sg-calendarView').click(function () {
            $('.fc-dayGridMonth-button').trigger("click");
            setTimeout(function () {
              $('.fc-dayGridMonth-button').trigger("click");
            }, 100);
            setTimeout(function () {
              $('.fc-dayGridMonth-button').trigger("click");
            }, 500);
          });
        }
      },
      error: function (error) {
        console.error(error + "  FAILED TO RELOAD CONTENTS");
        AJS.$('#sg-planner-userAbsenceHistory-loader').hide();
      }
    });


  }
  var isUserAbsent = function (userName, context) {
    AJS.$.ajax({
      type: "GET",
      url: AJS.contextPath() + '/rest/absenter/1.0/absenceCheck?userName=' + userName + '&context=' + context + '&date=' + Date.parse(new Date()),
      contentType: 'application/json; charset=utf-8',
      dataType: "html",
      success: function (absenseStatus) {
        if(absenseStatus==="NoAccess")
        return "";
        AJS.$('#sg-planner-absence-status-loader').hide();
        AJS.$('#sg-planner-absence-status').html(absenseStatus);
        AJS.$('#user-hover-email').after(absenseStatus);
      },
      error: function (error) {
        AJS.$('#sg-planner-absence-status-loader').hide();

      }
    });

  }

  isUserAbsent(AJS.$('#profileUserName').val(), "profile");

  var addAbsenceRegularEntry = function (that) {
    AJS.$('#sg-planner-userAbsenceHistory-Loader').show();
    var UserAbsenceModel = new Object();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    UserAbsenceModel.user = AJS.$('#profileUserName').val();
    UserAbsenceModel.reportedBy = AJS.$('#sg-planner-reporter').val();
    var fromMilliSec = Date.parse($('#sg-planner-from').val());
    UserAbsenceModel.startDate = 1;// AJS.$('#sg-planner-from').val();
    var toMilliSec = Date.parse($('#sg-planner-to').val());
    UserAbsenceModel.endDate = 1;// AJS.$('#sg-planner-to').val();

    UserAbsenceModel.type = AJS.$('#sg-planner-type').val();
    UserAbsenceModel.message = AJS.$('#sg-planner-message').val();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    var dateMillSec = Date.parse(new Date());
    UserAbsenceModel.date = dateMillSec;
    UserAbsenceModel.recurring = true;
    var days = [];
    $('.sg_recurring').each(function () {
      if (AJS.$(this).is(":checked")) {
        days.push(AJS.$(this).attr("name"));
      }
    });
    UserAbsenceModel.recurringDays = days.toString();

    var errors = 0;
    if (UserAbsenceModel.summary.length < 5) {
      var myFlag = AJS.flag({
        type: 'error',
        body: 'Summary is required field',
        close: 'auto',
      });
      errors += 1;
    }


    var parameters = JSON.stringify(UserAbsenceModel);
    var jiraPath = AJS.Meta.get('context-path');
    var addAbsenceEntryCall = {
      type: 'POST',
      url: jiraPath + '/rest/absenter/1.0/addUserAbsence',
      data: parameters,
      contentType: 'application/json; charset=utf-8',
      // dataType: "te",
      timeout: -1,
      async: true,
      success: function (data) {
//          var myFlag = AJS.flag({
//            type: 'error',
//            body: 'License is not valid!',
//            close: 'auto',
//          });
          var myFlag = AJS.flag({
            type: 'success',
            body: 'Absence entry added successfully!',
            close: 'auto',
          });
          LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
          resetDialog();
          AJS.dialog2("#sg-planner-add-absence-dialog").hide();
      },
      error: function (e) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Failed to save Absence entry!',
          close: 'auto',
        });
        AJS.$('#sg-planner-userAbsenceHistory-Loader').hide();

      },
      complete: function (m) {

      }
    }
    if (errors == 0)
      AJS.$.ajax(addAbsenceEntryCall);
    if (that.isBusy()) {
      that.idle();
    }
  }

  var addAbsenceDateBasedEntry = function (that) {
    AJS.$('#sg-planner-userAbsenceHistory-Loader').show();
    var UserAbsenceModel = new Object();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    UserAbsenceModel.user = AJS.$('#profileUserName').val();
    UserAbsenceModel.reportedBy = AJS.$('#sg-planner-reporter').val();
    var fromMilliSec = Date.parse($('#sg-planner-from').val());
    UserAbsenceModel.startDate = fromMilliSec;// AJS.$('#sg-planner-from').val();
    var toMilliSec = Date.parse($('#sg-planner-to').val());
    UserAbsenceModel.endDate = toMilliSec;// AJS.$('#sg-planner-to').val();

    UserAbsenceModel.type = AJS.$('#sg-planner-type').val();
    UserAbsenceModel.message = AJS.$('#sg-planner-message').val();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    var dateMillSec = Date.parse(new Date());
    UserAbsenceModel.date = dateMillSec;
    UserAbsenceModel.recurring = false;


    var errors = 0;
    if (UserAbsenceModel.summary.length < 5) {
      var myFlag = AJS.flag({
        type: 'error',
        body: 'Summary is required field',
      });
      errors += 1;
    }
    if (UserAbsenceModel.type === "-1") {
      var myFlag = AJS.flag({
        type: 'error',
        body: 'Type is required field',
        close: 'auto',
      });
      errors += 1;
    }
    if (isNaN(UserAbsenceModel.endDate) || isNaN(UserAbsenceModel.endDate)) {
      var myFlag = AJS.flag({
        type: 'error',
        body: 'Start And End Dates is required field',
        close: 'auto',
      });
      errors += 1;
    }

    var parameters = JSON.stringify(UserAbsenceModel);
    var jiraPath = AJS.Meta.get('context-path');
    var addAbsenceEntryCall = {
      type: 'POST',
      url: jiraPath + '/rest/absenter/1.0/addUserAbsence',
      data: parameters,
      contentType: 'application/json; charset=utf-8',
      // dataType: "te",
      timeout: -1,
      async: true,
      success: function (data) {
          var myFlag = AJS.flag({
            type: 'success',
            body: 'Absence entry added successfully!',
            close: 'auto',
          });
          LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
          resetDialog();
          AJS.dialog2("#sg-planner-add-absence-dialog").hide();
      },
      error: function (e) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Failed to save Absence entry!',
          close: 'auto',
        });
        AJS.$('#sg-planner-userAbsenceHistory-Loader').hide();

      },
      complete: function (m) {

      }
    }
    if (errors == 0)
      AJS.$.ajax(addAbsenceEntryCall);
    if (that.isBusy()) {
      that.idle();
    }
  }

  AJS.$('#sg-planner-addAbsenceEntry').click(function (e) {
    e.preventDefault();
    var that = this;
    if (!that.isBusy()) {
      that.busy();
    }
    if (AJS.$('#sg-regDaysContainer').is(":visible")) {
      addAbsenceRegularEntry(that);
    } else {
      addAbsenceDateBasedEntry(that);
    }
  });

  // показать объект для отладки
  AJS.$('#sg-planner-addAbsenceEntryTest').click(function (e) {
    e.preventDefault();
    var that = this;

    var UserAbsenceModel = new Object();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    UserAbsenceModel.user = AJS.$('#profileUserName').val();
    UserAbsenceModel.reportedBy = AJS.$('#sg-planner-reporter').val();
    var fromMilliSec = Date.parse($('#sg-planner-from').val());
    UserAbsenceModel.startDate = fromMilliSec;// AJS.$('#sg-planner-from').val();
    var toMilliSec = Date.parse($('#sg-planner-to').val());
    UserAbsenceModel.endDate = toMilliSec;// AJS.$('#sg-planner-to').val();

    UserAbsenceModel.type = AJS.$('#sg-planner-type').val();
    UserAbsenceModel.message = AJS.$('#sg-planner-message').val();
    UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
    var dateMillSec = Date.parse(new Date());
    UserAbsenceModel.date = dateMillSec;
    UserAbsenceModel.recurring = false;

    console.log(UserAbsenceModel);

  });

  AJS.$('#sg-planner-userAbsenceHistory').on("click", ".sg-edit-rec-absence", function () {

    var absenceId = AJS.$(this).attr("sg-data-absid");
    var absenceSummary = AJS.$(this).closest("tr").find(".absenceSummary").attr("sg-data");
    var absenceType = AJS.$(this).closest("tr").find(".absenceType").attr("sg-data");
    var absenceStartDate = AJS.$(this).closest("tr").find(".absenceStartDate").attr("sg-data");
    var absenceEndDate = AJS.$(this).closest("tr").find(".absenceEndDate").attr("sg-data");
    var absenceMessage = AJS.$(this).closest("tr").find(".absenceMessage").attr("sg-data");
    var $absenceDays = AJS.$(this).closest("tr").find(".absenceDays").attr("sg-data");
    AJS.$('#sg-fromToContainer').hide();

    $.each($absenceDays.split(","), function (i, day) {

      AJS.$('#' + day).attr("checked", true);
    });

    AJS.$('#sg-planner-summary').val(absenceSummary);
    //AJS.$('#sg-planner-from').val(absenceStartDate);
    //AJS.$('#sg-planner-to').val(absenceEndDate);
    AJS.$('#sg-planner-absenceId').val(absenceId);
    AJS.$('#sg-planner-message').val(absenceMessage);
    //AJS.$('#sg-planner-type').val(absenceType).trigger("change");
    AJS.$('#sg-planner-updateAbsenceEntry').show();
    AJS.$('#sg-planner-addAbsenceEntry').hide();
    AJS.$('#sg-regDaysContainer').show();
    AJS.$('#sg-absenceTypeContainer').hide();
    // AJS.$('#sg-fromToContainer').hide();
    AJS.dialog2("#sg-planner-add-absence-dialog").show();

  });
  AJS.$('#sg-planner-userAbsenceHistory').on("click", ".sg-edit-absence", function () {
    var absenceId = AJS.$(this).attr("sg-data-absid");
    var absenceSummary = AJS.$(this).closest("tr").find(".absenceSummary").attr("sg-data");
    var absenceType = AJS.$(this).closest("tr").find(".absenceType").attr("sg-data");
    var absenceStartDate = AJS.$(this).closest("tr").find(".absenceStartDate").attr("sg-data");
    var absenceEndDate = AJS.$(this).closest("tr").find(".absenceEndDate").attr("sg-data");
    var absenceMessage = AJS.$(this).closest("tr").find(".absenceMessage").attr("sg-data");
    AJS.$('#sg-planner-summary').val(absenceSummary);
    AJS.$('#sg-planner-from').val(absenceStartDate);
    AJS.$('#sg-planner-to').val(absenceEndDate);
    AJS.$('#sg-planner-absenceId').val(absenceId);
    AJS.$('#sg-planner-message').val(absenceMessage);
    AJS.$('#sg-planner-type').val(absenceType).trigger("change");
    AJS.$('#sg-planner-updateAbsenceEntry').show();
    AJS.$('#sg-planner-addAbsenceEntry').hide();
    AJS.dialog2("#sg-planner-add-absence-dialog").show();
    AJS.$('#sg-regDaysContainer').hide();
    AJS.$('#sg-absenceTypeContainer').show();
    AJS.$('#sg-fromToContainer').show();
    // console.log("AbsenceID:" + absenceId + "    Summary:" + absenceSummary + " Type:" + absenceType + " StDate:" + absenceStartDate + " EndDate:" + absenceEndDate + " Message:" + absenceMessage);
  });
  AJS.$('.sg-edit-absence').click(function () {
    var absenceId = AJS.$(this).attr("sg-data-absid");
    var absenceSummary = AJS.$(this).closest("tr").find(".absenceSummary").attr("sg-data");
    var absenceType = AJS.$(this).closest("tr").find(".absenceType").attr("sg-data");
    var absenceStartDate = AJS.$(this).closest("tr").find(".absenceStartDate").attr("sg-data");
    var absenceEndDate = AJS.$(this).closest("tr").find(".absenceEndDate").attr("sg-data");
    var absenceMessage = AJS.$(this).closest("tr").find(".absenceMessage").attr("sg-data");

    AJS.$('#sg-planner-summary').val(absenceSummary);
    AJS.$('#sg-planner-from').val(absenceStartDate);
    AJS.$('#sg-planner-to').val(absenceEndDate);
    AJS.$('#sg-planner-absenceId').val(absenceId);
    AJS.$('#sg-planner-message').val(absenceMessage);
    AJS.$('#sg-planner-type').val(absenceType).trigger("change");
    AJS.$('#sg-planner-updateAbsenceEntry').show();
    AJS.$('#sg-planner-addAbsenceEntry').hide();

    AJS.$('#sg-planner-to').datePicker().setMin(AJS.$('#sg-planner-from').val());

    AJS.dialog2("#sg-planner-add-absence-dialog").show();

    //console.log("AbsenceID:" + absenceId + "    Summary:" + absenceSummary + " Type:" + absenceType + " StDate:" + absenceStartDate + " EndDate:" + absenceEndDate + " Message:" + absenceMessage);
  });

  /*AJS.$('.sg-cancel-absence').click(function () {
      var absenceId = AJS.$(this).attr("sg-data-absid");
      var user = AJS.$('#profileUserName').val();
      console.log("AbsenceID:" + absenceId + "    User:" + user);
  });*/
  AJS.$('#sg-planner-userAbsenceHistory').on("click", ".sg-confirm-yes", function () {
    AJS.$(this).click(function () {
      var absenceId = AJS.$(this).attr("sg-absenceId");
      var user = AJS.$('#profileUserName').val();

      AJS.$.ajax({
        type: "DELETE",
        url: AJS.contextPath() + '/rest/absenter/1.0/cancelAbsenceEntry?user=' + user + '&absenceId=' + absenceId,
        contentType: 'application/json; charset=utf-8',
        dataType: "html",
        success: function (absenceHistory) {
          AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
          var myFlag = AJS.flag({
            type: 'success',
            body: 'Absence removed!',
            close: 'auto',
          });
          LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
        },
        error: function (error) {
          AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
          var myFlag = AJS.flag({
            type: 'error',
            body: 'Error occurs!',
            close: 'auto',
          });
        }
      });
    });
  });
  AJS.$('.sg-confirm-yes').each(function () {

    AJS.$(this).click(function () {
      var absenceId = AJS.$(this).attr("sg-absenceId");
      var user = AJS.$('#profileUserName').val();

      AJS.$.ajax({
        type: "DELETE",
        url: AJS.contextPath() + '/rest/absenter/1.0/cancelAbsenceEntry?user=' + user + '&absenceId=' + absenceId,
        contentType: 'application/json; charset=utf-8',
        dataType: "html",
        success: function (absenceHistory) {
          AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
          var myFlag = AJS.flag({
            type: 'success',
            body: 'Absence removed!',
            close: 'auto',
          });
          LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
        },
        error: function (error) {
          AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
          var myFlag = AJS.flag({
            type: 'error',
            body: 'Error occurs!',
            close: 'auto',
          });
        }
      });
    });
  });
  AJS.$('.sg-confirm-no').each(function () {
    AJS.$(this).click(function () {
      var absenceId = AJS.$(this).attr("sg-absenceId");
      AJS.$('#sg-cancel-confirm-dialog-' + absenceId).hide();
    });
  });
  var resetDialog = function () {
    AJS.$('#sg-planner-summary').val('');
    AJS.$('#sg-planner-from').val('');
    AJS.$('#sg-planner-to').val('');
    AJS.$('#sg-planner-absenceId').val('');
    AJS.$('#sg-planner-message').val('');
    AJS.$('#sg-planner-type').val('-1').trigger("change");
    AJS.$('#sg-planner-updateAbsenceEntry').hide();
    AJS.$('#sg-planner-addAbsenceEntry').show();
    AJS.dialog2("#sg-planner-add-absence-dialog").hide();

  }
  AJS.$('#sg-planner-updateAbsenceEntry').click(function (e) {
    e.preventDefault();
    if (AJS.$('#sg-regDaysContainer').is(":visible")) {
      updateAbsenceEntry(true, this);
    } else {
      updateAbsenceEntry(false, this);

    }

  });
  var updateAbsenceEntry = function (isRecurrent, that) {
    AJS.$('#sg-planner-userAbsenceHistory-Loader').show();

    if (isRecurrent == true) {
      var UserAbsenceModel = new Object();
      UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
      UserAbsenceModel.user = AJS.$('#profileUserName').val();
      UserAbsenceModel.reportedBy = AJS.$('#sg-planner-reporter').val();

      UserAbsenceModel.type = "recurrent";
      UserAbsenceModel.message = AJS.$('#sg-planner-message').val();
      UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
      var dateMillSec = Date.parse(new Date());
      UserAbsenceModel.date = dateMillSec;
      UserAbsenceModel.id = AJS.$('#sg-planner-absenceId').val();
      UserAbsenceModel.recurring = true;
      var days = [];
      $('.sg_recurring').each(function () {
        if (AJS.$(this).is(":checked")) {
          days.push(AJS.$(this).attr("name"));
        }
      });
      UserAbsenceModel.recurringDays = days.toString();

      var errors = 0;
      if (UserAbsenceModel.summary.length < 5) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Summary is required field',
        });
        errors += 1;
      }
      if (UserAbsenceModel.type === "-1") {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Type is required field',
          close: 'auto',
        });
        errors += 1;
      }
      if (UserAbsenceModel.recurringDays.length < 1) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Please select at least one day!',
          close: 'auto',
        });
        errors += 1;
      }
      var parameters = JSON.stringify(UserAbsenceModel);
      var jiraPath = AJS.Meta.get('context-path');
      var addAbsenceEntryCall = {
        type: 'POST',
        url: jiraPath + '/rest/absenter/1.0/updateUserAvailability',
        data: parameters,
        contentType: 'application/json; charset=utf-8',
        // dataType: "te",
        timeout: -1,
        async: true,
        success: function (data) {
          if (data == "LicenseError") {
            var myFlag = AJS.flag({
              type: 'error',
              body: 'License is not valid!',
              close: 'auto',
            });
          } else {
            var myFlag = AJS.flag({
              type: 'success',
              body: 'Absence entry updated successfully!',
              close: 'auto',
            });
            LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
            resetDialog();
          }

        },
        error: function (e) {
          var myFlag = AJS.flag({
            type: 'error',
            body: 'Failed to save Absence entry!',
            close: 'auto',
          });
          AJS.$('#sg-planner-userAbsenceHistory-Loader').hide();

        },
        complete: function (m) {

        }
      }
      if (errors == 0)
        AJS.$.ajax(addAbsenceEntryCall);
      if (that.isBusy()) {
        that.idle();
      }
    } else {
      var UserAbsenceModel = new Object();
      UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
      UserAbsenceModel.user = AJS.$('#profileUserName').val();
      UserAbsenceModel.reportedBy = AJS.$('#sg-planner-reporter').val();
      var fromMilliSec = Date.parse($('#sg-planner-from').val());
      UserAbsenceModel.startDate = fromMilliSec;// AJS.$('#sg-planner-from').val();
      var toMilliSec = Date.parse($('#sg-planner-to').val());
      UserAbsenceModel.endDate = toMilliSec;// AJS.$('#sg-planner-to').val();

      UserAbsenceModel.type = AJS.$('#sg-planner-type').val();
      UserAbsenceModel.message = AJS.$('#sg-planner-message').val();
      UserAbsenceModel.summary = AJS.$('#sg-planner-summary').val();
      var dateMillSec = Date.parse(new Date());
      UserAbsenceModel.date = dateMillSec;
      UserAbsenceModel.id = AJS.$('#sg-planner-absenceId').val();

      var errors = 0;
      if (UserAbsenceModel.summary.length < 5) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Summary is required field',
        });
        errors += 1;
      }
      if (UserAbsenceModel.type === "-1") {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Type is required field',
          close: 'auto',
        });
        errors += 1;
      }
      if (isNaN(UserAbsenceModel.endDate) || isNaN(UserAbsenceModel.endDate)) {
        var myFlag = AJS.flag({
          type: 'error',
          body: 'Start And End Dates is required field',
          close: 'auto',
        });
        errors += 1;
      }
  
     
      var parameters = JSON.stringify(UserAbsenceModel);
      var jiraPath = AJS.Meta.get('context-path');

      var addAbsenceEntryCall = {
        type: 'POST',
        url: jiraPath + '/rest/absenter/1.0/updateUserAvailability',
        data: parameters,
        contentType: 'application/json; charset=utf-8',
        // dataType: "te",
        timeout: -1,
        async: true,
        success: function (data) {
          if (data == "LicenseError") {
            var myFlag = AJS.flag({
              type: 'error',
              body: 'License is not valid!',
            });
          } else {
            var myFlag = AJS.flag({
              type: 'success',
              body: 'Absence entry updated successfully!',
              close: 'auto',
            });
            LoadUserAbsenceHistory(AJS.$('#profileUserName').val());
            resetDialog();
          }

        },
        error: function (e) {
          var myFlag = AJS.flag({
            type: 'error',
            body: 'Failed to save Absence entry!',
            close: 'auto',
          });
          AJS.$('#sg-planner-userAbsenceHistory-Loader').hide();

        },
        complete: function (m) {

        }
      }
      if (errors == 0)
        AJS.$.ajax(addAbsenceEntryCall);
      if (that.isBusy()) {
        that.idle();
      }
    }
  }


  JIRA.userhover.INLINE_DIALOG_OPTIONS.initCallback = function () {
    var dialog = AJS.$('#inline-dialog-' + this.id);
    var userName = dialog.find('#avatar-full-name-link').attr('title');
    if (typeof userName === "undefined") {
      console.log(" User is not available ...");
    } else {

      AJS.$.ajax({
        "url": AJS.contextPath() + '/rest/absenter/1.0/absenceCheck?userName=' + userName + '&context=issue' + '&date=' + Date.parse(new Date())
      }).done(function (data) {
        if(data==="NoAccess")
        return "";
        AJS.$('#user-hover-email').after(data);

      });
    }
  }

  if (AJS.$('#sg-planner-from').length > 0) {
    AJS.$('#sg-planner-type').auiSelect2();
    AJS.$('.userAbsent-led').tooltip();
    AJS.$('.userOnline-led').tooltip();
    AJS.$('.userOffline-led').tooltip();
    AJS.$('#sg-planner-from').datePicker({ 'overrideBrowserDefault': true });
    AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true });

    if (JIRA.Version.isGreaterThanOrEqualTo("8.3.0")) {
      AJS.$('#sg-planner-to').focusout(function () {


        if (!($('#sg-planner-to').val() === '') && $('#sg-planner-from').val() === '') {
          AJS.$('#sg-planner-from').datePicker().setMax(AJS.$(this).val());
          var fromMilliSec = Date.parse($('#sg-planner-from').val());
          var toMilliSec = Date.parse($('#sg-planner-to').val());


        } else if (!($('#sg-planner-to').val() === '') && !($('#sg-planner-from').val() === '')) {
          AJS.$('#sg-planner-from').datePicker().setMax(AJS.$(this).val());
          var fromMilliSec = Date.parse($('#sg-planner-from').val());
          var toMilliSec = Date.parse($('#sg-planner-to').val());

        } else {
          AJS.$('#sg-planner-from').datePicker().setMax('');


        }

      });
      AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true });

      AJS.$('#sg-planner-from').focusout(function () {
        // AJS.$('#sg-planner-from').datePicker({ 'overrideBrowserDefault': true});
        // AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true});

        AJS.$('#sg-planner-to').datePicker().setMin(AJS.$(this).val());

      });
    } else {
      AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true, minDate: AJS.$('#sg-planner-from').val() });
    }

    AJS.$('#sg-planner-type').change(function () {
      AJS.$('#sg-planner-from').datePicker({ 'overrideBrowserDefault': true });
      AJS.$('#sg-planner-to').datePicker({ 'overrideBrowserDefault': true });

    });

  }


  var calendarEl = document.getElementById('calendar');
  if (AJS.$(calendarEl).length > 0) {


    var userName = AJS.$('#profileUserName').val();
    var calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      //initialDate: '2021-01-07',
      initialDate: new Date(),
      defaultDate: new Date(),
      eventClassNames: 'sg-calView js-calView2',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay,listYear,monthGridYear'
      },
      eventSources: [
        // your event source
        {
          url: AJS.contextPath() + '/rest/absenter/1.0/plannedAbsences?userName=' + userName,
          method: 'GET',

          failure: function () {
            console.error('[ABSENCE-PLANNER-ERROR]there was an error while fetching events!');
          },
          success: function () {
            AJS.$('.fc-event-title').each(function () {
              var html = AJS.$(this).html();
              AJS.$(this).html('<span class="aui-icon aui-icon-small aui-iconfont-recent">Rec</span>' + html);
            });
          },
          color: '#BF2600',   //#8E1010   a non-ajax option
          textColor: 'white' // a non-ajax option
        }

        // any other sources...

      ]
    });

    calendar.render();

    AJS.$('#sg-calendarView').click(function () {

      $('.fc-dayGridMonth-button').trigger("click");
      setTimeout(function () {
        $('.fc-dayGridMonth-button').trigger("click");

        AJS.$('.fc-event-time').remove();

      }, 100);
      setTimeout(function () {
        $('.fc-dayGridMonth-button').trigger("click");
        AJS.$('.fc-event-time').remove();
      }, 500);

    });

  }
  // });

  AJS.$("#sg-planner-configdialog-show-button").click(function (e) {
    AJS.$('#sg_representatives').auiSelect2();

    e.preventDefault();
    AJS.$('#sg-close-dialog').click(function () {
      AJS.dialog2("#sg-planner-config-dialog").hide();
    });
    AJS.dialog2("#sg-planner-config-dialog").show();
    var getRepresentativesList = [];
    AJS.$('select.sg_assignee').each(function () {
      try {
        AJS.$(this).auiSelect2();
      } catch (error) {
      }
    });
    var afterUsersLists = function (e) {
      AJS.$('select.sg_representatives').each(function () {
        var id = AJS.$(this).attr("id");
        var selectList = AJS.$(this);
        var assignee = AJS.$(selectList).val();

        var defAssignee = "";
        AJS.$.each(e, function (i, elmnt) {
          if ($('#' + id).find("option[value='" + elmnt.name + "']").length) {

          } else {
            var newOption = new Option(elmnt.displayName, elmnt.name, true, true);
            $('#' + id).append(newOption);
            if (assignee === elmnt.name) {
              defAssignee = assignee;
            }
          }
        });
      });
      /////AJS.$('#sg_load_assignee').hide();
    }
    var findAssignableUsers = function () {
      var profileUser = AJS.$('#profileUserName').val();
      var userName = AJS.$('#sg-planner-locum').val();
      AJS.$(document).on("keyup", '.select2-input', function (e) {
        var id = AJS.$(this).attr("id");
        console.log(AJS.$(this).val());
        var userName = AJS.$(this).val();

        if (userName.length > 2) {
          /////AJS.$('#sg_load_assignee').show();
          var url = AJS.contextPath() +
            '/rest/absenter/1.0/findAssignableUsers?profileUser=' + profileUser + "&userName=" + userName;

          var getRepresentatives = $.getJSON(url);
          getRepresentatives.done(function (e) {
            getRepresentativesList = getRepresentativesList.concat(e);

            afterUsersLists(getRepresentativesList);

          });
        }
      });

    }
    findAssignableUsers();


  });

  // Hides the dialog
  AJS.$("#sg-config-dialog-submit-button").click(function (e) {
    e.preventDefault();
    var representativeValue = AJS.$('#sg_representatives').val();
    var assignable = AJS.$('#sg-planner-assignable').is(":checked");
    var commentOnAssignment = AJS.$('#sg-planner-comment').is(":checked");

    representativeValue += ":" + assignable + ":" + commentOnAssignment;

    var profileUser = AJS.$('#profileUserName').val();
    var jiraPath = AJS.Meta.get('context-path');
    var setRepresentativeUser = {
      type: 'POST',
      url: jiraPath + '/rest/absenter/1.0/setLocum?userName=' + profileUser + '&locum=' + representativeValue,
      contentType: 'application/json; charset=utf-8',
      timeout: -1,
      async: true,
      success: function (data) {
        var myFlag = AJS.flag({
          type: 'success',
          body: 'Configuration updated successfully!',
          close: 'auto',
        });

      }
    }
    AJS.$.ajax(setRepresentativeUser);

    AJS.dialog2("#sg-planner-config-dialog").hide();
  });
});