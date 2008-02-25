// Revision: 2007-09-21 gthompson

// This flyout menu implementation relies on an iframe shim placed behind
// the div for each submenu.  This iframe prevents the submenu from 
// sliding behind form elements

var UPFlyoutMenu = function(divClass, iframeClass) {

	this.divClass = divClass;
	this.iframeClass = iframeClass;

	// show the subnavigation menu under the element
	this.showSubnav = function(id) {
		var tab = $("#" + id);
		// show the subnavigation div
		var div = tab.find(this.divClass)
			.css("display", "block");
		// show the backing iframe to the size of the associated div
		tab.find(this.iframeClass)
			.css("width", div.clientWidth + 'px')
			.css("height", div.clientHeight + 'px')
			.css("display", "block");
	},
	
	// hide the subnavigation menu, but only if the user really
	// moused off it
	this.hideSubnav = function(id, e) {
		// check to see if the user really moused out of the nav menu
	    if (!e) var e = window.event;
	    if ($((e.relatedTarget) ? e.relatedTarget : e.toElement).parents("[@id=" + id + "]").length > 0) 
	    	return;
		else
			this.closeSubnav(id);
	},
	
	// close the subnavigation menu
	this.closeSubnav = function(id) {
		// get the tab and find and hide it's subnavigation
		$("#" + id)
			.find(this.divClass).css("display", "none").end() // hide subnav container
			.find(this.iframeClass).css("display", "none");   // hide backing iframe
	}
	
}
