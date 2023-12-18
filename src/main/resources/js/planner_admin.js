AJS.toInit(function () {
    AJS.$('#sg-selected-groups').auiSelect2();
    AJS.$('#sg-selected-groups').auiSelect2();
    AJS.$('#sg-access-selected-groups').auiSelect2();
    AJS.$('#sg-access-selected-groups').auiSelect2();
    var newGroup = ' <div class="groupItem" groupName="§GroupName" style="" id="sgplanner_div_#Counter_users">' +
        '<div id="sgplanner_div_#Counter_inner_users" title=""><span class="aui-icon aui-icon-small aui-iconfont-admin-roles" style="">Group</span>' +
        '<span id="sgplanner_div_#Counter_inner_users_span"><b>Group</b>: §GroupName</span>' +
        '<span class="aui-icon aui-icon-small aui-iconfont-delete groupTrash" style="">Delete Group</span>' +
        '</div></div>';
    AJS.$('#sgplanner_add_group_users').click(function () {
        var groupName = AJS.$('#sg-selected-groups').val();
        var count = AJS.$('#sgplanner_display_div_users').find(".groupItem").length;
        var found = false;
        if(groupName=="-1")
        return ;
        $.each(AJS.$('#sgplanner_display_div_users').find(".groupItem"),function(i,item){
            if(AJS.$(item).attr("groupName")===groupName)
                {
                    found = true;
                    return;
                }
        });
        if(found===true){
            var myFlag = AJS.flag({
                type: 'Error',
                body: "Group is already in the list."
            });
            return 0;
        }
        var newGrouptHtml = newGroup.replaceAll("#Counter", count);
        newGrouptHtml = newGrouptHtml.replaceAll("§GroupName", groupName);
        
        AJS.$('#sgplanner_display_div_users').append(newGrouptHtml);
       // AJS.$('#sg-update-configs').css("color","red");

    });

    AJS.$('#sgplanner_access_access_add_group_users').click(function(){

        var groupName = AJS.$('#sg-access-selected-groups').val();
        var count = AJS.$('#sgplanner_access_display_div_users').find(".groupItem").length;
        var found = false;
        if(groupName=="-1")
        return ;
        $.each(AJS.$('#sgplanner_access_display_div_users').find(".groupItem"),function(i,item){
            if(AJS.$(item).attr("groupName")===groupName)
                {
                    found = true;
                    return;
                }
        });
        if(found===true){
            var myFlag = AJS.flag({
                type: 'Error',
                body: "Group is already in the list."
            });
            return 0;
        }
        var newGrouptHtml = newGroup.replaceAll("#Counter", count);
        newGrouptHtml = newGrouptHtml.replaceAll("§GroupName", groupName);
        
        AJS.$('#sgplanner_access_display_div_users').append(newGrouptHtml);
       // AJS.$('#sg-update-configs').css("color","red");


    });
    var newUser = ' <div class="userItem" userName="§userName" style="" id="sgplanner_div_#Counter_users">' +
    '<div id="sgplanner_div_#Counter_inner_users" title=""><span class="aui-icon aui-icon-small aui-iconfont-admin-roles" style="">User</span>' +
    '<span id="sgplanner_div_#Counter_inner_users_span"><b>User</b>: §userFullName</span>' +
    '<span class="aui-icon aui-icon-small aui-iconfont-delete userTrash" style="">Delete User</span>' +
    '</div></div>';

    AJS.$('#sgplanner_add_user_users').click(function () {
        var userName = AJS.$('#sg_permittedUsers').val();
        var userFullName = AJS.$('#sg_permittedUsers :selected').text();
        var count = AJS.$('#sgplanner_display_div_permittedusers').find(".userItem").length;
        var found = false;
        if(userName=="-1")
        return ;
        $.each(AJS.$('#sgplanner_display_div_permittedusers').find(".userItem"),function(i,item){
            if(AJS.$(item).attr("userName")===userName)
                {
                    found = true;
                    return;
                }
        });
        if(found===true){
            var myFlag = AJS.flag({
                type: 'Error',
                body: "User is already in the list."
            });
            return 0;
        }
        var newUserstHtml = newUser.replaceAll("#Counter", count);
        newUserstHtml = newUserstHtml.replaceAll("§userName", userName);
        newUserstHtml = newUserstHtml.replaceAll("§userFullName", userFullName);

        
        
        AJS.$('#sgplanner_display_div_permittedusers').append(newUserstHtml);
       // AJS.$('#sg-update-configs').css("color","red");

    });

    AJS.$("#sgplanner_display_div_permittedusers").on("click",'.userTrash',function (e) {
        AJS.$(this).closest(".userItem").remove();
    });

    AJS.$("#sgplanner_display_div_users").on("click",'.groupTrash',function (e) {
        AJS.$(this).closest(".groupItem").remove();

    });
    AJS.$("#sgplanner_access_display_div_users").on("click",'.groupTrash',function (e) {
        AJS.$(this).closest(".groupItem").remove();

    });
    AJS.$("#sgplanner_display_div_projects").on("click",'.projectTrash',function (e) {
        AJS.$(this).closest(".projectItem").remove();
    });


    AJS.$('#sg-update-configs').click(function (e) {
        e.preventDefault();
        saveConfigurations();
    });
    var saveConfigurations = function () {
        var PlannerConfig = new Object();
        PlannerConfig.disableAutoComment = AJS.$('#disableAutoComment').is(":checked");
        PlannerConfig.enabledForSystemAdmins =  AJS.$('#sysAdministrators').is(":checked");;
       
        var groups = [];
        $.each(AJS.$('#sgplanner_display_div_users').find(".groupItem"),function(i,group){
            groups.push(AJS.$(group).attr("groupName"));
        });



        PlannerConfig.groups =groups.toString();
        var accessGroups = [];
        $.each(AJS.$('#sgplanner_access_display_div_users').find(".groupItem"),function(i,group){
            accessGroups.push(AJS.$(group).attr("groupName"));
        });
        PlannerConfig.accessGroups = accessGroups.toString();
        PlannerConfig.accessUsers = "";
        var users = [];
        $.each(AJS.$('#sgplanner_display_div_permittedusers').find(".userItem"),function(i,user){
            users.push(AJS.$(user).attr("userName"));
        });
        PlannerConfig.users =users.toString();

        var parameters = JSON.stringify(PlannerConfig);
        var jiraPath = AJS.Meta.get('context-path');
        var uap_EpicConfigRestCall = {
            type: 'POST',
            url: jiraPath + '/rest/absenter/1.0/updatePlannerSettings',
            data: parameters,
            contentType: 'application/json; charset=utf-8',
            timeout: 0,
            async: true,
            success: function (data) {
                var myFlag = AJS.flag({
                    type: 'Success',
                    body: "Configurations updated successfully!",
                });
            },
            error: function (data) {
                var myFlag = AJS.flag({
                    type: 'Error',
                    body: "Configurations does not updated successfully!"
                });
            }
            ,
            complete: function (data) {
                /*   var myFlag = AJS.flag({
                       type: 'Sucess',
                       body: "Configurations updated Completed!"
                   });*/
            }
        };
        AJS.$.ajax(uap_EpicConfigRestCall);
    }


var getPermittedUsersList = [];
    var afterUsersLists = function (e) {
        AJS.$('select.sg_permittedUsers').each(function () {
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
      var findPermittableUsers = function () {
      //  var userName = AJS.$('#sg-planner-permittableuser').val();
        AJS.$(document).on("keyup", '.select2-input', function (e) {
          var id = AJS.$(this).attr("id");
          console.log(AJS.$(this).val());
          var userName = AJS.$(this).val();
  
          if (userName.length > 2) {
            /////AJS.$('#sg_load_assignee').show();
            var url = AJS.contextPath() + '/rest/absenter/1.0/findPermittableUsers?searchToken=' + userName;
  
            var getPermittedUsers = $.getJSON(url);
            getPermittedUsers.done(function (e) {
              getPermittedUsersList = getPermittedUsersList.concat(e);
  
              afterUsersLists(getPermittedUsersList);
  
            });
          }
        });
  
      }
      AJS.$('#sg_permittedUsers').auiSelect2();
      AJS.$('#sg_access_permittedUsers').auiSelect2();


      findPermittableUsers();
});