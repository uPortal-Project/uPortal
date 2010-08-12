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

    /**
     * Instantiate a PortletBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.DefaultPortletSearchView = function(container, overallThat, options) {
        
        // construct the new component
        var that = fluid.initView("up.DefaultPortletSearchView", container, options);

        // initialize a state map for this component
        that.state = {};

        // define the renderer cutpoints based on the configured selectors
        var cutpoints = [
            { id: "searchForm", selector: that.options.selectors.searchForm },
            { id: "searchInput", selector: that.options.selectors.searchInput }
        ];

        var tree = { children: [
                { ID: "searchForm",  
                    decorators: [
                        { type: "jQuery", func: "submit", args: 
                            function() { 
                                overallThat.events.onPortletSearch.fire(overallThat, that.locate("searchInput").val(), true); 
                                return false;
                            } 
                        }
                    ] 
                },
                { ID: "searchInput",
                    decorators: [{ type: "jQuery", func: "keyup", args: 
                        function(){ 
                            overallThat.events.onPortletSearch.fire(overallThat, $(this).val(), false);
                        }
                    }]
                }
            ] 
        };

        // render the component 
        that.state.templates = fluid.selfRender(that.locate("portletSearchView"), tree, { cutpoints: cutpoints });
        
        /**
         * 
         */
        that.refresh = function() {
            fluid.reRender(that.state.templates, that.locate("portletSearchView"), tree, { cutpoints: cutpoints });
        };

        return that;
    };

    
    // defaults
    fluid.defaults("up.DefaultPortletSearchView", {
        selectors: {
            portletSearchView: ".portlet-search-view",
            searchForm: ".portlet-search-form",
            searchInput: ".portlet-search-input"
        }
    });
    
    var getCategoriesComponentTree = function(overallThat, that) {
        // initialize the new component tree
        var tree = { children: [] };

        // Build an array of all categories containing at least
        // one deep member, sorted by name
        var categories = [];
        categories.push({
            id: "",
            name: that.options.allCategoriesName,
            description: "All Categories",
            categories: [],
            deepCategories: [],
            portlets: [],
            deepPortlets: []
        });
        $(overallThat.registry.getAllCategories()).each(function(idx, category){
            if (category.deepPortlets.length > 0 && that.options.excludedCategories.indexOf(category.id) < 0 ) {
                categories.push(category);
            }
        });
        categories.sort(up.getStringPropertySortFunction("name", that.options.allCategoriesName));
        
        // add each category to the component tree
        $(categories).each(function(idx, category){
            tree.children.push({ 
                ID: "category:",
                children: [
                    {
                        ID: "categoryLink",
                        decorators: [
                            { type: "jQuery", func: "click", 
                                args: function(){
                                    overallThat.events.onCategorySelect.fire(overallThat, category);
                                }
                            }
                        ]
                    },
                    { ID: "categoryTitle", value: category.name }
                ]
            });
        });

        return tree;
    }

    var getPortletsComponentTree = function(that) {
        // initialize the new component tree
        var tree = { children: [] };
        
        // Build a list of all portlets that are a deep member of the
        // currently-selected category, sorted by title
        var portlets = [];
        
        var members = (that.state.currentCategory && that.state.currentCategory != "") ? that.registry.getMemberPortlets(that.state.currentCategory, true) : that.registry.getAllPortlets();
        $(members).each(function(idx, portlet){
            if (!that.state.portletRegex || that.state.portletRegex.test(portlet.title) || that.state.portletRegex.test(portlet.description)) {
                portlets.push(portlet);
            }
        });
        portlets.sort(up.getStringPropertySortFunction("title"));

        // add each portlet to the component tree
        $(portlets).each(function(idx, portlet){
            tree.children.push({
                ID: "portlet:",
                children: [
                    {
                        ID: "portletLink",
                        decorators: [
                            { type: "jQuery", func: "click",
                                args: function(){
                                    that.events.onPortletSelect.fire(that, portlet);
                                }
                            }
                        ]
                    },
                    { ID: "portletTitle", value: portlet.title }
                ]
            });
        });

        return tree;
        
    }

    /**
     * Instantiate a PortletBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.PortletBrowser = function(container, options) {
        
        // construct the new component
        var that = fluid.initView("up.PortletBrowser", container, options);

        // initialize the portlet registry subcomponent
        that.registry = fluid.initSubcomponent(that, "portletRegistry", [container, fluid.COMPONENT_OPTIONS]);
        
        // initialize a state map for this component
        that.state = {};

        that.categoryListView = fluid.initSubcomponent(that, "categoryListView", [container, that, fluid.COMPONENT_OPTIONS]);

        that.searchView = fluid.initSubcomponent(that, "searchView", [container, that, fluid.COMPONENT_OPTIONS]);

        that.portletListView = fluid.initSubcomponent(that, "portletListView", [container, that, fluid.COMPONENT_OPTIONS]);

        that.events.onLoad.fire(that);
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.PortletBrowser", {
        portletRegistry: {
            type: "up.PortletRegistry",
            options: {
                portletListUrl: null,               
            }
        },
        searchView: {
            type: "up.DefaultPortletSearchView"
        },
        listeners: {
            onLoad: null,
            onCategorySelect: function(that, category) {
                that.state.currentCategory = category.id;
                that.categoryListView.refresh();
                that.portletListView.refresh();
            },
            onPortletSearch: function(that, searchTerm, submitted) {
                searchTerm = searchTerm.trim();
                that.state.portletRegex = searchTerm.length > 0 ? new RegExp(up.escapeSpecialChars(searchTerm), "i") : undefined;
                that.portletListView.refresh();
            },
            onPortletSelect: function(that, portlet) {
                that.portletListView.refresh();
            }
        }
    });
    
})(jQuery, fluid);
