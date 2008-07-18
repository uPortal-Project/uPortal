<%@ include file="/WEB-INF/jsp/include.jsp"%>

idswapper: Swap Identity to ${uid}

<br />

<portlet:actionURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="personLookup" />
</portlet:actionURL>

<a href="${newSearchUrl}">Lookup Person</a>