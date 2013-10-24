<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<portlet:actionURL var="formUrl"/>
<portlet:resourceURL var="autocompleteUrl" id="retrieveSearchJSONResults"/>

<!-- Portlet -->
<div class="fl-widget portlet search-portlet" role="section">

  <!-- Portlet Titlebar 
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="search"/></h2>
  </div>
  -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}search" class="portlet-section" role="region">

      <div class="portlet-section-body">

        <form action="${ formUrl }" method="POST">
            <input id="${n}searchInput" class="searchInput" name="query" value="${ fn:escapeXml(query )}"/>
            <input type="submit" value="<spring:message code="search.submit"/>"/>
            <input class="autocompleteUrl" name="autocompleteUrl" type="hidden" value="${autocompleteUrl}"/>
        </form>
        
        <c:if test="${hitMaxQueries}">
            <div>
                <spring:message code="search.rate.limit.reached"/>
            </div>
        </c:if>

        <c:if test="${not empty results}">

            <div class="portlet-section" role="region">
          
                <div class="content">
                    <div id="${n}searchResults" class="hidden">
                        <ul>
                            <li><a href="#${n}_DEFAULT_TAB"><span><spring:message code="${defaultTabKey}"/></span></a></li>
                            <c:forEach var="tabKey" items="${tabKeys}" varStatus="loopStatus">
                                <li><a href="#${n}_${loopStatus.index}"><span><spring:message code="${tabKey}"/></span></a></li>
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
                                <div>
                                  <a class="result_link" href="${result.second}"><span class="result_title">${ result.first.title }</span></a>
                                </div>
                                <div class="result_excerpt">${ result.first.summary }</div>
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
                                  <div>
                                    <a class="result_link" href="${result.second}"><span class="result_title">${ result.first.title }</span></a>
                                  </div>
                                  <div class="result_excerpt">${ result.first.summary }</div>
                                </div>
                              </c:forEach>
                            </div>
                          </div>
                        </c:forEach>

                    </div>
                </div>
            </div>
        </c:if>
      </div>
    </div>
  </div>
</div>

<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.10.2/jquery-1.10.2.min.js"/>"></script>
<script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.10.3/jquery-ui-1.10.3.min.js"/>"></script>

<%@ include file="autosuggest_handler.jsp"%>

<script language="javascript" type="text/javascript"><rs:compressJs>
/*
* Switch jQuery to extreme noConflict mode, keeping a reference to it in the searchjQ["${n}"] namespace
*/
var searchjQ = searchjQ || {};
searchjQ["${n}"] = searchjQ["${n}"] || {};
searchjQ["${n}"].jQuery = jQuery.noConflict(true);

searchjQ["${n}"].jQuery(document).ready(function() {
    initSearchAuto(searchjQ["${n}"].jQuery, "#${n}searchInput");
    searchjQ["${n}"].jQuery("#${n}searchResults").tabs();
    searchjQ["${n}"].jQuery("#${n}searchResults").removeClass("hidden"); // Unhide the search results now that the tabs are rendered
});

</rs:compressJs></script>
