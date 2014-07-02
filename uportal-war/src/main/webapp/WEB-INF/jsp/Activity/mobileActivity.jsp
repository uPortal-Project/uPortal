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

<%--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
For the standards and guidelines that govern
the user interface of this portlet
including HTML, CSS, JavaScript, accessibilty,
naming conventions, 3rd Party libraries
(like jQuery and the Fluid Skinning System)
and more, refer to:
http://www.ja-sig.org/wiki/x/cQ
--%>

<style type="text/css">
    .portal-activity .results {
        padding: 2em;
    }

    .portal-activity .box-outer {
        height: 135px;

        background-image: linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -o-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -moz-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -webkit-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -ms-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -webkit-gradient(
            linear,
            left top,
            left bottom,
            color-stop(0.28, #3D3D3D),
            color-stop(0.64, #2E2E2E)
        );
    }

    .portal-activity .fl-col .box-header {
        width: 100%;
        border: 1px solid #353535;
        font-size: 16px;
        font-weight: bold;
        color: white;
        box-sizing: border-box;

        background-image: linear-gradient(bottom, #000000 31%, #141414 85%);
        background-image: -o-linear-gradient(bottom, #000000 31%, #141414 85%);
        background-image: -moz-linear-gradient(bottom, #000000 31%, #141414 85%);
        background-image: -webkit-linear-gradient(bottom, #000000 31%, #141414 85%);
        background-image: -ms-linear-gradient(bottom, #000000 31%, #141414 85%);
        background-image: -webkit-gradient(
            linear,
            left bottom,
            left top,
            color-stop(0.31, #000000),
            color-stop(0.85, #141414)
        );
    }

    .portal-activity .fl-col .box-header .box-header-text {
        padding: 5px 0 5px 10px;
    }

    .portal-activity .box-total {
        color: white;
        font-size: xx-large;
        font-weight: bold;
        text-align: center;
    }

    .portal-activity .box-data {
        color: white;
        font-size: large;
    }

    .portal-activity .box-data table {
        margin-left: auto;
        margin-right: auto;
        border-collapse: collapse;
    }

    .portal-activity .box-data table td {
        line-height: 0.3em;
    }

    .portal-activity .cell-left {
        text-align: right;
        padding-right: 0.5ex;
        padding-top: 0.4em;
        padding-bottom: 0.4em;
        float: left;
    }

    .portal-activity .cell-right {
        text-align: left;
        padding-left: 0.5ex;
        padding-top: 0.4em;
        padding-bottom: 0.4em;
    }

    .portal-activity .popular-search {
        clear: both;
    }

    .portal-activity .title {
        font-weight: bold;
        color: white;
    }

    .portal-activity a: link, .portal-activity a: visited,
    .portal-activity a: hover, .portal-activity a: active {
        color: inherit;
        text-decoration: none;
    }

    .portal-activity .ui-collapsible {
        margin: 0;
    }

    .portal-activity .ui-collapsible-heading {
        margin: 0;
    }

    .portal-activity .ui-collapsible-content {
        margin: 0;
        padding: 0;
    }

    .portal-activity .utilities {
        margin-top: 0;
    }

    .portal-activity .ui-corner-top {
        -moz-border-radius-topleft: 0;
        -webkit-border-top-left-radius: 0;
        border-top-left-radius: 0;
        -moz-border-radius-topright: 0;
        -webkit-border-top-right-radius: 0;
        border-top-right-radius: 0;
    }

    .portal-activity .ui-corner-bottom {
        -moz-border-radius-bottomleft: 0;
        -webkit-border-bottom-left-radius: 0;
        border-bottom-left-radius: 0;
        -moz-border-radius-bottomright: 0;
        -webkit-border-bottom-right-radius: 0;
        border-bottom-right-radius: 0;
    }

    .portal-activity .ui-body-a {
        background-image: linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -o-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -moz-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -webkit-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -ms-linear-gradient(top, #3D3D3D 28%, #2E2E2E 64%);
        background-image: -webkit-gradient(
            linear,
            left top,
            left bottom,
            color-stop(0.28, #3D3D3D),
            color-stop(0.64, #2E2E2E)
        );
    }
</style>

<%-- Portlet --%>
<div class="fl-widget portlet portal-activity up" role="section">
    <form id="${n}form">

        <%-- Portlet Body --%>
        <div class="fl-widget-content portlet-body portlet-content" role="main">

            <%-- Portlet Section --%>
            <div id="${n}popularPortlets" class="portlet-section fl-pager" role="region">
                <div class="portlet-section-body utilities">
                    <div data-role="collapsible" data-collapsed="false" data-content-theme="a"><%-- First collapsible --%>
                        <h3 class="title"><spring:message code="portal.activity.who"/></h3>
                        <div class="fl-container-flex fl-centered">
                            <div class="fl-container-flex fl-col-flex3 fl-fix content">
                                <div class="fl-col box-outer"><%-- "Now" box --%>
                                    <div class="box-header">
                                        <div class="box-header-text">
                                            <spring:message code="portal.activity.now"/>
                                        </div>
                                    </div>
                                    <div class="box-total">${usageNow.total}</div>
                                    <div class="box-data">
                                        <table cellpadding="0">
                                            <c:forEach items="${usageNow.groupActivity}" var="groupActivity">
                                                <tr>
                                                    <td class="cell-left">${groupActivity.groupName}</td>
                                                    <td class="cell-right">${groupActivity.total}</td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </div>
                                </div>
                                <div class="fl-col box-outer"><%-- "Today" box --%>
                                    <div class="box-header">
                                        <div class="box-header-text">
                                            <spring:message code="portal.activity.today"/>
                                        </div>
                                    </div>
                                    <div class="box-total">${usageToday.total}</div>
                                    <div class="box-data">
                                        <table cellpadding="0">
                                            <c:forEach items="${usageToday.groupActivity}" var="groupActivity">
                                                <tr>
                                                    <td class="cell-left">${groupActivity.groupName}</td>
                                                    <td class="cell-right">${groupActivity.total}</td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </div>
                                </div>
                                <div class="fl-col box-outer"><%-- "Yesterday" box --%>
                                    <div class="box-header">
                                        <div class="box-header-text">
                                            <spring:message code="portal.activity.yesterday"/>
                                        </div>
                                    </div>
                                    <div class="box-total">${usageYesterday.total}</div>
                                    <div class="box-data">
                                        <table cellpadding="0">
                                            <c:forEach items="${usageYesterday.groupActivity}" var="groupActivity">
                                                <tr>
                                                    <td class="cell-left">${groupActivity.groupName}</td>
                                                    <td class="cell-right">${groupActivity.total}</td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div><%-- /collapsible --%>
                    <div class="popular-search" data-role="collapsible" data-collapsed="false" data-theme="a" data-content-theme="a"><%-- Second collapsible --%>
                        <h3 class="title"><spring:message code="portal.activity.searching"/></h3>
                        <div class="results">
                            <c:forEach items="${popularSearchTerms}" var="searchInfo" varStatus="status">
                                <c:if test="${status.index > 0}"><bold>|</bold></c:if>
                                <a href="${renderRequest.contextPath}/p/search/max/action.uP?pP_query=${searchInfo.searchTerm}">${searchInfo.searchTerm}</a>
                            </c:forEach>
                        </div>
                    </div><%-- /collapsible --%>
                </div>
            </div>
        </div>
    </form>
</div>
