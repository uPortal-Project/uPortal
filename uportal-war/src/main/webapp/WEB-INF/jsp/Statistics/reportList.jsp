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

<%@ taglib prefix="gvis" tagdir="/WEB-INF/tags/google-visualization" %>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}" class="portlet-section" role="region">

      <div class="portlet-section-body">
        <h4><spring:message code="available.reports"/></h4>
        <ul>
          <c:forEach var="reportName" items="${reports}">
            <portlet:renderURL var="reportNameUrl" windowState="MAXIMIZED">
              <portlet:param name="report" value="${reportName}"/>
            </portlet:renderURL>
            <li><a href="${reportNameUrl}"><spring:message code="${reportName}"/></a>
          </c:forEach>
        </ul>
      </div>  

    </div>
    
  </div>

</div>
