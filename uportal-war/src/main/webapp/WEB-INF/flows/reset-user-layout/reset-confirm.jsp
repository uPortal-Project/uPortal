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

        <div class="portlet-msg-alert" role="alert">
            <h3>Caution!</h3>
            <p>Resetting a customer's layout is irreversible.</p>
        </div>

        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
          <div class="portlet-section-body">
            <p>Are you sure you want to reset the layout for <c:out value="${person}"/>?</p>
          </div>
        </div> <!-- end: portlet-section -->
        
        <form action="${formUrl}" method="POST">
        <div class="portlet-button-group">
            <input class="portlet-button portlet-button-primary" type="submit" value="Reset Layout" name="_eventId_confirm"/>
            <input class="portlet-button secondary" type="submit" value="Cancel" name="_eventId_cancel"/>
        </div>
        </form>
      </div>
    </div>
  </div>
  
</div>