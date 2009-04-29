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
  	<h2 role="heading">${ pageTitleText }</h2>
    <h3>${ pageSubtitleText }</h3>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
 		
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="model">
	    <div class="portlet-msg-error" role="alert">
	      <form:errors path="*" element="div" />
	    </div>
    </spring:hasBindErrors> <!-- end: portlet-msg -->
    
    <!-- start: 2 panel -->
    <div class="fl-col-flex2">
    
      <!-- start: left panel -->
      <div class="fl-col fl-force-left">
      	<!-- start: selections -->
      	<div class="portlet-selection">
        
          <h4 class="portlet-heading-selections">Your Selections</h4>
          <form action="${ queryUrl }" method="post">
          <div id="${n}selectionBasket" class="portlet-selection-basket">
            <ul>
              <c:forEach items="${model.groups}" var="group">
                <li>
                  <a key="${group}" href="javascript:;">${groupNames[group]}</a>
                  <input type="hidden" name="groups" value="${group}"/>
                </li>
              </c:forEach>
            </ul>
          </div>
          
          <!-- Portlet Buttons --> 
          <div class="portlet-button-group">
            <c:if test="${ showBackButton }">
              <input class="portlet-button" type="submit" value="${ backButtonText }" name="_eventId_back"/>
            </c:if>
              <input class="portlet-button portlet-button-primary" type="submit" value="${ saveButtonText }" name="_eventId_save"/>
            <c:if test="${ showCancelButton }">
              <input class="portlet-button" type="submit" value="${ cancelButtonText }" name="_eventId_cancel"/>
            </c:if>
          </div> <!-- end: Portlet Buttons --> 
          
          </form>
        
        </div><!-- end: selections -->
      </div><!-- end: left panel -->
      
      
      <!-- start: right panel -->
      <div class="fl-col">
        
        <!-- start: search -->
        <div class="portlet-search">
          <h4 class="portlet-heading-search">Search</h4>
          <form id="${n}searchForm">
            <input type="text" name="searchterm" value="Enter a name" />
            <input type="submit" value="Go" />
          </form>
        </div><!-- end: search -->

        <!-- start: browse -->
        <div class="portlet-browse">
          <h4 class="portlet-heading-browse">Browse</h4>
          <!-- Not yet implemented
          <ul class="fl-tabs fl-tabs-left">
            <li class="fl-activeTab"><a href="#" title="Groups"><span>Groups</span></a></li>
            <li><a href="#" title="Favorites"><span>Favorites</span></a></li>
            <li><a href="#" title="Recently Selected"><span>Recently Selected</span></a></li>
          </ul>-->
          
          <!-- start: browse content -->
          <div class="fl-tab-content">
            
            <!-- start: browse content header -->
            <div id="${n}groupBrowsingHeader" class="portlet-browse-header">
            	<div id="${n}groupBrowsingBreadcrumbs" class="portlet-browse-breadcrumb"></div>
              <div class="fl-container fl-col-flex2">
                <div class="fl-col">
                    <h5 id="${n}currentGroupName"></h5>
                </div>
                <div class="fl-col fl-text-align-right">
                  <a class="portlet-browse-select" id="${n}selectGroupLink" href="javascript:;"><span>Select</span></a>
                </div>
              </div>
            </div>
            <!-- end: browse content header -->
            
            <!-- start: browse content: selections -->
            <div class="fl-container portlet-browse-body">
              <p><span class="current-group-name">Everyone</span> includes:</p>
              <p id="${n}browsingResultNoMembers" style="display:none">No members</p>
              <c:forEach items="${selectTypes}" var="type">
                <c:choose>
                  <c:when test="${type == 'group'}">
                    <h7>Groups</h7>
                    <ul class="group-member">
                    </ul>
                  </c:when>
                  <c:when test="${type == 'person'}">
                    <h7>People</h7>
                    <ul class="person-member">
                    </ul>
                  </c:when>
                  <c:when test="${type == 'category'}">
                    <h7>Categories</h7>
                    <ul class="category-member">
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
    
  </div> <!-- end: portlet-body -->
  
</div> <!-- end: portlet -->

<div id="${n}searchDialog" title="Search">
    <p id="${n}searchResultNoMembers" style="display:none">No results</p>
    <ul id="${n}searchResults"></ul>
</div>

