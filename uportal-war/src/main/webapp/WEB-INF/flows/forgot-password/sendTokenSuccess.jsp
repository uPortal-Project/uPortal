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

<portlet:actionURL var="loginUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="finish"/>
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead" data-role="header">
        <h2 class="title" role="heading"><spring:message code="reset.email.sent"/></h2>
    </div>

    <div class="fl-widget-content content portlet-content" role="main" data-role="content">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">

      <div class="portlet-section-body">
          <p><spring:message code="password.reset.instructions.have.been.sent.to.your.email.address"/></p>
          
          <p><a href="${ loginUrl }"><spring:message code="return.to.log.in.form"/></a></p>
      </div>

    </div>
    
  </div>

</div>