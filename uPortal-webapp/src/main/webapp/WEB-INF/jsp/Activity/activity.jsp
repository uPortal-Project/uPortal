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

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| docs/SKINNING_UPORTAL.md
-->

<!-- Portlet -->
<div class="fl-widget portlet portal-activity" role="section">
    <form id="${n}form">

        <!-- Portlet Body -->
        <div class="portlet-body" role="main">

            <!-- Portlet Section -->
            <div id="${n}popularPortlets" class="portlet-section" role="region">

                <div class="portlet-section-body">
                    <a id="portalActivityToggle" class="button btn"><spring:message code="portal.activity.who"/></a>
                    <br/><br/>
                    <div id="portalActivity">
                        <div>
                            <div class="box-outer">
                                <div class="box-header"><spring:message code="portal.activity.now"/></div>
                                <div class="box-total">${usageNow.total}</div>
                                <div class="box-data">
                                    <table cellpadding="0" cellspacing="0">
                                        <c:forEach items="${usageNow.groupActivity}" var="groupActivity">
                                            <tr>
                                                <td style="text-align:right;">${groupActivity.groupName}</td>
                                                <td style="text-align:left;">${groupActivity.total}</td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </div>
                            </div>
                            <div class="box-outer">
                                <div class="box-header"><spring:message code="portal.activity.today"/></div>
                                <div class="box-total">${usageToday.total}</div>
                                <div class="box-data">
                                    <table cellpadding="0" cellspacing="0">
                                        <c:forEach items="${usageToday.groupActivity}" var="groupActivity">
                                            <tr>
                                                <td style="text-align:right;">${groupActivity.groupName}</td>
                                                <td style="text-align:left;">${groupActivity.total}</td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </div>
                            </div>
                            <div class="box-outer">
                                <div class="box-header"><spring:message code="portal.activity.yesterday"/></div>
                                <div class="box-total">${usageYesterday.total}</div>
                                <div class="box-data">
                                    <table cellpadding="0" cellspacing="0">
                                        <c:forEach items="${usageYesterday.groupActivity}" var="groupActivity">
                                            <tr>
                                                <td style="text-align:right;">${groupActivity.groupName}</td>
                                                <td style="text-align:left;">${groupActivity.total}</td>
                                            </tr>
                                        </c:forEach>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                    <c:if test="${showSearches}">
                        <br/>
                        <a id="portalSearchActivityToggle" class="button btn"><spring:message code="portal.activity.searching"/></a>
                        <div id="portalSearchActivity" class="popular-search">
                            <div class="results">
                                <c:forEach items="${popularSearchTerms}" var="searchInfo" varStatus="status">
                                    <c:if test="${status.index > 0}"><bold>|</bold></c:if>
                                    <a href="${renderRequest.contextPath}/p/search/max/action.uP?pP_query=${searchInfo.searchTerm}">${searchInfo.searchTerm}</a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </form>
</div>

<script>
up.jQuery( "#portalActivityToggle" ).click(function() {
    up.jQuery( "#portalActivity" ).slideToggle( "slow" );
});

up.jQuery( "#portalSearchActivityToggle" ).click(function() {
    up.jQuery( "#portalSearchActivity" ).slideToggle( "slow" );
});
</script>
