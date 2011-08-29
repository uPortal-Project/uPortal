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

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<!-- START: VALUES BEING PASSED FROM BACKEND -->
<!-- END: VALUES BEING PASSED FROM BACKEND -->

<c:set var="n"><portlet:namespace/></c:set>

<!--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
-->
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
<form id="${n}form">
  
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
  	<h2 role="heading"><spring:message code="most.frequently.added"/></h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Toolbar -->
  <div class="portlet-toolbar" role="toolbar">
    <spring:message code="previous"/>
    <select id="${n}days" name="days">
      <option value="1">1</option>
      <option value="7">7</option>
      <option value="30" selected="selected">30</option>
      <option value="90">90</option>
      <option value="365">365</option>
    </select>
    <spring:message code="days"/>
    <c:if test="${showAdminFeatures}">
      <spring:message code="from"/>
      <input id="${n}fromDate" name="fromDate" type="text" class="cal-datepicker" value="<spring:message code="today"/>"/>
      <spring:message code="inclusive"/>
    </c:if>
  </div> <!-- end: portlet-toolbar -->
        
	<!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
  	<!-- Portlet Messages -->
  	<div class="portlet-msg-info" role="status" id="${n}loadingMessage">
    	<h3><spring:message code="loading.information"/></h3>
    	<p><spring:message code="please.wait.while.the.system.finishes.loading.the.requested.data"/></p>
    </div> <!-- end: portlet-msg -->
    
    <!-- Portlet Section -->
    <div id="${n}popularPortlets" class="portlet-section fl-pager" role="region">      
      <!-- Portlet Section Options -->
      <div class="view-pager flc-pager-top portlet-section-options">
        <ul id="pager-top" class="fl-pager-ui">
          <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="previous"/></a></li>
          <li>
            <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
              <li class="flc-pager-pageLink"><a href="javascript:;">1</a></li>
              <li class="flc-pager-pageLink-disabled">2</li>
              <li class="flc-pager-pageLink"><a href="javascript:;">3</a></li>
            </ul>
          </li>
          <li class="flc-pager-next"><a href="#"><spring:message code="next"/> &gt;</a></li>
          <li>
            <span class="flc-pager-summary"><spring:message code="show"/></span>
            <span> <select class="pager-page-size flc-pager-page-size">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
            </select></span> <spring:message code="per.page"/>
          </li>
        </ul>
      </div><!-- end: portlet-section-options -->

      <div class="portlet-section-body">
        <table id="${n}portletsTable" summary="" xmlns:rsf="http://ponder.org.uk" style="width:100%;">
          <thead>
            <tr rsf:id="header:">
              <th id="${n}portletTitle" class="flc-pager-sort-header"><a rsf:id="title" title="Click to sort" href="javascript:;"><spring:message code="title"/></a></th>
              <th id="${n}portletCount" class="flc-pager-sort-header"><a rsf:id="count" title="Click to sort" href="javascript:;"><spring:message code="number.times"/></a></th>
            </tr>
          </thead>
          <tbody id="${n}portletsBody">
            <tr rsf:id="row:">
              <td headers="${n}portletTitle" rsf:id="title"></td>
              <td headers="${n}portletCount" rsf:id="count"></td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div id="${n}noneAdded" style="display: none;">
        <p><spring:message code="no.apps.have.been.added.by.users.in.the.specified.time.period"/></p>
      </div> 

      <c:if test="${showAdminFeatures}">
        <!-- Portlet Buttons -->
        <div class="portlet-button-group">
          <portlet:actionURL var="doneUrl">
            <portlet:param name="execution" value="${flowExecutionKey}" />
            <portlet:param name="_eventId" value="done" />
          </portlet:actionURL>
          <a class="portlet-button portlet-button-primary" href="${doneUrl}"><spring:message code="done"/></a>
        </div>
      </c:if>
    </div> <!-- end: portlet-section -->
    
  </div> <!-- end: portlet-body -->

</form>
</div> <!-- end: portlet -->
    	
<script type="text/javascript">
up.jQuery(function() {

    var $ = up.jQuery;
    var portletDeepLinkUrl = '<c:url value="/render.userLayoutRootNode.uP?uP_fname=PORTLETFNAME"/>';
    var pager;

    var fetchStats = function() {
        var counts;

        $("#${n}loadingMessage").slideDown(500);

        $.ajax({
            url: '<c:url value="/api/userLayoutModificationsCounts"/>',
            async: false,
            data: $("#${n}form").serialize(),
            type: 'POST',
            dataType: "json",
            success: function(data) { 
                counts = data.counts; 
            },
            error: function(request, textStatus, error) {
                alert("ERROR:  " + textStatus); 
            }
        });
        
        $("#${n}loadingMessage").slideUp(1000);
        
        if (counts) {
            if (counts.length > 0) {
                $("#${n}noneAdded").hide();
            } else {
                $("#${n}noneAdded").show();
            }
        }

        return counts;
    }

    var updateTable = function() {
        var newPortlets = fetchStats();
        var newModel = up.fluid.copy(pager.model);
        newModel.totalRange = newPortlets.length;
        newModel.pageIndex = 0;
        newModel.pageCount = Math.max(1, Math.floor((newModel.totalRange - 1)/ newModel.pageSize) + 1);
        up.fluid.clear(pager.options.dataModel);
        up.fluid.model.copyModel(pager.options.dataModel, newPortlets);
        pager.permutation = undefined;
        pager.events.onModelChange.fire(newModel, pager.model, pager);
        up.fluid.model.copyModel(pager.model, newModel)
    }

    var options = {
        dataModel: fetchStats(),
        annotateColumnRange: 'title',
        columnDefs: [
            { key: "title", valuebinding: "*.portletTitle", sortable: true,
                components: function(row, index) {
                    return {
                        markup: '<a href="' + portletDeepLinkUrl.replace("PORTLETFNAME", '${"${*.portletFName}"}') + '" title="\${*.portletDescription}">\${*.portletTitle}</a>'
                    }
                }
            },
            { key: "count", valuebinding: "*.count", sortable: true }
        ],
        bodyRenderer: {
          type: "fluid.pager.selfRender",
          options: {
              selectors: {
                 root: "#${n}portletsTable"
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
    pager = up.fluid.pager("#${n}popularPortlets", options);

    $("#${n}days").change(updateTable);
    $("#${n}fromDate").change(updateTable);

    $(".cal-datepicker").datepicker();

});
</script>
