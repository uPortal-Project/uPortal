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


(function($, _){
   /**
    * Finds the appropriate property configuration for the current institution
    */
   var findPropertyConfig = function() {
      if (up.analytics.model == null) {
         return null;
      }
      
      if (_.isArray(up.analytics.model.institutions)) {
         var propertyConfig = _.find(up.analytics.model.institutions, function(propertyConfig) {
            if (propertyConfig.name == up.analytics.institution) {
               return propertyConfig;
            }
         });
         
         if (propertyConfig != null) {
            return propertyConfig;
         }
      }
      
      return up.analytics.model.defaultConfig;
   };
   
   /**
    * Create the tracker with the propertyId and configuration from the
    * specified propertyConfig
    */
   var createTracker = function(propertyConfig) {
      var createSettings = {};
      _.each(propertyConfig.config, function(setting) {
         //Name isn't supported, we assume the default tracker is used
         if (setting.name != "name") {
            createSettings[setting.name] = setting.value;
         }
      });
      ga('create', propertyConfig.propertyId, createSettings);  // Create the tracker.
   };
   
   /**
    * Set the dimensions that apply to the current user
    */
   var setDimensions = function(propertyConfig) {
      var dimensions = {};
      _.each(propertyConfig.dimensionGroups, function(setting) {
         dimensions['dimension' + setting.name] = setting.value;
      });
      
      ga('set', dimensions); 
   };
   
   var getTabUri = function() {
      return '/tab/' + up.analytics.pageData.tab.fragmentName + '/' + up.analytics.pageData.tab.tabName;
   };
   
   var setPageVariables = function() {
      ga('set', {
         'page': getTabUri(),
         'title': 'Tab: ' + up.analytics.pageData.tab.tabName
      });
   };
   
   var getPortletUri = function(portletData) {
      //TODO need the title here, might need to include entity or window id in the portlet event
      return '/portlet/' + portletData.fname + '/' + portletData.fname;
   };
   
   var setPortletVariables = function(portletData) {
      ga('set', {
         'page': getPortletUri(portletData),
         //TODO need the title here, might need to include entity or window id in the portlet event
         'title': 'Portlet: ' + portletData.fname
      });
   };
   
   $(document).ready(function(){
      var propertyConfig = findPropertyConfig();

      //Create the tracker
      createTracker(propertyConfig);
      
      //Set Dimensions
      setDimensions(propertyConfig);

      //Page Event
      setPageVariables();
      ga('send', 'pageview');
      ga('send', 'timing', 'tab', getTabUri(), (up.analytics.pageData.executionTimeNano / 1000000));
      
      //Portlet Events
      _.each(up.analytics.portletData, function(portletData) {
         //TODO configure portlet analytics include/exclude list 
         if (portletData.fname == "google-analytics-config") {
            return;
         }
         
         setPortletVariables(portletData);
         ga('send', 'pageview');
         ga('send', 'timing', 'portlet', getPortletUri(portletData), (portletData.executionTimeNano / 1000000));
      });
      
   });
})(jQuery, _);
