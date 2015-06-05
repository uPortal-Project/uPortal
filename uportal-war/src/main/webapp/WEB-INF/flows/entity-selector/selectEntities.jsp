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

    <link href="<c:url value="/media/skins/common/css/entity-selector.css"/>" rel="stylesheet" type="text/css" />

    <!--
    | PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
    | For the standards and guidelines that govern the user interface of this portlet
    | including HTML, CSS, JavaScript, accessibility, naming conventions, 3rd Party libraries
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
        <div id="${n}chooseGroupsBody" class="fl-widget-content content portlet-content container-fluid" role="main">
            <c:choose>
                <c:when test="${selectionMode}">
                    <!--
                    | View: Multi Select.
                    ================================================-->
                    <div class="view-multi-select">
                        <div class="columns-2 row-fluid row">
                            <div class="fl-container-flex60 span8 col-md-8">
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser row">
                                    <!--breadcrumb-->
                                    <div id="${n}entityBreadcrumb" class="link-breadcrumb col-md-12">
                                        <h5 class="title"><spring:message code="groups"/>:</h5>
                                        <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                    </div><!--end: breadcrumb-->
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix col-md-12">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>
                                    </div><!--end: titlebar-->
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content row">
                                        <!--includes-->
                                        <p class="col-md-12"><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <!--members-->
                                        <c:forEach items="${selectTypes}" var="type">
                                        <c:choose>
                                            <c:when test="${type == 'group'}">
                                                <div class="group col-md-12">
                                                    <h6 class="title"><spring:message code="groups"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'person'}">
                                                <div class="person col-md-12">
                                                    <h6 class="title"><spring:message code="people"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.people"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'category'}">
                                                <div class="category col-md-12">
                                                    <h6 class="title"><spring:message code="categories"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subcategories"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'portlet'}">
                                                <div class="portlet col-md-12">
                                                    <h6 class="title"><spring:message code="portlets"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.portlets"/></p>
                                                </div>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                    </div><!--end: content-->
                                    <!--search-->
                                    <div id="${n}portletSearch" class="portlet-search">
                                        <form id="${n}searchForm" class="form-inline" role="form">
                                            <input type="text" class="form-control" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                            <input type="submit" class="button btn" value="<spring:message code="go"/>" />
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
                                    </div><!--end: search-->
                                </div><!--end: entity-->
                                <div id="${n}adHocGroups" class="entity-browser row" style="margin-top: 7px;">
                                    <div class="content row">
                                        <div>
                                            <div class="col-md-8">
                                                <h5 id="${n}currentAdHocGroupName" class="title">Ad Hoc Groups</h5>
                                            </div>
                                            <div id="${n}adHocCreate" class="col-md-4">
                                                <button type="button" class="btn btn-primary pull-right" data-toggle="modal" data-target="#myModal">Add Custom Group <i class="fa fa-plus-circle"></i></button>
                                            </div>
                                        </div>
                                        <div class="group col-md-12">
                                            <div id="${n}adHocBreadcrumbs" class="breadcrumbs"></div>
                                            <table id="${n}adHocMemberList" class="table table-condensed table-striped">
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Authenticated Users</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Faculty Users</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Students</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                        </div>
                                    </div>
                                </div><!--end: ad hoc groups-->
                            </div>
                            <div class="fl-container-flex40 span4 col-md-4">
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
                                                    <input class="button btn" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                                </c:if>
                                                <input id="${n}buttonPrimary" class="button btn btn-primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                                <c:if test="${ showCancelButton }">
                                                    <input class="button btn btn-link" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
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
                        <div class="columns-2 row-fluid row">
                            <div class="fl-container-flex60 span8 col-md-8">
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser row">
                                    <!--breadcrumb-->
                                    <div id="${n}entityBreadcrumb" class="link-breadcrumb col-md-12">
                                        <h5 class="title"><spring:message code="groups"/>:</h5>
                                        <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                    </div><!--end: breadcrumb-->
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix col-md-12">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>
                                    </div><!--end: titlebar-->
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content row">
                                        <!--includes-->
                                        <p class="col-md-12"><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <!--members-->
                                        <c:forEach items="${selectTypes}" var="type">
                                            <c:choose>
                                                <c:when test="${type == 'group'}">
                                                    <div class="group col-md-12">
                                                        <h6 class="title"><spring:message code="groups"/></h6>
                                                        <ul class="member-list"></ul>
                                                        <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'person'}">
                                                    <div class="person col-md-12">
                                                        <h6 class="title"><spring:message code="people"/></h6>
                                                        <ul class="member-list"></ul>
                                                        <p class="no-members" style="display:none"><spring:message code="no.direct.member.people"/></p>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'category'}">
                                                    <div class="category col-md-12">
                                                        <h6 class="title"><spring:message code="categories"/></h6>
                                                        <ul class="member-list"></ul>
                                                        <p class="no-members" style="display:none"><spring:message code="no.member.subcategories"/></p>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'portlet'}">
                                                    <div class="portlet col-md-12">
                                                        <h6 class="title"><spring:message code="portlets"/></h6>
                                                        <ul class="member-list"></ul>
                                                        <p class="no-members" style="display:none"><spring:message code="no.direct.member.portlets"/></p>
                                                    </div>
                                                </c:when>
                                            </c:choose>
                                        </c:forEach>
                                    </div><!--end: content-->
                                    <!--search-->
                                    <div id="${n}portletSearch" class="portlet-search">
                                        <form id="${n}searchForm" class="form-inline" role="form">
                                            <input type="text" class="form-control" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                            <input type="submit" class="button btn" value="<spring:message code="go"/>" />
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
                                    </div><!--end: search-->
                                </div><!--end: entity-->
                                <div id="${n}adHocGroups" class="entity-browser row" style="margin-top: 7px;">
                                    <div class="content row">
                                        <div>
                                            <div class="col-md-8">
                                                <h5 id="${n}currentAdHocGroupName" class="title">Ad Hoc Groups</h5>
                                            </div>
                                            <div id="${n}adHocCreate" class="col-md-4">
                                                <button type="button" class="btn btn-primary pull-right" data-toggle="modal" data-target="#myModal">Add Custom Group <i class="fa fa-plus-circle"></i></button>
                                            </div>
                                        </div>
                                        <div class="group col-md-12">
                                            <div id="${n}adHocBreadcrumbs" class="breadcrumbs"></div>
                                            <table id="${n}adHocMemberList" class="table table-condensed table-striped">
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Authenticated Users</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Faculty Users</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png" alt="Folder icon"/> <a href="#">Custom Group for Students</a></td>
                                                    <td>
                                                        <div class="btn-group pull-right" role= "group">
                                                            <button type="button" class="btn btn-success btn-xs">Add Group to Selection <i class="fa fa-plus-circle"></i></button>
                                                            <button type="button" class="btn btn-info btn-xs">Edit Group <i class="fa fa-pencil"></i></button>
                                                            <button type="button" class="btn btn-danger btn-xs">Delete Group <i class="fa fa-trash-o"></i></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                        </div>
                                    </div>
                                </div><!--end: ad hoc groups-->
                            </div>
                            <div class="fl-container-flex40 span4 col-md-4">
                                <!--selection-->
                                <div class="portlet-selection">
                                    <!--titlebar-->
                                    <div class="titlebar">
                                        <h4 class="title selections"><spring:message code="your.selection"/></h4>
                                    </div>
                                    <!--content-->
                                    <div class="content">
                                        <form action="${submitUrl}" method="post">
                                            <div id="${n}selectionBasket" class="selection-basket">
                                                <ul>
                                                    <c:choose>
                                                        <c:when test="${fn:length(groups) == 0}">
                                                            <li>
                                                                <span class="selection" title="<spring:message code="nothing.selected"/>"><spring:message code="nothing.selected"/></span>
                                                            </li>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:forEach items="${groups}" var="group">
                                                                <li>
                                                                    <a key="${group.entityType}:${group.id}" href="javascript:;"><c:out value="${group.name}"/></a>
                                                                    <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                                                                </li>
                                                            </c:forEach>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </ul>
                                            </div>
                                            <div id="${n}buttonPanel" class="buttons">
                                                <c:if test="${showBackButton}">
                                                    <input class="button btn" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                                </c:if>
                                                <input id="${n}buttonPrimary" class="button btn btn-primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                                <c:if test="${showCancelButton}">
                                                    <input class="button btn btn-link" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                                                </c:if>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div><!--end: content-->
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
                    entityTypes: [<c:forEach items="${selectTypes}" var="type" varStatus="status">'<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${type}</spring:escapeBody>'${status.last ? '' : ','}</c:forEach>],
                    selected: [<c:forEach items="${groups}" var="group" varStatus="status">'<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${group.entityType}:${group.id}</spring:escapeBody>'${ status.last ? '' : ',' }</c:forEach>],
                    initialFocusedEntity: '${rootEntity.entityType}:${rootEntity.id}',
                    enableAdHocGroups: ${not empty enableAdHocGroups ? enableAdHocGroups : false},
                    initialAdHocEntity: '${adHocEntity.entityType}:${adHocEntity.id}',
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
                        closeSearch: "#${n}closeDropDown",
                        searchForm: "#${n}searchForm",
                        searchDropDown: "#${n}searchDropDown",
                        searchResults: "#${n}searchResults",
                        searchResultsNoMembers: "#${n}searchResultsNoMembers",
                        searchLoader: "#${n}searchLoader",
                        adHocGroups: "#${n}adHocGroups",
                        currentAdHocGroupName: "#${n}currentAdHocGroupName",
                        adHocCreate: "#${n}adHocCreate",
                        adHocBreadcrumbs: "#${n}adHocBreadcrumbs",
                        adHocMemberList: "#${n}adHocMemberList",
                        buttonPanel: "#${n}buttonPanel",
                        buttonPrimary: "#${n}buttonPrimary"
                    },
                    messages: {
                        selectButtonMessage: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        deselectButtonMessage: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="deselect"/></spring:escapeBody>',
                        removeCrumb: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        removeSelection: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        addSelection: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        selected: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="selected"/></spring:escapeBody>',
                        nothingSelected: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="nothing.selected"/></spring:escapeBody>',
                        searchValue: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="please.enter.name"/></spring:escapeBody>'
                    }
                });
            });
        });
    </script>
