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

<portlet:actionURL var="choosePrinicipalsUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="choosePrinicipals"/>
</portlet:actionURL>
<portlet:actionURL var="groupTargetUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="groupTarget"/>
</portlet:actionURL>
<portlet:actionURL var="portletTargetUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="portletTarget"/>
</portlet:actionURL>
<portlet:actionURL var="cancelUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="cancel"/>
</portlet:actionURL>
<portlet:actionURL var="submitUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="submit"/>
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
<div class="fl-widget portlet" role="section">
  <form method="POST" id="${n}editPermissionForm">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="editPermission.title"/></h2>
  </div> <!-- end: portlet-title -->
  
  <div id="${n}errors" role="alert" class="portlet-msg-error" style="display: none">
    <ul>
      <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="msg">
        <li><c:out value="${msg.text}"/></li>
      </c:forEach>
    </ul>
  </div>
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <!-- General Configuration Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="editPermission.createPermissionHeader"/></h3>
      <div class="portlet-section-body">
      
        <table summary="<spring:message code="editPermission.permissionTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="editPermission.itemHeading"/></th>
              <th><spring:message code="editPermission.valueHeading"/></th>
            <tr>
          </thead>
          <tbody>
            <tr>
              <td class="fl-text-align-right"><spring:message code="editPermission.ownerLabel"/></td>
              <td><input id="${n}owner" name="owner" type="text" style="outline-color: -moz-use-text-color; outline-style: none; outline-width: medium;"></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="editPermission.principalsLabel"/></td>
              <td>
                <ul>
                    <c:forEach var="root" items="${permissionDefinition.assignments}">
                        <c:set var="assignment" value="${root}" scope="request"/>
                        <c:import url="/WEB-INF/flows/edit-permission/principal.jsp"/>
                    </c:forEach>
                </ul>
                <a id="${n}principal" href="javascript: void(0);"><spring:message code="editPermission.clickToAdd"/></a>
              </td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="editPermission.activityLabel"/></td>
              <td><input id="${n}activity" name="activity" type="text" style="outline-color: -moz-use-text-color; outline-style: none; outline-width: medium;"></td>
            </tr>
            <tr>
              <td class="fl-text-align-right"><spring:message code="editPermission.targetLabel"/></td>
              <td>
                <p><input id="${n}target" name="target" type="text" style="outline-color: -moz-use-text-color; outline-style: none; outline-width: medium;"></p>
                <a id="${n}groupTarget" href="javascript: void(0);"><spring:message code="editPermission.selectGroupLink"/></a>
                <a id="${n}portletTarget" href="javascript: void(0);"><spring:message code="editPermission.selectPortletLink"/></a>
              </td>
            </tr>
          </tbody>
        </table>
        
      </div>
    </div>
    <!-- END: General Configuration Section -->
    
  </div> <!-- end: portlet-body -->
  
  <!-- Portlet Toolbar -->
  <div class="fl-col-flex2" role="toolbar">
    <div class="fl-col">
      <ul>
        <li><a href="${cancelUrl}" title="<spring:message code="editPermission.cancelButton"/>"><span><spring:message code="editPermission.cancelButton"/></span></a></li>
        <li><a id="${n}submit" href="javascript: void(0);" title="<spring:message code="editPermission.submitButton"/>"><span><spring:message code="editPermission.submitButton"/></span></a></li>
      </ul>
    </div>
  </div> <!-- end: portlet-toolbar -->

  </form>
</div> <!-- end: portlet -->

<script type="text/javascript">

up.jQuery(function() {

    var $ = up.jQuery;

    // Suggest Boxes
    var owner = "<c:out value="${permissionDefinition.owner}"/>" 
                    ? [{name: "<c:out value="${permissionDefinition.owner}"/>", id: "<c:out value="${permissionDefinition.owner}"/>"}]
                    : [];
    var ownerOptions = {
        prePopulate: owner,
        tokenLimit: 1,
        onResult: function(results) {
            return results.suggestions;
        }
    };
    $("#${n}owner").tokenInput("<c:out value="${renderRequest.contextPath}"/>/mvc/permissionsOwnerSuggest", ownerOptions);

    var activity = "<c:out value="${permissionDefinition.activity}"/>" 
                    ? [{name: "<c:out value="${permissionDefinition.activity}"/>", id: "<c:out value="${permissionDefinition.activity}"/>"}]
                    : [];
    var activityOptions = {
        prePopulate: activity,
        tokenLimit: 1,
        onResult: function(results) {
            return results.suggestions;
        }
    };
    $("#${n}activity").tokenInput("<c:out value="${renderRequest.contextPath}"/>/mvc/permissionsActivitySuggest", activityOptions);

    var target = "<c:out value="${permissionDefinition.target}"/>" 
                    ? [{name: "<c:out value="${permissionDefinition.target}"/>", id: "<c:out value="${permissionDefinition.target}"/>"}]
                    : [];
    var targetOptions = {
        prePopulate: target,
        tokenLimit: 1,
        onResult: function(results) {
            return results.suggestions;
        }
    };
    $("#${n}target").tokenInput("<c:out value="${renderRequest.contextPath}"/>/mvc/permissionsTargetSuggest", targetOptions);

    // Transitions
    $("#${n}principal").click(function() {
        $("#${n}editPermissionForm")
            .attr("action", "<c:out value="${choosePrinicipalsUrl}" escapeXml="false"/>")
            .submit();
    });
    $("#${n}groupTarget").click(function() {
        $("#${n}editPermissionForm")
            .attr("action", "<c:out value="${groupTargetUrl}" escapeXml="false"/>")
            .submit();
    });
    $("#${n}portletTarget").click(function() {
        $("#${n}editPermissionForm")
            .attr("action", "<c:out value="${portletTargetUrl}" escapeXml="false"/>")
            .submit();
    });
    $("#${n}submit").click(function() {
        <c:if test="${permissionDefinition.mode == 'UPDATE'}">if (!confirm('These entries will REPLACE the previous entreis.  Is that what you want to do?')) return;</c:if>
        $("#${n}editPermissionForm")
            .attr("action", "<c:out value="${submitUrl}" escapeXml="false"/>")
            .submit();
    });
    
    <c:if test="${not empty flowRequestContext.messageContext.allMessages}">
    $("#${n}errors").slideDown(1000);
    </c:if>

});

</script>

