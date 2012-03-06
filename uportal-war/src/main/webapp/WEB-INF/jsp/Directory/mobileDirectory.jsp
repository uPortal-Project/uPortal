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

    <div data-role="header" class="titlebar portlet-titlebar search-back-div" style="${ empty query ? 'display:none' : '' }">
        <a data-role="button"  data-icon="back" data-inline="true" class="search-back-link" id="${n}gridViewLink" href="javascript:;">Back</a>
        <h2 class="search-engine-name">Directory Search Results</h2>
    </div>

    <div class="portlet">
    <div class="portlet-content" data-role="content">
    
    <div class="search-form" style="${ not empty query ? 'display:none' : '' }">
        <div>
            <form action="${ formUrl }" method="POST">
                <input type="hidden" name="engine" value="${ engine }"/>
                <input name="query" value="${ fn:escapeXml(query )}"/> 
                <input data-inline="true" type="submit" value="Search"/>
            </form>
        </div>
    </div>
    
    <c:if test="${not empty query }">
    
        <div class="search-results">

            <div class="person-search-results-summary">
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
            
            <style type="text/css">
            .person-search-result-detail table tr td { padding-bottom:20px; }
            .person-search-result-detail table tr td.person-search-result-attr-name { padding-right:10px; }
            </style>
            
            <c:forEach items="${ people }" var="person">
            
                <div class="person-search-result-detail" style="display:none;">
                    <h3 style="text-align:center">${fn:escapeXml(person.attributes.displayName[0])}</h3>
                    <table>
                        <c:forEach items="${ attributeNames }" var="attribute">
                            <c:set var="values" value="${ person.attributes[attribute.key] }"/>
                            <c:if test="${ fn:length(values) > 0 }">
                                <tr style="padding-bottom: 30px;">
                                    <td style="font-weight: bold; font-size:80%" class="person-search-result-attr-name">
                                        <spring:message code="attribute.displayName.${ attribute.key }"/>
                                    </td>
                                    <td style="">
                                        <c:choose>
                                            <c:when test="${ attribute.value == 'EMAIL' }">
                                                <a href="mailto:${ person.attributes[attribute.key][0] }">${ fn:escapeXml(person.attributes[attribute.key][0]) }</a>
                                            </c:when>
                                            <c:when test="${ attribute.value == 'PHONE' }">
                                                <a href="tel:${ person.attributes[attribute.key][0] }">${ fn:escapeXml(person.attributes[attribute.key][0]) }</a>
                                            </c:when>
                                            <c:when test="${ attribute.value == 'MAP' }">
                                                <a href="<c:url value="http://maps.google.com/maps"><c:param name="q" value="${ fn:escapeXml(fn:replace(person.attributes[attribute.key][0], '$', ' ')) }"/></c:url>">${ fn:replace(fn:escapeXml(person.attributes[attribute.key][0]), '$', '<br/>') }</a>
                                            </c:when>
                                            <c:when test="${ attribute.value == 'LINK' }">
                                                <a href="${ fn:escapeXml(person.attributes[attribute.key][0]) }">${ fn:escapeXml(person.attributes[attribute.key][0]) }</a>
                                            </c:when>
                                            <c:otherwise>
                                                ${ fn:escapeXml(person.attributes[attribute.key][0]) }
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </table>

                </div>
            </c:forEach>
            
        </div>
        
    </c:if>

    </div>
    </div>

</div>  

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;
    var fluid = up.fluid;

    $(document).ready(function() { 

        var showSearchForm = function() {
            $("#${n}search .search-form").show();
            $("#${n}search .person-search-results-summary").hide();
            $("#${n}search .person-search-result-detail").hide();
            $("#${n}search .search-back-div").hide();
        };
        
        var showResults = function() {
            $("#${n}search .person-search-results-summary").show();
            $("#${n}search .person-search-result-detail").hide();
            $("#${n}search .search-back-link").unbind("click").click(showSearchForm);
        };

        $("#${n}search .search-back-link").unbind("click").click(showSearchForm);

        <c:if test="${not empty query}">
            $("#${n}search .person-result-link").each(function (idx, link) {
                $(link).click(function () {
                    $("#${n}search .search-form").hide();
                    $("#${n}search .person-search-results-summary").hide();
                    $("#${n}search .person-search-result-detail").hide();
                    $($("#${n}search .person-search-result-detail").get(idx)).show();
                    
                    $("#${n}search .search-back-div").show();
                    $("#${n}search .search-back-link").unbind("click").click(showResults);
                    return false;
                });
            });
        </c:if>
        
    });
});        
</script>