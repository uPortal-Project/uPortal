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
<portlet:renderURL var="groupUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="group"/>
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
#${n}permissionBrowser .row {
    margin-left: 0px;
    margin-right: 0px;
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
<div class="card portlet prm-mgr" role="section">

    <!-- Portlet Titlebar -->
    <div class="card-header portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <a href="${ groupUrl }">${ fn:escapeXml(group.name )}</a> >
            <spring:message code="permissions"/>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <!-- Portlet Content -->
    <div id="${n}permissionBrowser" class="card-body portlet-content">

        <!-- Portlet Section -->
        <div id="${n}permissionAddingTabs" class="portlet-section view-permissions" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="assignments"/></h3>
            </div>

            <div id="${n}assignmentTabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
                <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
                    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active">
                        <a href="#${n}principalTab" shape="rect"><span><spring:message code="permissions.for.name" arguments="${group.name}"/></span></a>
                    </li>
                    <li class="ui-state-default ui-corner-top">
                        <a href="#${n}targetTab" shape="rect"><span><spring:message code="permissions.on.name" arguments="${group.name}"/></span></a>
                    </li>
                </ul>

                <div id="${n}principalTab">
                    <div class="content">
                        <table class="portlet-table table table-bordered table-hover" id="${n}principalpermissionsTable">
                            <thead>
                            <tr>
                                <th><spring:message code="owner"/></th>
                                <th><spring:message code="principal"/></th>
                                <th><spring:message code="activity"/></th>
                                <th><spring:message code="target"/></th>
                                <th><spring:message code="edit"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <div id="${n}targetTab">
                    <div class="content">
                        <table class="portlet-table table table-bordered table-hover" id="${n}targetpermissionsTable">
                            <thead>
                            <tr>
                                <th><spring:message code="owner"/></th>
                                <th><spring:message code="principal"/></th>
                                <th><spring:message code="activity"/></th>
                                <th><spring:message code="target"/></th>
                                <th><spring:message code="edit"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var principalList_configuration = {
        column: {
            owner: 0,
            principal: 1,
            activity: 2,
            target: 3,
            placeHolderForEditLink: 4
        },
        main: {
            table : null,
            pageSize: 10
        },
        url: "<c:url value="/api/assignments/principal/${ up:encodePathSegment(principalString) }.json?includeInherited=true"/>",
        searchExclude: 1, //Exclude principal
        showPrincipal: false,
        showTarget: true
    };
    var targetList_configuration = {
        column: {
            owner: 0,
            principal: 1,
            activity: 2,
            target: 3,
            placeHolderForEditLink: 4
        },
        main: {
            table : null,
            pageSize: 10
        },
        url: "<c:url value="/api/assignments/target/${ up:encodePathSegment(principalString) }.json?includeInherited=true"/>",
        searchExclude: 3, //Exclude target
        showPrincipal: true,
        showTarget: false
    };

    // Url generating helper function
    var getEditAnchorTag = function(owner, activity, target) {
        var url = "${editUrl}".replace("OWNER", owner).
                                      replace("ACTIVITY", activity).
                                      replace("TARGET", target);
        return '<a href="' + url + '"><spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/></a>';
    };
    // Get activity value
    var getActivityValue = function(activity, inherited) {
        // Add Inherited if applicable
        var markup = '<span>${"' + activity + '"}</span>';
        if (inherited) {
            markup += ' <span class="inherited-permission"><spring:message code="inherited" htmlEscape="false" javaScriptEscape="true"/></span>';
        }
        return markup;
    }


    // Created as its own
    var initializeTable = function(tableName) {
        var config = null;
        if (tableName == "principal") {
            config = principalList_configuration;
        } else {
            config = targetList_configuration;
        }
        config.main.table = $("#${n}" + tableName + "permissionsTable").DataTable({
            pageLength: config.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            serverSide: false,
            ajax: {
                url: config.url,
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
                { data: 'principalName', width: '25%', visible: config.showPrincipal },
                { data: 'activityName', width: '25%' },
                { data: 'targetName', searchable: false, width: '25%', visible: config.showTarget },
                { data: 'targetName', searchable: false, width: '25%' }
            ],
            initComplete: function (settings) {
                config.main.table.draw();
            },
            rowCallback: function (row, data, displayNum, displayIndex, dataIndex) {
                $('td:eq(3)', row).html( getEditAnchorTag(data.ownerKey, data.activityKey, data.targetKey) );
                if (config.showPrincipal) {
                    $('td:eq(2)', row).html( getActivityValue(data.activityName, data.inherited) );
                } else {
                    $('td:eq(1)', row).html( getActivityValue(data.activityName, data.inherited) );
                }
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
                columns: [0, 2],
                cascadePanes: true,
                viewTotal: true,
                layout: 'columns-2'
            }
        });
    };

    initializeTable('principal');
    initializeTable('target');
    $("#${n}assignmentTabs").tabs({ active: 0 });
    $("div.toolbar-filter").html('<b><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></b>:');
});
</script>
