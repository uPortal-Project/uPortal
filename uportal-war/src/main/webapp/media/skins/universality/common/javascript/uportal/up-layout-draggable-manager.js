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

/**
 * The LayoutDraggableManager component houses the UI Gallery's initial drag 
 * and drop implementation. The implementation makes use of jQuery UI draggable
 * and droppable objects. As of this implementation, a user can drag a 'portlet'
 * thumbnail that appears in the gallery 'Add Stuff' tab and drag it to the top
 * of any available column. The ability to drag and drop a 'portlet' thumbnail
 * into a specific location does not yet exist.
 * 
 * For more information on the portal gallery, please see the below url:
 * https://wiki.jasig.org/display/UPC/Customize+Portal+Gallery
 * 
 * Some configurations listed below stem from jQuery UI droppable and draggable objects.
 * For more information, please see the below urls:
 * http://jqueryui.com/demos/droppable/
 * http://jqueryui.com/demos/draggable/
 * 
 * -------------------
 * Available selectors
 * -------------------
 * body: reference to portal's <body> element.
 * Default: Selector "#portal"
 * 
 * galleryList: reference to the list of portlets rendered by the portal gallery. 
 * Default: Selector "#galleryPortletList"
 * 
 * columnContainer: reference to outermost container to envelopes all portal columns. 
 * Default: Selector "#portalPageBodyColumns"
 * 
 * column: reference to portal column container.
 * Default: Selector ".portal-page-column"
 * 
 * innerColumn: reference to inner portal column container.
 * Default: Selector ".portal-page-column-inner"
 * 
 * dragHandle: reference to 'gripper' handle rendered out by the portal gallery. 
 * Default: Selector ".portlet-gripper"
 * 
 * pseudoDropTarget: reference to visual indicator that appears when a gallery list item is dragged
 * over a portal column.
 * Default: Selector ".layout-draggable-drop-target"
 * 
 * loader: reference to the gallery loading screen.
 * Default: Selector "#galleryLoader"
 * 
 * accept:  reference to acceptable draggable element. All draggables that match the selector will be accepted.
 * Default: Selector ".portlet"
 * 
 * ----------------
 * Available styles
 * ----------------
 * pseudoDropTarget: reference to a class name that is applied by default to the drop target <div>.
 * Default: String "layout-draggable-drop-target"
 * 
 * targetDropped: reference to a class name that is applied to the drop target <div> when an item has been dropped.
 * Default: String "layout-draggable-target-dropped"
 * 
 * ----------------
 * Available events
 * ----------------
 * onDropTarget: reference to an event that is triggered when a list item is successfully dropped.
 * Default: null
 * 
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 * pseudoDropTargetLabel: reference to label that appears within the drop target <div>.
 * Default: Drop Here
 * 
 * iframeFix: Prevent iframes from capturing the mousemove events during a drag.
 * Default: Boolean true
 * 
 * opacity: Opacity for the helper (avatar) while being dragged.
 * Default: Number .80
 * 
 * helper: Allows for a helper (avatar) element to be used for dragging display. Possible values: 'original', 'clone', Function.
 * Default: String "clone"
 * 
 * revert: If set to true, the element will return to its start position when dragging stops. Possible string values: 'valid', 'invalid'.
 * If set to invalid, revert will only occur if the draggable has not been dropped on a droppable. For valid, it's the other way around.
 * Default: Boolean, String "invalid"
 * 
 * cursor: The css cursor during the drag operation.
 * Default: String "move"
 * 
 * cursorAt: Moves the dragging helper so the cursor always appears to drag from the same position.
 * Default: Object {top: 7, left: 100 }
 * 
 * stack: Controls the z-Index of the set of elements that match the selector, always brings to front the dragged item.
 * Default: Selector ".ui-draggable-dragging"
 * 
 * tolerance: Specifies which mode to use for testing whether a draggable is 'over' a droppable. Possible values: 'fit', 'intersect', 'pointer', 'touch'. 
 * Default: String "intersect"
 * 
 */

"use strict";
var up = up || {};

