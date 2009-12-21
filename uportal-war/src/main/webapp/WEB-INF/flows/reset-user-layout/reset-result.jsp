<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<portlet:actionURL var="formUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
        
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">   

        <!-- Portlet Section -->
        <div class="portlet-section" role="region"> 
          <div class="portlet-section-body">
            <p>Layout for <c:out value="${person.attributes['username']}"/> has been reset.</p>
          </div>
        </div> <!-- end: portlet-section -->
        
        <form action="${formUrl}" method="POST">
        <div class="portlet-button-group">
            <input class="portlet-button portlet-button-primary" type="submit" value="Continue" name="_eventId_continue"/>
            
        </div>
        </form>
      </div>
    </div>
  </div>
  
</div>