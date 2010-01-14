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
    <tr>
        <th>Demo URL:</th> 
        <td>${fn:escapeXml(registrationData.demoUrl)}</td>
    </tr>
    <tr>
        <th>Number of Users:</th> 
        <td>${fn:escapeXml(registrationData.numberOfUsers)}</td>
    </tr>
    <tr>
        <th>Authentication System:</th> 
        <td>${fn:escapeXml(registrationData.authnSystem)}</td>
    </tr>
    <tr>
        <th>Additional Notes:</th> 
        <td>${fn:escapeXml(registrationData.notes)}</td>
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

<h2>Sharing</h2>
<p>
    We will <c:if test="${!registrationData.shareInfo}"><b>not</b> </c:if> add your information to the uPortal deployment list.
    <a href="http://www.jasig.org/uportal/deployments" target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>
</p>

<portlet:renderURL var="editRegistrationUrl" windowState="NORMAL">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editRegistration" />
</portlet:renderURL>
<a href="${editRegistrationUrl}" class="portlet-form-button">Edit Data</a>

<portlet:actionURL var="submitRegistrationUrl" windowState="NORMAL">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="submitRegistration" />
</portlet:actionURL>
<a href="${submitRegistrationUrl}" class="portlet-form-button">Submit Registration</a>
