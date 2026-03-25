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

<portlet:renderURL var="selectPersonUrl" escapeXml="false">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="select"/>
    <portlet:param name="username" value="USERNAME"/>
</portlet:renderURL>

<portlet:renderURL var="cancelUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="_eventId" value="cancel"/>
</portlet:renderURL>

<c:set var="n"><portlet:namespace/></c:set>

<style>
#${n}personBrowser .dt-search,
#${n}personBrowser .first.dt-paging-button,
#${n}personBrowser .last.dt-paging-button {
    display: none;
}
#${n}personBrowser .dt-container {
    width: 100%;
}
#${n}personBrowser .view-filter {
    padding: 10px 15px 15px;
    margin-bottom: 0;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
    font-size: 14px;
}
#${n}personBrowser .view-filter h4 {
    margin: 0 0 6px;
    font-size: 14px;
    font-weight: bold;
}
#${n}personBrowser .column-filter-widgets {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
}
#${n}personBrowser .column-filter-widget {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
#${n}personBrowser .column-filter-widget select {
    font-size: 14px;
}
#${n}personBrowser .filter-term {
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
#${n}personBrowser .filter-term:hover {
    background-color: #c4e3f3;
    text-decoration: none;
}
#${n}personBrowser .dt-paging-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 4px;
    font-size: 14px;
}
#${n}personBrowser .dt-paging-row .dt-info {
    white-space: nowrap;
}
#${n}personBrowser .dt-paging-row .dt-length {
    display: flex;
    align-items: center;
    gap: 4px;
    white-space: nowrap;
}
#${n}personBrowser .dt-paging-row .dt-length select {
    display: inline-block !important;
    width: auto;
    font-size: 14px;
}
#${n}personBrowser .dt-paging-row .dt-length label {
    font-weight: normal;
    margin: 0;
}
#${n}personBrowser .dt-paging .page-link {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    font-size: 14px;
}
#${n}personBrowser .dt-paging .page-item.active .page-link {
    color: #000;
    background: none;
    border-color: transparent;
}
#${n}personBrowser table tr td a {
    color: #428BCA;
}
#${n}personBrowser .datatable-search-view {
    text-align: right;
}

/* Fix form layout to match release version */
#${n}searchForm {
    margin-bottom: 0;
}
#${n}searchForm .row {
    margin: 0;
    align-items: end;
}
#${n}searchForm .col-md-6 {
    padding-left: 0;
    padding-right: 15px;
}
#${n}searchForm .buttons {
    margin-top: 0;
}
/* Fix form control heights to match */
#${n}searchForm .form-select,
#${n}searchForm .form-control {
    height: 34px !important;
    padding: 6px 12px !important;
    font-size: 14px !important;
    line-height: 20px !important;
    box-sizing: border-box !important;
}
#${n}searchForm .form-select {
    padding-right: 30px !important;
}
</style>

