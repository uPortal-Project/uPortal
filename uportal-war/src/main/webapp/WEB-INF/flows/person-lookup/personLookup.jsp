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

<portlet:actionURL var="selectPersonUrl" escapeXml="false">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="select"/>
    <portlet:param name="username" value="USERNAME"/>
</portlet:actionURL>

<portlet:renderURL var="cancelUrl">
    <portlet:param name="execution" value="${flowExecutionKey}"/>
    <portlet:param name="_eventId" value="cancel"/>
</portlet:renderURL>

<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div id="${n}personBrowser" class="portlet prs-lkp view-lookup" role="section">

    <!-- Portlet Titlebar -->
    <div class="titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="search.for.users" /></h2>
    </div>
    
    <!-- Portlet Content -->
    <div class="content portlet-content" role="main">

        <div class="portlet-content">
            <form id="${n}searchForm" action="javascript:;">
                <select id="${n}queryAttribute" name="queryAttribute">
                        <option value=""><spring:message code="default.directory.attributes"/></option>
                    <c:forEach var="queryAttribute" items="${queryAttributes}">
                        <option value="${ queryAttribute }">
                            <spring:message code="attribute.displayName.${queryAttribute}" text="${queryAttribute}"/>
                        </option>
                    </c:forEach>
                </select>
                <input id="${n}queryValue" type="text" name="queryValue"/>
                
                <!-- Buttons -->
                <div class="buttons">
                    <spring:message var="searchButtonText" code="search" />
                    <input class="btn btn-primary" type="submit" value="${searchButtonText}" />

                    <a class="btn btn-default" href="${ cancelUrl }">
                        <spring:message code="cancel" />
                    </a>
                </div>
                
            </form>
        
        <div id="${n}searchResults" class="portlet-section" style="display:none" role="region">
            <div class="titlebar">
                <h3 role="heading" class="title">Search Results</h3>
            </div>
                <table id="${n}resultsTable" class="portlet-table table table-striped table-bordered table-hover">
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
        	pageLength: personList_configuration.main.pageSize,
        	lengthMenu: [5, 10, 20, 50],
        	ajax: {
        		url: '<c:url value="/api/people.json"/>',
        		dataSrc: "people",
        		data: queryData
        	},
            processing: true,
            autoWidth: false,
            pagingType: 'full_numbers',
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
            columns: [
                //setting default column content prevents popup errors when user has no 
                //name or when username attribute is not available or mapped.
                //actual content is overritten later
                { 	data: 'attributes.displayName',
                	type: 'string',
                	width: '50%',
                	defaultContent: "<i>Not set</i>",
                	render: function ( data, type, row, meta ) {
                		return getSelectPersonAnchorTag(data, row.attributes.username);		
                	}
                },  // Name
                { 	data: 'attributes.username', 
                	type: 'string', 
                	width: '50%', 
                	defaultContent: "<i>Not set</i>",
                	render: function ( data, type, row, meta ) {
						return getSelectPersonAnchorTag(data, data);                
                	}  // User Name 
                }
            ],
            initComplete: function (settings, json) {
            	$(".column-filter-widgets").prepend('<label><spring:message code="filters" htmlEscape="false" javaScriptEscape="true"/></label>');                
                $(".column-filter-widget select").addClass("form-control input-sm");
                personList_configuration.main.table.fnDraw();
            },
         	// this is the default except with an extra row for the add-portlet button placeholder
            // and a 'W' (filter plugin) inserted next to the 'l - length changing input control'
           	dom: 	"<'row column-filter-container'<'col-sm-6'Wl><'col-sm-6'f>>" +
					"<'row'<'col-sm-12'tr>>" +
					"<'row'<'col-sm-5'i><'col-sm-7'p>>",
			responsive: true
        });
        $("#${n}searchResults").show();
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