<script type="text/javascript">
	up.jQuery(function() {
			var $ = up.jQuery;
			var groupBrowser = $.groupbrowser({});
			var entities = new Array();
			var searchInitialized = false;
			var entityTypes = [<c:forEach items="${selectTypes}" var="type" varStatus="status">'${type}'${status.last ? '' : ','}</c:forEach>];
			var selected = [ <c:forEach items="${model.groups}" var="group" varStatus="status">'${group}'${ status.last ? '' : ',' }</c:forEach> ];

			var updateBreadcrumbs = function(entity) {
                var currentTitle = $("#${n}currentGroupName");
						var breadcrumbs = $("#${n}groupBrowsingBreadcrumbs");
						if (breadcrumbs.find("span a[key=" + entity.id + "]").size() > 0) {
								// if this entity already exists in the breadcrumb trail
								var removeBreadcrumb = false;
								$(breadcrumbs.find("span")).each(function(){
										if (removeBreadcrumb) { $(this).remove(); }
										else if ($(this).find("a[key=" + entity.id + "]").size() > 0) { 
												removeBreadcrumb = true;
												$(this).remove();
										}
								});
						} else {
								// otherwise, append this entity to the end of the breadcrumbs
								if (currentTitle.text() != '') {
									var breadcrumb = $(document.createElement("span"));
									breadcrumb.append(
											$(document.createElement("a")).html(currentTitle.text())
													.attr("href", "javascript:;").attr("key", currentTitle.attr("key"))
													.click(function(){browseGroup($(this).attr("key"));})
									).append(document.createTextNode(" > "));
									$("#${n}groupBrowsingBreadcrumbs").append(breadcrumb);
								}
						}
                        currentTitle.text(entity.name).attr("key", entity.id);
			};

			var selectGroup = function(key) {
				if ($.inArray(key, selected) < 0) {
						var entity = groupBrowser.getEntity(entityTypes, key);
						$("#${n}selectionBasket ul").append(
								$(document.createElement("li")).append(
									$(document.createElement("a")).html(entity.name)
											.attr("href", "javascript:;").attr("key", entity.id)
											.click(function(){ removeGroup($(this).attr("key")); })
								).append(
										$(document.createElement("input")).attr("type", "hidden")
												.attr("name", "groups").val(entity.id)
								)
						);
                        selected.push(key);
                if ($("#${n}currentGroupName").attr("key") == key) {
                    setBreadcrumbSelectionState(true);
                }
				}
			};
			
			var setBreadcrumbSelectionState = function(selected) {
                if (!selected) {
                    $("#${n}selectGroupLink span").text("Select").unbind("click")
                        .click(function(){ selectGroup($("#${n}currentGroupName").attr("key")); });
                    $("#${n}groupBrowsingHeader").removeClass("selected");
                } else {
                    $("#${n}selectGroupLink span").text("De-select").unbind("click")
                        .click(function(){ removeGroup($("#${n}currentGroupName").attr("key")); });
                    $("#${n}groupBrowsingHeader").addClass("selected");
                }
			};

			var removeGroup = function(key) {
				var newselections = new Array();
				$("#${n}selectionBasket a").each(function(){
					if ($(this).attr("key") != key) newselections.push($(this).attr("key"));
					else $(this).parent().remove();  
			    });
			    selected = newselections;
			    if ($("#${n}currentGroupName").attr("key") == key) {
			        setBreadcrumbSelectionState(false);
			    }
			};

			var browseGroup = function(key) {
						var entity = groupBrowser.getEntity(entityTypes, key);
						updateBreadcrumbs(entity);
				
				$(".current-group-name").text(entity.name);
				$(".category-member").html("");
				$(".group-member").html("");
						$(".person-member").html("");
				$(entity.children).each(function(i){
								var link = $(document.createElement("a")).attr("href", "javascript:;")
									.html("<span>" + this.name + "</span>").attr("key", this.id)
									.click(function(){ browseGroup($(this).attr("key")); });
						$("." + this.entityType + "-member").append(
							$(document.createElement("li")).addClass(this.entityType).append(link)
					);
				});
				$(entityTypes).each(function(){
					var results = $("." + this + "-member");
					if (results.find("li").size() == 0) results.prev().css("display", "none");
					else results.prev().css("display", "block");
				});
				if ($(".portlet-browse-body li").size() == 0) {
                    $("#${n}browsingResultNoMembers").css("display", "block");
				} else {
                    $("#${n}browsingResultNoMembers").css("display", "none");
				}
				if ($.inArray(key, selected) < 0) {
                    setBreadcrumbSelectionState(false);
				} else {
                    setBreadcrumbSelectionState(true);
				}
			};

			var search = function(searchTerm) {
				var entities = groupBrowser.searchEntities(entityTypes, searchTerm);
				var list = $("#${n}searchResults").html("");
				$(entities).each(function(){
                    var link = $(document.createElement("a")).attr("href", "javascript:;")
                        .html("<span>" + this.name + "</span>").attr("key", this.id)
                        .click(function(){ selectGroup($(this).attr("key")); $(this).addClass("selected"); });
                    list.append($(document.createElement("li")).addClass(this.entityType).append(link));
				});
                if ($("#${n}searchResults li").size() == 0) {
                    $("#${n}searchResultNoMembers").css("display", "block");
                } else {
                    $("#${n}searchResultNoMembers").css("display", "none");
                }
				if (searchInitialized) {
	                $("#${n}searchDialog").dialog('open');
				} else { 
	                $("#${n}searchDialog").dialog({ width:550, modal:true });
	                searchInitialized = true;
				}
				return false;
			};

			$(document).ready(function(){
				browseGroup('${rootEntityId}');
				$("#${n}selectionBasket a").click(function(){ removeGroup($(this).attr("key")); });
				$("#${n}searchForm").submit(function(){ return search(this.searchterm.value) });
                $("#${n}searchForm input[name=searchterm]").focus(function(){ $(this).val(""); $(this).unbind("focus"); });
			});
			
	});
</script>
