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
/*TODO : namespace these so that they only effect this page */
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables display
 */
table.display {
    margin: 0 auto;
    clear: both;
    width: 100%;
}

.dataTables_wrapper {
    position: relative;
    clear: both;
    zoom: 1; /* Feeling sorry for IE */
}

.dataTables_processing {
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

.dataTables_filter {
    width: 100%;
    float: right;
}

.dataTables_info, .dataTables_length, .dataTables_paginate{
    white-space:nowrap;
}

/* Pagination nested */
.paginate_disabled_previous, .paginate_enabled_previous,
.paginate_disabled_next, .paginate_enabled_next {
    height: 19px;
    float: left;
    cursor: pointer;
    *cursor: hand;
    color: #111 !important;
}
.paginate_disabled_previous:hover, .paginate_enabled_previous:hover,
.paginate_disabled_next:hover, .paginate_enabled_next:hover {
    text-decoration: none !important;
}
.paginate_disabled_previous:active, .paginate_enabled_previous:active,
.paginate_disabled_next:active, .paginate_enabled_next:active {
    outline: none;
}

.paginate_disabled_previous,
.paginate_disabled_next {
    color: #666 !important;
}
.paginate_disabled_previous, .paginate_enabled_previous {
    padding-left: 23px;
}
.paginate_disabled_next, .paginate_enabled_next {
    padding-right: 23px;
    margin-left: 10px;
}

.paginate_disabled_previous {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/back_disabled.png') no-repeat top left;
}

.paginate_enabled_previous {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/back_enabled.png') no-repeat top left;
}
.paginate_enabled_previous:hover {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/back_enabled_hover.png') no-repeat top left;
}

.paginate_disabled_next {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/forward_disabled.png') no-repeat top right;
}

.paginate_enabled_next {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/forward_enabled.png') no-repeat top right;
}
.paginate_enabled_next:hover {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/forward_enabled_hover.png') no-repeat top right;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables sorting
 */

.sorting_asc {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/sort_asc.png') no-repeat center right;
}

.sorting_desc {
    background: url('portal/media/org/jasig/portal/channels/marketplace/sort_desc.png') no-repeat center right;
}

.sorting {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/sort_both.png') no-repeat center right;
}

.sorting_asc_disabled {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/sort_asc_disabled.png') no-repeat center right;
}

.sorting_desc_disabled {
    background: url('/portal/media/org/jasig/portal/channels/marketplace/sort_desc_disabled.png') no-repeat center right;
}
 
table.display thead th:active,
table.display thead td:active {
    outline: none;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * DataTables row classes
 */
tr.odd {
    background-color: #eee;
}

tr.even {
    background-color: #fff;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Misc
 */
 
 .top{
    background-color: #F5F5F5;
 }
 
 label>input{
    width:90%;
 }
 
.top .dataTables_info {
    float: none;
}

.clear {
    clear: both;
}

.dataTables_empty {
    text-align: center;
}

.example_alt_pagination div.dataTables_info {
    width: 40%;
}

.paging_full_numbers {
    width: 400px;
    height: 22px;
    line-height: 22px;
}

.paging_full_numbers a:active {
    outline: none
}

.paging_full_numbers a:hover {
    text-decoration: none;
}

.paging_full_numbers a.paginate_button,
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

.paging_full_numbers a.paginate_button {
    background-color: #ddd;
}

.paging_full_numbers a.paginate_button:hover {
    background-color: #ccc;
    text-decoration: none !important;
}

.paging_full_numbers a.paginate_active {
    background-color: #99B3FF;
}

@media only screen and (max-width: 992px){
    .sort-btn-group{
        width: 100%;
    }
    .popular-sort-button{
        width: 25%;
    }
    .alphabetical-sort-button{
        width: 25%;
    }
    .audience-sort-button{
        width: 25%;
    }
    .category-sort-button{
        width: 25%;
    }
}
 
@media only screen and (max-width: 768px) {
    #unseen table th:nth-child(1),
    #unseen table th:nth-child(3),
    #unseen table td:nth-child(2),
    #unseen table th:nth-child(2){display: none;}
    .dataTables_filter{
        text-align: left;
    }
    .dataTables_filter>label{
        width: 100%;
        float: left;
    }
    .dataTables_filter>label>input{
        width: 90%;
        float: right:
    }
    
    tr.odd {
        background-color: #fff;
    }
}


.sort_info, .dataTables_info{
    text-align: left;
}

.marketplace_center_text, .dataTables_paginate,
.btn_group_container, .dataTables_processing{
    text-align:center;
}

.dataTables_length, .dataTables_filter {
    text-align:right;
}

a:hover {
 cursor:pointer;
}

.${n}bottom{
    border-top: thin solid black;
    padding-top: 1em;
}


</style>


<div id="${n}marketplace">

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
                        <a href="${renderRequest.contextPath}/p/${portlet.FName}">${portlet.title}</a>
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
                "<button type=\"button\" class=\"btn btn-default popular-sort-button\">${mostPopular}</button>"+
                "<button type=\"button\" id=\"${n}alphabetical-sort-button\" class=\"btn btn-default alphabetical-sort-button\">${azIndex}</button>"+
                "<button type=\"button\" class=\"btn btn-default audience-sort-button\">${audience}</button>"+
                "<button type=\"button\" id=\"${n}category-sort-button\" class=\"btn btn-default category-sort-button\">${categoryLabel}</button>"+
                "</div><br><br></div>");
            $("#${n}categoryListContainer").insertAfter($(".${n}sort_buttons"));
            $("#${n}marketplace div.sort_info").html("<div><BR><BR><span><strong>${browseBy}</strong><br><br></div>");
            $("#${n}marketplace div.dataTables_filter").append("<form action='${entryURL}'><button>${labelSearch}</button></form>");

            var setFilter = function(text){
                myDataTable.fnFilter(text);
            };

            var sortColumns = function(column){
                myDataTable.fnSort([[column, 'asc']]);
            }

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
