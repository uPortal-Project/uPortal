<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}sqlPager" class="portlet-section fl-pager" role="region">

	    <div class="portlet-section-options fl-text-align-right">
	        <div class="view-pager flc-pager-top">
	          <ul id="pager-top" class="fl-pager-ui">
	            <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="listChannels.pagerPrevious"/></a></li>
	            <li>
	              <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
	                <li class="flc-pager-pageLink"><a href="javascript:;">1</a></li>
	                <li class="flc-pager-pageLink-disabled">2</li>
	                <li class="flc-pager-pageLink"><a href="javascript:;">3</a></li>
	              </ul>
	            </li>
	            <li class="flc-pager-next"><a href="#"><spring:message code="listChannels.pagerNext"/> &gt;</a></li>
	            <li>
	              <span class="flc-pager-summary"><spring:message code="listChannels.pagerPerPagePrefix"/></span>
	              <span> <select class="pager-page-size flc-pager-page-size">
	              <option value="5">5</option>
	              <option value="10">10</option>
	              <option value="20">20</option>
	              <option value="50">50</option>
	              </select></span> <spring:message code="listChannels.pagerPerPageSuffix"/>
	            </li>
	          </ul>
	        </div>
        </div>

      <div class="portlet-section-body">
		<table id="${n}sqlResults" summary="" xmlns:rsf="http://ponder.org.uk" style="width:100%;">
		    <thead rsf:id="header:">
		        <tr>
			        <c:forEach items="${ results[0] }" var="cell" varStatus="status">
			            <th id="${n}column${ status.index }" class="flc-pager-sort-header">
			                <a rsf:id="column${ status.index }" href="javascript:;">${ cell.key }</a></th>
			        </c:forEach>
		        </tr>
		    </thead>
		    <tbody>
		        <tr rsf:id="row:">
		            <c:forEach items="${ results[0] }" var="cell" varStatus="status">
		                <td headers="${n}column${ status.index }" rsf:id="column${ status.index }"></td>
		            </c:forEach>
		        </tr>
		    </tbody>
		</table>
      </div>  

    </div>
    
  </div>

</div>

<script type="text/javascript">
 up.jQuery(function() {
    var $ = up.jQuery;
    var fluid = up.fluid;
    var results = [<c:forEach items="${ results }" var="row" varStatus="status">{<c:forEach items="${ row }" var="cell" varStatus="cellStatus">'column${ cellStatus.index }': '<spring:escapeBody javaScriptEscape="true">${ cell.value }</spring:escapeBody>'${ cellStatus.last ? '' : ','}</c:forEach>}${ status.last ? '' : ','}</c:forEach>];

    $(document).ready(function() {
        var options = {
          dataModel: results,
          columnDefs: [
              <c:forEach items="${ results[0] }" var="row" varStatus="status">
              { key: "column${ status.index }", valuebinding: "*.column${ status.index }", sortable: true}${ status.last ? "" : "," }
              </c:forEach>
          ],
          bodyRenderer: {
            type: "fluid.pager.selfRender",
            options: {
                selectors: {
                   root: "#${n}sqlResults"
                },
                row: "row:"
              }
              
          },
          pagerBar: {type: "fluid.pager.pagerBar", options: {
            pageList: {type: "fluid.pager.renderedPageList",
              options: { 
                linkBody: "a"
              }
            }
          }}
      };
      pager = fluid.pager("#${n}sqlPager", options);
    });
 });
</script>