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
        ${ permissionDefinition.owner.name } > 
        <h2 class="title" role="heading">${ permissionDefinition.activity.name }</h2>
        <h3 class="subtitle">${ permissionDefinition.activity.description }</h3>
    </div>

    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">
        
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading">
                    Target: ${ permissionDefinition.target.name }
                </h3>   
            </div>
        </div>
    
        <!-- Portlet Section -->
        <div class="portlet-section" role="region">
        
            <div class="titlebar">
                <h3 class="title" role="heading">Assignments</h3>
            </div>
            
            <div>
                <ul id="assignments"></ul>
            </div>
            
            <p>
                <a href="${ choosePrinicipalsUrl }">Choose Principals</a>
            </p>

            <form:form method="POST" id="${n}editPermissionForm" action="${ formUrl }" commandName="permissionDefinition">

                <form:errors path="*"/>      
  
                <div id="${n}inputs" style="display:none">
                </div>
  
                <!-- Buttons -->
                <div class="buttons">
                    <input class="button primary" type="submit" value="<spring:message code="editPermission.submitButton"/>" name="_eventId_submit"/>
                    <input class="button" type="submit" value="<spring:message code="editPermission.cancelButton"/>" name="_eventId_cancel"/>
                </div> <!-- end: buttons -->
                
            </form:form>        
            
        </div>
      
    </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var allAssignments;
    var permissions = { <c:forEach items="${ permissionDefinition.permissions }" var="permission" varStatus="status">'${ permission.key }': '${ permission.value }'${ status.last ? '' : ',' }</c:forEach> };

    var addAssignments = function(assignments, list) {
        $(assignments).each(function(idx, assignment) {
            var li = $(document.createElement("li"));
            var html = assignment.principal.name;
            html += " <select id=\"permissions'" + assignment.principalId + "'\" name=\"permissions['" + assignment.principalId + "']\"><option value=\"INHERIT\">INHERIT</option><option value=\"GRANT\">GRANT</option><option value=\"DENY\">DENY</option></select>";
            li.html(html);
            li.find("select")
                .val(assignment.type).attr("principal", assignment.principalId)
                .change(function(){ 
                    $("#${n}inputs").find("input[principal=" + $(this).attr("principal") + "]").val( $(this).val() );
                    $("#assignments").find("select[principal=" + $(this).attr("principal") + "]").val( $(this).val() );
                });
            if (assignment.children.length > 0) {
                var ul = $(document.createElement("ul"));
                li.append(ul);
                addAssignments(assignment.children, ul);
            }
            list.append(li);

            var input = $("#${n}inputs").find("input[principal=" + assignment.principalId + "]");
            if (input.size() > 0) {
                $(input).val(assignment.type);
            } else {
                $("#${n}inputs").append(
                    $(document.createElement("input"))
                        .attr("name", "permissions['" + assignment.principalId + "']")
                        .attr("principal", assignment.principalId)
                        .val(assignment.type)
                );
            }
        });
    };
    
    $(document).ready(function(){
        $.get(
            "mvc/permissionAssignmentMap", 
            { permissions: JSON.stringify(permissions) },
            function(data) {
                allAssignments = data.assignments;
                addAssignments(allAssignments, $("#assignments"));
            },
            "json"
        );
    });


});
</script>
