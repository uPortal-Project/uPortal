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
<div class="fl-widget portlet grp-mgr view-confirmremove" role="section">
    
    <!-- Portlet Title -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="delete.group"/></h2>
    </div> <!-- end: portlet-title -->
    
    <!-- Portlet Body -->
    <div class="fl-widget-content content portlet-content" role="main">
        
    <form action="${formUrl}" method="POST">

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
          <div class="titlebar">
              <h3 class="title" role="heading"><spring:message code="delete.group.name" arguments="${ group.name }"/></h3>
          </div>
          <div class="content">
            <spring:message code="delete.group.confirmation" arguments="${ group.name }"/>
          </div>
        </div> <!-- end: portlet-section -->
        
        <!-- Portlet Buttons -->
        <div class="buttons">
          <input class="button primary" type="submit" value="<spring:message code="remove"/>" name="_eventId_removeGroup"/>
          <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
        </div>
        
        </form> <!-- End Form -->
        
    </div>
</div>