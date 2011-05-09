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

    up.Autocomplete = function (container, options) {
        var that = fluid.initView("up.Autocomplete", container, options);
        that.state = that.state || {};

        var cutpoints = [
            { id: "match:", selector: that.options.selectors.match },
            { id: "matchLink", selector: that.options.selectors.matchLink },
            { id: "matchText", selector: that.options.selectors.matchText }
        ];

        that.search = function () {
            
            that.locate("dropdown").show();
            
            var results = that.options.searchFunction(that.locate("input").val());
            
            var tree = { children: [] };
            $(results).each(function (idx, result) {
                tree.children.push({
                    ID: "match:",
                    children: [
                        { 
                            ID: "matchLink",
                            decorators: [
                                { 
                                    type: "jQuery", 
                                    func: "click", 
                                    args: function() { 
                                        that.locate("input").val(result.text);
                                        that.state.currentValue = result.value;
                                        that.locate("dropdown").hide();
                                    } 
                                }
                            ]
                        },
                        { ID: "matchText", value: result.text }
                    ]
                });
            });
            
            if (that.state.templates) {
                fluid.reRender(that.state.templates, that.locate("matches"), tree, { cutpoints: cutpoints });
            } else {
                that.state.templates = fluid.selfRender(that.locate("matches"), tree, { cutpoints: cutpoints });
            }
            that.locate("loadingMessage").hide();
        };
        
        that.getValue = function() {
            return that.state.currentValue;
        };

        that.locate("close").click(function () { that.locate("dropdown").hide() });
        that.locate("input").keyup(that.search);
        
        // remove the initial instructional text when the input is focused
        that.locate("input").focus(function () {
            var text = that.locate("input").val();
            if (text == that.options.initialText) {
                that.locate("input").val("");
            }
        });

        // replace the initial instruction text if no option is selected
        that.locate("input").blur(function () {
            var text = that.locate("input").val();
            if (text == "") {
                that.locate("input").val(that.options.initialText);
            }
        });
                            
        return that;

    };

    fluid.defaults("up.Autocomplete", {
        initialText: "",
        searchFunction: null,
        selectors: {
            input: ".up-autocomplete-searchterm",
            dropdown: ".up-autocomplete-dropdown",
            close: ".up-autocomplete-close",
            noResultsMessage: ".up-autocomplete-noresults",
            matches: ".up-autocomplete-matches",
            match: ".up-autocomplete-match",
            matchLink: ".up-autocomplete-match-link",
            matchText: ".up-autocomplete-match-text",
            loadingMessage: ".up-autocomplete-loading"
        },
        events: {
            onSearch: null,
            onClose: null
        },
        listeners: {
            onSearch: null,
            onClose: null
        }
    });
    
})(jQuery, fluid);