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

/**
 * The FragmentBrowser component provides a user interface for viewing and
 * selecting subscribable uPortal DLM fragments.  A list of available fragments
 * is collected from the configured AJAX target URL and parsed as JSON data.
 * This component expects the AJAX target to indicate which, if any, of the 
 * fragments are already included in the authenticated user's list of subscribed
 * content.
 * 
 * -------------------
 * Available selectors
 * -------------------
 * 
 * fragmentContainer
 *       Container for each individual fragment item (*not* included in the 
 *       final markup). This element is used as a top-level container with the 
 *       elision syntax to allow us to apply CSS classes to what becomes the 
 *       top-level HTML element for each fragment item.
 * fragment
 *       Container for each individual fragment item
 * fragmentTitle
 *       Fragment title
 * fragmentDescription
 *       Fragment description
 * selectFragmentLink 
 *       Link allowing a user to select the fragment. Will fire the 
 *       onFragmentSelect event.
 * 
 * ----------------
 * Available events
 * ----------------
 * 
 * onFragmentSelect 
 *     JSON representation of the selected fragment. Fired when 
 *     a user "selects" a fragment by clicking the select fragment link
 * 
 * ---------------------------
 * Other Configuration Options
 * ---------------------------
 * 
 * fragmentServiceUrl
 *     URL of the fragment listing AJAX service
 * 
 */
(function ($, fluid) {

    /**
     * Use the configured fragment service URL to collect an array of defined
     * fragments for the currently-authenticated user.  This fragment array
     * will be stored to the state-holding array of the provided component
     * instance.
     * 
     * @param that {FragmentBrowser} FragmentBrowser component instance
     */
    var findFragments = function (that) {
        // request the list of fragments from the server
        $.ajax({
            url: that.options.fragmentServiceUrl,
            async: false,
            success: function (data) {
                // add the reported fragments to the state array, indexing
                // each by fragment owner username to expedite future lookups
                that.state = that.state || {};
                that.state.fragments = data.fragments;
                $(that.state.fragments).each(function (idx, fragment) {
                    that.state.fragments[fragment.owner] = fragment;
                });
            }, 
            dataType: "json"
        });
    };
    
    /**
     * Instantiate a FragmentBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.FragmentBrowser = function (container, options) {
        
        // construct the new component
        var that = fluid.initView("up.FragmentBrowser", container, options);

        // initialize the cutpoints to match the configured selectors
        var cutpoints = [
            { id: "fragmentContainer:", selector: that.options.selectors.fragmentContainer },
            { id: "fragment", selector: that.options.selectors.fragment },
            { id: "fragmentTitle", selector: that.options.selectors.fragmentTitle },
            { id: "fragmentDescription", selector: that.options.selectors.fragmentDescription },
            { id: "selectFragmentLink", selector: that.options.selectors.selectFragmentLink }
        ];
        
        // collect the defined DLM fragments for the current user
        findFragments(that);
        
        that.refresh = function () {
            
            if (that.state.pager) {
                that.state.pager.refresh(that.state.fragments);
            } else {

                var columnDefs = [
                    { key: "fragment", valuebinding: "*.owner",
                        components: function (row) {
                            return { decorators: [
                                { type: "addClass", classes: row.subscribed ? "fragment-disabled" : "" }
                            ] };
                        }
                    },
                    { key: "fragmentTitle", valuebinding: "*.name" },
                    { key: "fragmentDescription", valuebinding: "*.description" },
                    { key: "selectFragmentLink", valuebinding: "*.owner",
                        components: function (row) {
                            return { decorators: [
                                { type: "jQuery", func: "click", args: function () {
                                        that.events.onFragmentSelect.fire(that, row);
                                    } 
                                }
                            ] };
                        }
                    }
                ];
                
                // set the other pager options
                var pagerOptions = {
                    dataModel: that.state.fragments,
                    annotateColumnRange: 'fragmentTitle',
                    columnDefs: columnDefs,
                    bodyRenderer: {
                        type: "fluid.pager.selfRender",
                        options: {
                            selectors: {
                                root: that.options.selectors.pagerRoot
                            },
                            row: "fragmentContainer:",
                            renderOptions: {
                                cutpoints: cutpoints
                            }
                        }
                    },
                    pagerBar: {
                        type: "fluid.pager.pagerBar", 
                        options: {
                            pageList: {
                                type: "fluid.pager.renderedPageList",
                                options: { 
                                    linkBody: "a"
                                }
                            }
                        }
                    }
                };
                
                that.state.pager = fluid.pager($(container).find(".package-results"), pagerOptions);
                that.state.pager.events.initiatePageSizeChange.fire(6);
            }
        };

        that.refresh();
        that.events.onLoad.fire(that);
        return that;
    };

    
    // defaults
    fluid.defaults("up.FragmentBrowser", {
        fragmentServiceUrl: null,
        selectors: {
            fragmentContainer: ".package",
            fragment: ".package-wrapper",
            fragmentTitle: ".package-titlebar",
            fragmentDescription: ".package-description",
            selectFragmentLink: ".package-link",
            pagerRoot: ".package-list"
        },
        events: {
            onFragmentSelect: null,
            onLoad: null
        },
        listeners: {
            onFragmentSelect: null,
            onLoad: null
        }
    });
    
})(jQuery, fluid);
