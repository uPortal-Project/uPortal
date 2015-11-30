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

#${n}marketplace .dataTables_wrapper {
    position: relative;
    clear: both;
    zoom: 1; /* Feeling sorry for IE */
}

#${n}marketplace  .dataTables_processing {
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

#${n}marketplace .dataTables_paginate{
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
#${n}marketplace tr.odd {
    background-color: #eee;
}

#${n}marketplace tr.even {
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

#${n}marketplace.top .dataTables_info {
    float: none;
}

#${n}marketplace .clear {
    clear: both;
}

#${n}marketplace .dataTables_empty {
    text-align: center;
}

#${n}marketplace .example_alt_pagination div.dataTables_info {
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
    #${n}marketplace #unseen table th:nth-child(1),
    #unseen table th:nth-child(3),
    #unseen table td:nth-child(2),
    #unseen table th:nth-child(2){display: none;}
    #${n}marketplace .dataTables_filter{
        text-align: left;
    }
    #${n}marketplace .dataTables_filter>label{
        width: 100%;
        float: left;
    }
    #${n}marketplace .dataTables_filter>label>input{
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

#${n}marketplace .dataTables_length, .dataTables_filter {
    text-align:right;
}

#${n}marketplace a:hover {
    cursor:pointer;
}

#${n}marketplace .${n}bottom{
    border-top: thin solid black;
    padding-top: 1em;
}

#${n}marketplace .marketplaceSection .panel {
    padding: 0;
    border: none;
}

#${n}marketplace .marketplaceSection .panel .portlet-box {
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

#${n}marketplace .marketplaceSection .panel .portlet-box:hover {
    background-color: #eee;
    cursor: pointer;
}

#${n}marketplace .marketplaceSection .panel .portlet-box a {
    width: 100%;
    display: block;
}

#${n}marketplace .marketplaceSection .panel .portlet-box a:hover {
    text-decoration: none;
    color: #000000;
}

#${n}marketplace .marketplaceSection .panel .portlet-box .portlet-icon {
    width: 92px;
    height: 92px;
    max-height: 92px;
    background-color: #eee;
    margin-right: 15px;
    float: left;
    border:3px solid #999;
    text-align: center;
}

#${n}marketplace .marketplaceSection .panel .portlet-box .portlet-icon img {
    width: 72px;
    height: 72px;
    margin-top: 7px;

}

#${n}marketplace .marketplaceSection .panel .portlet-box .portlet-details {
    text-align: left;
    color: #000;
    margin-right: 0;
}

#${n}marketplace .marketplaceSection .panel .portlet-box .portlet-details h5 {
    font-size: 16px;
    margin: 0 0 3px 0;
}


#${n}marketplace .marketplaceSection .panel .portlet-box .portlet-details p {
    font-size: 11px;
    margin: 0;
}
</style>


<div id="${n}marketplace">

<c:if test="${fn:length(featuredList) > 0}">
    <div id="${n}featured" class="marketplaceSection">
        <div>
            <h3><strong><spring:message code="featured" text="Featured" /></strong></h3><br>
        </div>
        <div class="row">
            <c:url value="/media/skins/icons/mobile/default.png" var="defaultIcon"/>
            <c:forEach var="featuredPortlet" items="${featuredEntries}" varStatus="status">
                <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                    <portlet:param name="action" value="view"/>
                    <portlet:param name="fName" value="${featuredPortlet.fname}"/>
                </portlet:renderURL>
                <div class="col-sm-6 col-lg-3">
                    <div class="panel panel-default">
                        <div class="row portlet-box">
                            <a href="${entryURL}">
                                <div class="portlet-icon">
                                    <c:choose>
                                        <c:when test="${empty featuredPortlet.getParameter('mobileIconUrl')}">

                                            <img src="${defaultIcon}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${featuredPortlet.getParameter('mobileIconUrl').value}">
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
            <p><strong><spring:message code="label.browseBy" text="Browse By"/></strong></p>
        </div>
        <div class="col-sm-8">
            <div class="sort_buttons_group">
                <button type="button" id="${n}alphabetical-sort-button" class="btn btn-default"><spring:message code="label.azIndex" text="A-Z Index" /></button>
                <!-- Offer Browse By: Category (but only if there are categories in the list) -->
                <c:if test="${not empty categoryList}">
                <button type="button" id="${n}category-sort-button" class="btn btn-default"><spring:message code="label.category" text="Categories" /></button>
                </c:if>
            </div>
        </div>
        <div class="col-sm-3">
            <div class="input-group">
                <input type="text" class="form-control" id="${n}portletTable_filter" placeholder="Search Portlets">
                <span class="input-group-btn">
                    <button class="btn btn-default" id="${n}clear_filter_button"><spring:message code="label.clear" text="Clear" /></button>
                </span>
            </div>
        </div>
    </div>
