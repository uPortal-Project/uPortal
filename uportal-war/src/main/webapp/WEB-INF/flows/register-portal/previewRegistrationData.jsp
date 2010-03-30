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

<!-- Portlet -->
<div class="fl-widget portlet reg-portal view-preview" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Registration Preview</h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
		
        <!-- Note -->
        <div class="note">
        	<p>The following information will be submitted to Jasig:</p>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">Organizational Information</h3>
            </div>
            <div class="content">
            	<div class="portlet-form">
                    <table class="purpose-layout">
                        <tr>
                            <td class="label">Institution name:</td> 
                            <td>${fn:escapeXml(registrationData.institutionName)}</td>
                        </tr>
                        <tr>
                            <td class="label">Technical contact name:</td> 
                            <td>${fn:escapeXml(registrationData.deployerName)}</td>
                        </tr>
                        <tr>
                            <td class="label">Technical contact email address:</td> 
                            <td>${fn:escapeXml(registrationData.deployerAddress)}</td>
                        </tr>
                        <tr>
                            <td class="label">Portal name (e.g. "MyPortal"):</td> 
                            <td>${fn:escapeXml(registrationData.portalName)}</td>
                        </tr>
                        <tr>
                            <td class="label">Portal URL:</td> 
                            <td>${fn:escapeXml(registrationData.portalUrl)}</td>
                        </tr>
                        <tr>
                            <td class="label">Demo URL:</td> 
                            <td>${fn:escapeXml(registrationData.demoUrl)}</td>
                        </tr>
                        <tr>
                            <td class="label">Number of Users:</td> 
                            <td>${fn:escapeXml(registrationData.numberOfUsers)}</td>
                        </tr>
                        <tr>
                            <td class="label">Authentication System:</td> 
                            <td>${fn:escapeXml(registrationData.authnSystem)}</td>
                        </tr>
                        <tr>
                            <td class="label">Additional Notes:</td> 
                            <td>${fn:escapeXml(registrationData.notes)}</td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>    
            
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">System Information</h3>
            </div>
            <div class="content">
            	<div class="portlet-form">
                    <c:forEach var="dataEntry" items="${registrationData.collectedData}">
                        <h4>${fn:escapeXml(dataEntry.key)}</h4>
                        <table class="purpose-layout">
                            <c:forEach var="valueEntry" items="${dataEntry.value}">
                                <tr>
                                    <td class="label">${fn:escapeXml(valueEntry.key)}</td>
                                    <td>${fn:escapeXml(valueEntry.value)}</td>
                                </tr>
                            </c:forEach>
                        </table>
                    </c:forEach>
                </div>
            </div>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">Sharing</h3>
            </div>
            <div class="content">
            
				<div class="note" role="note">
                    <p>
                        We will <c:if test="${!registrationData.shareInfo}"><b>not</b> </c:if> add your information to the uPortal deployment list.
                        <a href="http://www.jasig.org/uportal/deployments" target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>
                    </p>
                </div>
                
                <div class="buttons">
                    <portlet:renderURL var="editRegistrationUrl" windowState="NORMAL">
                        <portlet:param name="execution" value="${flowExecutionKey}" />
                        <portlet:param name="_eventId" value="editRegistration" />
                    </portlet:renderURL>
                    <a href="${editRegistrationUrl}" class="button">Edit Data</a>
                    
                    <portlet:actionURL var="submitRegistrationUrl" windowState="NORMAL">
                        <portlet:param name="execution" value="${flowExecutionKey}" />
                        <portlet:param name="_eventId" value="submitRegistration" />
                    </portlet:actionURL>
                    <a href="${submitRegistrationUrl}" class="button">Submit Registration</a>
                </div>
        
                </div>
            </div>
        </div>

	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->