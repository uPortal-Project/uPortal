<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:set var="idSwapped"><spring:message code="you.are.idswapped.as"/></c:set>
<c:set var="signedIn"><spring:message code="you.are.signed.in.as"/></c:set>

<c:choose>
  <c:when test="${userImpersonating} = 'true'">
    <div id="portalWelcome"><span class="user-name">${idSwapped}&nbsp;${displayName}</span></div>
  </c:when>
  <c:otherwise>
    <div id="portalWelcome"><span class="user-name">${signedIn}&nbsp;${displayName}</span></div>
  </c:otherwise>
</c:choose>