(function ($, fluid) {
    
    /**
     * This function handles the UI interactions that occur once a portlet thumbnail
     * or icon is dropped into the body of the portal.
     * 
     * @param {Object} that - reference to LayoutDraggableManager instance.
     * @param {String} action - reference to jQuery UI droppable events (over, out & drop).
     * @param {Object} target - reference to a DOM element. Specifically, a portal column. 
     */
    var dropTargetHandler = function (that, action, target) {
        var column, columnID, method, placeholder;
        
        column = $(target);
        columnID = up.defaultNodeIdExtractor(target);
        method = "insertBefore";
        placeholder = column.find(that.options.selectors.pseudoDropTarget);
        
        switch (action) {
            case "over":
                placeholder.show();
            break;
            case "out":
                placeholder.hide().removeClass(that.options.styles.targetDropped);
            break;
            case "drop":
                that.elem.loader.css({
                    "margin" : "7px 0 0 0",
                    "opacity" : .70,
                    "background-color" : "#000"
                });
                up.showLoader(that.elem.loader);
                placeholder.addClass(that.options.styles.targetDropped);
                that.events.onDropTarget.fire(method, columnID);
            break;
        }//end:switch.
    };//end:function.
    
    /**
     * Initializes elem object that stores references to DOM containers that
     * are used through out the component.
     * 
     * @param {Object} that - reference to LayoutDraggableManager instance.
     */
    var initialize = function (that) {
        that.elem = {};
        that.elem.columnContainer = $(that.options.selectors.body).find(that.options.selectors.columnContainer);
        that.elem.pseudoDropTargetMarkUp = '<div class="' + that.options.styles.pseudoDropTarget + '"><span>' + that.options.pseudoDropTargetLabel +'</span></div>';
        that.elem.loader = $(that.options.selectors.loader);
    };//end:function.
    
    /**
     * Creator function for the LayoutDraggableManager component.
     * 
     * @param {Object} container - reference to HTML DOM element by ID.
     * @param {Object} options - reference to object containing all configurations.
     */
    up.LayoutDraggableManager = function (container, options) {
        var that;
        that = fluid.initView("up.LayoutDraggableManager", container, options);
        
        /**
         * This function inserts a <div> at the top each of the portal's columns.
         * The inserted <div> acts as a drop target.
         * 
         * @param {Object} that - reference to an instance of the LayoutDraggableManager component.
         */
        that.insertDropTarget = function () {
            var column, target;
            
            column = that.elem.columnContainer.find(that.options.selectors.innerColumn);
            target = column.find(that.options.selectors.pseudoDropTarget);
            
            // The number of columns should match the number of drop targets.
            // If they do not match, remove all instances of the target and prepend
            // new drop targets.
            if (column.length !== target.length) {
                // Remove targets.
                target.remove();
                
                // Add droppables & targets.
                column.prepend(that.elem.pseudoDropTargetMarkUp);
                that.makeDroppable(column);
            }//end:if.
        };//end:function.
        
        /**
         * This function takes a jQuery selector and applies the jQuery UI
         * droppable object to the passed selector. It also makes use of
         * droppable's over, out, and drop callback functions.
         * 
         * @param {Object} selector - reference to jQuery selector.
         * The passed selector should be a reference to a portal column.
         */
        that.makeDroppable = function (selector) {
            selector.droppable({
                accept: that.options.selectors.accept,
                over: function (event, ui) {
                    console.log(event, ui);
                    dropTargetHandler(that, "over", event.target);
                },
                out: function (event, ui) {
                    dropTargetHandler(that, "out", event.target);
                },
                drop: function(event, ui) {
                    dropTargetHandler(that, "drop", event.target);
                }
            });
        };//end:function.
        
        /**
         * This function takes a jQuery selector and applies the jQuery UI
         * draggable object to the passed selector.
         * 
         * @param {Object} selector - reference to jQuery selector.
         * The passed selector should be a reference to gallery list items.
         */
        that.makeDraggable = function (selector) {
            var dragHandle;
            
            dragHandle = selector.find(that.options.selectors.dragHandle)
            selector.draggable({
                handle: dragHandle,
                appendTo: that.options.selectors.body,
                iframeFix: that.options.iframeFix,
                opacity: that.options.opacity,
                helper: that.options.helper,
                revert: that.options.revert,
                cursor: that.options.cursor,
                cursorAt: that.options.cursorAt,
                stack: that.options.stack,
                tolerance: that.options.tolerance,
                containment: that.options.selectors.body,
                start: function (event, ui) {
                    that.insertDropTarget();
                }
            });
        };//end:function.
        
        /**
         * This function initializes drag and drop functionality.
         * It applies the jQuery UI draggable object to a collection
         * of list items passed over from a fluid.pager implementation.
         
         * @param {Object} newModel - reference to the updated state of the pager model.
         * @param {Object} oldModel - reference to the old state of the pager model.
         * @param {Object} pager - reference to an instance of the pager component.
         */
        that.initDragAndDrop = function (newModel, oldModel, pager) {
            var galleryList, listItem;
            
            // DOM Caching.
            galleryList = pager.container.find(that.options.selectors.galleryList);
            listItem = galleryList.find("li");
            
            // Set gallery items as draggable. Delay setting columns
            // as droppable until an item is dragged.
            that.makeDraggable(listItem);
        };//end:function.
        
        initialize(that);
        return that;
    };//end:component.
    
    /**
     * LayoutDraggableManager
     * Defaults function for the LayoutDroppables component.
     ---------------------------------*/
    fluid.defaults("up.LayoutDraggableManager", {
        selectors: {
            body: "#portal",
            galleryList: "#galleryPortletList",
            columnContainer: "#portalPageBodyColumns",
            column: ".portal-page-column",
            innerColumn: ".portal-page-column-inner",
            dragHandle: ".portlet-gripper",
            pseudoDropTarget: ".layout-draggable-drop-target",
            loader: "#galleryLoader",
            accept: ".portlet"
        },
        styles: {
            pseudoDropTarget: "layout-draggable-drop-target",
            targetDropped: "layout-draggable-target-dropped"
        },
        events: {
            onDropTarget: null
        },
        pseudoDropTargetLabel: "Drop Here",
        iframeFix: true,
        opacity: .80,
        helper: "clone",
        revert: "invalid",
        cursor: "move",
        cursorAt: {top: 7, left: 100 },
        stack: ".ui-draggable-dragging",
        tolerance: "intersect"
    });
})(jQuery, fluid);