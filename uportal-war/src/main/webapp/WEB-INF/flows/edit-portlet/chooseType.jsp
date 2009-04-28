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
          Edit Portlet
        </c:when>
        <c:otherwise>
          Register New Portlet
        </c:otherwise>
      </c:choose>
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
     
    <form:form modelAttribute="channel" action="${queryUrl}" method="POST">
		
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Select Type</h3>
      <div class="portlet-section-body">
      
        <table summary="This table lists the type of portlets available">
          <thead>
            <tr>
              <th>Option</th>
              <th>Type</th>
              <th>Description</th>
            </tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ channelTypes }" var="chanType">
              <tr>
                <td align="center">
                  <form:radiobutton path="typeId" value="${ chanType.id  }" cssClass="portlet-form-input-field"/>
                </td>
                <td>${ chanType.name }</td>
                <td>${ chanType.description }</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->

		<!-- Portlet Buttons -->    
    <div class="portlet-button-group">
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="Review" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button portlet-button-primary" type="submit" value="Next" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="portlet-button" type="submit" value="Cancel" name="_eventId_cancel"/>
    </div> <!-- end: Portlet Buttons --> 
    
    </form:form>  <!-- End Form -->
    
  </div> <!-- end: portlet-body -->

</div> <!-- end: portlet -->