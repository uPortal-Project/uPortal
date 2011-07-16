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
     * DefaultPortletSearchView component provides a generic portlet searching
     * interface to serve as a view subcomponent of PortletBrowser.  This
     * default implementation makes use of a simple search form and executes
     * portlet search events as appropriate against the supplied parent component.
     *
     * ----------------
     * Available selectors
     * ----------------
     * 
     * portletSearchView
     *     overall container to be used while performing the Fluid rendering
     *     operation
     * searchForm
     *     portlet search form
     * searchInput
     *     portlet search input field.  Must be contained within the search form
     * 
     * ---------------------------
     * Other Configuration Options
     * ---------------------------
     * 
     * searchInvitationMessage
     *     default instructional message to be displayed within the search input
     *     field
     *       
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} parent PortletBrowser component (overall that)
     * @param {Object} options configuration options for the components
     */
    up.DefaultPortletSearchView = function (container, portletBrowser, options) {
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

        // generate the component tree
        tree = { children: [
                { 
                    ID: "searchForm",  
                    decorators: [
                        { type: "jQuery", func: "submit", args: 
                            function () { 
                                // Upon form submission, fire the portlet search
                                // event on the parent component
                                portletBrowser.events.onPortletSearch.fire(portletBrowser, that.locate("searchInput").val(), true); 
                                return false;
                            } 
                        }
                    ] 
                },
                { 
                    ID: "searchInput", 
                    value: that.options.searchInvitationMessage,
                    decorators: [
                        { type: "jQuery", func: "keyup", args: 
                            function () { 
                                // As the user updates the search field, fire
                                // the portlet search event on the parent
                                // component
                                portletBrowser.events.onPortletSearch.fire(portletBrowser, $(this).val(), false);
                            }
                        },
                        { type: "jQuery", func: "focus", args:
                            function () {
                                // When the search input field is focused, check
                                // if the input value matches the search 
                                // invitation message.  If it does, clear out
                                // the input field.
                                if ($(this).val() === that.options.searchInvitationMessage) {
                                    $(this).val("");
                                }
                            }
                        },
                        { type: "jQuery", func: "blur", args:
                            function () {
                                // When the search input field loses focus, check
                                // if the input value is empty.  If it is, set
                                // the value back to the search invitation message.
                                
                                var str;
                                str = $.trim($(this).val());
                                if (str === "") {
                                    $(this).val(that.options.searchInvitationMessage);
                                }
                            }
                        }
                    ]
                }
            ] 
        };

        // render the component 
        that.state.templates = fluid.selfRender(that.locate("portletSearchView"), tree, { cutpoints: cutpoints });
        
        return that;
    };
    
    // defaults
    fluid.defaults("up.DefaultPortletSearchView", {
        searchInvitationMessage: "Search for stuff",
        selectors: {
            portletSearchView: ".portlet-search-view",
            searchForm: ".portlet-search-form",
            searchInput: ".portlet-search-input"
        }
    });

    /**
     * PortletBrowser component provides a user interface for viewing and 
     * selecting registered uPortal portlets.  This portlet list is collected from
     * the configured portlet registry, and a number of view-type subcomponents
     * govern the browsing, search, and display behaviors of this component.  The
     * parent component itself delegates all markup generation to the view
     * subcomponents.
     *
     * ----------------
     * Available events
     * ----------------
     * 
     * onLoad
     *     optional action to be executed after component initialization
     * onCategorySelect
     *     called when a user selects a category
     * onPortletSearch
     *     called when a user performs a search action
     * onPortletSelect
     *     called when a user selects a portlet
     * 
     * ---------------------------
     * Other Configuration Options
     * ---------------------------
     * 
     * portletListView
     *       Displays the portlets themselves, potentially filtered by a search
     *       term or category
     * categoryListView
     *       Displays the portlet categories and allows the user to use them as 
     *       filters
     * searchView
     *       Displays a search form, allowing the user to search the portlet registry
     * portletRegistry
     *       PortletRegistry component
     *       
     *       
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.PortletBrowser = function (container, overallThat, options) {
        
        // construct the new component
        var that = fluid.initView("up.PortletBrowser", container, options);

        // initialize the portlet registry subcomponent
        that.options.portletRegistry.options = that.options.portletRegistry.options || {};
        that.options.portletRegistry.options.listeners = that.options.portletRegistry.options.listeners || {};
        that.options.portletRegistry.options.listeners.onLoad = function () {
            initializePortletBrowser(that, overallThat, container);
        };
        that.registry = fluid.initSubcomponent(that, "portletRegistry", [container, fluid.COMPONENT_OPTIONS]);
        
        // initialize a state map for this component
        return that;
    };
    
    var initializePortletBrowser = function (that, overallThat, container) {
        // initialize a state map for this component
        that.state = {};

        // initialize the view subcomponents
        that.categoryListView = fluid.initSubcomponent(that, "categoryListView", [container, that, fluid.COMPONENT_OPTIONS]);
        that.searchView = fluid.initSubcomponent(that, "searchView", [container, that, fluid.COMPONENT_OPTIONS]);
        that.portletListView = fluid.initSubcomponent(that, "portletListView", [container, that, fluid.COMPONENT_OPTIONS]);

        // indicate to the caller that the component has been successfully loaded
        that.events.onLoad.fire(that, overallThat);
    };
    
    // defaults
    fluid.defaults("up.PortletBrowser", {
        portletRegistry: {
            type: "up.PortletRegistry"
        },
        searchView: {
            type: "up.DefaultPortletSearchView"
        },
        events: {
            onLoad: null,
            onCategorySelect: null,
            onPortletSearch: null,
            onPortletSelect: null,
            onPortletDrag: null
        },
        listeners: {
            onLoad: null,
            onCategorySelect: function (that, category) {
                that.state.currentCategory = category.id;
                that.categoryListView.refresh();
                that.portletListView.refresh();
            },
            onPortletSearch: function (that, searchTerm, submitted) {
                searchTerm = $.trim(searchTerm);
                that.state.portletRegex = searchTerm.length > 0 ? new RegExp(up.escapeSpecialChars(searchTerm), "i") : undefined;
                that.portletListView.refresh();
                
            },
            onPortletSelect: function (that, portlet) {
                that.portletListView.refresh();
            },
            onPortletDrag: null
        }
    });
    
})(jQuery, fluid);
