var channelXml,skinXml;

// Initialization tasks for non-focused mode
function initportal() {

	// initialize dialog menus
	$("#contentDialogLink").click(function(){initializeContentMenu();$(this).click(function(){$("#contentAddingDialog").dialog('open')})});
	$("#layoutDialogLink").click(function(){initializeLayouMenu();$(this).click(function(){$("#pageLayoutDialog").dialog('open')})});
	$("#skinDialogLink").click(function(){initializeSkinMenu();$(this).click(function(){$("#skinChoosingDialog").dialog('open')})});

	// initialize portlet drag and drop
    $('div[@id*=inner-column_]').each(function(i){
		$(this).Sortable({
			accept : 'movable',
			helperclass : 'dropborder',
			opacity : 0.5,
			handle : 'div.portlet-toolbar',
			onStop : function(){
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
		});
    });
    
    // add onclick events for portlet delete buttons
	$('a[@id*=removePortlet_]').each(function(i){$(this).click(function(){deletePortlet(this.id.split("_")[1]);return false;});});	

	$("#addTabLink").click(function(){addTab()});
}

// Initialization tasks for focus mode
function initfocusedportal() {
	$("#focusedContentDialogLink").click(function(){initializeFocusedContentMenu();$(this).click(function(){$("#focusedContentAddingDialog").dialog('open')})});
}
function initializeFocusedContentMenu() {
	$("#focusedContentAddingDialog").dialog({height:450, width:500});
}

function initializeLayoutMenu() {
	$("#changeColumns > input").each(function(i, val){$(this).click(function(){changeColumns(i+1)});});
	$("#changeColumns > input").get(columnCount - 1).attr("selected", true);
}

function initializeSkinMenu() {
	$("#skinChoosingDialog").dialog({height:450, width:500});
}

function initializeContentMenu() {

	$("#channelAddingTabs > ul").tabs();
	$("#contentAddingDialog").dialog({height:450, width:500});
	$("#addChannelSearchTerm").keyup(function(){searchChannels($(this).attr("value"))});
	var categorySelect = document.getElementById("categorySelectMenu");

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
	$("category[ID=" + $("#categorySelectMenu").attr("value") + "] > channel", channelXml)
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

// Column editing persistence functions
function changeColumns(num) {
	$.post(preferencesUrl, {action: 'changeColumns', tabId: tabId, columnNumber: num}, function(xml) { window.location = portalUrl; });
}

// Tab editing persistence functions
function addTab() {
	$.post(preferencesUrl, {action: 'addTab'}, function(xml) { 
		var tab = $(document.createElement("li")).attr("id", $("newNodeId", xml).text());
		var a = $(document.createElement("a"))
			.attr("href", portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" 
				+ ($("portalNavigationList > li").length + 1))
			.append($(document.createElement("span")).text("New Page"));
		tab.append(a);
		$("portalNavigationList").append(tab);
	});
}
function movePortlet(movedNode, targetNode) {
	var direction, targetElement;
	// moved to a different tab
	if (targetNode.is('[@id*=portalNavigation]')) {
		movedNode.remove();
		direction = "insertBefore";
		targetElement = targetNode;
	// moved within the page
	} else return;
	
	$.post(preferencesUrl, {action: 'movePortletHere', sourceID:movedNode.attr("id").split("_")[1], direction:direction, elementID:targetElement.attr("id").split("_")[1]});
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
