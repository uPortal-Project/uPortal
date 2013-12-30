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

/**
 * The PortletRegistry component provides a JavaScript utility for 
 * requesting information about registered portlets and portlet 
 * categories.  
 *
 * Note: All provided information is constrained by the 
 * permissions of the currently-authenticated user, and is furthermore
 * constrained by the purpose for which the portlets are requested. For
 * example, the list of portlets returned by a request for manageable
 * portlets will differ from that returned by a request for subscribable
 * portlets.
 *
 * ---------------------------
 * Configuration Options
 * ---------------------------
 * 
 * portletListUrl
 *     URL of the portlet registry AJAX service
 * allCategoriesName
 *     Name of the pseudo "All Categories" category
 *
 */
(function ($, fluid) {

    /**
     * Construct a new portlet from the backend-provided JSON.
     */
    var Portlet = function (json) {
        return {
            id: json.id,
            description: json.description,
            fname: json.fname,
            title: json.title,
            name: json.name,
            state: json.state,
            type: json.typeId,
            iconUrl: json.iconUrl
        };
    };
    
    /**
     * Construct a new category from the backend-provided JSON.
     */
    var Category = function (json) {
        return {
            id: json.id,
            name: json.name,
            description: json.description,
            categories: [],
            deepCategories: [],
            portlets: [],
            deepPortlets: []
        };
    };

    var processCategory = function (that, category) {

        // If this category isn't already present in the array,
        // perform all necessary processing for it and its children,
        // then return the category
        if (!that.state.categories[category.id]) {
            
            var c = new Category(category);

            // Process each of the member categories.  First add each
            // category and its deep members to the list of deep members
            // for this category, then do the same for each of its deep
            // member portlets.
            $(category.categories).each(function (idx, subCategory) {
                subCategory = processCategory(that, subCategory);

                c.categories.push(subCategory);
                c.categories[subCategory.id] = c.categories[c.categories.length - 1];

                // add the subcategory to the deep categories array
                if (!c.deepCategories[subCategory.id]) {
                    c.deepCategories.push(subCategory);
                    c.deepCategories[subCategory.id] = c.deepCategories[c.deepCategories.length - 1];
                }
                
                // add all deep member categories of this subcategory
                // to the deep members array
                $(subCategory.deepCategories).each(function (idx, member) {
                    if (!c.deepCategories[member.id]) {
                        c.deepCategories.push(member);
                        c.deepCategories[member.id] = c.deepCategories[c.deepCategories.length - 1];
                    }
                });
                
                // add all deep member portlets of this subcategory to the
                // deep members array
                $(subCategory.deepPortlets).each(function (idx, member) {
                    if (!c.deepPortlets["portlet." + member.id]) {
                        c.deepPortlets.push(member);
                        c.deepPortlets["portlet." + member.id] = c.deepPortlets[c.deepPortlets.length - 1];
                    }
                });
            });
            
            // Process each of the direct member portlets.  First add
            // each portlet to the cached portlet list, then add it
            // to the list of deep members for this portlet category.
            $(category.channels).each(function (idx, json) {
                var portlet = new Portlet(json);
                
                c.portlets.push(portlet);
                c.portlets["portlet." + portlet.id] = c.portlets[c.portlets.length - 1];
                
                // add the portlet to the cached list
                if (!that.state.portlets["portlet." + portlet.id]) {
                    that.state.portlets.push(portlet);
                    that.state.portlets["portlet." + portlet.id] = that.state.portlets[that.state.portlets.length - 1];
                }
                // add it to the list of deep members
                if (!c.deepPortlets["portlet." + portlet.id]) {
                    c.deepPortlets.push(portlet);
                    c.deepPortlets["portlet." + portlet.id] = that.state.portlets[c.deepPortlets.length - 1];
                }
            });

            // add this category to the cached category list
            that.state.categories.push(c);
            that.state.categories[c.id] = that.state.categories[that.state.categories.length - 1];
            return c;
        } 

        // otherwise just return the already-cached category
        else {
            return that.state.categories[category.id];
        }
    };
    
    var getRegistry = function (that) {
        $.ajax({
            url: that.options.portletListUrl,
            success: function (data) {
                // add the reported fragments to the state array, indexing
                // each by fragment owner username to expedite future lookups
                that.state = that.state || {};
                that.state.portlets = [];
                that.state.categories = [];
                
                $(data.registry.categories).each(function (idx, category) {
                    processCategory(that, category);
                });
                
                //Deal with uncategorized channels
                if (data.registry.channels.length > 0) {
                    processCategory(that, {
                        id: "uncat",
                        name: "Uncategorized",
                        description: "Uncategorized Channels",
                        categories: [],
                        channels: data.registry.channels
                    });
                    
                }
                
                that.events.onLoad.fire();
            }, 
            dataType: "json"
        });
    };
    

    /**
     * Instantiate a PortletRegistry component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.PortletRegistry = function (container, options) {
        
        // construct the new component
        var that = fluid.initView("up.PortletRegistry", container, options);

        //--------------------------------------------------
        // PUBLIC METHODS
        //--------------------------------------------------

        /**
         * Refresh the cached category and portlet information by
         * making a fresh request to the backend portlet registry service.
         */
        that.refreshRegistry = function () {
            getRegistry(that);
        };

        /**
         * Return the portlet with the specified ID.
         * 
         * @param id {Number} portlet ID
         * @return {Object} portlet
         */
        that.getPortlet = function (id) {
            return that.state.portlets["portlet." + id];
        };
        
        /**
         * Return the category with the specified key.  This key should
         * be the fully qualified entity key of the format service.id.
         * 
         * @param key {String} category key
         */
        that.getCategory = function (key) {
            return that.state.categories[key];
        };
        
        /**
         * Return an array of all portlets belonging to the category
         * with the specified key.
         * 
         * @param key {String} category key
         * @param deepMembers {boolean} <code>true</code> to return all
         *          deep member portlets of the specified category, and
         *          <code>false</code> for solely the direct members
         */
        that.getMemberPortlets = function (key, deepMembers) {
            return deepMembers ? that.state.categories[key].deepPortlets : that.state.categories[key].portlets;
        };
        
        /**
         * Return an array of all categories belonging to the category
         * with the specified key.
         * 
         * @param key {String} category key
         * @param deepMembers {boolean} <code>true</code> to return all
         *           deep member categories of the specified category, and
         *           <code>false</code> for solely the direct members
         */
        that.getMemberCategories = function (key, deepMembers) {
            return deepMembers ? that.state.categories[key].deepCategories : that.state.categories[key].categories;
        };
        
        /**
         * Return an array of all categories retrieved for the current
         * portlet registry request.
         */
        that.getAllCategories = function () {
            return that.state.categories;
        };
        
        /**
         * Return an array of all portlets retrieved for the current
         * portlet registry request.
         */
        that.getAllPortlets = function () {
            return that.state.portlets;
        };
        
        
        // request the portlet registry from the back-end service and 
        // parse it as required by this component
        getRegistry(that);

        return that;
    };

    
    // defaults
    fluid.defaults("up.PortletRegistry", {
        portletListUrl: null,
        allCategoriesName: "All",
        events: {
            onLoad: null
        },
        listeners: {
            onLoad: null
        }
    });
    
})(jQuery, fluid);
