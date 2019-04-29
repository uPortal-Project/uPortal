<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<div class="jumbotron">
  <h2 class="text-warning" style="font-size: 200%;">
    <i class="fa fa-exclamation-circle"></i>
    <spring:message code="errorportlet.maintenance.title"/>
  </h2>
  <p>
    <c:choose>
      <c:when test="${customMaintenanceMessage ne null}">
        <c:out value="${customMaintenanceMessage}"/>
      </c:when>
      <c:otherwise>
        <spring:message code="errorportlet.maintenance.text"/>
      </c:otherwise>
    </c:choose>
  </p>
</div>
