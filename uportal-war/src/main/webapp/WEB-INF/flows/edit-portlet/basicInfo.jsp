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
      <h3 class="portlet-section-header" role="heading">Summary Information</h3>
      <div class="portlet-section-body">

        <table summary="This table lists a portlet's general settings.">
          <thead>
            <tr>
            	<th>Option</th>
              <th>Setting</th>
            </tr>
          </thead>
          <tbody>
            <tr>
            	<td class="fl-text-align-right">Channel Title:</td>
            	<td><form:input path="title"/></td>
            </tr>  
            <tr>
            	<td class="fl-text-align-right">Channel Name:</td>
            	<td><form:input path="name"/></td>
           </tr>      
            <tr>
            	<td class="fl-text-align-right">Channel Functional Name:</td>
            	<td><form:input path="fname"/></td>
            </tr>     
            <tr>
            	<td class="fl-text-align-right">Channel Description:</td>
            	<td><form:input path="description"/></td>
            </tr> 
            <tr>
            	<td class="fl-text-align-right">Channel Timeout:</td>
            	<td><form:input path="timeout"/>ms</td>
            </tr>  
            <tr>
            	<td class="fl-text-align-right">Channel Secure:</td>
            	<td><form:checkbox path="secure"/></td>
            </tr> 
            <c:if test="${ channel.typeId < 0 }">
              <tr>
              	<td class="fl-text-align-right">Channel class:</td>
              	<td><form:input path="javaClass"/></td>
              </tr>
          	</c:if>
          </tbody>
        </table>
        
			</div>
		</div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Controls</h3>
      <div class="portlet-section-body">
      
      	<fieldset>
        	<legend>Portlet Controls</legend>
          <c:forEach items="${ cpd.controls.controls }" var="control">
            <c:if test="${ control.override }">
              <c:choose>
                <c:when test="${ control.type == 'help' }">
                  <c:set var="controlPath" value="hasHelp"/>
                </c:when>
                <c:when test="${ control.type == 'edit' }">
                  <c:set var="controlPath" value="editable"/>
                </c:when>
                <c:when test="${ control.type == 'about' }">
                  <c:set var="controlPath" value="hasAbout"/>
                </c:when>
              </c:choose>
              <form:input path="${controlPath}"/>
            </c:if>
            <input type="checkbox" name="${ control.type }" value="${ control.type }"/><label for="${ control.type }">${ control.type }</label><br/>
          </c:forEach>
        </fieldset>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <c:choose>
        <c:when test="${ completed }">
          <input class="portlet-button portlet-button-primary" type="submit" value="Review" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="portlet-button secondary" type="submit" value="Back" name="_eventId_back"/>
          <input class="portlet-button portlet-button-primary" type="submit" value="Next" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="portlet-button secondary" type="submit" value="Cancel" name="_eventId_cancel"/>
    </div>
    
    </form:form> <!-- End Form -->
            
	</div> <!-- end: portlet-body -->
        
</div> <!-- end: portlet -->