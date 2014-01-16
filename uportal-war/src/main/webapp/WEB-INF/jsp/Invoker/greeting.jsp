<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:set var="greeting">
    <c:choose>
        <c:when test="${userInfo['impersonating'] eq 'true'}"><spring:message code="you.are.idswapped.as"/></c:when>
        <c:otherwise><spring:message code="you.are.signed.in.as"/></c:otherwise>
    </c:choose>
</c:set>

<span class="user-name">${greeting} ${userInfo['displayName']}</span>
