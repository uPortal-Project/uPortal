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
<portlet:renderURL var="userUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="viewUserDetails"/>
</portlet:renderURL>
<portlet:renderURL var="editUrl" escapeXml="false">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="editPermission"/>
    <portlet:param name="owner" value="OWNER"/>
    <portlet:param name="activity" value="ACTIVITY"/>
    <portlet:param name="target" value="TARGET"/>
</portlet:renderURL>

<style>
#${n}permissionBrowser .dataTables_filter, #${n}permissionBrowser .first.paginate_button, #${n}permissionBrowser .last.paginate_button{
    display: none;
}
#${n}permissionBrowser .dataTables-inline, #${n}permissionBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}permissionBrowser .dataTables_wrapper {
    width: 100%;
}
#${n}permissionBrowser .dataTables_paginate .paginate_button {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    *cursor: hand;
}
#${n}permissionBrowser .dataTables_paginate .paginate_active {
    margin: 2px;
    color:#000;
}

#${n}permissionBrowser .dataTables_paginate .paginate_active:hover {
    text-decoration: line-through;
}

#${n}permissionBrowser table tr td a {
    color: #428BCA;
}

#${n}permissionBrowser .dataTables-left {
    float:left;
}
#${n}permissionBrowser .row {
    margin-left:0px;
    margin-right:0px;
}

#${n}permissionBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}

#${n}permissionBrowser .filter-term {
    display: block;
    text-align:bottom;
}

#${n}permissionBrowser .dataTables_length label {
    font-weight: normal;
}
#${n}permissionBrowser .datatable-search-view {
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
<div class="fl-widget portlet prm-mgr" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <a href="${ userUrl }">${ fn:escapeXml(person.name )}</a> >
            <spring:message code="permissions"/>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <!-- Portlet Content -->
    <div id="${n}permissionBrowser" class="fl-widget-content portlet-content">

        <!-- Portlet Section -->
        <div id="${n}permissionAddingTabs" class="portlet-section view-permissions" role="region">
            <div class="titlebar">
                <h3 class="title" role="heading"><spring:message code="assignments"/></h3>
            </div>

            <div id="${n}assignmentTabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
                <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
                    <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active">
                        <a href="#${n}principalTab" shape="rect"><span><spring:message code="permissions.for.name" arguments="${person.name}"/></span></a>
                    </li>
                    <li class="ui-state-default ui-corner-top">
                        <a href="#${n}targetTab" shape="rect"><span><spring:message code="permissions.on.name" arguments="${person.name}"/></span></a>
                    </li>
                </ul>

            <c:forTokens items="principal,target" delims="," var="token">
                <div id="${n}${fn:escapeXml(token)}Tab">
                    <div class="content">
                        <table class="portlet-table table table-bordered table-hover" id="${n}${fn:escapeXml(token)}permissionsTable">
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
            </c:forTokens>
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
        url: "<c:url value="/api/assignments/principal/${ up:encodePathSegment(personEntity.principalString) }.json?includeInherited=true"/>",
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
        url: "<c:url value="/api/assignments/target/${ up:encodePathSegment(personEntity.principalString) }.json?includeInherited=true"/>",
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
        config.main.table = $("#${n}" + tableName + "permissionsTable").dataTable({
            iDisplayLength: config.main.pageSize,
            aLengthMenu: [5, 10, 20, 50],
            bServerSide: false,
            sAjaxSource: config.url,
            sAjaxDataProp: "assignments",
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
                { mData: 'ownerName', sType: 'html', sWidth: '25%' },  // Owner
                { mData: 'principalName', sType: 'html', sWidth: '25%', bVisible: config.showPrincipal },  // Principal
                { mData: 'activityName', sType: 'html', sWidth: '25%' },  // Activity
                { mData: 'targetName', sType: 'html', bSearchable: false, sWidth: '25%', bVisible: config.showTarget },  // Target
                { mData: 'targetName', sType: 'html', bSearchable: false, sWidth: '25%' }  // Edit Link
            ],
            fnInitComplete: function (oSettings) {
                config.main.table.fnDraw();
            },
            fnServerData: function (sUrl, aoData, fnCallback, oSettings) {
                oSettings.jqXHR = $.ajax({
                    url: sUrl,
                    data: aoData,
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
                // Create edit link
                $('td:eq(3)', nRow).html( getEditAnchorTag(aData.ownerKey, aData.activityKey, aData.targetKey) );
                // Set activity inherited markup
                if (config.showPrincipal) {
                    $('td:eq(2)', nRow).html( getActivityValue(aData.activityName, aData.inherited) );
                } else {
                    $('td:eq(1)', nRow).html( getActivityValue(aData.activityName, aData.inherited) );
                }
            },
            // Setting the top and bottom controls
            sDom: 'r<"row alert alert-info view-filter"<"toolbar-filter"><W><"toolbar-br"><"dataTables-inline dataTables-left"p><"dataTables-inline dataTables-left"i><"dataTables-inline dataTables-left"l>><"row"<"span12"t>>>',
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',', // Used for multivalue column Categories
                aiExclude: [config.column.placeHolderForEditLink,
                            config.searchExclude]
            }
        });
    };

    initializeTable('principal');
    initializeTable('target');
    $("#${n}assignmentTabs").tabs({ active: 0 });
    // Adding formatting to sDom
    $("div.toolbar-br").html('<BR>');
    $("div.toolbar-filter").html('<B><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></B>:');
});
</script>

