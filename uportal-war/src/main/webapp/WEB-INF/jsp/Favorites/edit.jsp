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
<div>

  <%-- Flag whether a not-un-favorite-able item is encountered.
  Used to condition display of help text about locked items.
  If the user has no locked favorites, no need to bother the user about the complexities that do not apply. --%>
  <c:set var="lockedItemListed" value="false" />

  <c:if test="${not empty marketplaceFname}">
    <c:set var="marketplaceUrl">${renderRequest.contextPath}/p/${marketplaceFname}/max/render.uP</c:set>
  </c:if>

  <p><spring:message code="favorites.edit" text="Edit your favorites:"/></p>

    <c:if test="${not empty errorMessage}">
      <div class="alert alert-warning alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <c:out value="${errorMessage}" escapeXml="true" />
      </div>
    </c:if>

    <c:if test="${not empty successMessage}">
      <div class="alert alert-success alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <c:out value="${successMessage}" escapeXml="true" />
      </div>
    </c:if>


      
    <ul class="list-group">

      <c:forEach var="collection" items="${collections}">
        <portlet:actionURL var="unFavoriteCollectionUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${collection.id}" />
        </portlet:actionURL>
        <li class="list-group-item">
          <c:choose>
            <c:when test="${collection.deleteAllowed}">
              <a href="${unFavoriteCollectionUrl}">
                <span class="glyphicon glyphicon-trash pull-right"></span>${collection.name}
              </a>
            </c:when>
            <c:otherwise>
              <div data-toggle="tooltip" title="<spring:message
                  code="favorites.lack.permission.to.unfavorite.collection"
                  text="You lack permission to un-favorite this collection."/>">
                <span class="glyphicon glyphicon-lock pull-right"></span>${collection.name}
              </div>
              <c:set var="lockedItemListed" value="true" />
            </c:otherwise>
          </c:choose>
        </li>
      </c:forEach>

      <c:forEach var="favorite" items="${favorites}">
        <portlet:actionURL var="unFavoritePortletUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${favorite.id}" />
        </portlet:actionURL>
        <li class="list-group-item">
          <c:choose>
            <c:when test="${favorite.deleteAllowed}">
              <a href="${unFavoritePortletUrl}">
                <span class="glyphicon glyphicon-trash pull-right"></span>${favorite.name}
              </a>
            </c:when>
            <c:otherwise>
              <div data-toggle="tooltip" title="<spring:message
                  code="favorites.lack.permission.to.unfavorite.portlet"
                  text="You lack permission to un-favorite this portlet."/>" >
                <span class="glyphicon glyphicon-lock pull-right"></span>${favorite.name}
              </div>
              <c:set var="lockedItemListed" value="true" />
            </c:otherwise>
          </c:choose>
        </li>
      </c:forEach>
    </ul>


    <c:if test="${lockedItemListed}">
      <span class="help-block"><spring:message
              code="favorites.lock.icon.legend"
              text="Favorites with the lock icon cannot be un-favorited."/>
      </span>
    </c:if>

  <span>
    <portlet:renderURL portletMode="VIEW" var="returnToViewModeUrl" />
    <%-- TODO: Use a message bundle for this label --%>
    <a href="${returnToViewModeUrl}"><spring:message
            code="favorites.stop.editing"
            text="Stop editing"/></a>
  </span>

  <%-- Display short link to Marketplace if available, suppress otherwise --%>
  <c:if test="${not empty marketplaceUrl}">
    <span class="pull-right">
	  <a href="${marketplaceUrl}">
        <spring:message code="favorites.invitation.to.marketplace.short" text="Visit Marketplace"/>
      </a>
    </span>
  </c:if>
</div>
