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

<portlet:renderURL var="editUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
</portlet:renderURL>
<style>
#${n}permissionBrowser .dt-search,
#${n}permissionBrowser .first.dt-paging-button,
#${n}permissionBrowser .last.dt-paging-button {
    display: none;
}
#${n}permissionBrowser .dataTables-inline, #${n}permissionBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}permissionBrowser .view-filter {
    padding-bottom: 15px;
    overflow: hidden;
    margin-bottom: 0;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
}
#${n}permissionBrowser .dt-container {
    width: 100%;
}
#${n}permissionBrowser .dt-layout-row:has(.dt-paging) {
    background-color: rgb(217, 237, 247);
    border: 1px solid rgb(188, 232, 241);
    border-top: none;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
    padding: 8px 15px;
    margin-bottom: 20px;
}
#${n}permissionBrowser .dt-paging .page-link {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
}
#${n}permissionBrowser .dt-paging .page-item.active .page-link {
    color: #000;
    background: none;
    border-color: transparent;
}
#${n}permissionBrowser table tr td a {
    color: #428BCA;
}
#${n}permissionBrowser .dataTables-left {
    float: left;
}
#${n}permissionBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}
#${n}permissionBrowser .filter-term {
    display: block;
    text-align: bottom;
}
#${n}permissionBrowser .dt-length label {
    font-weight: normal;
}
#${n}permissionBrowser .datatable-search-view {
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
<div id="${n}permissionBrowser" class="card portlet prm-mgr" role="section">

  <!-- Portlet Titlebar -->
  <div class="card-header portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">
        <spring:message code="activityName.permissions.assigned.to.principalName" arguments="${ fn:escapeXml(activityDisplayName) }, ${ principalDisplayName }"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->

  <!-- Portlet Content -->
  <div class="card-body portlet-content">
      <div class="titlebar">
          <h3 class="title" role="heading"><spring:message code="assignments"/></h3>
      </div>
      <div class="content">
          <table class="portlet-table table table-bordered table-hover" id="${n}permissionsTable">
              <thead>
                  <tr>
                      <th><spring:message code="owner"/></th>
                      <th><spring:message code="activity"/></th>
                      <th><spring:message code="target"/></th>
                      <th><spring:message code="edit"/></th>
                  </tr>
              </thead>
          </table>
      </div>

      <a href="${ ownersUrl }">Back to permission owners</a>
  </div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;
    var principalList_configuration = {
        column: {
            owner: 0,
            activity: 1,
            target: 2,
            placeHolderForEditLink: 3
        },
        main: {
            table : null,
            pageSize: 10
        },
        url: "<c:url value="/api/assignments/principal/${ principal }.json?includeInherited=true"/>"
    };
    // Anchor tag generating helper function
    var getEditAnchorTag = function(owner, activity, target) {
        var url = "${editUrl}".replace("OWNER", owner).
                                      replace("ACTIVITY", activity).
                                      replace("TARGET", target);
        return '<a href="' + url + '"><spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/></a>';
    };
    // Get activity value generating helper function
    var getActivityValue = function(activity, inherited) {
        // Add Inherited if applicable
        var markup = '<span>${"' + activity + '"}</span>';
        if (inherited) {
            markup += ' <span class="inherited-permission"><spring:message code="inherited" htmlEscape="false" javaScriptEscape="true"/></span>';
        }
        return markup;
    };

    var initializeTable = function() {
        var table = $("#${n}permissionsTable");
        principalList_configuration.main.table = $("#${n}permissionsTable").DataTable({
            pageLength: principalList_configuration.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            serverSide: false,
            ajax: {
                url: principalList_configuration.url,
                dataSrc: "assignments"
            },
            deferRender: false,
            processing: true,
            autoWidth: false,
            language: {
                lengthMenu: '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>',
                paginate: {
                    previous: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    next: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                }
            },
            columns: [
                { data: 'ownerName', width: '25%' },
                { data: 'activityName', width: '25%' },
                { data: 'targetName', width: '25%' },
                { data: 'targetName', searchable: false, width: '25%' }
            ],
            initComplete: function (settings) {
                principalList_configuration.main.table.draw();
            },
            rowCallback: function (row, data, displayNum, displayIndex, dataIndex) {
                $('td:eq(3)', row).html( getEditAnchorTag(data.ownerKey, data.activityKey, data.targetKey) );
                $('td:eq(1)', row).html( getActivityValue(data.activityName, data.inherited) );
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
