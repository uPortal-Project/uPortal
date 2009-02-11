/*
	CONFIGURATION PARAMETERS
	

*/
(function($){
	
    // if the uPortal scope is not availalable, add it
    $.uportal = $.uportal || {};
    
    $.uportal.UportalLayoutManager = function(callerSettings) {
		var settings = $.extend({
		    preferencesUrl: 'ajax/preferences',
		    channelListUrl: 'ajax/channelList',
		    portalUrl: null,
		    mediaPath: null,
		    currentSkin: null,
		    isFocusMode: false
	    }, callerSettings||{});


		/*
			INTERNAL METHODS
		*/
	
		// Initialization tasks for non-focused mode
		var initportal = function() {
		
		    settings.columnCount = $("#portalPageBodyColumns td[id*=column_]").size(); 
		    settings.tabId = $("#portalNavigationList li.active").attr("id").split("_")[1];

			// initialize dialog menus
			$("#contentDialogLink").click(initializeContentAddingMenu);
			$("#layoutDialogLink").click(initializeLayoutMenu);
			$("#skinDialogLink").click(initializeSkinMenu);
		
			// initialize portlet drag and drop
		    $('div[@id*=inner-column_]').each(function(i){
				$(this).Sortable({
					accept : 'movable',
					helperclass : 'dropborder',
					opacity : 0.5,
					handle : 'div.portlet-toolbar',
					onStop : movePortlet
				});
		    });
		    
		    // add onclick events for portlet delete buttons
			$('a[@id*=removePortlet_]').each(function(i){$(this).click(function(){deletePortlet(this.id.split("_")[1]);return false;});});	
		
			// set click handlers for tab moving and editing links
			$("#addTabLink").click(function(){addTab()});
			$("#deletePageLink").click(function(){deleteTab()});
			$("#editPageLink").click(initializeLayoutMenu);
			$("#movePageLeftLink").click(function(){ moveTab('left')});
			$("#movePageRightLink").click(function(){moveTab('right')});
			initTabEditLinks();
		
		};
		
		// Initialization tasks for focus mode
		var initfocusedportal = function() {
			$("#focusedContentDialogLink").click(initializeFocusedContentMenu);
		};
		
		var initializeContentAddingMenu = function() {
			$("#contentAddingDialog").dialog({height:450, width:500});
		    $("#contentAddingDialog").parent().parent().css("z-index", 12);
			$("#contentDialogLink")
				.unbind('click', initializeContentAddingMenu)
				.click(function(){$("#contentAddingDialog").dialog('open');});
			$("#channelAddingTabs > ul").tabs();
			$("#channelAddingTabs").channelbrowser({
				onDataLoad: function() {
			   		$("#channelLoading").css("display", "none");
			   		$("#categorySelectMenu").css("background-image", "none");
			   		$("#channelSelectMenu").css("background-image", "none");
				},
				categorySelect: "#categorySelectMenu", 
				channelSelect: "#channelSelectMenu",
				channelSearchInput: "#addChannelSearchTerm",
				channelSearchResults: "#addChannelSearchResults",
				onChannelSelect: function(channel) {
					$("#channelTitle").text(channel.name);
					$("#channelDescription").text(channel.description);
					$("#addChannelId").attr("value", channel.id);
					$("#previewChannelLink").unbind("click").click(function(){ 
						window.location = settings.portalUrl + "?uP_fname=" + channel.fname;
					});
				}
			});
			$("#addChannelLink").click(function(){addPortlet()});
		};
		
		var initializeFocusedContentMenu = function() {
			$("#focusedContentAddingDialog").dialog({width:500});
		    $("#focusedContentAddingDialog").parent().parent().css("z-index", 12);
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
			
			$("#pageLayoutDialog").dialog({height:300, width:400});
		
			$("#layoutDialogLink")
				.unbind('click', initializeLayoutMenu)
				.click(function(){$("#pageLayoutDialog").dialog('open');});
			$("#editPageLink")
				.unbind('click', initializeLayoutMenu)
				.click(function(){$("#pageLayoutDialog").dialog('open');});
		
		    $("#pageLayoutDialog").parent().parent()
		       .css("z-index", 12)
		       .css("height", $("#pageLayoutDialog").parent().height() + 20);
	        $("#pageLayoutDialog form").submit(function(){return updatePage(this);});
		
		};
		var getCurrentLayoutString = function() {
			var str = "";
			$('#portalPageBodyColumns > td[@id*=column_]').each(function(){
				if (str != '')
					str += '-';
				str += parseInt($(this).attr("width"));
			});
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
			$.post(settings.preferencesUrl, {action: 'renameTab', tabId: settings.tabId, tabName: name}, function(xml){});
			return false;
		};
		// Column editing persistence functions
		var changeColumns = function(newcolumns) {
		    settings.columnCount = newcolumns.length;
			$.post(settings.preferencesUrl, {action: 'changeColumns', tabId: settings.tabId, columns: newcolumns}, 
				function(xml) { 
				    var columns = $('#portalPageBodyColumns > td[@id*=column_]');
				    if (columns.length < newcolumns.length) {
				    	$("newColumns > id", xml).each(function(){
				    		$("#portalPageBodyColumns")
				    			.append(
				    				$(document.createElement('td')).attr("id", 'column_' + $(this).text())
				    					.addClass("portal-page-column")
				    					.append(
				    						$(document.createElement('div'))
				    							.attr("id", 'inner-column_' + $(this).text())
				    							.addClass("portal-page-column-inner")
				    							.Sortable({
													accept : 'movable',
													helperclass : 'dropborder',
													opacity : 0.5,
													handle : 'div.portlet-toolbar',
													onStop : movePortlet
												})
										)
				    			);
				    	});
				    	
				    } else if(columns.length > newcolumns.length) {
				    	for (var i = newcolumns.length; i < columns.length; i++) {
				    		var lastColumn = $("#inner-column_" + $(columns[newcolumns.length-1]).attr("id").split("_")[1]);
				    		var portlets = $(columns[i]).find("div[@id*=portlet_]")
					    		.each(function(){
					    			$(this).appendTo(lastColumn);
					    			lastColumn.SortableAddItem($(this));
					    		})
				    			.end().remove();
				    	}
				    	
				    }
		
				    $("#portalPageBodyTitleRow").attr("colspan", newcolumns.length);
				    $('#portalPageBodyColumns > td[@id*=column_]').each(function(i){
				    	$(this).attr("width", newcolumns[i] + "%")
				    	.removeClass("right").removeClass("left").removeClass("single");
				    	if (newcolumns.length == 1) $(this).addClass("single");
				    	else if (i == 0) $(this).addClass("left");
				    	else if (i == newcolumns.length - 1) $(this).addClass("right");
				    });

                    // remove the checked and default checked attributes from all radio
                    // buttons and re-apply them to the current value to compensate
                    // for IE radio button bug
			    	$("#changeColumns").find("input").removeAttr("checked").removeAttr("defaultChecked");
                    $("#changeColumns").find("input[value=" + getCurrentLayoutString() + "]").attr("checked", "checked").attr("defaultChecked","defaultChecked");
					
				}
			);
		};
		
		// Portlet editing persistence functions
		var addPortlet = function(chanId) {
		    var options = { action: 'addChannel', channelID: $("#addChannelId").attr("value") };
            var firstChannel = $("div[id*=portlet_].movable");
            if (firstChannel.size() == 0) {
		        options['elementID'] = settings.tabId;
		    } else {
		        options['elementID'] = firstChannel.attr("id").split("_")[1];
		        options['position'] = 'insertBefore';
		    }
			$.post(settings.preferencesUrl, options,
			   function(xml) { window.location = settings.portalUrl; }, "text"
			);
		};
		var deletePortlet = function(id) {
			if (!confirm("Are you sure you want to remove this portlet?")) return false;
			$('#portlet_'+id).remove();
			$.post(settings.preferencesUrl, {action: 'removeElement', elementID: id}, function(xml) { });
		};
		
		
		// Tab editing persistence functions
		var addTab = function() {
			$.post(settings.preferencesUrl, {action: 'addTab'}, function(xml) {
				window.location = settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
					($("#portalNavigationList > li").length + 1);
			});
		};
		var deleteTab = function() {
			if (!confirm("Are you sure you want to remove this tab and all its content?")) return false;
			$.post(settings.preferencesUrl, {action: 'removeElement', elementID: settings.tabId}, function(xml) { 
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
			$("[@id*=portalNavigation_]").each(function(i){
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
				function(xml){}
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
			$("[@id*=tabLink_]").each(function(i){
				$(this).attr("href", settings.portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (i+1));
			});
		//	fly.closeSubnav(tabId);
		};
		var movePortlet = function(movedNode, targetNode) {
			var method = 'insertBefore';
			var target = null;
			if ($(this).next('div[@id*=portlet_]').attr('id') != undefined) {
				target = $(this).next('div[@id*=portlet_]');
			} else if ($(this).prev('div[@id*=portlet_]').attr('id') != undefined) {
				target = $(this).prev('div[@id*=portlet_]');
				method = 'appendAfter';
			} else {
				target = $(this).parent();
			}
		    var columns = $('#portalPageBodyColumns > td[@id*=column_]');
		    for (var i = 0; i < columns.length; i++) {
		        $(columns[i]).attr("width", $(columns[i]).attr("width"));
		    }
			$.post(settings.preferencesUrl, {action: 'movePortletHere', method: method, elementID: $(target).attr('id').split('_')[1], sourceID: $(this).attr('id').split('_')[1]}, function(xml) { });
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
		    		channelID: 'chan' + channelId,
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
			$("#skinChoosingDialog").dialog({height:450, width:500});
		    $("#skinChoosingDialog").parent().parent().css("z-index", 12);
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
							
						var span = $(document.createElement("div")).append(
						    $(document.createElement("span"))
								.append(input)
								.append(document.createTextNode($(this).children("skin-name").text()))
								.addClass("portlet-form-field-label")
							);
						skinMenu.append(span);
						var div = $(document.createElement("div"))
							.addClass("portlet-font-dim").css("padding-left", "20px")
							.css("padding-bottom", "10px");
						div.append($(document.createElement("span")).text($(this).children("skin-description").text()));
						div.append($(document.createElement("br")));
						div.append($(document.createElement("img")).attr("src", settings.mediaPath + "/" + key + "/" + key + "_thumb.gif"));
						skinMenu.append(div);
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
					window.location = settings.portalUrl;
				}
			);
			return false;
		};
		
        // initialize our portal code
        if (settings.isFocusMode) initfocusedportal(); else initportal();

	};

})(jQuery);
