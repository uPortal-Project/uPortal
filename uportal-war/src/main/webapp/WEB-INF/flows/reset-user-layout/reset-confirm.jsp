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
<portlet:actionURL var="formUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<portlet:actionURL var="userUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="cancel"/>
</portlet:actionURL>
        
<!-- Portlet -->
<div class="fl-widget portlet reset-layout view-result" role="section">

    <!-- Portlet Title -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">
            <a href="${ userUrl }">${ fn:escapeXml(person.name )}</a> >
    	    <spring:message code="reset.user.layout"/>
    	</h2>
    </div> <!-- end: portlet-titlebar -->
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">

        <form action="${formUrl}" method="POST">

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="reset.user.layout"/></h3>
            </div>
            <div class="content">
                <spring:message code="resetting.user.layout.irreversible"/>
		        <spring:message code="are.you.sure.reset.layout" arguments="${person.name}"/>
            </div>
        </div><!-- end: portlet-section -->

        <!-- Portlet Buttons -->
        <div class="buttons">
            <input class="button primary" type="submit" value="<spring:message code="reset.layout"/>" name="_eventId_confirm"/>
            <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
        </div>

        </form>
    
    </div> <!-- end: portlet-content -->

</div> <!-- end:portlet -->