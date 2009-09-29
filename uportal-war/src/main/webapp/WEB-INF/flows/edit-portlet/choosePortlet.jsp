<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
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
    <h2 role="heading">
       <spring:message code="edit-portlet.newPortletHeading"/>
    </h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div" />
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
    
    <form action="${queryUrl}" method="POST">
        
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="basicInfo.summaryHeading"/></h3>
      <div class="portlet-section-body">

        <select name="application">
            <c:forEach items="${contexts}" var="context">
                <option value="${context.applicationId}">${context.portletContextName != null ? context.portletContextName : context.applicationName}</option>
            </c:forEach>
        </select>
        
        <select name="portlet">
            <c:forEach items="${contexts[0].portletApplicationDefinition.portlets}" var="portlet">
                <option value="${portlet.portletName}">${fn:length(portlet.displayNames) > 0 ? portlet.displayNames[0].displayName : portlet.portletName}</option>
            </c:forEach>
        </select>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.reviewButton"/>" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button secondary" type="submit" value="<spring:message code="edit-portlet.backButton"/>" name="_eventId_back"/>
          <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="edit-portlet.nextButton"/>" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="portlet-button secondary" type="submit" value="<spring:message code="edit-portlet.cancelButton"/>" name="_eventId_cancel"/>
    </div>
    
    </form> <!-- End Form -->
            
    </div> <!-- end: portlet-body -->
    
    <script type="text/javascript">
	    up.jQuery(function() {
	        var $ = up.jQuery;
	        var portlets = {};
	        <c:forEach items="${contexts}" var="context">
	            portlets['${context.applicationId}'] = [<c:forEach items="${context.portletApplicationDefinition.portlets}" var="portlet" varStatus="status">{name: '${portlet.portletName}', title: '${fn:length(portlet.displayNames) > 0 ? portlet.displayNames[0].displayName : portlet.portletName}'}${status.last ? '' : ','}</c:forEach>];
	        </c:forEach>
	        $(document).ready(function(){
	            $("select[name=application]").change(function(){
	                var select = $("select[name=portlet]").html("");
	                var p = portlets[$(this).val()];
	                $(p).each(function(i){
                        select.get(0).options[i] = new Option(this.title, this.name);
	                });
	            });
	        });
        });
        
        
    </script>
        
</div> <!-- end: portlet -->