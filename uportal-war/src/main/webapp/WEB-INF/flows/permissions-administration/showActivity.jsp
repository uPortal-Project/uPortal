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
  <portlet:param name="_eventId" value="delete"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="principalKey" value="PRINCIPALKEY"/>
  <portlet:param name="principalName" value="PRINCIPALNAME"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="targetName" value="TARGET"/>
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
<div id="${n}activityBrowser" class="portlet prm-mgr" role="section">
  
  <!-- Portlet Titlebar -->
    <div role="sectionhead" class="titlebar portlet-titlebar">
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
                <li><a href="${ createUrl }" class="button btn"><spring:message code="add.assignment"/></a></li>
            </ul>
        </div>
    </div>
  
  <!-- Portlet Content -->
  <div class="portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}permissionAddingTabs" class="portlet-section" role="region">
        <table class="portlet-table table table-hover" id="${n}permissionsTable" title='<spring:message code="assignments.of.this.permission"/>'>
            <thead>
                <tr>
                    <th><spring:message code="principal"/></th>
                    <th><spring:message code="target"/></th>
                    <th><spring:message code="grant.deny"/></th>
                    <th><spring:message code="edit"/></th>
                    <th><spring:message code="delete"/></th>
                </tr>
            </thead>
        </table>
    </div> <!-- end: portlet-section -->
  </div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var activityList_configuration = {
        column: {
            principal: 0,
            target: 1,
            grantDeny: 2,
            placeHolderForEditLink: 3,
            placeHolderForDeleteLink: 4
        },
        main: {
            table : null,
            pageSize: 10
        },
        url: "<c:url value="/api/permissionAssignments"/>"
    };

    // edit anchor tag generating helper function
    var getEditAnchorTag = function(owner, activity, target) {
        var url = "${editUrl}".replace("OWNER", owner)
                              .replace("ACTIVITY", activity)
                              .replace("TARGET", target);
        return '<a href="' + url + '"><spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/></a>';
    };

    // delete anchor tag generating helper function
    var getDeleteAnchorTag = function(owner, principalName, principalKey, activity, target, permissionType) {
        var url = "${deleteUrl}".replace("OWNER", owner)
                                .replace("PRINCIPALNAME", principalName)
                                .replace("PRINCIPALKEY", principalKey)
                                .replace("ACTIVITY", activity)
                                .replace("TARGET", target)
                                .replace("PERMISSIONTYPE", permissionType);
        return '<a href="' + url + '"><spring:message code="delete" htmlEscape="false" javaScriptEscape="true"/></a>';
    };

    var initializeTable = function() {
        activityList_configuration.main.table = $("#${n}permissionsTable").dataTable({
        	pageLength: activityList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	ajax: {
        		url: activityList_configuration.url,
        		dataSrc: "permissionsList",
        		data: { 
                    owner: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ owner.fname }</spring:escapeBody>', 
                    activity: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ activity.fname }</spring:escapeBody>' 
                }
        	},
        	processing: true,
        	autoWidth: false,
        	pagingType: 'full_numbers',
        	language: {
            	info: '<spring:message code="datatables.info.message" htmlEscape="false" javaScriptEscape="true"/>',
            	lengthMenu: '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>',
            	search: '<spring:message code="datatables.search" htmlEscape="false" javaScriptEscape="true"/>',
            	paginate: {
            		first: '<spring:message code="datatables.paginate.first" htmlEscape="false" javaScriptEscape="true"/>',
                    last: '<spring:message code="datatables.paginate.last" htmlEscape="false" javaScriptEscape="true"/>',
                    previous: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    next: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                }
            },
            columns: [
                { 	data: 'principalName', type: 'html', width: '30%' }, // Owner
                { 	data: 'targetName', type: 'html', width: '30%'}, // Principal
                { 	data: 'permissionType', type: 'html', width: '20%' }, // Activity
                { 	data: 'targetName', type: 'html', searchable: false, width: '10%',
                	render: function ( data, type, row, meta ) {
                		return getEditAnchorTag(row.owner, row.activity, row.target);
                	}
                }, // Target
                { 	data: 'targetName', type: 'html', searchable: false, width: '10%',
                	render: function ( data, type, row, meta ) {
                		return getDeleteAnchorTag(row.owner,
                				row.principalName,
                				row.principalKey,
                				row.activity,
                				row.target,
                				row.permissionType);
                	}
                } // Edit Link
            ],
            initComplete: function (oSettings) {
            	$(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
                activityList_configuration.main.table.fnDraw();
            },           
            // Setting the top and bottom controls
            dom: 	"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',', // Used for multivalue column Categories
                aiExclude: [activityList_configuration.column.placeHolderForEditLink,
                            activityList_configuration.column.placeHolderForDeleteLink]
            },
            responsive: true
        });
    };

    initializeTable();
});
</script>
