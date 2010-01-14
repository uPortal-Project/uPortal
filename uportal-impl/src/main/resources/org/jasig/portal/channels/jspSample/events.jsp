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

<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table cellpadding="0" cellspacing="0" border="0" width="100%">
 <tr><td valign="top"><span class="uportal-channel-subtitle">Channel Events</span></td></tr>
 <tr><td><img src="/media/org/jasig/portal/channels/jsp/tree/trnsPoint.gif" height="5" width="1"/></td></tr>
<c:choose>
  <c:when test="${! empty requestScope.events}">
    <c:forEach items="${requestScope.events}" var="event">
      <tr><td valign="top"><span class="uportal-channel-text" id="event_txt"><c:out value="${event}"/></span></td></tr>
      <tr><td><img src="/media/org/jasig/portal/channels/jsp/tree/trnsPoint.gif" height="5" width="1"/></td></tr>
    </c:forEach>
  </c:when>
  <c:otherwise>
    <tr><td valign="top"><span class="uportal-channel-text" id="event_txt"><b>There are no recorded channel events.</b></span></td></tr>
    <tr><td><img src="/media/org/jasig/portal/channels/jsp/tree/trnsPoint.gif" height="5" width="1"/></td></tr>
  </c:otherwise>
</c:choose>
</table>
