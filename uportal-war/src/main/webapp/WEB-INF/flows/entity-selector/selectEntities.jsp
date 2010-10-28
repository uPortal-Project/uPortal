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

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="submitUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>
<c:set var="selectionMode">${ selectMultiple }</c:set>
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
-->

<!-- Portlet -->
<div class="fl-widget portlet grp-mgr view-selectgroups" role="section">

	<!-- Portlet Title -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
  	<h2 class="title" role="heading"><spring:message code="${ pageTitleCode }" text="${ pageTitleText }"/></h2>
    <h3 class="subtitle"><spring:message code="${ pageSubtitleCode }" arguments="${ pageSubtitleArgument }" text="${ pageSubtitleText }"/></h3>
  </div> <!-- end: portlet-titlebar -->
  
	<!-- Portlet Body -->
  <div id="${n}chooseGroupsBody" class="fl-widget-content content portlet-content" role="main">
 
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error portlet-msg error" role="alert">
            <c:forEach var="error" items="${errors.allErrors}">
                <spring:message code="${error.code}" text="${error.defaultMessage}"/><br />
            </c:forEach>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
    
    <!-- 2 panel -->
    <div class="fl-col-flex2">  
        <!-- left panel -->
        <div class="fl-col fl-force-left">
        
            <!-- selections -->
            <div class="portlet-selection">
                <div class="titlebar">
                    <h4 class="title selections">
                        <c:choose>
                            <c:when test="${!selectMultiple}">
                                <spring:message code="your.selection"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="your.selections"/>
                            </c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="content">
                    <form action="${ submitUrl }" method="post">
                    <div id="${n}selectionBasket" class="selection-basket">
                        <ul>
                          <c:forEach items="${groups}" var="group">
                            <li>
                              <a key="${group}" href="javascript:;"><c:out value="${group.name}"/></a>
                              <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                            </li>
                          </c:forEach>
                        </ul>
                    </div>
                    <!-- buttons --> 
                    <div class="buttons">
                        <c:if test="${ showBackButton }">
                            <input class="button" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                        </c:if>
                        <input class="button primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                        <c:if test="${ showCancelButton }">
                            <input class="button" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                        </c:if>
                    </div> <!-- end: buttons --> 
                    </form>
				</div> <!-- end: selections content --> 
            </div><!-- end: selections -->
            
        </div><!-- end: left panel -->
        <!-- right panel -->
        <div class="fl-col">
        
            <!-- start: search -->
            <div class="portlet-search">
              <h4 class="title search"><spring:message code="search"/></h4>
              <form id="${n}searchForm">
                <input type="text" name="searchterm" value="<spring:message code="enter.name"/>"/>
                <input type="submit" class="button" value="<spring:message code="go"/>" />
              </form>
            </div><!-- end: search -->
    
            <!-- start: browse -->
            <div class="browse-hierarchy">
                <div class="titlebar">
                    <h4 class="title browse"><spring:message code="browse"/></h4>
                </div>
                <!-- browse content -->
                <div class="content">
                    
                    <!-- Entity -->
                    <div class="entity">
                        <!-- entity titlebar -->
                        <div id="${n}entityBrowsingHeader" class="titlebar">
                            <div id="${n}entityBrowsingBreadcrumbs" class="breadcrumb"></div>
                            <h5 class="title" id="${n}currentEntityName"></h5>
                            <div class="actions">
                                <a class="select button" id="${n}selectEntityLink" href="javascript:;"><span><spring:message code="select"/></span></a>
                            </div>
                        </div> <!-- end: titlebar -->    
                        <!-- entity selections -->
                        <div class="fl-container content">
                            <p><span class="current">Everyone</span> <spring:message code="includes"/>:</p>
                            <p id="${n}browsingResultNoMembers" style="display:none"><spring:message code="no.members"/></p>
                            <c:forEach items="${selectTypes}" var="type">
                                <c:choose>
                                    <c:when test="${type == 'group'}">
                                        <div class="group">
                                            <h6 class="title"><spring:message code="groups"/></h6>
                                            <ul class="member-list">
                                            </ul>
                                        </div>
                                    </c:when>
                                    <c:when test="${type == 'person'}">
                                        <div class="person">
                                            <h6 class="title"><spring:message code="people"/></h6>
                                            <ul class="member-list">
                                            </ul>
                                        </div>
                                    </c:when>
                                    <c:when test="${type == 'category'}">
                                        <div class="category">
                                            <h6 class="title"><spring:message code="categories"/></h6>
                                            <ul class="member-list">
                                            </ul>
                                        </div>
                                    </c:when>
                                </c:choose>
                            </c:forEach>
                        </div> <!-- end: selections -->
                    </div> <!-- end: entity -->
                
                </div> <!-- end: browse content -->
            </div> <!-- end: browse-hierarcy -->
        
        </div> <!-- end: Right panel -->
    </div> <!-- end: 2 panel -->

	<div id="${n}searchDialog" title="<spring:message code="search"/>">
	    <p id="${n}searchResultsNoMembers" style="display:none"><spring:message code="no.results"/></p>
	    <ul id="${n}searchResults"></ul>
	</div>

  </div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->

<script type="text/javascript">
	up.jQuery(function() {
		var $ = up.jQuery;

		$(document).ready(function(){
			up.entityselection("#${n}chooseGroupsBody", {
                findEntityUrl: "<c:url value="/api/findEntity"/>",
                searchEntitiesUrl: "<c:url value="/api/searchEntities"/>",
		        entityTypes: [<c:forEach items="${selectTypes}" var="type" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${type}</spring:escapeBody>'${status.last ? '' : ','}</c:forEach>],
		        selected: [<c:forEach items="${groups}" var="group" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${group.entityType}:${group.id}</spring:escapeBody>'${ status.last ? '' : ',' }</c:forEach>],
		        initialFocusedEntity: '${rootEntity.entityType}:${rootEntity.id}',
		        selectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
		        deselectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="deselect"/></spring:escapeBody>',
                selectMultiple: ${selectMultiple},
		        selectors: {
		            selectionBasket: "#${n}selectionBasket ul",
		            breadcrumbs: "#${n}entityBrowsingBreadcrumbs",
		            currentEntityName: "#${n}currentEntityName",
		            selectEntityLink: "#${n}selectEntityLink",
		            entityBrowsingHeader: "#${n}entityBrowsingHeader",
		            browsingResultNoMembers: "#${n}browsingResultNoMembers",
		            searchForm: "#${n}searchForm",
		            searchDialog: "#${n}searchDialog",
		            searchResults: "#${n}searchResults",
		            searchResultsNoMembers: "#${n}searchResultsNoMembers"
		        }
			});
		});
			
	});
</script>
