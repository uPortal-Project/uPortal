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
<portlet:actionURL var="queryUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<c:set var="n"><portlet:namespace/></c:set>

<portlet:actionURL var="newPortletUrl" >
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="createPortlet"/>
</portlet:actionURL>
<portlet:actionURL var="editPortletUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPortlet"/>
  <portlet:param name="portletId" value="PORTLETID"/>
</portlet:actionURL>
<portlet:actionURL var="removePortletUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="removePortlet"/>
  <portlet:param name="portletId" value="PORTLETID"/>
</portlet:actionURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<!-- Portlet -->
<div id="${n}portletBrowser" class="portlet ptl-mgr view-home" role="section">

  <c:if test="${not empty statusMsgCode}">
   <div class="row">
   <div class="col-xs-12">
    <div class="alert alert-success alert-dismissable">
      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
      <spring:message code="${statusMsgCode}" arguments="${portlet.name}" htmlEscape="true"/>
      <c:if test="${not empty layoutURL}">
        <spring:message code="add.portlet.to.layout" arguments="${layoutURL}" htmlEscape="false"/>
      </c:if>
	</div>
    </div>
    </div>
  </c:if>

  <h2 class="title" role="heading"><spring:message code="portlet.registry"/></h2>
  
  <table id="${n}portletsList" class="table table-striped table-hover table-bordered">
     <thead>
       <tr>
         <th><spring:message code="name"/></th>
         <th><spring:message code="type"/></th>
         <th><spring:message code="state"/></th>
         <th><spring:message code="edit"/></th>
         <th><spring:message code="delete"/></th>
         <th><spring:message code="category"/></th>
       </tr>
     </thead>
   </table>

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {

    var $ = up.jQuery;  // de-alias jQuuery to the customary name

    var portletList_configuration = {
        column: {
            name: 0,
            type: 1,
            Lifecycle: 2,
            placeHolderForEditLink  : 3,
            placeHolderForDeleteLink: 4,
            categories: 5
        },
        main: {
            table : null,
            pageSize: 10
        }
    };

    // Url generating helper functions
    var getEditURL = function(portletId) {
        var url = '${editPortletUrl}'.replace("PORTLETID", portletId);
        return '<a href="' + url + '"><spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/> <span class="pull-right"><i class="fa fa-edit"></i></span></a>';
    };
    var getDeleteURL = function(portletId) {
        var url = '${removePortletUrl}'.replace("PORTLETID", portletId);
        return '<a href="' + url + '"><spring:message code="delete" htmlEscape="false" javaScriptEscape="true"/> <span class="pull-right"><i class="fa fa-trash-o"></i></span></a>';
    };

    // Created as its own 
    var initializeTable = function() {
        portletList_configuration.main.table = $("#${n}portletsList").dataTable({
        	pageLength: portletList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
            ajax: {
            	url: '<c:url value="/api/portlets.json"/>',
            	dataSrc: "portlets"
            },
            processing: true,
            autoWidth: false,
            pagingType: "full_numbers",
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
                { data: 'name', type: 'html', width: '30%' },  // Name
                { data: 'type', type: 'html', width: '30%' },  // Type 
                { data: 'lifecycleState', type: 'html', width: '20%' },  // Lifecycle State
                { 	data: 'id', 
                	type: 'html', 
                	searchable: false,
                	width: '10%',
                	render: function ( data, type, row, meta ) {
                		return getEditURL(data)
                	}
                },  // Edit Link
                { data: 'id', 
                	type: 'html', 
                	searchable: false, 
                	width: '10%',
                	render: function ( data, type, row, meta ) {
                		return getDeleteURL(data);
                	}
                },  // Delete Link
                {
                    data: function(row, type, set, meta) {
                        // this function sets the value (set), returns original source of value (undefined), and then returns the value
                        if (type === undefined) {
                            return row.categories.join(); //join into string only to be split later by oColumnFilterWidgets sSeparator
                        } else if (type === 'set') {
                        	row.display = row.categories.join();
                            return;
                        }
                        // 'display', 'filter', 'sort', and 'type' all just use the formatted string
                        return row.categories;
                    },
                    searchable: true,
                    visible: false,
                    orderSequence: [ "desc", "asc" ]
                }  // Categories - hidden 
            ],
            initComplete: function (settings, json) {
                $(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
                $(".add-portlet").html('<a class="btn btn-primary button pull-right" href="${ newPortletUrl }" title="<spring:message code="register.new.portlet"/>"><span><spring:message code="register.new.portlet"/></span>&nbsp;&nbsp;<i class="fa fa-plus-circle"></i></a>');
            },
            // this is the default except with an extra row for the add-portlet button placeholder
            // and a 'W' (filter plugin) inserted next to the 'l - length changing input control'
            dom: 	"<'row'<'col-xs-12 add-portlet'>>" +
            		"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
            // Filtering
            oColumnFilterWidgets: {
                sSeparator: ',',
                aiExclude: [portletList_configuration.column.name,
                                portletList_configuration.column.type,
                                portletList_configuration.column.placeHolderForEditLink,
                                portletList_configuration.column.placeHolderForDeleteLink]
            },
            responsive: true
        });
    };

    initializeTable();
});
</script>
