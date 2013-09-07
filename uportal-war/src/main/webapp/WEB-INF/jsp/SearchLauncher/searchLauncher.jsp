<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="webSearchContainer" class="fl-widget">
  <div class="fl-widget-inner">
    
    <div class="fl-widget-content">
	    <c:set var="searchLabel"><spring:message code="search"/></c:set>
        <form method="post" action="${launchUrl}" id="webSearchForm">
          <input id="webSearchInput" value="" name="query" type="text" />
          <input id="webSearchSubmit" type="submit" name="submit" class="btn" value="${searchLabel}" />
        </form>
    </div>
  </div>
</div>
