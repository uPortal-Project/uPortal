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
<div class="fl-widget portlet ptl-mgr view-chooseportlet" role="section">

    <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">
       <spring:message code="register.new.portlet"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
    
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="portlet">
        <div class="portlet-msg-error portlet-msg error" role="alert">
            <form:errors path="*" element="div" />
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
    
    <form action="${queryUrl}" method="POST">
        
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    	<div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
      </div>
      <div class="content">

        <select name="application">
            <c:forEach items="${contexts}" var="context">
                <option value="${fn:escapeXml(context.name)}">${fn:escapeXml(context.name)}</option>
            </c:forEach>
        </select>
        
        <select name="portlet">
            <c:forEach items="${contexts[0].portlets}" var="portlet">
                <option value="${fn:escapeXml(portlet.portletName)}">${fn:escapeXml(fn:length(portlet.displayNames) > 0 ? portlet.displayNames[0].displayName : portlet.portletName)}</option>
            </c:forEach>
        </select>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Buttons -->
    <div class="buttons">
      <c:choose>
        <c:when test="${ completed }">
          <input class="button primary" type="submit" value="<spring:message code="review"/>" name="_eventId_review"/>
        </c:when>
        <c:otherwise>
          <input class="button primary" type="submit" value="<spring:message code="continue"/>" name="_eventId_next"/>
          <input class="button" type="submit" value="<spring:message code="back"/>" name="_eventId_back"/>
        </c:otherwise>
      </c:choose>
      <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
    </div>
    
    </form> <!-- End Form -->
            
    </div> <!-- end: portlet-content -->
    
    <script type="text/javascript">
	    up.jQuery(function() {
	        var $ = up.jQuery;
	        var portlets = {};
	        <c:forEach items="${contexts}" var="context">
	            portlets['${context.name}'] = [<c:forEach items="${context.portlets}" var="portlet" varStatus="status">{name: '${portlet.portletName}', title: '${fn:length(portlet.displayNames) > 0 ? portlet.displayNames[0].displayName : portlet.portletName}'}${status.last ? '' : ','}</c:forEach>];
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