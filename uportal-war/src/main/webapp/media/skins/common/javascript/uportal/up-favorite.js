var up = up || {};

(function($) {
	up.addToFavorite = function addToFavoritesFunction(event) {
		var portletId = event.data.portletId;
		var context = event.data.context;
		$.ajax({
	        url: context + "/api/layout?action=addFavorite&channelId=" + portletId,
	        type: "POST",
	        data: null,
	        dataType: "json",
	        async: true,
	        success: function (request, text){
	        	$('#up-notification').noty({text: request.response});
	        },
	        error: function(request, text, error) {
	        	$('#up-notification').noty({text: request.response});
	        }
	    });
	}
})(jQuery);