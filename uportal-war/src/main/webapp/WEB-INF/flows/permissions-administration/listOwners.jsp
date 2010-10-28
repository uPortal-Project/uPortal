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
<div class="fl-widget portlet prm-mgr view-listperms" role="section">
    
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
      <h2 role="heading" class="title"><spring:message code="permissions.by.category"/></h2>
  </div>
  <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <form id="${n}permissionLookupForm">
    <p>
        Does <input id="${n}principalSuggest" class="target-input multiselect" name="principal"/> have <input id="${n}permissionSuggest" class="target-input multiselect" name="permission"/>?
        <input type="submit" value="<spring:message code="show.me"/>"/>
    </p>
    </form>
  
  	<!-- Panel list -->
    <div class="fl-col-flex2 panel-list icon-large"> 
    
        <!-- 2 column layout -->
        <div class="fl-col fl-force-left">
            <c:set var="numOwners" value="${ fn:length(owners) }" />
            <c:set var="split" value="${ numOwners / 2 }" />
            <c:forEach items="${ owners }" var="owner" varStatus="ownerStatus">
            	<!-- Panel -->
                <div class="permission-owner ${ owner.fname } panel">
                	<div class="titlebar">
                        <h2 class="title">
                            <portlet:actionURL var="ownerUrl">
                                <portlet:param name="execution" value="${flowExecutionKey}" />
                                <portlet:param name="_eventId" value="listActivities"/>
                                <portlet:param name="ownerFname" value="${ owner.fname }"/>
                            </portlet:actionURL>
                            <a href="${ ownerUrl }">${ owner.name }</a>
                        </h2>    
                        <h3 class="subtitle">${ owner.description }</h3>
                    </div>
                    <div class="content">
                        <span class="link-list">
                            <c:forEach items="${ owner.activities }" var="activity" varStatus="status">
                                <portlet:actionURL var="activityUrl">
                                    <portlet:param name="execution" value="${ flowExecutionKey }"/>
                                    <portlet:param name="_eventId" value="showActivity"/>
                                    <portlet:param name="ownerFname" value="${ owner.fname }"/>
                                    <portlet:param name="activityFname" value="${ activity.fname }"/>
                                </portlet:actionURL>
                                <a href="${ activityUrl }">${ activity.name }</a>${ status.last ? "" : ", " }
                            </c:forEach>
                        </span>
                    </div>
                </div> <!-- end: panel -->
                <!-- Second column -->
                <c:if test="${ split <= ownerStatus.index+1 and ownerStatus.index+1 < split+1 }">
                    </div>
                    <div class="fl-col">
                </c:if>
                
            </c:forEach>
    
        </div> <!-- end: panel list -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var submitForm = function(){
        alert("Sorry, I'm not implemented yet! :(");
        return false;
    };

    $(document).ready(function(){
        $("#${n}principalSuggest").tokenInput(
            "<c:url value="/api/permissions/principals.json"/>",
            {
                prePopulate: [],
                tokenLimit: 1,
                onResult: function(results) {
                    var principals = [];
                    $(results.groups).each( function (idx, group) {
                        principals.push({ id: group.principalString, name: group.name || group.keys });
                    });
                    $(results.people).each( function (idx, person) {
                        principals.push({ id: person.principalString, name: person.name || person.id });
                    });
                    return principals;
                }
            } 
        );
        
        $("#${n}permissionSuggest").tokenInput(
            "<c:url value="/api/permissions/activities.json"/>",
            {
                prePopulate: [],
                tokenLimit: 1,
                onResult: function(results) {
                    var activities = [];
                    $(results.activities).each( function (idx, activity) {
                        activities.push({ id: activity.id, name: activity.name || activity.fname });
                    });
                    return activities;
                }
            } 
        );
        $("#${n}permissionLookupForm").submit(submitForm);
    });
    
});
</script>

