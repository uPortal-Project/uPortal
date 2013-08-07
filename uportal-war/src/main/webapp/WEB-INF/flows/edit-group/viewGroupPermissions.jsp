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
<portlet:actionURL var="groupUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="group"/>
</portlet:actionURL>

<portlet:actionURL var="editUrl" escapeXml="false">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
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
        <a href="${ groupUrl }">${ fn:escapeXml(group.name )}</a> > 
        <spring:message code="permissions"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}permissionAddingTabs" class="portlet-section" role="region">
        <div class="titlebar">
            <h3 class="title" role="heading"><spring:message code="assignments"/></h3>

                <div id="${n}assignmentTabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
                    <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
                        <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active">
                            <a href="#${n}principalTab" shape="rect"><span><spring:message code="permissions.for.name" arguments="${group.name}"/></span></a>
                        </li>
                        <li class="ui-state-default ui-corner-top">
                            <a href="#${n}targetTab" shape="rect"><span><spring:message code="permissions.on.name" arguments="${group.name}"/></span></a>
                        </li>
                    </ul>

                    <c:forTokens items="principal,target" delims="," var="token">
                        
                        <div id="${n}${ token }Tab" class="pager-container-${ token } ui-tabs-panel ui-widget-content ui-corner-bottom${ token == 'target' ? ' ui-tabs-hide' : '' }">
                            <div class="fl-col-mixed-200 options">
                                <div class="fl-col-fixed fl-force-left view-filter">
                                <!-- This space left blank for future filtering options... -->
                                </div>
                                <div class="fl-col-flex">
                                    <div class="view-pager flc-pager-top" style="display:none;">
                                        <ul id="pager-top" class="fl-pager-ui">
                                            <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="previous"/></a></li>
                                            <li>
                                                 <ul class="flc-pager-links demo-pager-links" style="margin:0; display:inline">
                                                     <li class="flc-pager-pageLink"><a href="#">1</a></li>
                                                     <li class="flc-pager-pageLink-skip">...</li>
                                                     <li class="flc-pager-pageLink"><a href="#">2</a></li>
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
                                                </select></span> <spring:message code="per.page"/>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="content">
                            
                                <div class="permissions-loading-message portlet-msg-info portlet-msg info" role="status">
                                    <div class="titlebar">
                                    <h3 class="title"><spring:message code="loading"/> . . .</h3>
                                  </div>
                                  <div class="content">
                                      <p><spring:message code="please.wait.while.the.system.finishes.loading.permissions"/></p>
                                  </div>
                                </div>
                
                                <p class="no-permissions-message" style="display:none"><spring:message code="no.group.permissions"/></p>
                                
                                <table class="portlet-table" id="${n}permissionsTable" summary="" xmlns:rsf="http://ponder.org.uk" style="display:none;">
                                    <thead>
                                        <tr rsf:id="header:">
                                            <c:if test="${ token == 'target' }"><th id="${n}permissionPrincipal" class="flc-pager-sort-header"><a rsf:id="permissionPrincipal" title="Click to sort" href="javascript:;"><spring:message code="principal"/></a></th></c:if>
                                            <th id="${n}permissionOwner" class="flc-pager-sort-header"><a rsf:id="permissionOwner" title="Click to sort" href="javascript:;"><spring:message code="owner"/></a></th>
                                            <th id="${n}permissionActivity" class="flc-pager-sort-header"><a rsf:id="permissionActivity" title="Click to sort" href="javascript:;"><spring:message code="activity"/></a></th>
                                            <c:if test="${ token == 'principal' }"><th id="${n}permissionTarget" class="flc-pager-sort-header"><a rsf:id="permissionTarget" title="Click to sort" href="javascript:;"><spring:message code="target"/></a></th></c:if>
                                            <th id="${n}permissionEdit"><span rsf:id="permissionEdit"><spring:message code="edit"/></span></th>				
                                        </tr>
                                    </thead>
                                    <tbody id="${n}permissionsBody">
                                        <tr rsf:id="row:">
                                            <c:if test="${ token == 'target' }"><td headers="${n}permissionPrincipal"><span rsf:id="permissionPrincipal"></span></td></c:if>
                                            <td headers="${n}permissionOwner"><span rsf:id="permissionOwner"></span></td>
                                            <td headers="${n}permissionActivity" rsf:id="permissionActivity"></td>
                                            <c:if test="${ token == 'principal' }"><td headers="${n}permissionTarget"><span rsf:id="permissionTarget"></span></td></c:if>
                                            <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                                        </tr>
                                    </tbody>
                                </table>
                            
                            </div>
                        </div>
                    </c:forTokens>

                </div>
            </div>


    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var pager;
    var editUrl = "${editUrl}";
    var targetUrl = "<c:url value="/api/assignments/target/${ principalString }.json?includeInherited=true"/>";
    var principalUrl = "<c:url value="/api/assignments/principal/${ principalString }.json?includeInherited=true"/>";

    var getPermissionAssignments = function(url) {
        var rslt;
        $.ajax({
            url: url,
             async: false,
             dataType: "json",
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus + " : " + errorThrown);
             },
             success: function(data) {
                rslt = data.assignments;
             }
        });
        return rslt;
    };

    // Initialize the pager
    var options = {
        annotateColumnRange: 'permissionOwner',
        columnDefs: [
             { key: "permissionOwner", valuebinding: "*.ownerName", sortable: true },
             { 
                 key: "permissionActivity", 
                 valuebinding: "*.activityName", 
                 sortable: true,
                 components: function (row, index) {
                     var markup = '<span>${"${*.activityName}"}</span>';
                     if (row.inherited) {
                         markup += ' <span class="inherited-permission">Inherited</span>';
                     }
                     return { markup: markup };
                 }
             },
             { key: "permissionPrincipal", valuebinding: "*.principalName", sortable: true },
             { key: "permissionTarget", valuebinding: "*.targetName", sortable: true },
             { key: "permissionEdit", valuebinding: "*.ownerKey",
                 components: {
                     target: editUrl.replace("OWNER", '${"${*.ownerKey}"}')
                                     .replace("ACTIVITY", '${"${*.activityKey}"}')
                                     .replace("TARGET", '${"${*.targetKey}"}'),
                     linktext: '<spring:message code="edit" htmlEscape="false" javaScriptEscape="true"/>'
                 }
             }
         ],
        bodyRenderer: {
          type: "fluid.pager.selfRender",
          options: {
              selectors: {
                 root: "#${n}permissionsTable"
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

    $(document).ready(function(){
        var principalOptions, targetOptions, principalPager, targetPager, targetPermissions, principalPermissions;
        $("#${n}assignmentTabs").tabs();
        
        
        principalPermissions = getPermissionAssignments(principalUrl);
        if ($(principalPermissions).size() > 0) {
            principalOptions = options;
            principalOptions.dataModel = principalPermissions;
            principalPager = up.fluid.pager(".pager-container-principal", principalOptions);
            $("#${n}principalTab .view-pager").show();
            $("#${n}principalTab .portlet-table").show();
        } else {
            $("#${n}principalTab .no-permissions-message").show();            
        }
        $("#${n}principalTab .permissions-loading-message").hide();

        targetPermissions = getPermissionAssignments(targetUrl);
        if ($(targetPermissions).size() > 0) {
            targetOptions = options;
            targetOptions.dataModel = targetPermissions;
            targetPager = up.fluid.pager(".pager-container-target", targetOptions);
            $("#${n}targetTab .view-pager").show();
            $("#${n}targetTab .portlet-table").show();
        } else {
            $("#${n}targetTab .no-permissions-message").show();            
        }
        $("#${n}targetTab .permissions-loading-message").hide();

    });
    
});
</script>
