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

<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${not empty marketplaceFname}">
    <c:set var="marketplaceUrl">${renderRequest.contextPath}/p/${marketplaceFname}/max/render.uP</c:set>
</c:if>

<div>
<ul class="list-group">
  <c:forEach var="collection" items="${collections}">
    <li class="list-group-item">
      <span class="glyphicon glyphicon-chevron-right pull-right"></span>
      <a href="${renderRequest.contextPath}/f/${collection.id}/render.uP">${collection.name}</a>
    </li>
  </c:forEach>

  <c:forEach var="favorite" items="${favorites}">
    <li class="list-group-item">
      <span class="glyphicon glyphicon-star pull-right"></span>
      <a href="${renderRequest.contextPath}/p/${favorite.functionalName}/render.uP">${favorite.name}</a>
    </li>
  </c:forEach>

</ul>

  <%-- Display link to Marketplace if available, suppress otherwise --%>
  <c:if test="${not empty marketplaceUrl}">
  <span class="pull-right">
    <a href="${marketplaceUrl}">
      <spring:message code="favorites.invitation.to.marketplace.short" text="Visit Marketplace"/>
    </a>
  </span>
  </c:if>
</div>
