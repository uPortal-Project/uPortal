<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:set var="logOffAndExit"><spring:message code="log.off.and.exit"/></c:set>
<c:set var="signOut"><spring:message code="sign.out"/></c:set>
<a href="${launchUrl}" title="${logOffAndExit}" class="btn up-portlet-control hide-content portal-logout">${signOut}</a>
