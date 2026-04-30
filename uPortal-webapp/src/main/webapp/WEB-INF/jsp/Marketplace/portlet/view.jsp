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
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables display
 */
#${n}marketplace table.display {
    margin: 0 auto;
    clear: both;
    width: 100%;
}

#${n}marketplace .dt-container {
    position: relative;
    clear: both;
    zoom: 1;
}

#${n}marketplace .dt-processing {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 250px;
    height: 30px;
    margin-left: -125px;
    margin-top: -15px;
    padding: 14px 0 2px 0;
    border: 1px solid #ddd;
    color: #999;
    font-size: 14px;
    background-color: white;
}

#${n}marketplace .dt-paging {
    white-space:nowrap;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables sorting
 */

#${n}marketplace .sorting_asc {
    background: url('<c:url value="/media/org/jasig/portal/channels/marketplace/sort_asc.png"/>') no-repeat center right;
}

#${n}marketplace .sorting_desc {
    background: url('<c:url value="/media/org/jasig/portal/channels/marketplace/sort_desc.png"/>') no-repeat center right;
}

#${n}marketplace .sorting {
    background: url('<c:url value="/media/org/jasig/portal/channels/marketplace/sort_both.png"/>') no-repeat center right;
}

#${n}marketplace .sorting_asc_disabled {
    background: url('<c:url value="/media/org/jasig/portal/channels/marketplace/sort_asc_disabled.png"/>') no-repeat center right;
}

#${n}marketplace .sorting_desc_disabled {
    background: url('<c:url value="/media/org/jasig/portal/channels/marketplace/sort_desc_disabled.png"/>') no-repeat center right;
}

#${n}marketplace table.display thead th:active,
#${n}marketplace table.display thead td:active {
    outline: none;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables row classes
 */
#${n}marketplace tr.odd td,
#${n}marketplace tr.odd th {
    background-color: #eee;
}

#${n}marketplace tr.even td,
#${n}marketplace tr.even th {
    background-color: #fff;
}

#${n}marketplace tbody tr:nth-child(odd) td,
#${n}marketplace tbody tr:nth-child(odd) th {
    background-color: #eee;
}

#${n}marketplace tbody tr:nth-child(even) td,
#${n}marketplace tbody tr:nth-child(even) th {
    background-color: #fff;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Misc
 */

#${n}marketplace .top{
    background-color: #F5F5F5;
 }

#${n}marketplace label>input{
    height: 30px;
    margin-right: 10px;
 }

#${n}marketplace .dt-info {
    float: none;
}

#${n}marketplace .clear {
    clear: both;
}

#${n}marketplace .dt-empty {
    text-align: center;
}

#${n}marketplace .example_alt_pagination div.dt-info {
    width: 40%;
}

#${n}marketplace .paging_full_numbers {
    width: 400px;
    height: 22px;
    line-height: 22px;
}

#${n}marketplace .paging_full_numbers a:active {
    outline: none;
}

#${n}marketplace .paging_full_numbers a:hover {
    text-decoration: none;
}

#${n}marketplace .pagination > .disabled > a {
    color: #333;
}

#${n}marketplace .paging_full_numbers a.paginate_button,
    .paging_full_numbers a.paginate_active {
    border: 1px solid #aaa;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    padding: 2px 5px;
    margin: 0 3px;
    cursor: pointer;
    *cursor: hand;
    color: #333 !important;
}

#${n}marketplace .paging_full_numbers a.paginate_button {
    background-color: #ddd;
}

#${n}marketplace .paging_full_numbers a.paginate_button:hover {
    background-color: #ccc;
    text-decoration: none !important;
}

#${n}marketplace .paging_full_numbers a.paginate_active {
    background-color: #99B3FF;
}

@media only screen and (max-width: 768px) {
    #${n}marketplace #unseen table td:nth-child(2),
    #unseen table th:nth-child(2){display: none;}
    #${n}marketplace .dt-search {
        text-align: left;
    }
    #${n}marketplace .dt-search>label {
        width: 100%;
        float: left;
    }
    #${n}marketplace .dt-search>label>input {
        width: 90%;
        float: right:
    }

    #${n}marketplacem tr.odd {
        background-color: #fff;
    }
}

