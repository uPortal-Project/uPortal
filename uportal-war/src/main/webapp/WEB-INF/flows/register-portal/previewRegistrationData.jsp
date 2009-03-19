<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<h2>The following information will be submitted to Jasig</h2>

<h3>Organizational Information</h3>
<ul class="fl-controls-right">
    <li>
        Institution name: ${registrationData.institutionName}
    </li>
    <li>
        Technical contact name: ${registrationData.deployerName}
    </li>
    <li>
        Technical contact email address: ${registrationData.deployerAddress}
    </li>
    <li>
        Portal name (e.g. "MyPortal"): ${registrationData.portalName}
    </li>
    <li>
        Portal URL: ${registrationData.portalUrl}
    </li>
</ul>

<h3>System information</h3>
<ul>
    <c:forEach var="dataEntry" items="${registrationData.collectedData}">
        <li>
            ${dataEntry.key}: ${dataEntry.value}
        </li>
    </c:forEach>
</ul>

<p>
    Your deployment will <c:if test="${!registrationData.shareInfo}">not </c:if>be added to the uPortal deployment list:
    <a href="http://www.jasig.org/uportal/deployments" target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>
</p>

<portlet:renderURL var="editRegistrationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editRegistration" />
</portlet:renderURL>
<a href="${editRegistrationUrl}">Change Registration Data</a><br/>

<portlet:actionURL var="submitRegistrationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="submitRegistration" />
</portlet:actionURL>
<a href="${submitRegistrationUrl}">Submit Registration</a><br/>
