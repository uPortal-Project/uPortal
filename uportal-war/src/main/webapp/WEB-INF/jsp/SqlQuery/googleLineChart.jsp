<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<script type="text/javascript" src="http://www.google.com/jsapi?key=ABQIAAAA6IxXqpYkVvIBECmLUV99fRT2yXp_ZAY8_ufC3CFXhHIE1NvwkxRKtN-K3WJJ0qPp2xOcWG-RdEe73Q"></script>
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}sqlPager" class="portlet-section" role="region">

      <div class="portlet-section-body">
        <div id="${n}chart"></div>
      </div>  

    </div>
    
  </div>

</div>

<script type="text/javascript">
  google.load("visualization", "1", {packages:["linechart"]});
  google.setOnLoadCallback(drawChart);
  function drawChart() {
    var data = new google.visualization.DataTable();
    <c:forEach items="${ results[0] }" var="cell" varStatus="status">
        data.addColumn('${ status.index == 0 ? "date" : "number" }', '<spring:escapeBody javaScriptEscape="true">${ cell.key }</spring:escapeBody>');
    </c:forEach>
    data.addRows(${ fn:length(results) });
    <c:forEach items="${ results }" var="row" varStatus="status">
        <c:forEach items="${ row }" var="cell" varStatus="cellStatus">
            <c:choose><c:when test="${ cellStatus.index == 0 }">
                data.setValue(${ status.index }, ${ cellStatus.index }, new Date(<fmt:formatDate value="${ cell.value }" pattern="yyyy"/>, <fmt:formatDate value="${ cell.value }" pattern="M"/>-1, <fmt:formatDate value="${ cell.value }" pattern="d"/>));
            </c:when><c:otherwise>
                data.setValue(${ status.index }, ${ cellStatus.index }, ${ cell.value });
            </c:otherwise></c:choose>
        </c:forEach>
    </c:forEach>

    var chart = new google.visualization.LineChart(document.getElementById('${n}chart'));
    chart.draw(data, {width: 400, height: 240, is3D: true });
  }
</script>
