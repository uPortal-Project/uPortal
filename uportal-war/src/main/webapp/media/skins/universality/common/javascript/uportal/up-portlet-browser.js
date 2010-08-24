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

    /**
     * Instantiate a PortletBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.DefaultPortletSearchView = function (container, overallThat, options) {
        var that, cutpoints, tree;
        
        // construct the new component
        that = fluid.initView("up.DefaultPortletSearchView", container, options);

        // initialize a state map for this component
        that.state = {};

        // define the renderer cutpoints based on the configured selectors
        cutpoints = [
            { id: "searchForm", selector: that.options.selectors.searchForm },
            { id: "searchInput", selector: that.options.selectors.searchInput }
        ];

        tree = { children: [
                { 
                    ID: "searchForm",  
                    decorators: [
                        { type: "jQuery", func: "submit", args: 
                            function () { 
                                overallThat.events.onPortletSearch.fire(overallThat, that.locate("searchInput").val(), true); 
                                return false;
                            } 
                        }
                    ] 
                },
                { 
                    ID: "searchInput",
                    decorators: [{ type: "jQuery", func: "keyup", args: 
                        function () { 
                            overallThat.events.onPortletSearch.fire(overallThat, $(this).val(), false);
                        }
                    }]
                }
            ] 
        };

        // render the component 
        that.state.templates = fluid.selfRender(that.locate("portletSearchView"), tree, { cutpoints: cutpoints });
        
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

    /**
     * Instantiate a PortletBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.PortletBrowser = function (container, options) {
        
        // construct the new component
        var that = fluid.initView("up.PortletBrowser", container, options);

        // initialize the portlet registry subcomponent
        that.registry = fluid.initSubcomponent(that, "portletRegistry", [container, fluid.COMPONENT_OPTIONS]);
        
        // initialize a state map for this component
        that.state = {};

        // initialize the view subcomponents
        that.categoryListView = fluid.initSubcomponent(that, "categoryListView", [container, that, fluid.COMPONENT_OPTIONS]);
        that.searchView = fluid.initSubcomponent(that, "searchView", [container, that, fluid.COMPONENT_OPTIONS]);
        that.portletListView = fluid.initSubcomponent(that, "portletListView", [container, that, fluid.COMPONENT_OPTIONS]);

        // indicate to the caller that the component has been successfully loaded
        that.events.onLoad.fire(that);
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.PortletBrowser", {
        portletRegistry: {
            type: "up.PortletRegistry",
            options: {
                portletListUrl: null
            }
        },
        searchView: {
            type: "up.DefaultPortletSearchView"
        },
        listeners: {
            onLoad: null,
            onCategorySelect: function (that, category) {
                that.state.currentCategory = category.id;
                that.categoryListView.refresh();
                that.portletListView.refresh();
            },
            onPortletSearch: function (that, searchTerm, submitted) {
                searchTerm = searchTerm.trim();
                that.state.portletRegex = searchTerm.length > 0 ? new RegExp(up.escapeSpecialChars(searchTerm), "i") : undefined;
                that.portletListView.refresh();
            },
            onPortletSelect: function (that, portlet) {
                that.portletListView.refresh();
            }
        }
    });
    
})(jQuery, fluid);
