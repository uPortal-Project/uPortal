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
<portlet:actionURL var="userUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="viewUserDetails"/>
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
    <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <a href="${ userUrl }">${ fn:escapeXml(person.name )}</a> >
            <spring:message code="permissions"/>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <!-- Portlet Content -->
    <div id="${n}permissionBrowser" class="fl-widget-content portlet-content" role="main">

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
        	pageLength: config.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	ajax: {
        		url: config.url,
        		dataSrc: "assignments",
        		data: tableName
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
                { 	data: 'ownerName', type: 'html', width: '25%'}, // Owner
                { 	data: 'principalName', type: 'html', width: '25%', visible: config.showPrincipal},  // Principal 
                { 	data: 'activityName', type: 'html', width: '25%',
                	render: function ( data, type, row, meta ) {
                		return getActivityValue(row.activityName, row.inherited);
                	}
                },  // Activity
                { 	data: 'targetName', type: 'html', searchable: false, width: '25%', visible: config.showTarget},  // Target
                { 	data: 'targetName', type: 'html', searchable: false, width: '25%',
                	render: function ( data, type, row, meta ) {
                		return getEditAnchorTag(row.ownerKey, row.activityKey, row.targetKey);
                	}
                }  // Edit Link
            ],
            initComplete: function (oSettings) {
                config.main.table.fnDraw();
            },
            // this is the default except with an extra row for the add-portlet button placeholder
            // and a 'W' (filter plugin) inserted next to the 'l - length changing input control'
            dom: 	"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',', // Used for multivalue column Categories
                aiExclude: [config.column.placeHolderForEditLink,
                            config.searchExclude]
            },
            responsive: true
        });
    };

    initializeTable('principal');
    initializeTable('target');
    $("#${n}assignmentTabs").tabs({ active: 0 });
});
</script>

