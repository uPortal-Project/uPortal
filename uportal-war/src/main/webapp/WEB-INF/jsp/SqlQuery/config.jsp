<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="queryUrl"/>

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