// Revision: 2007-09-21 gthompson

// This flyout menu implementation relies on an iframe shim placed behind
// the div for each submenu.  This iframe prevents the submenu from 
// sliding behind form elements

var UPFlyoutMenu = function(divClass, iframeClass, position) {

	this.divClass = divClass;
	this.iframeClass = iframeClass;
	this.position = position;
	var xoffset = $("#portalNavigationList").get(0).offsetLeft; 
	var yoffset = $("#portalNavigationList").get(0).offsetTop; 
	
	this.init = function() {
        $("[@id*=portalNavigation_]")
          .mouseover(function(){fly.showSubnav(this.id.split("_")[1]);})
          .mouseout(function(event){fly.hideSubnav(this.id.split("_")[1], event);});
        $("[@id*=portalFlyoutNavigation_]")
          .mouseout(function(event){fly.hideSubnav(this.id.split("_")[1], event);});
        $("[@id*=navFrame_]")
          .mouseout(function(event){fly.hideSubnav(this.id.split("_")[1], event);});

	}

	// show the subnavigation menu under the element
	this.showSubnav = function(id) {
		var tab = $("#portalNavigation_" + id).get(0);
		var x, y = 0;
		if (this.position == 'top') {
			var x = xoffset + tab.offsetLeft;
			var y = yoffset + tab.offsetTop + tab.offsetHeight;
		} else {
			var x = xoffset + tab.offsetLeft + tab.offsetWidth;
			var y = yoffset + tab.offsetTop;
		}
		// show the subnavigation div
		var div = $("#portalFlyoutNavigation_" + id)
			.css("left", x).css("top", y)
			.css("display", "block");
		// show the backing iframe to the size of the associated div
		var frame = $("#navFrame_" + id)
			.css("left", x).css("top", y)
			.css("width", div.get(0).offsetWidth + 'px')
			.css("height", div.get(0).offsetHeight + 'px')
			.css("display", "block");
	},
	
	// hide the subnavigation menu, but only if the user really
	// moused off it
	this.hideSubnav = function(id, e) {
		// check to see if the user really moused out of the nav menu
	    if (!e) var e = window.event;
	    var f = $((e.relatedTarget) ? e.relatedTarget : e.toElement);
	    if (f.is("[@id*=_" + id + "]") || f.parents("[@id*=_" + id + "]").length > 0) {
	    	return;
	    }
		else {
			this.closeSubnav(id);
		}
	},
	
	// close the subnavigation menu
	this.closeSubnav = function(id) {
		// get the tab and find and hide it's subnavigation
		$("#portalFlyoutNavigation_" + id).css("display", "none").end() // hide subnav container
		$("#navFrame_" + id).css("display", "none");   // hide backing iframe
	}
	
}
