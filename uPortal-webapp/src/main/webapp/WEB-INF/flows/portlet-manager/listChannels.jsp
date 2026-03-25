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

<portlet:renderURL var="newPortletUrl" >
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="createPortlet"/>
</portlet:renderURL>
<portlet:renderURL var="editPortletUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPortlet"/>
  <portlet:param name="portletId" value="PORTLETID"/>
</portlet:renderURL>
<portlet:renderURL var="removePortletUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="removePortlet"/>
  <portlet:param name="portletId" value="PORTLETID"/>
</portlet:renderURL>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

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

<style>
#${n}portletBrowser .dt-search {
    display: none;
}
#${n}portletBrowser .dt-container {
    width: 100%;
}
#${n}portletBrowser .view-filter {
    padding: 10px 15px 15px;
    margin-bottom: 20px;
    border-top: none;
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    font-size: 14px;
}
#${n}portletBrowser .view-filter h4 {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: bold;
}
#${n}portletBrowser .column-filter-widgets {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
}
#${n}portletBrowser .column-filter-widget {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
#${n}portletBrowser .column-filter-widget select {
    font-size: 14px;
}
#${n}portletBrowser .filter-term {
    display: inline-block;
    margin: 2px 0;
    padding: 2px 8px;
    background-color: #d9edf7;
    border: 1px solid #bce8f1;
    border-radius: 3px;
    color: #31708f;
    text-decoration: none;
    font-size: 12px;
    cursor: pointer;
    width: fit-content;
}
#${n}portletBrowser .filter-term:hover {
    background-color: #c4e3f3;
    text-decoration: none;
}
#${n}portletBrowser .dt-paging-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 4px;
    font-size: 14px;
}
#${n}portletBrowser .dt-paging-row .dt-info {
    white-space: nowrap;
}
#${n}portletBrowser .dt-paging-row .dt-length {
    display: flex;
    align-items: center;
    gap: 4px;
    white-space: nowrap;
}
#${n}portletBrowser .dt-paging-row .dt-length select {
    display: inline-block !important;
    width: auto;
    font-size: 14px;
}
#${n}portletBrowser .dt-paging-row .dt-length label {
    font-weight: normal;
    margin: 0;
}
#${n}portletBrowser .dt-paging .page-link {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    font-size: 14px;
}
#${n}portletBrowser .dt-paging .page-item.active .page-link {
    color: #000;
    background: none;
    border-color: transparent;
}
#${n}portletBrowser table tr td a {
    color: #428BCA;
}
#${n}portletBrowser .datatable-search-view {
    text-align: right;
}
</style>

