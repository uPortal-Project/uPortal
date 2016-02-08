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

<div id="${n}marketplace">

<c:if test="${fn:length(featuredList) > 0}">
    <div id="${n}featured" class="marketplaceSection">
		<h3><spring:message code="featured" text="Featured" /></h3>
			<div class="row">
            <c:url value="/media/skins/icons/mobile/default.png" var="defaultIcon"/>
            <c:forEach var="featuredPortlet" items="${featuredEntries}" varStatus="status">
                <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                    <portlet:param name="action" value="view"/>
                    <portlet:param name="fName" value="${featuredPortlet.fname}"/>
                </portlet:renderURL>
                <div class="col-sm-6 col-lg-3">
                    <div class="panel panel-default marketplace-panel">
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
                <button type="button" id="${n}alphabetical-sort-button" class="btn btn-default active"><spring:message code="label.azIndex" text="A-Z Index" /></button>
                <!-- Offer Browse By: Category (but only if there are categories in the list) -->
                <c:if test="${not empty categoryList}">
                <button type="button" id="${n}category-sort-button" class="btn btn-default"><spring:message code="label.category" text="Categories" /></button>
                </c:if>
            </div>
        </div>
    </div>
</div>

<div id="${n}categoryListContainer" class="panel panel-default" style="display:none">
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
    <table id="${n}portletTable" class="table table-striped">
        <thead>
            <tr>
                <th class="essential">
                    <spring:message code="label.title" text="Title" />
                </th>
                <th class="optional">
                    <spring:message code="label.description" text="Description" />
                </th>
                    <th class="essential">
                </th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="portlet" items="${marketplaceEntries}">
                <tr>
                    <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                        <portlet:param name="action" value="view"/>
                        <portlet:param name="fName" value="${portlet.fname}"/>
                    </portlet:renderURL>
                    <td class="essential" style="white-space: nowrap;">
                        <strong><a href="${entryURL}">${portlet.title}</a></strong>
                    </td>
                    <td class="optional">
                        ${portlet.description}
                    </td>
                    <td class="essential">
                        <a href="${portlet.renderUrl}">Go to ${portlet.title}</a>
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
                "bAutoWidth":false
            });

            $("#${n}featured").insertAfter($("#${n}marketplace .top"));

            var alphabeticalSortButton = $("#${n}alphabetical-sort-button");
            var categorySortButton = $("#${n}category-sort-button");
            
            var setFilter = function(text){
            	alphabeticalSortButton.removeClass("active");
                myDataTable.fnFilter(text);
            };
            
            $(".${n}marketplace_category_link").click(function(){
                setFilter(this.textContent);
            });

            alphabeticalSortButton.click(function(){
                myDataTable.fnFilter("");
            	myDataTable.fnSort([[0,'asc']]);
            	alphabeticalSortButton.addClass("active");
            	
            	$("#${n}categoryListContainer").hide();
            	categorySortButton.removeClass("active");
            });

            categorySortButton.click(function(){
            	alphabeticalSortButton.removeClass("active");
            	
                $("#${n}categoryListContainer").toggle();
                categorySortButton.toggleClass("active");
            });

            if("${initialFilter}"){
                setFilter("${initialFilter}");
            };
        });
    });
})(up.jQuery);
</script>


