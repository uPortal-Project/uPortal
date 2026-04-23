<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
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
| (like jQuery and Bootstrap)
| and more, refer to:
| docs/SKINNING_UPORTAL.md
-->

<!-- Portlet -->
<div class="card portlet prm-mgr view-listperms container-fluid" role="section">

  <!-- Portlet Titlebar -->
  <div class="card-header titlebar portlet-titlebar" role="sectionhead">
      <h2 role="heading" class="title"><spring:message code="permissions.by.category"/></h2>
  </div>
  <!-- end: portlet-titlebar -->

  <!-- Portlet Content -->
  <div class="card-body portlet-content row">

    <div class="permission-lookup">
        <form id="${n}permissionLookupForm" class="d-flex flex-wrap align-items-center gap-3" action="${permissionLookupUrl}" method="POST">
            <div id="${n}principalSuggest" class="principal-input">
                <label for="${n}principalSuggest" class="form-label">
                  <spring:message code="permission.suggest.principal"/>
                </label>
                <input class="up-autocomplete-searchterm form-control" type="text" name="principalDisplayName" value="<spring:message code="permission.suggest.principal.value"/>" aria-labelledby="<spring:message code="permission.suggest.principal.label"/>" autocomplete="off"/>
                <input class="form-control" type="hidden" name="principal"/>
                <label for="${n}permissionSuggest" class="form-label">
                  <spring:message code="permission.suggest.permission"/>
                </label>
                <div class="up-autocomplete-dropdown">
                    <div class="up-autocomplete-noresults portlet-msg info alert alert-danger" role="alert">
                        <p><spring:message code="no.matches"/></p>
                    </div>
                    <ul class="up-autocomplete-matches">
                        <li class="up-autocomplete-match group">
                            <a href="#" class="up-autocomplete-match-link" title="&nbsp;">
                                <span class="up-autocomplete-match-text">&nbsp;</span>
                            </a>
                        </li>
                    </ul>
                    <div class="up-autocomplete-loading"><span><spring:message code="loading"/></span></div>
                    <div class="up-autocomplete-close"><a href="#"><spring:message code="close"/></a></div>
                </div>
            </div>
            <div id="${n}permissionSuggest" class="activity-input">
                <input class="up-autocomplete-searchterm form-control" type="text" name="activityDisplayName" value="<spring:message code="permission.suggest.permission.value"/>" aria-labelledby="<spring:message code="permission.suggest.permission.label"/>" autocomplete="off"/>
                <input type="hidden" name="activity"/>
                <span class="punctuation">?</span>
                <input type="submit" class="btn btn-primary" value="<spring:message code="show.me"/>"/>
                <div class="up-autocomplete-dropdown">
                    <div class="up-autocomplete-noresults portlet-msg info alert alert-danger" role="alert">
                        <p><spring:message code="no.matches"/></p>
                    </div>
                    <ul class="up-autocomplete-matches">
                        <li class="up-autocomplete-match group">
                            <a href="#" class="up-autocomplete-match-link" title="&nbsp;">
                                <span class="up-autocomplete-match-text">&nbsp;</span>
                            </a>
                        </li>
                    </ul>
                    <div class="up-autocomplete-loading alert alert-success"><span><spring:message code="loading"/></span></div>
                    <div class="up-autocomplete-close"><a href="#"><spring:message code="close"/></a></div>
                </div>
            </div>
        </form>
        <span class="permission-lookup-error-container" style="font-size: 13px; color: #dd7615;"></span>
    </div>

  	<!-- Card list -->
    <div class="fl-col-flex2 card-list icon-large">

        <!-- 2 column layout -->
        <div class="fl-col float-start">
            <c:set var="numOwners" value="${ fn:length(owners) }" />
            <c:set var="split" value="${ numOwners / 2 }" />
            <c:forEach items="${ owners }" var="owner" varStatus="ownerStatus">
            	<!-- Card -->
                <div class="permission-owner ${ fn:escapeXml(owner.fname )} card">
                	<div class="card-header titlebar">
                        <h2 class="card-title title">
                            <portlet:renderURL var="ownerUrl">
                                <portlet:param name="execution" value="${flowExecutionKey}" />
                                <portlet:param name="_eventId" value="listActivities"/>
                                <portlet:param name="ownerFname" value="${ owner.fname }"/>
                            </portlet:renderURL>
                            <a href="${ ownerUrl }">${ fn:escapeXml(owner.name )}</a>
                        </h2>
                        <h3 class="subtitle">${ fn:escapeXml(owner.description )}</h3>
                    </div>
                    <div class="card-body content">
                        <span class="link-list">
                            <c:forEach items="${ owner.activities }" var="activity" varStatus="status">
                                <portlet:renderURL var="activityUrl">
                                    <portlet:param name="execution" value="${ flowExecutionKey }"/>
                                    <portlet:param name="_eventId" value="showActivity"/>
                                    <portlet:param name="ownerFname" value="${ owner.fname }"/>
                                    <portlet:param name="activityFname" value="${ activity.fname }"/>
                                </portlet:renderURL>
                                <a href="${ activityUrl }">${ fn:escapeXml(activity.name )}</a>${ status.last ? "" : ", " }
                            </c:forEach>
                        </span>
                    </div>
                </div> <!-- end: card -->
                <c:if test="${ split <= ownerStatus.index+1 and ownerStatus.index+1 < split+1 }">
                    </div>
                    <!-- Second column -->
                    <div class="fl-col">
                </c:if>

            </c:forEach>

            </div>

        </div> <!-- end: card list -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->

<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    $(function(){

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
            document.querySelector("#${n}principalSuggest"),
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
            document.querySelector("#${n}permissionSuggest"),
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

        $("#${n}permissionLookupForm").on('submit', function () {
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
