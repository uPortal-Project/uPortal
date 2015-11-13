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
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<%-- The action URLs, including the ajaxSearchUrl, should have windowState=maximized or uPortal will waste time doing an
     HTTP redirect to set it to maximized. --%>
<portlet:actionURL var="launchUrl" windowState="maximized"></portlet:actionURL>
<portlet:actionURL var="ajaxSearchUrl" windowState="maximized"/>
<portlet:resourceURL var="ajaxResults" id="retrieveSearchJSONResults"/>

<%-- Do not namespace webSearchContainer or webSearchSubmit. They are referenced in less files. You can change to
     class names, but that may impact users who have customized their less files. --%>
<div id="webSearchContainer" class="fl-widget">
    <div class="fl-widget-inner">
      <div class="fl-widget-content">
        <c:set var="searchLabel"><spring:message code="search"/></c:set>
        <form class="form-search" role="form" method="post" action="${searchLaunchUrl}" id="${n}webSearchForm">
          <div class="input-group">
            <input id="${n}webSearchInput"  class="searchInput input-large search-query form-control" value="" name="query" type="text" placeholder="Enter search terms"/>
            <span class="input-group-btn">
              <button id="webSearchSubmit" type="submit" name="submit" class="btn btn-default" value="${searchLabel}">
                <span>${searchLabel}</span><i class="fa fa-search"></i></button>
            </span>
          </div>
        </form>
      </div>
    </div>
</div>

<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.10.2/jquery-1.10.2.min.js"/>"></script>
<script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.10.3/jquery-ui-1.10.3.min.js"/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/search/autosuggestHandler.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/search/autosuggest.css">

<script language="javascript" type="text/javascript"><rs:compressJs>
/*
 * Switch jQuery to extreme noConflict mode, keeping a reference to it in the searchjQ["${n}"] namespace
 */
var searchjQ = searchjQ || {};
searchjQ["${n}"] = searchjQ["${n}"] || {};
searchjQ["${n}"].jQuery = jQuery.noConflict(true);

<%-- Only set prepopulateAutoSuggestUrl if the portlet preference is not empty. --%>
<c:if test="${not empty portletPreferencesValues['prepopulateAutoSuggestUrl'][0]}">
    <c:set var="prepopulateAutoSuggestUrl" value="${pageContext.request.contextPath}${portletPreferencesValues['prepopulateAutoSuggestUrl'][0]}"/>
</c:if>

searchjQ["${n}"].jQuery(document).ready(function() {
    initSearchAuto(searchjQ["${n}"].jQuery, {
        searchFieldSelector: "#${n}webSearchInput",
        prepopulateAutoSuggestUrl: "${prepopulateAutoSuggestUrl}",
        prepopulateUrlPattern: "${pageContext.request.contextPath}${portletPreferencesValues['prepopulateUrlPattern'][0]}",
        autoSuggestResultsProcessor: "${portletPreferencesValues['autoSuggestResultsProcessor'][0]}",
        autoSuggestSearchUrl: "${autocompleteUrl}"
    });
});

// Only search if the user entered some text to search for
searchjQ["${n}"].jQuery( "#${n}webSearchForm" ).submit(function( event ) {
    if ( searchjQ["${n}"].jQuery( "#${n}webSearchInput" ).val().trim().length == 0 ) {
        event.preventDefault();
    } else {
        document.getElementById('webSearchSubmit').disabled = 1;
    }

});

</rs:compressJs></script>
