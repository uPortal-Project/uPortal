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

<!-- Portlet -->
<div class="fl-widget portlet prs-lkp view-details" role="section">
	
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="personDetails.detailsTitle" arguments="${person.name}" /></h2>
    </div>
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <table class="portlet-table">
            <thead>
                <tr>
                    <th><spring:message code="personDetails.attributeHeader"/></th>
                    <th><spring:message code="personDetails.valuesHeader"/></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="attrName" items="${fn:escapeXml(displayAttributes)}">
                    <tr>
                        <td><spring:message code="${attrName}" text="${attrName}" arguments="${attrName}"/></td>
                        <c:set var="attrValues" value="${fn:escapeXml(person.attributes[attrName])}" />
                        <c:choose>
                            <c:when test="${fn:escapeXml(fn:length(attrValues) >= 1)}">
                                <c:forEach var="attrValue" items="${fn:escapeXml(attrValues)}" varStatus="attrValueStatus">
                                    <td>${fn:escapeXml(fn:escapeXml(attrValue))}</td>
                                    <c:if test="${fn:escapeXml(not attrValueStatus.last)}">
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
        
        <!-- Buttons -->
        <div class="buttons">
            <portlet:actionURL var="selectUserUrl">
                <portlet:param name="execution" value="${fn:escapeXml(flowExecutionKey)}" />
                <portlet:param name="_eventId" value="selectAndGo" />
            </portlet:actionURL>
            
            <a class="button primary" href="${fn:escapeXml(selectUserUrl)}"><spring:message code="${personDetails_selectAndGoButtonTextKey}" /></a> -
            <c:if test="${fn:escapeXml(fn:length(personQueryResults) > 1)}">
                <portlet:renderURL var="backToResultsUrl">
                    <portlet:param name="execution" value="${fn:escapeXml(flowExecutionKey)}" />
                    <portlet:param name="_eventId" value="searchResults" />
                </portlet:renderURL>
                <a class="button" href="${fn:escapeXml(backToResultsUrl)}"><spring:message code="personDetails.backToResultsLink" /></a> -
            </c:if>
            
            <portlet:renderURL var="newSearchUrl">
                <portlet:param name="execution" value="${fn:escapeXml(flowExecutionKey)}" />
                <portlet:param name="_eventId" value="newSearch" />
            </portlet:renderURL>
            <a class="button" href="${fn:escapeXml(newSearchUrl)}"><spring:message code="personDetails.newSearchLink" /></a>
        </div>

    </div>
</div>