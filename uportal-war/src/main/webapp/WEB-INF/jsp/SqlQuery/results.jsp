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

<!-- Portlet -->
<div class="portlet" role="section">
  
  <!-- Portlet Body -->
  <div id="${n}resultBrowser" class="portlet-body" role="main">
  
        <table id="${n}sqlResults" class="table table-bordered">
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
        resultList_configuration.main.table = $("#${n}sqlResults").dataTable({
        	pageLength: resultList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	pagingType: 'full_numbers',
        	processing: true,
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
            // use results right from the model instead of pulling from server via ajax
            data: results,
            // dynamically create columns
            columns: [
                <c:forEach items="${ results[0] }" var="row" varStatus="status">
                { data: 'column${ status.index }', type: 'string' }${ status.last ? "" : ","}
                </c:forEach>
            ],
            initComplete: function (oSettings) {
            	$(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
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
 });
</script>
