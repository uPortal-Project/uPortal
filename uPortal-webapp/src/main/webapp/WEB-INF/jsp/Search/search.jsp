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
<%@ taglib uri="/WEB-INF/tag/portletUrl.tld" prefix="pURL" %>
<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="formUrl"/>
<portlet:resourceURL var="autocompleteUrl" id="retrieveSearchJSONResults"/>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="portlet search-portlet" role="section">

    <!-- Portlet Titlebar
        <div class="titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="search"/></h2>
    </div>
    -->

    <!-- Portlet Body -->
    <div class="portlet-body" role="main">

        <!-- Portlet Section -->
        <div id="${n}search" class="portlet-section" role="region">

            <div class="portlet-section-body">

                <form id="${n}searchForm" action="${ formUrl }" class="form-inline" style="margin-bottom:10px;" method="POST">
                    <div class="form-group">
                        <input id="${n}searchInput" type="search" class="searchInput form-control" name="query" value="${ fn:escapeXml(query )}" aria-label="<spring:message code="search"/>"/>
                        <input id="${n}searchButton" type="submit" class="btn btn-default" value="<spring:message code="search.submit"/>"/>
                    </div>
                </form>

                <c:if test="${hitMaxQueries}">
                    <div>
                        <spring:message code="search.rate.limit.reached"/>
                    </div>
                </c:if>

                <c:choose>

                <c:when test="${not empty results}">

                    <div class="portlet-section" role="region">

                        <div class="content">
                            <div id="${n}searchResults" class="hidden">
                                <ul class="searchTabsContainer">
                                    <li><a href="#${n}_DEFAULT_TAB"><span><spring:message code="${defaultTabKey}"/></span> <span class="badge"><c:out value="${fn:length(results[defaultTabKey])}" /></span></a></li>
                                    <c:forEach var="tabKey" items="${tabKeys}" varStatus="loopStatus">
                                        <li><a href="#${n}_${loopStatus.index}"><span><spring:message code="${tabKey}"/></span> <span class="badge"><c:out value="${fn:length(results[tabKey])}" /></span></a></li>
                                    </c:forEach>
                                </ul>

                                <%--
                                 | result.first is the SearchResult object
                                 | result.second is the calculated URL
                                 +--%>

                                <!-- Write out the default results tab -->
                                <div id="${n}_DEFAULT_TAB">
                                    <div class="search-results">
                                        <c:forEach items="${ results[defaultTabKey] }" var="result">
                                            <div class="search-result">
                                                <div class="panel panel-default">
                                                    <div class="panel-heading">
                                                        <h3 class="panel-title"><a class="result_link" href="${result.second}"><span class="result_title"><i class="fa fa-arrow-circle-right"></i> ${ result.first.title }</span></a></h3>
                                                    </div>
                                                    <div class="panel-body">
                                                        <p class="result_excerpt">${ result.first.summary }</p>
                                                    </div>
                                                    <%-- Start of display marketplace specific information --%>
                                                    <c:if test="${up:contains(result.first.type, 'marketplace')}">
                                                        <div class="panel-footer">
                                                            <p><a class="marketplace_entry_link" href="${pURL:getStringFromPortletUrl(result.first.portletUrl, pageContext.request)}">About this app</a>
                                                        </div>
                                                    </c:if>
                                                    <%-- End of display marketplace specific information --%>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>

                                <!-- write out each additional results tab -->
                                <c:forEach var="tabKey" items="${tabKeys}" varStatus="loopStatus">
                                    <div id="${n}_${loopStatus.index}" class="${tabKey}">
                                        <div class="search-results">
                                            <c:forEach items="${ results[tabKey] }" var="result">
                                                <div class="search-result">
                                                    <div class="panel panel-default">
                                                        <div class="panel-heading">
                                                            <h3 class="panel-title"><a class="result_link" href="${result.second}"><span class="result_title"><i class="fa fa-external-link"></i> ${ result.first.title }</span></a></h3>
                                                        </div>
                                                        <div class="panel-body">
                                                            <p class="result_excerpt">${ result.first.summary }</p>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:when>
                    <c:otherwise>
                        <div class="search-results-empty">
                            <p><spring:message code="no.results"/></p>
                        </div>
                </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/search/autosuggestHandler.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/search/autosuggest.css">

<%-- Only set prepopulateAutoSuggestUrl if the portlet preference is not empty. --%>
<c:if test="${not empty portletPreferencesValues['prepopulateAutoSuggestUrl'][0]}">
    <c:set var="prepopulateAutoSuggestUrl" value="${pageContext.request.contextPath}${portletPreferencesValues['prepopulateAutoSuggestUrl'][0]}"/>
</c:if>

<script language="javascript" type="text/javascript"><rs:compressJs>
(function($) {

    $(function() {
        up.initSearchAuto($, {
            searchFieldSelector: '#${n}searchInput',
            prepopulateAutoSuggestUrl: '${prepopulateAutoSuggestUrl}',
            prepopulateUrlPattern: "${pageContext.request.contextPath}${portletPreferencesValues['prepopulateUrlPattern'][0]}",
            autoSuggestResultsProcessor: "${portletPreferencesValues['autoSuggestResultsProcessor'][0]}",
            autoSuggestSearchUrl: '${autocompleteUrl}'
        });
        $('#${n}searchResults').tabs();
        <c:if test="${empty tabKeys}">
            // Do not display the tabs header when tabKeys is empty
            $('#${n}searchResults .searchTabsContainer').addClass('hidden');
        </c:if>
        $('#${n}searchResults').removeClass('hidden'); // Un-hide the search results now that the tabs are rendered
    });

    // Only search if the user entered some text to search for
    $('#${n}searchForm').submit(function(event) {
        if ($('#${n}searchInput').val().trim().length == 0) {
            event.preventDefault();
        } else {
            document.getElementById('${n}searchButton').disabled = 1;
        }

    });

})(up.jQuery);
</rs:compressJs></script>
