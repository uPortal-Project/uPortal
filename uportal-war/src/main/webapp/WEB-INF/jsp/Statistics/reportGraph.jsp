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

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<script type="text/javascript" src="//www.google.com/jsapi"></script>

<rs:resourceURL var="rgbcolorScriptPath" value="/rs/canvg/r144/rgbcolor-r144.min.js"/>
<script type="text/javascript" src="${rgbcolorScriptPath}"></script>

<rs:resourceURL var="canvgScriptPath" value="/rs/canvg/r144/canvg-r144.min.js"/>
<script type="text/javascript" src="${canvgScriptPath}"></script>

<rs:resourceURL var="base64ScriptPath" value="/rs/canvas2image/base64-1.0.min.js"/>
<script type="text/javascript" src="${base64ScriptPath}"></script>

<rs:resourceURL var="canvas2imageScriptPath" value="/rs/canvas2image/canvas2image-0.1.min.js"/>
<script type="text/javascript" src="${canvas2imageScriptPath}"></script>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}" class="portlet-section" role="region">
      <spring:message var="reportNameStr" code="${reportName}"/>
        <!-- hidden div to hold string to use for base report title when report parameters are changed -->
        <div id="${n}_titleUnmodified" style="display: none"><spring:message code="${reportName}"/></div>
        <div id="${n}_titleTemplate" style="display: none"><spring:message code="report.titleWithSingularFields" arguments="${reportNameStr}"/></div>

        <div class="portlet-section-body">
        <div>
            <div id="${n}_chartLinkBar">
                <a id="${n}_downloadCsv">CSV</a>
                <a id="${n}_downloadHtml" target="_blank">HTML</a>
                <a id="${n}_downloadPng" download="${reportName}.png" target="_blank">PNG</a>
                -
                <a id="${n}_editChart" href="#"><spring:message code="edit.chart"/></a>
                -
                <portlet:renderURL var="currentReportUrl">
                    <portlet:param name="report" value="${reportName}"/>
                </portlet:renderURL>
                <a id="${n}_permLink" href="${currentReportUrl}"><spring:message code="perm.link"/></a>
                -
                <portlet:renderURL var="reportListUrl"/>
                <a href="${reportListUrl}"><spring:message code="report.list"/></a>
            </div>
            <p id="${n}_chartLoading" class="portlet-msg-info" style="display: none">
                <spring:message var="loadingStr" code="loading" />
                <img alt="${loadingStr} ..." src="${renderRequest.contextPath}/media/skins/universality/common/images/loading.gif">
                <span> ${loadingStr} ${reportNameStr}</span>
            </p>
            <div id="${n}_chart" class="chart"></div>
        </div>
        
        <form:form id="${n}_reportForm" commandName="reportRequest">
            <p>
                <spring:message code="generate.new.report" arguments="${reportNameStr}"/>
            </p>
            <p>
                <form:label path="start"><spring:message code="start.date"/></form:label>
                <form:input path="start" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="end"><spring:message code="end.date"/></form:label>
                <form:input path="end" cssClass="datepicker"/> 
            </p>
            <p>
                <form:label path="interval"><spring:message code="interval"/></form:label>
                <form:select path="interval">
                    <c:forEach items="${ intervals }" var="interval">
                        <form:option value="${ interval }"/>
                    </c:forEach>
                </form:select> 
            </p>
            <p id="${n}_formError" class="portlet-msg-error" style="display: none"></p>
            <jsp:include page="/WEB-INF/jsp/Statistics/${reportName}_FormFragment.jsp"/>
            <p>
                <form:label path="groups"><spring:message code="groups"/></form:label>
                <form:select path="groups">
                    <c:forEach items="${ groups }" var="group">
                        <form:option value="${ group.id }" label="${group.groupName}"/>
                    </c:forEach>
                </form:select> 
            </p>
            <input type="submit" value="Submit"/>
        </form:form>
      </div>  

    </div>
    
  </div>

</div>

<script type="text/javascript">
<rs:compressJs>
google.load("visualization", "1.0", {
   packages : [ "corechart", "charteditor" ]
});

