<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:set var="idSwapped"><spring:message code="you.are.idswapped.as"/></c:set>
<c:set var="signedIn"><spring:message code="you.are.signed.in.as"/></c:set>

<c:choose>
  <c:when test="${userImpersonating} = 'true'">
    <span class="user-name">${idSwapped}&nbsp;${displayName}&nbsp;</span>
  </c:when>
  <c:otherwise>
    <span class="user-name">${signedIn}&nbsp;${displayName}&nbsp;</span>
  </c:otherwise>
</c:choose>
