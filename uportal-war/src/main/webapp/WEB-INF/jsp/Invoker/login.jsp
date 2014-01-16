<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="webLoginContainer" class="fl-widget portal-login">
  <div class="fl-widget-inner">
    <div class="fl-widget-content">
        <div id="portalCASLogin" class="fl-widget-content">
            <a id="portalCASLoginLink" class="btn" title="<spring:message code="sign.in.via.cas"/>" href="${casRefUrlEncoder.casLoginUrl}"><spring:message code="sign.in"/></a>
            <a id="portalCASLoginNewLink" title="<spring:message code="create.new.portal.account"/>" href="http://www.jasig.org/cas" class="btn"><spring:message code="new.user.question"/></a>
        </div>
    </div>
  </div>
</div>
