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

var uportal = uportal || {};

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
(function($, fluid){

    /**
     * Use the configured fragment service URL to collect an array of defined
     * fragments for the currently-authenticated user.  This fragment array
     * will be stored to the state-holding array of the provided component
     * instance.
     * 
     * @param that {FragmentBrowser} FragmentBrowser component instance
     */
    var findFragments = function(that) {
        // request the list of fragments from the server
        $.ajax({
            url: that.options.fragmentServiceUrl,
            async: false,
            success: function(data){
                // add the reported fragments to the state array, indexing
                // each by fragment owner username to expedite future lookups
                that.state = that.state || {};
                that.state.fragments = data.fragments;
                $(that.state.fragments).each(function(idx, fragment){
                    that.state.fragments[fragment.owner] = fragment;
                });
            }, 
            dataType: "json"
        });
    };
    
    /**
     * Return a chunk of component tree markup for the specified component's
     * array of fragments.
     * 
     * @param that {FragmentBrowser} FragmentBrowser component instance
     */
    var getComponentTree = function(that) {
        
        // initialize the new component tree
        var tree = { children: [] };
        
        // for each listed fragment, add a new branch to the component tree
        $(that.state.fragments).each(function(idx, fragment){
            tree.children.push({ 
                ID: "fragmentContainer:",
                children: [
                    {
                        ID: "fragment",
                        decorators: [
                            { type: "addClass", classes: fragment.subscribed ? "fragment-disabled" : "" }
                        ]
                    },
                    { ID: "fragmentTitle", value: fragment.name },
                    { ID: "fragmentDescription", value: fragment.description },
                    { ID: "selectFragmentLink", 
                        decorators: [
                            { type: "jQuery", func: "click", 
                                args: function(){
                                    that.events.onFragmentSelect.fire(fragment);
                                }
                            }
                        ] 
                    }
                ]
            });
        });
        
        return tree;
    };

    /**
     * Instantiate a FragmentBrowser component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    uportal.FragmentBrowser = function(container, options) {
        
        // construct the new component
        var that = fluid.initView("uportal.FragmentBrowser", container, options);

        // initialize the cutpoints to match the configured selectors
        var cutpoints = [
            { id: "~fragmentContainer:", selector: that.options.selectors.fragmentContainer },
            { id: "fragment", selector: that.options.selectors.fragment },
            { id: "fragmentTitle", selector: that.options.selectors.fragmentTitle },
            { id: "fragmentDescription", selector: that.options.selectors.fragmentDescription },
            { id: "selectFragmentLink", selector: that.options.selectors.selectFragmentLink }
        ];
        
        // collect the defined DLM fragments for the current user
        findFragments(that);
        
        // get a chunk of component tree markup for the defined DLM fragment array
        var tree = getComponentTree(that);
        
        // render the component 
        that.state.templates = fluid.selfRender(container, tree, 
            { cutpoints: cutpoints });

        return that;
    };

    
    // defaults
    fluid.defaults("uportal.FragmentBrowser", {
        fragmentServiceUrl: null,
        selectors: {
            fragmentContainer: ".fragment-container",
            fragment: ".fragment-choice",
            fragmentTitle: ".fragment-title",
            fragmentDescription: ".fragment-description",
            selectFragmentLink: ".add-fragment-link"
        },
        listeners: {
            onFragmentSelect: null
        }
    });
    
})(jQuery, fluid);
