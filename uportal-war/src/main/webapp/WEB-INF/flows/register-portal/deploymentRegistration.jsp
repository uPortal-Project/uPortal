<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<portlet:actionURL var="postUrl"/>

<h2>Register this portal instance</h2>

<p>
	Registering this portal instance is easy and will only take a moment!  Just
	fill out the short form below to send us some basic information about your
	portal environment.  We appreciate knowing more about our users!
</p>

<form:form modelAttribute="registrationRequest" action="${postUrl}" method="post">
	
	<h3>Organizational Information</h3>
	<p>
		<form:label path="institutionName">Institution name:</form:label> 
		<form:input path="institutionName"/>
	</p>
	<p>
		<form:label path="deployerName">Technical contact name:</form:label> 
		<form:input path="deployerName"/>
	</p>
	<p>
		<form:label path="deployerAddress">Technical contact email address:</form:label> 
		<form:input path="deployerAddress"/>
	</p>
	<p>
		<form:label path="portalName">Portal name (e.g. "MyPortal"):</form:label> 
		<form:input path="portalName"/>
	</p>
	<p>
		<form:label path="portalUrl">Portal URL:</form:label> 
		<form:input path="portalUrl"/>
	</p>
	
	<h3>System information</h3>
	<p>
		We can automatically gather information about your portal's environment
		for you and submit it along with this registration.  If you prefer not
		to submit this information, just uncheck the appropriate box.
	</p>
    <c:forEach var="dataEntry" items="${dataToSubmit}">
        <p>
        <form:checkbox path="dataEntry['${dataEntry.key}']"/> 
        <form:label path="dataEntry['${dataEntry.key}']">${dataEntry.key}</form:label>
        </p>
    </c:forEach>
    <%--
	<p>
		<form:checkbox path="upVersion"/> 
		<form:label path="upVersion">uPortal version</form:label>
	</p>
	<p>
		<form:checkbox path="databaseInfo"/> 
		<form:label path="databaseInfo">Database info (database and driver names and versions)</form:label>
	</p>
	<p>
		<form:checkbox path="jvmInfo"/> 
		<form:label path="jvmInfo">JVM Info (vendor, version, spec version)</form:label>
	</p>
	<p>
		<form:checkbox path="containerInfo"/> 
		<form:label path="containerInfo">Container info (vendor, version, spec version)</form:label>
	</p>
	<p>
		<form:checkbox path="ipAddress"> 
		<form:label path="ipAddress">IP Address (used for geographical data)</form:label>
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
	
	<p><form:checkbox path="shareInfo"/> It's OK to share my information</p>
	
	<p><input type="submit" class="button" name="_eventId_previewRegistration" value="Submit" /></p>
	
</form:form>
