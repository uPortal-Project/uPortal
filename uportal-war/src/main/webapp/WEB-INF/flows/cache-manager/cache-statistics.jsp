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
<div class="fl-widget portlet" role="section">
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading"><spring:message code="cache-statistics.title"/></h2>
    <h3><c:out value="${cacheName}"/></h3>
  </div> <!-- end: portlet-title -->

  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
      
        <table>
            <thead>
                <tr><th><spring:message code="cache-statistics.propertyColumn"/></th><th><spring:message code="cache-statistics.valueColumn"/></th></tr>
            </thead>
            <tbody>
                <tr>
                    <td><spring:message code="cache-statistics.objectCount"/></td><td>${statistics.objectCount}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.cacheHits"/></td><td>${statistics.cacheHits}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.cacheMisses"/></td><td>${statistics.cacheMisses}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.memoryStoreObjectCount"/></td><td>${statistics.memoryStoreObjectCount}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.inMemoryHits"/></td><td>${statistics.inMemoryHits}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.diskStoreObjectCount"/></td><td>${statistics.diskStoreObjectCount}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.onDiskHits"/></td><td>${statistics.onDiskHits}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.evictionCount"/></td><td>${statistics.evictionCount}</td>
                </tr>
                <tr>
                    <td><spring:message code="cache-statistics.statisticsAccuracy"/></td><td>${statistics.statisticsAccuracyDescription}</td>
                </tr>
            </tbody>
        </table>
        
      </div>
    </div>

    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
        <a class="portlet-button portlet-button-primary" href="${ flushUrl }"><spring:message code="cache-statistics.emptyCacheButton"/></a>
        <a class="portlet-button" href="${ homeUrl }"><spring:message code="cache-statistics.cancelButton"/></a>
    </div>
    
  </div>
  
</div>