var channelXml,skinXml;

// Initialization tasks for non-focused mode
function initportal() {

	// initialize dialog menus
	$("#contentDialogLink").click(initializeContentMenu);
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
}

// Initialization tasks for focus mode
function initfocusedportal() {
	$("#focusedContentDialogLink").click(initializeFocusedContentMenu);
}
function initializeFocusedContentMenu() {
	$("#focusedContentAddingDialog").dialog({height:450, width:500});
	$("#focusedContentDialogLink")
		.unbind('click', initializeFocusedContentMenu)
		.click(function(){$("#focusedContentAddingDialog").dialog('open');});
}

function initializeLayoutMenu() {
	$("#changeColumns").find("img")
		.click(function(){$(this).prev().attr("checked", true)})
		.end().find("input[value=" + getCurrentLayoutString() + "]").attr("checked", true);
	$("#pageLayoutDialog").dialog({height:200, width:400});

	$("#layoutDialogLink")
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
function deleteTab(id) {
	if (!confirm("Are you sure you want to remove this tab and all its content?")) return false;
	$.post(preferencesUrl, {action: 'removeElement', elementID: id}, function(xml) { window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1"; });
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

function initializeContentMenu() {

	$("#channelAddingTabs > ul").tabs();
	$("#contentAddingDialog").dialog({height:450, width:500});
	$("#addChannelSearchTerm").keyup(function(){searchChannels($(this).attr("value"))});
	var categorySelect = document.getElementById("categorySelectMenu");

	$("#contentDialogLink")
		.unbind('click', initializeContentMenu)
		.click(function(){$("#contentAddingDialog").dialog('open');});

	$.get(channelListUrl, {}, function(xml) {
		channelXml = xml;
		var matching = new Array();
		$("category:has(channel)", channelXml).each(function(){matching.push($(this))});
        matching.sort(sortCategoryResults);
        $(matching).each(function(i, val) {
        	categorySelect.options[i] = new Option($(this).attr("name"), $(this).attr("ID"));
        });
		categorySelect.options[0].selected = true;
       	browseChannelCategory();
       	
       	// remove the loading graphics and message
   		$("#channelLoading").css("display", "none");
   		$("#categorySelectMenu").css("background-image", "none");
   		$("#channelSelectMenu").css("background-image", "none");
	});
	
}
function browseChannelCategory() {

	var channelSelect = document.getElementById("channelSelectMenu");
	$("#channelSelectMenu").html("");
	
    var matching = new Array();
	$("category[ID=" + $("#categorySelectMenu").attr("value") + "]", channelXml)
		.find("channel")
		.each(function(){matching.push($(this))});
    matching.sort(sortChannelResults);
    
    $(matching).each(function(i, val){
    	if (i == 0 || $(this).attr("ID") != $(this).prev().attr("ID")) {
    		channelSelect.options[i] = new Option($(this).attr("name"), $(this).attr("ID"));
    	}
    });
    channelSelect.options[0].selected = true;
	selectChannel(channelSelect.value);
	
}

function searchChannels(searchTerm) {
	if (searchTerm == null || searchTerm == '') return;
    var matching = new Array();
	$("channel[name*=" + searchTerm + "]", channelXml).each(function(){matching.push($(this))});
	$("channel[description*=" + searchTerm + "]", channelXml).each(function(){matching.push($(this))});
	
    var searchResults = document.getElementById("addChannelSearchResults");
    searchResults.innerHTML = "";

    matching.sort(sortChannelResults);
    $(matching).each(function(i){
		if (i == 0 || $(this).attr("ID") != $(matching[i-1]).attr("ID")) {
		     $("#addChannelSearchResults").append(
		     	$(document.createElement('li')).append(
		     		$(document.createElement('a'))
		     			.attr("id", $(this).attr("ID")).attr("href", "javascript:;")
		     			.click(function(){selectChannel(this.id);})
		     			.text($(this).attr("name"))
		     	)
		     );
	     }
    });
    
}

// sort a list of returned channels by name
function sortCategoryResults(a, b) {
    var aname = a.attr("name").toLowerCase();
    var bname = b.attr("name").toLowerCase();
    if (aname == 'new') return -1;
    if (bname == 'new') return 1;
    if (aname == 'popular') return -1;
    if (bname == 'popular') return 1;
    if(aname > bname) return 1;
    if(aname < bname) return -1;
    return 0;
}

// sort a list of returned channels by name
function sortChannelResults(a, b) {
    var aname = a.attr("name").toLowerCase();
    var bname = b.attr("name").toLowerCase();
    if(aname > bname) return 1;
    if(aname < bname) return -1;
    return 0;
}

function selectChannel(channelId) {
	if (channelId.indexOf("_") > -1)
		channelId = channelId.split("_")[1];
	var channel = $("channel[ID=" + channelId + "]", channelXml);

	$("#channelTitle").text(channel.attr("name"));
	$("#channelDescription").text(channel.attr("description"));
	$("#addChannelId").attr("value", channelId);
	$("#previewChannelLink").click(function(){ window.location = portalUrl + "?uP_fname=" + channel.attr("fname"); });

    // if this channel has user-overrideable parameters, present a form allowing the
    // user to input values
    var parameters = channel.children("parameter[override=yes]");
    for (var i = 0; i < parameters.length; i++) {
        var input = $(document.createElement("input")).attr("type", "hidden").attr("name", $(parameters[i]).attr("name")).attr("value", $(parameters[i]).attr("value"));
        var p = $(document.createElement("p")).append(input);
        $("#channelDescription").append(p);
    }

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
    console.debug(tabPosition, elementId, channelId);
    
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
	$("#skinDialogLink")
		.unbind('click', initializeSkinMenu)
		.click(function(){$("#skinChoosingDialog").dialog('open');});

    var skinMenu = $("#skinList").html("");
    $.get(mediaPath + '/skinList.xml', { },
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
					.text($(this).children("skin-description").text())
					.addClass("portlet-font-dim").css("padding-left", "20px")
					.css("padding-bottom", "10px");
				skinMenu.append(div);
			});
        	
        	// remove the loading graphics and message
        	$("#skinLoading").css("display", "none");
    	}
    );
}

function chooseSkin(form) {
	$.post(preferencesUrl,
		{ action: 'chooseSkin', skinName:$("#skinList > input:selected").val() },
		function(xml) {
			window.location = portalUrl;
		}
	);
	return false;
}


