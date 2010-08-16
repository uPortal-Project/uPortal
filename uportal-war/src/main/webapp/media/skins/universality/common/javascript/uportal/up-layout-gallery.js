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

var up = up || {};

(function($, fluid){
    
    up.AjaxLayoutCategoryListView = function(container, overallThat, options) {

        // construct the new component
        var that = fluid.initView("up.AjaxLayoutCategoryListView", container, options);

        // initialize a state map for this component
        that.state = {};

        var cutpoints = [ 
            { id: "~categoryContainer:", selector: ".category-choice-container" },
            { id: "category", selector: ".category-choice" },
            { id: "categoryLink", selector: ".category-choice-link" },
            { id: "categoryName", selector: ".category-choice-name" }
        ];

        that.refresh = function() {
            // Build an array of all categories containing at least
            // one deep member, sorted by name
            var categories = [];
            categories.push({
                id: "",
                name: "All",
                description: "All Categories",
                categories: [],
                deepCategories: [],
                portlets: [],
                deepPortlets: []
            });
            $(overallThat.registry.getAllCategories()).each(function(idx, category){
                if (category.deepPortlets.length > 0 && category.id != "local.1") {
                    categories.push(category);
                }
            });
            categories.sort(up.getStringPropertySortFunction("name", "All"));

            var tree = { children: [] };

            var s = overallThat.state.currentCategory || "";

            $(categories).each(function(idx, category){
                tree.children.push({
                    ID: "categoryContainer:",
                    children: [
                        { ID: "category", decorators: [
                                { type: "addClass", classes: category.id == s ? "selected" : "" }
                            ] 
                        },
                        { ID: "categoryLink", decorators: [
                                { type: "jQuery", func: "click",
                                    args: function() {
                                        overallThat.events.onCategorySelect.fire(overallThat, category);
                                    }
                                }
                            ] 
                        },
                        { ID: "categoryName", value: category.name }
                    ]
                });
            });

            if (that.state.templates) {
                fluid.reRender(that.state.templates, $(container).find(".category-choice-list"), tree, { cutpoints: cutpoints });
            } else {
                that.state.templates = fluid.selfRender($(container).find(".category-choice-list"), tree, { cutpoints: cutpoints });
            }
            
        };

        that.refresh();
        return that;        
    };
    

    up.AjaxLayoutPortletListView = function(container, overallThat, options) {

        // construct the new component
        var that = fluid.initView("up.AjaxLayoutPortletListView", container, options);

        // initialize a state map for this component
        that.state = {};

        var cutpoints = [
            { id: "portlet:", selector: ".portlet-choice" },
            { id: "portletLink", selector: ".portlet-choice-link" },
            { id: "portletTitle", selector: ".portlet-choice-title" },
            { id: "portletDescription", selector: ".portlet-choice-description" },
            { id: "portletIcon", selector: ".portlet-choice-icon" }
        ];
        
        that.refresh = function() {

            var tree = { children: [] };

            // Build a list of all portlets that are a deep member of the
            // currently-selected category, sorted by title
            var portlets = [];
            var members = (overallThat.state.currentCategory && overallThat.state.currentCategory != "" ) ? overallThat.registry.getMemberPortlets(overallThat.state.currentCategory, true) : overallThat.registry.getAllPortlets();
            $(members).each(function(idx, portlet){
                if (!overallThat.state.portletRegex || overallThat.state.portletRegex.test(portlet.title) || overallThat.state.portletRegex.test(portlet.description)) {
                    for (var i = 0; i < portlet.parameters.length; i++) {
                        var parameter = portlet.parameters[i];
                        if (parameter.name == "iconUrl") {
                            console.log("found icon!");
                            portlet.iconUrl = parameter.value;
                            console.log(portlet);
                            break;
                        }
                    }
                    portlets.push(portlet);
                }
            });
            portlets.sort(up.getStringPropertySortFunction("title"));

            $(portlets).each(function(idx, portlet){
                var subtree = {
                    ID: "portlet:",
                    children: [
                        { ID: "portletLink", decorators: [
                                { type: "jQuery", func: "click",
                                    args: function() {
                                        overallThat.events.onPortletSelect.fire(overallThat, portlet);
                                    }
                                }
                            ] 
                        },
                        { ID: "portletTitle", value: portlet.title },
                        { ID: "portletDescription", value: portlet.description }
                    ]
                };
                if (portlet.iconUrl) {
                    subtree.children.push(
                        { ID: "portletIcon", decorators: [
                                { type: "attrs", attributes: { src: portlet.iconUrl } }
                            ]
                        }
                    );
                }
                tree.children.push(subtree);
            });
                
            if (that.state.templates) {
                fluid.reRender(that.state.templates, $(container).find(".portlet-choice-list"), tree, { cutpoints: cutpoints });
            } else {
                that.state.templates = fluid.selfRender($(container).find(".portlet-choice-list"), tree, { cutpoints: cutpoints });
            }
            
        };

        that.refresh();
        return that;
    };
    
})(jQuery, fluid);