<!-- Portlet -->
<div id="${n}personBrowser" class="card portlet prs-lkp view-lookup" role="section">

    <!-- Portlet Titlebar -->
    <div class="card-header titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="search.for.users" />
        </h2>
    </div>

    <!-- Portlet Content -->
    <div class="card-body content portlet-content">

        <div class="portlet-content">
            <form id="${n}searchForm" action="javascript:;" class="clearfix">
                <div class="row g-2">
                    <div class="col-md-6">
                        <select id="${n}queryAttribute" class="form-select" name="queryAttribute" aria-label="<spring:message code="type"/>">
                            <option value="">
                                <spring:message code="default.directory.attributes"/>
                            </option>
                            <c:forEach var="queryAttribute" items="${queryAttributes}">
                                <option value="${ queryAttribute }">
                                    <spring:message code="attribute.displayName.${queryAttribute}" text="${queryAttribute}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <input id="${n}queryValue" aria-label="<spring:message code="search.terms"/>" class="form-control" type="search" name="queryValue"/>
                    </div>
                    <div class="col-md-6">
                        <!-- Buttons -->
                        <div class="buttons">
                            <spring:message var="searchButtonText" code="search" />
                            <input class="button btn btn-primary" type="submit" value="${searchButtonText}" />
                            <a class="button btn btn-secondary" href="${ cancelUrl }">
                                <spring:message code="cancel" />
                            </a>
                        </div>
                    </div>
                </div>
            </form>

            <div id="${n}searchResults" class="portlet-section" style="display:none" role="region">
                <div class="titlebar">
                    <h3 role="heading" class="title">Search Results</h3>
                </div>
                <div class="row alert alert-info view-filter" id="${n}viewFilter">
                    <h4>Filters</h4>
                    <div class="column-filter-widgets" id="${n}columnFilterWidgets"></div>
                    <div class="dt-paging-row" id="${n}pagingRow"></div>
                </div>
                <table id="${n}resultsTable" class="portlet-table table table-bordered table-hover" style="width:100%;">
                    <thead>
                        <tr>
                            <th><spring:message code="name"/></th>
                            <th><spring:message code="username"/></th>
                        </tr>
                    </thead>
                </table>
            </div>

        </div>
    </div>

    <script type="text/javascript">
    // Move variables to global scope for debugging
    var attrs = ${up:json(queryAttributes)};
    var personList_configuration = {
        column: {
            name: 0,
            username: 1
        },
        main: {
            table : null,
            pageSize: 10
        }
    };
    
    up.jQuery(function() {
        var $ = up.jQuery;
        
        // Debug namespace
        console.log('Namespace: "${n}"');
        console.log('Form selector: "#${n}searchForm"');
        console.log('Form exists:', $("#${n}searchForm").length > 0);

        // Url generating helper functions
        var getSelectPersonAnchorTag = function(displayName, userName) {
            var url = '${selectPersonUrl}'.replace("USERNAME", userName);
            return '<a href="' + url + '">' + displayName + '</a>';
        };
        var showSearchResults = function (queryData) {
            personList_configuration.main.table = $("#${n}resultsTable").DataTable({
                pageLength: personList_configuration.main.pageSize,
                lengthMenu: [5, 10, 20, 50],
                serverSide: false,
                ajax: {
                    url: '<c:url value="/api/people.json"/>',
                    data: queryData,
                    dataSrc: "people",
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
                    { data: 'attributes.displayName', width: '50%' },
                    { data: 'attributes.username', width: '50%' }
                ],
                initComplete: function (settings) {
                    this.api().draw();
                },
                rowCallback: function (row, data, displayNum, displayIndex, dataIndex) {
                    $('td:eq(0)', row).html( getSelectPersonAnchorTag(data.attributes.displayName, data.attributes.username) );
                    $('td:eq(1)', row).html( getSelectPersonAnchorTag(data.attributes.username, data.attributes.username) );
                },
                drawCallback: function() {
                    var table = this.api();

                    // Build filters once data is loaded
                    var $widgets = $('#${n}columnFilterWidgets');
                    if (table.data().length > 0 && $widgets.children().length === 0) {
                        var selectedNames = [];
                        var selectedUsernames = [];

                        var nameSearchFn = function(settings, data, dataIndex) {
                            if (selectedNames.length === 0) return true;
                            var rowData = table.row(dataIndex).data();
                            var val = Array.isArray(rowData.attributes.displayName) ? rowData.attributes.displayName[0] : rowData.attributes.displayName;
                            return selectedNames.indexOf(val) !== -1;
                        };
                        var usernameSearchFn = function(settings, data, dataIndex) {
                            if (selectedUsernames.length === 0) return true;
                            var rowData = table.row(dataIndex).data();
                            var val = Array.isArray(rowData.attributes.username) ? rowData.attributes.username[0] : rowData.attributes.username;
                            return selectedUsernames.indexOf(val) !== -1;
                        };

                        function updateFilters() {
                            $.fn.dataTable.ext.search = $.fn.dataTable.ext.search.filter(function(fn) {
                                return fn !== nameSearchFn && fn !== usernameSearchFn;
                            });
                            if (selectedNames.length > 0) $.fn.dataTable.ext.search.push(nameSearchFn);
                            if (selectedUsernames.length > 0) $.fn.dataTable.ext.search.push(usernameSearchFn);
                            table.draw();
                        }

                        function makeWidget(selectEl, selectedArr, updateFn) {
                            var $widget = $('<div class="column-filter-widget"></div>').append(selectEl);
                            selectEl.on('change', function() {
                                var val = this.value;
                                if (val && selectedArr.indexOf(val) === -1) {
                                    selectedArr.push(val);
                                    var $term = $('<a class="filter-term" href="#">' + val + '</a>');
                                    $term.on('click', function(e) {
                                        e.preventDefault();
                                        var text = $(this).text();
                                        selectedArr.splice(selectedArr.indexOf(text), 1);
                                        $(this).remove();
                                        updateFn();
                                    });
                                    $widget.append($term);
                                    updateFn();
                                }
                                this.value = '';
                            });
                            return $widget;
                        }

                        var nameSelect = $('<select class="form-select form-select-sm"><option value="">Name</option></select>');
                        table.column(0).data().unique().sort().each(function(d) {
                            var v = Array.isArray(d) ? d[0] : d;
                            nameSelect.append('<option value="' + v + '">' + v + '</option>');
                        });

                        var usernameSelect = $('<select class="form-select form-select-sm"><option value="">Username</option></select>');
                        table.column(1).data().unique().sort().each(function(d) {
                            var v = Array.isArray(d) ? d[0] : d;
                            usernameSelect.append('<option value="' + v + '">' + v + '</option>');
                        });

                        $widgets
                            .append(makeWidget(nameSelect, selectedNames, updateFilters))
                            .append(makeWidget(usernameSelect, selectedUsernames, updateFilters));
                    }

                    // Move DT-rendered info/length/paging into our static paging row
                    var $pagingRow = $('#${n}pagingRow');
                    $('#${n}personBrowser .dt-info').appendTo($pagingRow);
                    $('#${n}personBrowser .dt-length').appendTo($pagingRow);
                    $('#${n}personBrowser .dt-paging').appendTo($pagingRow);
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
                },
            });
            
            $("#${n}searchResults").show();
        };

        $(function(){
            // Find form dynamically if namespace fails
            var $form = $("#${n}searchForm");
            if ($form.length === 0) {
                $form = $("form[id*='searchForm']");
                console.log('Using fallback form selector, found:', $form.length, 'forms');
            }
            
            // Also find table with fallback
            var $table = $("#${n}resultsTable");
            if ($table.length === 0) {
                $table = $("table[id*='resultsTable']");
                console.log('Using fallback table selector, found:', $table.length, 'tables');
            }
            
            $form.on('submit', function(e) {
                e.preventDefault();
                var queryData = { searchTerms: [] };
                var searchTerm = $("#${n}queryValue").val();
                var queryTerm = $("#${n}queryAttribute").val();

                // if no search term present do not submit form
                if (searchTerm.length == 0) {
                    $("#${n}searchResults").hide();
                    return false;
                }

                if (!queryTerm) {
                    $(attrs).each(function (idx, attr) {
                        queryData.searchTerms.push(attr);
                        queryData[attr] = searchTerm;
                    });
                } else {
                    queryData.searchTerms.push(queryTerm);
                    queryData[queryTerm] = searchTerm;
                }
                
                // To allow the datatable to be repopulated to search multiple times
                // clear and destroy the original
                if (personList_configuration.main.table != undefined && $.fn.DataTable.isDataTable('#${n}resultsTable')) {
                    $("#${n}searchResults").hide();
                    $('#${n}columnFilterWidgets').empty();
                    $('#${n}pagingRow').empty();
                    personList_configuration.main.table.clear().draw();
                    personList_configuration.main.table.destroy();
                    personList_configuration.main.table = null;
                }
                showSearchResults(queryData);
                return false;
            });
            
            // Cleanup on page unload to prevent memory leaks
            $(window).on('beforeunload', function() {
                if (personList_configuration.main.table != undefined && $.fn.DataTable.isDataTable('#${n}resultsTable')) {
                    personList_configuration.main.table.clear();
                    personList_configuration.main.table.destroy();
                    personList_configuration.main.table = null;
                }
            });
        });
    });
    </script>
</div>
