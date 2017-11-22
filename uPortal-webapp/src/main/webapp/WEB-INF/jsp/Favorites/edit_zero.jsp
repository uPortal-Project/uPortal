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

<div id="${n}fav_edit">

  <%-- Display messages even though no favorites because we may have entered or remained in this
       got-no-favorites state through a user action that generated a message. --%>
  <c:if test="${not empty errorMessageCode}">
      <div class="alert alert-warning alert-dismissable">
          <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
          <spring:message
                  code="${errorMessageCode}"
                  text="Un-defined error message."
                  arguments="${nameOfFavoriteActedUpon}"/>
      </div>
  </c:if>

  <c:if test="${not empty successMessageCode}">
      <div class="alert alert-success alert-dismissable">
          <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
          <spring:message
                  code="${successMessageCode}"
                  text="Un-defined success message."
                  arguments="${nameOfFavoriteActedUpon}"/>
      </div>
  </c:if>

  <p><spring:message code="favorites.have.none.to.edit" text="You have no favorites to edit."/></p>

  <%-- Display invitation to access marketplace to find some favorites, if marketplace is available. --%>
  <c:if test="${not empty marketplaceUrl}">
    <p><spring:message code="favorites.invitation.to.marketplace" arguments="${marketplaceUrl}" htmlEscape="false"
          text="You could <a href=`${marketplaceUrl}`>visit the Marketplace</a>to find some new favorites." /></p>
  </c:if>

  <p><spring:message code="favorites.instruction.add.via.contextual.menu"
       text="You can favorite a portlet via the contextual options menu accessed from its title bar."/></p>

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

