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
	        	$('#up-notification').noty({text: request.response, type: 'success'});
	        },
	        error: function(request, text, error) {
	        	$('#up-notification').noty({text: request.response, type: 'error'});
	        }
	    });
	};
	
	up.removeFromFavorite = function removeFromFavoritesFunction(event) {
		var portletId = event.data.portletId;
		var context = event.data.context;
		$.ajax({
	        url: context + "/api/layout?action=removeFavorite&channelId=" + portletId,
	        type: "POST",
	        data: null,
	        dataType: "json",
	        async: true,
	        success: function (request, text){
	        	$('#up-notification').noty({text: request.response, type: 'success'});
	        },
	        error: function(request, text, error) {
	        	$('#up-notification').noty({text: request.response, type: 'error'});
	        }
	    });
	};
})(jQuery);