var channelXml,skinXml;
var myReorderer;
               
// Initialization tasks for non-focused mode
function initportal() {

	// initialize dialog menus
	jQuery("#contentAddingDialog").channelbrowser({handles: new Array("#contentDialogLink")});
	jQuery("#layoutDialogLink").click(initializeLayoutMenu);
	jQuery("#skinDialogLink").click(initializeSkinMenu);

    var options = {
         selectors: {
	        columns: ".portal-page-column-inner",
	        modules: ".portlet-container",
	        lockedModules: ".locked",
	        dropWarning: "#portalDropWarning",
	        grabHandle: "[id*=toolbar_]"
         },
         listeners: {
         	afterMove: movePortlet
         },
         styles: {
         	mouseDrag: "orderable-dragging-mouse"
         }
     };
     myReorderer = fluid.reorderLayout ("#portalPageBodyColumns",options);

    // add onclick events for portlet delete buttons
	jQuery('a[@id*=removePortlet_]').each(function(i){jQuery(this).click(function(){deletePortlet(this.id.split("_")[1]);return false;});});	

	jQuery("#addTabLink").click(function(){addTab()});
	jQuery("#deletePageLink").click(deleteTab);
	jQuery("#editPageLink").click(initializeLayoutMenu);
	jQuery("#movePageLeftLink").click(function(){ moveTab('left')});
	jQuery("#movePageRightLink").click(function(){moveTab('right')});
	initTabEditLinks();

}

// Initialization tasks for focus mode
function initfocusedportal() {
	jQuery("#focusedContentDialogLink").click(initializeFocusedContentMenu);
}
function initializeFocusedContentMenu() {
	jQuery("#focusedContentAddingDialog").dialog({width:500, modal:true});
	jQuery("#focusedContentDialogLink")
		.unbind('click', initializeFocusedContentMenu)
		.click(function(){jQuery("#focusedContentAddingDialog").dialog('open');});
}

function initializeLayoutMenu() {
	jQuery("#changeColumns").find("img")
		.click(function(){jQuery(this).prev().attr("checked", true)})
		.end().find("input[value=" + getCurrentLayoutString() + "]").attr("checked", true);
	if (jQuery("#changeColumns").find("input:checked").length == 0) {
	   jQuery("#changeColumns").find("tr:eq(1)").find("td:eq(" + (columnCount-1) + ")").find("input").attr("checked", true);
	}
	jQuery("#pageLayoutDialog").dialog({height:300, width:400, modal:true });

	jQuery("#layoutDialogLink")
		.unbind('click', initializeLayoutMenu)
		.click(function(){jQuery("#pageLayoutDialog").dialog('open');});
	jQuery("#editPageLink")
		.unbind('click', initializeLayoutMenu)
		.click(function(){jQuery("#pageLayoutDialog").dialog('open');});

    jQuery("#pageLayoutDialog").parent().parent()
       .css("height", jQuery("#pageLayoutDialog").parent().height() + 20);

}
function getCurrentLayoutString() {
	var str = "";
	jQuery('#portalPageBodyColumns > td[@id*=column_]').each(function(){
		if (str != '')
			str += '-';
		str += parseInt(jQuery(this).attr("width"));
	});
	return str;
}
function updatePage(form) {
	var name = form.pageName.value;
	var layout = jQuery(form.layoutChoice).filter(":checked").val();
	var columns = layout.split("-");
	if (name != jQuery("#portalPageBodyTitle").text())
		updatePageName(name);
	if (layout != getCurrentLayoutString())
		changeColumns(columns);
	jQuery("#pageLayoutDialog").dialog('close');
	return false;
}
function updatePageName(name) {
	jQuery("#tabLink_" + tabId + " > span").text(name);
	jQuery("#portalPageBodyTitle").text(name);
	jQuery.post(preferencesUrl, {action: 'renameTab', tabId: tabId, tabName: name}, function(xml){});
	return false;
}
// Column editing persistence functions
function changeColumns(newcolumns) {
    columnCount = newcolumns.length;
	jQuery.post(preferencesUrl, {action: 'changeColumns', tabId: tabId, columns: newcolumns}, 
		function(xml) { 
		    var columns = jQuery('#portalPageBodyColumns > td[@id*=column_]');
		    if (columns.length < newcolumns.length) {
		    	jQuery("newColumns > id", xml).each(function(){
		    		jQuery("#portalPageBodyColumns")
		    			.append(
		    				jQuery(document.createElement('td')).attr("id", 'column_' + jQuery(this).text())
		    					.addClass("portal-page-column")
		    					.append(
		    						jQuery(document.createElement('div'))
		    							.attr("id", 'inner-column_' + jQuery(this).text())
		    							.addClass("portal-page-column-inner")
								)
		    			);
		    	});
		    	
		    } else if(columns.length > newcolumns.length) {
		    	for (var i = newcolumns.length; i < columns.length; i++) {
		    		var lastColumn = jQuery("#inner-column_" + jQuery(columns[newcolumns.length-1]).attr("id").split("_")[1]);
		    		var portlets = jQuery(columns[i]).find("div[@id*=portlet_]")
			    		.each(function(){
			    			jQuery(this).appendTo(lastColumn);
	    					myReorderer.refresh();		    	
			    		})
		    			.end().remove();
		    	}

		    }

		    jQuery("#portalPageBodyTitleRow").attr("colspan", newcolumns.length);
		    jQuery('#portalPageBodyColumns > td[@id*=column_]').each(function(i){
		    	jQuery(this).attr("width", newcolumns[i] + "%")
		    	.removeClass("right").removeClass("left").removeClass("single");
		    	if (newcolumns.length == 1) jQuery(this).addClass("single");
		    	else if (i == 0) jQuery(this).addClass("left");
		    	else if (i == newcolumns.length - 1) jQuery(this).addClass("right");
		    });
		    
	    	myReorderer.refresh();
			
		}
	);
}

