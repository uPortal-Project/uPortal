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

<portlet:actionURL var="selectPersonUrl" escapeXml="false">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="select"/>
    <portlet:param name="username" value="USERNAME"/>
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div id="${n}" class="fl-widget portlet prs-lkp view-lookup" role="section">

	<!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="search.for.users" /></h2>
    </div>
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">

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
                    <input class="button primary" type="submit" class="button" value="${searchButtonText}" />

                    <portlet:renderURL var="cancelUrl">
                        <portlet:param name="execution" value="${flowExecutionKey}"/>
                        <portlet:param name="_eventId" value="cancel"/>
                    </portlet:renderURL>
                    <a class="button" class="button" href="${ cancelUrl }">
                        <spring:message code="cancel" />
                    </a>
                </div>
                
            </form>
        
        <div id="${n}searchResults" class="portlet-section" style="display:none" role="region">
            <div class="titlebar">
                <h3 role="heading" class="title">Search Results</h3>
            </div>
            <p class="no-users-message" style="display:none"><spring:message code="no.matching.users"/></p>

            <div class="results-pager">
                <div class="view-pager flc-pager-top">
                    <ul id="pager-top" class="fl-pager-ui">
                        <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="previous"/></a></li>
                        <li>
                             <ul class="flc-pager-links demo-pager-links" style="margin:0; display:inline">
                                 <li class="flc-pager-pageLink"><a href="#">1</a></li>
                                 <li class="flc-pager-pageLink-skip">...</li>
                                 <li class="flc-pager-pageLink"><a href="#">2</a></li>
                             </ul>
                        </li>
                        <li class="flc-pager-next"><a href="#"><spring:message code="next"/> &gt;</a></li>
                        <li>
                            <span class="flc-pager-summary"><spring:message code="show"/></span>
                            <span> <select class="pager-page-size flc-pager-page-size">
                                <option value="5">5</option>
                                <option value="10">10</option>
                                <option value="20">20</option>
                                <option value="50">50</option>
                            </select></span> <spring:message code="per.page"/>
                        </li>
                    </ul>
                </div>
                
                <table id="${n}resultsTable" xmlns:rsf="http://ponder.org.uk">
                    <thead>
                        <tr rsf:id="header:">
                            <th id="${n}displayName" class="flc-pager-sort-header">
                                <a rsf:id="displayName" href="javascript:;">Name</a>
                            </th>
                            <th id="${n}username" class="flc-pager-sort-header">
                                <a rsf:id="username" href="javascript:;">Username</a>
                            </th>
                        </tr>
                    </thead>
                    <tbody id="${n}resultsBody">
                        <tr rsf:id="row:">
                            <td headers="${n}displayName">
                                <a href="javascript:;" rsf:id="displayName">Name</a>
                            </td>
                            <td headers="${n}username" rsf:id="username">Username</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

<script type="text/javascript">
    up.jQuery(function() {
        var $ = up.jQuery;
        var fluid = up.fluid;
        var pager = null;
        
        var attrs = <json:serialize value="${ queryAttributes }"/>;

        var options = {
            annotateColumnRange: 'displayName',
            columnDefs: [
                 { key: "displayName", valuebinding: "*.displayName", sortable: true,
                     components: {
                         target: "${selectPersonUrl}".replace("USERNAME", '${"${*.username}"}'),
                         linktext: '${"${*.displayName}"}'
                     }

                 },
                 { key: "username", valuebinding: "*.username", sortable: true }
             ],
            bodyRenderer: {
              type: "fluid.pager.selfRender",
              options: {
                  selectors: {
                     root: "#${n}resultsTable"
                  },
                  row: "row:"
                }
                
            },
            pagerBar: {type: "fluid.pager.pagerBar", options: {
              pageList: {type: "fluid.pager.renderedPageList",
                options: { 
                  linkBody: "a"
                }
              }
            }}
        };
        
        var showSearchResults = function (data) {
            $.get(
                "<c:url value="/api/people.json"/>", 
                data, 
                function(data) {
                    options.dataModel = [];
                    $(data.people).each(function (idx, person) {
                        options.dataModel.push({ username: person.name, displayName: person.attributes.displayName[0] });
                    });
                    if (options.dataModel.length == 0) {
                        $("#${n} .no-users-message").show();
                    	$("#${n} .results-pager").hide();
                    } else {
                        if (pager) {
                            up.refreshPager(pager, options.dataModel);
                        } else {
                            pager = up.fluid.pager("#${n}searchResults", options);
                        }
                        $("#${n} .no-users-message").hide();
                        $("#${n} .results-pager").show();
                    }
                    $("#${n}searchResults").show();
                }
            );
        };
        
        $(document).ready(function(){
            $("#${n}searchForm").submit(function() {
                var data = { searchTerms: [] };
                var searchTerm = $("#${n}queryValue").val();
                var queryTerm = $("#${n}queryAttribute").val();
                if (!queryTerm) {
                    $(attrs).each(function (idx, attr) {
                        data.searchTerms.push(attr);
                        data[attr] = searchTerm;
                    });
                } else {
                	data.searchTerms.push(queryTerm);
                	data[queryTerm] = searchTerm;
                }
                showSearchResults(data);
                return false;
            }); 
        });
    });
</script>