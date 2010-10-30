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

<c:set var="ns"><portlet:namespace/></c:set>

<style type="text/css">
    .person-lookup .attributeItem {
        font-size: 75%;
    }
</style>


<div id="${ns}_person-lookup" class="person-lookup">
<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<spring:message var="backToResultsLinkText" code="personSearchResults.newSearchLink" />
<a href="${newSearchUrl}">${backToResultsLinkText}</a>

<div><spring:message code="personSearchResults.resultsTitle" arguments="${fn:length(queryDisplayResults)}"/></div>

<ul>
    <c:forEach var="personDisplayEntry" items="${queryDisplayResults}">
        <li>
            <portlet:actionURL var="selectPersonUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="select" />
                <portlet:param name="uid" value="${personDisplayEntry.key.name}" />
            </portlet:actionURL>
            <portlet:actionURL var="selectAndGoPersonUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="selectAndGo" />
                <portlet:param name="uid" value="${personDisplayEntry.key.name}" />
            </portlet:actionURL>
            
            <spring:message text="${personDisplayEntry.value}"/>
            <ul style="margin-top: 0;">
                <li>
                    <a href="${selectAndGoPersonUrl}"><spring:message code="${personSearchResults_selectAndGoButtonTextKey}"/></a>
                    �
                    <a href="${selectPersonUrl}"><spring:message code="personSearchResults.viewDetailsLink"/></a>
                </li>

                <c:set var="moreInfoAttributes" value="${renderRequest.preferences.map['person-lookup.personSearchResults.moreInfoAttributes']}" />
                <c:if test="${not empty moreInfoAttributes}">
                    <c:forEach var="moreInfoAttr" items="${moreInfoAttributes}">
                        <c:if test="${not empty personDisplayEntry.key.attributes[moreInfoAttr]}">
                            <li class="attributeItem">
                                <span>${fn:escapeXml(moreInfoAttr}: </span>
                                <span>
                                    <c:forEach var="attrValue" items="${personDisplayEntry.key.attributes[moreInfoAttr]}" varStatus="attrValueStatus">
                                        ${fn:escapeXml(attrValue)}
                                        <c:if test="${not attrValueStatus.last}">, </c:if>
                                    </c:forEach>
                                </span>
                            </li>
                        </c:if>
                    </c:forEach>
                </c:if>
            </ul>
        </li>
    </c:forEach>
</ul>

<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<a href="${newSearchUrl}">${fn:escapeXml(backToResultsLinkText)}</a>

</div>
