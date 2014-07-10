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

/*
 * This file contains a set of results processor functions to process JSON results of pre-population or search as
 * you type ajax auto-suggest search results and prepare them for search auto-suggest rendering.
 */
var autoSuggestResultsProcessors = function (jQuery) {
    var $ = jQuery;

    var portletListProcessorFunc = function(categoriesArr, urlPattern) {
        var channels = $.map(categoriesArr.channels, function (channel, index) {
            return {
                label: channel.title,
                desc: channel.description === null ? '' : channel.description,
                url: urlPattern.replace('$fname', channel.fname)
            }
        });
        return channels.concat($.map(categoriesArr.categories, function(category, index) {
            return portletListProcessorFunc(category, urlPattern);
        }));
    };

    return {
        /* Default processor to handle auto-suggest queries to uPortal. Note that default is a reserved word in
         * Javascript so must be quoted. */
        "default": function (json, urlPattern) {
            return $.map(json, function (value, key) {
                return {
                    label: value.title,
                    desc: (value.description === null) ? '' : value.description,
                    url: value.url
                }
            });
        },
        /* Processor to handle /api/portletList JSON results */
        portletListProcessor: function (portletListJson, urlPattern) {
            return portletListProcessorFunc(portletListJson.registry, urlPattern);
        }
    }
};
