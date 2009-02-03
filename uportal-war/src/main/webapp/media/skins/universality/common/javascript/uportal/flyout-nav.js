// Revision: 2007-09-21 gthompson

// This flyout menu implementation relies on an iframe shim placed behind
// the div for each submenu.  This iframe prevents the submenu from 
// sliding behind form elements
(function($){
	
    // if the uPortal scope is not availalable, add it
    $.uportal = $.uportal || {};

	$.uportal.initFlyouts = function(container, options) {
		
		var defaults = {
			iframeId: '',
			divId: '',
			orientation: 'horizontal',
			xoffset: 0,
			yoffset: 0
		};
		
		var options = $.extend(defaults, options);
		
		$(container).mouseover(function(){
			var tab = $(this).get(0);
			var x = options.xoffset + tab.offsetLeft;
			var y = options.yoffset + tab.offsetTop;
			if (options.orientation == 'horizontal')
				y += tab.offsetHeight;
			else
				x += tab.offsetWidth;

			// show the subnavigation div
			var div = $("#" + options.divId)
				.css("left", x).css("top", y)
				.css("display", "block");
			// show the backing iframe to the size of the associated div
			var frame = $("#" + options.iframeId)
				.css("left", x).css("top", y)
				.css("width", div.get(0).offsetWidth + 'px')
				.css("height", div.get(0).offsetHeight + 'px')
				.css("display", "block");
		});
		
		$(container).mouseout(function(event){
			$.uportal.closeFlyout(this, event, options);
		});
		
		$("#" + options.divId).mouseout(function(event){
			$.uportal.closeFlyout(this, event, options);
		});
		
		return;
	
	};

	$.uportal.closeFlyout = function(element, event, options) {
		// check to see if the user really moused out of the nav menu
	    if (!event) var event = window.event;
	    var f = $((event.relatedTarget) ? event.relatedTarget : event.toElement);
	    if (f.is("#" + $(element).attr("id"))
	    		|| f.parents("#" + $(element).attr("id")).length > 0
	    		|| f.is("#" + options.divId) 
	    		|| f.parents("#" + options.divId).length > 0)
	    	return;
		// get the tab and find and hide it's subnavigation
		$("#" + options.divId).css("display", "none"); // hide subnav container
		$("#" + options.iframeId).css("display", "none");   // hide backing iframe				
	};

})(jQuery);


function startFlyouts() {

	// get the x and y offset of the navigation container
	var navcontainer = up("#portalNavigationList").get(0);
	var xoffset = navcontainer.offsetLeft; 
	var yoffset = navcontainer.offsetTop; 
	
	// create flyout menus for any tabs that have them
	up("#portalFlyoutNavigation").children("[id*=navFrame_]").each(function(){
		up.uportal.initFlyouts(up("#portalNavigation_" + up(this).attr("id").split("_")[1]),
			{
				iframeId: up(this).attr("id"), 
				divId: "portalFlyoutNavigation_" + up(this).attr("id").split("_")[1],
				xoffset: xoffset,
				yoffset: yoffset
			}
		);
	});
}
