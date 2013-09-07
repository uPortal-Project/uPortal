<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div class="fl-widget-inner">
    <div class="fl-widget-content">
        <c:set var="loginTitle"><spring:message code="sign.in.via.cas"/></c:set>
        <c:set var="signIn"><spring:message code="sign.in"/></c:set>
        <c:set var="newUserQuestion"><spring:message code="new.user.question"/></c:set>
        <c:set var="createNewPortalAccount"><spring:message code="create.new.portal.account"/></c:set>
        <div id="portalCASLogin" class="fl-widget-content">
            <a id="portalCASLoginLink" class="button" title="${loginTitle}" href="${launchUrl}">
            <span class="label">${signIn}</span>
            </a>
            <p><a id="portalCASLoginNewLink" title="${createNewPortalAccount}" href="http://www.jasig.org/cas"><span class="label">${newUserQuestion}</span></a></p>
        </div>
    </div>
  </div>
</div>