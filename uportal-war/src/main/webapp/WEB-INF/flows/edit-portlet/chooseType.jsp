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
<div class="fl-widget portlet ptl-mgr view-choosetype" role="section">

	<!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
  	<h2 class="title" role="heading">
		  <c:choose>
        <c:when test="${ completed }">
          <spring:message code="edit-portlet.editPortletHeading"/>
        </c:when>
        <c:otherwise>
          <spring:message code="edit-portlet.newPortletHeading"/>
        </c:otherwise>
      </c:choose>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
	<!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
     
    <form:form modelAttribute="channel" action="${queryUrl}" method="POST">
	
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="channel">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
		
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="chooseType.selectTypeHeading"/></h3>
      </div>
      <div class="content">
      
        <table class="portlet-table" summary="<spring:message code="chooseType.portletTypesTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="chooseType.optionHeading"/></th>
              <th><spring:message code="chooseType.typeHeading"/></th>
              <th><spring:message code="chooseType.descriptionHeading"/></th>
            </tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ channelTypes }" var="chanTypeEntry">
              <c:if test="${!chanTypeEntry.value.deprecated}">
                <tr>
                  <td align="center">
                    <form:radiobutton path="typeId" value="${ chanTypeEntry.key.id  }" cssClass="portlet-form-input-field"/>
                  </td>
                  <td><c:out value="${ chanTypeEntry.key.name }"/></td>
                  <td><c:out value="${ chanTypeEntry.key.description }"/></td>
                </tr>
              </c:if>
            </c:forEach>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->

    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="chooseType.selectDeprecatedTypeHeading"/></h3>
      </div>
      <div class="content">
      
        <table class="portlet-table" summary="<spring:message code="chooseType.deprecatedTypesTableSummary"/>">
          <thead>
            <tr>
              <th><spring:message code="chooseType.optionHeading"/></th>
              <th><spring:message code="chooseType.typeHeading"/></th>
              <th><spring:message code="chooseType.descriptionHeading"/></th>
            </tr>
          </thead>
          <tfoot></tfoot>
          <tbody>
            <c:forEach items="${ channelTypes }" var="chanTypeEntry">
              <c:if test="${chanTypeEntry.value.deprecated}">
                <tr>
                  <td align="center">
                    <form:radiobutton path="typeId" value="${ chanTypeEntry.key.id  }" cssClass="portlet-form-input-field"/>
                  </td>
                  <td><c:out value="${ chanTypeEntry.key.name }"/></td>
                  <td><c:out value="${ chanTypeEntry.key.description }"/></td>
                </tr>
              </c:if>
            </c:forEach>
          </tbody>
        </table>
        
      </div>
    </div> <!-- end: portlet-section -->

		<!-- Buttons -->    
    <div class="buttons">
      <c:choose>
        <c:when test="${ completed }">
          <input class="button primary" type="submit" value="<spring:message code="edit-portlet.reviewButton"/>" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="button primary" type="submit" value="<spring:message code="edit-portlet.nextButton"/>" name="_eventId_next"/>
        </c:otherwise>
      </c:choose>
      <input class="button" type="submit" value="<spring:message code="edit-portlet.cancelButton"/>" name="_eventId_cancel"/>
    </div> <!-- end: Portlet Buttons --> 
    
    </form:form>  <!-- End Form -->
    
  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->