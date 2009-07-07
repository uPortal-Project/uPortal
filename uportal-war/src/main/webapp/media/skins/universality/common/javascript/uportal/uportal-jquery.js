/*
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
/*
	CONFIGURATION PARAMETERS
	

*/
(function($){
    // if the uPortal scope is not availalable, add it
    $.uportal = $.uportal || {};
    
    $.uportal.personLookup_searchResults = function(callerSettings) {
		var settings = $.extend({
		    moreInfoLinkVisibleText: null,
		    moreInfoLinkHiddenText: null,
		    moreInfoLinkSelector: null,
		    moreInfoSelector: null
	    }, callerSettings||{});
			
		
		//run the currently selected effect
	    function toggleMoreInfo(target) {
	        var moreInfo = target.siblings(settings.moreInfoSelector);

	        //Toggle info block
	        moreInfo.toggle();

	        //Toggle link text
	        if (moreInfo.is(':visible')) {
	            target.html(settings.moreInfoLinkVisibleText);
	        }
	        else {
	            target.html(settings.moreInfoLinkHiddenText);
	        }
	    };
	    
	    //set effect from select menu value
	    $(settings.moreInfoLinkSelector).click(function() {
	    	toggleMoreInfo($(this));
	        return false;
	    });

	    //Hide all the more-info divs
	    $(settings.moreInfoSelector).hide();
    };
})(jQuery);