#${n}marketplace .sort_info, .dataTables_info{
    text-align: left;
}

#${n}marketplace .marketplace_center_text, .dataTables_paginate,
.dataTables_processing{
    text-align:center;
}

#${n}marketplace .marketplace_center_text {
    margin-top: 30px;
}

#${n}marketplace .dt-length, .dt-search {
    text-align:right;
}

#${n}marketplace .dt-length select {
    width: auto;
    display: inline-block;
    font-size: 14px;
    font-weight: bold;
}

#${n}marketplace .dt-length label {
    font-size: 14px;
    font-weight: bold;
}

#${n}marketplace .dt-paging {
    text-align: center;
}

#${n}marketplace .dt-paging .pagination {
    display: inline-flex;
    flex-wrap: wrap;
    padding-left: 0;
    margin: 4px 0;
    border-radius: 4px;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button {
    list-style: none;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button .page-link {
    position: relative;
    float: left;
    padding: 6px 12px;
    line-height: 1.42857143;
    color: #428bca;
    text-decoration: none;
    background-color: #fff;
    border: 1px solid #ddd;
    margin-left: -1px;
    cursor: pointer;
    font-size: 14px;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button .page-link:hover {
    background-color: #eee;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button.active .page-link {
    color: #fff;
    background-color: #428bca;
    border-color: #428bca;
    z-index: 2;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button.disabled .page-link {
    color: #777;
    cursor: not-allowed;
    background-color: #fff;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button:first-child .page-link {
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
}

#${n}marketplace .dt-paging .pagination .dt-paging-button:last-child .page-link {
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
}

#${n}marketplace a:hover {
    cursor:pointer;
}

#${n}marketplace .${n}bottom{
    border-top: thin solid black;
    padding-top: 1em;
}

#${n}marketplace .marketplaceSection .card {
    padding: 0;
    border: none;
}

#${n}marketplace .marketplaceSection .card .portlet-box {
    height: 112px;
    max-height: 112px;
    margin: 0;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #dddddd;
    -webkit-border-radius: 8px;
    -moz-border-radius: 8px;
    box-shadow: 1px 1px 6px rgba(0, 0, 0, 0.25);
    -webkit-box-shadow: 1px 1px 6px rgba(0, 0, 0, 0.25);
    -moz-box-shadow: 1px 1px 6px rgba(0, 0, 0, 0.25);
    overflow: hidden;
}

#${n}marketplace .marketplaceSection .card .portlet-box:hover {
    background-color: #eee;
    cursor: pointer;
}

#${n}marketplace .marketplaceSection .card .portlet-box a {
    width: 100%;
    display: block;
}

#${n}marketplace .marketplaceSection .card .portlet-box a:hover {
    text-decoration: none;
    color: #000000;
}

#${n}marketplace .marketplaceSection .card .portlet-box .portlet-icon {
    width: 92px;
    height: 92px;
    max-height: 92px;
    background-color: #eee;
    margin-right: 15px;
    float: left;
    border:3px solid #999;
    text-align: center;
}

#${n}marketplace .marketplaceSection .card .portlet-box .portlet-icon img {
    width: 72px;
    height: 72px;
    margin-top: 7px;

}

#${n}marketplace .marketplaceSection .card .portlet-box .portlet-details {
    text-align: left;
    color: #000;
    margin-right: 0;
}

#${n}marketplace .marketplaceSection .card .portlet-box .portlet-details h5 {
    font-size: 16px;
    margin: 0 0 3px 0;
}


#${n}marketplace .marketplaceSection .card .portlet-box .portlet-details p {
    font-size: 11px;
    margin: 0;
}
</style>


<div id="${n}marketplace">

