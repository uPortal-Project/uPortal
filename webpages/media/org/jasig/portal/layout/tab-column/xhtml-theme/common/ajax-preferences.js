/* Javascript resource for AJAX-style functionality.
 *
 * This script is specific to the tab-column layout developed by Rutgers
 * for uPortal 2.5 with DLM.
 *
 * Author: Jen Bourey (jennifer.bourey@yale.edu)
/*--------------------------------------------------------------------------*/

var channelXml;

/*--------------------------------------------------------------------------
 * Main page functions
/*--------------------------------------------------------------------------*/
 

// initialize dojo menus and clean up unneeded links for the general page
function init(e) {

    var activeTab = dojo.byId("activeTabLink");
    activeTab.removeAttribute('href');
					
    dlg0 = dojo.widget.byId("dialog0");
	var btn = document.getElementById("hider0");
	dlg0.setCloseControl(btn);
					
	dlg1 = dojo.widget.byId("dialog1");
	var btn = document.getElementById("hider1");
	dlg1.setCloseControl(btn);
					
}

// initialize dojo menus and clean up unneeded links for the focused channel page
function initFocused(e) {

    dlg0 = dojo.widget.byId("dialog0");
	var btn = document.getElementById("hider0");
	dlg0.setCloseControl(btn);
					
    var activeLink = dojo.byId("add-channel-link");
    activeLink.href = "javascript:;";
    activeLink.onclick = function(){ dlg0.show() };
					
}

// minimize / maximize channels via javascript
function toggleChannel(channelId) {
    var channel = dojo.byId("portletContent_" + channelId);
    if (channel.style.display == "none") {
        channel.style.display = "block";
        dojo.byId("portletToggleImg_" + channelId).src = skinPath + "/controls/max.gif";
    } else {
        channel.style.display = "none";
        dojo.byId("portletToggleImg_" + channelId).src = skinPath + "/controls/min.gif";
    }
    return false;
}


/*--------------------------------------------------------------------------
 * General helper functions
/*--------------------------------------------------------------------------*/

// Return an array of the direct child elements for a parent with the specified tag name
function getChildElementsByTagName(parentElement, tagName) {
	var items = parentElement.childNodes;
	var desired = new Array();
	for (var i = 0; i < items.length; i++) {
		if (items[i].nodeName.toLowerCase() == tagName.toLowerCase())
			desired.push(items[i]);
	}
	return desired;
}

// Display a dojo.io.bind error as a javascript popup
function displayErrorMessage(type, error) {
    var errmsg = error.message;
    errmsg = errmsg.replace(/XMLHttpTransport Error: 500 /, '');
    alert(errmsg);
}

// Use dojo animations to flash a message to the user.  This method accepts 
// a message to display, as well as an element to use as the target.
function flashMessage(message, elementId) {
    var element = dojo.byId(elementId)
    element.innerHTML = message;
    var flash = dojo.lfx.wipeIn(element, 400, null, function(n) {
        dojo.lang.setTimeout(function(){dojo.lfx.wipeOut(n, 400).play()},1500);
    });
    flash.play();
}


/*--------------------------------------------------------------------------
 * Tab editing functions
/*--------------------------------------------------------------------------*/

// Add a new tab to the layout
function addTab() {
	
	dojo.io.bind({
		content: {
			action: 'addTab'
		},
		load: function(type, data, evt){
		    // add the new tab to the UI and create the link to the newly created layout node
			var newTabId = data.getElementsByTagName("newNodeId")[0].firstChild.data;
			var tab = document.createElement("li");
			tab.id = "tab_" + newTabId;
			var a = document.createElement("a");
			a.href = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (getChildElementsByTagName(dojo.byId("tabList"), "li").length + 1);
			var span = document.createElement("span");
			span.appendChild(document.createTextNode("New Tab"));
			a.appendChild(span);
			tab.appendChild(a);
			dojo.byId("tabList").appendChild(tab);
		},
		url: preferencesUrl,
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});

}

