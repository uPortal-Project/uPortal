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
        <c:forEach var="attrEntry" items="${person.attributes}">
            <c:choose>
                <c:when test="${fn:length(attrEntry.value) > 1}">
                    <c:forEach var="attrValue" items="attrEntry.value" varStatus="attrValueStatus">
                        <tr>
                            <c:choose>
                                <c:when test="${attrValueStatus.first}">
                                    <td><spring:message code="${attrEntry.key}" text="${attrEntry.key}" /></td>
                                </c:when>
                                <c:otherwise>
                                    <td/>
                                </c:otherwise>
                            </c:choose>
                            <td>${fn:escapeXml(attrValue)}</td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:when test="${fn:length(attrEntry.value) == 1}">
                    <tr>
                        <td><spring:message code="${attrEntry.key}" text="${attrEntry.key}" /></td>
                        <td>${fn:escapeXml(attrEntry.value[0])}</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td><spring:message code="${attrEntry.key}" text="${attrEntry.key}" /></td>
                        <td/>
                    </tr>
                </c:otherwise>
            </c:choose>
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