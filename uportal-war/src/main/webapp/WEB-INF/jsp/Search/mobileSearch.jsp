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

<portlet:renderURL var="formUrl"/>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet Section -->
<div id="${n}search">

    <p class="search-back-div" style="${ empty engine ? 'display:none' : '' }"><a href="javascript:;" class="search-back-link">Back</a></p>

    <div class="search-engines" style="${ empty engine ? '' : 'display:none' }">
        <ul data-role="listview">
            <li>
                <a href="javascript:;" class="directory-search-link">
                    <spring:message code="directory"/>
                </a>
            </li>
            <li>
                <a href="javascript:;" class="campus-web-search-link">
                    <spring:message code="campus.web"/>
                </a>
            </li>
            <li>
                <a href="javascript:;" class="portal-search-link">
                    <spring:message code="portal"/>
                </a>
            </li>
        </ul>
    </div>

    <div class="search-form" style="${ not empty engine ? '' : 'display:none' }">
        <h2>
            <c:choose>
                <c:when test="${ engine == 'directory' }">Search the directory</c:when>
                <c:when test="${ engine == 'campus-web' }">Search the campus web</c:when>
                <c:when test="${ engine == 'portal' }">Search the portal</c:when>
            </c:choose>
        </h2>
        <div>
            <form action="${ formUrl }" method="POST">
                <input type="hidden" name="engine" value="${ engine }"/>
                <input name="query" value="${ fn:escapeXml(query )}"/> 
                <input data-inline="true" type="submit" value="Search"/>
            </form>
        </div>
    </div>
    
    <c:if test="${not empty query and not empty engine}">
    
        <div class="search-results">

            <c:choose>
                <c:when test="${ engine == 'directory' }">
                    <div class="person-search-results-summary">
                        <h2>Directory search results</h2>
                        <c:if test="${ fn:length(people) == 0 }">
                            <spring:message code="no.results"/>
                        </c:if>
                        
                        <ul data-role="listview" class="person-search-results">
                            <c:forEach items="${ people }" var="person" varStatus="status">
                                <li>
                                    <a index="${ status.index }" class="person-result-link" href="javascript:;">
                                        ${fn:escapeXml(person.attributes.displayName[0])}
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                    
                    <c:forEach items="${ people }" var="person">
                    
                        <div class="person-search-result-detail" style="display:none;">
                            <h3>${fn:escapeXml(person.attributes.displayName[0])}</h3>
                            
                            <ul data-role="listview">
                                <c:forEach items="${ attributeNames }" var="attributeName">
                                    <c:if test="${ fn:length(person.attributes[attributeName]) > 0 }">
                                        <li>
                                            <spring:message code="attribute.displayName.${attributeName}"/>:
                                            ${fn:escapeXml(person.attributes[attributeName][0])}
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:forEach>
                </c:when>
                <c:when test="${ engine == 'campus-web' }">
                    <h2>Campus Web Search Results</h2>
                    <ul data-role="listview">
                        <c:forEach items="${ gsaResults.searchResults }" var="result">
                            <li>
                                <h3><a href="${ result.link }">${ result.title }</a></h3>
                                <p>${ result.snippet }</p>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:when test="${ engine == 'portal' }">
                    <h2>Portal Search Results</h2>
                    <ul data-role="listview" class="portlet-search-results">
                        <li class="portlet-match">
                            <img class="portlet-match-icon"/>
                            <h3><a class="portlet-match-link"></a></h3>
                            <p class="portlet-match-description"></p>
                        </li>
                    </ul>
                </c:when>
            </c:choose>
            
        </div>
        
    </c:if>

</div>  

