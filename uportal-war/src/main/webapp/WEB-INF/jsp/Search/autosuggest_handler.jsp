<style type="text/css">
	.ui-autocomplete li {
		list-style: none;
	}

	.ui-autocomplete .ui-menu-item a:hover {
		color: #212121;
		cursor: pointer;
	}

	.ui-autocomplete a.ui-state-focus {
		border: 0;
	}

</style>
<script type="text/javascript">
<%--
	NOTE: The "j" variable is an over-ride to prevent a colission with the other versions of 
	      jQuery/jQueryUI on the page.

	      This is not an optimal solution and should be refactored at some point to make it 
	      less dependant on specific versions of jQuery/jQueryUI.
--%>

(function($, _) {
	var initSearchAuto = function() {

		var searchField = $('form input.searchInput');
		var actionUrl = searchField.closest('form').attr('action');
		var searchUrl = $('input.autocompleteUrl')[0].value;

		/**
		 * Helper method for making it a little easier to format the output in the menu
		 * @param  {object} item A JavaScript Object containing the item values
		 * @return {string}      Returns a formatted string that will be injected into the menu
		 */
		var formatOutput = function(item) {
			var output = "<a>" + item.label + "<br>" + item.desc + "</a>";
			return output;
		}

		searchField.autocomplete({
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

	$(document).ready(function() {
		initSearchAuto();
	});
})(searchJq, _);

</script>