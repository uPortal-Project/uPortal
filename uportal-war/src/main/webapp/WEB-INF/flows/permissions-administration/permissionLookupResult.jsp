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

<portlet:actionURL var="editUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
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
<div id="${n}permissionBrowser" class="portlet prm-mgr" role="section">
  
  <!-- Portlet Titlebar -->
  <div class="portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">
        <spring:message code="activityName.permissions.assigned.to.principalName" arguments="${ fn:escapeXml(activityDisplayName) }, ${ principalDisplayName }"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="portlet-content" role="main">
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

      <a href="${ ownersUrl }" class="btn btn-default">Back to permission owners</a> 
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
        principalList_configuration.main.table = $("#${n}permissionsTable").dataTable({
        	pageLength: principalList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	ajax: {
        		url: principalList_configuration.url,
        		dataSrc: "assignments"        	
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
                { 	data: 'ownerName', type: 'html', width: '25%' }, // Owner
                { 	data: 'activityName', type: 'html', width: '25%',
                	render: function ( data, type, row, meta ) {
                		return getActivityValue(row.activityName, row.inherited);
                	}
                }, // Activity
                { 	data: 'targetName', type: 'html', width: '25%' }, // Target
                { 	data: 'targetName', type: 'html', searchable: false, width: '25%',
                	render: function ( data, type, row, meta ) {
                		return getEditAnchorTag(row.ownerKey, row.activityKey, row.targetKey);		
                	}
                } // Edit Link
            ],
            initComplete: function (oSettings) {
            	$(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
                principalList_configuration.main.table.fnDraw();
            },
            // Setting the top and bottom controls
            dom: 	"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',', // Used for multivalue column Categories
                aiExclude: [principalList_configuration.column.placeHolderForEditLink]
            },
            responsive: true
        });
    };
    initializeTable();
});
</script>
