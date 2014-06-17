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

<%-- Portlet Namespace  --%>
<c:set var="n"><portlet:namespace/></c:set>

<%-- Parameters --%>
<portlet:actionURL var="queryUrl">
	<portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<%--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
--%>

<%-- Styles specific to this portlet --%>
<style type="text/css">
  #${n} .portlet-section {
    margin-bottom: 2em;
  }

  #${n} .portlet-section .titlebar {
    margin: 2em 0;
  }

  #${n} .portlet-section .titlebar .title {
    background-color: #f5f6f7;
    border-bottom: 2px solid #eee;
    border-radius: 4px;
    font-weight: 400;
    margin: 0;
    padding: 0.25em 1em;
  }

  #${n} .form-group label {
    color: #000;
  }

  #${n} .buttons {
    border-top: 1px dotted #ccc;
    border-bottom: 1px dotted #ccc;
    margin: 1em 0;
    padding: 1em 0;
  }
</style>
    
<!-- Portlet -->
<div class="fl-widget portlet ptl-mgr view-basicinfo" id="${n}" role="section">

	<!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
  	<h2 class="title" role="heading">
        <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
    
    <form:form modelAttribute="portlet" action="${queryUrl}" method="POST" role="form" class="form-horizontal">
	
	<!-- Portlet Messages -->
    <spring:hasBindErrors name="portlet">
        <!--div class="portlet-msg-error portlet-msg error text-danger" role="alert">
            <form:errors path="*" element="div"/>
        </div--> <!-- end: portlet-msg -->

        <div class="alert alert-danger" role="alert">
          <form:errors path="*" element="div"/>
        </div>
    </spring:hasBindErrors>
		
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
      </div>
      <div class="content">

        <div class="form-group">
          <label for="portletTitle" class="col-sm-2 control-label"><spring:message code="portlet.title"/></label>
          <div class="col-sm-10">
            <form:input path="title" type="text" class="form-control" id="portletTitle"/>  
          </div>
          
        </div>
        <div class="form-group">
          <label for="portletName" class="col-sm-2 control-label"><spring:message code="portlet.name"/></label>
          <div class="col-sm-10">
            <form:input path="name" type="text" class="form-control" id="portletName"/>
          </div>
        </div>
        <div class="form-group">
          <label for="portletFname" class="col-sm-2 control-label"><spring:message code="portlet.functional.name"/></label>
          <div class="col-sm-10">
            <form:input path="fname" type="text" class="form-control" id="portletFname"/>
          </div>
        </div>
        <div class="form-group">
          <label for="portletDescription" class="col-sm-2 control-label"><spring:message code="portlet.description"/></label>
          <div class="col-sm-10">
            <form:input path="description" type="text" class="form-control" id="portletDescription"/>
          </div>
        </div>
        <div class="form-group">
          <label for="portletTimeout" class="col-sm-2 control-label"><spring:message code="portlet.timeout"/></label>
          <div class="col-sm-10">
            <form:input path="timeout" type="text" class="form-control" id="portletTimeout"/>
          </div>
        </div>
        
			</div>
		</div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="controls"/></h3>
      </div>
      <div class="content">
      
        <div class="form-group">
          <label for="portletControls" class="col-sm-2 control-label"><spring:message code="portlet.controls"/></label>
          <div class="col-sm-10">
            <div class="checkbox">
              <label for="hasHelp">
                <form:checkbox path="hasHelp"/>
                <spring:message code="hasHelp"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="editable">
                <form:checkbox path="editable"/>
                <spring:message code="editable"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="configurable">
                <form:checkbox path="configurable"/>
                <spring:message code="configurable"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="hasAbout">
                <form:checkbox path="hasAbout"/>
                <spring:message code="hasAbout"/>
              </label>
            </div>
          </div>
        </div>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Buttons -->
    <div class="buttons form-group">
      <div class="col-sm-10 col-sm-offset-2">
        <c:choose>
          <c:when test="${ completed }">
            <input class="button btn btn-primary" type="submit" value="<spring:message code="review"/>" name="_eventId_review"/>
          </c:when>
          <c:otherwise>
            <input class="button btn btn-primary" type="submit" value="<spring:message code="continue"/>" name="_eventId_next"/>
            <input class="button btn" type="submit" value="<spring:message code="back"/>" name="_eventId_back"/>
          </c:otherwise>
        </c:choose>
        <input class="button btn btn-link" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
      </div>
    </div>
    
    </form:form> <!-- End Form -->
            
	</div> <!-- end: portlet-content -->
        
</div> <!-- end: portlet -->