<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ taglib prefix="gvis" tagdir="/WEB-INF/tags/google-visualization" %>
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
  google.load("visualization", "1", {packages:["columnchart"]});
  google.setOnLoadCallback(drawChart);
  function drawChart() {
    var data = new google.visualization.DataTable();
    <c:forEach items="${ results[0] }" var="cell" varStatus="status">
        data.addColumn('<gvis:dataType value="${ cell.value }"/>', '<spring:escapeBody javaScriptEscape="true">${ cell.key }</spring:escapeBody>');
    </c:forEach>
    data.addRows(${ fn:length(results) });
    <c:forEach items="${ results }" var="row" varStatus="status">
        <c:forEach items="${ row }" var="cell" varStatus="cellStatus">
            data.setValue(${ status.index }, ${ cellStatus.index }, <gvis:formatValue value="${ cell.value }"/>);
        </c:forEach>
	</c:forEach>

    var chart = new google.visualization.ColumnChart(document.getElementById('${n}chart'));
    chart.draw(data, {width: 400, height: 240, is3D: true });
  }
</script>
