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
