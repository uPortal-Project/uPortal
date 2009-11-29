<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<portlet:actionURL var="flushUrl">
  <portlet:param name="_eventId" value="flush"/>
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<portlet:actionURL var="homeUrl">
  <portlet:param name="_eventId" value="home"/>
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
        
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
      <h3>Cache Statistics for <c:out value="${cacheName}"/></h3>
        <ul>
        <li>Object count: <c:out value="${statistics.objectCount }"/></li>
        <li>Cache hits: <c:out value="${statistics.cacheHits }"/></li>
        <li>Cache misses: <c:out value="${statistics.cacheMisses }"/></li>
        <li>Disk store object count: <c:out value="${statistics.diskStoreObjectCount }"/></li>
        <li>On disk hits: <c:out value="${statistics.onDiskHits }"/></li>
        <li>Eviction count: <c:out value="${statistics.evictionCount }"/></li>
        <li>In memory hits: <c:out value="${statistics.inMemoryHits }"/></li>
        <li>Memory store object count: <c:out value="${statistics.memoryStoreObjectCount }"/></li>
        <li>Accuracy: <c:out value="${statistics.statisticsAccuracyDescription }"/></li>
        </ul>   
        <a href="${flushUrl}">Flush this cache</a>, or <a href="${homeUrl}">Select a different cache</a>.
      </div>
    </div>
  </div>
  
</div>