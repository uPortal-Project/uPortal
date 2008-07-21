<%@ include file="/WEB-INF/jsp/include.jsp"%>

<c:if test="${emptyQueryResults == 'true'}">
    <div><spring:message code="personLookup.emptyResults" /></div>
</c:if>

<div><spring:message code="personLookup.searchTitle" /></div>

<portlet:renderURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:renderURL>
<form:form modelAttribute="personQuery" action="${queryUrl}">
    <table>
        <tbody>
            <c:forEach var="queryAttribute" items="${queryAttributes}">
                <tr>
                    <td>
                        <form:label path="attributes['${queryAttribute}'].value"><spring:message code="${queryAttribute}" text="${queryAttribute}" /></form:label>
                    </td>
                    <td>
                        <form:input path="attributes['${queryAttribute}'].value"/>
                    </td>
                    <td>
                        <form:errors path="attributes['${queryAttribute}'].value"/>
                    </td>
                </tr>
            </c:forEach>
        </tbody>            
    </table>
    
    <spring:message var="searchButtonText" code="personLookup.searchButton" />
    <input type="submit" class="button" name="_eventId_search" value="${searchButtonText}" />
    
    <spring:message var="newSearchButtonText" code="personLookup.newSearchButton" />
    <input type="submit" class="button" name="_eventId_newSearch" value="${newSearchButtonText}" />
    
    <c:if test="${showCancelButton == 'true'}">
        <spring:message var="cancelButtonText" code="personLookup.cancelButton" />
        <input type="submit" class="button" name="_eventId_cancel" value="${cancelButtonText}" />
    </c:if>
</form:form>