<script type="text/javascript">
<rs:compressJs>up.jQuery(function() {
    var $ = up.jQuery;
    var fluid = up.fluid;
    
    var showSearchForm =  function() {
        
        $("#${n}search .search-engines").hide();
        $("#${n}search .search-form").show();
        
        $("#${n}search .search-back-link").show().unbind("click").click(function () {
            $("#${n}search .search-engines").show();
            $("#${n}search .search-form").hide();
            $("#${n}search .search-results").hide();
            $("#${n}search .search-back-div").hide();
        });
        $("#${n}search .search-back-text").text("Search Engines");
        $("#${n}search .search-back-div").show();
    };
    
        $("#${n}search .directory-search-link").click(function () {
            $("#${n}search .search-form input[name=engine]").val("directory");
            $("#${n}search .search-form h2").text("Search the directory");
            $("#${n}search .search-form input[name=query]").val("");
            showSearchForm();
        });
        $("#${n}search .campus-web-search-link").click(function () {
            $("#${n}search .search-form input[name=engine]").val("campus-web");
            $("#${n}search .search-form h2").text("Search the campus web");
            $("#${n}search .search-form input[name=query]").val("");
            showSearchForm();
        });
        $("#${n}search .portal-search-link").click(function () {
            $("#${n}search .search-form input[name=engine]").val("portal");
            $("#${n}search .search-form h2").text("Search the portal");
            $("#${n}search .search-form input[name=query]").val("");
            showSearchForm();
        });
        
        <c:if test="${ not empty engine }">
            showSearchForm();
        </c:if>
        
        <c:if test="${engine == 'directory' and not empty query}">
            $("#${n}search .person-result-link").each(function (idx, link) {
                $(link).click(function () {
                    $("#${n}search .search-form").hide();
                    $("#${n}search .person-search-results-summary").hide();
                    $("#${n}search .person-search-result-detail").hide();
                    $($("#${n}search .person-search-result-detail").get(idx)).show();
                    
                    $("#${n}search .search-back-text").text("Directory Results");
                    $("#${n}search .search-back-link").show().unbind("click").click(function () {
                        $("#${n}search .search-form").show();
                        $("#${n}search .person-search-results-summary").show();
                        $("#${n}search .person-search-result-detail").hide();
                        $("#${n}search .search-back-text").text("Search Engines");
                        
                        $("#${n}search .search-back-link").click(function () {
                            $("#${n}search .search-engines").show();
                            $("#${n}search .search-form").hide();
                            $("#${n}search .search-results").hide();
                            $("#${n}search .search-back-div").hide();
                        });
                    });
                    
                    return false;
                });
            });
        </c:if>
        
        <c:if test="${engine == 'portal' and not empty query}">        
            var registry = up.PortletRegistry(
                "#${n}search .search-results", 
                { 
                    portletListUrl: "<c:url value="/api/portletList"/>",
                    listeners: {
                        onLoad: function () {
                            var cutpoints, portlets, members, portletRegex;
                            cutpoints = [
                                 { id: "portlet:", selector: ".portlet-match" },
                                 { id: "portletLink", selector: ".portlet-match-link" },
                                 { id: "portletIcon", selector: ".portlet-match-icon" },
                                 { id: "portletDescription", selector: ".portlet-match-description" }
                             ];
                            
                            // Build a list of all portlets that are a deep member of the
                            // currently-selected category, sorted by title
                            portlets = [];
                            members = registry.getAllPortlets();
                            portletRegex = new RegExp(up.escapeSpecialChars("${query}"), "i");
                            $(members).each(function (idx, portlet) {
                                if (portletRegex.test(portlet.title) || portletRegex.test(portlet.description)) {
                                    portlets.push(portlet);
                                }
                            });
                            portlets.sort(up.getStringPropertySortFunction("title"));
    
                            var tree = { children: [] };
    
                            $(portlets).each(function (id, portlet) {
                                tree.children.push({
                                    ID: "portlet:",
                                    children: [
                                       { 
                                           ID: "portletLink", target: "/uPortal/p/" + portlet.fname, linktext: portlet.title
                                       },
                                       {
                                           ID: "portletIcon",
                                           decorators: [
                                               { type: "attrs", attributes: { src: portlet.iconUrl } }
                                           ]
                                       },
                                       {
                                           ID: "portletDescription", value: portlet.description
                                       }
                                    ]
                                });
                            });
    
                            fluid.selfRender($(".portlet-search-results"), tree, { cutpoints: cutpoints });
                            
                        }
                    } 
                }
            );
        </c:if>
        
 });
</rs:compressJs></script>