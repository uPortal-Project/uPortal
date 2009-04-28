
(function($){

  $.groupbrowser = function(callerSettings) {
    var settings = $.extend({
      groupXmlUrl: "mvc/groupList"
    }, callerSettings||{});

	var that = this;
	
	// initialise a cache for retrieved entities
	var localCache = new Array();
	
	/**
	 * Retrieve a single entity.
	 */
	that.getEntity = function(entityTypes, entityId) {
		if (localCache[entityId] != null) return localCache[entityId];
		$.ajax({ async: false, url: settings.groupXmlUrl,
			type: "GET", dataType: "json", data: { entityType: entityTypes, entityId: entityId },
			success: function(json) {
				var entity;
				if (json.results.length > 0) {
					for (var i = 0; i < json.results.length; i++) {
						if (json.results[i].name != entityId) {
							entity = json.results[i];
							break;
						}
					}
				}
				if (entity == null || entity == undefined) {
					entity = json.results[0];
				}
				localCache[entity.id] = entity;
		    }
		});
		return localCache[entityId];
	};
	
	/**
	 * Search for all matching entities.
	 */
	that.searchEntities = function(entityTypes, searchTerm) {
		var results;
		$.ajax({ async: false, url: settings.groupXmlUrl,
			type: "GET", dataType: "json", data: { entityType: entityTypes, searchTerm: searchTerm },
			success: function(json) { 
				results = json.results;
			}
		});
		return results;
	};

	return that;
  };

})(jQuery);
