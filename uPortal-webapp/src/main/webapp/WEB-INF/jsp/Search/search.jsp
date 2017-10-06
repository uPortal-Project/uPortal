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

<template id="search-results-tab-header-template">
    <a class="mdl-tabs__tab"></a>
</template>

<template id="search-results-tab-panel-template">
    <div class="mdl-tabs__panel">
        <ul class="mdl-list"></ul>
    </div>
</template>

<template id="search-result-item-template">
    <li class="mdl-list__item mdl-list__item--three-line">
        <span class="mdl-list__item-primary-content">
            <i class="material-icons mdl-list__item-avatar"></i>
            <span class="up-list-item-title"></span>
            <span class="mdl-list__item-text-body">
                <dl></dl>
            </span>
        </span>
        <span class="mdl-list__item-secondary-content">
            <a class="mdl-list__item-secondary-action" href="#">
                <i class="material-icons">
                    open_in_browser
                </i>
            </a>
        </span>
    </li>
</template>

<template id="search-result-item-detail-template">
    <dt></dt>
    <dd></dd>
</template>

<!-- TODO: use existing material design lite from resource server -->
<script src="https://code.getmdl.io/1.3.0/material.min.js"></script>
<link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.indigo-pink.min.css">

<!-- TODO: move material design icons, lodash/lodash, github/fetch, webcomponents/template, and taylorhakes/promise-polyfill to resource server -->
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
<script src="https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.4/lodash.min.js"></script>
<script src="https://rawgit.com/github/fetch/v2.0.3/fetch.js"></script>
<script src="https://rawgit.com/webcomponents/template/v1.0.0/template.js"></script>
<script src="https://rawgit.com/taylorhakes/promise-polyfill/6.0.2/promise.js"></script>

<div id="search-results-tab-panel" class="mdl-tabs mdl-js-tabs mdl-js-ripple-effect">
    <div id="search-results-tab-header" class="mdl-tabs__tab-bar"></div>
</div>

<script language="javascript" type="text/javascript">
// fetch search results
// TODO: pull query from search param or jsp value
fetch('/uPortal/api/search?q=admin', {credentials: 'same-origin'})
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
    .then(function (response) {
        _.forEach(response, function (resultSet, tabProperty) {
            // generate formatted text
            var tabName = _.startCase(tabProperty);
            var tabId = _.kebabCase(tabProperty);

            // setup tab header template
            var tabTemplate = document.getElementById('search-results-tab-header-template');

            // add content to tab header template
            var tabHeaderLink = tabTemplate.content.querySelector('a');
            tabHeaderLink.textContent = tabName;
            tabHeaderLink.href = '#' + tabId;

            // add tab header to page
            var tabHeader = document.importNode(tabTemplate.content, true);
            document.getElementById('search-results-tab-header').appendChild(tabHeader);

            // setup the tab panel template
            var tabPanelTemplate = document.getElementById('search-results-tab-panel-template');
            // TODO set a default active panel
            var tabPanelResultList = tabPanelTemplate.content.querySelector('div').id = tabId;
            var tabPanelResultList = tabPanelTemplate.content.querySelector('ul');

            // add each result from the result set to the panel
            _.forEach(resultSet, function (result) {
                // setup search result item template
                var searchResultTemplate = document.getElementById('search-result-item-template');

                // add top level content
                searchResultTemplate.content.querySelector('.mdl-list__item-avatar').textContent = 'person'; // TODO: change icon based off type of result
                searchResultTemplate.content.querySelector('.up-list-item-title').textContent = 'Terrance Smith'; // TODO: real content
                // TODO: add destination link for result
                var resultAttributeList = searchResultTemplate.content.querySelector('dl');

                // TODO: pick only wanted attributes
                // QUESTION: should that attribute picking be handled server-side?
                // add each attribute that should be shown for a result
                _.forEach(result, function (attributeValue, attributeName) {
                    // setup attribute pairing template
                    var attributePairTemplate = document.getElementById('search-result-item-detail-template');

                    // add values
                    attributePairTemplate.content.querySelector('dt').textContent = _.startCase(attributeName);
                    attributePairTemplate.content.querySelector('dd').textContent = attributeValue;

                    // add attributes to the result
                    var attributePair = document.importNode(attributePairTemplate.content, true);
                    resultAttributeList.appendChild(attributePair);
                });

                // add result to panel
                var searchResult = document.importNode(searchResultTemplate.content, true);
                tabPanelResultList.appendChild(searchResult);
            });

            // add the tab panel to the page
            var tabPanel = document.importNode(tabPanelTemplate.content, true);
            document.getElementById('search-results-tab-panel').appendChild(tabPanel);
        });
    })
    // log error to browser console
    .catch(function(error) {
        console.log('request failed', error)
    });
</script>
