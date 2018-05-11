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
#${n}portletBrowser .dataTables_filter, #${n}portletBrowser .first.paginate_button, #${n}portletBrowser .last.paginate_button{
    display: none;
}
#${n}portletBrowser .dataTables-inline, #${n}portletBrowser .column-filter-widgets {
    display: inline-block;
}
#${n}portletBrowser .dataTables_wrapper {
    width: 100%;
}
#${n}portletBrowser .dataTables_paginate .paginate_button {
    margin: 2px;
    color: #428BCA;
    cursor: pointer;
    *cursor: hand;
}
#${n}portletBrowser .dataTables_paginate .paginate_active {
    margin: 2px;
    color:#000;
}

#${n}portletBrowser .dataTables_paginate .paginate_active:hover {
    text-decoration: line-through;
}

#${n}portletBrowser table tr td a {
    color: #428BCA;
}

#${n}portletBrowser .dataTables-left {
    float:left;
}

#${n}portletBrowser .column-filter-widget {
    vertical-align: top;
    display: inline-block;
    overflow: hidden;
    margin-right: 5px;
}

#${n}portletBrowser .filter-term {
    display: block;
    text-align:bottom;
}

#${n}portletBrowser .dataTables_length label {
    font-weight: normal;
}
#${n}portletBrowser .datatable-search-view {
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
<div id="${n}portletBrowser" class="fl-widget portlet" role="section">
<form id="${n}form">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="most.frequently.added"/></h2>
  </div> <!-- end: portlet-title -->

  <!-- Portlet Toolbar -->
  <div class="portlet-toolbar" role="toolbar">
    <label for="${n}days">
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
  <div class="fl-widget-content portlet-body">

    <!-- Portlet Section -->
    <div id="${n}popularPortlets" class="portlet-section fl-pager" role="region">

      <div class="portlet-section-body">
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
        portletList_configuration.main.table = $("#${n}portletsTable").dataTable({
            iDisplayLength: portletList_configuration.main.pageSize,
            aLengthMenu: [5, 10, 20, 50],
            bServerSide: false,
            sAjaxSource: '${popularPortletCountsUrl}',
            sAjaxDataProp: "counts",
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
                { mData: 'portletFName', sType: 'string', sWidth: '50%' },  // Name
                { mData: 'count', sType: 'string', sWidth: '50%' }  // Times
            ],
            fnInitComplete: function (oSettings) {
                portletList_configuration.main.table.fnDraw();
            },
            fnServerData: function (sUrl, aoData, fnCallback, oSettings) {
                oSettings.jqXHR = $.ajax({
                    url: sUrl,
                    data: $("#${n}form").serialize(),
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
                // get deeplink anchor tag
                $('td:eq(0)', nRow).html( getDeepLinkAnchorTag(aData.portletFName, aData.portletDescription, aData.portletTitle) );
            },
            // Setting the top and bottom controls
            sDom: 'r<"row alert alert-info view-filter"<"toolbar-filter"><W><"toolbar-br"><"dataTables-inline dataTables-left"p><"dataTables-inline dataTables-left"i><"dataTables-inline dataTables-left"l>><"row"<"span12"t>>>',
            // Filtering
            oColumnFilterWidgets: { }
        });
    };

    initializeTable();
    $("#${n}days").change(initializeTable);
    // Adding formatting to sDom
    $("div.toolbar-br").html('<BR>');
    $("div.toolbar-filter").html('<B><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></B>:');
});
</script>
