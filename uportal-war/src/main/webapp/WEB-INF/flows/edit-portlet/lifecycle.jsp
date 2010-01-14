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
<%@ page import="org.jasig.portal.channel.ChannelLifecycleState,java.util.Set" %>

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
       
      <c:set var="lStates" value="${ lifecycleStates }"/>
      <% Set states = (Set) pageContext.getAttribute("lStates"); %>
      <c:if test="<%= states.contains(ChannelLifecycleState.PUBLISHED) %>">
      <div id="${n}publishingDateSection" style="${ channel.lifecycleState == 'PUBLISHED' || channel.lifecycleState == 'EXPIRED' ? 'display:none;' : '' }">
	      <h3 class="portlet-section-header" role="heading"><spring:message code="lifecycle.publishDateHeading"/></h3>
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
	                <td>
	                   <form:input path="publishDate" size="10" cssClass="cal-datepicker"/>
	                   <span style="${ channel.publishDate == null ? 'display:none' : '' }">
                           <form:select path="publishHour">
                              <c:forEach begin="1" end="12" var="hour">
                                  <form:option value="${ hour }"/>
                              </c:forEach>
                           </form:select>:<form:select path="publishMinute">
                              <c:forEach begin="0" end="59" var="min">
                                  <fmt:formatNumber var="m" value="${ min }" minIntegerDigits="2"/>
                                  <form:option value="${ m }"/>
                              </c:forEach>
                           </form:select>
                           <form:select path="publishAmPm">
                              <form:option value="0" label="AM"/>
                              <form:option value="1" label="PM"/>
                           </form:select>
                           (<a class="clear-date" href="javascript:;"><spring:message code="lifecycle.autoDateReset"/></a>)
                       </span>
	                </td>
	            </tr>  
	          </tbody>
	        </table>
	 
	      </div>
      </div>
      </c:if>

      <c:if test="<%= states.contains(ChannelLifecycleState.EXPIRED) %>">
      <div id="${n}expirationDateSection" style="${ channel.lifecycleState == 'EXPIRED' ? 'display:none;' : '' }">
	      <h3 class="portlet-section-header" role="heading"><spring:message code="lifecycle.expirationDateHeading"/></h3>
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
	                <td class="fl-text-align-right"><spring:message code="lifecycle.expirationDate"/></td>
	                <td>
	                   <form:input path="expirationDate" size="10" cssClass="cal-datepicker"/>
                       <span style="${ channel.expirationDate == null ? 'display:none' : '' }">
		                   <form:select path="expirationHour">
		                      <c:forEach begin="1" end="12" var="hour">
                                  <form:option value="${ hour }"/>
		                      </c:forEach>
		                   </form:select>:<form:select path="expirationMinute">
                              <c:forEach begin="0" end="59" var="min">
                                  <fmt:formatNumber var="m" value="${ min }" minIntegerDigits="2"/>
                                  <form:option value="${ m }"/>
                              </c:forEach>
		                   </form:select>
		                   <form:select path="expirationAmPm">
		                      <form:option value="0" label="AM"/>
                              <form:option value="1" label="PM"/>
		                   </form:select>
                           (<a class="clear-date" href="javascript:;"><spring:message code="lifecycle.autoDateReset"/></a>)
		               </span>
	                </td>
	           </tr>      
	          </tbody>
	        </table>
	 
	      </div>
      </div>
      </c:if>
      
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

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;
    $(document).ready(function(){
        $(".cal-datepicker").datepicker().change(function(){
            if ($(this).val()) $(this).next().css("display", "inline");
            else $(this).next().css("display", "none");
        });
        $(".clear-date").click(function(){ $(this).parent().css("display", "none").prev().val(""); });
        $(":radio").click(function(){
            var lifecycle = $(this).val();
            $('#${n}publishingDateSection').css('display', lifecycle == "PUBLISHED" || lifecycle == "EXPIRED" ? "none" : "block");
            $('#${n}expirationDateSection').css('display', lifecycle == "EXPIRED" ? "none" : "block");
        });
    });
});
</script>