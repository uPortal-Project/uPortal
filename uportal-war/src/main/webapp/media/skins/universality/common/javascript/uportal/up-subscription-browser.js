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

(function($, fluid){
    
    var init = function(that, container) {
        var cutpoints = [
            { id: "~fragmentContainer:", selector: that.options.selectors.fragmentContainer },
            { id: "fragment", selector: that.options.selectors.fragment },
            { id: "fragmentTitle", selector: that.options.selectors.fragmentTitle },
            { id: "fragmentDescription", selector: that.options.selectors.fragmentDescription },
            { id: "selectFragmentLink", selector: that.options.selectors.selectFragmentLink }
        ];

        that.state = {};
        $.ajax({
            url: that.options.fragmentServiceUrl,
            async: false,
            success: function(data){
                that.state.fragments = data.fragments;
                $(that.state.fragments).each(function(idx, fragment){
                    that.state.fragments[fragment.owner] = fragment;
                });
                var tree = getComponentTree(that);
                that.state.templates = fluid.selfRender(container, tree, { cutpoints: cutpoints });
            }, 
            dataType: "json"
        });

    };
    
    var getComponentTree = function(that) {
        
        var tree = { children: [] };
        
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
     * Create a new Entity Selection component
     */
    uportal.FragmentBrowser = function(container, options) {
        var that = fluid.initView("uportal.FragmentBrowser", container, options);
        
        init(that, container);
        
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
