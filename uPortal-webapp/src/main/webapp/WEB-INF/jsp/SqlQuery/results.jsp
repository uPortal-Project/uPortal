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
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<style>
#${n}resultBrowser .dt-search,
#${n}resultBrowser .first.dt-paging-button,
#${n}resultBrowser .last.dt-paging-button {
    display: none;
}
#${n}resultBrowser .dt-container {
    width: 100%;
}
#${n}resultBrowser .view-filter {
    padding: 10px 15px 15px;
    margin-bottom: 20px;
    font-size: 14px;
}
#${n}resultBrowser .view-filter h4 {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: bold;
}
#${n}resultBrowser .column-filter-widgets {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
}
#${n}resultBrowser .column-filter-widget {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
#${n}resultBrowser .column-filter-widget select {
    font-size: 14px;
}
#${n}resultBrowser .filter-term {
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
#${n}resultBrowser .filter-term:hover {
    background-color: #c4e3f3;
    text-decoration: none;
}
#${n}resultBrowser .dt-paging-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 4px;
    font-size: 14px;
}
#${n}resultBrowser .dt-paging-row .dt-info {
    white-space: nowrap;
}
#${n}resultBrowser .dt-paging-row .dt-length {
    display: flex;
    align-items: center;
    gap: 4px;
    white-space: nowrap;
}
#${n}resultBrowser .dt-paging-row .dt-length select {
    display: inline-block !important;
    width: auto;
    font-size: 14px;
}
#${n}resultBrowser .dt-paging-row .dt-length label {
    font-weight: normal;
    margin: 0;
}
#${n}resultBrowser .dt-paging .page-link {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    font-size: 14px;
}
#${n}resultBrowser .dt-paging .page-item.active .page-link {
    color: #000;
    background: none;
    border-color: transparent;
}
#${n}resultBrowser table tr td a {
    color: #428BCA;
}
</style>

<!-- Portlet -->
<div class="card portlet" role="section">
  
  <!-- Portlet Body -->
  <div id="${n}resultBrowser" class="card-body portlet-body">
        <div class="row alert alert-info view-filter" id="${n}viewFilter">
            <h4>Filters</h4>
            <div class="column-filter-widgets" id="${n}columnFilterWidgets"></div>
            <div class="dt-paging-row" id="${n}pagingRow"></div>
        </div>
        <table id="${n}sqlResults" style="width:100%;">
            <thead>
                <tr style="text-transform:capitalize">
                  <!-- Dynamically create number of columns -->
                    <c:forEach items="${ results[0] }" var="cell" varStatus="status">
                        <th>${ fn:escapeXml(cell.key) }</th>
                    </c:forEach>
                </tr>
            </thead>
        </table>
    
  </div>

</div>

<script type="text/javascript">
 up.jQuery(function() {
    var $ = up.jQuery;
    // create data set from model
    var results = [<c:forEach items="${ results }" var="row" varStatus="status">{<c:forEach items="${ row }" var="cell" varStatus="cellStatus">'column${ cellStatus.index }': '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ cell.value }</spring:escapeBody>'${ cellStatus.last ? '' : ','}</c:forEach>}${ status.last ? '' : ','}</c:forEach>];

    var resultList_configuration = {
        column: {
            <c:forEach items="${ results[0] }" var="row" varStatus="status">
                column${ status.index } : ${ status.index }${ status.last ? "" : ","}
            </c:forEach>
        },
        main: {
            table : null,
            pageSize: 10
        }
    };
    var initializeTable = function() {
        resultList_configuration.main.table = $("#${n}sqlResults").DataTable({
            pageLength: resultList_configuration.main.pageSize,
            lengthMenu: [5, 10, 20, 50],
            deferRender: false,
            processing: true,
            language: {
                lengthMenu: '_MENU_ per page',
                paginate: {
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
            data: results,
            columns: [
                <c:forEach items="${ results[0] }" var="row" varStatus="status">
                { data: 'column${ status.index }' }${ status.last ? "" : ","}
                </c:forEach>
            ],
            drawCallback: function() {
                var table = this.api();
                var $widgets = $('#${n}columnFilterWidgets');

                if (table.data().length > 0 && $widgets.children().length === 0) {
                    var selectedValues = [];
                    var searchFns = [];

                    function updateFilters() {
                        $.fn.dataTable.ext.search = $.fn.dataTable.ext.search.filter(function(fn) {
                            return searchFns.indexOf(fn) === -1;
                        });
                        searchFns.forEach(function(fn) {
                            $.fn.dataTable.ext.search.push(fn);
                        });
                        table.draw();
                    }

                    table.columns().every(function(colIdx) {
                        var header = $(table.column(colIdx).header()).text();
                        var colSelected = [];
                        selectedValues.push(colSelected);

                        var searchFn = (function(idx, arr) {
                            return function(settings, data, dataIndex) {
                                if (arr.length === 0) return true;
                                return arr.indexOf(data[idx]) !== -1;
                            };
                        })(colIdx, colSelected);
                        searchFns.push(searchFn);

                        var uniqueVals = table.column(colIdx).data().unique().sort();
                        var select = $('<select class="form-select form-select-sm"><option value="">' + header + '</option></select>');
                        uniqueVals.each(function(v) {
                            select.append('<option value="' + v + '">' + v + '</option>');
                        });
                        if (uniqueVals.length <= 1) select.prop('disabled', true);

                        var $widget = $('<div class="column-filter-widget"></div>').append(select);

                        select.on('change', function() {
                            var val = this.value;
                            if (val && colSelected.indexOf(val) === -1) {
                                colSelected.push(val);
                                var $term = $('<a class="filter-term" href="#">' + val + '</a>');
                                $term.on('click', function(e) {
                                    e.preventDefault();
                                    var text = $(this).text();
                                    colSelected.splice(colSelected.indexOf(text), 1);
                                    $(this).remove();
                                    updateFilters();
                                });
                                $widget.append($term);
                                updateFilters();
                            }
                            this.value = '';
                        });

                        $widgets.append($widget);
                    });
                }

                var $pagingRow = $('#${n}pagingRow');
                $('#${n}resultBrowser .dt-info').appendTo($pagingRow);
                $('#${n}resultBrowser .dt-length').appendTo($pagingRow);
                $('#${n}resultBrowser .dt-paging').appendTo($pagingRow);
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
 });
</script>
