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
                <spring:message var="dataDisplayName" text="${fn:escapeXml(dataEntry.key)}" code="data.${dataEntry.key}" />
                <form:checkbox path="dataToSubmit['${fn:escapeXml(dataEntry.key)}']" value="true"/> 
                <form:label path="dataToSubmit['${fn:escapeXml(dataEntry.key)}']" cssClass="fl-label"> ${fn:escapeXml(dataDisplayName)}</form:label>
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
