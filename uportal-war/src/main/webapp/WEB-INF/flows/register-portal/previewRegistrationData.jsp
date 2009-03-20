<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<h1>Registration Preview</h1>

<p>The following information will be submitted to Jasig:</p>

<h2>Organizational Information</h2>
<table>
    <tr>
        <th>Institution name:</th> 
        <td>${fn:escapeXml(registrationData.institutionName)}</td>
    </tr>
    <tr>
        <th>Technical contact name:</th> 
        <td>${fn:escapeXml(registrationData.deployerName)}</td>
    </tr>
    <tr>
        <th>Technical contact email address:</th> 
        <td>${fn:escapeXml(registrationData.deployerAddress)}</td>
    </tr>
    <tr>
        <th>Portal name (e.g. "MyPortal"):</th> 
        <td>${fn:escapeXml(registrationData.portalName)}</td>
    </tr>
    <tr>
        <th>Portal URL:</th> 
        <td>${fn:escapeXml(registrationData.portalUrl)}</td>
    </tr>
</table>

<h2>System information</h2>
<c:forEach var="dataEntry" items="${registrationData.collectedData}">
    <h4>${fn:escapeXml(dataEntry.key)}</h4>
    <table>
        <c:forEach var="valueEntry" items="${dataEntry.value}">
            <tr>
                <th>${fn:escapeXml(valueEntry.key)}</th>
                <td>${fn:escapeXml(valueEntry.value)}</td>
            </tr>
        </c:forEach>
    </table>
</c:forEach>

<p>
    Your deployment will <c:if test="${!registrationData.shareInfo}">not </c:if>be added to the uPortal deployment list:
    <a href="http://www.jasig.org/uportal/deployments" target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>
</p>

<portlet:renderURL var="editRegistrationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editRegistration" />
</portlet:renderURL>
<a href="${editRegistrationUrl}" class="portlet-form-button">Edit Data</a>

<portlet:actionURL var="submitRegistrationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="submitRegistration" />
</portlet:actionURL>
<a href="${submitRegistrationUrl}" class="portlet-form-button">Submit Registration</a>