// Portlet editing persistence functions
function addPortlet(chanId) {
    var options = { action: 'addChannel', channelID: jQuery("#addChannelId").attr("value") };
    if (firstChannelId == null || firstChannelId == '') {
        options['elementID'] = tabId;
    } else {
        options['elementID'] = firstChannelId;
        options['position'] = 'insertBefore';
    }
	jQuery.post(preferencesUrl, options,
	   function(xml) { window.location = portalUrl; }, "text"
	);
}
function deletePortlet(id) {
	if (!confirm("Are you sure you want to remove this portlet?")) return false;
	if (id == firstChannelId) {
        firstChannelId = tabId;
    }
	jQuery('#portlet_'+id).remove();
	jQuery.post(preferencesUrl, {action: 'removeElement', elementID: id}, function(xml) { });
}


// Tab editing persistence functions
function addTab() {
	jQuery.post(preferencesUrl, {action: 'addTab'}, function(xml) {
		window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + 
			(jQuery("#portalNavigationList > li").length + 1);
	});
}
function deleteTab() {
	if (!confirm("Are you sure you want to remove this tab and all its content?")) return false;
	jQuery.post(preferencesUrl, {action: 'removeElement', elementID: tabId}, function(xml) { 
		window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1"; 
	});
}
function moveTab(direction) {
	var tab = jQuery("#portalNavigation_" + tabId);
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
	jQuery("[@id*=portalNavigation_]").each(function(i){
		if (jQuery(this).attr("id") == tab.attr("id"))
			tabPosition = i+1;
	});
	
	jQuery.post(preferencesUrl,
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
	var tab = jQuery("#portalNavigation_" + tabId);
	if (tab.not(":first-child") && tab.prev().hasClass("movable-tab"))
		jQuery("#movePageLeftLink").css("display", "block");
	else 
		jQuery("#movePageLeftLink").css("display", "none");
		
	if (tab.is(":last-child")) 
		jQuery("#movePageRightLink").css("display", "none");
	else
		jQuery("#movePageRightLink").css("display", "block");
		
	var links = jQuery("#editTabInner").find(".portal-subnav").not("[display=none]")
	links.each(function(i){
		if (links.length == 1) jQuery(this).removeClass("first").removeClass("last").addClass("single");
		else if (i == 0) jQuery(this).removeClass("single").removeClass("last").addClass("first");
		else if (i == links.length-1) jQuery(this).removeClass("single").removeClass("first").addClass("last");
		else jQuery(this).removeClass("single").removeClass("last").removeClass("first");
	});
}
function redoTabs(tabId) {
	jQuery("[@id*=tabLink_]").each(function(i){
		jQuery(this).attr("href", portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (i+1));
	});
//	fly.closeSubnav(tabId);
}
function movePortlet(movedNode) {
   var method = 'insertBefore';
   var target = null;
   if (jQuery(movedNode).nextAll('div[@id*=portlet_]').size() > 0) {
       target = jQuery(movedNode).nextAll('div[@id*=portlet_]').get(0);
   } else if (jQuery(movedNode).prevAll('div[@id*=portlet_]').size() > 0) {
       target = jQuery(movedNode).prevAll('div[@id*=portlet_]').get(0);
       method = 'appendAfter';
   } else {
       target = jQuery(movedNode).parent();
   }
   var columns = jQuery('#portalPageBodyColumns > td[@id*=column_]');
   for (var i = 0; i < columns.length; i++) {
       jQuery(columns[i]).attr("width", jQuery(columns[i]).attr("width"));
   }
   jQuery.post(preferencesUrl, {action: 'movePortletHere', method: method, elementID: jQuery(target).attr('id').split('_')[1], sourceID: jQuery(movedNode).attr('id').split('_')[1]}, function(xml) { });
}

function addFocusedChannel(form) {

    var channelId = form.channelId.value;
    var tabPosition, elementId;
    
    jQuery("#focusedContentAddingDialog input[name=targetTab]").each(function(i){
    	if (jQuery(this).is(":checked")) {
    		tabPosition = i+1;
    		elementId = jQuery(this).val();
    	}
    });
    
    jQuery.post(preferencesUrl, 
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
	jQuery("#skinChoosingDialog").dialog({height:450, width:500, modal:true});
	jQuery("#skinDialogLink")
		.unbind('click', initializeSkinMenu)
		.click(function(){jQuery("#skinChoosingDialog").dialog('open');});

    var skinMenu = jQuery("#skinList").html("").css("padding", "10px");
    jQuery.get(mediaPath + '/skinList.xml?noCache=' + new Date().getTime(), { },
    	function(xml){
			skinXml = xml;
			jQuery("skin", skinXml).each(function(i){
				var key = jQuery(this).children("skin-key").text();
				
				var input
				if (jQuery.browser.msie) {
				    if (key == currentSkin) {
    				    input = jQuery(document.createElement("<input type=\"radio\" name=\"skinChoice\" value=\"" + key + "\" checked=\"true\"/>"));
    				} else {
                        input = jQuery(document.createElement("<input type=\"radio\" name=\"skinChoice\" value=\"" + key + "\"/>"));    				
    				}
				} else {
                    input = jQuery(document.createElement("input")).attr("type", "radio")
                        .attr("name", "skinChoice").val(key);
	                if (key == currentSkin)
	                    input.attr("checked", true);
				}
					
				var span = jQuery(document.createElement("div")).append(
				    jQuery(document.createElement("span"))
						.append(input)
						.append(document.createTextNode(jQuery(this).children("skin-name").text()))
						.addClass("portlet-form-field-label")
					);
				skinMenu.append(span);
				var div = jQuery(document.createElement("div"))
					.addClass("portlet-font-dim").css("padding-left", "20px")
					.css("padding-bottom", "10px");
				div.append(jQuery(document.createElement("span")).text(jQuery(this).children("skin-description").text()));
				div.append(jQuery(document.createElement("br")));
				div.append(jQuery(document.createElement("img")).attr("src", mediaPath + "/" + key + "/" + key + "_thumb.gif"));
				skinMenu.append(div);
			});

            skinMenu.css("height", "300px").css("overflow", "auto");
            
        	// remove the loading graphics and message
        	jQuery("#skinLoading").css("display", "none");
            jQuery("#skinChoosingDialog").parent().parent().css("height", jQuery("#skinChoosingDialog").parent().height() + 20);
    	}
    );
}

function chooseSkin(form) {
    var newskin = jQuery("#skinList").find("input:checked").val();
    if (newskin == undefined || newskin == '')
        return false;
	jQuery.post(preferencesUrl,
		{ action: 'chooseSkin', skinName: newskin },
		function(xml) {
			window.location = portalUrl;
		}
	);
	return false;
}


