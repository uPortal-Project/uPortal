/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * This function contains a set of results processor functions to process JSON results of pre-population or search as
 * you type ajax auto-suggest search results and prepare them for search auto-suggest rendering.  The result is
 * an array of javascript objects containing properties:
 *
 * label: full text for auto-suggest filtering (for pre-sourced data auto-suggest searches)
 * title: auto-suggest result 1st line of item to display
 * desc: auto-suggest result 2nd line of item to display
 * url: url to go to when an auto-suggest item is chosen
 */
var autoSuggestResultsProcessors = function (jQuery) {
    var $ = jQuery;

    /* Processor to process the portletListAPI JSON response for use by auto-suggest.  This is expected to be used
     * for pre-populating auto-suggest source data.
     * categoriesArr - portletListAPI JSON response data
     * urlPattern - url pattern containing the string '$fname' which is replaced with the channel name
     */
    var portletListProcessorFunc = function(categoriesArr, urlPattern, uniqueItems) {
        uniqueItems = uniqueItems || {};
        var channels = $.map(categoriesArr.channels, function (channel, index) {
            // Keep an associative array of IDs we've found.  If we have already processed this portlet earlier in
            // the list, skip it so we don't have duplicates in the list.
            if (uniqueItems[channel.id]) {
                return null;
            } else {
                uniqueItems[channel.id] = channel.id;
            }
            return {
                label: channel.name + '~' + channel.title + '~' + channel.fname + '~' + channel.description,
                title: channel.title,
                desc: channel.description === null ? '' : channel.description,
                url: urlPattern.replace('$fname', channel.fname)
            }
        });
        return channels.concat($.map(categoriesArr.categories, function(category, index) {
            return portletListProcessorFunc(category, urlPattern, uniqueItems);
        }));
    };

    return {
        /* Default processor to handle auto-suggest queries to uPortal (queried, not pre-populated). Note that
         * default is a reserved word in Javascript so must be quoted.
         * json - auto-suggest query response JSON
         * urlPattern - Not used. Required for general API signature. The resulting data contains the URL to use.
         */
        "default": function (json, urlPattern) {
            return $.map(json, function (value, key) {
                return {
                    label: value.title + (value.description === null ? '' : ' ') + value.description,
                    title: value.title,
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

/*
 * This is the main auto-suggest search function.
 */
var initSearchAuto = initSearchAuto || function($, params) {
    var settings = $.extend({
        prepopulateAutoSuggestUrl: '',
        prepopulateUrlPattern: '',
        autoSuggestResultsProcessor: 'default'
    }, params);
    var searchFieldSelector = settings.searchFieldSelector;
    var prepopulateAutoSuggestUrl = settings.prepopulateAutoSuggestUrl;
    var prepopulateUrlPattern = settings.prepopulateUrlPattern;
    var autoSuggestResultsProcessor = autoSuggestResultsProcessors($)[settings.autoSuggestResultsProcessor];
    if(typeof autoSuggestResultsProcessor === "undefined") {
        alert ("Invalid autoSuggestResultsProcessor defined in portlet preferences. Must fix for auto-suggest search to work.")
    }
    var searchUrl = settings.autoSuggestSearchUrl;

    var searchField = $(searchFieldSelector);
    var actionUrl = searchField.closest('form').attr('action');

    /**
     * Helper method for making it a little easier to format the output in the menu
     * @param  {object} item A JavaScript Object containing the item values
     * @return {string}      Returns a formatted string that will be injected into the menu
     */
    var formatOutput = function(item) {
        var output = '<a><span class="autocomplete-header">' + htmlEscape(item.title) + '</span><br>' + htmlEscape(item.desc) + '</a>';
        return output;
    }

    // Simple function to escape specific HTML characters so they aren't a problem. &, quote, and single quote
    // are handled by jQuery so only need to deal with < and >
    function htmlEscape(str) {
        return String(str)
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');
    }

    searchField.autocomplete({
        position: { collision: "flip" },
        
        minLength: 3,
        source: function(request, response) {
            $.get( actionUrl, { query: request.term, ajax: "true" } )
            .done(function( data ) {
                /**
                 * Make a second AJAX request to get the actual values.
                 */
                $.getJSON( searchUrl, { query: request.term, ajax: true } )
                .done(function ( data ) {
                    response(autoSuggestResultsProcessors($)['default'](data.results, ''))
                })
            });
        },
        select: function( event, ui ) {
            window.location.href = ui.item.url;
            return false;
        }
    })
    .data( "ui-autocomplete" )
    ._renderItem = function( ul, item ) {
        return $( "<li>" )
        .append( formatOutput(item) )
        .appendTo( ul );
    };
    if (prepopulateAutoSuggestUrl.length > 0) {
        $.get(prepopulateAutoSuggestUrl)
        .done(function(data) {
            var autoCompleteData = autoSuggestResultsProcessor(data, prepopulateUrlPattern);
            searchField.autocomplete( "option", "source", autoCompleteData);
            searchField.autocomplete( "option", "minLength", 1);
            searchField.autocomplete( "option", "delay", 200);
        });
    }
};
