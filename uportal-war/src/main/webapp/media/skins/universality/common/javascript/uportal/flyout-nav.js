/*
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
// Revision: 2007-09-21 gthompson

(function($){
    // if the uPortal scope is not availalable, add it
    $.uportal = $.uportal || {};

    $.uportal.initFlyouts = function(flyout, options) {
        
        var defaults = {
            iframeId: '',
            divId: '',
            orientation: 'horizontal',
            horzalign: 'left',
            vertalign: 'bottom'
        };
        
        var options = $.extend(defaults, options || {});
        
        var container = flyout.parent();
        
        $(container).mouseover(function(){
            var tab = $(this);
            
            //Show first so calculations are accurate
            flyout.show();

            var foTop;
            var foLeft;
            if (options.orientation == 'horizontal') {
                if (options.horzalign == 'left') {
                    foLeft = 0;
                }
                else {
                    foLeft = tab.outerWidth() - flyout.outerWidth();
                }
                
                if (options.vertalign == 'bottom') {
                    foTop = tab.outerHeight();
                }
                else {
                    foTop = flyout.outerHeight() * -1;
                }
            }
            else {
                if (options.horzalign == 'left') {
                    foLeft = flyout.outerWidth() * -1;
                }
                else {
                    foLeft = tab.outerWidth();
                }
                
                if (options.vertalign == 'bottom') {
                    foTop = 0;
                }
                else {
                    foTop = (flyout.outerHeight() - tab.outerHeight()) * -1;
                }
            }
            
            
            flyout.css({
                top: foTop,
                left: foLeft
            }).bgiframe();
        });
        
        $(container).mouseout(function(event){
            $.uportal.closeFlyout(this, event, flyout);
        });
        
        $(flyout).mouseout(function(event){
            $.uportal.closeFlyout(this, event, flyout);
        });
        
        return;
    
    };

    $.uportal.closeFlyout = function(element, event, flyout) {
        // If the provided event is null use the window event
        if (!event) {
            event = window.event;
        }
            
        var eventSource = $((event.relatedTarget) ? event.relatedTarget : event.toElement);
        
        if (
            //if the event source is the flyout or container don't close
            eventSource.is("#" + $(element).attr("id")) || eventSource.is("#" + $(flyout).attr("id")) || 
            //if the event source is a child of the flyout or container don't close
            eventSource.parents("#" + $(element).attr("id")).length > 0 || eventSource.parents("#" + $(flyout).attr("id")).length > 0 
            ) {
            return;
        }
        
        // get the tab and find and hide it's subnavigation
        flyout.hide();
    };
    
    $.uportal.zIndexWorkaround = function() {
        if($.browser.msie) {
            // From http://richa.avasthi.name/blogs/tepumpkin/2008/01/11/ie7-lessons-learned/
            // Iterate over the parents of the flyout containers
            $("div.portal-flyout-container").parents().each(function() {
                var p = $(this);
                var pos = p.css("position");
    
                // If it's positioned,
                if(pos == "relative" || pos == "absolute" || pos == "fixed") {
                    /*
                     * Add the "on-top" class name when the mouse is hovering over it, and remove
                     * it when the mouse leaves.
                     */
                    p.hover(function() {
                            $(this).addClass("on-top");
                        },
                        function() {
                            $(this).removeClass("on-top");
                        }
                    );
                }
            });
            
            return;
        }
    }

})(jQuery);


function startFlyouts() {
    up.jQuery.uportal.zIndexWorkaround();

    var flyouts = up.jQuery("ul.fl-tabs li.portal-navigation div.portal-flyout-container");
    flyouts.each( function() {
            up.jQuery.uportal.initFlyouts(up.jQuery(this))
        }
    );
}
