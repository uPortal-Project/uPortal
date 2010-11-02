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

<!-- Portlet -->
<div id="portalFragAdminList" class="fl-widget portlet snooper view-main" role="section">

	
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading"><spring:message code="fragment.administration"/></h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
        
        <portlet:actionURL var="formUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="selectFragment"/>
        </portlet:actionURL>
        <form method="post" name="fragmentAdminForm" action="${formUrl}">
            <select id="fragmentOwner" name="impersonateUser" title="<spring:message code="choose.fragment.to.edit"/>">
            	<option value="NONE"> -- <spring:message code="fragments"/> -- </option>
                <c:forEach items="${fragments}" var="item">
                	<option value="${fn:escapeXml(item.key)}">${fn:escapeXml(item.value)}</option>
                </c:forEach>
            </select>
            <%-- onclick="if (document.fragmentAdminForm.fragmentOwner.options[document.fragmentAdminForm.fragmentOwner.selectedIndex].value != 'NONE') document.fragmentAdminForm.submit()" --%>
            <input class="button" type="submit" value="<spring:message code="go"/>" />
        </form>

	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->