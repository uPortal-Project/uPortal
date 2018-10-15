<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<div class="container-fluid" id="sitemap-holder">
    <!--
     | Tab layout:
     | Tab1          Tab2          Tab3          Tab4
     |   -portlet1     -portlet5     -portlet7     -portlet8
     |   -portlet2     -portlet6                   -portlet9
     |   -portlet3                                 -portlet10
     |   -portlet4
     |
     | Tab5 ....
     +-->

    <a name="sitemap"></a>
</div>

<template id="sitemap-tab-row-template">
    <div class="row"></div>
</template>

<template id="sitemap-tab-template">
    <div class="up-sitemap-tab-header col-md-3">
        <h4>
            <a href=""></a>
        </h4>
        <ul></ul>
    </div>
</template>

<template id="sitemap-tab-portlet-template">
    <li><a href=""><span class="title"></span></a></li>
</template>

<script src="<rs:resourceURL value="/rs/lodash/4.17.4/lodash.min.js"/>"></script>

<%-- API URL for fetching layout details to build sitemap --%>
<c:set value="${renderRequest.contextPath}" var="portalContextPath" />
<c:url value="/api/v4-3/dlm/layout.json" var="layoutApiUrl" />

<%--
    UI strings for i18n
--%>
<spring:message var="i18n_error_loading_sitemap" code="error.loading.sitemap" />

<script language="javascript" type="text/javascript">
(function() { // Prevent adding to the global namespace

    // If a path check fails, it'll throw an error.
    function sitemapJsonCheck(jsonObj, pathChecks, errMsg) {
        return !_.every(pathChecks, function(pathCheck) {
            if(!_.has(jsonObj, pathCheck)) {
                console.log(errMsg + pathCheck);
                return false;
            }
            return true;
        });
    }

    fetch('${layoutApiUrl}', {credentials: 'same-origin'})
        // check for HTTP 2XX Okay response
        .then(function (response) {
            if (response.status >= 200 && response.status < 300) {
                return response;
            } else {
                var error = new Error(response.statusText);
                error.response = response;
                throw error;
            }
        })
        // extract json from response
        .then(function (response) {
            return response.json();
        })
        // Generate sitemap
        .then(function (response) {
            if (sitemapJsonCheck(response, ['layout.navigation.tabs'], "Missing required object path ")) {
                throw new Error("Missing 'layout.navigation.tabs' in the layout.");
            }

            // Begin tab row
            var tabRowTemplate = document.getElementById('sitemap-tab-row-template');
            var tabRow = document.importNode(tabRowTemplate.content, true).querySelector('div');
            _.forEach(response.layout.navigation.tabs, function (tab, tabIndex) {
                if (sitemapJsonCheck(tab, ['name','ID','content'], "Missing required object path [layout.navigation.tabs] > ")) {
                    return;
                }

                // Setup tab link
                var tabTemplate = document.getElementById('sitemap-tab-template');
                var tabHeader = document.importNode(tabTemplate.content, true).querySelector('div');
                // Add content to tab header template
                var tabHeaderLink = tabHeader.querySelector('a');
                tabHeaderLink.textContent = tab.name;
                tabHeaderLink.href = '${portalContextPath}/f/' + tab.ID + '/normal/render.uP';
                var portletList = tabHeader.querySelector('ul');

                _.forEach(tab.content, function (parentContent, parentContentIndex) {
                    if (sitemapJsonCheck(parentContent, ['content'], "Missing required object path [layout.navigation.tabs] > content > ")) {
                        return;
                    }

                    _.forEach(parentContent.content, function (portlet, portletIndex) {
                        if (sitemapJsonCheck(portlet, ['name', 'fname', 'ID'], "Missing required object path [layout.navigation.tabs] > content > content > ")) {
                            return;
                        }

                        // Setup portlet link
                        var portletTemplate = document.getElementById('sitemap-tab-portlet-template');
                        var portletListItem = document.importNode(portletTemplate.content, true).querySelector('li');
                        // Add content to portlet template
                        var portletTitle = portletListItem.querySelector('span');
                        portletTitle.textContent = portlet.name;
                        var portletLink = portletListItem.querySelector('a');
                        portletLink.href = '${portalContextPath}/f/' + tab.ID + '/p/' + portlet.fname + '.' + portlet.ID + '/max/render.uP';
                        // Add portlet to tab list
                        portletList.appendChild(portletListItem);
                    });
                });

                tabRow.appendChild(tabHeader);

                if(tabIndex === (response.layout.globals.tabsInTabGroup - 1)) {
                    // Add final tab row to page
                    document.getElementById('sitemap-holder').appendChild(tabRow);
                } else if (tabIndex % 4 === 3) { // Four per row
                    // Add tab row to page, and initialize a new tab row
                    document.getElementById('sitemap-holder').appendChild(tabRow);
                    tabRow = document.importNode(tabRowTemplate.content, true).querySelector('div');
                }
            });
        })
        // Let user know and log error to browser console
        .catch(function(error) {
            var errTemplate = document.getElementById('sitemap-tab-row-template');
            var errDiv = document.importNode(errTemplate.content, true).querySelector('div');
            errDiv.textContent = '${i18n_error_loading_sitemap}';
            document.getElementById('sitemap-holder').appendChild(errDiv);
            console.log('${i18n_error_loading_sitemap}', error);
        });

})();
</script>
