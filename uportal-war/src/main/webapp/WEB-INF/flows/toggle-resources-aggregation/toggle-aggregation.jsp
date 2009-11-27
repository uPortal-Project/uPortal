<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<portlet:actionURL var="actionUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Toggle CSS/JavaScript Aggregation</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
        <form action="${actionUrl}" method="POST">
            Resources Aggregation is currently <c:choose>
            <c:when test="${aggregationEnabled}">enabled.</c:when>
            <c:otherwise>disabled.</c:otherwise>
            </c:choose>
            <br/> 
            <c:choose>
            <c:when test="${aggregationEnabled}">
            <input type="hidden" name="newAggregationValue" value="false"/> 
            <div class="portlet-button-group">
               <input class="portlet-button portlet-button-primary" type="submit" value="Disable Aggregation" name="_eventId_disableAggregation"/>
            </div>
            </c:when>
            
            <c:otherwise>
            <input type="hidden" name="newAggregationValue" value="true"/> 
            <div class="portlet-button-group">
               <input class="portlet-button portlet-button-primary" type="submit" value="Enable Aggregation" name="_eventId_enableAggregation"/>
            </div></c:otherwise>
            </c:choose>
            
        </form>
      </div>  
    </div>
    
  </div>
  
</div>