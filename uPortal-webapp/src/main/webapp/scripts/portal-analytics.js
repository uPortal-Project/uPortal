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
'use strict';

// Wrapped in an IIFE to remove the global scope of the functions
(function () {
  // Function that captures a click on an outbound link in Analytics.
  var outboundClick = function (event) {
    if (event === undefined || event === null) {
      // Tried to process an outbound click, but there was no originating event
      return;
    }

    // Both path and composedPath need to be checked due to browser support
    var anchorForEvent = (event.path ||
      (event.composedPath && event.composedPath()))[0].closest('a');
    if (anchorForEvent === undefined || anchorForEvent === null) {
      // Tried to process an outbound click, but there was no originating event anchor
      return;
    }

    if (
      anchorForEvent.href === undefined ||
      anchorForEvent.href.startsWith('javascript')
    ) {
      // Not firing an analytic event due to href condition
      return;
    }

    const tab = document.querySelector('li.fl-tabs-active span');
    const portletWrapper = event.target.closest('section.up-portlet-wrapper');
    const portletTitleA = portletWrapper
      ? portletWrapper.querySelector('h2.portlet-title a')
      : null;

    var eventDetails = {
      type: 'link',
      url: anchorForEvent.href,
      tab_name: tab ? tab.textContent : null,
      portlet_id: portletWrapper ? portletWrapper.id : null,
      portlet_name: portletTitleA ? portletTitleA.title : null
    };

    fetch('/uPortal/api/analytics', {
      keepalive: true,
      method: 'POST',
      body: JSON.stringify(eventDetails),
      mode: 'cors',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'text/plain'
      }
    })
      .then(function (response) {
        console.log(response);
      })
      .catch(function (error) {
        console.log(error);
      });
  };

  var addPageLevelListeners = function () {
    document.addEventListener('click', outboundClick);
    document.addEventListener('beforeunload', function () {
      document.removeEventListener('click', outboundClick);
    });
  };

  window.addEventListener('load', function () {
    console.log('Setting up Portal Analytics on links');
    var observer = new MutationObserver(addPageLevelListeners);
    observer.observe(document.body, {
      attributeFilter: ['href'],
      childList: true,
      subtree: true
    });

    addPageLevelListeners();
  });
})();
