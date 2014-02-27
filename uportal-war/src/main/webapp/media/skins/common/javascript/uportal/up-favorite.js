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
	
	up.moveStuff = function moveStuffFunction(tabOrPortlet, item, context) {
	    var sourceID = item.getAttribute('sourceid');
        var destinationID = item.previousSibling.getAttribute != undefined ? item.previousSibling.getAttribute('sourceid') : item.nextSibling.getAttribute('sourceid');
        var method = item.previousSibling.getAttribute == undefined ? 'insertBefore' : 'appendAfter';
        
	    var theURL=context + "/api/layout?action=move" + tabOrPortlet
	                + "&sourceID=" + sourceID 
	                + "&elementID=" + destinationID 
	                + "&method=" + method;
		
		$.ajax({
	        url: theURL,
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