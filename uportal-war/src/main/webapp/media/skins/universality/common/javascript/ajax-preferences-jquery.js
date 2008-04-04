var channelXml,skinXml;

// Initialization tasks for non-focused mode
function initportal() {

	// initialize dialog menus
	$("#contentAddingDialog").channelbrowser({handles: new Array("#contentDialogLink")});
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

	$("#addTabLink").click(function(){addTab()});
	$("#deletePageLink").click(deleteTab);
	$("#editPageLink").click(initializeLayoutMenu);
	$("#movePageLeftLink").click(function(){ moveTab('left')});
	$("#movePageRightLink").click(function(){moveTab('right')});
	initTabEditLinks();

}

// Initialization tasks for focus mode
function initfocusedportal() {
	$("#focusedContentDialogLink").click(initializeFocusedContentMenu);
}
function initializeFocusedContentMenu() {
	$("#focusedContentAddingDialog").dialog({width:500});
    $("#focusedContentAddingDialog").parent().parent().css("z-index", 12);
	$("#focusedContentDialogLink")
		.unbind('click', initializeFocusedContentMenu)
		.click(function(){$("#focusedContentAddingDialog").dialog('open');});
}

function initializeLayoutMenu() {
	$("#changeColumns").find("img")
		.click(function(){$(this).prev().attr("checked", true)})
		.end().find("input[value=" + getCurrentLayoutString() + "]").attr("checked", true);
	$("#pageLayoutDialog").dialog({height:300, width:400});
	$("#pageLayoutDialog").parent().parent().css("z-index", 12);

	$("#layoutDialogLink")
		.unbind('click', initializeLayoutMenu)
		.click(function(){$("#pageLayoutDialog").dialog('open');});
	$("#editPageLink")
		.unbind('click', initializeLayoutMenu)
		.click(function(){$("#pageLayoutDialog").dialog('open');});

}
function getCurrentLayoutString() {
	var str = "";
	$('#portalPageBodyColumns > td[@id*=column_]').each(function(){
		if (str != '')
			str += '-';
		str += parseInt($(this).attr("width"));
	});
	return str;
}
function updatePage(form) {
	var name = form.pageName.value;
	var layout = $(form.layoutChoice).filter(":checked").val();
	var columns = layout.split("-");
	if (name != $("#portalPageBodyTitle").text())
		updatePageName(name);
	if (layout != getCurrentLayoutString())
		changeColumns(columns);
	$("#pageLayoutDialog").dialog('close');
	return false;
}
function updatePageName(name) {
	$("#tabLink_" + tabId + " > span").text(name);
	$("#portalPageBodyTitle").text(name);
	$.post(preferencesUrl, {action: 'renameTab', tabId: tabId, tabName: name}, function(xml){});
	return false;
}
// Column editing persistence functions
function changeColumns(newcolumns) {
	$.post(preferencesUrl, {action: 'changeColumns', tabId: tabId, columns: newcolumns}, 
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
			
		}
	);
}

// Portlet editing persistence functions
function addPortlet(chandId) {
	$.post(preferencesUrl, {action: 'addChannel', elementID: tabId, channelID: $("#addChannelId").attr("value")}, function(xml) { window.location = portalUrl; });
}
function deletePortlet(id) {
	if (!confirm("Are you sure you want to remove this portlet?")) return false;
	$('#portlet_'+id).remove();
	$.post(preferencesUrl, {action: 'removeElement', elementID: id}, function(xml) { });
}


