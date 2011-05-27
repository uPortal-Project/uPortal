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

<portlet:actionURL var="formUrl"/>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading"><spring:message code="search"/></h2>
  </div>
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}search" class="portlet-section" role="region">

      <div class="portlet-section-body">

        <form action="${ formUrl }" method="POST">
            <input name="query" value="${ fn:escapeXml(query )}"/> <input type="submit" value="Search"/>
        </form>

        <c:if test="${not empty results}">

            <c:forEach items="${ results.results }" var="type">
                <h2>${ type.key }</h2>
                <ul>
                    <c:forEach items="${ type.value }" var="result">
                        <li>${ result.title }</li>
                    </c:forEach>
                </ul>
            </c:forEach>
        </c:if>

      </div>  

    </div>
    
  </div>

</div>
