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
        	<span class="breadcrumb-1"><a href="${ permissionsUrl }">Categories</a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-1"><a href="${ ownerUrl }">Permissions</a></span>
            <span class="separator">&gt; </span>
            <span class="breadcrumb-1"><a href="?">Assignments</a></span>
            <span class="separator">&gt; </span>
        </div>
        <h2 class="title" role="heading">Add an Assignment</h2>
        <h3 class="subtitle">${ activity.description }</h3>
        
        <div class="details">
            <ul class="permission_info">
            <li>
            	<span class="info_label">Category: </span>${ owner.name } 
            </li>
            <li class="last">
            	<span class="info_label">Permission: </span>${ activity.name } 
            </li>
            </ul>
        </div>
    </div>
  
    
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    
            <form id="${n}targetForm" action="${ formUrl }" method="POST">
                <label for="${n}target">Start by selecting a target:</label> 
                <input id="${n}target" class="target-input multiselect" name="target"/>

                <!-- Buttons -->
                <div class="buttons">
                    <input class="button primary" type="submit" value="<spring:message code="editPermission.submitButton"/>" name="_eventId_editPermission"/>
                    <input class="button" type="submit" value="<spring:message code="editPermission.cancelButton"/>" name="_eventId_cancel"/>
                </div> <!-- end: buttons -->
                
            </form>

    
  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var targetOptions = {
        prePopulate: [],
        tokenLimit: 1,
        onResult: function(results) {
            return results.suggestions;
        }
    };

    var submitForm = function(){
        var form = this;
        var target = $("#${n}target").val().split(",")[0];
        $("#${n}target").val(target);
    };

    $(document).ready(function(){
        $("#${n}target").tokenInput("<c:url value="/mvc/permissionsTargetSuggest"/>", targetOptions);
        $("#${n}targetForm").submit(submitForm);
    });
    
});
</script>