// delete a tab from the layout
function deleteTab(tabId) {
	
	// make sure the user really wants to do this
	var conf = confirm("Are you sure you want to remove this tab and all its content?");
    if (!conf)
        return false;

	dojo.io.bind({
		content: { 
			action: 'removeElement', 
			elementID: tabId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		    // refresh the window to the first tab, since the tab we were on doesn't
		    // exist anymore
		    window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=1";
		 },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/plain"
	});

}

// Show / hide the tab editing dialog
function toggleEditTabDialog(action) {
    var tabNameDiv = dojo.byId("activeTabLink");
    var editTabDiv = dojo.byId("editTabLink");
    if (action == "show") {
        tabNameDiv.style.display = "none";
        editTabDiv.style.display = "block";
        tabNameDiv.parentNode.className = "tab-edit";
    } else {
        tabNameDiv.parentNode.className = "editable-tab";
        tabNameDiv.style.display = "block";
        editTabDiv.style.display = "none";
    }
}

// Update the name of the current tab
function updateTabName(tabId) {
    var name = dojo.byId('newTabName').value;
    var tabNameDiv = dojo.byId("tabName");

	dojo.io.bind({
		content: {
			action: 'renameTab',
			tabId: tabId,
			tabName: name
		},
		url: preferencesUrl,
		load: function(type, data, evt){
            tabNameDiv.innerHTML = "";
            var editTabDiv = dojo.byId("editTabName");
            tabNameDiv.appendChild(document.createTextNode(name));
            toggleEditTabDialog("hide");
		},
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/plain"
	});
	return false;
}

// Move the current tab left or right
function moveTab(sourceId, direction) {
				
	var source = dojo.byId("tab_" + sourceId);
	var parent = dojo.byId("tabList");
	var columns = getChildElementsByTagName(parent, "li");
	var sourcePosition;
	
	// find the current position of the tab
	for (var i = 0; i < columns.length; i++) {
		if (("tab_" + sourceId) == columns[i].id) {
			sourcePosition = i;
			break;
		}
	}
	
	// if this is not a valid move request, just return
	if ((direction == "left" && sourcePosition == 0) || direction == "right" && sourcePosition == columns.length-1)
	    return;
	
	parent.removeChild(source);
	var method = 'insertBefore';
	var elementId;
	if (direction == "left") {
		parent.insertBefore(source, columns[sourcePosition-1]);
		elementId = columns[sourcePosition-1].id.split("_")[1];
		sourcePosition--;
	} else if (sourcePosition != columns.length-2) {
		elementId = columns[sourcePosition+2].id.split("_")[1];
		parent.insertBefore(source, columns[sourcePosition+2]);
		sourcePosition++;
	} else {
		method = 'appendAfter';
		elementId = columns[sourcePosition+1].id.split("_")[1];
		parent.appendChild(source);
		sourcePosition++;
	}


	// the tabs' links work by relative position, so we need
	// to redo them now that we've changed the order
    var tabLinks = dojo.byId("tabList").getElementsByTagName("a");
    var count = 1;
    for (var i = 0; i < tabLinks.length; i++) {
        var link = tabLinks[i].href;
        if (tabLinks[i].id != 'editTabLink') {
            if (link != null && i != sourcePosition)
                tabLinks[i].href = link.substring(0, link.lastIndexOf("=")+1) + count;
            count++;
        }
    }
    sourcePosition++;
    
    // persist the change
	dojo.io.bind({
		content: {
			action: 'moveTabHere',
			sourceID: sourceId,
			method: method,
			elementID: elementId,
			tabPosition: sourcePosition
		},
		url: preferencesUrl,
		load: function(type, data, evt){ },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/plain"
	});
	
}


/*--------------------------------------------------------------------------
 * Page layout (column) editing functions
/*--------------------------------------------------------------------------*/

