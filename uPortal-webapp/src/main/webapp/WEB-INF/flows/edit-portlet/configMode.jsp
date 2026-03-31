<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="navigationUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and Bootstrap)
| and more, refer to:
| docs/SKINNING_UPORTAL.md
-->

<!-- Portlet -->
<div class="card portlet ptl-mgr view-configmode" role="section">

  <!-- Portlet Titlebar -->
  <div class="card-header titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">
      <spring:message code="edit.portlet.configuration"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->

  <!-- Portlet Content -->
  <div class="card-body content portlet-content">
    <up:render-delegate fname="${PORTLET_FNAME}" portletMode="CONFIG" windowState="maximized">
        <up:parent-url>
            <up:param name="execution" value="${flowExecutionKey}"/>
            <up:param name="_eventId" value="configModeAction"/>
        </up:parent-url>
    </up:render-delegate>

    <!-- Configuration Navigation Buttons -->
    <div class="buttons config-mode-buttons">
        <form action="${navigationUrl}" method="post">
            <input type="hidden" name="execution" value="${flowExecutionKey}"/>
            <input class="button btn btn-primary" type="submit" value="<spring:message code='save'/>" name="_eventId_update"/>
            <input class="button btn btn-link" type="submit" value="<spring:message code='return.without.saving'/>" name="_eventId_cancel"/>
        </form>
    </div>

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->
