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
<div class="fl-widget portlet" role="section">
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">   

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
          <div class="portlet-section-body">
            <p><spring:message code="reset.my.layout.description"/></p>
            <p><em><spring:message code="reset.my.layout.note"/></em></p>
          </div>
        </div> <!-- end: portlet-section -->
        
        <form action="${formUrl}" method="POST">
        <div class="portlet-button-group">
            <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="reset.now"/>" name="_eventId_reset"/>
        </div>
        </form>
      </div>
    </div>
  </div>
  
</div>
