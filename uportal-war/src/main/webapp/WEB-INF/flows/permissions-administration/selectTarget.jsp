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
  <div class="fl-widget-titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">
        <a href="${ permissionsUrl }">Permissions</a> > 
        <a href="${ ownerUrl }">${ owner.name }</a> > 
        ${ activity.name }
    </h2>
    <h3 class="subtitle">${ activity.description }</h3>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}permissionAddingTabs" class="portlet-section" role="region">
        <div class="titlebar">
            <h3 class="title" role="heading">Select a new target</h3>   

            <form id="${n}targetForm" action="${ formUrl }" method="POST">
                <label for="${n}target">Target:</label> 
                <input id="${n}target" class="target-input" name="target"/>

                <!-- Buttons -->
                <div class="buttons">
                    <input class="button primary" type="submit" value="<spring:message code="editPermission.submitButton"/>" name="_eventId_editPermission"/>
                    <input class="button" type="submit" value="<spring:message code="editPermission.cancelButton"/>" name="_eventId_cancel"/>
                </div> <!-- end: buttons -->
                
            </form>

    </div> <!-- end: portlet-section -->

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
