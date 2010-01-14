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

<h2>Your portal has been successfully registered</h2>

<p>
	Thanks for registering this portal instance with Jasig!  We appreciate
	knowing more about our users.  If you'd like to see a list of other uPortal
	deployers who have registered their portals, Jasig maintains a list at
	<a href="http://www.jasig.org/uportal/deployments" target="_blank" 
	alt="uPortal deployments list">http://www.jasig.org/uportal/deployments</a>.
</p>

<portlet:actionURL var="backToStartUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="registrationForm" />
</portlet:actionURL>
<a href="${backToStartUrl}">Back</a><br/>