</div>


<div id="${n}categoryListContainer" class="marketplace_center_text panel panel-default" style="display:none">
    <div class="panel-body">
        <c:set var="categoryCount" value="0" />
        <c:forEach var="category" items="${categoryList}">
            <c:if test="${categoryCount mod 4 == 0}">
                <div class="row">
                    <div class="col-xs-0 col-md-2"></div>
            </c:if>
            <div class="col-xs-6 col-sm-3 col-md-2">
                <a class="${n}marketplace_category_link">${category.name}</a>
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
                <th class="essential" style="border:none;">
                    <spring:message code="label.title" text="Title" />
                </th>
                <th class="optional" style="border:none;">
                    <spring:message code="label.description" text="Description" />
                </th>
                    <th class="essential" style="border:none;">
                </th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="portlet" items="${marketplaceEntries}">
                <tr>
                    <td class="essential" style="white-space: nowrap; border:none;">
                        <strong><a href="${portlet.renderUrl}">${portlet.title} <i class="fa fa-external-link"></i></a></strong>
                    </td>
                    <td class="optional" style="border:none;">
                        ${portlet.description}
                    </td>
                    <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                        <portlet:param name="action" value="view"/>
                        <portlet:param name="fName" value="${portlet.fname}"/>
                    </portlet:renderURL>
                    <td class="essential" style="border:none;">
                        <a href="${entryURL}"><spring:message code="label.details" text="Details" /> <i class="fa fa-edit"></i></a>
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

<spring:message code="label.mostPopular" var="mostPopular" text="Most Popular" />
<spring:message code="label.audience" var="audience" text="Audience" />

<script type="text/javascript">
    var $ = up.jQuery;
    (function($) {
        up.jQuery(function() {
        $(document).ready(function() {

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

            var myDataTable = $('#${n}portletTable').dataTable({
                "aoColumnDefs": [{"bSortable": false, "aTargets": [ 2 ] }, { "bVisible": false, "aTargets": [ 3 ] }],
                "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull){
                       applyEllipsis(nRow,1,75);
                      },
                "sDom": '<rt'+
                    '<"row ${n}bottom" <"col-xs-6 col-sm-8 col-md-3" i>'+
                    '<"col-xs-6 col-md-push-6 col-sm-4 col-md-3"l>'+
                    '<"col-xs-12 col-md-pull-3 col-md-6"p>>',
                "bStateSave": false,
                "bAutoWidth":false
            });

            $("#${n}featured").insertAfter($("#${n}marketplace .top"));

            var setFilter = function(text){
                myDataTable.fnFilter(text);
            };

            var sortColumns = function(column){
                myDataTable.fnSort([[column, 'asc']]);
            }

            $(".${n}marketplace_category_link").click(function(){
                setFilter(this.textContent);
            });

            $("#${n}alphabetical-sort-button").addClass("active");
            $("#${n}category-sort-button").removeClass("active");

            $("#${n}alphabetical-sort-button").click(function(){
                sortColumns(0);
                $(this).toggleClass("active");
            });

            $("#${n}category-sort-button").click(function(){
                $("#${n}categoryListContainer").toggle();
                $(this).toggleClass("active");
            });

            $("#${n}portletTable_filter").keyup(function(e) {
                setFilter(this.value);
            });

            $("#${n}clear_filter_button").click(function(){
                myDataTable.fnFilter("");
                $("#${n}portletTable_filter").val("").focus();
            });

            if("${initialFilter}"){
                setFilter("${initialFilter}");
            };
        });
    });
})(up.jQuery);
</script>


