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
<portlet:renderURL var="ownersUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="owners"/>
</portlet:renderURL>
<portlet:renderURL var="activitiesUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="activities"/>
</portlet:renderURL>

<portlet:renderURL var="createUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="createPermission"/>
</portlet:renderURL>
<portlet:renderURL var="editUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
</portlet:renderURL>
<portlet:renderURL var="deleteUrl" escapeXml="false"><!-- Actual delete happens on the next screen -->
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="delete"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="principalKey" value="PRINCIPALKEY"/>
  <portlet:param name="principalName" value="PRINCIPALNAME"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="targetName" value="TARGET"/>
  <portlet:param name="permissionType" value="PERMISSIONTYPE"/>
</portlet:renderURL>

<style>
#${n}activityBrowser .dt-search,
#${n}activityBrowser .first.dt-paging-button,
#${n}activityBrowser .last.dt-paging-button {
    display: none;
}
#${n}activityBrowser .dataTables-inline, #${n}activityBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}activityBrowser .view-filter {
    padding-bottom: 15px;
    overflow: hidden;
    margin-bottom: 0;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
}
#${n}activityBrowser .dt-container {
    width: 100%;
}
#${n}activityBrowser .dt-layout-row:has(.dt-paging) {
    background-color: rgb(217, 237, 247);
    border: 1px solid rgb(188, 232, 241);
    border-top: none;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
    padding: 8px 15px;
    margin-bottom: 20px;
}
#${n}activityBrowser .dt-paging .page-link {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
}
#${n}activityBrowser .dt-paging .page-item.active .page-link {
    color: #000;
    background: none;
    border-color: transparent;
}
#${n}activityBrowser table tr td a {
    color: #428BCA;
}
#${n}activityBrowser .dataTables-left {
    float: left;
}
#${n}activityBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}
#${n}activityBrowser .filter-term {
    display: block;
    text-align: bottom;
}
#${n}activityBrowser .dt-length label {
    font-weight: normal;
}
#${n}activityBrowser .datatable-search-view {
    text-align: right;
}
</style>

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
<div id="${n}activityBrowser" class="card portlet prm-mgr" role="section">

  <!-- Portlet Titlebar -->
    <div role="sectionhead" class="card-header titlebar portlet-titlebar">
        <div class="breadcrumb">
            <span class="breadcrumb-item breadcrumb-1"><a href="${ ownersUrl }"><spring:message code="categories"/></a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-item breadcrumb-2"><a href="${ activitiesUrl }">${ fn:escapeXml(owner.name )}</a></span>
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
  <div class="card-body portlet-content">

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
        activityList_configuration.main.table = $("#${n}permissionsTable").DataTable({
            pageLength: activityList_configuration.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            serverSide: false,
            ajax: {
                url: activityList_configuration.url,
                dataSrc: "permissionsList",
                data: {
                    owner: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ owner.fname }</spring:escapeBody>',
                    activity: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ activity.fname }</spring:escapeBody>'
                }
            },
            deferRender: false,
            processing: true,
            autoWidth: false,
            language: {
                lengthMenu: '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>',
                paginator: {
                    previous: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    next: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                }
            },
            columns: [
                { data: 'principalName', width: '30%' },
                { data: 'targetName', width: '30%' },
                { data: 'permissionType', width: '20%' },
                { data: 'targetName', searchable: false, width: '10%' },
                { data: 'targetName', searchable: false, width: '10%' }
            ],
            initComplete: function (settings) {
                activityList_configuration.main.table.draw();
            },
            rowCallback: function (row, data, displayNum, displayIndex, dataIndex) {
                $('td:eq(3)', row).html( getEditAnchorTag(data.owner, data.activity, data.target) );
                $('td:eq(4)', row).html( getDeleteAnchorTag(data.owner,
                                                             data.principalName,
                                                             data.principalKey,
                                                             data.activity,
                                                             data.target,
                                                             data.permissionType) );
            },
            layout: {
                topStart: null,
                topEnd: null,
                top: function() {
                    var div = document.createElement('div');
                    div.className = 'row alert alert-info view-filter';
                    return div;
                },
                top2Start: 'paging',
                top2: 'info',
                top2End: 'pageLength',
                bottomStart: null,
                bottom: null,
                bottomEnd: null
            },
            searchPanes: {
                columns: [0, 1, 2],
                cascadePanes: true,
                viewTotal: true,
                layout: 'columns-3'
            }
        });
    };

    initializeTable();
    $("div.toolbar-filter").html('<b><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></b>:');
});
</script>
