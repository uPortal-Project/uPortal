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
"use strict";

// Wrapped in an IIFE to remove the global scope of the functions
(function () {
  // Function that captures a click on an outbound link in Analytics.
  const outboundClick = (event) => {
    if ((event === undefined) || (event === null)) {
      //console.log("Tried to process an outbound click, but there was no originating event");
      return;
    }

    // Both path and composedPath need to be checked due to browser support
    const anchorForEvent = (event.path || (event.composedPath && event.composedPath()))[0].closest('a');
    if ((anchorForEvent === undefined) || (anchorForEvent === null)) {
      //console.log("Tried to process an outbound click, but there was no originating event anchor");
      return;
    }

    if ((anchorForEvent.href === undefined) ||
        (
          anchorForEvent.href.startsWith('javascript')
        )) {
      //console.log("Not firing an analytic event due to href condition: " + eventDetails.href);
      //console.log(anchorForEvent);
      return;
    }

    const eventDetails = {
      type: 'link',
      href: anchorForEvent.href,
      target: anchorForEvent.target,
    }

    console.log("MILESTONE - would be capturing outbound link for: " + eventDetails.href);
    // mem leak - do not merge this!
    console.log(anchorForEvent);
    console.log(eventDetails);
    console.log(event);
    console.log(event.target);
  }

  const addPageLevelListeners = () => {
    document.addEventListener('click', outboundClick);
    document.addEventListener("beforeunload", (event) => {
      document.removeEventListener('click', outboundClick);
    });
  }

  window.onload = () => {
    console.log(
        'Setting up Portal Analytics on links');
    const observer = new MutationObserver(addPageLevelListeners);
    observer.observe(document.body, {attributeFilter: ["href"], childList: true, subtree: true});

    addPageLevelListeners();
  }
})();
