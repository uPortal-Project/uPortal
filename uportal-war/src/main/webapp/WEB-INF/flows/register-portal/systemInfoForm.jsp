<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<h2>Register this portal instance</h2>

<portlet:actionURL var="postUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<form:form modelAttribute="registrationRequest" action="${postUrl}" method="post">
    <h3>System information</h3>
    <p>
        We can automatically gather information about your portal's environment
        for you and submit it along with this registration.  If you prefer not
        to submit this information, just uncheck the appropriate box. You will
        be able to review this infomration before it is submitted.
    </p>
    <ul>
        <c:forEach var="dataEntry" items="${registrationRequest.dataToSubmit}">
            <li style="list-style-type: none;">
                <spring:message var="dataDisplayName" text="${dataEntry.key}" code="data.${dataEntry.key}" />
                <form:checkbox path="dataToSubmit['${dataEntry.key}']" value="true"/> 
                <form:label path="dataToSubmit['${dataEntry.key}']" cssClass="fl-label"> ${dataDisplayName}</form:label>
            </li>
        </c:forEach>
    </ul>

	<h3>Sharing</h3>
	
	<p>
		Jasig maintains a public list of deployed uPortal instances.  We'd love to 
		include your portal in our list of uPortal-powered sites and on a map of 
		worldwide portal deployments.  If you're interested in viewing our current list
		of uPortal sites, you can visit it at <a href="http://www.jasig.org/uportal/deployments"
		target="_blank" alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>.
	</p>
	
	<ul>
	   <li style="list-style-type: none;">
		   <form:checkbox path="shareInfo"/> 
		   <form:label path="shareInfo" cssClass="fl-label">
              It's OK to include my deployment information on
              <a href="http://www.jasig.org/uportal/deployments" target="_blank" alt="uPortal deployments list">jasig.org</a>.
              Contact information will never be shared.
           </form:label>
        </li>
	</ul>
	
    <p>
       <input type="submit" name="_eventId_previous" value="Back" class="portlet-form-button" title="Back to Organization Infomration" />
       <input type="submit" name="_eventId_next" value="Preview Registration" class="portlet-form-button" />
    </p>
</form:form>