up.jQuery(function() {
   var $ = up.jQuery;
   var reportDataUrl = '<portlet:resourceURL id="${reportDataResourceId}"/>';
   var reportUrl = '${currentReportUrl}';
   var privateChartWrapper = undefined;
   var resizeInterval = undefined;
   var intervalsCache = {};
   
   // Used by svg->canvas->png conversion
   $.fn.outerHTML = function(s) {
      return s ? this.before(s).remove() : $("<p>").append(
            this.eq(0).clone()).html();
   };

   var getChartSize = function() {
      var width = $("#${n} .portlet-section-body").width();
      return {
         width : width,
         height : Math.max(Math.ceil(width * 0.5625), 240)
      // 16:9 aspect ration, same as HDTV
      };
   };

   var appendParams = function(url, params) {
      if (params.length == 0) {
         return url;
      }
      if (url.indexOf("?") == -1) {
         return url + "?" + params;
      } else {
         return url + "&" + params;
      }
   };

   var buildUrl = function(url, queryString, queryData) {
      url = appendParams(url, queryString);
      if (queryData == undefined) {
         return url;
      }

      var params = $.param(queryData, true);
      return appendParams(url, params);
   };

   var getChartWrapper = function() {
      if (privateChartWrapper == undefined) {
         var chartSize = getChartSize();
         privateChartWrapper = new google.visualization.ChartWrapper({
            chartType : 'LineChart',
            containerId : '${n}_chart',
            options : {
               title : '${reportNameStr}',
               chartArea : {
                  width : '90%',
                  height : '90%'
               },
               legend : {
                  position : 'in'
               },
               axisTitlesPosition : 'in',
               width : chartSize.width,
               height : chartSize.height,
               is3D : true
            }
         });
      }

      return privateChartWrapper;
   };

   var resizeChart = function() {
      if (resizeInterval != undefined) {
         clearInterval(resizeInterval);
      }

      // Resize in an callback to reduce unneeded processing
      resizeInterval = setInterval(function() {
         var chartWrapper = getChartWrapper();

         var chartSize = getChartSize();
         chartWrapper.setOption('height', chartSize.height);
         chartWrapper.setOption('width', chartSize.width);

         chartWrapper.draw();
         clearInterval(resizeInterval);
         resizeInterval = undefined;
      }, 100);
   };

   var editChart = function() {
      var editor = new google.visualization.ChartEditor();
      google.visualization.events.addListener(editor, 'ok', function() {
         $("#${n}_downloadPng").attr('href', '');
         var chartWrapper = editor.getChartWrapper();
         chartWrapper.draw($('#${n}_chart').get(0));
      });

      var chartWrapper = getChartWrapper();
      editor.openDialog(chartWrapper);
   };

   var downloadPng = function(event) {
      var downloadLink = $(this);
      if (downloadLink.attr('href').length > 0) {
         return;
      }

      // Create a temporary canvas for the rendering
      var imageWidth = 1920;
      var imageHeight = 1080;
      var canvas = $(
            '<canvas width="' + imageWidth + '" height="' + imageHeight
                  + '"/>', {
               width : imageWidth,
               height : imageHeight
            }).get(0);

      // Capture the chart's SVG content as a string
      var svgText = $('#${n}_chart svg').outerHTML();

      // Render the SVG to the Canvas
      canvg(canvas, svgText, {
         ignoreDimensions : true,
         scaleWidth : imageWidth,
         scaleHeight : imageHeight,
         renderCallback : function() {
            // Prompt the user to save the PNG
            var imgData = $(Canvas2Image.saveAsPNG(canvas, true)).attr(
                  'src');
            downloadLink.attr('href', imgData);
         }
      });
   };

   var validateIntervals = function() {
      var data = {
         start : $("#${n}_reportForm input[name=start]").val(),
         end : $("#${n}_reportForm input[name=end]").val(),
         interval : $("#${n}_reportForm select[name=interval]").val()
      };

      var cacheKey = $.param(data);
      var intervalInfo = intervalsCache[cacheKey];

      if (intervalInfo == undefined) {
         // Fetch the interval info
         $.ajax({
            url : '<portlet:resourceURL id="intervalCount"/>',
            data : data,
            async : false,
            success : function(data) {
               intervalInfo = data;
            }
         });

         // Cache the result
         intervalsCache[cacheKey] = intervalInfo;
      }

      if (intervalInfo.intervalsBetween > intervalInfo.maxIntervals) {
         $("#${n}_formError").text(
               "There are " + intervalInfo.intervalsBetween + " "
                     + data.interval + " intervals between " + data.start
                     + " and " + data.end
                     + " which is more than the maximum of "
                     + intervalInfo.maxIntervals);
         $("#${n}_formError").show();
         $("#${n}_reportForm input[type=submit]").attr("disabled",
               "disabled");
         return false;
      } else {
         $("#${n}_formError").hide();
         $("#${n}_reportForm input[type=submit]").removeAttr("disabled");
         return true;
      }
   };

   $(window).resize(resizeChart);

   $("#${n} .datepicker").datepicker();
   $("#${n}_reportForm").ajaxForm({
      url : reportDataUrl,
      type : 'GET',
      traditional : true,
      dataType : "json",
      beforeSubmit : function(arr, form, options) {
         if (!validateIntervals()) {
            return false;
         }
         
         $("#${n}_chartLoading").show();

         // Update report download links for the new query
         var queryString = form.serialize();
         $("#${n}_downloadCsv").attr('href', buildUrl(reportDataUrl, queryString, {
            format : "csv"
         }));
         $("#${n}_downloadHtml").attr('href', buildUrl(reportDataUrl, queryString, {
            format : "html"
         }));
         $("#${n}_downloadPng").attr('href', '');
         $("#${n}_permLink").attr('href', buildUrl(reportUrl, queryString));
         
         

         $(document.body).animate({
            'scrollTop' : $('#${n}_chartLinkBar').offset().top
         }, 1000);
      },
      success : function(data) {
         // Render the updated graph
         var table = new google.visualization.DataTable(data.table);

         // Render the report title, or the title with the augmented text based on report parameters
         var tableTitle = $("#${n}_titleUnmodified").text();
         if (data.titleAugmentation) {
             tableTitle = $("#${n}_titleTemplate").text().replace("$0",data.titleAugmentation);
         }

         var chartWrapper = getChartWrapper();
         chartWrapper.setOption('title',tableTitle);
         chartWrapper.setDataTable(table);
         chartWrapper.draw();
         
         $("#${n}_chartLoading").hide();
      },
      error : function() {
         alert("Report Query Failed");
         $("#${n}_chartLoading").hide();
      }
   });

   $("#${n}_editChart").click(function() { editChart(); return false; });
   $("#${n}_downloadPng").mousedown(downloadPng);

   // Add validation listeners
   $("#${n}_reportForm input[name=start]").change(validateIntervals);
   $("#${n}_reportForm input[name=end]").change(validateIntervals);
   $("#${n}_reportForm select[name=interval]").change(validateIntervals);

   // Render the graph once the google libraries have loaded
   google.setOnLoadCallback(function() {
      $("#${n}_reportForm").submit();
   });

});
</rs:compressJs>
</script>
