/*
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

(function($){

  $.groupbrowser = function(callerSettings) {
    var settings = $.extend({
      findEntityUrl: "mvc/findEntity",
      searchEntitiesUrl: "mvc/searchEntities"
    }, callerSettings||{});

	var that = this;
	
	// initialise a cache for retrieved entities
	var localCache = new Array();
	
	/**
	 * Retrieve a single entity.
	 */
	that.getEntity = function(entityType, entityId) {
		if (localCache[entityId] != null) return localCache[entityId];
		$.ajax({ async: false, url: settings.findEntityUrl,
			type: "POST", dataType: "json", data: { entityType: entityType, entityId: entityId },
			success: function(json) {
				var entity = json.result;
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
		$.ajax({ async: false, url: settings.searchEntitiesUrl,
			type: "POST", dataType: "json", data: { entityType: entityTypes, searchTerm: searchTerm },
			success: function(json) { 
				results = json.results;
			}
		});
		return results;
	};

	return that;
  };

})(jQuery);
