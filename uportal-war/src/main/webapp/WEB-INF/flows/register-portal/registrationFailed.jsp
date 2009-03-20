<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<h2>Registration Error</h2>

<p>
	An unexpected error occured while submitting your registration. Please
    either try submitting your registration again or check the portal log
    file for related exceptions. 
</p>

<portlet:actionURL var="backToStartUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="registrationForm" />
</portlet:actionURL>
<a href="${backToStartUrl}">Back</a><br/>

