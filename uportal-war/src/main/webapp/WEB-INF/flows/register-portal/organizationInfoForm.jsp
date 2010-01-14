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

<h2>Register this portal instance</h2>
<p>
	Registering this portal instance is easy and will only take a moment!  Just
	fill out the short form below to send us some basic information about your
	portal environment.  We appreciate knowing more about our users!
</p>

<portlet:actionURL var="postUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<form:form modelAttribute="registrationRequest" action="${postUrl}" method="post">
    <h3>Organizational Information</h3>

    <spring:hasBindErrors name="registrationRequest">
        <div class="portlet-msg-error"><form:errors path="*" element="p"/></div>
    </spring:hasBindErrors>
    
    <ul class="fl-controls-right">
		<li>
			<form:label path="institutionName" cssClass="fl-label">Institution name:</form:label> 
			<form:input path="institutionName"/>*
		</li>
		<li>
			<form:label path="deployerName" cssClass="fl-label">Technical contact name:</form:label> 
			<form:input path="deployerName"/>*
		</li>
		<li>
			<form:label path="deployerAddress" cssClass="fl-label">Technical contact email address:</form:label> 
			<form:input path="deployerAddress"/>*
		</li>
		<li>
			<form:label path="portalName" cssClass="fl-label">Portal name (e.g. "MyPortal"):</form:label> 
			<form:input path="portalName"/>
		</li>
		<li>
			<form:label path="portalUrl" cssClass="fl-label">Portal URL:</form:label> 
			<form:input path="portalUrl"/>
		</li>
        <li>
            <form:label path="demoUrl" cssClass="fl-label">Demo URL:</form:label> 
            <form:input path="demoUrl"/>
        </li>
        <li>
            <form:label path="numberOfUsers" cssClass="fl-label">Number of Users:</form:label> 
            <form:input path="numberOfUsers"/>
        </li>
        <li>
            <form:label path="audience" cssClass="fl-label">Portal Audience:</form:label> 
            <form:input path="audience"/>
        </li>
        <li>
            <form:label path="authnSystem" cssClass="fl-label">Authentication System:</form:label> 
            <form:input path="authnSystem"/>
        </li>
        <li>
            <form:label path="notes" cssClass="fl-label">Additional Notes:</form:label> 
            <form:textarea path="notes"/>
        </li>
	</ul>
    <p>* Denotes required fields</p>
	
	<p><input type="submit" name="_eventId_next" value="Next" class="portlet-form-button" /></p>
</form:form>
