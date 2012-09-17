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
    <div id="${n}" class="portlet-section" role="region">

      <div class="portlet-section-body">
      
        <form:form commandName="loginReportRequest">
            <p>
                Generate a new login report:
            </p>
            <p>
                <form:label path="start" code="start"/>
                <form:input path="start" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="end" code="end"/>
                <form:input path="end" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="interval" code="Interval"/>
                <form:select path="interval">
                    <c:forEach items="${ intervals }" var="interval">
                        <form:option value="${ interval }"/>
                    </c:forEach>
                </form:select> 
            </p>
            <p>
                <form:label path="uniqueOnly" code="Unique"/>
                <form:select path="uniqueOnly">
                    <form:option value="true" label="unique.users"/>
                    <form:option value="false" label="total.sessions"/> 
                </form:select>
            </p>
            <p>
                <form:label path="groups" code="Group"/>
                <form:select path="groups">
                    <c:forEach items="${ groups }" var="group">
                        <form:option value="${ group.key }" label="${group.value}"/>
                    </c:forEach>
                </form:select> 
            </p>
            <input type="submit" value="Submit"/>
        </form:form>
      
        <div class="chart"></div>
      </div>  

    </div>
    
  </div>

</div>

<script type="text/javascript">

    var $ = up.jQuery;

    var drawChart = function() {
        var queryData = {
            start: $("#${n} input[name=start]").val(),
            end: $("#${n} input[name=end]").val(),
            interval: $("#${n} select[name=interval]").val(),
            uniqueOnly: $("#${n} select[name=uniqueOnly]").val()
        };
        
        var groups = $("#${n} select[name=groups]");
        if (groups.length > 0) {
            queryData.groups = groups.val();
        }
        
        $.ajax({
            url: "<portlet:resourceURL/>",
            traditional: true,
            data: queryData, 
            success: function (data) { 
                var chart = new google.visualization.LineChart($('#${n} .chart').get(0));
                var table = new google.visualization.DataTable(data.logins);
                
                var width = $("#${n} .portlet-section-body").width();
                var height = Math.max(width * .7, 240);
                chart.draw(table, {width: width, height: height, is3D: true });
            }, 
            dataType: "json"
        });
    };

    $(document).ready(function(){
        $(".datepicker").datepicker();
        $("#${n} form").submit(function () {
            drawChart();
            return false;
        });
    });

    google.load("visualization", "1.0", {packages:["corechart"]});
    google.setOnLoadCallback(drawChart);

</script>
