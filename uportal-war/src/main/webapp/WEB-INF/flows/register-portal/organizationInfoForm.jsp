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
    	<h2 class="title" role="heading">Register your portal</h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
        
        <!-- Note -->
        <div class="note" role="note">
            <p>
                Registering this portal instance is easy and will only take a moment!  Just
                fill out the short form below to send us some basic information about your
                portal environment.  We appreciate knowing more about our users!
            </p>
        </div>
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">Organizational Information</h3>
            </div>
            <div class="content">
                
                <portlet:actionURL var="postUrl">
                    <portlet:param name="execution" value="${flowExecutionKey}" />
                </portlet:actionURL>
                
                <!-- Note -->
                <div class="note" role="note">
                    <p>* Denotes required fields</p>
                </div>
            
            	<div class="portlet-form">
                	<form:form modelAttribute="registrationRequest" action="${postUrl}" method="post">
                        <spring:hasBindErrors name="registrationRequest">
                            <!-- Messages -->
                            <div class="portlet-msg-error portlet-msg error" role="alert">
                            	<div class="titlebar">
                                	<h3 class="title">Error</h3>
                                </div>
                            	<div class="content">
                                	<form:errors path="*" element="p"/>
                                </div>
                            </div>
                        </spring:hasBindErrors>
                        <table class="purpose-layout">
                            <tr>
                                <td class="label"><form:label path="institutionName" cssClass="fl-label">Institution name:</form:label></td>
                                <td><form:input path="institutionName"/>*</td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="deployerName" cssClass="fl-label">Technical contact name:</form:label></td>
                                <td><form:input path="deployerName"/>*</td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="deployerAddress" cssClass="fl-label">Technical contact email address:</form:label></td>
                                <td><form:input path="deployerAddress"/>*</td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="portalName" cssClass="fl-label">Portal name (e.g. "MyPortal"):</form:label></td>
                                <td><form:input path="portalName"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="portalUrl" cssClass="fl-label">Portal URL:</form:label></td>
                                <td><form:input path="portalUrl"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="demoUrl" cssClass="fl-label">Demo URL:</form:label></td>
                                <td><form:input path="demoUrl"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="numberOfUsers" cssClass="fl-label">Number of Users:</form:label></td>
                                <td><form:input path="numberOfUsers"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="audience" cssClass="fl-label">Portal Audience:</form:label></td>
                                <td><form:input path="audience"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="authnSystem" cssClass="fl-label">Authentication System:</form:label></td>
                                <td><form:input path="authnSystem"/></td>
                            </tr>
                            <tr>
                                <td class="label"><form:label path="notes" cssClass="fl-label">Additional Notes:</form:label></td>
                                <td><form:textarea path="notes"/></td>
                            </tr>
                        </table>
                        <div class="buttons">
                            <input type="submit" name="_eventId_next" value="Next" class="button primary" />
                        </div>
                    </form:form>
                    
                </div>
            </div>
        </div>
                    
    </div> <!-- end: portlet-content -->
</div> <!-- end:portlet -->