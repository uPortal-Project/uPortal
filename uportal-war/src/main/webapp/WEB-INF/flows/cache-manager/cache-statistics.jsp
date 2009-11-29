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
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Cache Statistics</h2>
    <h3><c:out value="${cacheName}"/></h3>
  </div> <!-- end: portlet-title -->

  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
      
        <table>
            <thead>
                <tr><th>Cache property</th><th>Value</th></tr>
            </thead>
            <tbody>
                <tr>
                    <td>Object count</td><td>${statistics.objectCount}</td>
                </tr>
                <tr>
                    <td>Cache hits</td><td>${statistics.cacheHits}</td>
                </tr>
                <tr>
                    <td>Cache misses</td><td>${statistics.cacheMisses}</td>
                </tr>
                <tr>
                    <td>Dist store object count</td><td>${statistics.diskStoreObjectCount}</td>
                </tr>
                <tr>
                    <td>On disk hits</td><td>${statistics.onDiskHits}</td>
                </tr>
                <tr>
                    <td>Eviction count</td><td>${statistics.evictionCount}</td>
                </tr>
                <tr>
                    <td>In memory hits</td><td>${statistics.inMemoryHits}</td>
                </tr>
                <tr>
                    <td>Memory store object count</td><td>${statistics.memoryStoreObjectCount}</td>
                </tr>
                <tr>
                    <td>Accuracy</td><td>${statistics.statisticsAccuracyDescription}</td>
                </tr>
            </tbody>
        </table>
        
      </div>
    </div>

    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
        <a class="portlet-button portlet-button-primary" href="${ flushUrl }">Flush</a>
        <a class="portlet-button" href="${ homeUrl }">Cancel</a>
    </div>
    
  </div>
  
</div>