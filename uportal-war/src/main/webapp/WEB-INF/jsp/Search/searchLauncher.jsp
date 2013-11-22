<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<portlet:actionURL var="launchUrl" windowState="maximized"></portlet:actionURL>
<portlet:actionURL var="ajaxSearchUrl"/>
<portlet:resourceURL var="ajaxResults" id="retrieveSearchJSONResults"/>

<h1 id="${n}search" class="search-launcher" style="text-align:right;float:right;vertical-align:middle;">
  <div id="webSearchContainer" class="fl-widget">
    <div class="fl-widget-inner">
      <div class="fl-widget-content">
        <c:set var="searchLabel"><spring:message code="search"/></c:set>
        <form method="post" action="${searchLaunchUrl}" id="webSearchForm" class="form-search">
          <input id="webSearchInput"  class="searchInput input-large search-query" value="" name="query" type="text" />
          <input id="webSearchSubmit" type="submit" name="submit" class="btn" value="${searchLabel}" />
            <input class="autocompleteUrl" name="autocompleteUrl" type="hidden" value="${autocompleteUrl}"/>
        </form>
      </div>
    </div>
  </div>
</h1>

    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.10.2/jquery-1.10.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.10.3/jquery-ui-1.10.3.min.js"/>"></script>


    <script language="javascript" type="text/javascript">
      
      var searchJq = jQuery.noConflict();

      
    </script>
    <%@ include file="autosuggest_handler.jsp"%>