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

/**
  * Function that captures a click on an outbound link in Analytics.
  */
  const outboundClick = (event) => {
    if((event === undefined) || (event === null)) {
      //console.log("Tried to process an outbound click, but there was no originating event");
      return;
    }

    // Both path and composedPath need to be checked due to browser support
    const anchorForEvent = (event.path || (event.composedPath && event.composedPath()))[0].closest('a');
    if((anchorForEvent === undefined) || (anchorForEvent === null)) {
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

    console.log("MILESTONE - would be capturing outbound link for: ");
    // mem leak - do not merge this!
    console.log(anchorForEvent);
    console.log(eventDetails);
    console.log(event);
    console.log(event.target);

// TODO - this will likely need to be rejigged. GA must still work when configured.
//    ga('send', 'event', 'outbound', 'click', title, {
//      'transport': 'beacon',
//      'hitCallback': () => {}
//    })
  }

  const addPageLevelListeners = () => {
    document.addEventListener('click', outboundClick);
    document.addEventListener("beforeunload", (event) => {
      document.removeEventListener('click', outboundClick);
    });
    //addAnchorListeners('page', document.links);
  }

  // TODO - review, this may not be needed anymore.
  const addWebComponentListeners = () => {
    const startTime = new Date().getTime();
    // TODO - add other web component names...
    const webComponentNames = ["esco-content-grid", "waffle-menu", "notification-icon"]

    webComponentNames.forEach(webComponentName => {
      // If there is at least one web component on the page with that name...
      if ((document.querySelector(webComponentName))) {
        // Supports multiple web components of the same name
        const webComponents = document.querySelectorAll(webComponentName);
        webComponents.forEach(webComponent => {
          webComponent.addEventListener('click', outboundClick);
          webComponent.addEventListener("beforeunload", (event) => {
            webComponent.removeEventListener('click', outboundClick);
          });
          //addAnchorListeners(webComponentName, webComponent.shadowRoot.querySelectorAll("a"));
        });
      } else {
        console.log(webComponentName + " - no links found")
      }
    })

    console.log("Added web component listeners in " + ((new Date().getTime())-startTime) + "ms");
  }

  // TODO - can remove once the .addEventListener flow is confirmed to work.
//  // handle: name of web component or 'page'
//  // links: Array of anchor elements
//  const addAnchorListeners = (handle, links) => {
//    console.log(handle + " - adding listeners with hrefs.");
//    [...links].filter(a => {
//      // Filter the format of links to capture analytics on, optionally based on the web component name
//      return (a.href !== undefined) &&
//        (
//         // a.href.startsWith('http') ||
//          !a.href.startsWith('javascript')
//        );
//    }).forEach(a => {
//      if (a.onclick == outboundClick) {
//        //console.log(handle + " > " + a.href + " already configured")
//      } else {
//        a.onclick = outboundClick
//        //console.log(handle + " > " + a.href + " configured")
//      }
//    })
//  }

  window.onload = () => {
    console.log(
        'Setting up Portal Analytics on links');
    const observer = new MutationObserver(addPageLevelListeners);
    observer.observe(document.body, {attributeFilter: ["href"], childList: true, subtree: true});

    addPageLevelListeners();

    // TODO - review if this is even needed... The shadow doms are picked up with just the page level listeners...
    //addWebComponentListeners();
    // Because web components can be loaded without a page load, check every 5 seconds
    //setInterval(addWebComponentListeners, 5000);
  }