// Change the number of columns on the page
function changeColumns(num) {
	var parentDiv = document.getElementById("portal-page-columns");
	var columns = getChildElementsByTagName(parentDiv, "td");
	var container = dojo.widget.byId("columnWidths");
	for (var i = num; i < columns.length; i++) {
	    var column = dojo.widget.byId("columnWidth_" + columns[i].id.split("_")[1]);
	    container.removeChild(column);
	    
	    var channels = getChildElementsByTagName(columns[i], "div");
	    for (var j = 0; j < channels.length; j++) {
	        columns[i].removeChild(channels[j]);
	        columns[num-1].appendChild(channels[j]);
	    }
	    parentDiv.removeChild(columns[i]);
	}
	
	dojo.io.bind({
		content: { 
			action: 'changeColumns', 
			columnNumber: num,
			tabId: tabId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		    var ids = data.getElementsByTagName("id");
		    if (ids.length > 0) {
		        window.location = portalUrl;
		        return;
		    }
		    
//		    for (var i = 0; i < ids.length; i++) {
//		        var newId = ids[i].firstChild.data;
//        	    var column = document.createElement("td");
//        	    column.id = "column_" + newId;
//            	column.style.verticalAlign = "top";
//	            parentDiv.appendChild(column);
//        	    var columnWidth = dojo.widget.createWidget("ContentPane", {widgetId:'columnWidth_' + newId, isContainer: true});
//                columnWidth.setContent('Column ' + (columns.length + i + 1));
//                columnWidth.id = 'columnWidth_' + newId;
//        	    container.addChild(columnWidth);
//		    }

        	// update column widths
	        columns = getChildElementsByTagName(parentDiv, "td");
    		var columnIds = new Array();
	        var columnWidth = Math.floor(100 / (columns.length));
	        for (var i = 0; i < columns.length; i++) {
	            columns[i].style.width = columnWidth + "%";
		        columnIds.push(columns[i].id.split("_")[1] + "dt");
		        dojo.widget.byId("columnWidth_" + columns[i].id.split("_")[1]).sizeShare = columnWidth;
	        }
	        container._layoutPanels();

   			// add the new column as a drag target for channel arranging
	        for (var i = 0; i < ids.length; i++) {
    		    new portal.widget.PortletDropTarget("column_" + ids[i].firstChild.data, columnIds);
	        }
		 },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});
	
	
}

function updateContainerWidths() {
	var parentDiv = document.getElementById("portal-page-columns");
	var columns = getChildElementsByTagName(parentDiv, "td");
	var container = dojo.widget.byId("columnWidths");
	var sizes = new Array();
	var totalSize = 0;
	var ids = new Array();
	var widths = new Array();
	var containers = container.getChildrenOfType('ContentPane', false);
	for (var i = 0; i < columns.length; i++) {
	    var id = containers[i].widgetId.split("_")[1];
	    sizes[id] = containers[i].sizeShare;
	    totalSize += sizes[id];
	}
	for (var i = 0; i < columns.length; i++) {
	    var id = columns[i].id.split("_")[1];
	    columns[i].width = ((sizes[id]/totalSize) * 100) + "%";
	    ids.push(id);
	    widths.push(columns[i].width);
	}
	dojo.io.bind({
		content: { 
			action: 'updateColumnWidths', 
			columnIds: ids,
			columnWidths: widths
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		 },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});
}


/*--------------------------------------------------------------------------
 * Channel editing UI functions
/*--------------------------------------------------------------------------*/

// display the add channel dialog and initialize it if necessary
function showAddChannelDialog() {

	// show the dialog
	dlg0.show();

    // if this is the first time we're displaying the add channel
    // dialog, get the channel list and add it to the dialog
	var categorySelect = dojo.byId("categorySelectMenu");
	if (categorySelect.getElementsByTagName("option").length == 0) {
		initializeChannelSelection();
	}
	
}

// loads the channel list into the channel selection dialog
function initializeChannelSelection() {

	var categorySelect = dojo.byId("categorySelectMenu");

	dojo.io.bind({
		url: channelListUrl,
		load: function(type, data, evt){
			channelXml = data;
        	var categories = channelXml.getElementsByTagName("category");
            var matching = new Array();
            for (var i = 0; i < categories.length; i++) {
               matching.push(categories[i]);
            }
            matching.sort(sortCategoryResults);
        	var j = 0;
        	for (var i = 0; i < matching.length; i++) {
        		if (matching[i].getElementsByTagName("channel").length > 0) {
        		    categorySelect.options[j] = new Option(matching[i].getAttribute("name"), matching[i].getAttribute("ID"));
                    if (j == 0)
            		    categorySelect.options[j].selected = true;
            		j++;
        		}
        	}
        
        	browseChannelCategory();
        	
        	// remove the loading graphics and message
    		dojo.byId("channelLoading").style.display = "none";
    		dojo.byId("categorySelectMenu").style.backgroundImage = "none";
    		dojo.byId("channelSelectMenu").style.backgroundImage = "none";
		},	
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});

				
}

