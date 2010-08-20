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

        currentLayoutString = that.options.currentLayout.join("-");

        tree = { children: [ ] };
        $(that.options.allowedLayouts).each(function (idx, layout) {
            var layoutString = layout.layout.join("-");
            
            tree.children.push({
                ID: "layoutContainer:",
                children: [
                    {
                        ID: "layout",
                        decorators: [
                            { type: "addClass", classes: layoutString === currentLayoutString ? "selected" : "" }
                        ]
                    },
                    {
                        ID: "layoutLink",
                        decorators: [
                            { type: "jQuery", func: "click", 
                                args: function () {
                                    that.events.onLayoutSelect.fire(layout, that);
                                } 
                            }
                        ]
                    },
                    { ID: "layoutTitle", value: layout.layout.length + " Column" },
                    { ID: "layoutDescription", value: layout.name },
                    {
                        ID: "layoutImage", 
                        decorators: [
                            { type: "attrs", attributes: { src: that.options.imagePath + "layout_" + layoutString + ".gif" } }
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
            { id: "layoutImage", selector: that.options.selectors.layoutImage }
        ];
        
        that.render = function () {
            var tree = getComponentTree(that);
            if (that.templates) {
                fluid.reRender(that.templates, $(container), tree, { cutpoints: cutpoints });
            } else {
                that.templates = fluid.selfRender($(container), tree, { cutpoints: cutpoints });
            }
        };

        that.render();
        return that;
    };

    
    // defaults
    fluid.defaults("up.LayoutSelector", {
        currentLayout: [ 50, 50 ],
        allowedLayouts: [ 
            { name: "Full-width", layout: [ 100 ] },
            { name: "Narrow, wide", layout: [ 40, 60 ] },
            { name: "Even", layout: [ 50, 50 ] },
            { name: "Wide, narrow", layout: [ 60, 40 ] },
            { name: "Even", layout: [ 33, 34, 33 ] },
            { name: "Narrow, wide, narrow", layout: [ 25, 50, 25 ] },
            { name: "Even", layout: [ 25, 25, 25, 25 ] }
        ],
        imagePath: "test/",
        selectors: {
            layoutContainer: ".layout",
            layout: ".layout-wrapper",
            layoutLink: ".layout-link",
            layoutTitle: ".layout-titlebar",
            layoutDescription: ".layout-description",
            layoutImage: ".layout-image"
        },
        listeners: {
            onLayoutSelect: null
        }
    });
    
})(jQuery, fluid);
