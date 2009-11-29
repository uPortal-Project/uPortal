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
        
        <div class="warning">
        <strong>Warning!</strong>
        <br/>
        Flushing caches while uPortal running will slow performance temporarily.
        Use caution with this feature in production environments. Please be patient,
        this operation may take several moments to complete.
        </div>
        <form action="${flushUrl}" method="POST">
        <div class="portlet-button-group">
        <label for="_eventId_confirm">Are you sure you want to Flush All Caches?</label><br/><br/>
        <input class="portlet-button portlet-button-primary" type="submit" value="Flush All Caches" name="_eventId_confirm"/>
        <input class="portlet-button portlet-button-primary" type="submit" value="Cancel" name="_eventId_cancel"/>
        </div>
        </form>
      </div>
    </div>
  </div>
  
</div>