// select a channel category from the menu and display a list of channels in
// the category
function browseChannelCategory() {
	var categoryId = document.getElementById("categorySelectMenu").value;
	var category;
	var categories = channelXml.getElementsByTagName("category");
	for (var i = 0; i < categories.length; i++) {
		if (categories[i].getAttribute("ID") == categoryId) {
			category = categories[i];
		}
	}
				
	var channelSelect = document.getElementById("channelSelectMenu");
	channelSelect.innerHTML = "";
	
	var channels = category.getElementsByTagName("channel");
    var matching = new Array();
    for (var i = 0; i < channels.length; i++) {
       matching.push(channels[i]);
    }
    matching.sort(sortChannelResults);

    var lastId = "";
	var j = 0;
	for (var i = 0; i < matching.length; i++) {
        if (matching[i].getAttribute("ID") != lastId) {
    	    channelSelect.options[j] = new Option(matching[i].getAttribute("name"), matching[i].getAttribute("ID"));
            if (j == 0)
  		        channelSelect.options[j].selected = true;
            lastId = matching[i].getAttribute("ID");
   		    j++;
   	    }
	}

	selectChannel(channelSelect.value);
	
}

// Search the channel registry for names or descriptions matching a given string on
// case-insensitive basis
function searchChannels() {
    var searchTerm = dojo.byId("addChannelSearchTerm").value.toLowerCase();
    var channels = channelXml.getElementsByTagName("channel");
    var searchResults = dojo.byId("addChannelSearchResults");
    searchResults.innerHTML = "";
    
    // find matching channels
    var matching = new Array();
    for (var i = 0; i < channels.length; i++) {
        if (channels[i].getAttribute("name").toLowerCase().indexOf(searchTerm) >= 0 || channels[i].getAttribute("description").toLowerCase().indexOf(searchTerm) >= 0) {
            matching.push(channels[i]);
        }
    }

    // sort the resulting channels by name
    matching.sort(sortChannelResults);
    
    // add each match to the results list on the UI screen
    var lastId = "";
    for (var i = 0; i < matching.length; i++) {
        if (matching[i].getAttribute("ID") != lastId) {
            var li = document.createElement("li");
            var a = document.createElement("a");
            a.id = "searchResult_" + matching[i].getAttribute("ID");
            a.appendChild(document.createTextNode(matching[i].getAttribute("name")));
            a.href = "javascript:;";
            a.onclick = function(){selectChannel(this.id.split("_")[1])};
            li.appendChild(a);
            searchResults.appendChild(li);
            lastId = matching[i].getAttribute("ID");
        }
    }

}

// sort a list of returned channels by name
function sortCategoryResults(a, b) {
    var aname = a.getAttribute("name").toLowerCase();
    var bname = b.getAttribute("name").toLowerCase();
    if (aname == 'new')
        return -1;
    if (bname == 'new')
        return 1;
    if (aname == 'popular')
        return -1;
    if (bname == 'popular')
        return 1;
    if(aname > bname)
        return 1;
    if(aname < bname)
        return -1;
    return 0;
}

// sort a list of returned channels by name
function sortChannelResults(a, b) {
    var aname = a.getAttribute("name").toLowerCase();
    var bname = b.getAttribute("name").toLowerCase();
    if(aname > bname)
        return 1;
    if(aname < bname)
        return -1;
    return 0;
}