<c:if test="${fn:length(featuredList) > 0}">
    <div id="${n}featured" class="marketplaceSection">
        <div>
            <h3>
                <strong>
                    <spring:message code="marketplace.featured"/>
                </strong>
            </h3>
            <br>
        </div>
        <div class="row">
            <c:url value="/media/skins/icons/mobile/default.png" var="defaultIcon"/>
            <c:forEach var="featuredPortlet" items="${featuredEntries}" varStatus="status">
                <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                    <portlet:param name="action" value="view"/>
                    <portlet:param name="fName" value="${featuredPortlet.fname}"/>
                </portlet:renderURL>
                <div class="col-sm-6 col-lg-3">
                    <div class="card">
                        <div class="row portlet-box">
                            <a href="${entryURL}">
                                <div class="portlet-icon">
                                    <c:choose>
                                        <c:when test="${empty featuredPortlet.getParameter('mobileIconUrl')}">

                                            <img src="${defaultIcon}" alt="">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${featuredPortlet.getParameter('mobileIconUrl').value}" alt="">
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="portlet-details">
                                    <h5><c:out value="${featuredPortlet.title}"/></h5>
                                    <p><c:out value="${featuredPortlet.description}"/></p>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>
                <c:if test="${(status.index + 1) mod 4 == 0}">
                    <div class="clearfix"></div>
                </c:if>
            </c:forEach>
        </div>
    </div>
</c:if>

<div class="sort_filter_group">
    <div class="row">
        <div class="col-sm-1">
            <p>
                <strong>
                    <spring:message code="marketplace.browseBy"/>
                </strong>
            </p>
        </div>
        <div class="col-sm-8">
            <div class="sort_buttons_group">
                <button type="button" id="${n}alphabetical-sort-button" class="btn btn-secondary">
                    <spring:message code="marketplace.azIndex" />
                </button>
                <!-- Offer Browse By: Category (but only if there are categories in the list) -->
                <c:if test="${not empty categoryList}">
                    <button type="button" id="${n}category-sort-button" class="btn btn-secondary">
                        <spring:message code="marketplace.categories" />
                    </button>
                </c:if>
            </div>
        </div>
        <div class="col-sm-3">
            <div class="input-group">
                <input type="search" class="form-control" id="${n}portletTable_filter" placeholder="<spring:message code="marketplace.searchPortlets"/>" aria-label="<spring:message code="marketplace.searchPortlets"/>">
                <button class="btn btn-secondary" id="${n}clear_filter_button" aria-controls="${n}portletTable_filter">
                    <spring:message code="marketplace.clear" />
                </button>
            </div>
        </div>
    </div>
</div>


<div id="${n}categoryListContainer" class="marketplace_center_text card" style="display:none">
    <div class="card-body">
        <c:set var="categoryCount" value="0" />
        <c:forEach var="category" items="${categoryList}">
            <c:if test="${categoryCount mod 4 == 0}">
                <div class="row">
                    <div class="col-0 col-md-2"></div>
            </c:if>
            <div class="col-6 col-sm-3 col-md-2">
                <a class="${n}marketplace_category_link">
                    ${category.name}
                </a>
            </div>
            <c:if test="${(categoryCount+1) mod 4 == 0}">
                </div>
            </c:if>
            <c:set var="categoryCount" value="${categoryCount + 1}" />
        </c:forEach>
        <c:if test="${(categoryCount) mod 4 !=0}">
            </div>
        </c:if>
    </div>
</div>

<div id="unseen">
    <table id="${n}portletTable" class="display table">
        <thead>
            <tr>
                <th class="essential" style="border:none;" scope="col">
                    <spring:message code="marketplace.title" />
                </th>
                <th class="optional" style="border:none;" scope="col">
                    <spring:message code="marketplace.description" />
                </th>
                <th class="essential" style="border:none;" scope="col">
                    <spring:message code="marketplace.details" />
                </th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="portlet" items="${marketplaceEntries}">
                <tr>
                    <th class="essential" style="white-space: nowrap; border:none;" scope="row">
                        <strong><a href="${portlet.renderUrl}">${portlet.title} <i class="fa fa-external-link"></i></a></strong>
                    </th>
                    <td class="optional" style="border:none;">
                        ${portlet.description}
                    </td>
                    <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                        <portlet:param name="action" value="view"/>
                        <portlet:param name="fName" value="${portlet.fname}"/>
                    </portlet:renderURL>
                    <td class="essential" style="border:none;">
                        <a href="${entryURL}">
                            <spring:message code="marketplace.details" />
                            <i class="fa fa-edit"></i>
                        </a>
                    </td>
                    <td>
                        <c:forEach var="category" items="${portlet.marketplacePortletDefinition.parentCategories}">
                            ${category.name}
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>
</div>

