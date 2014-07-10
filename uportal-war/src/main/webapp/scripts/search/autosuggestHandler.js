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
    var searchUrl = settings.autoSuggestSearchUrl;

    var searchField = $(searchFieldSelector);
    var actionUrl = searchField.closest('form').attr('action');

    /**
     * Helper method for making it a little easier to format the output in the menu
     * @param  {object} item A JavaScript Object containing the item values
     * @return {string}      Returns a formatted string that will be injected into the menu
     */
    var formatOutput = function(item) {
        var output = '<a><span class="autocomplete-header">' + htmlEscape(item.label) + '</span><br>' + htmlEscape(item.desc) + '</a>';
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
                    response($.map(data.results, function (value, key) {
                        return {
                            label: value.title,
                            desc: (value.description === null) ? '' : value.description,
                            url: value.url
                        };
                    }));
                });
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
