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
<style>
#${n}portletBrowser .dt-search,
#${n}portletBrowser .first.dt-paging-button,
#${n}portletBrowser .last.dt-paging-button {
    display: none;
}
#${n}portletBrowser .dt-container {
    width: 100%;
}
#${n}portletBrowser .column-filter-widgets {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 4px;
}
#${n}portletBrowser .column-filter-widget select {
    font-size: 14px;
}
#${n}portletBrowser .filter-term {
    display: inline-block;
    background: #d9edf7;
    border: 1px solid #bce8f1;
    border-radius: 3px;
    padding: 2px 6px;
    font-size: 13px;
    cursor: pointer;
    margin-top: 4px;
}
#${n}portletBrowser .filter-term::after {
    content: ' \00d7';
}
#${n}portletBrowser .view-filter {
    padding: 10px 15px 15px;
    margin-bottom: 20px;
    font-size: 14px;
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
<div id="${n}portletBrowser" class="card portlet" role="section">
<form id="${n}form">

  <!-- Portlet Title -->
  <div class="card-header portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="most.frequently.added"/></h2>
  </div> <!-- end: portlet-title -->

  <!-- Portlet Toolbar -->
  <div class="portlet-toolbar" role="toolbar">
    <label for="${n}days" class="form-label">
      <spring:message code="previous"/>
    </label>
    <select id="${n}days" name="days">
      <option value="1">1</option>
      <option value="7">7</option>
      <option value="30" selected="selected">30</option>
      <option value="90">90</option>
      <option value="365">365</option>
    </select>
  </div> <!-- end: portlet-toolbar -->

    <!-- Portlet Body -->
  <div class="card-body portlet-body">

    <!-- Portlet Section -->
    <div id="${n}popularPortlets" class="portlet-section fl-pager" role="region">

      <div class="portlet-section-body">
          <div class="row alert alert-info view-filter" id="${n}viewFilter">
              <div class="toolbar-filter"><b>Filters</b>:</div>
              <div>
                  <div class="column-filter-widgets" id="${n}columnFilterWidgets"></div>
              </div>
              <div class="toolbar-br"><br/></div>
              <div class="dt-paging-row" id="${n}pagingRow"></div>
          </div>
          <table id="${n}portletsTable" style="width:100%;">
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
          <portlet:renderURL var="doneUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="done" />
          </portlet:renderURL>
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
        portletList_configuration.main.table = $("#${n}portletsTable").DataTable({
            pageLength: portletList_configuration.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            serverSide: false,
            ajax: {
                url: '${popularPortletCountsUrl}',
                data: function(d) {
                    var formData = $("#${n}form").serializeArray();
                    formData.forEach(function(item) { d[item.name] = item.value; });
                },
                dataSrc: 'counts',
                error: function(xhr, error, thrown) {
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
                { data: 'portletFName', width: '50%' },
                { data: 'count', width: '50%' }
            ],
            initComplete: function() {
                this.api().draw();
            },
            rowCallback: function(row, data) {
                $('td:eq(0)', row).html(getDeepLinkAnchorTag(data.portletFName, data.portletDescription, data.portletTitle));
            },
            drawCallback: function() {
                var table = this.api();
                var $widgets = $('#${n}columnFilterWidgets');
                var $pagingRow = $('#${n}pagingRow');
                if (table.data().length > 0 && $widgets.children().length === 0) {
                    var searchFns = [];
                    table.columns().every(function() {
                        var col = this;
                        var colIdx = col.index();
                        var header = $(col.header()).text().trim();
                        var colSelected = [];
                        var vals = [];
                        col.data().each(function(v) {
                            var s = String(v);
                            if (vals.indexOf(s) === -1) vals.push(s);
                        });
                        vals.sort();
                        var $wrap = $('<div class="column-filter-widget"></div>');
                        var $sel = $('<select></select>');
                        $sel.append($('<option value="">' + header + '</option>'));
                        vals.forEach(function(v) { $sel.append($('<option></option>').val(v).text(v)); });
                        if (vals.length <= 1) $sel.prop('disabled', true);
                        var searchFn = (function($sel, colSelected, col) {
                            return function(settings, data, dataIndex) {
                                if (colSelected.length === 0) return true;
                                var cellVal = String(col.data()[dataIndex] !== undefined ? col.data()[dataIndex] : data[col.index()]);
                                return colSelected.indexOf(cellVal) !== -1;
                            };
                        })($sel, colSelected, col);
                        searchFns.push(searchFn);
                        $.fn.dataTable.ext.search.push(searchFn);
                        $sel.on('change', function() {
                            var v = $(this).val();
                            if (!v || colSelected.indexOf(v) !== -1) return;
                            colSelected.push(v);
                            $(this).val('');
                            var $term = $('<span class="filter-term"></span>').text(v);
                            $term.on('click', function() {
                                var idx = colSelected.indexOf(v);
                                if (idx !== -1) colSelected.splice(idx, 1);
                                $(this).remove();
                                table.draw();
                            });
                            $wrap.append($term);
                            table.draw();
                        });
                        $wrap.append($sel);
                        $widgets.append($wrap);
                    });
                }
                $('#${n}portletBrowser .dt-info').appendTo($pagingRow);
                $('#${n}portletBrowser .dt-length').appendTo($pagingRow);
                $('#${n}portletBrowser .dt-paging').appendTo($pagingRow);
            },
            layout: {
                topStart: null,
                topEnd: null,
                top: null,
                bottomStart: null,
                bottom: { features: ['info', 'pageLength', 'paging'] },
                bottomEnd: null
            }
        });
    };

    initializeTable();
    $("#${n}days").change(initializeTable);
});
</script>
