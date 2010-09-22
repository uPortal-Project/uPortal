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
 * The TabManger component houses the tab renaming functionality. This
 * component is a custom wrapper for the fluid simple text inline editor component.
 * The TabManger component is most likely a temporary solution as wrapping the
 * the fluid simple text inline editor component is somewhat overkill. As work continues
 * on enhancing uPortal's Tab management, the TabManger component will most likely
 * be renamed to house forthcoming tab management features.
 * 
 * For more information on planned tab management options, please see the below url:
 * https://wiki.jasig.org/display/UPC/Manage+Tabs
 * 
 * -------------------
 * Available selectors
 * -------------------
 * 
 * text
 *         :
 *         
 * edit
 *         :
 *         
 * ----------------
 * Available events
 * ----------------
 * 
 * afterFinishEdit
 *         :boolean
 *         :Fires just after the newly edited value is stored in the model.
 *         
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 * 
 * submitOnEnter
 *         :boolean
 *         :Causes the component to finish editing and commit changes to
 *          the component's model when the "Enter" button is clicked.
 *         
 * selectOnEdit
 *         :boolean
 *         :Automactically selects the editable text when the component
 *          switches into edit mode.
 *         
 */

"use strict";
var up = up || {};

(function ($, fluid) {
    
    /**
     * Initializes the fluid.inlineEditor component to be used with
     * the active portal tab, as long as the active tab is editable.
     * 
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var editTabHandler = function (that) {
        var edit, text, editorOptions;
        
        edit = that.locate("edit");
        if (edit.length > 0) {
            // Map default options to the fluid.inlineEdit component.
            editorOptions = that.options;
            
            // Initialize fluid.inlineEdit component.
            that.inlineEditor = fluid.inlineEdits(that.container, editorOptions);
            
            // Mouseenter event listener. Give focus to the
            // tab name on mouseenter. The mouseenter event
            // only triggers its handler on the element to which
            // it is bound.
            text = that.locate("text");
            text.bind("mouseenter", function () {
                text.css("cursor", "text");
                text.focus();
            });
            
            // Mouseleave event listener. Remove focus from
            // the tab name on mouseleave. The mouseleave event
            // only triggers its handler on the element to which
            // it is bound.
            text.bind("mouseleave", function () {
                text.css("cursor", "pointer");
                text.blur();
            });
        }//end:if.
    };//end:function.
    
    /**
     * Fires the onRemove event when the 'remove' icon is clicked. Passes a 
     * reference to the onRemove listener.
     * 
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var removeTabHandler = function (that) {
        var remove;
        remove = that.locate("remove");
        remove.bind("click", function () {
            that.events.onRemove.fire(this);
        });
    };//end: function.
    
    /**
     * Fires the onAdd event when the 'Add Tab' link is clicked. Passes new 
     * tab configurations to the onAdd listener.
     * 
     * @param {Object} that- reference to an instance of the TabManger component.
     */
    var addTabHandler = function (that) {
        var add;
        add = that.locate("add");
        add.bind("click", function () {
            that.events.onAdd.fire(that.options.addTabLabel, that.options.addTabWidths);
        });
    };//end: function.
    
    /**
     * Creator function for the TabManger component.
     * 
     * @param {Object} container - reference to HTML DOM element by ID.
     * @param {Object} options - reference to object containing all configurations.
     -------------------------------------------------------*/
    up.TabManager = function (container, options) {
        var that;
        that = fluid.initView("up.TabManager", container, options);
        
        editTabHandler(that);
        removeTabHandler(that);
        addTabHandler(that);
        return that;
    };//end:component.
    
    /**
     * Defaults function for the TabManger component.
     -------------------------------------------------------*/
    fluid.defaults("up.TabManager", {
        selectors: {
            text: ".flc-inlineEdit-text",
            edit: ".flc-inlineEditable",
            remove: ".portal-navigation-delete",
            add: ".portal-navigation-add"
        },
        events: {
            afterFinishEdit: null,
            onRemove: null,
            onAdd: null
        },
        addTabLabel: "My Tab",
        addTabWidths: [50, 50],
        submitOnEnter: true,
        selectOnEdit: true,
        lazyEditView: true
    });
})(jQuery, fluid);