// Select a channel from the menu and display the associated information along with 
// a form allowing the user to add it to the layout
function selectChannel(channelId) {
	var channel;

    // find the requested channel in the channel registry
	var channels = channelXml.getElementsByTagName("channel");
	for (var i = 0; i < channels.length; i++) {
		if (channels[i].getAttribute("ID") == channelId) {
			channel = channels[i];
		}
	}

    // add the title and description to the UI				
	var channelTitle = dojo.byId("channelTitle");
	channelTitle.innerHTML = "";
	channelTitle.appendChild(document.createTextNode(channel.getAttribute("name")));

	var channelDescription = dojo.byId("channelDescription");
	channelDescription.innerHTML = "";
	channelDescription.appendChild(document.createTextNode(channel.getAttribute("description")));
	
	var addChannelInput = dojo.byId("addChannelId");
	addChannelInput.value = channelId;
	
	var previewChannelLink = dojo.byId("previewChannelLink");
	previewChannelLink.onclick = function(){ window.location = portalUrl + "?uP_fname=" + channel.getAttribute("fname"); };

    // if this channel has user-overrideable parameters, present a form allowing the
    // user to input values
    var parameters = channel.getElementsByTagName("parameter");
    for (var i = 0; i < parameters.length; i++) {
        if (parameters[i].getAttribute("override") == "yes") {
            var p = document.createElement("p");
//            p.appendChild(document.createTextNode(parameters[i].getAttribute("name") + ": "));
            var input = document.createElement("input");
            input.type = "hidden";
            input.name = parameters[i].getAttribute("name");
            input.value = parameters[i].getAttribute("value");
            p.appendChild(input);
            channelDescription.appendChild(p);
        }
    }

}


/*--------------------------------------------------------------------------
 * Channel editing persistence functions
/*--------------------------------------------------------------------------*/

// Commit a channel add request to the storage layer.  This function is used when
// users add channels via the browse or search methods on a non-focused page.
function addChannel() {

	var columnContainer = document.getElementById("portal-page-columns");
	var column = getChildElementsByTagName(columnContainer, "td")[0];
	var channelId = document.getElementById("addChannelId").value;
	var channels = column.getElementsByTagName("div");

    var params = new Array();
    
    // add any channel-specific user-editable parameters
	var inputs = dojo.byId("channelDescription").getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
	    params[inputs[i].name] = inputs[i].value;
	}

    params['action'] = 'addChannel';
    params['channelID'] = channelId;
    params['position'] = 'insertBefore';
    
    // if the first column has multiple channels, use the id of the first channel.
    // otherwise, use the id of the column itself
	if (channels.length > 0)
        params['elementID'] = channels[0].id.split("_")[1];
    else
        params['elementID'] = column.id.split("_")[1];

    // persist the channel addition
	dojo.io.bind({
		content: params,
		url: preferencesUrl,
		load: function(type, data, evt){
		    // once the change has been persisted, refresh the current tab to
		    // show the new channel
		    window.location = portalUrl;
		 },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});

}

// Commit a focused channel add request to the storage layer.  This function is used
// by the "add to my layout" feature on a focused channel page.
function addFocusedChannel(form) {

    var channelId = form.channelId.value;
    var selectedTab;
    var tabPosition;
    
    // find the tabId of the desired target page to add
    // this channel to
    for (var i = 0; i < form.targetTab.length; i++) {
        if (form.targetTab[i].checked) {
            selectedTab = form.targetTab[i].value;
            tabPosition = i+1;
        }
    }
    
    var params = new Array();
    params['action'] = 'addChannel';
    params['channelID'] = 'chan' + channelId;
    params['position'] = 'insertBefore';
    params['elementID'] = selectedTab;

    // persist the change
	dojo.io.bind({
		content: params,
		url: preferencesUrl,
		load: function(type, data, evt){
		    // once the change has been saved to the user layout, 
		    // reload the page to the target tab
		    window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + tabPosition;
	    },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/xml"
	});
	return false;

}

// Strips a channel from the layout and persists the change to the backend 
function deleteChannel(channelId) {
	
	var channel = dojo.byId("portlet_" + channelId);
    
    // make sure the user really wants to delete the channel
	var conf = confirm("Are you sure you want to remove this portlet?");
	if (!conf)
	    return false;

    // persist the change
	dojo.io.bind({
		content: { 
			action: 'removeElement', 
			elementID: channelId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		    // once the change has been persisted, strip the channel
		    // div from the page
			var parentDiv = channel.parentNode;
			parentDiv.removeChild(channel);
		 },
		error: function(type, error){ displayErrorMessage(type, error); },
		mimetype: "text/plain"
	});
	
	return false;
				
}
