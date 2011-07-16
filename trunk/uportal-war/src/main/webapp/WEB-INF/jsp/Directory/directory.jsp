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

<portlet:renderURL var="formUrl"/>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="search"/></h2>
  </div>
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}search" class="portlet-section" role="region">

      <div class="portlet-section-body">

        <form action="${ formUrl }" method="POST">
            <input name="query" value="${ fn:escapeXml(query )}"/> <input type="submit" value="Search"/>
        </form>

        <c:if test="${not empty query}">

            <div class="portlet-section" role="region">
          
                <div class="titlebar">
                  <h3 class="title" role="heading">
                      <spring:message code="search.results"/>
                  </h3>
                </div>
                
                <div class="content">
                        
                    <div id="${n}_directory">
                        <c:if test="${ fn:length(people) == 0 }">
                            <spring:message code="no.results"/>
                        </c:if>
                        <c:forEach items="${ people }" var="person">
                            <div class="person-search-result">
                                <h3><a class="person-link" href="javascript:;">${fn:escapeXml(person.attributes.displayName[0])}</a></h3>
                                <table>
                                    <c:forEach items="${ attributeNames }" var="attribute">
                                        <c:if test="${ fn:length(person.attributes[attribute.key]) > 0 }">
                                            <tr>
                                                <td>
                                                    <spring:message code="attribute.displayName.${ attribute.key }"/>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${ attribute.value == 'EMAIL' }">
                                                            <a href="mailto:${ person.attributes[attribute.key][0] }">${ fn:escapeXml(person.attributes[attribute.key][0]) }</a>
                                                        </c:when>
                                                        <c:when test="${ attribute.value == 'MAP' }">
                                                            <a href="<c:url value="http://maps.google.com/maps"><c:param name="q" value="${ fn:escapeXml(fn:replace(person.attributes[attribute.key][0], '$', ' ')) }"/></c:url>">${ fn:replace(fn:escapeXml(person.attributes[attribute.key][0]), '$', '<br/>') }</a>
                                                        </c:when>
                                                        <c:when test="${ attribute.value == 'LINK' }">
                                                            <a href="${ fn:escapeXml(person.attributes[attribute.key][0]) }">${ fn:escapeXml(person.attributes[attribute.key][0]) }</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${ fn:escapeXml(person.attributes[attribute.key][0]) }
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                </table>
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
