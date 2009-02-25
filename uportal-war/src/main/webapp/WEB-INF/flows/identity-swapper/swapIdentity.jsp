<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

idswapper: Swap Identity to ${uid}

<br />

<portlet:actionURL var="newSearchUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="personLookup" />
</portlet:actionURL>

<a href="${newSearchUrl}">Lookup Person</a>