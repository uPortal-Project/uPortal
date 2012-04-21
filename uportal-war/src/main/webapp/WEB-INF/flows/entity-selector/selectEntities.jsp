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

    <!--
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
    -->
    
    <!--
    | selectEntities.jsp.
    | Description: 
    ================================================-->
    
    <!--
    | Taglibs, definitions, etc.
    ================================================-->
    <%@ include file="/WEB-INF/jsp/include.jsp" %>
    <portlet:actionURL var="submitUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" /></portlet:actionURL>
    <c:set var="n"><portlet:namespace/></c:set>
    <c:set var="selectionMode">${selectMultiple}</c:set>
    <!--
    | PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
    | For the standards and guidelines that govern the user interface of this portlet
    | including HTML, CSS, JavaScript, accessibilty, naming conventions, 3rd Party libraries
    | (like jQuery and the Fluid Skinning System) and more, refer to:
    | http://www.ja-sig.org/wiki/x/cQ
    -->
    
    <!--
    | Portlet.
    ================================================-->
    <div class="fl-widget portlet grp-mgr view-selectgroups" role="section">
        <!--titlebar-->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading"><spring:message code="${pageTitleCode}" text="${pageTitleText}"/></h2>
            <h3 class="subtitle"><spring:message code="${pageSubtitleCode}" arguments="${pageSubtitleArgument}" text="${pageSubtitleText}"/></h3>
        </div>
        <!--content-->
        <div id="${n}chooseGroupsBody" class="fl-widget-content content portlet-content" role="main">
            <c:choose>
                <c:when test="${selectionMode}">
                    <!--
                    | View: Multi Select.
                    ================================================-->
                    <div class="view-multi-select">
                        <div class="columns-2">
                            <div class="fl-container-flex60">
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser">
                                    <!--breadcrumb-->
                                    <div id="${n}entityBreadcrumb" class="breadcrumb">
                                        <h5 class="title"><spring:message code="groups"/>:</h5>
                                        <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                    </div>
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>
                                    </div>
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content">
                                        <!--includes-->
                                        <p><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <p id="${n}browsingResultNoMembers" style="display:none"><spring:message code="no.members"/></p>
                                        <!--members-->
                                        <c:forEach items="${selectTypes}" var="type">
                                        <c:choose>
                                            <c:when test="${type == 'group'}">
                                                <div class="group">
                                                    <h6 class="title"><spring:message code="groups"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'person'}">
                                                <div class="person">
                                                    <h6 class="title"><spring:message code="people"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'category'}">
                                                <div class="category">
                                                    <h6 class="title"><spring:message code="categories"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'portlet'}">
                                                <div class="portlet">
                                                    <h6 class="title"><spring:message code="portlets"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                    </div><!--end: content-->
                                    
                                    <!--search-->
                                    <div id="${n}portletSearch" class="portlet-search">
                                        <form id="${n}searchForm">
                                            <input type="text" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                            <input type="submit" class="button" value="<spring:message code="go"/>" />
                                        </form>
                                        <div id="${n}searchDropDown" class="search-dropdown">
                                            <div id="${n}closeDropDown" class="search-close"><a href="javascript:;">Close</a></div>
                                            <div id="${n}searchResultsNoMembers" class="portlet-msg info" role="alert"><p><spring:message code="no.members"/></p></div>
                                            <ul id="${n}searchResults" class="search-list">
                                                <li class="group">
                                                    <a href="javascript:;" title="&nbsp;"><span>&nbsp;</span></a>
                                                </li>
                                            </ul>
                                            <div id="${n}searchLoader" class="search-loader"><span>&nbsp;</span></div>
                                        </div>
                                    </div>
                                </div><!--end: entity-->
                            </div>
                            <div class="fl-container-flex40">
                                <!--selection-->
                                <div class="portlet-selection">
                                    <!--titlebar-->
                                    <div class="titlebar">
                                        <h4 class="title selections"><spring:message code="your.selections"/></h4>
                                    </div>
                                    <!--content-->
                                    <div class="content">
                                        <form action="${ submitUrl }" method="post">
                                            <div id="${n}selectionBasket" class="selection-basket">
                                                <ul>
                                                    <c:forEach items="${groups}" var="group">
                                                        <li>
                                                            <a key="${group.entityType}:${group.id}" href="javascript:;"><c:out value="${group.name}"/></a>
                                                            <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </div>
                                            
                                            <div id="${n}buttonPanel" class="buttons">
                                                <c:if test="${ showBackButton }">
                                                    <input class="button" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                                </c:if>
                                                <input id="${n}buttonPrimary" class="button primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                                <c:if test="${ showCancelButton }">
                                                    <input class="button" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                                                </c:if>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div><!--end:view-multi-select-->
                </c:when>
                <c:otherwise>
                    <!--
                    | View: Single Select.
                    ================================================-->
                    <div class="view-single-select">
                        <form action="${submitUrl}" method="post">
                            <!--titlebar-->
                            <div class="titlebar">
                                <!--selection-->
                                <div class="portlet-selection">
                                    <h4 class="title selections"><spring:message code="your.selection"/>:</h4>
                                    <div id="${n}selectionBasket" class="selection-basket">
                                        <c:choose>
                                            <c:when test="${fn:length(groups) == 0}">
                                                <span class="selection" title="<spring:message code="nothing.selected"/>"><spring:message code="nothing.selected"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${groups}" var="group">
                                                    <a key="${group.entityType}:${group.id}" href="javascript:;" class="selection"><c:out value="${group.name}"/></a>
                                                    <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                            <!--content-->
                            <div class="content">
                                <!--breadcrumb-->
                                <div id="${n}entityBreadcrumb" class="breadcrumb">
                                    <h5 class="title"><spring:message code="groups"/>:</h5>
                                    <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                </div>
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser">
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>
                                    </div>
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content">
                                        <p><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <p id="${n}browsingResultNoMembers" style="display:none"><spring:message code="no.members"/></p>
                                        <c:forEach items="${selectTypes}" var="type">
                                        <c:choose>
                                            <c:when test="${type == 'group'}">
                                                <div class="group">
                                                    <h6 class="title"><spring:message code="groups"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'person'}">
                                                <div class="person">
                                                    <h6 class="title"><spring:message code="people"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'category'}">
                                                <div class="category">
                                                    <h6 class="title"><spring:message code="categories"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'portlet'}">
                                                <div class="portlet">
                                                    <h6 class="title"><spring:message code="portlets"/></h6>
                                                    <ul class="member-list"></ul>
                                                </div>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                    </div><!--end: content-->
                                </div><!--end: entity-->
                                
                                <!--buttons-->
                                <div id="${n}buttonPanel" class="buttons">
                                    <c:if test="${showBackButton}">
                                        <input class="button" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                    </c:if>
                                    <input id="${n}buttonPrimary" class="button primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                    <c:if test="${showCancelButton}">
                                        <input class="button" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                                    </c:if>
                                </div><!--end: buttons-->
                            </div><!--end: content-->
                        </form>
                        
                        <!--search-->
                        <div id="${n}portletSearch" class="portlet-search">
                            <form id="${n}searchForm">
                                <input type="text" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                <input type="submit" class="button" value="<spring:message code="go"/>" />
                            </form>
                            <div id="${n}searchDropDown" class="search-dropdown">
                                <div id="${n}closeDropDown" class="search-close"><a href="javascript:;">Close</a></div>
                                <div id="${n}searchResultsNoMembers" class="portlet-msg info" role="alert"><p><spring:message code="no.members"/></p></div>
                                <ul id="${n}searchResults" class="search-list">
                                    <li class="group">
                                        <a href="javascript:;" title="&nbsp;"><span>&nbsp;</span></a>
                                    </li>
                                </ul>
                                <div id="${n}searchLoader" class="search-loader"><span>&nbsp;</span></div>
                            </div>
                        </div>
                    </div><!--end:view-single-select-->
                </c:otherwise>
            </c:choose>
        </div><!--end:portlet-content-->
    </div><!--end:portlet-->
    <script type="text/javascript">
        up.jQuery(function() {
            var $ = up.jQuery;
            
            $(document).ready(function(){
                up.entityselection("#${n}chooseGroupsBody", {
                    entityRegistry: {
                        options: { entitiesUrl: "<c:url value="/api/entities"/>" }
                    },
                    entityTypes: [<c:forEach items="${selectTypes}" var="type" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${type}</spring:escapeBody>'${status.last ? '' : ','}</c:forEach>],
                    selected: [<c:forEach items="${groups}" var="group" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${group.entityType}:${group.id}</spring:escapeBody>'${ status.last ? '' : ',' }</c:forEach>],
                    initialFocusedEntity: '${rootEntity.entityType}:${rootEntity.id}',
                    selectMultiple: ${selectionMode},
                    requireSelection: ${ not empty requireSelection ? requireSelection : true },
                    selectors: {
                        selectionBasket: "#${n}selectionBasket",
                        breadcrumbs: "#${n}entityBreadcrumbs",
                        currentEntityName: "#${n}currentEntityName",
                        selectEntityLink: "#${n}selectEntityLink",
                        entityBrowserContent: "#${n}entityBrowserContent",
                        entityBrowserTitlebar: "#${n}entityBrowserTitlebar",
                        browsingInclude: "#${n}browsingInclude",
                        browsingResultNoMembers: "#${n}browsingResultNoMembers",
                        closeSearch: "#${n}closeDropDown",
                        searchForm: "#${n}searchForm",
                        searchDropDown: "#${n}searchDropDown",
                        searchResults: "#${n}searchResults",
                        searchResultsNoMembers: "#${n}searchResultsNoMembers",
                        searchLoader: "#${n}searchLoader",
                        buttonPanel: "#${n}buttonPanel",
                        buttonPrimary: "#${n}buttonPrimary"
                    },
                    messages: {
                        selectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        deselectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="deselect"/></spring:escapeBody>',
                        removeCrumb: '<spring:escapeBody javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        removeSelection: '<spring:escapeBody javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        addSelection: '<spring:escapeBody javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        selected: '<spring:escapeBody javaScriptEscape="true"><spring:message code="selected"/></spring:escapeBody>',
                        nothingSelected: '<spring:escapeBody javaScriptEscape="true"><spring:message code="nothing.selected"/></spring:escapeBody>',
                        searchValue: '<spring:escapeBody javaScriptEscape="true"><spring:message code="please.enter.name"/></spring:escapeBody>'
                    }
                });
            });
        });
    </script>