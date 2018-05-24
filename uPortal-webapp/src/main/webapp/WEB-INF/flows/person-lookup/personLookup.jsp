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
#${n}personBrowser .dataTables_filter, #${n}personBrowser .first.paginate_button, #${n}personBrowser .last.paginate_button{
    display: none;
}
#${n}personBrowser .dataTables-inline, #${n}personBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}personBrowser .dataTables_wrapper {
    width: 100%;
}
#${n}personBrowser .dataTables_paginate .paginate_button {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    *cursor: hand;
}
#${n}personBrowser .dataTables_paginate .paginate_active {
    margin: 2px;
    color:#000;
}

#${n}personBrowser .dataTables_paginate .paginate_active:hover {
    text-decoration: line-through;
}

#${n}personBrowser table tr td a {
    color: #428BCA;
}

#${n}personBrowser .dataTables-left {
    float:left;
}

#${n}personBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}

#${n}personBrowser .filter-term {
    display: block;
    text-align:bottom;
}

#${n}personBrowser .dataTables_length label {
    font-weight: normal;
}
#${n}personBrowser .datatable-search-view {
    text-align:right;
}
</style>

<!-- Portlet -->
<div id="${n}personBrowser" class="fl-widget portlet prs-lkp view-lookup" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="search.for.users" />
        </h2>
    </div>

    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content">

        <div class="portlet-content">
            <form id="${n}searchForm" action="javascript:;">
                <div class="col-md-6">
                    <select id="${n}queryAttribute" class="form-control" name="queryAttribute" aria-label="<spring:message code="type"/>">
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

                        <a class="button btn btn-default" href="${ cancelUrl }">
                            <spring:message code="cancel" />
                        </a>
                    </div>
                </div>
            </form>

        <div id="${n}searchResults" class="portlet-section" style="display:none" role="region">
            <div class="titlebar">
                <h3 role="heading" class="title">Search Results</h3>
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
up.jQuery(function() {
    var $ = up.jQuery;

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
    // Url generating helper functions
    var getSelectPersonAnchorTag = function(displayName, userName) {
        var url = '${selectPersonUrl}'.replace("USERNAME", userName);
        return '<a href="' + url + '">' + displayName + '</a>';
    };
    var showSearchResults = function (queryData) {
        personList_configuration.main.table = $("#${n}resultsTable").dataTable({
            iDisplayLength: personList_configuration.main.pageSize,
            aLengthMenu: [5, 10, 20, 50],
            bServerSide: false,
            sAjaxSource: '<c:url value="/api/people.json"/>',
            sAjaxDataProp: "people",
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
                { mData: 'attributes.displayName', sType: 'string', sWidth: '50%' },  // Name
                { mData: 'attributes.username', sType: 'string', sWidth: '50%' }  // User Name
            ],
            fnInitComplete: function (oSettings) {
                personList_configuration.main.table.fnDraw();
            },
            fnServerData: function (sUrl, aoData, fnCallback, oSettings) {
                oSettings.jqXHR = $.ajax({
                    url: sUrl,
                    data: queryData,
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
                // Create links to user
                $('td:eq(0)', nRow).html( getSelectPersonAnchorTag(aData.attributes.displayName, aData.attributes.username) );
                $('td:eq(1)', nRow).html( getSelectPersonAnchorTag(aData.attributes.username, aData.attributes.username) );
            },
            // Setting the top and bottom controls
            sDom: 'r<"row alert alert-info view-filter"<"toolbar-filter"><W><"toolbar-br"><"dataTables-inline dataTables-left"p><"dataTables-inline dataTables-left"i><"dataTables-inline dataTables-left"l>><"row"<"span12"t>>>'
        });
        $("#${n}searchResults").show();
        // Adding formatting to sDom
        $("div.toolbar-br").html('<BR>');
        $("div.toolbar-filter").html('<B><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></B>:');
    };

    $(document).ready(function(){
        $("#${n}searchForm").submit(function() {
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
            if (personList_configuration.main.table != undefined && typeof personList_configuration.main.table.fnClearTable !== 'undefined') {
                $("#${n}searchResults").hide();
                personList_configuration.main.table.fnClearTable();
                personList_configuration.main.table.fnDestroy();
            }
            showSearchResults(queryData);
            return false;
        });
    });
});
</script>
</div>
