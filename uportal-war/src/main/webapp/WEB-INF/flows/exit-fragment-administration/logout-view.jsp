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

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<portlet:actionURL var="actionUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<div id="portalFragAdminExit" class="fl-widget">
	<div class="fl-widget-titlebar">
		<h2><spring:message code="fragment.administration"/></h2>
	</div>
	<div class="fl-widget-content">
	  <form name="fragmentAdminExitForm" action="${actionUrl}" method="POST">
	    <c:set var="userHtml"><strong>${fn:escapeXml(remoteUser)}</strong></c:set>
	  	<label for="exitFragment"><spring:message code="you.are.currently.logged.in.as.for.dlm.fragment.administration" arguments="${userHtml}" htmlEscape="false"/></label>
	  	<input class="button" id="exitFragment" type="Submit" value="<spring:message code="exit"/>" name="_eventId_logout"/>
	  </form>
	</div>
</div>
