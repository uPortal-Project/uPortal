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
		  <c:choose>
        <c:when test="${ completed }">
          <spring:message code="edit-portlet.editPortletHeading"/>
        </c:when>
        <c:otherwise>
          <spring:message code="edit-portlet.newPortletHeading"/>
        </c:otherwise>
      </c:choose>
    </h2>
  </div> <!-- end: portlet-title -->
  
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
     
    <form:form modelAttribute="channel" action="${queryUrl}" method="POST">

    <div class="portlet-msg-info" role="alert">
        This page is a placeholder for future functionality.  The values set below
        are not persisted.
    </div>
	
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>

		
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading"><spring:message code="lifecycle.heading"/></h3>
      <div class="portlet-section-body">
      
        <table summary="<spring:message code="chooseType.portletTypesTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="lifecycle.optionHeading"/></th>
              <th><spring:message code="lifecycle.stateHeading"/></th>
              <th><spring:message code="lifecycle.descriptionHeading"/></th>
            </tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ lifecycleStates }" var="lifecycleState">
	            <tr>
	              <td align="center">
	                <form:radiobutton path="lifecycleState" value="${ lifecycleState }" cssClass="portlet-form-input-field"/>
	              </td>
	              <td><spring:message code="lifecycle.name.${ lifecycleState }"/></td>
	              <td><spring:message code="lifecycle.description.${ lifecycleState }"/></td>
	            </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
       
      <h3 class="portlet-section-header" role="heading"><spring:message code="lifecycle.datesHeading"/></h3>
      <div class="portlet-section-body"> 
 
        <table summary="<spring:message code="lifecycle.datesSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="lifecycle.optionHeading"/></th>
              <th><spring:message code="lifecycle.settingHeading"/></th>
            </tr>
          </thead>
          <tbody>
            <tr>
                <td class="fl-text-align-right"><spring:message code="lifecycle.publishDate"/></td>
                <td><form:input path="publishDate"/></td>
            </tr>  
            <tr>
                <td class="fl-text-align-right"><spring:message code="lifecycle.expirationDate"/></td>
                <td><form:input path="expirationDate"/></td>
           </tr>      
          </tbody>
        </table>
 
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
      <input class="portlet-button" type="submit" value="<spring:message code="edit-portlet.cancelButton"/>" name="_eventId_cancel"/>
    </div> <!-- end: Portlet Buttons --> 
    
    </form:form>  <!-- End Form -->
    
  </div> <!-- end: portlet-body -->

</div> <!-- end: portlet -->