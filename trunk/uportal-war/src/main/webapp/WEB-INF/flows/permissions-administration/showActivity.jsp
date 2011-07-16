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
<portlet:actionURL var="ownersUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="owners"/>
</portlet:actionURL>
<portlet:actionURL var="activitiesUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="activities"/>
</portlet:actionURL>

<portlet:actionURL var="createUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="createPermission"/>
</portlet:actionURL>
<portlet:actionURL var="editUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
</portlet:actionURL>
<portlet:actionURL var="deleteUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="deletePermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="principalType" value="PRINCIPALTYPE"/>
  <portlet:param name="principalName" value="PRINCIPALNAME"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
  <portlet:param name="permissionType" value="PERMISSIONTYPE"/>
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
<div class="fl-widget portlet prm-mgr" role="section">
  
  <!-- Portlet Titlebar -->
	<div role="sectionhead" class="fl-widget-titlebar titlebar portlet-titlebar">
    	<div class="breadcrumb">
        	<span class="breadcrumb-1"><a href="${ ownersUrl }"><spring:message code="categories"/></a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-2"><a href="${ activitiesUrl }">${ fn:escapeXml(owner.name )}</a></span>
            <span class="separator">&gt; </span>
            
        </div>
        <h2 class="title" role="heading"><spring:message code="assignments.for"/> <span class="name">${ fn:escapeXml(activity.name )}</span></h2>
        <h3 class="subtitle">${ fn:escapeXml(activity.description )}</h3>
        <div role="toolbar" class="toolbar">
            <ul>
                <li><a href="${ createUrl }" class="button"><spring:message code="add.assignment"/></a></li>
            </ul>
        </div>         
    </div>
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}permissionAddingTabs" class="portlet-section" role="region">
        <div class="view-pager flc-pager-top">
            <ul id="pager-top" class="fl-pager-ui">
                <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="previous"/></a></li>
                <li>
                     <ul class="flc-pager-links demo-pager-links" style="margin:0; display:inline">
                         <li class="flc-pager-pageLink"><a href="#">1</a></li>
                         <li class="flc-pager-pageLink-skip">...</li>
                         <li class="flc-pager-pageLink"><a href="#">2</a></li>
                     </ul>
                </li>
                <li class="flc-pager-next"><a href="#"><spring:message code="next"/> &gt;</a></li>
                <li>
                    <span class="flc-pager-summary"><spring:message code="listPermissions.pagerPerPagePrefix"/></span>
                    <span> <select class="pager-page-size flc-pager-page-size">
                        <option value="5">5</option>
                        <option value="10">10</option>
                        <option value="20">20</option>
                        <option value="50">50</option>
                    </select></span> <spring:message code="per.page"/>
                </li>
            </ul>
        </div>
        <table class="portlet-table" id="${n}permissionsTable" summary="" xmlns:rsf="http://ponder.org.uk" style="width:100%;" title="Assignments of this permission">
            <thead>
                <tr rsf:id="header:">
                    <th id="${n}permissionPrincipal" class="flc-pager-sort-header"><a rsf:id="permissionPrincipal" title="Click to sort" href="javascript:;"><spring:message code="principal"/></a></th>
                    <th id="${n}permissionTarget" class="flc-pager-sort-header"><a rsf:id="permissionTarget" title="Click to sort" href="javascript:;"><spring:message code="target"/></a></th>
                    <th id="${n}permissionType" class="flc-pager-sort-header"><a rsf:id="permissionType" title="Click to sort" href="javascript:;"><spring:message code="grant.deny"/></a></th>
                    <th id="${n}permissionEdit" rsf:id="permissionEdit"><spring:message code="edit"/></th>
                    <th id="${n}permissionDelete" rsf:id="permissionDelete"><spring:message code="delete"/></th>
                </tr>
            </thead>
            <tbody id="${n}permissionsBody">
                <tr rsf:id="row:">
                    <td headers="${n}permissionPrincipal"><span rsf:id="permissionPrincipal"></span></td>
                    <td headers="${n}permissionTarget"><span rsf:id="permissionTarget"></span></td>
                    <td headers="${n}permissionType"><span rsf:id="permissionType"></span></td>
                    <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                    <td headers="${n}permissionDelete"><a href="" rsf:id="permissionDelete"></a></td>
                </tr>
            </tbody>
        </table>

    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var pager;
    var editUrl = "${editUrl}";
    var deleteUrl = "${deleteUrl}";

    var getPermissions = function() {
        var rslt;
        $.ajax({
             url: "<c:url value="/api/permissionAssignments"/>",
             async: false,
             data: { 
                 owner: '<spring:escapeBody javaScriptEscape="true">${ owner.fname }</spring:escapeBody>', 
                 activity: '<spring:escapeBody javaScriptEscape="true">${ activity.fname }</spring:escapeBody>' 
             },
             dataType: "json",
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus + " : " + errorThrown);
             },
             success: function(data) {
                rslt = data.permissionsList;
             }
        });
        return rslt;
    };

    // Initialize the pager
    var options = {
        dataModel: getPermissions(),
        annotateColumnRange: 'permissionPrincipal',
        columnDefs: [
            { key: "permissionPrincipal", valuebinding: "*.principalName", sortable: true },
            { key: "permissionTarget", valuebinding: "*.targetName", sortable: true },
            { key: "permissionType", valuebinding: "*.permissionType", sortable: true },
            { key: "permissionEdit", valuebinding: "*.owner",
                components: {
                    target: editUrl.replace("OWNER", '${"${*.owner}"}')
                                    .replace("ACTIVITY", '${"${*.activity}"}')
                                    .replace("TARGET", '${"${*.target}"}'),
                    linktext: "<spring:message code="edit"/>"
                }
            },
            { key: "permissionDelete", valuebinding: "*.owner",
                components: {
                    target: deleteUrl.replace("OWNER", escape('${"${*.owner}"}'))
                                    .replace("PRINCIPALTYPE", escape('${"${*.principalType}"}'))
                                    .replace("PRINCIPALKEY", escape('${"${*.principalKey}"}'))
                                    .replace("ACTIVITY", escape('${"${*.activity}"}'))
                                    .replace("TARGET", escape('${"${*.target}"}'))
                                    .replace("PERMISSIONTYPE", escape('${"${*.permissionType}"}')),
                    linktext: "<spring:message code="delete"/>"
                }
            }
        ],
        bodyRenderer: {
          type: "fluid.pager.selfRender",
          options: {
              selectors: {
                 root: "#${n}permissionsTable"
              },
              row: "row:"
            }
            
        },
        pagerBar: {type: "fluid.pager.pagerBar", options: {
          pageList: {type: "fluid.pager.renderedPageList",
            options: { 
              linkBody: "a"
            }
          }
        }}
    };

    $(document).ready(function(){
        var pager = up.fluid.pager("#${n}permissionAddingTabs", options);
    });
    
});
</script>
