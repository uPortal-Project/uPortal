// Revision: 2007-09-21 gthompson

// This flyout menu implementation relies on an iframe shim placed behind
// the div for each submenu.  This iframe prevents the submenu from 
// sliding behind form elements

var tabsContainerName = "portalNavigationList";
var tabContainerName = "portalNavigation_";
var tabLinkName = "tabLink_";

function initFlyoutMenus() {

	// initialize the x and y positions for our flyout menus
	var yoffset = document.getElementById(tabsContainerName).clientHeight;
	var xoffset = 0;

	$("[@id*=" + tabLinkName + "]").each(function(i){

		var id = $(this).attr("id").split("_")[1];
		var tab = $('#' + tabContainerName + id);
		xoffset += parseInt(tab.css("margin-left"));

		// if this navigation item has submenu items, set up the flyout menu
		if ($(this).parent().children("iframe").length > 0) {

			// set the mouseover and mouseout functions
			$(this).parent()
				.mouseover(function(){showSubnav(this.id.split("_")[1]);})
				.mouseout(function(event){hideSubnav(this.id.split("_")[1], event);});

			// pin the associated div and iframe to the bottom of the navigation item
			var menu = $(this).find(".portal-flyout-container")
				.css("top", yoffset + 'px').css("left", xoffset + 'px');
			var frame = $(this).find(".portal-flyout-iframe")
				.css("top", yoffset + 'px').css("left", xoffset + 'px');
		}
		
		// add the width and associated padding of this element to the xoffset
		// so we can position the next element
		xoffset += $(this).get(0).clientWidth;
		xoffset += parseInt(tab.css("margin-right")) + parseInt(tab.css("border-left"))
			+ parseInt(tab.css("border-right"));
	});
}

function showSubnav(id) {
	var tab = $("#" + tabContainerName + id);
	// show the subnavigation div
	var div = tab.find(".portal-flyout-container")
		.css("display", "block");
	// show the backing iframe to the size of the associated div
	tab.find("portal-flyout-iframe")
		.css("display", "block")
		.css("width", div.clientWidth + 'px')
		.css("height", div.clientHeight + 'px');
}

function hideSubnav(id, e) {
	// check to see if the user really moused out of the nav menu
    if (!e) var e = window.event;
    var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
    if ($(reltg).parents("[@id=" + tabContainerName + id + "]").length > 0) return;

	// hide the div and iframe
	$("#" + tabContainerName + id)
		.find(".portal-flyout-container").css("display", "none").end()
		.find("portal-flyout-iframe").css("display", "none");
}
function forceHideSubnav(id) {
	// hide the div and iframe
	$("#" + tabContainerName + id)
		.find(".portal-flyout-container").css("display", "none").end()
		.find("portal-flyout-iframe").css("display", "none");
}
