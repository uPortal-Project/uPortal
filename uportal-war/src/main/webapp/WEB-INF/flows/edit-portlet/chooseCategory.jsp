<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="queryUrl">
<portlet:param name="execution" value="${flowExecutionKey}" />
	</portlet:actionURL>
<c:set var="namespace"><portlet:namespace/></c:set>
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
  	<h2 role="heading">Choose Categories</h2>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
  	<!-- Note -->
  	<div class="portlet-note" role="note">
  		<p>Select the categories this portlet should be associated with. Categories help people find the portlet and give an indication of what the portlet content is about.</p>
    </div> <!-- end: portlet-note -->
  	
    <!-- Portlet Messages -->
  	<div class="portlet-msg-error" role="alert">
    	<form:errors path="*" element="div" />
    </div> <!-- end: portlet-msg -->
    
    <form method="POST" action="${queryUrl}">
    
    <!-- 2 Panel -->
    <div class="fl-col-flex2 portlet-2panel">
    	<!-- Left Panel -->
    	<div class="fl-col portlet-panel-left">
      	<h3>Browse Categories</h3>    
        <ul id="${namespace}categories">
        </ul>      
      </div>
      <!-- Right Panel -->
      <div class="fl-col portlet-panel-right">
      	<h3>Selected Categories</h3>
        <ul id="${namespace}selectedCategories">
          <c:forEach items="${ channel.categories }" var="category">
            <li>${categoryNames[category]} <a id="selectedCategory-${category}" href="javascript:;">delete</a></li>
          </c:forEach>
        </ul> 
        <div id="${namespace}selectedCategoryInputs" style="display: none">
          <c:forEach items="${ channel.categories }" var="category">
            <input type="hidden" name="categories" value="${category}"/>
          </c:forEach>
        </div>
      </div>
    </div>
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="Review" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button" type="submit" value="Back" class="secondary" name="_eventId_back"/>
          <input class="portlet-button portlet-button-primary" type="submit" value="Next" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
    </div>
    
    </form> <!-- End Form -->
    
  </div> <!-- end: portlet-body -->

</div> <!-- end: portlet -->
    
<script type="text/javascript">
	 up.jQuery(function() {
      var $ = up.jQuery;
	    $(document).ready(function(){
			var getCategories = function(type, root) {
				var data = { groupType: type };
				if (root != null) data.groupKey = root;
		    	$.get("ajax/groupList", data, 
		    		function(xml){
		    			var groups = $("groups", xml);
		    			var cdiv = $("#<portlet:namespace/>categories").html("");
		    			var li = $(document.createElement("li"));
		    			li.html(groups.children("rootGroup").children("name").text());
	    				li.attr("id", "category-" + $(this).attr("key"));
	    				var a = $(document.createElement("a"));
	    				a.html("+");
	    				a.attr("href", "javascript:;");
	    				a.click(addSelection);
	    				li.append(a);
		    			cdiv.append(li);
		    			var ul = $(document.createElement("ul"));
		    			cdiv.append(ul);
		    			groups.children("rootGroup").find("group").each(function(){
		    				var li = $(document.createElement("li"));
		    				li.html($(this).text() + " ");
		    				li.attr("id", "category-" + $(this).attr("key"));
		    				var a = $(document.createElement("a"))
		    					.html("+").attr("href", "javascript:;")
		    					.click(addSelection);
		    				li.append(a);
		    				a = $(document.createElement("a")).html("->")
		    					.attr("href", "javascript:;")
		    					.click(function(){ getCategories(type, $(this).parent().attr("id").split("-")[1]) });
		    				li.append(a);
		    				ul.append(li);
		    			});
		    		}, "xml"
		    	);
	    	}
			var deleteSelection = function() {
	    		$("#${namespace}selectedCategoryInputs").find(":input[value=" + $(this).attr("id").split("-")[1] + "]").remove(); 
	    		$(this).parent().remove();
			};
			var addSelection = function() {
				var id = $(this).parent().attr("id").split("-")[1];
				var li = $(document.createElement("li")).html(id + " ");
				var a = $(document.createElement("a")).html("delete").attr("href", "javscript:;")
					.attr("id", "selectedCategory-" + id).click(deleteSelection);
				$("#${namespace}selectedCategoryInputs").append(
					$(document.createElement("input")).attr("type", "hidden")
						.attr("name", "categories").val(id)
				);
				li.append(a);
				$("#${namespace}selectedCategories").append(li);
			};
	    	
			getCategories("category");
	    	$("#${namespace}selectedCategories a").click(deleteSelection);
	    });
	});
</script>