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
  <%-- TODO: label should come from message bundle --%>
  <p>Edit your favorites:</p>
    <ul class="list-group">

      <c:forEach var="collection" items="${collections}">
        <portlet:actionURL var="unFavoriteCollectionUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${collection.id}" />
        </portlet:actionURL>
        <li class="list-group-item">
          <a href="${unFavoriteCollectionUrl}">
            <span class="glyphicon glyphicon-trash pull-right"></span>${collection.name}
          </a>
        </li>
      </c:forEach>

      <c:forEach var="favorite" items="${favorites}">
        <portlet:actionURL var="unFavoritePortletUrl">
          <portlet:param name="action" value="delete" />
          <portlet:param name="nodeId" value="${favorite.id}" />
        </portlet:actionURL>
        <li class="list-group-item">
          <a href="${unFavoritePortletUrl}">
            <span class="glyphicon glyphicon-trash pull-right"></span>${favorite.name}
          </a>
        </li>
      </c:forEach>
    </ul>

  <span>
    <portlet:renderURL portletMode="VIEW" var="returnToViewModeUrl" />
    <a href="${returnToViewModeUrl}">Stop editing</a>
  </span>

  <span class="pull-right">
    <!-- TODO : Link to marketplace -->
    <a href="#">Browse for Resources</a>
  </span>
</div>
