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

<portlet:actionURL var="permissionLookupUrl">
    <portlet:param name="execution" value="${ flowExecutionKey }"/>
    <portlet:param name="_eventId" value="lookupPermission"/>
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
<div class="fl-widget portlet prm-mgr view-listperms" role="section">
    
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
      <h2 role="heading" class="title"><spring:message code="permissions.by.category"/></h2>
  </div>
  <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
	
    <div class="permission-lookup">  
        <form id="${n}permissionLookupForm" action="${permissionLookupUrl}" method="POST">
                        
            <label for="${n}principalSuggest"><spring:message code="permission.suggest.principal"/></label>
            <div id="${n}principalSuggest" class="principal-input">
                <input class="up-autocomplete-searchterm" type="text" name="principalDisplayName" value="John" autocomplete="off"/>
                <input type="hidden" name="principal"/>
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
            
            <label for="${n}permissionSuggest"><spring:message code="permission.suggest.permission"/></label>
            <div id="${n}permissionSuggest" class="activity-input">
                <input class="up-autocomplete-searchterm" type="text" name="activityDisplayName" value="<spring:message code="permission"/>" autocomplete="off"/>
                <input type="hidden" name="activity"/>
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
            <span class="punctuation">?</span>
            <input type="submit" value="<spring:message code="show.me"/>"/>
            <span class="permission-lookup-error-container" style="font-size: 13px; color: #dd7615;"></span>
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
                <c:if test="${ split <= ownerStatus.index+1 and ownerStatus.index+1 < split+1 }">
                    </div>
                    <!-- Second column -->
                    <div class="fl-col">
                </c:if>
                
            </c:forEach>
            
            </div>
    
        </div> <!-- end: panel list -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    $(document).ready(function(){

        var submitForm = function(form){
            if (!principalSuggest.getValue() || !permissionSuggest.getValue()) {
                alert('<spring:message code="please.choose.principal.and.permission.from.the.autocomplete.menus" htmlEscape="false" javaScriptEscape="true"/>');
                return false;
            }
            form.principal.value = principalSuggest.getValue();
            form.activity.value = permissionSuggest.getValue();
            return true;
        };

        var principalSuggest = up.Autocomplete(
            "#${n}principalSuggest", 
            {
                initialText: "John",
                searchFunction: function(searchterm) {
                    var principals = [];
                    if (searchterm.length > 2) {
                       $.ajax({
                          url: "<c:url value="/api/permissions/principals.json"/>",
                          data: { q: searchterm },
                          async: false,
                          timeout: '10000',
                          success: function (data) {
                              $(data.groups).each( function (idx, group) {
                                  principals.push({ value: group.principalString, text: group.name || group.keys });
                              });
                              $(data.people).each( function (idx, person) {
                                  principals.push({ value: person.principalString, text: person.name || person.id });
                              });
                          }
                       });
                    }
                    return principals;
                }
            }
        );

        var permissionSuggest = up.Autocomplete(
            "#${n}permissionSuggest", 
            {
                initialText: '<spring:message code="permission" htmlEscape="false" javaScriptEscape="true"/>',
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

        $("#${n}permissionLookupForm").submit(function () {
            var form, errorContainer;
            form = this;
            errorContainer = $(form).find(".permission-lookup-error-container");
            
            if ( principalSuggest.getValue() && permissionSuggest.getValue() ) {
                submitForm(form);
            } else {
                if ( errorContainer.text().length < 1 ) {
                    errorContainer.append("Please choose values from the autocomplete menus.");
                }
                return false;
            }
        });
    });
    
});
</script>

