/* Javascript resource for AJAX-style functionality.
 *
 * This script is specific to the tab-column layout developed by Rutgers
 * for uPortal 2.5 with DLM.
 *
 * Author: Jen Bourey (jennifer.bourey@yale.edu)
/*--------------------------------------------------------------------------*/

var channelXml;

/*--------------------------------------------------------------------------*/

function init(e) {

	var dojoMenus = dojo.byId("dojoMenus");
	dojoMenus.style.display = "block";

    var activeTab = dojo.byId("activeTabLink");
    activeTab.removeAttribute('href');
					
    dlg0 = dojo.widget.byId("dialog0");
	var btn = document.getElementById("hider0");
	dlg0.setCloseControl(btn);
					
	var editableTab = dojo.widget.byId("editableTab");
	dojo.event.connect(editableTab, "onSave", "updateTabName");
					
	dlg1 = dojo.widget.byId("dialog1");
	var btn = document.getElementById("hider1");
	dlg1.setCloseControl(btn);
					
	dlg2 = dojo.widget.byId("dialog2");
	var btn = document.getElementById("hider2");
	dlg2.setCloseControl(btn);

}

function initFocused(e) {

	var dojoMenus = dojo.byId("dojoMenus");
	dojoMenus.style.display = "block";

    dlg0 = dojo.widget.byId("dialog0");
	var btn = document.getElementById("hider0");
	dlg0.setCloseControl(btn);
					
    var activeLink = dojo.byId("add-channel-link");
    activeLink.href = "javascript:;";
    activeLink.onclick = function(){ dlg0.show() };
					
}

// return the element ID of the first portal column
function getFirstColumn() {
	var columnContainer = document.getElementById("portal-page-columns");
	var first = getChildElementsByTagName(columnContainer, "td")[0];
	return first.id;
}

// return an array of the direct child elements for a parent with the specified tag name
function getChildElementsByTagName(parentElement, tagName) {
	var items = parentElement.childNodes;
	var desired = new Array();
	for (var i = 0; i < items.length; i++) {
		if (items[i].nodeName.toLowerCase() == tagName.toLowerCase())
			desired.push(items[i]);
	}
	return desired;
}

function createControl(type, item) {
	var a = document.createElement("a");
	var img = document.createElement("img");
	if (type == 'left') {
		img.src = skinPath + '/controls/leftarrow.gif';
		a.title = 'Move ' + item + ' left';
	} else if (type == 'right') {
		img.src = skinPath + '/controls/rightarrow.gif';
		a.title = 'Move ' + item + ' right';
	} else if (type == 'remove') {
		img.src = skinPath + '/controls/remove.gif';
		a.title = 'Remove ' + item;
	}
	a.href = "javascript:;";
	a.appendChild(img);
	return a;
}


/*--------------------------------------------------------------------------*/

function updateTabName(newValue, oldValue) {
	dojo.io.bind({
		content: {
			action: 'renameTab',
			tabId: tabId,
			tabName: newValue
		},
		url: preferencesUrl,
		load: function(type, data, evt){ },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});
}


function addTab() {
	
	var editParentDiv = document.getElementById("layout-edit-tabs");
	var editColumns = getChildElementsByTagName(editParentDiv, "td");
	var lastColumn = editColumns[editColumns.length - 1].id.split("_")[1];
				
	var td = document.createElement("td");
	td.verticalAlign = "top";

	var editTd = document.createElement("td");
	var titleDiv = document.createElement("div");
	titleDiv.className = "container-title";
	titleDiv.appendChild(document.createTextNode("New Tab"));
	var columnDiv = document.createElement("div");

	var a = createControl('right', 'tab');
	a.onclick = function(){moveTab(this.parentNode.parentNode.id.split("_")[1], "left")};
	columnDiv.appendChild(a);

	var a = createControl('left', 'tab');
	a.onclick = function(){moveTab(this.parentNode.parentNode.id.split("_")[1], "right")};
	columnDiv.appendChild(a);

	var a = createControl('remove', 'tab');
	a.onclick = function(){deleteTab(this.parentNode.parentNode.id.split("_")[1])};
	columnDiv.appendChild(a);

	editTd.appendChild(titleDiv);
	editTd.appendChild(columnDiv);

	dojo.io.bind({
		content: {
			action: 'addTab'
		},
		load: function(type, data, evt){
			var newTabId = data.getElementsByTagName("tabId")[0].firstChild.data;
			var tab = document.createElement("li");
			tab.id = "tab_" + newTabId;
			var a = document.createElement("a");
			a.href = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + (getChildElementsByTagName(dojo.byId("tabList"), "li").length + 1);
			var span = document.createElement("span");
			span.appendChild(document.createTextNode("New Tab"));
			a.appendChild(span);
			tab.appendChild(a);
			dojo.byId("tabList").appendChild(tab);
			editTd.id = "layoutTab_" + newTabId;
			editParentDiv.appendChild(editTd);
		},
		url: preferencesUrl,
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/xml"
	});
	

}

