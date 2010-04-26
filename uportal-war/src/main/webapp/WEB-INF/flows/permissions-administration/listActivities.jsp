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
<portlet:actionURL var="backUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="back"/>
</portlet:actionURL>

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
  
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
    <a href="${ backUrl }">Permissions</a> > 
    <div class="permission-owner ${ owner.fname }">
        <h2 class="title" role="heading">
            ${ owner.name }
        </h2>
        <h3>${ owner.description }</h3>
    </div>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
        <div class="titlebar">
            <h3 class="title" role="heading">
                Permissions within ${ owner.name }
            </h3>   
        </div>

        <table class="portlet-table" style="width:100%">    
            <tr>
                <th>Name</th>
                <th>System ID</th>
                <th>Description</th>
            </tr>
            <c:forEach items="${ owner.activities }" var="activity">
                <tr>
                    <td>
                        <portlet:actionURL var="activityUrl">
                            <portlet:param name="execution" value="${flowExecutionKey}" />
                            <portlet:param name="_eventId" value="showActivity"/>
                            <portlet:param name="activityFname" value="${ activity.fname }"/>
                        </portlet:actionURL>
                        <a href="${ activityUrl }">${ activity.name }</a>
                    </td>
                    <td>${ activity.fname }</td>
                    <td>${ activity.description }</td>
                </tr>
            </c:forEach>
        </table>

    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->
