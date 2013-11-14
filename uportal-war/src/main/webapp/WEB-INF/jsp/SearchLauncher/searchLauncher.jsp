<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="webSearchContainer" class="fl-widget col-sm-4">
  <div class="fl-widget-inner">
    
    <div class="fl-widget-content">
	    <c:set var="searchLabel"><spring:message code="search"/></c:set>
        <form class="form-inline" role="form" method="post" action="${searchUrl}" id="webSearchForm">
          <div class="input-group">
            <input id="webSearchInput" value="" name="query" type="text" class="form-control" id="exampleInputEmail2" placeholder="Enter search terms">
            <span class="input-group-btn">
              <button id="webSearchSubmit" type="submit" name="submit" value="${searchLabel}" class="btn btn-default">
                <span>Search</span><i class="icon icon-search"></i></button>
            </span>
          </div>
      </form>
    </div>
  </div>
</div>