function moveTab(sourceId, direction) {
				
	var source = dojo.byId("tab_" + sourceId);
	var editColumn = dojo.byId("layoutTab_" + sourceId);
	var parent = dojo.byId("tabList");
	var editParent = dojo.byId("layout-edit-tabs");
	var columns = getChildElementsByTagName(parent, "li");
	var editColumns = getChildElementsByTagName(editParent, "td");
	var sourcePosition;
	
	for (var i = 0; i < columns.length; i++) {
		if (("tab_" + sourceId) == columns[i].id) {
			sourcePosition = i;
			break;
		}
	}
	
	parent.removeChild(source);
	editParent.removeChild(editColumn);
	var method = 'insertBefore';
	var elementId;
	if (direction == "left") {
		parent.insertBefore(source, columns[sourcePosition-1]);
		editParent.insertBefore(editColumn, editColumns[sourcePosition-1]);
		elementId = columns[sourcePosition-1].id.split("_")[1];
	} else if (sourcePosition != columns.length-2) {
		elementId = columns[sourcePosition+2].id.split("_")[1];
		parent.insertBefore(source, columns[sourcePosition+2]);
		editParent.insertBefore(editColumn, editColumns[sourcePosition+2]);
	} else {
		method = 'appendAfter';
		elementId = columns[sourcePosition+1].id.split("_")[1];
		parent.appendChild(source);
		editParent.appendChild(editColumn);
	}


	// the tabs' links work by relative position, so we need
	// to redo them now that we've changed the order
    var tabLinks = dojo.byId("tabList").getElementsByTagName("a");
    for (var i = 0; i < tabLinks.length; i++) {
        var link = tabLinks[i].href;
        if (link != null) {
            tabLinks[i].href = link.substring(0, link.lastIndexOf("=")+1) + (i+1);
        }
    }

	dojo.io.bind({
		content: {
			action: 'moveTabHere',
			sourceID: sourceId,
			method: method,
			elementID: elementId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});
	
}


function deleteTab(tabId) {
	
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
		    // remove the tab from the page
			var parentDiv = document.getElementById("tabList");
			parentDiv.removeChild(document.getElementById("tab_" + tabId));
            // remove the tab from the edit tabs menu
			parentDiv = dojo.byId("layout-edit-tabs");
			parentDiv.removeChild(dojo.byId("layoutTab_" + tabId));
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});

}



/*--------------------------------------------------------------------------*/

function showEditColumnsDialog() {

    // for each column, add a list of its channels so the user knows what 
    // s/he is editing
    var columns = getChildElementsByTagName(dojo.byId("portal-page-columns"), "td");
    for (var i = 0; i < columns.length; i++) {
        var contentsDiv = dojo.byId("columnContents_" + columns[i].id.split("_")[1]);
        contentsDiv.innerHTML = "";
        var headings = columns[i].getElementsByTagName("h2");
        for (var j = 0; j < headings.length; j++) {
            var h2 = headings[j];
            if (h2.parentNode.id != null && h2.parentNode.id.indexOf("toolbar") == 0) {
                var div = document.createElement("div");
                div.appendChild(document.createTextNode(h2.firstChild.data));
                contentsDiv.appendChild(div);
            }
        }
    }
    
    // show the edit columns dialog screen
    dlg1.show();
    
}

