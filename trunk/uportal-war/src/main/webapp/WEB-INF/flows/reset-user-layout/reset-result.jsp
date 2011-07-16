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
        
<!-- Portlet -->
<div class="fl-widget portlet reset-layout view-result" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading"><spring:message code="reset.user.layout"/></h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
    
    <!-- Messages --> 
    <div class="portlet-msg-success portlet-msg success" role="status">
    	<div class="titlebar">
            <h3 class="title"><spring:message code="success"/></h3>
        </div>
        <div class="content">
            <p><spring:message code="layout.for.name.has.been.reset" arguments="${person.name}"/></p>
        </div>
    </div>
    
    <!-- Buttons -->
    <form action="${formUrl}" method="POST">
        <div class="buttons">
        	<input class="button primary" type="submit" value="<spring:message code="continue"/>" name="_eventId_continue"/>
        </div>
    </form>
    
    </div> <!-- end: portlet-content -->
</div> <!-- end:portlet -->