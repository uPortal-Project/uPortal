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
	
    // if the uPortal scope is not available, add it
    $.uportal = $.uportal || {};
    
    $.uportal.UportalLayoutManager = function(callerSettings) {
		var settings = $.extend({
		    preferencesUrl: 'ajax/preferences',
		    channelListUrl: 'mvc/channelList?xml=true',
		    portalUrl: null,
		    mediaPath: null,
		    currentSkin: null,
		    isFocusMode: false,
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
			$("#layoutDialogLink").click(initializeLayoutMenu);
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
		
		    // add onclick events for portlet delete buttons
			$('a[id*=removePortlet_]').each(function(i){$(this).click(function(){deletePortlet(this.id.split("_")[1]);return false;});});	
		
			// set click handlers for tab moving and editing links
			$("#addTabLink").click(function(){addTab()});
			$("#deletePageLink").click(function(){deleteTab()});
			$("#editPageLink").click(initializeLayoutMenu);
			$("#movePageLeftLink").click(function(){moveTab('left')});
			$("#movePageRightLink").click(function(){moveTab('right')});
			initTabEditLinks();
		
		};
		
		var chooseCategory = function(categoryId) {
			var select = $("#channelSelectMenu").html("");
			var channels = settings.channelBrowser.getChannelsForCategory(categoryId);
			$(channels).each(function(i, val) {
				select.get(0).options[i] = new Option(this.name, this.id);
			});

			select
				.change(function(){ chooseChannel(this.value); })
				.children("option:first").attr("selected", true);
			chooseChannel(select.val());
			
		};

		var chooseChannel = function(chanId) {
			var channel = settings.channelBrowser.getChannel(chanId);
			$("#channelTitle").text(channel.name);
			$("#channelDescription").text(channel.description);
			$("#addChannelId").attr("value", channel.id);
			$("#previewChannelLink").unbind("click").click(function(){ 
				window.location = settings.portalUrl + "?uP_fname=" + channel.fname;
			});
		};

		var searchChannels = function(searchTerm) {
			var results = $("#addChannelSearchResults").html("");
			var channels = settings.channelBrowser.searchChannels(searchTerm);
			$(channels).each(function(i){
				results.append(
					$(document.createElement('li')).append(
						$(document.createElement('a'))
							.attr("id", this.id).attr("href", "javascript:;")
							.click(function(){ chooseChannel(this.id); })
							.text(this.name)
					)
				 );
			});
			
		};

		// Initialization tasks for focus mode
		var initfocusedportal = function() {
			$("#focusedContentDialogLink").click(initializeFocusedContentMenu);
		};
		
		var initializeContentAddingMenu = function() {
            $("#channelAddingTabs").tabs();
			$("#contentAddingDialog").dialog({ width:550, modal:true});
			$("#contentDialogLink")
				.unbind('click', initializeContentAddingMenu)
				.click(function(){$("#contentAddingDialog").dialog('open');});
			settings.channelBrowser = $.channelbrowser({
				onDataLoad: function(categories) {
					var categorySelect = $("#categorySelectMenu");
					$(categories).each(function(i, val) {
						categorySelect.get(0).options[i] = new Option(this.name, this.id);
					});
					categorySelect.change(function(){chooseCategory(this.value)})
						.children("option:first").attr("selected", true);
					$("#addChannelSearchTerm").keyup(function(){
						searchChannels($(this).val());
					});
					chooseCategory(categorySelect.val());
			   		$("#channelLoading").css("display", "none");
			   		$("#categorySelectMenu").css("background-image", "none");
			   		$("#channelSelectMenu").css("background-image", "none");
				}
			});
			$("#addChannelLink").click(function(){addPortlet()});
		};
		
		var initializeFocusedContentMenu = function() {
			$("#focusedContentAddingDialog").dialog({ width:500, modal:true});
			$("#focusedContentDialogLink")
				.unbind('click', initializeFocusedContentMenu)
				.click(function(){$("#focusedContentAddingDialog").dialog('open');});
		    $("#focusedContentAddingDialog form").submit(function(){return addFocusedChannel(this);});
		};
		
		var initializeLayoutMenu = function() {
			// using defaultChecked attribute to compensate for IE radio button bug
			$("#changeColumns").find("img")
				.click(function(){
					$("#changeColumns").find("input").removeAttr("checked").attr("defaultChecked");
					$(this).prev().attr("checked", "checked").attr("defaultChecked","defaultChecked");
				})
				.end().find("input[value=" + getCurrentLayoutString() + "]").attr("checked", "checked").attr("defaultChecked","defaultChecked");
			if ($("#changeColumns").find("input:checked").length == 0) {
			   $("#changeColumns").find("tr:eq(1)").find("td:eq(" + (settings.columnCount-1) + ")").find("input").attr("checked", true).attr("defaultChecked","defaultChecked");
			}
			$("#pageLayoutDialog").dialog({ width:400, modal:true });
		
			$("#layoutDialogLink")
				.unbind('click', initializeLayoutMenu)
				.click(function(){$("#pageLayoutDialog").dialog('open');});
			$("#editPageLink")
				.unbind('click', initializeLayoutMenu)
				.click(function(){$("#pageLayoutDialog").dialog('open');});
				
		    $("#pageLayoutDialog form").submit(function(){return updatePage(this);});
		
		};
		var getCurrentLayoutString = function() {
			var str = "";
			$('#portalPageBodyColumns > [id^=column_]').each(function(){
				var flClass = $(this).get(0).className.match("fl-col-flex[0-9]+");
				if (flClass != null) {
					if (str != '')
						str += '-';
					str += flClass[0].match("[0-9]+")[0];
				}
			});
			if (str == '') str = '100';
			return str;
		};
		var updatePage = function(form) {
			var name = form.pageName.value;
			var layout = $(form.layoutChoice).filter(":checked").val();
			var columns = layout.split("-");
			if (name != $("#portalPageBodyTitle").text())
				updatePageName(name);
			if (layout != getCurrentLayoutString())
				changeColumns(columns);
			$("#pageLayoutDialog").dialog('close');
			return false;
		};
		var updatePageName = function(name) {
			$("#tabLink_" + settings.tabId + " > span").text(name);
			$("#portalPageBodyTitle").text(name);
			$.post(settings.preferencesUrl, {action: 'renameTab', tabId: settings.tabId, tabName: name}, 
			    function(xml){
			        if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
			    }
			);
			return false;
		};
		// Column editing persistence functions
		var changeColumns = function(newcolumns) {
		    settings.columnCount = newcolumns.length;
			$.post(settings.preferencesUrl, {action: 'changeColumns', tabId: settings.tabId, columns: newcolumns}, 
				function(xml) { 
			        if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
				    var columns = $('#portalPageBodyColumns > [id^=column_]');
				    if (columns.length < newcolumns.length) {
				    	$("newColumns > id", xml).each(function(){
				    		$("#portalPageBodyColumns")
				    			.append(
				    				$(document.createElement('div')).attr("id", 'column_' + $(this).text())
				    					.addClass("portal-page-column")
				    					.append(
				    						$(document.createElement('div'))
				    							.attr("id", 'inner-column_' + $(this).text())
				    							.addClass("portal-page-column-inner")
										)
				    			);
				    	});
				    	
				    } else if(columns.length > newcolumns.length) {
				    	for (var i = newcolumns.length; i < columns.length; i++) {
				    		var lastColumn = $("#inner-column_" + $(columns[newcolumns.length-1]).attr("id").split("_")[1]);
				    		var portlets = $(columns[i]).find("div[id*=portlet_]")
					    		.each(function(){
					    			$(this).appendTo(lastColumn);
					    		})
				    			.end().remove();
				    	}
		
				    }
				    
				    $("#portalPageBodyTitleRow").attr("colspan", newcolumns.length);
				    $('#portalPageBodyColumns > [id^=column_]').each(function(i){
				    	$(this).removeClass().addClass("portal-page-column fl-col-flex"+newcolumns[i]);
				    	if (newcolumns.length == 1) $(this).addClass("single");
				    	else if (i == 0) $(this).addClass("left");
				    	else if (i == newcolumns.length - 1) $(this).addClass("right");
				    });
				    
			    	settings.myReorderer.refresh();
			    	
			    	// remove the checked and default checked attributes from all radio
			    	// buttons and re-apply them to the current value to compensate
			    	// for IE radio button bug
			    	$("#changeColumns").find("input").removeAttr("checked").removeAttr("defaultChecked");
			    	$("#changeColumns").find("input[value=" + getCurrentLayoutString() + "]").attr("checked", "checked").attr("defaultChecked","defaultChecked");
					
				});
		};
		
		// Portlet editing persistence functions
		var addPortlet = function(chanId) {
		    var options = { action: 'addChannel', channelID: $("#addChannelId").attr("value") };
		    var firstChannel = $("div[id*=portlet_]:not(.locked)");
		    if (firstChannel.size() == 0) {
		        options['elementID'] = settings.tabId;
		    } else {
		        options['elementID'] = firstChannel.attr("id").split("_")[1];
		        options['position'] = 'insertBefore';
		    }
			$.post(settings.preferencesUrl, options,
			   function(xml) {
			      if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
			      window.location = settings.portalUrl; 
			   }, "text"
			);
		};
		var deletePortlet = function(id) {
			if (!confirm(settings.messages.confirmRemovePortlet)) return false;
			$('#portlet_'+id).remove();
			$.post(settings.preferencesUrl, {action: 'removeElement', elementID: id}, 
			    function(xml){
			        if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
			    }
			);
		};
		
		
		// Tab editing persistence functions
		var addTab = function() {
			$.post(settings.preferencesUrl, {action: 'addTab'}, function(xml) {
                if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
				window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
					($("#portalNavigationList > li").length + 1);
			});
		};
		var deleteTab = function() {
			if (!confirm(settings.messages.confirmRemoveTab)) return false;
			$.post(settings.preferencesUrl, {action: 'removeElement', elementID: settings.tabId}, 
			    function(xml) {
                if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
				window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1"; 
			});
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
			
			$.post(settings.preferencesUrl,
				{
					action: 'moveTabHere',
					sourceID: settings.tabId,
					method: method,
					elementID: targetId,
					tabPosition: tabPosition
				},
				function(xml){
	                if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
				}
			);
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
		   $.post(settings.preferencesUrl, {action: 'movePortletHere', method: method, elementID: $(target).attr('id').split('_')[1], sourceID: $(movedNode).attr('id').split('_')[1]}, 
		       function(xml) { 
		           if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
		   });
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
		    
		    $.post(settings.preferencesUrl, 
		    	{
		    		action: 'addChannel',
		    		channelID: channelId,
		    		position: 'insertBefore',
		    		elementID: elementId
		    	},
		    	function(xml) {
		    	    if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
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
			$.post(settings.preferencesUrl,
				{ action: 'chooseSkin', skinName: newskin },
				function(xml) {
				    if ($("success", xml).text() == 'false') { handleServerError(xml); return false; }
					window.location = settings.portalUrl;
				}
			);
			return false;
		};
		
		var handleServerError = function(xml) {
		    alert($("message", xml).text());
		};

		// initialize our portal code
		if (settings.isFocusMode) initfocusedportal(); else initportal();

	};

})(jQuery);
