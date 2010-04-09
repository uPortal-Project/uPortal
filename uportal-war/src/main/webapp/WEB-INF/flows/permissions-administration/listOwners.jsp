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

<c:set var="n"><portlet:namespace/></c:set>

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
-->

<!-- Portlet -->
<div class="fl-widget portlet prm-mgr view-listperms" role="section">
  <form id="${n}listPermissionsForm">
  
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">Permissions</h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    
        <c:forEach items="${ owners }" var="owner">
            <div class="${ owner.fname }">
                <h2>
                    <portlet:actionURL var="ownerUrl">
                        <portlet:param name="execution" value="${flowExecutionKey}" />
                        <portlet:param name="_eventId" value="listActivities"/>
                        <portlet:param name="ownerFname" value="${ owner.fname }"/>
                    </portlet:actionURL>
                    <a href="${ ownerUrl }">${ owner.name }</a>
                </h2>
                <p>${ owner.description }</p>
                <p>
                    <c:forEach items="${ owner.activities }" var="activity" varStatus="status">
                        <portlet:actionURL var="activityUrl">
                            <portlet:param name="execution" value="${ flowExecutionKey }"/>
                            <portlet:param name="_eventId" value="showActivity"/>
                            <portlet:param name="ownerFname" value="${ owner.fname }"/>
                            <portlet:param name="activityFname" value="${ activity.fname }"/>
                        </portlet:actionURL>
                        <a href="${ activityUrl }">${ activity.name }</a>${ status.last ? "" : ", " }
                    </c:forEach>
                </p>
            </div>
        </c:forEach>

    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-content -->
  </form>

</div> <!-- end: portlet -->
