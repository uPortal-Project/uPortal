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
<portlet:actionURL var="ownersUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
    <portlet:param name="_eventId" value="owners"/>
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
        <spring:message code="activityName.permissions.assigned.to.principalName" arguments="${ fn:escapeXml(activityDisplayName) }, ${ principalDisplayName }"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div id="${n}permissionAddingTabs" class="portlet-section" role="region">
        <div class="titlebar">
            <h3 class="title" role="heading"><spring:message code="assignments"/></h3>

                <div id="${n}permissionList">
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
                        
                        <table class="portlet-table" id="${n}permissionsTable" summary="" xmlns:rsf="http://ponder.org.uk" style="width:100%; display:none;">
                            <thead>
                                <tr rsf:id="header:">
                                    <th id="${n}permissionOwner" class="flc-pager-sort-header"><a rsf:id="permissionOwner" title="Click to sort" href="javascript:;"><spring:message code="owner"/></a></th>
                                    <th id="${n}permissionActivity" class="flc-pager-sort-header"><a rsf:id="permissionActivity" title="Click to sort" href="javascript:;"><spring:message code="activity"/></a></th>
                                    <th id="${n}permissionTarget" class="flc-pager-sort-header"><a rsf:id="permissionTarget" title="Click to sort" href="javascript:;"><spring:message code="target"/></a></th>
                                    <th id="${n}permissionEdit" rsf:id="permissionEdit"><spring:message code="edit"/></th>
                                </tr>
                            </thead>
                            <tbody id="${n}permissionsBody">
                                <tr rsf:id="row:">
                                    <td headers="${n}permissionOwner"><span rsf:id="permissionOwner"></span></td>
                                    <td headers="${n}permissionActivity" rsf:id="permissionActivity"></td>
                                    <td headers="${n}permissionTarget"><span rsf:id="permissionTarget"></span></td>
                                    <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                                </tr>
                            </tbody>
                        </table>
                    
                    </div>
                </div>
            </div>

        <a href="${ ownersUrl }">Back to permission owners</a> 


    </div> <!-- end: portlet-section -->

  </div> <!-- end: portlet-content -->

</div> <!-- end: portlet -->


<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;

    var pager;
    var editUrl = "${editUrl}";
    var deleteUrl = "${deleteUrl}";
    var principalUrl = "<c:url value="/api/assignments/principal/${ principal }.json?includeInherited=true"/>";

    var getPermissionAssignments = function(url) {
        var rslt = [];
        $.ajax({
            url: url,
             async: false,
             dataType: "json",
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus + " : " + errorThrown);
             },
             success: function(data) {
                 $(data.assignments).each(function (idx, assignment) {
                     if (assignment.activityKey == '<spring:escapeBody javaScriptEscape="true">${activity}</spring:escapeBody>') {
                         rslt.push(assignment);
                     }
                 });
             }
        });
        return rslt;
    };

    var getBaseComponent = function (row, index) {
        var tree = { };
        if (row.inherited) {
            tree.decorators = [{ type: "addClass", classes: "inherited" }];
        }
        return tree;
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
                         markup += ' <span class="inherited-permission"><spring:message code="inherited"/></span>';
                     }
                     return { markup: markup };
                 }
             },
             { key: "permissionTarget", valuebinding: "*.targetName", sortable: true },
             { key: "permissionEdit", valuebinding: "*.ownerKey",
                 components: {
                     target: editUrl.replace("OWNER", '${"${*.ownerKey}"}')
                                     .replace("ACTIVITY", '${"${*.activityKey}"}')
                                     .replace("TARGET", '${"${*.targetKey}"}'),
                     linktext: "<spring:message code="edit"/>"
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
        var principalOptions, principalPager, principalPermissions;
        
        principalPermissions = getPermissionAssignments(principalUrl);
        if ($(principalPermissions).size() > 0) {
            principalOptions = options;
            principalOptions.dataModel = principalPermissions;
            principalPager = up.fluid.pager("#${n}permissionList", principalOptions);
            $("#${n}permissionList .view-pager").show();
            $("#${n}permissionList .portlet-table").show();
        } else {
            $("#${n}permissionList .no-permissions-message").show();            
        }
        $("#${n}permissionList .permissions-loading-message").hide();

    });
    
});
</script>
