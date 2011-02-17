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
<portlet:actionURL var="formUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
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
<div class="fl-widget portlet prm-mgr view-editperm" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<div class="breadcrumb">
            <c:forEach items="${ breadcrumbs }" var="breadcrumb" varStatus="status">
                <portlet:actionURL var="breadcrumbUrl">
                  <portlet:param name="execution" value="${flowExecutionKey}" />
                  <portlet:param name="_eventId" value="breadcrumb"/>
                  <portlet:param name="breadcrumb" value="${ breadcrumb.key }"/>
                </portlet:actionURL>
                <span class="breadcrumb-${ status.index + 1 }">
                    <a href="${ breadcrumbUrl }">${ fn:escapeXml(breadcrumb.value )}</a>
                </span>
                <span class="separator">&gt; </span>
            </c:forEach>
        </div>
        <h2 class="title" role="heading">
            <c:set var="message"><span class="name"><spring:escapeBody htmlEscape="true">${ fn:escapeXml(permissionDefinition.activity.name )}</spring:escapeBody></span></c:set>
            <spring:message code="edit.assignment.for.name" arguments="${ message }" htmlEscape="false"/>
        </h2>
        <h3 class="subtitle">
            <c:set var="message"><span class="name"><spring:escapeBody htmlEscape="true">${ fn:escapeXml(permissionDefinition.target.name )}</spring:escapeBody></span></c:set>
            <spring:message code="with.target.name" arguments="${ message }" htmlEscape="false"/>
        </h3>
        <div class="toolbar">
        	<ul>
                <li><a href="${ choosePrinicipalsUrl }" class="button"><spring:message code="choose.principals"/></a></li>
            </ul>
        </div>
    </div>

    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">

            <form:form method="POST" id="${n}editPermissionForm" action="${ formUrl }" commandName="permissionDefinition">

                <form:errors path="*"/>  
                
                <ul id="assignments"></ul>
  
            </form:form>
    
    </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var allAssignments;

    var inheritRegex = /INHERIT_/;
    var addAssignments = function(assignments, list) {
        $(assignments).each(function(idx, assignment) {
            
            var type = assignment.type;
            if (type.match(inheritRegex)) {
                type = "INHERIT";
            }
            
            var inheritText;
            if (assignment.type == 'INHERIT_GRANT') {
                inheritText = "Inherit (Grant)";
            } else if (assignment.type == "INHERIT_DENY") {
                inheritText = "Inherit (Deny)";
            } else {
                inheritText = "Inherit";
            }
            
            var li = $(document.createElement("li"));
            var span = $(document.createElement("span")).addClass("assignment-wrapper")
                .addClass(assignment.type.toLowerCase())
                .append($(document.createElement("span")).addClass("principal-name").text(assignment.principal.name + " "));
            var select = $(document.createElement("select"));
            var html = "<option value=\"INHERIT\">" + inheritText + "</option>";
            html += "<option value=\"GRANT\">Grant</option>";
            html += "<option value=\"DENY\">Deny</option></select></span>";
            select.html(html).val(type);
            span.append(select);
            li.append(span);

            if (assignment.children.length > 0) {
                var ul = $(document.createElement("ul"));
                li.append(ul);
                addAssignments(assignment.children, ul);
            }
            list.append(li);

        });
    };
    
    $(document).ready(function(){
        $.get(
            "<c:url value="/api/permissionAssignmentMap"/>", 
            { 
                owner: '${ permissionDefinition.owner.fname }',
                activity: '${ permissionDefinition.activity.fname }',
                target: '${ permissionDefinition.target.key }',
                principals: [<c:forEach items="${ principals }" var="principal" varStatus="status">'${ principal.principalString }'${ status.last ? '' : ', '}</c:forEach>],
                
            },
            function(data) {
                allAssignments = data.assignments;
                addAssignments(allAssignments, $("#assignments"));
            },
            "json"
        );
    });


});
</script>
