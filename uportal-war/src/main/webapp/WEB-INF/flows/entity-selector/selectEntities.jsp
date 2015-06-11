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
    <link href="<c:url value="/scripts/jstree/style.min.css"/>" rel="stylesheet" type="text/css" />

    <script type="text/javascript" src="<c:url value="/scripts/jstree/jstree.min.js"/>"></script>

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
            <div class="${selectionMode ? 'view-multi-select' : 'view-single-select'}">
                <div class="columns-2 row-fluid row">
                    <div class="fl-container-flex60 span8 col-md-8">
                        <!-- entity -->
                        <div id="${n}entityBrowserContent" class="entity-browser row">
                            <div class="content row">
                                <div>
                                    <div class="col-md-6">
                                        <h4 class="title">
                                            <span id="${n}currentEntityName">Ad Hoc Groups</span>
                                            <button id="${n}currentSelectBtn" type="button" class="btn btn-success btn-xs">Add to Selection <i class="fa fa-plus-circle"></i></button>
                                        </h4>
                                    </div>
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
                                </div>
                                <div class="col-md-12">
                                    <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                        <c:forEach items="${selectTypes}" var="type">
                                            <c:choose>
                                                <c:when test="${type == 'group'}">
                                                    <div class="group col-md-12">
                                                        <h6 class="title"><spring:message code="groups"/></h6>
                                                        <table class="table table-condensed table-striped member-list"></table>
                                                        <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                                        <div id="${n}adHocCreate" class="col-md-4">
                                                            <button type="button" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#adhocGroupModal">Add Custom Group <i class="fa fa-plus-circle"></i></button>
                                                        </div>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'person'}">
                                                    <div class="person col-md-12">
                                                        <h6 class="title"><spring:message code="people"/></h6>
                                                        <table class="table table-condensed table-striped member-list"></table>
                                                        <p class="no-members" style="display:none"><spring:message code="no.direct.member.people"/></p>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'category'}">
                                                    <div class="category col-md-12">
                                                        <h6 class="title"><spring:message code="categories"/></h6>
                                                        <table class="table table-condensed table-striped member-list"></table>
                                                        <p class="no-members" style="display:none"><spring:message code="no.member.subcategories"/></p>
                                                    </div>
                                                </c:when>
                                                <c:when test="${type == 'portlet'}">
                                                    <div class="portlet col-md-12">
                                                        <h6 class="title"><spring:message code="portlets"/></h6>
                                                        <table class="table table-condensed table-striped member-list"></table>
                                                        <p class="no-members" style="display:none"><spring:message code="no.direct.member.portlets"/></p>
                                                    </div>
                                                </c:when>
                                            </c:choose>
                                        </c:forEach>
                                </div>
                            </div>
                        </div><!--end: ad hoc groups-->
                    </div>
                    <div class="fl-container-flex40 span4 col-md-4">
                        <!--selection-->
                        <div class="portlet-selection">
                            <!--titlebar-->
                            <div class="titlebar">
                                <h4 class="title selections"><spring:message code="${selectionMode ? 'your.selections' : 'your.selection'}"/></h4>
                            </div>
                            <!--content-->
                            <div class="content">
                                <form action="${submitUrl}" method="post">
                                    <div id="${n}selectionBasket" class="selection-basket">
                                        <ul>
                                            <c:choose>
                                                <c:when test="${!selectionMode && fn:length(groups) == 0}">
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
            </div><!--end:view-select-->

    <!-- Adhoc-Group Modal -->
    <div class="modal fade" id="adhocGroupModal" tabindex="-1" role="dialog" aria-labelledby="adhocGroupModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel">Create/Edit Custom Group</h4>
                </div> <!-- end .modal-header div -->
                <div class="modal-body">
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="groupName" class="col-sm-4 control-label">Group Name</label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control" id="groupName" placeholder="Group Name">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="groupDesc" class="col-sm-4 control-label">Group Description</label>
                            <div class="col-sm-8">
                                <textarea class="form-control" id="groupDesc" rows="3" placeholder="Group Description" readonly="readonly"></textarea>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-6">
                                <div class="panel panel-default">
                                    <div class="panel-body">
                                        <h5>Group must <strong>INCLUDE</strong></h5>
                                        <!-- Data Include Node Tree -->
                                        <div id="${n}dataIncludes" class="demo"></div>
                                        <hr/>
                                        <ul id="${n}dataIncludesList">
                                        </ul>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="panel panel-default">
                                    <div class="panel-body">
                                        <h5>Group must <strong>EXCLUDE</strong></h5>
                                        <!-- Data Exclude Node Tree -->
                                        <div id="${n}dataExcludes" class="demo"></div>
                                        <hr/>
                                        <ul id="${n}dataExcludesList">
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div> <!-- end .modal-content div -->
                <div class="modal-footer">
                    <button id="${n}cancelAdHocButton" type="button" class="btn btn-default" data-dismiss="modal">Close <i class="fa fa-times"></i></button>
                    <button id="${n}saveAdHocButton" type="button" class="btn btn-primary" data-dismiss="modal">Save changes <i class="fa fa-save"></i></button>
                </div> <!-- end .modal-footer div -->
            </div> <!-- end .modal-content div -->
        </div> <!-- end .modal-dialog div -->
    </div> <!-- end #content div -->
        </div><!--end:portlet-content-->
    </div><!--end:portlet-->

    <script type="text/javascript">
    </script>

    <script type="text/javascript">
        var setGroupDescription = function() {
            var includes, excludes, description;
            descInput = $("#groupDesc");
            includes = $("li", "#${n}dataIncludesList").map(function () { return $(this).text(); }).get();
            excludes = $("li", "#${n}dataExcludesList").map(function () { return $(this).text(); }).get();

            if (includes.length + excludes.length == 0) {
                $("#groupDesc").val("Collection of ad hoc groups");
                return;
            }

            description = "Users who";

            if (includes.length > 0) {
                description += " are members of ("
                description += includes.toString();
                description += ")";
            }

            if (excludes.length > 0) {
                if (includes.length > 0) {
                    description += " but";
                }
                description += " are not members of ("
                description += excludes.toString();
                //trim last ,
                description += ")";
            }

            $("#groupDesc").val(description);
        };

        var setJSTreeEventListeners = function() {
            $("#${n}dataIncludes").on("changed.jstree", function (e, data) {
                var list = $("#${n}dataIncludesList");
                list.empty();
                $.each(data.selected, function (i, name) {
                    var li = $("<li/>").text(data.instance.get_node(name).text).appendTo(list);
                });
                setGroupDescription();
            });

            $("#${n}dataExcludes").on("changed.jstree", function (e, data) {
                var list = $("#${n}dataExcludesList");
                list.empty();
                $.each(data.selected, function (i, name) {
                    var li = $("<li/>").text(data.instance.get_node(name).text).appendTo(list);
                });
                setGroupDescription();
            });

            $("#${n}dataIncludes").on("loaded.jstree", resetAdHocDialog);
            $("#${n}dataExcludes").on("loaded.jstree", resetAdHocDialog);
        };

        var createTestMaps = function (groupNames, testAttr) {
            var tests = [];
            $.each(groupNames, function (i, name) {
                tests.push( { "testValue": name, "attributeName": testAttr,
                              "testerClassName" : "org.jasig.portal.groups.pags.testers.AdHocGroupTester" } );
            });
            return tests;
        };

        var resetAdHocDialog = function () {
            console.log("resetting ad hoc dialog");
            $("#groupName").val("");

            $(":jstree").each(function () {
                $(this).jstree("deselect_all",false);
                /* var node = $(this).jstree("get_node", "${rootEntity.entityType}:${rootEntity.id}"); */
                $(this).jstree("open_node", "${rootEntity.entityType}:${rootEntity.id}");
            });

            /*
            $("#${n}dataIncludesList").empty();
            $("#${n}dataExcludesList").empty();
            */
            setGroupDescription();
        };

        $("#${n}cancelAdHocButton").bind("click", resetAdHocDialog);

        var displayResponseMessage = function(xmlhttp) {
            console.log("display response message");
            console.log(xmlhttp.responseText);
        };

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
                    selectMultiple: ${selectionMode},
                    requireSelection: ${ not empty requireSelection ? requireSelection : true },
                    pagsApiUrl: "<c:url value='/api/v4-3/pags/'/>",
                    selectors: {
                        selectionBasket: "#${n}selectionBasket",
                        breadcrumbs: "#${n}entityBreadcrumbs",
                        currentEntityName: "#${n}currentEntityName",
                        entityBrowserContent: "#${n}entityBrowserContent",
                        closeSearch: "#${n}closeDropDown",
                        searchForm: "#${n}searchForm",
                        searchDropDown: "#${n}searchDropDown",
                        searchResults: "#${n}searchResults",
                        searchResultsNoMembers: "#${n}searchResultsNoMembers",
                        searchLoader: "#${n}searchLoader",
                        currentSelectBtn: "#${n}currentSelectBtn",
                        adHocCreate: "#${n}adHocCreate",
                        dialogIncludesTree: '#${n}dataIncludes',
                        dataIncludesList: '#${n}dataIncludesList',
                        dialogExcludesTree: '#${n}dataExcludes',
                        dataExcludesList: '#${n}dataExcludesList',
                        saveAdHocButton: '#${n}saveAdHocButton',
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

                setJSTreeEventListeners();
            });
       });
    </script>
