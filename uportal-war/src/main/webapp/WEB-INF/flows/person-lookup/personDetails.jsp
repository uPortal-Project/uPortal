<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<div>
    <portlet:actionURL var="selectUserUrl">
        <portlet:param name="execution" value="${flowExecutionKey}" />
        <portlet:param name="_eventId" value="selectAndGo" />
    </portlet:actionURL>
    <a href="${selectUserUrl}"><spring:message code="${personDetails_selectAndGoButtonTextKey}" /></a> -
    
    <c:if test="${fn:length(personQueryResults) > 1}">
        <portlet:renderURL var="backToResultsUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="searchResults" />
        </portlet:renderURL>
        <a href="${backToResultsUrl}"><spring:message code="personDetails.backToResultsLink" /></a> -
    </c:if>
    
    <portlet:renderURL var="newSearchUrl">
        <portlet:param name="execution" value="${flowExecutionKey}" />
        <portlet:param name="_eventId" value="newSearch" />
    </portlet:renderURL>
    <a href="${newSearchUrl}"><spring:message code="personDetails.newSearchLink" /></a>
</div>

<div style="padding-bottom: 0.5em;"><spring:message code="personDetails.detailsTitle" arguments="${person.name}" /></div>

<table border="1">
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

<div>
    <portlet:actionURL var="selectUserUrl">
        <portlet:param name="execution" value="${flowExecutionKey}" />
        <portlet:param name="_eventId" value="selectAndGo" />
    </portlet:actionURL>
    <a href="${selectUserUrl}"><spring:message code="${personDetails_selectAndGoButtonTextKey}" /></a> -
    
    <c:if test="${fn:length(personQueryResults) > 1}">
        <portlet:renderURL var="backToResultsUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="searchResults" />
        </portlet:renderURL>
        <a href="${backToResultsUrl}"><spring:message code="personDetails.backToResultsLink" /></a> -
    </c:if>
    
    <portlet:renderURL var="newSearchUrl">
        <portlet:param name="execution" value="${flowExecutionKey}" />
        <portlet:param name="_eventId" value="newSearch" />
    </portlet:renderURL>
    <a href="${newSearchUrl}"><spring:message code="personDetails.newSearchLink" /></a>
</div>