// Tab editing persistence functions
function addTab() {
	$.post(preferencesUrl, {action: 'addTab'}, function(xml) {
		window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
			($("#portalNavigationList > li").length + 1);
	});
}
function deleteTab() {
	if (!confirm("Are you sure you want to remove this tab and all its content?")) return false;
	$.post(preferencesUrl, {action: 'removeElement', elementID: tabId}, function(xml) { 
		window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1"; 
	});
}
function moveTab(direction) {
	var tab = $("#portalNavigation_" + tabId);
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
	
	$.post(preferencesUrl,
		{
			action: 'moveTabHere',
			sourceID: tabId,
			method: method,
			elementID: targetId,
			tabPosition: tabPosition
		},
		function(xml){}
	);
	redoTabs(tabId);
	initTabEditLinks();
}
function initTabEditLinks() {
	var tab = $("#portalNavigation_" + tabId);
	if (tab.not(":first-child") && tab.prev().hasClass("movable-tab"))
		$("#movePageLeftLink").css("display", "block");
	else 
		$("#movePageLeftLink").css("display", "none");
		
	if (tab.is(":last-child")) 
		$("#movePageRightLink").css("display", "none");
	else
		$("#movePageRightLink").css("display", "block");
		
	var links = $("#editTabInner").find(".portal-subnav").not("[display=none]")
	links.each(function(i){
		if (links.length == 1) $(this).removeClass("first").removeClass("last").addClass("single");
		else if (i == 0) $(this).removeClass("single").removeClass("last").addClass("first");
		else if (i == links.length-1) $(this).removeClass("single").removeClass("first").addClass("last");
		else $(this).removeClass("single").removeClass("last").removeClass("first");
	});
}
function redoTabs(tabId) {
	$("[@id*=tabLink_]").each(function(i){
		$(this).attr("href", portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (i+1));
	});
//	fly.closeSubnav(tabId);
}
function movePortlet(movedNode, targetNode) {
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
	$.post(preferencesUrl, {action: 'movePortletHere', method: method, elementID: $(target).attr('id').split('_')[1], sourceID: $(this).attr('id').split('_')[1]}, function(xml) { });
}

function addFocusedChannel(form) {

    var channelId = form.channelId.value;
    var tabPosition, elementId;
    
    $("#focusedContentAddingDialog input[name=targetTab]").each(function(i){
    	if ($(this).is(":checked")) {
    		tabPosition = i+1;
    		elementId = $(this).val();
    	}
    });
    
    $.post(preferencesUrl, 
    	{
    		action: 'addChannel',
    		channelID: 'chan' + channelId,
    		position: 'insertBefore',
    		elementID: elementId
    	},
    	function(xml) {
			window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + tabPosition;
    	}
    );
	return false;

}

function initializeSkinMenu() {
	$("#skinChoosingDialog").dialog({height:450, width:500});
    $("#skinChoosingDialog").parent().parent().css("z-index", 12);
	$("#skinDialogLink")
		.unbind('click', initializeSkinMenu)
		.click(function(){$("#skinChoosingDialog").dialog('open');});

    var skinMenu = $("#skinList").html("");
    $.get(mediaPath + '/skinList.xml?noCache=' + new Date().getTime(), { },
    	function(xml){
			skinXml = xml;
			$("skin", skinXml).each(function(i){
				var key = $(this).children("skin-key").text();
				
				var input = $(document.createElement("input")).attr("type", "radio")
					.val(key)
					.attr("name", "skinChoice");
				if (key == currentSkin)
					input.attr("checked", true);
					
				var span = $(document.createElement("span"))
					.append(input)
					.append(document.createTextNode($(this).children("skin-name").text()))
					.addClass("portlet-form-field-label");
				skinMenu.append(span);
				var div = $(document.createElement("div"))
					.addClass("portlet-font-dim").css("padding-left", "20px")
					.css("padding-bottom", "10px");
				div.append($(document.createElement("span")).text($(this).children("skin-description").text()));
				div.append($(document.createElement("br")));
				div.append($(document.createElement("img")).attr("src", mediaPath + "/" + key + "/" + key + "_thumb.gif"));
				skinMenu.append(div);
			});

        	// remove the loading graphics and message
        	$("#skinLoading").css("display", "none");
            $("#skinChoosingDialog").parent().parent().css("height", $("#skinChoosingDialog").parent().get(0).clientHeight + 20);
    	}
    );
}

function chooseSkin(form) {
	$.post(preferencesUrl,
		{ action: 'chooseSkin', skinName:$("#skinList").find("input:checked").val() },
		function(xml) {
			window.location = portalUrl;
		}
	);
	return false;
}


