<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="submitUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<c:set var="n"><portlet:namespace/></c:set>
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
<div class="fl-widget portlet" role="section">

	<!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
  	<h2 role="heading"><spring:message code="${ pageTitleCode }" text="${ pageTitleText }"/></h2>
    <h3><spring:message code="${ pageSubtitleCode }" arguments="${ pageSubtitleChannelName }" text="${ pageSubtitleText }"/></h3>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div id="${n}chooseGroupsBody" class="fl-widget-content portlet-body" role="main">
 
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <c:forEach var="error" items="${errors.allErrors}">
                <spring:message code="${error.code}" text="${error.defaultMessage}"/><br />
            </c:forEach>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
    
    <!-- start: 2 panel -->
    <div class="fl-col-flex2">
    
      <!-- start: left panel -->
      <div class="fl-col fl-force-left">
      	<!-- start: selections -->
      	<div class="portlet-selection">
        
          <h4 class="portlet-heading-selections"><spring:message code="chooseGroups.selectionsHeading"/></h4>
          <form action="${ submitUrl }" method="post">
          <div id="${n}selectionBasket" class="portlet-selection-basket">
            <ul>
              <c:forEach items="${model.groups}" var="group">
                <li>
                  <a key="${group}" href="javascript:;"><c:out value="${groupNames[group]}"/></a>
                  <input type="hidden" name="groups" value="<c:out value="${group}"/>"/>
                </li>
              </c:forEach>
            </ul>
          </div>
          
          <!-- Portlet Buttons --> 
          <div class="portlet-button-group">
            <c:if test="${ showBackButton }">
              <input class="portlet-button" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
            </c:if>
              <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
            <c:if test="${ showCancelButton }">
              <input class="portlet-button" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
            </c:if>
          </div> <!-- end: Portlet Buttons --> 
          
          </form>
        
        </div><!-- end: selections -->
      </div><!-- end: left panel -->
      
      
      <!-- start: right panel -->
      <div class="fl-col">
        
        <!-- start: search -->
        <div class="portlet-search">
          <h4 class="portlet-heading-search"><spring:message code="chooseGroups.searchHeading"/></h4>
          <form id="${n}searchForm">
            <input type="text" name="searchterm" value="<spring:message code="chooseGroups.searchBoxDefault"/>"/>
            <input type="submit" value="<spring:message code="chooseGroups.searchSubmit"/>" />
          </form>
        </div><!-- end: search -->

        <!-- start: browse -->
        <div class="portlet-browse">
          <h4 class="portlet-heading-browse"><spring:message code="chooseGroups.browseHeading"/></h4>
          <!-- Not yet implemented
          <ul class="fl-tabs fl-tabs-left">
            <li class="fl-activeTab"><a href="#" title="<spring:message code="chooseGroups.groupsHeading"/>"><span><spring:message code="chooseGroups.groupsHeading"/></span></a></li>
            <li><a href="#" title="<spring:message code="chooseGroups.favoritesHeading"/>"><span><spring:message code="chooseGroups.favoritesHeading"/></span></a></li>
            <li><a href="#" title="<spring:message code="chooseGroups.recentlySelectedHeading"/>"><span><spring:message code="chooseGroups.recentlySelectedHeading"/></span></a></li>
          </ul>-->
          
          <!-- start: browse content -->
          <div class="fl-tab-content">
            
            <!-- start: browse content header -->
            <div id="${n}entityBrowsingHeader" class="portlet-browse-header">
            	<div id="${n}entityBrowsingBreadcrumbs" class="portlet-browse-breadcrumb"></div>
              <div class="fl-container fl-col-flex2">
                <div class="fl-col">
                    <h5 id="${n}currentEntityName"></h5>
                </div>
                <div class="fl-col fl-text-align-right">
                  <a class="portlet-browse-select" id="${n}selectEntityLink" href="javascript:;"><span><spring:message code="chooseGroups.selectButton"/></span></a>
                </div>
              </div>
            </div>
            <!-- end: browse content header -->
            
            <!-- start: browse content: selections -->
            <div class="fl-container portlet-browse-body">
              <p><span class="current-entity-name">Everyone</span> <spring:message code="chooseGroups.includes"/>:</p>
              <p id="${n}browsingResultNoMembers" style="display:none"><spring:message code="chooseGroups.noMembers"/></p>
              <c:forEach items="${selectTypes}" var="type">
                <c:choose>
                  <c:when test="${type == 'group'}">
                    <h7><spring:message code="chooseGroups.groupsHeading"/></h7>
                    <ul class="group-member-list">
                    </ul>
                  </c:when>
                  <c:when test="${type == 'person'}">
                    <h7><spring:message code="chooseGroups.peopleHeading"/></h7>
                    <ul class="person-member-list">
                    </ul>
                  </c:when>
                  <c:when test="${type == 'category'}">
                    <h7><spring:message code="chooseGroups.categoriesHeading"/></h7>
                    <ul class="category-member-list">
                    </ul>
                  </c:when>
                </c:choose>
              </c:forEach>
            </div>
            <!-- end: browse content: selections -->  
          
          </div> <!-- end: browse content -->
          
        </div> <!-- end: portlet-browse -->
        
      </div> <!-- end: left panel -->
    
    </div> <!-- end: 2 panel -->

	<div id="${n}searchDialog" title="<spring:message code="chooseGroups.searchResultsTitle"/>">
	    <p id="${n}searchResultNoMembers" style="display:none"><spring:message code="chooseGroups.noResults"/></p>
	    <ul id="${n}searchResults"></ul>
	</div>

  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->

<script src="media/org/jasig/portal/flows/groups-selector/groups-selector.min.js" language="JavaScript" type="text/javascript"></script>

<script type="text/javascript">
	up.jQuery(function() {
		var $ = up.jQuery;

		$(document).ready(function(){
			uportal.entityselection("#${n}chooseGroupsBody", {
		        entityTypes: [<c:forEach items="${selectTypes}" var="type" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${type}</spring:escapeBody>'${status.last ? '' : ','}</c:forEach>],
		        selected: [<c:forEach items="${model.groups}" var="group" varStatus="status">'<spring:escapeBody javaScriptEscape="true">${group}</spring:escapeBody>'${ status.last ? '' : ',' }</c:forEach>],
		        initialFocusedEntity: '${rootEntityId}',
		        selectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="chooseGroups.selectButton"/></spring:escapeBody>',
		        deselectButtonMessage: '<spring:escapeBody javaScriptEscape="true"><spring:message code="chooseGroups.deselectButton"/></spring:escapeBody>',
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
