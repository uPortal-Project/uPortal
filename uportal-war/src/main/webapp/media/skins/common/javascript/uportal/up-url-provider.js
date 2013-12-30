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

var uportal = uportal || {};


(function($, fluid){

    /**
     * Instantiate a LayoutPersistence component
     * 
     * @param {Object} component Container the element containing the fragment browser
     * @param {Object} options configuration options for the components
     */
    up.UrlProvider = function(container, options) {
        
        // construct the new component
        var that = fluid.initView("up.UrlProvider", container, options);

        that.getPortletUrl = function (fname) {
            return that.options.portalContext + "/p/" + fname;
        };
        
        that.getTabUrl = function (tabId) {
            return that.options.portalContext + "/f/" + tabId;
        };
        
        that.getPortalHomeUrl = function () {
            return that.options.portalContext + "/";
        };
        
        return that;
    };

    
    // defaults
    fluid.defaults("up.UrlProvider", {
        portalContext: "/uPortal"
    });
    
})(jQuery, fluid);
