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

#${n}marketplace  .dataTables_filter {
    width: 100%;
    float: right;
}

#${n}marketplace  .dataTables_info, .dataTables_length, .dataTables_paginate{
    white-space:nowrap;
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables sorting
 */

#${n}marketplace .sorting_asc {
    background: url('/uPortal/media/org/jasig/portal/channels/marketplace/sort_asc.png') no-repeat center right;
}

#${n}marketplace .sorting_desc {
    background: url('/uPortal/media/org/jasig/portal/channels/marketplace/sort_desc.png') no-repeat center right;
}

#${n}marketplace .sorting {
    background: url('/uPortal/media/org/jasig/portal/channels/marketplace/sort_both.png') no-repeat center right;
}

#${n}marketplace .sorting_asc_disabled {
    background: url('/uPortal/media/org/jasig/portal/channels/marketplace/sort_asc_disabled.png') no-repeat center right;
}

#${n}marketplace .sorting_desc_disabled {
    background: url('/uPortal/media/org/jasig/portal/channels/marketplace/sort_desc_disabled.png') no-repeat center right;
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
    width:90%;
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
    outline: none
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

@media only screen and (max-width: 992px){
    #${n}marketplace .sort-btn-group{
        width: 100%;
    }
    #${n}marketplace .alphabetical-sort-button{
        width: 50%;
    }
    #${n}marketplace .category-sort-button{
        width: 50%;
    }
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
.btn_group_container, .dataTables_processing{
    text-align:center;
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

#${n}marketplace .marketplaceSection{
    border-bottom-style:dotted;
    border-width:thin;
}

</style>


<div id="${n}marketplace">

<c:if test="${fn:length(featuredList) > 0}">
    <div id="${n}featured" class="marketplaceSection">
        <div>
            <span><strong><spring:message code="featured" text="Featured" /></strong></span><br>
        </div>
        <c:set var="endRowPortletCounter" value="0"/>
        <div class="row">
            <c:if test="${fn:length(featuredList)mod 2!=0 }">
                <div class="col-xs-3">
                <c:set var="endRowPortletCounter" value="1" />
                </div>
            </c:if>
            <c:forEach var="featuredPortlet" items="${featuredList}" varStatus="status">
                <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                    <portlet:param name="action" value="view"/>
                    <portlet:param name="fName" value="${featuredPortlet.FName}"/>
                </portlet:renderURL>
                <div class="col-xs-6 col-sm-6 text-center">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                        <a href="${entryURL}">
                            <div>
                                <c:out value="${featuredPortlet.title}"/>
                            </div>
                        </a>
                        </div>
                        <div class="panel-body">
                            <div>
                                <c:out value="${featuredPortlet.description}"/>
                            </div>
                        </div>
                    </div>
                </div>
                <c:if test="${(endRowPortletCounter + status.count) mod 2 ==0}">
                    <div class="clearfix"></div>
                </c:if>
            </c:forEach>
        </div>
    </div>
</c:if>


<div id="${n}categoryListContainer" class="marketplace_center_text panel panel-default" style="display:none">
    <div class="panel-body">
        <c:set var="categoryCount" value="0"/>
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
            <c:forEach var="portlet" items="${channelBeanList}">
                <tr>
                    <td class="essential" style="white-space: nowrap; border:none;">
                        <a href="${portlet.renderUrl}">${portlet.title}</a>
                    </td>
                    <td class="optional" style="border:none;">
                        ${portlet.description}
                    </td>
                    <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                        <portlet:param name="action" value="view"/>
                        <portlet:param name="fName" value="${portlet.FName}"/>
                    </portlet:renderURL>
                    <td class="essential" style="border:none;">
                        <a href="${entryURL}"><spring:message code="label.details" text="Details" /></a>
                    </td>
                    <td>
                        <c:forEach var="category" items="${portlet.parentCategories}">
                            ${category.name}
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>
</div>

<spring:message code="label.search" var="labelSearch" text="Search" />
<spring:message code="label.mostPopular" var="mostPopular" text="Most Popular" />
<spring:message code="label.azIndex" var="azIndex" text="A-Z index" />
<spring:message code="label.audience" var="audience" text="Audience" />
<spring:message code="label.browseby" var="browseBy" text="Browse by" />
<spring:message code="category" var="categoryLabel" text="Category" />

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
                   $cell.text(ellipsis($cell.text(),txtlen)); // ellipsis() from 2. above
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
                       // *** NOTE *** applyEllipsis(nrow,col,txtlen) col is 0-based,
                       // however, don't include hidden columns in count
                       applyEllipsis(nRow,1,75);
                      },
                "sDom": '<"top"f><"sort_info"><"${n}sort_buttons"><rt'+
                    '<"row ${n}bottom" <"col-xs-6 col-md-3" i>'+
                    '<"col-xs-6 col-md-push-6 col-md-3"l>'+
                    '<"col-xs-12 col-md-pull-3 col-md-6"p>>',
                "bStateSave": true,
                "bAutoWidth":false
            });

            $("#${n}marketplace div.${n}sort_buttons")
            .html("<div class=\"btn_group_container\">"+
                "<div class=\"btn-group sort-btn-group\">"+
                "<button type=\"button\" id=\"${n}alphabetical-sort-button\" class=\"btn btn-default alphabetical-sort-button\">${azIndex}</button>"+
                "<button type=\"button\" id=\"${n}category-sort-button\" class=\"btn btn-default category-sort-button\">${categoryLabel}</button>"+
                "</div><br><br></div>");
            $("#${n}categoryListContainer").insertAfter($(".${n}sort_buttons"));
            $("#${n}featured").insertAfter($("#${n}marketplace .top"));
            $("#${n}marketplace div.sort_info").html("<div><BR><BR><span><strong>${browseBy}</strong><br><br></div>");
            $("#${n}marketplace div.dataTables_filter").append("<form action='${entryURL}'><button>${labelSearch}</button></form><br>");

            var setFilter = function(text){
                myDataTable.fnFilter(text);
            };

            var sortColumns = function(column){
                myDataTable.fnSort([[column, 'asc']]);
            }
            
            $("#${n}marketplace .dataTables_filter").addClass("marketplaceSection");

            $(".${n}marketplace_category_link").click(function(){
                setFilter(this.textContent);
            });

            $("#${n}alphabetical-sort-button").click(function(){
                sortColumns(0);
            });

            $("#${n}category-sort-button").click(function(){
                $("#${n}categoryListContainer").toggle();
            });

            if("${initialFilter}"){
                setFilter("${initialFilter}");
            };
        });
    });
})(up.jQuery);
</script>
