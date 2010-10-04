"use strict";
var up = up || {};

(function ($, fluid) {
    
    /**
     * This function handles the UI interactions that occur once a portlet thumbnail
     * or icon is dropped into the body of the portal.
     * 
     * @param {Object} that
     * @param {Object} action
     * @param {Object} target
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
            var columnContainer, column, target;
            
            //columnContainer = $(that.options.selectors.body).find(that.options.selectors.columnContainer);
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
                over: function (event, ui) {
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
                appendTo: "body",
                iframeFix: true,
                opacity: .80,
                helper: "clone",
                revert: "invalid",
                cursor: "move",
                cursorAt: {top: 7, left: 100 },
                stack: ".ui-draggable-dragging",
                tolerance: "intersect",
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
            loader: "#galleryLoader"
        },
        styles: {
            pseudoDropTarget: "layout-draggable-drop-target",
            targetDropped: "layout-draggable-target-dropped"
        },
        events: {
            onDropTarget: null
        },
        pseudoDropTargetLabel: "Drop Here"
    });
})(jQuery, fluid);