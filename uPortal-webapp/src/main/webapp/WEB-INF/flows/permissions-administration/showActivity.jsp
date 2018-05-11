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
#${n}activityBrowser .dataTables_filter, #${n}activityBrowser .first.paginate_button, #${n}activityBrowser .last.paginate_button{
    display: none;
}
#${n}activityBrowser .dataTables-inline, #${n}activityBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}activityBrowser .dataTables_wrapper {
    width: 100%;
}
#${n}activityBrowser .dataTables_paginate .paginate_button {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    *cursor: hand;
}
#${n}activityBrowser .dataTables_paginate .paginate_active {
    margin: 2px;
    color:#000;
}

#${n}activityBrowser .dataTables_paginate .paginate_active:hover {
    text-decoration: line-through;
}

#${n}activityBrowser table tr td a {
    color: #428BCA;
}

#${n}activityBrowser .dataTables-left {
    float:left;
}

#${n}activityBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}

#${n}activityBrowser .filter-term {
    display: block;
    text-align:bottom;
}

#${n}activityBrowser .dataTables_length label {
    font-weight: normal;
}
#${n}activityBrowser .datatable-search-view {
    text-align:right;
}
</style>

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
<div id="${n}activityBrowser" class="fl-widget portlet prm-mgr" role="section">

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
                <li><a href="${ createUrl }" class="button btn"><spring:message code="add.assignment"/></a></li>
            </ul>
        </div>
    </div>

  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content">

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
            iDisplayLength: activityList_configuration.main.pageSize,
            aLengthMenu: [5, 10, 20, 50],
            bServerSide: false,
            sAjaxSource: activityList_configuration.url,
            sAjaxDataProp: "permissionsList",
            bDeferRender: false,
            bProcessing: true,
            bAutoWidth:false,
            sPaginationType: 'full_numbers',
            oLanguage: {
                sLengthMenu: '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>',
                oPaginate: {
                    sPrevious: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    sNext: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                }
            },
            aoColumns: [
                { mData: 'principalName', sType: 'html', sWidth: '30%' }, // Owner
                { mData: 'targetName', sType: 'html', sWidth: '30%'}, // Principal
                { mData: 'permissionType', sType: 'html', sWidth: '20%' }, // Activity
                { mData: 'targetName', sType: 'html', bSearchable: false, sWidth: '10%' }, // Target
                { mData: 'targetName', sType: 'html', bSearchable: false, sWidth: '10%' } // Edit Link
            ],
            fnInitComplete: function (oSettings) {
                activityList_configuration.main.table.fnDraw();
            },
            fnServerData: function (sUrl, aoData, fnCallback, oSettings) {
                oSettings.jqXHR = $.ajax({
                    url: sUrl,
                    data: {
                        owner: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ owner.fname }</spring:escapeBody>',
                        activity: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ activity.fname }</spring:escapeBody>'
                    },
                    dataType: "json",
                    cache: false,
                    type: oSettings.sServerMethod,
                    success: function (json) {
                        if (json.sError) {
                            oSettings.oApi._fnLog(oSettings, 0, json.sError);
                        }

                        $(oSettings.oInstance).trigger('xhr', [oSettings, json]);
                        fnCallback(json);
                    },
                    error: function (xhr, error, thrown) {
                        lib.handleError(xhr, error, thrown);
                    }
                });
            },
            fnInfoCallback: function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
                var infoMessage = '<spring:message code="datatables.info.message" htmlEscape="false" javaScriptEscape="true"/>';
                var iCurrentPage = Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength) + 1;
                infoMessage = infoMessage.replace(/_START_/g, iStart).
                                      replace(/_END_/g, iEnd).
                                      replace(/_TOTAL_/g, iTotal).
                                      replace(/_CURRENT_PAGE_/g, iCurrentPage);
                return infoMessage;
            },
            // Add links to the proper columns after we get the data
            fnRowCallback: function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                // Get edit anchor tag
                $('td:eq(3)', nRow).html( getEditAnchorTag(aData.owner, aData.activity, aData.target) );

                // Get delete anchor tag
                $('td:eq(4)', nRow).html( getDeleteAnchorTag(aData.owner,
                                                             aData.principalName,
                                                             aData.principalKey,
                                                             aData.activity,
                                                             aData.target,
                                                             aData.permissionType) );

            },
            // Setting the top and bottom controls
            sDom: 'r<"row alert alert-info view-filter"<"toolbar-filter"><W><"toolbar-br"><"dataTables-inline dataTables-left"p><"dataTables-inline dataTables-left"i><"dataTables-inline dataTables-left"l>><"row"<"span12"t>>>',
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',', // Used for multivalue column Categories
                aiExclude: [activityList_configuration.column.placeHolderForEditLink,
                            activityList_configuration.column.placeHolderForDeleteLink]
            }
        });
    };

    initializeTable();
    // Adding formatting to sDom
    $("div.toolbar-br").html('<BR>');
    $("div.toolbar-filter").html('<B><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></B>:');
});
</script>
