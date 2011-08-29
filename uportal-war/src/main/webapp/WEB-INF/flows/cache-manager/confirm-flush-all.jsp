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
<div class="fl-widget portlet cache-mgr view-flushall" role="section">
	<!-- Portlet Titlebar -->
	<div class="fl-widget-titlebar titlebar portlet-titlebar">
    	<h2 class="title"><spring:message code="flush.cache"/></h2>
    </div>
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <!-- Messages -->
        <div class="portlet-msg-alert portlet-msg alert" role="alert">
        	<h3><spring:message code="warning"/></h3>
        	<p><spring:message code="emptying.caches.will.degrade.performance.use.caution.may.take.several.minutes"/></p>
        </div>
        
        <!-- Note -->
        <div class="note" role="note">
        	<p><spring:message code="are.you.sure.remove.all.caches"/></p>
        </div>
        
        <!-- Buttons -->
        <form action="${flushUrl}" method="POST">
            <div class="buttons">
            	<input class="button primary" type="submit" value="<spring:message code="empty.all.caches"/>" name="_eventId_confirm"/>
            	<input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
            </div>
        </form>
    
    </div>
</div>