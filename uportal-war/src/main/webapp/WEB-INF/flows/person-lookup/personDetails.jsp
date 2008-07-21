<%@ include file="/WEB-INF/jsp/include.jsp"%>

<div><spring:message code="personDetails.detailsTitle" arguments="${person.name}" /></div>

<table>
    <thead>
        <tr>
            <th>Attribute</th>
            <th>Value(s)</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="attrName" items="${displayAttributes}">
            <tr>
                <td><spring:message code="${attrName}" text="${attrName}" /></td>
                <c:set var="attrValues" value="${person.attributes[attrName]}" />
                <c:choose>
                    <c:when test="${fn:length(attrValues) >= 1}">
                        <c:forEach var="attrValue" items="${attrValues}" varStatus="attrValueStatus">
                            <td>${fn:escapeXml(attrValue)}</td>
                            <c:if test="not attrValueStatus.last">
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

<portlet:actionURL var="selectUserUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="selectAndGo" />
</portlet:actionURL>
<spring:message var="selectAndGoButtonText" code="${personDetails_selectAndGoButtonTextKey}" />
<a href="${selectUserUrl}">${selectAndGoButtonText}</a>

<c:if test="${fn:length(personQueryResults) > 1}">
    <portlet:renderURL var="backToResultsUrl">
        <portlet:param name="execution" value="${flowExecutionKey}" />
        <portlet:param name="_eventId" value="searchResults" />
    </portlet:renderURL>
    <spring:message var="backToResultsLinkText" code="personDetails.backToResultsLink" />
    <a href="${backToResultsUrl}">${backToResultsLinkText}</a>
</c:if>

<portlet:renderURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="newSearch" />
</portlet:renderURL>
<spring:message var="backToResultsLinkText" code="personDetails.newSearchLink" />
<a href="${newSearchUrl}">${backToResultsLinkText}</a>