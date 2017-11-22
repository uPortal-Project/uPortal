/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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

      if (_.isArray(up.analytics.model.hosts)) {
         var propertyConfig = _.find(up.analytics.model.hosts, function(propertyConfig) {
            if (propertyConfig.name == up.analytics.host) {
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
   var getTabUri = function(fragmentName, tabName) {
      if (up.analytics.pageData.tab != null) {
         fragmentName = fragmentName || up.analytics.pageData.tab.fragmentName;
         tabName = tabName || up.analytics.pageData.tab.tabName
      }
      
      var uri = '/';
      
      if (fragmentName != null) {
         uri += 'tab/' + fragmentName;
         
         if (tabName != null) {
            uri += '/' + tabName;
         }
      }
      
      return uri
   };

   /**
    * Set variables specific to the current page
    */
   var setPageVariables = function(fragmentName, tabName) {
      if (up.analytics.pageData.tab != null) {
         fragmentName = fragmentName || up.analytics.pageData.tab.fragmentName;
         tabName = tabName || up.analytics.pageData.tab.tabName
      }
      
      var title;
      if (tabName != null) {
         title = 'Tab: ' + tabName;
      }
      else if (up.analytics.pageData.urlState == null) {
         title = "Portal Home"
      }
      else {
         title = "No Tab"
      }
      
      ga('set', {
         'page' : getTabUri(fragmentName, tabName),
         'title' : title
      });
   };

   /**
    * Safe way to resolve the portlet's fname from the windowId, falls back to
    * just using the portlet's windowId as the value if no fname is found in the
    * portletData
    */
   var getPortletFname = function(windowId) {
      var portletData = up.analytics.portletData[windowId];
      if (portletDate == null) {
         return windowId;
      }

      return portletData.fname;
   };

   /**
    * Safe way to resolve the portlet's title from the windowId, falls back to
    * just using getPortletFname(windowId) if the title can't be found
    */
   var getRenderedPortletTitle = function(windowId) {
      var portletWindowWrapper = $("div.up-portlet-windowId-content-wrapper." + windowId);
      if (portletWindowWrapper == null) {
         return getPortletFname(windowId);
      }

      var portletWrapper = portletWindowWrapper.parents("div.up-portlet-wrapper-inner");
      if (portletWrapper == null) {
         return getPortletFname(windowId);
      }

      var portletTitle = portletWrapper.find("div.up-portlet-titlebar h2 a");
      if (portletTitle == null) {
         return getPortletFname(windowId);
      }

      return portletTitle.text().trim();
   };

   /**
    * Build the portlet URI for the specified portlet
    */
   var getPortletUri = function(fname) {
      return '/portlet/' + fname;
   };

   /**
    * Set variables specific to the specified portlet
    */
   var setPortletVariables = function(windowId, portletData) {
      var portletTitle = getRenderedPortletTitle(windowId);

      if (portletData == null) {
         portletData = up.analytics.portletData[windowId];
      }

      ga('set', {
         'page' : getPortletUri(portletData.fname),
         'title' : 'Portlet: ' + portletTitle
      });
   };
   
   /**
    * Finds a child element based on the selector and then return the first
    * class element that is not equal to excludedClasses or contained in the
    * excludedClasses array
    */
   var getInfoClass = function(selectorFunction, excludedClasses) {
      // Convert excludedClasses to an array for simpler code below
      if (!_.isArray(excludedClasses)) {
         excludedClasses = [excludedClasses];
      }
      
      var classAttr = selectorFunction().attr("class");
      if (classAttr == null) {
         return null;
      }

      var classes = classAttr.split(/\s+/);
      return _.find(classes, function(cls) {
         if (!_.contains(excludedClasses, cls)) {
            return cls;
         }
      });
   };

   /**
    * Determine the fname of the portle the clicked flyout was rendered for
    */
   var getFlyoutFname = function(clickedLink) {
      return getInfoClass(function() {
         return clickedLink.parents("div.up-portlet-fname-subnav-wrapper")
      }, "up-portlet-fname-subnav-wrapper");
   };

   /**
    * Determine the fname of the portle the clicked flyout was rendered for
    */
   var getExternaLinkWindowId = function(clickedLink) {
      return getInfoClass(function() {
         return clickedLink.parents("div.up-portlet-windowId-content-wrapper")
      }, "up-portlet-windowId-content-wrapper");
   };

   /**
    * Handler for sending an analytics event when a link is clicked and then
    * dealing with opening a new window or emulating the click
    */
   var handleLinkClickEvent = function(event, clickedLink, eventOpts) {
      // Click will open in a new window if it is the middle button or the
      // meta or control keys are held
      var newWindow = event.button == 1 || event.metaKey || event.ctrlKey || clickedLink.attr("target") != null;

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
      setPageVariables();
      ga('send', $.extend({
         'hitType' : 'event',
         'hitCallback' : clickFunction
      }, eventOpts));

      // If not opening a new window prevent the event and set the fallback
      // to make sure the page navigates even if the hitCallback
      // is never called
      if (!newWindow) {
         // Fallback in case hitCallback takes too long to get called, don't
         // want clicks to hang
         setTimeout(clickFunction, 200);

         event.preventDefault();
      }
   };

   /**
    * Add click handlers to all of the flyout menus to fire flyout events when
    * they are used
    */
   var addFlyoutHandlers = function() {
      $("ul.fl-tabs li.portal-navigation a.portal-subnav-link").click(function(event) {
         var clickedLink = $(this);

         // Find the target portlet's title
         var portletFlyoutTitle = clickedLink.find("span.portal-subnav-label").text();

         // Find the target portlet's fname
         var fname = getFlyoutFname(clickedLink);

         // Setup the page level state
         setPageVariables();

         // Send the event and deal with the click
         handleLinkClickEvent(event, clickedLink, {
            'eventCategory' : 'Flyout Link',
            'eventAction' : getPortletUri(fname),
            'eventLabel' : portletFlyoutTitle
         });
      });
   };

   /**
    * Inspects all clicks on links, any of the clicks that result in existing to
    * a different host are tracked as Outbound Link events
    */
   var addExternalLinkHandlers = function() {
      $('a').click(function(event) {
         var clickedLink = $(this);

         var linkHost = clickedLink.prop("hostname");
         if (linkHost != "" && linkHost != document.domain) {
            var windowId = getExternaLinkWindowId(clickedLink);
            if (windowId != null) {
               setPortletVariables(windowId);
            } else {
               setPageVariables();
            }

            // Send the event and deal with the click
            handleLinkClickEvent(event, clickedLink, {
               'eventCategory' : 'Outbound Link',
               'eventAction' : clickedLink.prop("href"),
               'eventLabel' : clickedLink.text()
            });
         }
      });
   };
   
   var addMobileListTabHandlers = function() {
      $('ul.up-portal-nav li.up-tab').click(function(event) {
         var clickedTab = $(this);
         
         //Ignore clicks on already open tabs
         if (clickedTab.hasClass("up-tab-open")) {
            return;
         }
         
         var fragmentName = getInfoClass(function() {
               return clickedTab.find("div.up-tab-owner");
            }, "up-tab-owner");

         var tabName = clickedTab.find("span.up-tab-name").text().trim();
         
         setPageVariables(fragmentName, tabName);
         ga('send', 'pageview');
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
      // Don't bother sending the view in MAX WindowState
      if (up.analytics.pageData.urlState != "MAX") {
         ga('send', 'pageview');
      }
      ga('send', {
        'hitType': 'timing',
        'timingCategory': 'tab',
        'timingVar': getTabUri(),
        'timingValue': (up.analytics.pageData.executionTimeNano / 1000000)
      });

      // Portlet Events
      _.each(up.analytics.portletData, function(portletData, windowId) {
         // TODO configure portlet analytics include/exclude list
         if (portletData.fname == "google-analytics-config") {
            return;
         }

         setPortletVariables(windowId, portletData);
         ga('send', 'pageview');
         ga('send', {
           'hitType': 'timing',
           'timingCategory': 'portlet',
           'timingVar': getPortletUri(portletData.fname),
           'timingValue':(portletData.executionTimeNano / 1000000)
         });
      });

      // Add handlers to deal with click events on flyouts
      addFlyoutHandlers();

      // Add handlers to deal with click events on external links
      addExternalLinkHandlers();
      
      // Add handlers to deal with "tab" clicks on the mobile accordian view
      addMobileListTabHandlers();
   });
})(jQuery, _);
