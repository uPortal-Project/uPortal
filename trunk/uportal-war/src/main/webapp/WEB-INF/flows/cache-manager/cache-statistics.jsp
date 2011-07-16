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
<portlet:actionURL var="flushUrl">
  <portlet:param name="_eventId" value="flush"/>
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<portlet:actionURL var="homeUrl">
  <portlet:param name="_eventId" value="cache-list"/>
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
        
<!-- Portlet -->
<div class="fl-widget portlet cache-mgr view-statistics" role="section">
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="cache.statistics"/></h2>
    <h3 class="subtitle">${fn:escapeXml(cacheName)}</h3>
  </div> <!-- end: portlet-titlebar -->

  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
      
        <table class="portlet-table">
            <thead>
                <tr><th><spring:message code="cache.property"/></th><th><spring:message code="value"/></th></tr>
            </thead>
            <tbody>
                <tr>
                    <td><spring:message code="object.count"/></td><td>${fn:escapeXml(statistics.objectCount)}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache.hits"/></td><td>${fn:escapeXml(statistics.cacheHits)}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache.misses"/></td><td>${fn:escapeXml(statistics.cacheMisses)}</td>
                </tr>
                <tr>
                    <td><spring:message code="memory.store.object.count"/></td><td>${fn:escapeXml(statistics.memoryStoreObjectCount)}</td>
                </tr>
                <tr>
                    <td><spring:message code="in.memory.hits"/></td><td>${fn:escapeXml(statistics.inMemoryHits)}</td>
                </tr>
                <tr>
                    <td><spring:message code="disk.store.object.count"/></td><td>${fn:escapeXml(statistics.diskStoreObjectCount)}</td>
                </tr>
                <tr>
                    <td><spring:message code="on.disk.hits"/></td><td>${fn:escapeXml(statistics.onDiskHits)}</td>
                </tr>
                <tr>
                    <td><spring:message code="eviction.count"/></td><td>${fn:escapeXml(statistics.evictionCount)}</td>
                </tr>
                <tr>
                    <td><spring:message code="statistics.accuracy"/></td><td>${fn:escapeXml(statistics.statisticsAccuracyDescription)}</td>
                </tr>
            </tbody>
        </table>
        
      </div>
    </div>

    <!-- Portlet Buttons -->
    <div class="buttons">
        <a class="button primary" href="${ flushUrl }"><spring:message code="empty.cache"/></a>
        <a class="button" href="${ homeUrl }"><spring:message code="cancel"/></a>
    </div>
    
  </div>
  
</div>