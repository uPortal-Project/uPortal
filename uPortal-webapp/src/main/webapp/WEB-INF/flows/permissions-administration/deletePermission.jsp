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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="n"><portlet:namespace/></c:set>

<portlet:renderURL var="removeUrl"><!-- Removal uses the REST API;  this URL is merely the page that appears after -->
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="remove"/>
</portlet:renderURL>

<portlet:renderURL var="cancelUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="cancel"/>
</portlet:renderURL>

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| docs/SKINNING_UPORTAL.md
-->

<!-- Portlet -->
<div class="fl-widget portlet ptl-mgr view-confirmremove" role="section">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">

    <div class="breadcrumb">
        <c:forEach items="${ breadcrumbs }" var="breadcrumb" varStatus="status">
            <portlet:renderURL var="breadcrumbUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="${ breadcrumb.key }"/>
            </portlet:renderURL>
            <span class="breadcrumb-${ status.index + 1 }">
                <a href="${ breadcrumbUrl }">${ fn:escapeXml(breadcrumb.value )}</a>
            </span>
            <span class="separator">&gt; </span>
        </c:forEach>
    </div>

    <h2 class="title" role="heading">
      <spring:message code="remove.permission"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->

  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">

    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
         <h3class="title" role="heading"><spring:message code="remove.permission.confirmation" arguments="${permissionType},${ activity.name },  ${target.name }, ${principalName}"/></h3>

      </div>
    </div> <!-- end: portlet-section -->

    <div class="buttons">
      <a href="${removeUrl}" class="button btn btn-default" name="_eventId_remove" id="${n}deletePermissionButton"><spring:message code="remove"/></a>
      <a href="${cancelUrl}" class="button btn btn-default"><spring:message code="cancel"/></a>
    </div>


    </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    $(document).ready(function(){
        $('#${n}deletePermissionButton').click(function(e){
            e.preventDefault();
            $.ajax({
                url: "<c:url value="/api/deletePermission"/>",
                type: 'POST',
                data: {
                    principal: '${principalKey}',
                    owner: '${ owner.fname }',
                    activity: '${ activity.fname }',
                    target: '${ targetName }'
                },
                success: function(){
                    window.location = $('#${n}deletePermissionButton').attr('href');
                },
                error: function(){
                    alert('Delete Failed.  Please check logs.');
                }
            });
        });
    });

});
</script>
