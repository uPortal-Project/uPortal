<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="webLoginContainer" class="fl-widget portal-login">
  <div class="fl-widget-inner">
    <div class="fl-widget-content">
        <c:set var="loginTitle"><spring:message code="sign.in.via.cas"/></c:set>
        <c:set var="signIn"><spring:message code="sign.in"/></c:set>
        <c:set var="newUserQuestion"><spring:message code="new.user.question"/></c:set>
        <c:set var="createNewPortalAccount"><spring:message code="create.new.portal.account"/></c:set>
        <div id="portalCASLogin" class="fl-widget-content">
            <a id="portalCASLoginLink" class="btn" title="${loginTitle}" href="${launchUrl}">${signIn}</a>
            <a id="portalCASLoginNewLink" title="${createNewPortalAccount}" href="http://www.jasig.org/cas" class="btn">${newUserQuestion}</a>
        </div>
    </div>
  </div>
</div>