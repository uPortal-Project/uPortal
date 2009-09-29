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
      <spring:message code="confirmRemove.title"/>
    </h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <form:form modelAttribute="channel" action="${submitUrl}" method="POST">

    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>

    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="confirmRemove.heading" arguments="${ channel.name }"/></h3>
      <div class="portlet-section-body">
        <spring:message code="confirmRemove.text" arguments="${ channel.name }"/>
	  </div>
	</div> <!-- end: portlet-section -->
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <input class="portlet-button portlet-button-primary" type="submit" value="<spring:message code="confirmRemove.removeButton"/>" name="_eventId_remove"/>
      <input class="portlet-button secondary" type="submit" value="<spring:message code="confirmRemove.cancelButton"/>" name="_eventId_cancel"/>
    </div>
    
    </form:form> <!-- End Form -->
            
	</div> <!-- end: portlet-body -->
        
</div> <!-- end: portlet -->