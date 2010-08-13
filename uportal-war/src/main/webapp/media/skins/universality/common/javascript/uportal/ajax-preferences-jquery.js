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
    
    var availableLayouts = [ [ 100 ],
                      [ 40, 60 ], [ 50, 50 ], [ 60, 40 ],
                      [ 33, 34, 33 ], [ 25, 50, 25 ],
                      [ 25, 25, 25, 25 ] ];

	
    // if the uPortal scope is not available, add it
    $.uportal = $.uportal || {};
    
    $.uportal.UportalLayoutManager = function(callerSettings) {
		var settings = $.extend({
		    preferencesUrl: 'mvc/layout',
		    channelListUrl: 'mvc/channelList?xml=true',
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

		// Initialization tasks for non-focused mode
		var initportal = function() {

		    settings.columnCount = $("#portalPageBodyColumns [id^=column_]").size(); 
		    if ($("#portalNavigationList li.active").size() > 0) {
			    settings.tabId = $("#portalNavigationList li.active").attr("id").split("_")[1];
		    }
		
			// initialize dialog menus
			$("#contentDialogLink").click(initializeContentAddingMenu);
            $("#usePortletDialogLink").click(initializeUsePortletMenu);
			$("#skinDialogLink").click(initializeSkinMenu);
		
			// set up the Fluid reorderer to control portlet drag and drop
		    var options = {
		         selectors: {
			        columns: ".portal-page-column-inner",
			        modules: ".up-portlet-wrapper",
			        lockedModules: ".locked",
			        dropWarning: $("#portalDropWarning"),
			        grabHandle: "[id*=toolbar_]"
		         },
		         listeners: {
		         	afterMove: movePortlet
		         },
		         styles: {
		         	mouseDrag: "fl-reorderer-movable-dragging-mouse"
		         }
		    };
		    settings.myReorderer = up.fluid.reorderLayout ("#portalPageBodyColumns",options);

		    initializePageLayoutDialog();
		    
		    if (settings.isFragmentMode) {
                // tabs permissions manager
	            $(".edit-page-permissions-dialog").dialog({ width: 550, modal: true, autoOpen: false });
                settings.pagePermissionsManager = uportal.FragmentPermissionsManager(".edit-page-permissions-dialog", {
                    savePermissionsUrl: settings.preferencesUrl,
                    elementExtractor: function(that, link){
                        return $(link).parents(".portal-flyout-container"); 
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

		    // add onclick events for portlet delete buttons
			$('a[id*=removePortlet_]').each(function(i){$(this).click(function(){deletePortlet(this.id.split("_")[1]);return false;});});	
		
			// set click handlers for tab moving and editing links
            $("#addTabLink").click(addTab);
			$("#deletePageLink").click(deleteTab);
			$("#movePageLeftLink").click(function(){moveTab('left')});
			$("#movePageRightLink").click(function(){moveTab('right')});
			initTabEditLinks();
		
		};
		
		var initializePageLayoutDialog = function() {
	          // initialize the page layout modal dialog
            $("#pageLayoutDialog").dialog({ width: 550, modal: true, autoOpen: false });
            
            // initialize the page layout editing component within the just-created
            // modal dialog
            uportal.PageManager("#pageLayoutDialog", {
                // set the configured page name to the name of the current tab
                currentPageName: $("#tabLink_" + settings.tabId + " > span").text(),
                isDefault: true,
                currentLayout: getCurrentLayout(),
                allowedLayouts: getPermittedLayouts(),
                savePermissionsUrl: settings.preferencesUrl,
                imagePath: settings.mediaPath + "/" + settings.currentSkin + "/images/",
                selectors: { },
                listeners: {
                    onUpdateLayout: changeColumns,
                    onUpdatePageName: updatePageName,
                    onUpdateIsDefault: null
                }
            });

            // configure the new page editing dialog to launch when either of
            // the relevant page editing links are clicked
            $(".edit-page-link").click(function(){
              $("#pageLayoutDialog").dialog("open");
            });

		};
		
		// Initialization tasks for focus mode
		var initfocusedportal = function() {
			$("#focusedContentDialogLink").click(initializeFocusedContentMenu);
		};
		
		var initializeContentAddingMenu = function() {
		    
		    // initialize the modal container dialog and tabs
            $("#channelAddingTabs").tabs();
			$("#contentAddingDialog").dialog({ width:550, modal:true});
			$("#contentDialogLink")
				.unbind('click', initializeContentAddingMenu)
				.click(function(){$("#contentAddingDialog").dialog('open');});
			
			// initialize the portlet browser in the first tab
	        settings.addPortletsBrowser = up.PortletBrowser("#channel-tab-1 .portlet-browser", 
                { 
                    portletRegistry: { 
                        type: "up.PortletRegistry",
                        options: { portletListUrl: settings.channelListUrl } 
                    },
                    categoryListView: {
                        type: "up.AjaxLayoutCategoryListView"
                    },
                    portletListView: {
                        type: "up.AjaxLayoutPortletListView"
                    },
                    listeners: {
                        onPortletSelect: function(that, portlet) {
                            var options = { action: 'addPortlet', channelID: portlet.id };
                            var firstChannel = $("div[id*=portlet_]:not(.locked)");
                            if (firstChannel.size() == 0) {
                                options['elementID'] = settings.tabId;
                            } else {
                                options['elementID'] = firstChannel.attr("id").split("_")[1];
                                options['position'] = 'insertBefore';
                            }
                            updateLayout(options,
                               function(data) {
                                  window.location = settings.portalUrl; 
                               }
                            );
                        }
                    }
                }
	        );

	        // initialize the DLM subscribe tab dialog in the second tab
            settings.fragmentBrowser = uportal.FragmentBrowser($("#channel-tab-2"), 
                {
                    fragmentServiceUrl: settings.subscriptionListUrl,
                    listeners: { 
                        onFragmentSelect: function(fragment) {
                            var tab = $("#portalNavigation_" + settings.tabId);
                            var tabPosition = 1;
                            var targetId = null;
                            // figure out what the current tab's number is
                            $("[id*=portalNavigation_]").each(function(i){
                                if ($(this).attr("id") == tab.attr("id"))tabPosition = i+1;
                                targetId = $(this).attr("id").split("_")[1];
                            });
        
                            var subscribeToTabVal = $("#subscribeTabId").val();
                            updateLayout({action: "subscribeToTab" ,sourceID: fragment.ownerID,method: 'appendAfter', tabPosition: tabPosition, elementID: targetId  }, function(xml) {
                                window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
                                        ($("#portalNavigationList > li").length + 1);
                            });
                        }
                    }
                }
            );
		}

        var initializeUsePortletMenu = function() {
            $("#usePortletDialog").dialog({ width:550, modal:true});
            $("#usePortletDialogLink")
                .unbind('click', initializeUsePortletMenu)
                .click(function(){$("#usePortletDialog").dialog('open');});
            
            settings.usePortletsBrowser = up.PortletBrowser("#usePortletDialog", 
                { 
                    portletRegistry: { 
                        type: "up.PortletRegistry",
                        options: { portletListUrl: settings.channelListUrl } 
                    },
                    categoryListView: {
                        type: "up.AjaxLayoutCategoryListView"
                    },
                    portletListView: {
                        type: "up.AjaxLayoutPortletListView"
                    },
                    listeners: {
                        onPortletSelect: function(that, portlet) {
                            window.location = "/uPortal/p/" + portlet.fname;
                        }
                    }
                }
            );
		};
		
		var initializeFocusedContentMenu = function() {
			$("#focusedContentAddingDialog").dialog({ width:500, modal:true});
			$("#focusedContentDialogLink")
				.unbind('click', initializeFocusedContentMenu)
				.click(function(){$("#focusedContentAddingDialog").dialog('open');});
		    $("#focusedContentAddingDialog form").submit(function(){return addFocusedChannel(this);});
		};
		
		var getCurrentLayout = function() {
            var layouts = [];
            $('#portalPageBodyColumns > [id^=column_]').each(function(){
                var flClass = $(this).get(0).className.match("fl-col-flex[0-9]+");
                if (flClass != null) {
                    layouts.push(Number(flClass[0].match("[0-9]+")[0]));
                }
            });
            if (layouts.length == 0) layouts.push(100);
            return layouts;
		};
		
		var getDeletableColumns = function() {
            var columns = $('#portalPageBodyColumns > [id^=column_]');
            
            // a deletable column must be marked deletable and contain no locked
            // children
            var deletableColumns = columns.filter(".deletable:not(:has(.locked))");

            var contentColumns = deletableColumns.filter(":has(.up-portlet-wrapper)");
            if (contentColumns.size() > 0) {
                var acceptorColumns = columns.filter(".canAddChildren");
                // if there are no acceptor columns, mark any columns that 
                // have content as undeletable
                if (acceptorColumns.size() == 0) {
                    deletableColumns = deletableColumns.filter(":not(:has(.up-portlet-wrapper))");
                }
            }

            return deletableColumns;
		};
		
		var getPermittedLayouts = function() {
            var canAddColumns = $("#portalFlyoutNavigation_" + settings.tabId).hasClass("canAddChildren");
		    var columns = $('#portalPageBodyColumns > [id^=column_]');
		    
		    // a deletable column must be marked deletable and contain no locked
		    // children
            var deletableColumns = columns.filter(".deletable:not(:has(.locked))");
            
		    // set the minimum number of columns according to how
		    // many deletable columns the layout currently contains
            var minColumns = columns.length - deletableColumns.length;
		    
		    var contentColumns = deletableColumns.filter(":has(.up-portlet-wrapper)");
		    if (contentColumns.size() > 0) {
	            var acceptorColumns = columns.filter(".canAddChildren");
	            // if there are no acceptor columns, mark any columns that 
	            // have content as undeletable
	            if (acceptorColumns.size() == 0) {
	                deletableColumns = deletableColumns.filter(":not(:has(.up-portlet-wrapper))");
	                minColumns = columns.length - deletableColumns.length;
	            } else {
	                var separateAcceptor = false;
	                for (var i = 0; i < acceptorColumns.length; i++) {
	                    if ($.inArray(acceptorColumns[i], deletableColumns) < 0) {
	                        separateAcceptor = true;
	                        break;
	                    }
	                }
	                if (!separateAcceptor) minColumns++;
	            }
		    }
		    		    
		    var permitted = [];
		    $(availableLayouts).each(function(idx, layout){
		        if (
		            (canAddColumns || layout.length <= columns.length) &&
		            (layout.length >= minColumns)
		           ) {
		            permitted.push(layout);
		        }
		    });
		    return permitted;
		};
		
		var updatePageName = function(name) {
			$("#tabLink_" + settings.tabId + " > span").text(name);
			$("#portalPageBodyTitle").text(name);
			updateLayout({action: 'renameTab', tabId: settings.tabId, tabName: name});
			return false;
		};
		
		// Column editing persistence functions
		var changeColumns = function(newcolumns) {
		    
		    var post = {action: 'changeColumns', tabId: settings.tabId, widths: newcolumns};
		    
		    if (newcolumns.length < settings.columnCount) {
		        var deletables = getDeletableColumns();
                var deletes = deletables.filter(":gt(" + (newcolumns.length -1) + ")");
                post.deleted = [];
                $(deletes).each(function(idx, deletable){
                    post.deleted.push($(deletable).attr("id").split("_")[1]);
                });
		        
                var acceptors = $("#portalPageBodyColumns > [id^=column_].canAddChildren");
		        var acceptor = acceptors.filter(":first");
		        post.acceptor = $(acceptor).attr("id").split("_")[1];
		    }
		    
		    settings.columnCount = newcolumns.length;
		    
			updateLayout(post, 
				function(data) { 
			    
			        // add any new columns to the page
			    	$(data.newColumnIds).each(function(){
			    	    var id = this;
			    		$("#portalPageBodyColumns")
			    			.append(
			    				$(document.createElement('div')).attr("id", 'column_' + id)
			    					.addClass("portal-page-column movable deletable editable canAddChildren")
			    					.html("<div id=\"inner-column_" + id + "\" class=\"portal-page-column-inner\"></div>")
			    			);
			    	});
				    	
			    	// remove any deleted columns from the page
			        $(deletes).each(function(idx, del){
			            $(this).find("div[id*=portlet_]").each(function(idx, portlet){
			                $(portlet).appendTo(acceptor);
			            });
			            $(this).remove();
			        });
				    
			        // update the widths and CSS classnames for each column
			        // on the page
				    $('#portalPageBodyColumns > [id^=column_]').each(function(i){
				        
				        var column = $(this).removeClass("single left right");
				        $(this.className.split(" ")).each(function(idx, className){
				            if (className.match("fl-col-flex")) $(column).removeClass(className);
				        });
				        
				        var newclasses = "fl-col-flex" + newcolumns[i];
				    	if (newcolumns.length == 1) newclasses += " single";
				    	else if (i == 0) newclasses += " left";
				    	else if (i == newcolumns.length - 1) newclasses += " right";
				    	else newclasses += " middle";
                        $(column).addClass(newclasses);
				    });
				    
			    	settings.myReorderer.refresh();
				}
			);
		};
		
		// Portlet editing persistence functions
		var deletePortlet = function(id) {
			if (!confirm(settings.messages.confirmRemovePortlet)) return false;
			$('#portlet_'+id).remove();
			updateLayout({action: 'removeElement', elementID: id});
		};
		
		
		// Tab editing persistence functions
		var addTab = function() {
			updateLayout({action: 'addTab', tabName: "My Tab", widths: [50, 50]}, 
			    function(data) {
			        window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + ($("#portalNavigationList > li").length + 1);
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
			if (tab.not(":first-child") && tab.prev().hasClass("movable-tab"))
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
		//	fly.closeSubnav(tabId);
		};
		var movePortlet = function(movedNode) {
		   var method = 'insertBefore';
		   var target = null;
		   if ($(movedNode).nextAll('div[id*=portlet_]').size() > 0) {
		       target = $(movedNode).nextAll('div[id*=portlet_]').get(0);
		   } else if ($(movedNode).prevAll('div[id*=portlet_]').size() > 0) {
		       target = $(movedNode).prevAll('div[id*=portlet_]').get(0);
		       method = 'appendAfter';
		   } else {
		       target = $(movedNode).parent();
		   }
		   var columns = $('#portalPageBodyColumns > [id^=column_]');
		   updateLayout({ action: 'movePortlet', method: method, elementID: $(target).attr('id').split('_')[1], sourceID: $(movedNode).attr('id').split('_')[1]});
		};
		
		var addFocusedChannel = function(form) {
		
		    var channelId = form.channelId.value;
		    var tabPosition, elementId;
		    
		    $("#focusedContentAddingDialog input[name=targetTab]").each(function(i){
		    	if ($(this).is(":checked")) {
		    		tabPosition = i+1;
		    		elementId = $(this).val();
		    	}
		    });
		    
		    updateLayout(
		        {
		    		action: 'addPortlet',
		    		channelID: channelId,
		    		position: 'insertBefore',
		    		elementID: elementId
		    	},
		    	function(xml) {
					window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + tabPosition;
		    	}
		    );
			return false;
		
		};
		
		var initializeSkinMenu = function() {
			$("#skinChoosingDialog").dialog({height:450, width:500, modal:true});
			$("#skinDialogLink")
				.unbind('click', initializeSkinMenu)
				.click(function(){$("#skinChoosingDialog").dialog('open');});
		
		    var skinMenu = $("#skinList").html("").css("padding", "10px");
		    $("#skinChoosingDialog form").submit(function(){return chooseSkin(this);});
		    $.get(settings.mediaPath + '/skinList.xml?noCache=' + new Date().getTime(), { },
		    	function(xml){
					settings.skinXml = xml;
					$("skin", settings.skinXml).each(function(i){
						var key = $(this).children("skin-key").text();
						
						var input
						if ($.browser.msie) {
						    if (key == settings.currentSkin) {
		    				    input = $(document.createElement("<input type=\"radio\" name=\"skinChoice\" value=\"" + key + "\" checked=\"true\"/>"));
		    				} else {
		                        input = $(document.createElement("<input type=\"radio\" name=\"skinChoice\" value=\"" + key + "\"/>"));    				
		    				}
						} else {
		                    input = $(document.createElement("input")).attr("type", "radio")
		                        .attr("name", "skinChoice").val(key);
			                if (key == settings.currentSkin)
			                    input.attr("checked", true);
						}
						var widget = $(document.createElement("div"))
							.addClass("fl-widget");
						widget.append($(document.createElement("div"))
								.addClass("fl-widget-titlebar")
								.append(input)
								.append($(document.createElement("h2")).text($(this).children("skin-name").text()))
							);
						widget.append($(document.createElement("div"))
								.addClass("fl-widget-content")
								.append($(document.createElement("p")).text($(this).children("skin-description").text()))
								.append($(document.createElement("img")).attr("src", settings.mediaPath + "/" + key + "/" + "thumb.gif"))
							);
						skinMenu.append(widget);
					});
		
		            skinMenu.css("height", "300px").css("overflow", "auto");
		            
		        	// remove the loading graphics and message
		        	$("#skinLoading").css("display", "none");
		            $("#skinChoosingDialog").parent().parent().css("height", $("#skinChoosingDialog").parent().height() + 20);
		    	}
		    );
		};
		
		var chooseSkin = function(form) {
		    var newskin = $("#skinList").find("input:checked").val();
		    if (newskin == undefined || newskin == '')
		        return false;
			updateLayout({ action: 'chooseSkin', skinName: newskin },
				function(data) {
					window.location = settings.portalUrl;
				}
			);
			return false;
		};
		
        var initializeFragmentBrowserMenu = function() {
            uportal.FragmentBrowser($("#subscribeTab-tab-1"), 
                {
                    fragmentServiceUrl: settings.subscriptionListUrl,
                    listeners: { 
                        onFragmentSelect: function(fragment) {
                            var tab = $("#portalNavigation_" + settings.tabId);
                            var tabPosition = 1;
                            var targetId = null;
                            // figure out what the current tab's number is
                            $("[id*=portalNavigation_]").each(function(i){
                                if ($(this).attr("id") == tab.attr("id"))tabPosition = i+1;
                                targetId = $(this).attr("id").split("_")[1];
                            });
        
                            var subscribeToTabVal = $("#subscribeTabId").val();
                            updateLayout({action: "subscribeToTab" ,sourceID: fragment.ownerID,method: 'appendAfter', tabPosition: tabPosition, elementID: targetId  }, function(xml) {
                                window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
                                        ($("#portalNavigationList > li").length + 1);
                            });
                        }
                    }
                }
            );
            $("#subscribeTabLoading").css("display", "none");
        
            $("#addTabLink")
                .unbind("click")
                .click(function(){
                    $("#subscribeTabDialog").dialog('open');
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
        };
        
		// initialize our portal code
		if (settings.isFocusMode) initfocusedportal(); else initportal();

	};

})(jQuery);
