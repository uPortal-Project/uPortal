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
<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.6.1/jquery-1.6.1.min.js"/>"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.min.js" type="text/javascript"></script>
<style>
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
	text-align: center;
	color: #999;
	font-size: 14px;
	background-color: white;
}

.dataTables_length {
	width: 40%;
	float: left;
}

.dataTables_filter {
	width: 100%;
	float: right;
	text-align: right;
}

.dataTables_info {
	width: 40%;
	float: left;
}

.dataTables_paginate {
	float: right;
	text-align: right;
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
		width: 33%;
	}
	.alphabetical-sort-button{
		width: 33%;
	}
	.audience-sort-button{
		width: 33%;
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

.btn_group_container{
	text-align: center;
}

.sort_info{
	text-align: left;
}

</style>
<c:set var="n"><portlet:namespace/></c:set>
<div id="${n}marketplace">
<div id="unseen">
	<table id="${n}portletTable" class="display table">
		<thead>
			<tr>
				<th class="essential" style="border:none;">
					Title
				</th>
				<th class="optional" style="border:none;">
					Description
				</th>
				<th class="essential" style="border:none;">
				</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="portlet" items="${channelBeanList}">
				<tr>
					<td class="essential" style="white-space: nowrap; border:none;">
						${portlet.title}
					</td>
					<td class="optional" style="border:none;">
						${portlet.description}
					</td>
					<portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
						<portlet:param name="action" value="view"/>
						<portlet:param name="name" value="${portlet.name}"/>
					</portlet:renderURL>
					<td class="essential" style="border:none;">
						<a href="${entryURL}">get</a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	</div>
</div>

<script type="text/javascript">

function trim11 (str) {
    str = str.replace(/^\s+/, '');
    for (var i = str.length - 1; i >= 0; i--) {
        if (/\S/.test(str.charAt(i))) {
            str = str.substring(0, i + 1);
            break;
        }
    }
    return str;
}

function applyEllipsis(nrow,col,txtlen){
	   var $cell = $('td:eq('+col+')', nrow);
	   $cell.text(ellipsis($cell.text(),txtlen)); // ellipsis() from 2. above
	   return nrow;
	};
	
function ellipsis(text, n) {
	text = trim11(text);
	if(text.length > n){
	  return text.substring(0,n)+"...";
	}
	else       
	  return text;
};
	  
	  var myDataTable;
	  
function sortColumns(column){
	myDataTable.fnSort([[column, 'asc']]);
}
	    
	  
$(document).ready( function () {
	
    myDataTable = $('#${n}portletTable').dataTable({
    	"aoColumnDefs": [{"bSortable": false, "aTargets": [ 2 ] }],
    	"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull){
    		   // *** NOTE *** applyEllipsis(nrow,col,txtlen) col is 0-based,
    		   // however, don't include hidden columns in count
    		   applyEllipsis(nRow,1,75);// using variable from 1. above
    		   // In my app this was actually column 9 (10th col) but
    		   // because column 0 was hidden "8" is used
    		  },
        "sDom": '<"top"f><"sort_info"><"${n}sort_buttons">rt<"bottom"ipl>'
    });
    $("div.${n}sort_buttons").html("<div class=\"btn_group_container\"><div class=\"btn-group sort-btn-group\"><button type=\"button\" class=\"btn btn-default popular-sort-button\">Most Popular</button><button type=\"button\" onclick=\"sortColumns(0)\" class=\"btn btn-default alphabetical-sort-button\">A-Z index</button>  <button type=\"button\" class=\"btn btn-default audience-sort-button\">Audience</button></div><br><br></div>");
	$("div.sort_info").html("<div><BR><BR><span><strong>Browse by</strong><br><br></div>")
} );
</script>