// add a new column to the page and commit the change to the storage layer
function addColumn() {
				
	var parentDiv = document.getElementById("portal-page-columns");
	var columns = getChildElementsByTagName(parentDiv, "td");
	var editParentDiv = document.getElementById("layout-edit-columns");
	var editColumns = getChildElementsByTagName(editParentDiv, "td");
	var lastColumn = columns[columns.length - 1].id.split("_")[1];

    // adjust all the column widths
	var columnWidth = Math.floor(100 / (columns.length + 1)) + "%";
	for (var i = 0; i < columns.length; i++) {
	    columns[i].style.width = columnWidth;
	}
	for (var i = 0; i < editColumns.length; i++) {
	    editColumns[i].style.width = columnWidth;
	}
				
	var td = document.createElement("td");
	td.style.verticalAlign = "top";

	var editTd = document.createElement("td");
	var titleDiv = document.createElement("div");
	titleDiv.className = "container-title";
	titleDiv.appendChild(document.createTextNode("Column " + (columns.length + 1)));
	var columnDiv = document.createElement("div");

    // add the movement controls to the edit menu for the new column
	var a = createControl('left', 'column');
	a.onclick = function(){moveColumn(this.parentNode.parentNode.id.split("_")[1], "left")};
	columnDiv.appendChild(a);

	var a = createControl('right', 'column');
	a.onclick = function(){moveColumn(this.parentNode.parentNode.id.split("_")[1], "right")};
	columnDiv.appendChild(a);

	var a = createControl('remove', 'column');
	a.onclick = function(){deleteColumn(this.parentNode.parentNode.id.split("_")[1])};
	columnDiv.appendChild(a);
	
	var contentsDiv = document.createElement("div");
	columnDiv.appendChild(contentsDiv);

	editTd.appendChild(titleDiv);
	editTd.appendChild(columnDiv);

	dojo.io.bind({
		content: { 
			action: 'addColumn', 
			elementID: lastColumn
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		    var newId = data.getElementsByTagName("response")[0].firstChild.data;
			td.id = "column_" + newId;
			td.style.width = columnWidth;
			parentDiv.appendChild(td);
			editTd.id = "layoutColumn_" + newId;
			editTd.style.width = columnWidth;
        	contentsDiv.id = "columnContents_" + newId;
			editParentDiv.appendChild(editTd);
			
			// add the new column as a drag target for channel arranging
			var columnIds = new Array();
			for (var i = 0; i < columns.length; i++) {
			    columnIds.push(columns[i].id.split("_")[1] + "dt");
			}
			new portal.widget.PortletDropTarget("column_" + newId, columnIds);
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/xml"
	});
				
}

// move a portal column and commit the change to the storage layer
function moveColumn(sourceId, direction) {
				
	var source = dojo.byId("column_" + sourceId);
	var editColumn = dojo.byId("layoutColumn_" + sourceId);
	var parent = dojo.byId("portal-page-columns");
	var editParent = dojo.byId("layout-edit-columns");
	var columns = getChildElementsByTagName(parent, "td");
	var editColumns = getChildElementsByTagName(editParent, "td");
	var sourcePosition;
				
	for (var i = 0; i < columns.length; i++) {
		if (("column_" + sourceId) == columns[i].id) {
			sourcePosition = i;
			break;
		}
	}
				
	parent.removeChild(source);
	editParent.removeChild(editColumn);
	var method = 'insertBefore';
	var elementId;
	if (direction == "left") {
		parent.insertBefore(source, columns[sourcePosition-1]);
		editParent.insertBefore(editColumn, editColumns[sourcePosition-1]);
		elementId = columns[sourcePosition-1].id.split("_")[1];
	} else if (sourcePosition != columns.length-2) {
		elementId = columns[sourcePosition+2].id.split("_")[1];
		parent.insertBefore(source, columns[sourcePosition+2]);
		editParent.insertBefore(editColumn, editColumns[sourcePosition+2]);
	} else {
		method = 'appendAfter';
		elementId = columns[sourcePosition+1].id.split("_")[1];
		parent.appendChild(source);
		editParent.appendChild(editColumn);
	}


	dojo.io.bind({
		content: {
			action: 'moveColumnHere',
			sourceID: sourceId,
			method: method,
			elementID: elementId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});
	
}

// remove the requested column from the page and commit the change
// to the storage layer
function deleteColumn(columnId) {
	
	var conf = confirm("Are you sure you want to remove this column and all its channels?");
    if (!conf)
        return false;
	dojo.io.bind({
		content: { 
			action: 'removeElement', 
			elementID: columnId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
			var parentDiv = document.getElementById("portal-page-columns");
			parentDiv.removeChild(document.getElementById("column_" + columnId));
			parentDiv = dojo.byId("layout-edit-columns");
			parentDiv.removeChild(dojo.byId("layoutColumn_" + columnId));
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});

}


/*--------------------------------------------------------------------------*/

// loads the channel list into the channel selection dialog
function initializeChannelSelection() {

	var categorySelect = dojo.byId("categorySelectMenu");

	dojo.io.bind({
		url: channelListUrl,
		sync: true,
		load: function(type, data, evt){
			channelXml = data;
		},	
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/xml"
	});

	var categories = channelXml.getElementsByTagName("category");
	var j = 0;
	for (var i = 0; i < categories.length; i++) {
		if (categories[i].getElementsByTagName("channel").length > 0) {
		    categorySelect.options[j] = new Option(categories[i].getAttribute("name"), categories[i].getAttribute("ID"));
            if (j == 0)
    		    categorySelect.options[j].selected = true;
    		j++;
		}
	}

	selectChannelCategory();
				
}

function showAddChannelDialog() {

    // if this is the first time we're displaying the add channel
    // dialog, get the channel list and add it to the dialog
	var categorySelect = dojo.byId("categorySelectMenu");
	if (categorySelect.getElementsByTagName("option").length == 0) {
		initializeChannelSelection();
	}
	
	// show the dialog
	var dlg0 = dojo.widget.byId("dialog0");
	dlg0.show();
}
						

/*--------------------------------------------------------------------------*/

// select a channel category from the menu and display a list of channels in
// the category
function selectChannelCategory() {
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

	selectChannelChannel(channelSelect.value);
	
}

function selectChannelChannel(channelId) {
	var channel;
	var channels = channelXml.getElementsByTagName("channel");
	for (var i = 0; i < channels.length; i++) {
		if (channels[i].getAttribute("ID") == channelId) {
			channel = channels[i];
		}
	}
				
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

    var parameters = channel.getElementsByTagName("parameter");
    for (var i = 0; i < parameters.length; i++) {
        if (parameters[i].getAttribute("override") == "yes") {
            var p = document.createElement("p");
            p.appendChild(document.createTextNode(parameters[i].getAttribute("name") + ": "));
            var input = document.createElement("input");
            input.name = parameters[i].getAttribute("name");
            input.value = parameters[i].getAttribute("value");
            p.appendChild(input);
            channelDescription.appendChild(p);
        }
    }

}

function searchChannels() {
    var searchTerm = dojo.byId("addChannelSearchTerm").value.toLowerCase();
    var channels = channelXml.getElementsByTagName("channel");
    var searchResults = dojo.byId("addChannelSearchResults");
    searchResults.innerHTML = "";
    
    var matching = new Array();
    for (var i = 0; i < channels.length; i++) {
        if (channels[i].getAttribute("name").toLowerCase().indexOf(searchTerm) >= 0 || channels[i].getAttribute("description").toLowerCase().indexOf(searchTerm) >= 0) {
            matching.push(channels[i]);
        }
    }

    var lastId = "";
    matching.sort(sortChannelResults);
    for (var i = 0; i < matching.length; i++) {
        if (matching[i].getAttribute("ID") != lastId) {
            var li = document.createElement("li");
            li.appendChild(document.createTextNode(matching[i].getAttribute("name")));
            li.id = "searchResult_" + matching[i].getAttribute("ID");
            li.onclick = function(){selectChannelChannel(this.id.split("_")[1])};
            searchResults.appendChild(li);
            lastId = matching[i].getAttribute("ID");
        }
    }


}

function sortChannelResults(a, b) {
    var aname = a.getAttribute("name").toLowerCase();
    var bname = b.getAttribute("name").toLowerCase();
    if(aname > bname)
        return 1;
    if(aname < bname)
        return -1;
    return 0;
}


// commit a channel add request to the storage layer
function addChannel() {

	var column = document.getElementById(getFirstColumn());
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
	if (channels.length > 0)
        params['elementID'] = channels[0].id.split("_")[1];
    else
        params['elementID'] = column.id.split("_")[1];

	dojo.io.bind({
		content: params,
		url: preferencesUrl,
		load: function(type, data, evt){
		    window.location = portalUrl;
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/xml"
	});

}

// commit a channel add request to the storage layer
function addFocusedChannel(form) {

    var channelId = form.channelId.value;
    var selectedTab;
    var tabPosition;
    
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

	dojo.io.bind({
		content: params,
		url: preferencesUrl,
		load: function(type, data, evt){
		    window.location = portalUrl + "?uP_root=root&uP_sparam=activeTab&activeTab=" + tabPosition;
	    },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/xml"
	});
	return false;

}

function deleteChannel(channelId) {
	
	var channel = dojo.byId("portlet_" + channelId);
	var conf = confirm("Are you sure you want to remove this channel?");
	if (!conf)
	    return false;
	dojo.io.bind({
		content: { 
			action: 'removeElement', 
			elementID: channelId
		},
		url: preferencesUrl,
		load: function(type, data, evt){
			var parentDiv = channel.parentNode;
			parentDiv.removeChild(channel);
		 },
		error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
		mimetype: "text/plain"
	});
	
	return false;
				
}
