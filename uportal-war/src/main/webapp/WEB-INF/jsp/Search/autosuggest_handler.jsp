<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<style type="text/css">
	.ui-autocomplete.ui-menu {
		padding: 0;
		overflow: hidden;
	}
	.ui-autocomplete li {
		list-style: none;
        max-width: 315px; /* Fixes Autocomplete on 1st search after page render would have very wide results box off side of window */
        /*max-height: 75px;*/ /* Limiting # of chars sent tends to keep description to 2 lines in autocomplete results so don't need max-height. */
        overflow:hidden;
	}

	.ui-autocomplete .ui-menu-item a {
		border-bottom: 1px solid #efefef;
		padding: .5em;
	}

	.ui-autocomplete .ui-menu-item a:hover {
		cursor: pointer;
	}
    .ui-autocomplete a.ui-state-focus .autocomplete-header {
		color: #fff;
	}

	.ui-autocomplete a.ui-state-focus {
		border: 0;
		background: rgb(62, 70, 79);
		color: #fff;
	}
	.ui-autocomplete a.ui-corner-all {
		-webkit-border-radius: 0 0 0 0;
		border-radius: 0 0 0 0;
	}
	.ui-autocomplete .autocomplete-header {
		font-weight: 800;
		color: rgb(123, 34, 64);
	}

</style>
<script type="text/javascript">

var initSearchAuto = initSearchAuto || function($, searchFieldSelector) {

    var searchField = $(searchFieldSelector);
    var actionUrl = searchField.closest('form').attr('action');
    var searchUrl = $('input.autocompleteUrl')[0].value;

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
};
</script>