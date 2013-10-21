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

"use strict";
var up = up || {};

(function ($, fluid) {

    /**
     * Instantiate a PortletRegistry component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.EntityRegistry = function (container, options) {
        
        // construct the new component
        var that = fluid.initView("up.EntityRegistry", container, options);

        that.state = that.state || {};
    	that.state.entityCache = new Array();

        //--------------------------------------------------
        // PUBLIC METHODS
        //--------------------------------------------------
        
        that.searchEntities = function (entityTypes, searchTerm) {
    		var url, entities;
    		url= that.options.entitiesUrl + ".json";
    		
    		$.ajax({ async: false, url: url,
    			type: "GET", dataType: "json", data: { entityType: entityTypes, q: searchTerm },
    			success: function(json) {
    				entities = json.jsonEntityBeanList;
    			}
    		});
    		
    		return entities;
        };
        
        that.getEntity = function (entityType, entityId) {
        	var fullId, url, entity;
        	
        	// first check to see if this entity is already in the local cache
        	fullId = entityType + ":" + entityId;
    		if (that.state.entityCache[fullId]) {
    			entity = that.state.entityCache[fullId];
    		}
    		
    		// otherwise attempt to locate the entity on the server
    		url = that.options.entitiesUrl + "/" + entityType + "/" + entityId + ".json";
    		$.ajax({ async: false, url: url,
    			type: "GET", dataType: "json",
    			success: function(json) {
    				that.state.entityCache[fullId] = json.jsonEntityBean;
    				entity = json.jsonEntityBean;
    		    }
    		});

    		return entity;
        };

        return that;
    };

    
    // defaults
    fluid.defaults("up.EntityRegistry", {
        entitiesUrl: "/uPortal/api/entities"
    });
    
})(jQuery, fluid);