<!-- Portlet -->
<div id="${n}portletBrowser" class="card portlet ptl-mgr view-home" role="section">
  <c:if test="${not empty statusMsgCode}">
    <div class="alert alert-success alert-dismissable">
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-hidden="true"></button>
      <spring:message code="${statusMsgCode}" arguments="${portlet.name}" htmlEscape="true"/>
      <c:if test="${not empty layoutURL}">
        <spring:message code="add.portlet.to.layout" arguments="${layoutURL}" htmlEscape="false"/>
      </c:if>
    </div>
  </c:if>
  <!-- Portlet Titlebar -->
  <div class="card-header titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="portlet.registry"/></h2>
    <div class="fl-col-flex2 toolbar" role="toolbar">
      <div class="fl-col">
        <div class="btn-group" role="group">
          <a class="btn btn-primary" href="${ newPortletUrl }" title="<spring:message code="register.new.portlet"/>"><span><spring:message code="register.new.portlet"/></span>&nbsp;&nbsp;<i class="fa fa-plus-circle"></i></a>
        </div>
      </div>
      <div class="fl-col text-end datatable-search-view">
        <form class="portlet-search-form d-flex align-items-center gap-2" style="display:inline">
          <label for="${n}search" class="form-label">
            <spring:message code="search"/>
          </label>
          <input id="${n}search" type="search" class="portlet-search-input form-control"/>
        </form>
      </div>
    </div>
    <div style="clear:both"></div>
  </div>

  <!-- Portlet Content -->
  <div class="card-body content portlet-content">
      <div class="row alert alert-info view-filter" id="${n}viewFilter">
        <h4>Filters</h4>
        <div class="column-filter-widgets" id="${n}columnFilterWidgets"></div>
        <div class="dt-paging-row" id="${n}pagingRow"></div>
      </div>
      <div>
        <table id="${n}portletsList" class="portlet-table table table-bordered table-striped table-hover" style="width:100%;">
          <thead>
            <tr>
              <th><spring:message code="name"/></th>
              <th><spring:message code="type"/></th>
              <th><spring:message code="state"/></th>
              <th><spring:message code="edit"/></th>
              <th><spring:message code="delete"/></th>
            </tr>
          </thead>
        </table>
      </div>
  </div> <!-- end: portlet-body -->

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
        return '<a href="' + url + '"><spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/> <span class="float-end"><i class="fa fa-edit"></i></span></a>';
    };
    var getDeleteURL = function(portletId) {
        var url = '${removePortletUrl}'.replace("PORTLETID", portletId);
        return '<a href="' + url + '"><spring:message code="delete" htmlEscape="false" javaScriptEscape="true"/> <span class="float-end"><i class="fa fa-trash-o"></i></span></a>';
    };

    // Global variables for category filtering
    var selectedCategories = [];
    var categorySearchFunction = function(settings, data, dataIndex) {
        if (selectedCategories.length === 0) return true;
        
        var table = portletList_configuration.main.table;
        var portlet = table.row(dataIndex).data();
        if (!portlet.categories || !Array.isArray(portlet.categories)) return false;
        
        // OR logic: show if portlet has ANY of the selected categories
        return selectedCategories.some(function(term) {
            return portlet.categories.indexOf(term) !== -1;
        });
    };
    
    var createFilters = function(table) {
        var $columnFilterWidgets = $('#${n}columnFilterWidgets');
        
        // State filter
        var stateSelect = $('<select class="form-select form-select-sm"><option value="">State</option></select>');
        var stateData = table.column(2).data().unique().sort();
        stateData.each(function(state) {
            stateSelect.append('<option value="' + state + '">' + state + '</option>');
        });
        if (stateData.length <= 1) {
            stateSelect.prop('disabled', true);
        }
        stateSelect.on('change', function() {
            table.column(2).search(this.value).draw();
        });
        
        // Category filter
        var categorySelect = $('<select class="form-select form-select-sm"><option value="">Category</option></select>');
        var allCategories = [];
        table.data().each(function(portlet) {
            if (portlet.categories && Array.isArray(portlet.categories)) {
                allCategories = allCategories.concat(portlet.categories);
            }
        });
        var uniqueCategories = [...new Set(allCategories)].sort();
        uniqueCategories.forEach(function(cat) {
            categorySelect.append('<option value="' + cat + '">' + cat + '</option>');
        });
        
        var $categoryWidget = $('<div class="column-filter-widget"></div>').append(categorySelect);
        
        categorySelect.on('change', function() {
            var selectedValue = this.value;
            if (selectedValue && selectedCategories.indexOf(selectedValue) === -1) {
                selectedCategories.push(selectedValue);
                
                var $filterTerm = $('<a class="filter-term" href="#">' + selectedValue + '</a>');
                $filterTerm.on('click', function(e) {
                    e.preventDefault();
                    var termText = $(this).text();
                    selectedCategories = selectedCategories.filter(function(cat) {
                        return cat !== termText;
                    });
                    $(this).remove();
                    updateCategoryFilter();
                });
                $categoryWidget.append($filterTerm);
                updateCategoryFilter();
            }
            this.value = '';
        });
        
        function updateCategoryFilter() {
            $.fn.dataTable.ext.search = $.fn.dataTable.ext.search.filter(function(fn) {
                return fn !== categorySearchFunction;
            });
            if (selectedCategories.length > 0) {
                $.fn.dataTable.ext.search.push(categorySearchFunction);
            }
            table.draw();
        }
        
        $columnFilterWidgets
            .append($('<div class="column-filter-widget">').append(stateSelect))
            .append($categoryWidget);
    };

    // Created as its own
    var initializeTable = function() {
        portletList_configuration.main.table = $("#${n}portletsList").DataTable({
            pageLength: portletList_configuration.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            serverSide: false,
            ajax: {
                url: '<c:url value="/api/portlets.json"/>',
                dataSrc: "portlets",
                error: function (xhr, error, thrown) {
                    console.error('AJAX Error:', xhr.status, xhr.responseText, error, thrown);
                }
            },
            deferRender: false,
            processing: true,
            autoWidth: false,
            language: {
                lengthMenu: '_MENU_ per page',
                paginator: {
                    first: '\u00AB',
                    previous: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    next: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>',
                    last: '\u00BB'
                }
            },
            infoCallback: function(settings, start, end, max, total, pre) {
                var api = this.api();
                var pageInfo = api.page.info();
                return 'Viewing page ' + (pageInfo.page + 1) + '. Showing records ' + start + ' to ' + end + ' of ' + total + ' items.';
            },
            columns: [
                { data: 'name', width: '30%' },
                { data: 'type', width: '30%' },
                { data: 'lifecycleState', width: '20%' },
                { data: 'id', searchable: false, width: '10%' },
                { data: 'id', searchable: false, width: '10%' }
            ],
            initComplete: function (settings) {
                this.api().draw();
            },
            rowCallback: function (row, data, displayNum, displayIndex, dataIndex) {
                $('td:eq(3)', row).html( getEditURL(data.id) );
                $('td:eq(4)', row).html( getDeleteURL(data.id) );
            },
            drawCallback: function() {
                var table = this.api();
                if (table.data().length === 0) return;
                
                // Build filters once
                if ($('#${n}columnFilterWidgets').children().length === 0) {
                    createFilters(table);
                }
                
                // Move DT-rendered info/length/paging into our static paging row
                var $pagingRow = $('#${n}pagingRow');
                $('#${n}portletBrowser .dt-info').appendTo($pagingRow);
                $('#${n}portletBrowser .dt-length').appendTo($pagingRow);
                $('#${n}portletBrowser .dt-paging').appendTo($pagingRow);
            },
            layout: {
                topStart: null,
                topEnd: null,
                top: null,
                bottomStart: null,
                bottom: {
                    features: ['info', 'pageLength', 'paging']
                },
                bottomEnd: null
            }
        });
    };

    initializeTable();
    // Hide the out of the box search and populate it with our text box
    $('#${n}portletBrowser .portlet-search-input').on('keyup', function(){
        portletList_configuration.main.table.search( $(this).val() ).draw();
    });
    
    // Cleanup on page unload to prevent memory leaks
    $(window).on('beforeunload', function() {
        if (portletList_configuration.main.table != undefined && $.fn.DataTable.isDataTable('#${n}portletsList')) {
            portletList_configuration.main.table.clear();
            portletList_configuration.main.table.destroy();
            portletList_configuration.main.table = null;
        }
    });
});
</script>
