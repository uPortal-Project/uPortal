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

<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${not empty marketplaceFname}">
    <c:set var="marketplaceUrl">${renderRequest.contextPath}/p/${marketplaceFname}/max/render.uP</c:set>
</c:if>

<div>
  <p><spring:message code="favorites.have.none" text="You have no favorites."/></p>

  <%-- Display invitation to go favorite some portlets in marketplace if available, suppress otherwise --%>
  <c:if test="${not empty marketplaceUrl}">
    <p><spring:message code="favorites.invitation.to.marketplace" arguments="${marketplaceUrl}" htmlEscape="false"
          text="You can <a href=`${marketplaceUrl}`>visit the Marketplace</a> to find some new favorites." /></p>
  </c:if>

  <p><spring:message code="favorites.instruction.add.via.contextual.menu"
       text="Favorite any portlet via the contextual options menu accessed from its title bar."/></p>

  <%-- Display short link to Marketplace if available, suppress otherwise --%>
  <%-- Included even though redundant with link above, for consistency with UI when user has favorites. --%>
  <c:if test="${not empty marketplaceUrl}">
    <span class="pull-right">
      <a href="${marketplaceUrl}">
        <spring:message code="favorites.invitation.to.marketplace.short" text="Visit Marketplace"/>
      </a>
    </span>
  </c:if>

</div>
