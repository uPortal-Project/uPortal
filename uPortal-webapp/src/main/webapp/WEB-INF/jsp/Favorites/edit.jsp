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
<div id="${n}fav_edit">

  <%-- Flag whether a not-unfavorite-able item is encountered.
  Used to condition display of help text about locked items.
  If the user has no locked favorites, no need to bother the user about the complexities that do not apply. --%>
  <c:set var="lockedItemListed" value="false" />

  <c:if test="${not empty marketplaceFname}">
    <c:set var="marketplaceUrl">${renderRequest.contextPath}/p/${marketplaceFname}/max/render.uP</c:set>
  </c:if>

  <p><spring:message code="favorites.edit" text="Edit your favorites:"/></p>

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

    <ul class="list-group sortable-collections">

      <c:forEach var="collection" items="${collections}">
        <portlet:actionURL var="unFavoriteCollectionUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${collection.id}" />
        </portlet:actionURL>
        <li class="list-group-item ${collection.moveAllowed ? '' : 'sort-disabled'}" sourceID="${collection.id}">
          <c:if test="${collection.moveAllowed}">
            <span class="glyphicon glyphicon-sort pull-left" style='padding-right: 1em; cursor: move;'></span>
          </c:if>
          <c:choose>
            <c:when test="${collection.deleteAllowed}">
              <a class="up-remove-favorite" data-href="${unFavoriteCollectionUrl}" href="javascript:void(0)">
                <span class="glyphicon glyphicon-trash pull-right" aria-label="Remove favorite collection"></span>
              </a>
              <a href="${renderRequest.contextPath}/f/${collection.id}/render.uP">
                ${collection.name}
              </a>
            </c:when>
            <c:otherwise>
              <div data-toggle="tooltip" title="<spring:message
                  code="favorites.lack.permission.to.unfavorite.collection"
                  text="You lack permission to unfavorite this collection."/>">
                <span class="glyphicon glyphicon-lock pull-right"></span>${collection.name}
              </div>
              <c:set var="lockedItemListed" value="true" />
            </c:otherwise>
          </c:choose>
        </li>
      </c:forEach>
    </ul>
    <ul class="list-group sortable-portlet">
      <c:forEach var="favorite" items="${favorites}">
        <portlet:actionURL var="unFavoritePortletUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${favorite.id}" />
        </portlet:actionURL>
        <li class="list-group-item ${favorite.moveAllowed ? '' : 'sort-disabled'}" sourceID="${favorite.id}">
          <c:if test="${favorite.moveAllowed}">
            <%-- That sort glyph may be shown when only one item and so nothing to re-order against is
                 deliberate, meant to communicate to the user that if he or she had more items they would then
                 be re-order-able, and meant to provide UI consistency in the one and more-than-one items cases. --%>
            <span class="glyphicon glyphicon-sort pull-left" style='padding-right: 1em; cursor: move;'></span>
          </c:if>
          <c:choose>
            <c:when test="${favorite.deleteAllowed}">
              <a class="up-remove-favorite" data-href="${unFavoritePortletUrl}" href="javascript:void(0)">
                <span class="glyphicon glyphicon-trash pull-right" aria-label="Remove favorite"></span>
              </a>
              <a href="${renderRequest.contextPath}/p/${favorite.functionalName}/render.uP">
                ${favorite.name}
              </a>
            </c:when>
            <c:otherwise>
              <div data-toggle="tooltip" title="<spring:message
                  code="favorites.lack.permission.to.unfavorite.portlet"
                  text="You lack permission to unfavorite this portlet."/>" >
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
              text="Favorites with the lock icon cannot be unfavorited."/>
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

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;
    $(document).ready( function () {
        $('#${n}fav_edit .sortable-collections').sortable({
            deactivate: function(event, ui) {
                up.moveStuff('Tab', ui.item[0], '${renderRequest.contextPath}');
            }
        });
        $('#${n}fav_edit .sortable-portlet').sortable({
            deactivate: function(event, ui) {
                up.moveStuff('Portlet',ui.item[0], '${renderRequest.contextPath}');
            }
        });
        $('#${n}fav_edit li').disableSelection();

        // Removing a favorite requires an actionURL and a POST...
        $('#${n}fav_edit a.up-remove-favorite').click(function() {
            var url = $(this).attr('data-href');
            var form = $('<form />', {
                action: url,
                method: 'POST',
                style: 'display: none;'
            });
            form.appendTo('body').submit();
        });
    });
});
</script>


