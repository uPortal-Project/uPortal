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

<portlet:actionURL var="formUrl"/>
<c:set var="n"><portlet:namespace/></c:set>

<div class="portlet">

    <div class="portlet-content" data-role="content">
    
        <form action="${ formUrl }" method="POST">
            <c:choose>
                <c:when test="${ not empty query }">
                    <input data-type="search"  name="query" value="${ fn:escapeXml(query )}"/>
                </c:when>
                <c:otherwise>
                    <input placeholder="Search..." data-type="search"  name="query"/>
                </c:otherwise>
            </c:choose>
        </form>
        
        <c:if test="${hitMaxQueries}">
            <div>
                <spring:message code="search.rate.limit.reached"/>
            </div>
        </c:if>

        <c:if test="${not empty results}">
                        
            <%--
             | result.first is the SearchResult object
             | result.second is the calculated URL
             +--%>

            <ul data-role="listview" data-inset="true">
                <!-- Write out the default results tab -->
                <li data-role="list-divider"><spring:message code="${defaultTabKey}"/></li>
                <c:forEach items="${ results[defaultTabKey] }" var="result">
                  <li>
                      <c:choose>
                          <c:when test="${ not empty result.second }">
                              <a href="${ result.second }">
                                  <h3>${ result.first.title }</h3>
                                  <p>${ result.first.summary }</p>
                              </a>
                          </c:when>
                          <c:otherwise>
                              <h3>${ result.first.title }</h3>
                              <p>${ result.first.summary }</p>
                          </c:otherwise>
                      </c:choose>
                  </li>
                </c:forEach>
                
                <!-- write out each additional results tab -->
                <c:forEach var="tabKey" items="${tabKeys}" varStatus="loopStatus">
                    <li data-role="list-divider"><spring:message code="${tabKey}"/></li>
                    <c:forEach items="${ results[tabKey] }" var="result">
                      <li>
                          <c:choose>
                              <c:when test="${ not empty result.second }">
                                  <a href="${ result.second }">
                                      <h3>${ result.first.title }</h3>
                                      <p>${ result.first.summary }</p>
                                  </a>
                              </c:when>
                              <c:otherwise>
                                  <h3>${ result.first.title }</h3>
                                  <p>${ result.first.summary }</p>
                              </c:otherwise>
                          </c:choose>
                      </li>
                    </c:forEach>
                </c:forEach>
            </ul>
        </c:if>
    </div>
</div>