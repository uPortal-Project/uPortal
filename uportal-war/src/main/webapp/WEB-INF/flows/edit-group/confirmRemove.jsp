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
<portlet:renderURL var="formUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:renderURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
    
    <!-- Portlet Title -->
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 role="heading">Delete Group</h2>
    </div> <!-- end: portlet-title -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content portlet-body" role="main">
        
    <form:form modelAttribute="channel" action="${submitUrl}" method="POST">

        <!-- Portlet Messages -->
        <spring:hasBindErrors name="group">
            <div class="portlet-msg-error" role="alert">
                <form:errors path="*" element="div"/>
            </div> <!-- end: portlet-msg -->
        </spring:hasBindErrors>
    
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
          <h3 class="portlet-section-header" role="heading"><spring:message code="confirmRemove.heading" arguments="${ channel.name }"/></h3>
          <div class="portlet-section-body">
            <spring:message code="confirmRemove.text" arguments="${ group.name }"/>
          </div>
        </div> <!-- end: portlet-section -->
        
        <!-- Portlet Buttons -->
        <div class="portlet-button-group">
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="confirmRemove.removeButton"/>" name="_eventId_remove"/>
          <input class="portlet-button secondary" type="submit" value="<spring:message code="confirmRemove.cancelButton"/>" name="_eventId_cancel"/>
        </div>
        
        </form:form> <!-- End Form -->
        
    </div>
</div>