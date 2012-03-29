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

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

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
            <input name="query" value="${ fn:escapeXml(query )}"/> <input type="submit" value="Search"/>
        </form>

        <c:if test="${not empty results}">

            <div class="portlet-section" role="region">
          
                <div class="content">
                    <div id="${n}searchResults">
                        <ul>
                            <li><a href="#${n}_portal"><span><spring:message code="portal.results"/></span></a></li>
                            <li><a href="#${n}_uwPeople"><span><spring:message code="uwPeople.results"/></span></a></li>
                            <li><a href="#${n}_uwGoogle"><span><spring:message code="uwGoogle.results"/></span></a></li>
                        </ul>
                        
                        <div id="${n}_portal">
                            <div class="search-results">
                                <c:forEach items="${ results.results }" var="type">
                                    <!-- Display all non uwPeople/uwGoogle results -->
                                    <c:if test="${ type.key != 'uwPeople' and type.key != 'uwPeopleError' and type.key != 'uwGoogle' }">
                                        <c:forEach items="${ type.value }" var="result">
                                            <div class="portlet-match search-result">
                                                <div class="portlet_title result_title"><a class="portlet-match-link" href="${result.key}">${ result.value.title }</a></div>
                                                <p class="portlet-match-description">${ result.value.summary }</p>
                                            </div>
                                        </c:forEach>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                        
                        <div id="${n}_uwPeople" class="uwPeople">
                          <div class="search-results">
                            <c:forEach items="${ results['uwPeople'].value }" var="result">
                                <c:set var="summaryParts" value="${fn:split(result.value.summary, '|')}"/>
                                
                                <div class="person-match search-result">
                                  <div class="person_name result_title">${ result.value.title }</div>
                                  <c:if test="${summaryParts[0] not empty}">
                                    <div class="person_email">
                                      <a class="person_email-link" href="mailto:${summaryParts[0]}">${summaryParts[0]}</a>
                                    </div>
                                  </c:if>
                                  <c:if test="${fn:length(summaryParts) gt 1 and summaryParts[1] not empty}">
                                    <div class="person_phone">${summaryParts[1]}</div>
                                    <div class="person_more">
                                      <a class="person_more-link" href="${result.key}">More &raquo;</a>
                                    </div>
                                  </c:if>
                                </div>
                            </c:forEach>
                            <c:forEach items="${ results['uwPeopleError'].value }" var="result">
                              <div class="error-match">
                                <div class="error-msg">${ result.value.title }</div>
                              </div>
                            </c:forEach>
                          </div>
                        </div>
                        
                        
                        <div id="${n}_uwGoogle" class="uwGoogle">
                          <div class="search-results">
                            <c:forEach items="${ results['uwGoogle'].value }" var="result">
                              <div class="campus-search-result search-result">
                                <div>
                                  <a class="result_link" href="${result.key}"><span class="result_title">${ result.value.title }</span></a>
                                </div>
                                <div class="result_excerpt">${ result.value.summary }</div>
                              </div>
                            </c:forEach>
                          </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

      </div>  

    </div>
    
  </div>

</div>
