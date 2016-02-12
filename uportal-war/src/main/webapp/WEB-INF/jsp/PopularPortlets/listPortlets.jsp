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

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<c:set var="n"><portlet:namespace/></c:set>

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
<div id="${n}portletBrowser" class="portlet" role="section">
<form id="${n}form">
  
  <!-- Portlet Title -->
  <div class="portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="most.frequently.added"/></h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Toolbar -->
  <div class="portlet-toolbar" role="toolbar">
    <spring:message code="previous"/>
    <select id="${n}days" name="days">
      <option value="1">1</option>
      <option value="7">7</option>
      <option value="30" selected="selected">30</option>
      <option value="90">90</option>
      <option value="365">365</option>
    </select>
  </div> <!-- end: portlet-toolbar -->
        
    <!-- Portlet Body -->
  <div class="portlet-body" role="main">

    <!-- Portlet Section -->
    <div id="${n}popularPortlets" class="portlet-section" role="region">      

      <div class="portlet-section-body">
        <table id="${n}portletsTable" class="table table-stripped table-bordered">
          <thead>
            <tr rsf:id="header:">
              <th><spring:message code="title"/></th>
              <th><spring:message code="number.times"/></th>
            </tr>
          </thead>
        </table>
      </div>
      <c:if test="${showAdminFeatures}">
        <!-- Portlet Buttons -->
        <div class="portlet-button-group">
          <portlet:actionURL var="doneUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="done" />
          </portlet:actionURL>
          <a class="portlet-button portlet-button-primary" href="${doneUrl}"><spring:message code="done"/></a>
        </div>
      </c:if>
    </div> <!-- end: portlet-section -->
    
  </div> <!-- end: portlet-body -->

</form>
</div> <!-- end: portlet -->
<span class="label">* These values are provided by daily statistics aggregation</span>
<portlet:resourceURL id="popularPortletCounts" var="popularPortletCountsUrl" />
<script type="text/javascript">
up.jQuery(function() {

    var $ = up.jQuery;
    var portletDeepLinkUrl = '<c:url value="/p/PORTLETFNAME"/>';

    var portletList_configuration = {
        column: {
            title: 0,
            times: 1
        },
        main: {
            table : null,
            pageSize: 10
        }
    };

    var getDeepLinkAnchorTag = function(portletFName, portletDescription, portletTitle) {
        var url = portletDeepLinkUrl.replace("PORTLETFNAME", portletFName);
        return '<a href="' + url + '" title="' + portletDescription + '">' + portletTitle + '</a>';
    };

    var initializeTable = function() {
        // To allow the datatable to be repopulated to search multiple times
        // clear and destroy the original
        if (portletList_configuration.main.table != undefined && typeof portletList_configuration.main.table.fnClearTable !== 'undefined') {
            portletList_configuration.main.table.fnClearTable();
            portletList_configuration.main.table.fnDestroy();
        }
        portletList_configuration.main.table = $("#${n}portletsTable").dataTable({
        	pageLength: portletList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	ajax: {
        		url: '${popularPortletCountsUrl}',
        		dataSrc: "counts",
        		data: function(d) { return $("#${n}form").serialize() }
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
                { data: 'portletFName', type: 'string', width: '50%',
                	render: function ( data, type, row, meta ) {
                		return getDeepLinkAnchorTag(row.portletFName, row.portletDescription, row.portletTitle);
                	}
                },  // Name
                { data: 'count', type: 'string', width: '50%' }  // Times 
            ],
            initComplete: function (oSettings) {
            	$(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
                portletList_configuration.main.table.fnDraw();
            },
            // Setting the top and bottom controls
            dom: 	"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
            // Filtering
            oColumnFilterWidgets: { },
            responsive: true
        });
    };

    initializeTable();
    $("#${n}days").change(initializeTable);
});
</script>