<spring:message code="marketplace.mostPopular" var="mostPopular" />
<spring:message code="marketplace.audience" var="audience" />

<script type="text/javascript">
    var $ = up.jQuery;
    (function($) {
        up.jQuery(function() {
        $(function() {

            var trim11 =function(str) {
                str = str.replace(/^\s+/, '');
                for (var i = str.length - 1; i >= 0; i--) {
                    if (/\S/.test(str.charAt(i))) {
                        str = str.substring(0, i + 1);
                        break;
                    }
                }
                return str;
            };

            var applyEllipsis = function (nrow,col,txtlen){
                   var $cell = $('td:eq('+col+')', nrow);
                   $cell.text(ellipsis($cell.text(),txtlen));
                   return nrow;
            };

            var ellipsis = function(text, n) {
                text = trim11(text);
                if(text.length > n){
                  return text.substring(0,n)+"...";
                }
                else{
                  return text;
                }
            };

            var myDataTable = $('#${n}portletTable').DataTable({
                columnDefs: [{orderable: false, targets: [2]}, {visible: false, targets: [3]}],
                rowCallback: function(nRow, aData) {
                    applyEllipsis(nRow, 0, 75);
                },
                dom: '<rt<"row ${n}bottom"<"col-6 col-sm-8 col-md-3"i><"col-6 col-sm-4 col-md-3 order-md-2"l><"col-12 col-md-6 order-md-1"p>>',
                language: {
                    lengthMenu: '_MENU_ records per page',
                    paginate: {
                        previous: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                        next: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                    }
                },
                stateSave: false,
                autoWidth: false
            });

            $("#${n}featured").insertAfter($("#${n}marketplace .top"));

            var setFilter = function(text) {
                myDataTable.search('').column(3).search(text.trim()).draw();
            };

            var sortColumns = function(column) {
                myDataTable.order([column, 'asc']).draw();
            };

            var clearFilter = function() {
                myDataTable.search('').column(3).search('').draw();
                clearKeywordFilter();
                $("#${n}portletTable_filter").focus();
            };

            var clearKeywordFilter = function(){
                $("#${n}portletTable_filter").val("");
            }

            var disableCategoriesWidgets = function(){
                $("#${n}category-sort-button").removeClass("active");
                $("#${n}categoryListContainer").hide();
            }

            var enableCategoriesWidgets = function(){
                $("#${n}category-sort-button").addClass("active");
                $("#${n}categoryListContainer").show();
            }

            var enableAZIndexWidgets = function() {
                $("#${n}alphabetical-sort-button").addClass("active");
            } 

            var disableAZIndexWidgets = function() {
                $("#${n}alphabetical-sort-button").removeClass("active");
            } 

            $(".${n}marketplace_category_link").on('click', function(){
                setFilter(this.textContent);
                clearKeywordFilter();
            });

            disableCategoriesWidgets();
            enableAZIndexWidgets();

            $("#${n}alphabetical-sort-button").on('click', function(){
                disableCategoriesWidgets();
                enableAZIndexWidgets();
                clearFilter();
                sortColumns(0);
            });

            $("#${n}category-sort-button").on('click', function(){
                disableAZIndexWidgets();
                enableCategoriesWidgets();
            });

            $("#${n}portletTable_filter").keyup(function(e) {
                disableCategoriesWidgets();
                enableAZIndexWidgets();
                myDataTable.column(3).search('').search(this.value.trim()).draw();
            });

            $("#${n}clear_filter_button").on('click', function(){
                disableCategoriesWidgets();
                enableAZIndexWidgets();
                clearFilter();						
            });

            if("${initialFilter}"){
                setFilter("${initialFilter}");
            };
        });
    });
})(up.jQuery);
</script>
