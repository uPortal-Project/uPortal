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

(function($, _) {
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
      _.each(propertyConfig.config || [], function(setting) {
         // Name isn't supported, we assume the default tracker is used in other
         // places
         if (setting.name != "name") {
            createSettings[setting.name] = setting.value;
         }
      });
      ga('create', propertyConfig.propertyId, createSettings); // Create the
   };

   /**
    * Set the dimensions that apply to the current user
    */
   var setDimensions = function(propertyConfig) {
      var dimensions = {};
      _.each(propertyConfig.dimensionGroups || [], function(setting) {
         dimensions['dimension' + setting.name] = setting.value;
      });

      ga('set', dimensions);
   };

   /**
    * Build the page URI for a tab
    */
   var getTabUri = function() {
      return '/tab/' + up.analytics.pageData.tab.fragmentName + '/' + up.analytics.pageData.tab.tabName;
   };

   /**
    * Set variables specific to the current page
    */
   var setPageVariables = function() {
      ga('set', {
         'page' : getTabUri(),
         'title' : 'Tab: ' + up.analytics.pageData.tab.tabName
      });
   };

   /**
    * Build the portlet URI for the specified portlet
    */
   var getPortletUri = function(portletData) {
      // TODO need the title here, might need to include entity or window id in
      // the portlet event
      return '/portlet/' + portletData.fname + '/' + portletData.fname;
   };

   /**
    * Set variables specific to the specified portlet
    */
   var setPortletVariables = function(portletData) {
      ga('set', {
         'page' : getPortletUri(portletData),
         // TODO need the title here, might need to include entity or window id
         // in the portlet event
         'title' : 'Portlet: ' + portletData.fname
      });
   };

   /**
    * Add click handlers to all of the flyout menus to fire flyout events when
    * they are used
    */
   var addFlyoutHandlers = function() {
      $("ul.fl-tabs li.portal-navigation a.portal-subnav-link").click(function(event) {
         setPageVariables();

         var clickedLink = $(this);

         // Find the target portlet's title
         var portletTitle = clickedLink.find("span.portal-subnav-label").text()

         // Find the target portlet's fname
         var classes = clickedLink.parent().attr("class").split(/\s+/);
         var fname;
         if (classes.length > 0) {
            fname = classes[classes.length - 1];
         } else {
            fname = portletTitle;
         }

         // Click will open in a new window if it is the middle button or the
         // meta or control keys are held
         var newWindow = event.button == 1 || event.metaKey || event.ctrlKey;

         var clickFunction;
         if (newWindow) {
            clickFunction = function() {
            };
         } else {
            clickFunction = function() {
               document.location = clickedLink.attr("href");
            };
         }

         // Send the event
         ga('send', {
            'hitType' : 'event',
            'eventCategory' : 'Flyout Link',
            'eventAction' : getPortletUri({
               fname : fname
            }),
            'eventLabel' : portletTitle,
            'hitCallback' : clickFunction
         });

         // If not opening a new window prevent the event and set the fallback
         // to make sure the page navigates even if the hitCallback
         // is never called
         if (!newWindow) {
            // Fallback in case hitCallback takes too long to get called, don't
            // want clicks to hang
            setTimeout(clickFunction, 200);

            event.preventDefault();
         }
      });
   };

   $(document).ready(function() {
      // Fail safe, if the analytics library isn't loaded make sure the function
      // exists
      window['ga'] = window['ga'] || function() {
      };

      var propertyConfig = findPropertyConfig();

      // No property config means nothing to do
      if (propertyConfig == null) {
         return;
      }

      // Create the tracker
      createTracker(propertyConfig);

      // Set Dimensions
      setDimensions(propertyConfig);

      // Page Event
      setPageVariables();
      ga('send', 'pageview');
      ga('send', 'timing', 'tab', getTabUri(), (up.analytics.pageData.executionTimeNano / 1000000));

      // Portlet Events
      _.each(up.analytics.portletData, function(portletData) {
         // TODO configure portlet analytics include/exclude list
         if (portletData.fname == "google-analytics-config") {
            return;
         }

         setPortletVariables(portletData);
         ga('send', 'pageview');
         ga('send', 'timing', 'portlet', getPortletUri(portletData), (portletData.executionTimeNano / 1000000));
      });

      // Add handlers to deal with click events on flyouts
      addFlyoutHandlers();
   });
})(jQuery, _);
