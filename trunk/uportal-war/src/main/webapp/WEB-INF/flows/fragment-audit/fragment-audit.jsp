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

<c:set var="n"><portlet:namespace/></c:set>
        
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">   

        <p><spring:message code="fragmentAudit.summary"/>:</p>
        
        <div id="${n}error" role="alert" class="portlet-msg-error" style="display: none;">
            <p><strong class="textStatus"></strong></p>
            <p class="errorThrown"></p>
        </div>
        
        <!-- List of Fragments-->
        <div id="${n}fragments"></div>
        
      </div>
    </div>

  </div>
</div>

<div id="${n}template" class="fl-widget" style="display: none;">
    <div class="fl-widget-titlebar">
        <h2><span class="name"></span></h2>
    </div>
    <div class="fl-widget-content" style="padding: 4px;">
        <p>
            <div style="float: right;">
                <strong><spring:message code="fragmentAudit.precedence.label"/>:</strong> <span class="precedence"></span>
            </div>
            <strong><spring:message code="fragmentAudit.owner.label"/>:</strong> <span class="owner"></span>
        </p>
        <p>
            <strong><spring:message code="fragmentAudit.audience.label"/>:</strong>
            <ul class="audience"></ul>
        </p>
        <p>
            <strong><spring:message code="fragmentAudit.portlets.label"/>:</strong>
            <ul class="portlets"></ul>
        </p>
    </div>
</div>

<script type="text/javascript">
    up.jQuery(function() {
        var $ = up.jQuery;

        var options = {
            url: "<c:url value="/api/fragmentList"/>",
            dataType: "json",
            error: function(jqXHR, textStatus, errorThrown) {
                var errorDiv = $("#${n}error");
                errorDiv.find(".textStatus").text(textStatus);
                errorDiv.find(".errorThrown").text(err.description);
                errorDiv.slideDown(1000);
            },
            success: function(data, textStatus, jqXHR) {
                var templateDiv = $("#${n}template");
                $(data.fragments).each(function(index, frag) {
                    var copy = templateDiv.clone();
                    copy.find(".name").text(frag.name);
                    copy.find(".owner").text(frag.ownerId);
                    copy.find(".precedence").text(frag.precedence);
                    $(frag.audience).each(function(index, audience) {
                        copy.find(".audience").append("<li>" + audience + "</li>");
                    });
                    $(frag.portlets).each(function(index, portlet) {
                        copy.find(".portlets").append("<li>" + portlet + "</li>");
                    });
                    $("#${n}fragments").append(copy);
                    copy.show();
                });
            }
        };
        $.ajax(options);
    });
</script>
