/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var uportal = uportal || {};

(function($, fluid){
    
    /**
     * Initialize the flyout menu item
     */
    var init = function(that) {
        
        zIndexWorkaround(that);
        
        // set the mouseover event
        $(that.container).mouseover(function(){
            
            //Show first so calculations are accurate
            that.openFlyout();
            
            var tab = $(this);
            var flyout = that.locate('flyoutMenu');
            
            var foTop;
            var foLeft;
            if (that.options.orientation == 'horizontal') {
                if (that.options.horzalign == 'left') {
                    foLeft = 0;
                }
                else {
                    foLeft = tab.outerWidth() - flyout.outerWidth();
                }
                
                if (that.options.vertalign == 'bottom') {
                    foTop = tab.outerHeight();
                }
                else {
                    foTop = flyout.outerHeight() * -1;
                }
            }
            else {
                if (that.options.horzalign == 'left') {
                    foLeft = flyout.outerWidth() * -1;
                }
                else {
                    foLeft = tab.outerWidth();
                }
                
                if (that.options.vertalign == 'bottom') {
                    foTop = 0;
                }
                else {
                    foTop = (flyout.outerHeight() - tab.outerHeight()) * -1;
                }
            }

            // set the mouseout event
            $(that.container).mouseout(function(){
                that.closeFlyout();
            });
            
            // use the bgiframe plugin to ensure flyouts appear on top of
            // form elements in earlier versions of IE
            flyout.css({
                top: foTop,
                left: foLeft
            }).bgiframe();
        });

    };
    
    /**
     * Provide fix for z-index layering issues in older IE browsers
     * 
     * From http://richa.avasthi.name/blogs/tepumpkin/2008/01/11/ie7-lessons-learned/
     */
    var zIndexWorkaround = function(that) {
        if($.browser.msie) {
            // Iterate over the parents of the flyout containers
            that.locate('flyoutMenu').parents().each(function() {
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
    };


    /**
     * Create a new flyout menu component
     */
    uportal.flyoutmenu = function(container, options) {
        var that = fluid.initView("uportal.flyoutmenu", container, options);
        
        // initialize the flyout menu
        init(that);
        
        that.openFlyout = function() {
            that.locate('flyoutMenu').show();
        };
        
        that.closeFlyout = function() {
            that.locate('flyoutMenu').hide();
        };

    };

    // defaults
    fluid.defaults("uportal.flyoutmenu", {
        orientation: 'horizontal',
        horzalign: 'left',
        vertalign: 'bottom',
        selectors: {
            flyoutMenu: '.portal-flyout-container'
        }
    });
    
})(jQuery, fluid);