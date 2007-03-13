<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

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
