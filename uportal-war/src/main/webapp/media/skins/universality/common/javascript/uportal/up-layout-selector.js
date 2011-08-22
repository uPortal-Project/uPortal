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

"use strict";
var up = up || {};

(function ($, fluid) {
    
    var getComponentTree = function (that) {
        var currentLayoutString, tree;

        // construct a string representing the current layout
        currentLayoutString = that.options.currentLayout.join("-");

        tree = { children: [ ] };
        $(that.options.layouts).each(function (idx, layout) {
            var classes, layoutString;
            layoutString = layout.columns.join("-");
            classes = "";
            
            if (layout.columns.join("-") === currentLayoutString) {
                classes += "selected"
            }
            
            if (layout.disabled) {
                classes += " disabled";
            }
            
            tree.children.push({
                ID: "layoutContainer:",
                children: [
                    {
                        ID: "layout",
                        decorators: [
                            { type: "addClass", classes: classes }
                        ]
                    },
                    {
                        ID: "layoutLink",
                        decorators: [
                            { type: "jQuery", func: "click", 
                                args: function () {
                                    that.options.currentLayout = layout.columns;
                                    that.refresh();
                                    that.events.onLayoutSelect.fire(layout, that);
                                } 
                            }
                        ]
                    },
                    { ID: "layoutTitle", value: layout.columns.length + " " + that.options.strings[layout.columns.length == 1 ? 'column' : 'columns' ] },
                    { ID: "layoutDescription", value: that.options.strings[layout.nameKey] },
                    {
                        ID: "layoutThumb", 
                        decorators: [
                            {
                                type: "attrs",
                                attributes: {
                                    style: 'background: url(' + that.options.imagePath + 'layout_' + layoutString + '.gif' + ') top left no-repeat;'
                                }
                            }
                        ]
                    }
                ]
            });
            
        });
        
        return tree;
        
    };
    
    up.LayoutSelector = function (container, options) {
        var that, cutpoints;
        
        that = fluid.initView("up.LayoutSelector", container, options);
        
        cutpoints = [
            { id: "layoutContainer:", selector: that.options.selectors.layoutContainer },
            { id: "layout", selector: that.options.selectors.layout },
            { id: "layoutLink", selector: that.options.selectors.layoutLink },
            { id: "layoutTitle", selector: that.options.selectors.layoutTitle },
            { id: "layoutDescription", selector: that.options.selectors.layoutDescription },
            { id: "layoutThumb", selector: that.options.selectors.layoutThumb }
        ];
        
        /**
         * Refresh the rendered skin selector view
         */
        that.refresh = function () {
            var tree = getComponentTree(that);
            fluid.reRender(that.templates, $(container), tree, { cutpoints: cutpoints });
        };
        
        that.templates = fluid.selfRender($(container), getComponentTree(that), { cutpoints: cutpoints });
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.LayoutSelector", {
        currentLayout: [ 50, 50 ],
        strings: {
            fullWidth: "Full-width",
            narrowWide: "Narrow, wide",
            even: "Even",
            wideNarrow: "Wide, narrow",
            narrowWideNarrow: "Narrow, wide, narrow",
            column: "Column",
            columns: "Columns"
        },
        layouts: [ 
            { nameKey: "fullWidth", columns: [ 100 ] },
            { nameKey: "narrowWide", columns: [ 40, 60 ] },
            { nameKey: "even", columns: [ 50, 50 ] },
            { nameKey: "wideNarrow", columns: [ 60, 40 ] },
            { nameKey: "even", columns: [ 33, 34, 33 ] },
            { nameKey: "narrowWideNarrow", columns: [ 25, 50, 25 ] },
            { nameKey: "even", columns: [ 25, 25, 25, 25 ] }
        ],
        imagePath: "test/",
        selectors: {
            layoutContainer: ".layout",
            layout: ".layout-wrapper",
            layoutLink: ".layout-link",
            layoutTitle: ".layout-titlebar",
            layoutDescription: ".layout-description",
            layoutThumb: ".layout-thumb"
        },
        events: {
            onLayoutSelect: null
        },
        listeners: {
            onLayoutSelect: null
        }
    });
    
})(jQuery, fluid);
