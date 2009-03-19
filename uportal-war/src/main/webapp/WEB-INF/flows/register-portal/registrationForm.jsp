<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<h2>Register this portal instance</h2>

<p>
	Registering this portal instance is easy and will only take a moment!  Just
	fill out the short form below to send us some basic information about your
	portal environment.  We appreciate knowing more about our users!
</p>

<portlet:renderURL var="postUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:renderURL>
<form:form modelAttribute="registrationRequest" action="${postUrl}" method="post">
	
	<h3>Organizational Information</h3>
    <ul class="fl-controls-right">
		<li>
			<form:label path="institutionName" cssClass="fl-label">Institution name:</form:label> 
			<form:input path="institutionName"/>
		</li>
		<li>
			<form:label path="deployerName" cssClass="fl-label">Technical contact name:</form:label> 
			<form:input path="deployerName"/>
		</li>
		<li>
			<form:label path="deployerAddress" cssClass="fl-label">Technical contact email address:</form:label> 
			<form:input path="deployerAddress"/>
		</li>
		<li>
			<form:label path="portalName" cssClass="fl-label">Portal name (e.g. "MyPortal"):</form:label> 
			<form:input path="portalName"/>
		</li>
		<li>
			<form:label path="portalUrl" cssClass="fl-label">Portal URL:</form:label> 
			<form:input path="portalUrl"/>
		</li>
	</ul>
	
	<h3>System information</h3>
	<p>
		We can automatically gather information about your portal's environment
		for you and submit it along with this registration.  If you prefer not
		to submit this information, just uncheck the appropriate box.
	</p>
    <ul>
        <c:forEach var="dataEntry" items="${registrationRequest.dataToSubmit}">
            <li>
                <spring:message var="dataDisplayName" text="${dataEntry.key}" code="data.${dataEntry.key}" />
                <form:checkbox path="dataToSubmit['${dataEntry.key}']" value="true"/> 
                <form:label path="dataToSubmit['${dataEntry.key}']" cssClass="fl-label">${dataDisplayName}</form:label>
            </li>
        </c:forEach>
    </ul>
    <%--
	<p>
		<form:checkbox path="upVersion"/> 
		<form:label path="upVersion" cssClass="fl-label">uPortal version</form:label>
	</p>
	<p>
		<form:checkbox path="databaseInfo"/> 
		<form:label path="databaseInfo" cssClass="fl-label">Database info (database and driver names and versions)</form:label>
	</p>
	<p>
		<form:checkbox path="jvmInfo"/> 
		<form:label path="jvmInfo" cssClass="fl-label">JVM Info (vendor, version, spec version)</form:label>
	</p>
	<p>
		<form:checkbox path="containerInfo"/> 
		<form:label path="containerInfo" cssClass="fl-label">Container info (vendor, version, spec version)</form:label>
	</p>
	<p>
		<form:checkbox path="ipAddress"> 
		<form:label path="ipAddress" cssClass="fl-label">IP Address (used for geographical data)</form:label>
	</p>
	--%>
	<h3>Sharing</h3>
	
	<p>
		Jasig maintains a public list of deployed uPortal instances.  We'd love to 
		include your portal in our list of uPortal-powered sites and on a map of 
		worldwide portal deployments.  If you're interested in viewing our current list
		of uPortal sites, you can visit it at <a href="http://www.jasig.org/uportal/deployments"
		target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>.
	</p>
	
	<p>
	   <form:checkbox path="shareInfo"/> 
	   <form:label path="shareInfo" cssClass="fl-label"> It's OK to share my information</form:label>
	</p>
	
	<p><input type="submit" class="button" name="_eventId_previewRegistration" value="Submit" /></p>
	
</form:form>
