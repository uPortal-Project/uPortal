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

<style type="text/css">
    .up-autocomplete-dropdown {
        display: none; position: absolute; border: 1px solid #AAA; padding: 0px; min-width: 200px; min-height: 100px; z-index: 100}
    /* close */
    .up-autocomplete-dropdown .up-autocomplete-close {padding: 5px 7px; text-align: right; border-bottom: 1px solid #CCC;}
    /* list */
    .up-autocomplete-dropdown .up-autocomplete-matches {max-height: 250px; overflow: auto;}
    .up-autocomplete-dropdown .up-autocomplete-matches,
    .up-autocomplete-dropdown .up-autocomplete-matches li {padding: 0; margin: 0; list-style: none;}
    .up-autocomplete-dropdown .up-autocomplete-matches li {display: block; float:none; padding: 2px 5px;}
    /* anchors */
    .up-autocomplete-dropdown .up-autocomplete-matches a {display: block; padding: 0 0 0 20px;}
    /* loader */
    .up-autocomplete-dropdown .up-autocomplete-loading {position: absolute; top: 0; left: 0; right: 0; bottom: 0;}
    .up-autocomplete-dropdown .up-autocomplete-loading span {visibility: hidden;}
    /* no members message */
    .up-autocomplete-dropdown .up-autocomplete-noresults.info {display: none; margin: 5px 7px;}

    .up-autocomplete-dropdown {background-color: #FFF; -moz-box-shadow: 0px 0px 5px 0px #999; -webkit-box-shadow: 0 0 5px 0 #999; box-shadow: 0px 0px 5px 0px #999;}
    /* close */
    .up-autocomplete-dropdown .up-autocomplete-close {background-color: #F8F8F8;}
    .up-autocomplete-dropdown .up-autocomplete-close a {font-size: 77%;}
    /* list */
    .up-autocomplete-dropdown .up-autocomplete-matches li:hover,
    .up-autocomplete-dropdown .up-autocomplete-matches li:focus {background-color:#FFC;}
    /* anchors */
    .up-autocomplete-dropdown .up-autocomplete-matches a {background: url("/ResourceServingWebapp/rs/famfamfam/silk/1.3/add.png") 0% 50% no-repeat; text-decoration: none;}
    .up-autocomplete-dropdown .up-autocomplete-matches a:hover,
    .up-autocomplete-dropdown .up-autocomplete-matches a:focus {color:#000;}
    /* selected */
    .up-autocomplete-dropdown .up-autocomplete-matches .selected {background-color:#D1F0E0;}
    .up-autocomplete-dropdown .up-autocomplete-matches .selected:hover,
    .up-autocomplete-dropdown .up-autocomplete-matches .selected:focus {background-color:#C8F0DD;}
    .up-autocomplete-dropdown .up-autocomplete-matches .selected a {color: #248222; background-image: url("/ResourceServingWebapp/rs/famfamfam/silk/1.3/delete.png"); font-weight: bold;}
    /* loader */
    .up-autocomplete-dropdown .up-autocomplete-loading {background: #EFEFEF url("../images/loading.gif") 50% 50% no-repeat;}

</style>

<!-- Portlet -->
<div class="fl-widget portlet prm-mgr view-listperms" role="section">
    
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
      <h2 role="heading" class="title"><spring:message code="permissions.by.category"/></h2>
  </div>
  <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
	
    <div class="permission-lookup">  
        <form id="${n}permissionLookupForm">
                        
            <label for="${n}principalSuggest"><spring:message code="permission.suggest.principal"/></label>
            <div id="${n}principalSuggest" style="display:inline">
                <input class="up-autocomplete-searchterm" type="text" value="John"/>
                <div class="up-autocomplete-dropdown">
                    <div class="up-autocomplete-close"><a href="javascript:;">Close</a></div>
                    <div class="up-autocomplete-noresults portlet-msg info" role="alert">
                        <p>No members</p>
                    </div>
                    <ul class="up-autocomplete-matches">
                        <li class="up-autocomplete-match group">
                            <a href="javascript:;" class="up-autocomplete-match-link" title="&nbsp;">
                                <span class="up-autocomplete-match-text">&nbsp;</span>
                            </a>
                        </li>
                    </ul>
                    <div class="up-autocomplete-loading"><span>Loading . . .</span></div>
                </div>
            </div>
            
            <label for="${n}permissionSuggest"><spring:message code="permission.suggest.permission"/></label>
            <div id="${n}permissionSuggest"  style="display:inline">
                <input class="up-autocomplete-searchterm" type="text" value="Permission"/>
                <div class="up-autocomplete-dropdown">
                    <div class="up-autocomplete-close"><a href="javascript:;">Close</a></div>
                    <div class="up-autocomplete-noresults portlet-msg info" role="alert">
                        <p>No members</p>
                    </div>
                    <ul class="up-autocomplete-matches">
                        <li class="up-autocomplete-match group">
                            <a href="javascript:;" class="up-autocomplete-match-link" title="&nbsp;">
                                <span class="up-autocomplete-match-text">&nbsp;</span>
                            </a>
                        </li>
                    </ul>
                    <div class="up-autocomplete-loading"><span>Loading . . .</span></div>
                </div>
            </div>
            <span class="punctuation">?</span>
            <input type="submit" value="<spring:message code="show.me"/>"/>
        </form>
    </div>
  
  	<!-- Panel list -->
    <div class="fl-col-flex2 panel-list icon-large"> 
    
        <!-- 2 column layout -->
        <div class="fl-col fl-force-left">
            <c:set var="numOwners" value="${ fn:length(owners) }" />
            <c:set var="split" value="${ numOwners / 2 }" />
            <c:forEach items="${ owners }" var="owner" varStatus="ownerStatus">
            	<!-- Panel -->
                <div class="permission-owner ${ fn:escapeXml(owner.fname )} panel">
                	<div class="titlebar">
                        <h2 class="title">
                            <portlet:actionURL var="ownerUrl">
                                <portlet:param name="execution" value="${flowExecutionKey}" />
                                <portlet:param name="_eventId" value="listActivities"/>
                                <portlet:param name="ownerFname" value="${ owner.fname }"/>
                            </portlet:actionURL>
                            <a href="${ ownerUrl }">${ fn:escapeXml(owner.name )}</a>
                        </h2>    
                        <h3 class="subtitle">${ fn:escapeXml(owner.description )}</h3>
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
                                <a href="${ activityUrl }">${ fn:escapeXml(activity.name )}</a>${ status.last ? "" : ", " }
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

    $(document).ready(function(){

        var submitForm = function(){
            if (console) {
                console.log("NOT IMPLEMENTED");
                console.log("Selected principal: " + principalSuggest.getValue());
                console.log("Selected permission: " + permissionSuggest.getValue());
            }
            return false;
        };

        var principalSuggest = up.Autocomplete(
            "#${n}principalSuggest", 
            {
                initialText: "John",
                searchFunction: function(searchterm) {
                    var principals = [];
                    $.ajax({
                       url: "<c:url value="/api/permissions/principals.json"/>",
                       data: { q: searchterm },
                       async: false,
                       success: function (data) {
                           $(data.groups).each( function (idx, group) {
                               principals.push({ value: group.principalString, text: group.name || group.keys });
                           });
                           $(data.people).each( function (idx, person) {
                               principals.push({ value: person.principalString, text: person.name || person.id });
                           });
                       }
                    });
                    return principals;
                }
            }
        );

        var permissionSuggest = up.Autocomplete(
            "#${n}permissionSuggest", 
            {
                initialText: "Permission",
                searchFunction: function(searchterm) {
                    var principals = [];
                    $.ajax({
                       url: "<c:url value="/api/permissions/activities.json"/>",
                       data: { q: searchterm },
                       async: false,
                       success: function (data) {
                           $(data.activities).each( function (idx, activity) {
                               principals.push({ value: activity.fname, text: activity.name || activity.fname });
                           });
                       }
                    });
                    return principals;
                }
            }
        );

        $("#${n}permissionLookupForm").submit(submitForm);
    });
    
});
</script>

