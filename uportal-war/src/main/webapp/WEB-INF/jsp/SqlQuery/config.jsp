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

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="queryUrl" escapeXml="false"/>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

    <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">SQL Query Portlet Configuration</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <form:form modelAttribute="form" action="${queryUrl}" method="POST">
    
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="form">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
        
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
    
        <p>
    	    <label class="portlet-form-field-label">SQL query:</label>
	       <form:input path="sqlQuery" size="120"/>
	    </p>
	
	    <p>
    	    <label class="portlet-form-field-label">Spring data source bean name:</label>
	       <form:input path="dataSource" size="50"/>
	    </p>
	
        <p>
	       <label class="portlet-form-field-label">Spring view:</label>
	       <form:input path="viewName" size="50"/>
	    </p>
    
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <input class="portlet-button portlet-button-primary" type="submit" value="Save"/>
    </div>
    
    </form:form> <!-- End Form -->
            
    </div> <!-- end: portlet-body -->
        
</div> <!-- end: portlet -->