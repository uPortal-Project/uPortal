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

<div class="fl-widget portlet" role="section">
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 role="heading"><spring:message code="personDetails.detailsTitle" arguments="${person.name}" /></h2>
    </div>
    
    <div class="fl-widget-content portlet-body" role="main">
    
        <div class="portlet-section" role="region">
        
            <div class="portlet-section-body">
        
				<table>
				    <thead>
				        <tr>
				            <th><spring:message code="personDetails.attributeHeader"/></th>
				            <th><spring:message code="personDetails.valuesHeader"/></th>
				        </tr>
				    </thead>
				    <tbody>
				        <c:forEach var="attrName" items="${displayAttributes}">
				            <tr>
				                <td><spring:message code="${attrName}" text="${attrName}" arguments="${attrName}"/></td>
				                <c:set var="attrValues" value="${person.attributes[attrName]}" />
				                <c:choose>
				                    <c:when test="${fn:length(attrValues) >= 1}">
				                        <c:forEach var="attrValue" items="${attrValues}" varStatus="attrValueStatus">
				                            <td>${fn:escapeXml(attrValue)}</td>
				                            <c:if test="${not attrValueStatus.last}">
				                                </tr>
				                                <tr>
				                                    <td/>
				                            </c:if>
				                        </c:forEach>
				                    </c:when>
				                    <c:otherwise>
				                        <td/>
				                    </c:otherwise>
				                </c:choose>
				            </tr>
				        </c:forEach>
				    </tbody>
				</table>
				
				<div class="portlet-button-group">
				    <portlet:actionURL var="selectUserUrl">
				        <portlet:param name="execution" value="${flowExecutionKey}" />
				        <portlet:param name="_eventId" value="selectAndGo" />
				    </portlet:actionURL>
				    <a class="portlet-button portlet-button-primary" href="${selectUserUrl}"><spring:message code="${personDetails_selectAndGoButtonTextKey}" /></a> -
				    
				    <c:if test="${fn:length(personQueryResults) > 1}">
				        <portlet:renderURL var="backToResultsUrl">
				            <portlet:param name="execution" value="${flowExecutionKey}" />
				            <portlet:param name="_eventId" value="searchResults" />
				        </portlet:renderURL>
				        <a class="portlet-button secondary" href="${backToResultsUrl}"><spring:message code="personDetails.backToResultsLink" /></a> -
				    </c:if>
				    
				    <portlet:renderURL var="newSearchUrl">
				        <portlet:param name="execution" value="${flowExecutionKey}" />
				        <portlet:param name="_eventId" value="newSearch" />
				    </portlet:renderURL>
				    <a class="portlet-button" href="${newSearchUrl}"><spring:message code="personDetails.newSearchLink" /></a>
				</div>
            </div>
        </div>
    </div>
</div>