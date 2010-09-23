/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
    CONFIGURATION PARAMETERS
*/

(function($){
    
    $.uportal = $.uportal || {};
    
    // Merge all options into callerSettings object.
    $.uportal.UportalLayoutManager = function(callerSettings) {
        var settings = $.extend({
            preferencesUrl: 'mvc/layout',
            channelListUrl: 'mvc/channelList',
            subscriptionListUrl: 'mvc/tabList',
            portalUrl: null,
            mediaPath: null,
            currentSkin: null,
            isFocusMode: false,
            isFragmentMode: false,
            messages: {}
        }, callerSettings||{});
        
    /*
        INTERNAL METHODS
    */
    
    // Initialization tasks for non-focused mode.
    var initportal = function() {

        if ($("#portalNavigationList li.active").size() > 0) {
            settings.tabId = $("#portalNavigationList li.active").attr("id").split("_")[1];
        }

        
        if (settings.isFragmentMode) {
            // tabs permissions manager
            $(".edit-page-permissions-dialog").dialog({ width: 550, modal: true, autoOpen: false });
            settings.pagePermissionsManager = uportal.FragmentPermissionsManager(".edit-page-permissions-dialog", {
                savePermissionsUrl: settings.preferencesUrl,
                elementExtractor: function(that, link){
                    return $("#portalNavigationList li.active"); 
                },
                titleExtractor: function(element){ return "tab"; },
                selectors: {
                    formTitle: "h2"
                }
            });
            $("#editPagePermissionsLink a").click(function(){
                settings.pagePermissionsManager.refresh($(this));
                $(".edit-page-permissions-dialog").dialog("open");
            });
            
            // columns permissions manager
            $(".edit-column-permissions-dialog").dialog({ width: 550, modal: true, autoOpen: false });
            settings.columnPermissionsManager = uportal.FragmentPermissionsManager(".edit-column-permissions-dialog", {
                savePermissionsUrl: settings.preferencesUrl,
                elementExtractor: function(that, link){
                    return $(link).parents(".portal-page-column"); 
                },
                titleExtractor: function(element){ 
                    return "Column " + ($(".portal-page-column").index(element) + 1); 
                },
                selectors: {
                    formTitle: "h2"
                }
            });
            $(".portal-column-permissions-link").click(function(){
                settings.columnPermissionsManager.refresh($(this));
                $(".edit-column-permissions-dialog").dialog("open");
            });
            
            // portlet permissions manager
            $(".edit-portlet-permissions-dialog").dialog({ width: 550, modal: true, autoOpen: false });
            settings.portletPermissionsManager = uportal.FragmentPermissionsManager(".edit-portlet-permissions-dialog", {
                savePermissionsUrl: settings.preferencesUrl,
                elementExtractor: function(that, link){
                    return $(link).parents(".up-portlet-wrapper"); 
                },
                titleExtractor: function(element){ return element.find(".up-portlet-wrapper-inner h2 a").text(); },
                selectors: {
                    formTitle: "h2"
                },
                listeners: {
                    onUpdatePermissions: function(element, newPermissions) {
                        if (!newPermissions.movable) {
                            element.addClass("locked").removeClass("fl-reorderer-movable-default");
                            element.find("[id*=toolbar_]").removeClass("ui-draggable");
                        } else {
                            element.removeClass("locked").addClass("fl-reorderer-movable-default");
                            element.find("[id*=toolbar_]").addClass("ui-draggable");
                        }
                        settings.myReorderer.refresh();
                    }
                }
            });
            $(".portlet-permissions-link").click(function(){
                settings.portletPermissionsManager.refresh($(this));
                $(".edit-portlet-permissions-dialog").dialog("open");
            });
        }
        
        // set click handlers for tab moving and editing links
        $("#addTabLink").click(addTab);
        $("#deletePageLink").click(deleteTab);
        $("#movePageLeftLink").click(function(){moveTab('left')});
        $("#movePageRightLink").click(function(){moveTab('right')});
        initTabEditLinks();
        
    };
    
        // Tab editing persistence functions
        var addTab = function() {
            updateLayout({action: 'addTab', tabName: "My Tab", widths: [50, 50]}, 
                function(data) {
                    // redirect the browser to the new tab
                    window.location = getTabUrl(data.tabId);
                }
            );
        };
        var deleteTab = function() {
            if (!confirm(settings.messages.confirmRemoveTab)) return false;
            updateLayout({action: 'removeElement', elementID: settings.tabId}, 
                function(xml) {
                    window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1"; 
                }
            );
        };
        var moveTab = function(direction) {
            var tab = $("#portalNavigation_" + settings.tabId);
            var method = 'insertBefore';
            var targetId = null;
            var tabPosition = 1;
        
            // move the tab node
            if (direction == 'left') tab.insertBefore(tab.prev());
            else tab.insertAfter(tab.next());
            
            // get the target node and method parameters
            if (tab.is(":last-child")) {
                method = 'appendAfter';
                targetId = tab.prev().attr("id").split("_")[1];
            } else
                targetId = tab.next().attr("id").split("_")[1];
        
            // figure out what the current tab's number is
            $("[id*=portalNavigation_]").each(function(i){
                if ($(this).attr("id") == tab.attr("id"))
                    tabPosition = i+1;
            });
            
            updateLayout({
                action: 'moveTab',
                sourceID: settings.tabId,
                method: method,
                elementID: targetId,
                tabPosition: tabPosition
            });
            redoTabs(settings.tabId);
            initTabEditLinks();
        };
        var initTabEditLinks = function() {
            var tab = $("#portalNavigation_" + settings.tabId);
            if (tab.not(":first-child") && tab.prev().hasClass("movable"))
                $("#movePageLeftLink").css("display", "block");
            else 
                $("#movePageLeftLink").css("display", "none");
                
            if (tab.is(":last-child")) 
                $("#movePageRightLink").css("display", "none");
            else
                $("#movePageRightLink").css("display", "block");
                
            var links = $("#portalNavigationList .portal-navigation");
            links.each(function(i){
                if (links.length == 1) $(this).removeClass("first").removeClass("last").addClass("single");
                else if (i == 0) $(this).removeClass("single").removeClass("last").addClass("first");
                else if (i == links.length-1) $(this).removeClass("single").removeClass("first").addClass("last");
                else $(this).removeClass("single").removeClass("last").removeClass("first");
            });
                
            links = $("#portalFlyoutNavigationInner_" + settings.tabId).find(".portal-subnav").not("[display=none]");
            links.each(function(i){
                if (links.length == 1) $(this).removeClass("first").removeClass("last").addClass("single");
                else if (i == 0) $(this).removeClass("single").removeClass("last").addClass("first");
                else if (i == links.length-1) $(this).removeClass("single").removeClass("first").addClass("last");
                else $(this).removeClass("single").removeClass("last").removeClass("first");
            });
        };
        var redoTabs = function(tabId) {
            $("[id*=tabLink_]").each(function(i){
                $(this).attr("href", settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (i+1));
            });
        //    fly.closeSubnav(tabId);
        };


        /**
         * Initialize all required components for the focused view of the portal.
         */
        var initfocusedportal = function () {
            // initialize the focused content adding dialog link
            $("#focusedContentDialogLink").click(function () {
                
                // initialize the dialog
                $(".focused-content-dialog").dialog({ width: 500, modal: true });
                
                // wire the form to persist portlet addition
                $(".focused-content-dialog form").submit(function () {                    
                    var portletId, tabId, form;
                    
                    // collect form data
                    form = this;
                    portletId = form.portletId.value;
                    tabId = $(form).find("[name=targetTab]:checked").val();
                    
                    // persist the portlet addition
                    updateLayout(
                        {
                            action: "addPortlet",
                            channelID: portletId,
                            position: "insertBefore",
                            elementID: tabId
                        },
                        function(xml) {
                            window.location = getTabUrl(tabId);
                        }
                    );
                    return false;
                });
                
                // re-wire the form to open the initialized dialog
                $(this).unbind("click").click(function () {
                    $(".focused-content-dialog").dialog("open");
                });
            });
        };
        
        var updateLayout = function(data, success) {
            $.ajax({
                url: settings.preferencesUrl,
                type: "POST",
                data: data,
                dataType: "json",
                success: success,
                error: function(request, text, error) {
                    if (console) {
                        console.log(request, text, error);
                    }
                }
            });
        };//end:function.
        
        var getPortletUrl = function (fname) {
            return "/uPortal/p/" + fname;
        };
        
        var getTabUrl = function (tabId) {
            return "/uPortal/f/" + tabId;
        };
        
        // Initialize portal code.
        if (settings.isFocusMode) initfocusedportal(); else initportal();
    };
})(jQuery);
