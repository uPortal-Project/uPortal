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
<portlet:actionURL var="formUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
<portlet:actionURL var="ownersUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="owners"/>
</portlet:actionURL>
<portlet:actionURL var="activitiesUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="activities"/>
</portlet:actionURL>
<portlet:actionURL var="permissionsUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="permissions"/>
</portlet:actionURL>

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
<div class="fl-widget portlet prm-mgr" role="section">
  
<!-- Portlet Titlebar -->
	<div role="sectionhead" class="fl-widget-titlebar titlebar portlet-titlebar">
    	<div class="breadcrumb">
        	<span class="breadcrumb-1"><a href="${ ownersUrl }"><spring:message code="categories"/></a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-2"><a href="${ activitiesUrl }">${ fn:escapeXml(owner.name )}</a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-3"><a href="${ permissionsUrl }">${ fn:escapeXml(activity.name )}</a></span>
            <span class="separator">&gt; </span>
        </div>
        <h2 class="title" role="heading"><spring:message code="add.assignment.to"/> <span class="name">${ fn:escapeXml(activity.name )}</span></h2>
        <h3 class="subtitle">${ fn:escapeXml(activity.description )}</h3>
    </div>
  
    
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-form">
        <form id="${n}targetForm" action="${ formUrl }" method="POST">
            <label for="${n}targetSuggest"><spring:message code="select.target.instruction"/>:</label>
            <div id="${n}targetSuggest" class="target-input">
                <input class="up-autocomplete-searchterm" type="text" name="targetDisplayName" value="<spring:message code="target"/>" autocomplete="off"/>
                <input type="hidden" name="target"/>
                <div class="up-autocomplete-dropdown">
                    <div class="up-autocomplete-noresults portlet-msg info" role="alert">
                        <p><spring:message code="no.matches"/></p>
                    </div>
                    <ul class="up-autocomplete-matches">
                        <li class="up-autocomplete-match group">
                            <a href="javascript:;" class="up-autocomplete-match-link" title="&nbsp;">
                                <span class="up-autocomplete-match-text">&nbsp;</span>
                            </a>
                        </li>
                    </ul>
                    <div class="up-autocomplete-loading"><span><spring:message code="loading"/></span></div>
                    <div class="up-autocomplete-close"><a href="javascript:;"><spring:message code="close"/></a></div>
                </div>
            </div>

            <!-- Buttons -->
            <div class="buttons">
                <input class="button primary" type="submit" value="<spring:message code="submit"/>" name="_eventId_editPermission"/>
                <input class="button" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
            </div> <!-- end: buttons -->
            
        </form>
	</div>
    
  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var submitForm = function(){
        var form = this;
        form.target.value = targetSuggest.getValue();
    };

    var targetSuggest = up.Autocomplete(
            "#${n}targetSuggest", 
            {
                initialText: "<spring:message code="target"/>",
                searchFunction: function(searchterm) {
                    var targets = [];
                    $.ajax({
                       url: "<c:url value="/api/permissions/${activity.id}/targets.json"/>",
                       data: { q: searchterm },
                       async: false,
                       success: function (data) {
                           $(data.targets).each( function (idx, target) {
                               targets.push({ value: target.key, text: target.name || target.key });
                           });
                       }
                    });
                    return targets;
                }
            }
        );

    $(document).ready(function(){
        
        
        $("#${n}targetForm").submit(submitForm);
    });
    
});
</script>
