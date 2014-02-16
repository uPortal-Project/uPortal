<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
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
<div id="${n}portletBrowser" class="fl-widget portlet ptl-mgr view-home" role="section">
  
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="portlet.registry"/></h2>
    <div class="fl-col-flex2 toolbar" role="toolbar">
      <div class="fl-col">
        <ul class="btn-group">
          <li class="btn"><a class="button" href="${ newPortletUrl }" title="<spring:message code="register.new.portlet"/>"><span><spring:message code="register.new.portlet"/></span></a></li>
        </ul>
      </div>
      <div class="fl-col fl-text-align-right datatable-search-view">
        <form class="portlet-search-form" style="display:inline">
            <label><spring:message code="search"/></label>
            <input type="text" id="${n}portletListSearch" class="portlet-search-input"/>
        </form>
      </div>
    </div>
    <div style="clear:both"></div>
  </div>

  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
      <div>
        <table id="${n}portletsList" class="portlet-table table table-bordered table-hover" style="width:100%;">
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
      </div>
  </div> <!-- end: portlet-body -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {

var $ = up.jQuery;

var editPortletUrl = "${ editPortletUrl }";
var removePortletUrl = "${ removePortletUrl }";
var editMessage = '<spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/>';
var deleteMessage = '<spring:message code="delete" htmlEscape="false" javaScriptEscape="true"/>';
var lengthMenuMessage = '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>';
var paginatePrevious = '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>';
var paginateNext = '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>';

var portletList_configuration = {
    column: {
        name: 0,
        type: 1,
        Lifecycle: 2,
        placeHolderForEditLink: 3,
        placeHolderForDeleteLink: 4,
        categories: 5
    },
    main: {
        table : '',
        pageSize: 10,
        showHiddenCols: false
    }
};

// Created as its own 
$.setupTable = function() {
    portletList_configuration.main.table = $("#${n}portletsList").dataTable({
        iDisplayLength: portletList_configuration.main.pageSize,
        aLengthMenu: [5, 10, 20, 50],
        bServerSide: false,
        sAjaxSource: '/uPortal/api/dataTable/ManagePortlets/List',
        sAjaxDataProp: "dataTablesResponse.aaData",
        bDeferRender: false,
        bProcessing: true,
        bAutoWidth:false,
        sPaginationType: 'full_numbers',
        oLanguage: {
            sLengthMenu: lengthMenuMessage,
            oPaginate: {
                sPrevious: paginatePrevious,
                sNext: paginateNext
            }
        },
        aoColumns: [
            { sType: 'html', 'sWidth' : '30%' },  // Name
            { sType: 'html', 'sWidth' : '30%' },  // Type 
            { sType: 'html', 'sWidth' : '20%' },  // Lifecycle State
            { sType: 'html', 'bSearchable': false, 'sWidth' : '10%' },  // Edit Link
            { sType: 'html', 'bSearchable': false, 'sWidth' : '10%' },  // Delete Link
            {
                bSearchable: true,
                bVisible: portletList_configuration.main.showHiddenCols,
                asSorting: [ "desc", "asc" ]
            }  // Categories - hidden 
        ],
        fnInitComplete: function (oSettings) {
            portletList_configuration.main.table.fnDraw();
        },
        fnServerData: function (sUrl, aoData, fnCallback, oSettings) {
            oSettings.jqXHR = $.ajax({
                url: sUrl,
                data: aoData,
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
            // Create edit and delete links
            $('td:eq(3)', nRow).html( $.getEditURL(aData[3]) );
            $('td:eq(4)', nRow).html( $.getDeleteURL(aData[3]) );
        },
        // Setting the top and bottom controls
        sDom: 'r<"row alert alert-info view-filter"<"dataTables-inline"W><"dataTables-inline dataTables-right"l><"dataTables-inline dataTables-right"i><"dataTables-inline dataTables-right"p>><"row"<"span12"t>>>',//'<"top row-fluid"W<"pull-right pagination"l><"pull-right pagination"p>>rt<"bottom"i><"clear">',
        // Filtering
        oColumnFilterWidgets: {
            sSeparator: ',', // Used for multivalue column Categories
            aiExclude: [portletList_configuration.column.name,
                          portletList_configuration.column.type,
                          portletList_configuration.column.placeHolderForEditLink,
                          portletList_configuration.column.placeHolderForDeleteLink]
        }
    });
};

// Url generating helper functions
$.getEditURL = function(portletId) {
  var editURL = editPortletUrl.replace("PORTLETID", portletId);
  return '<a href="' + editURL + '">' + editMessage + '</a>';
};

$.getDeleteURL = function(portletId) {
  var editURL = removePortletUrl.replace("PORTLETID", portletId);
  return '<a href="' + editURL + '">' + deleteMessage + '</a>';
};

$(document).ready(function() {
    $.setupTable();
    // Hide the out of the box search and populate it with our text box
    $('#${n}portletListSearch').keyup(function(){
      portletList_configuration.main.table.fnFilter( $(this).val() );
    });
    // Hide the first and last page buttons on paginator
    $(".first.paginate_button, .last.paginate_button").hide();
});
});
</script>
