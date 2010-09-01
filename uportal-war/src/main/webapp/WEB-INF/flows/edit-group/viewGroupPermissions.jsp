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

<portlet:actionURL var="createUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="createPermission"/>
</portlet:actionURL>
<portlet:actionURL var="editUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="editPermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
</portlet:actionURL>
<portlet:actionURL var="deleteUrl">
  <portlet:param name="execution" value="${flowExecutionKey}" />
  <portlet:param name="_eventId" value="deletePermission"/>
  <portlet:param name="owner" value="OWNER"/>
  <portlet:param name="principalType" value="PRINCIPALTYPE"/>
  <portlet:param name="principalName" value="PRINCIPALNAME"/>
  <portlet:param name="activity" value="ACTIVITY"/>
  <portlet:param name="target" value="TARGET"/>
  <portlet:param name="permissionType" value="PERMISSIONTYPE"/>
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
        <a href="${ groupUrl }">${ group.name }</a> > 
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
                            <a href="#${n}principalTab" shape="rect"><span><spring:message code="principals"/></span></a>
                        </li>
                        <li class="ui-state-default ui-corner-top">
                            <a href="#${n}targetTab" shape="rect"><span><spring:message code="targets"/></span></a>
                        </li>
                    </ul>
    
                    <c:forTokens items="principal,target" delims="," var="token">
                        
                        <div id="${n}${token}Tab" class="pager-container-${token} ui-tabs-panel ui-widget-content ui-corner-bottom${ token == 'target' ? ' ui-tabs-hide' : '' }">
                            <div class="fl-col-mixed-200 options">
                                <div class="fl-col-fixed fl-force-left view-filter">
                                <!-- This space left blank for future filtering options... -->
                                </div>
                                <div class="fl-col-flex view-pager flc-pager-top">
                                    <ul id="pager-top" class="fl-pager-ui">
                                        <li class="flc-pager-previous"><a href="#">&lt; <spring:message code="previous"/></a></li>
                                        <li>
                                            <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                                <li class="flc-pager-pageLink"><a href="javascript:;">1</a></li>
                                                <li class="flc-pager-pageLink-disabled">2</li>
                                                <li class="flc-pager-pageLink"><a href="javascript:;">3</a></li>
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
                            
                            <div class="content">
                            
                                <table class="portlet-table" id="${n}permissionsTable" summary="" xmlns:rsf="http://ponder.org.uk" style="width:100%;">
                                    <thead>
                                        <tr rsf:id="header:">
                                            <th id="${n}permissionOwner" class="flc-pager-sort-header"><a rsf:id="permissionOwner" title="Click to sort" href="javascript:;"><spring:message code="owner"/></a></th>
                                            <th id="${n}permissionPrincipal" class="flc-pager-sort-header"><a rsf:id="permissionPrincipal" title="Click to sort" href="javascript:;"><spring:message code="principal"/></a></th>
                                            <th id="${n}permissionActivity" class="flc-pager-sort-header"><a rsf:id="permissionActivity" title="Click to sort" href="javascript:;"><spring:message code="activity"/></a></th>
                                            <th id="${n}permissionTarget" class="flc-pager-sort-header"><a rsf:id="permissionTarget" title="Click to sort" href="javascript:;"><spring:message code="target"/></a></th>
                                            <th id="${n}permissionType" class="flc-pager-sort-header"><a rsf:id="permissionType" title="Click to sort" href="javascript:;"><spring:message code="grant.deny"/></a></th>
                                            <th id="${n}permissionEdit" rsf:id="permissionEdit"><spring:message code="edit"/></th>
                                            <th id="${n}permissionDelete" rsf:id="permissionDelete"><spring:message code="delete"/></th>
                                        </tr>
                                    </thead>
                                    <tbody id="${n}permissionsBody">
                                        <tr rsf:id="row:">
                                            <td headers="${n}permissionOwner"><span rsf:id="permissionOwner"></span></td>
                                            <td headers="${n}permissionPrincipal"><span rsf:id="permissionPrincipal"></span></td>
                                            <td headers="${n}permissionActivity"><span rsf:id="permissionActivity"></span></td>
                                            <td headers="${n}permissionTarget"><span rsf:id="permissionTarget"></span></td>
                                            <td headers="${n}permissionType"><span rsf:id="permissionType"></span></td>
                                            <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                                            <td headers="${n}permissionDelete"><a href="" rsf:id="permissionDelete"></a></td>
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
    var deleteUrl = "${deleteUrl}";

    var getPermissionsForTarget = function() {
        var rslt;
        $.ajax({
             url: "<c:url value="/mvc/permissionsList"/>",
             async: false,
             cache: false,
             data: { target: '${ group.key }' },
             dataType: "json",
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus + " : " + errorThrown);
             },
             success: function(data) {
                rslt = data.permissionsList;
             }
        });
        return rslt;
    };

    var getPermissionsForPrincipal = function() {
        var rslt;
        $.ajax({
             url: "<c:url value="/mvc/permissionsList"/>",
             async: false,
             cache: false,
             data: { principal: '1.${ group.key }' },
             dataType: "json",
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert(textStatus + " : " + errorThrown);
             },
             success: function(data) {
                rslt = data.permissionsList;
             }
        });
        return rslt;
    };

    // Initialize the pager
    var options = {
        columnDefs: [
            { key: "permissionOwner", valuebinding: "*.owner", sortable: true },
            { key: "permissionPrincipal", valuebinding: "*.principalName", sortable: true },
            { key: "permissionActivity", valuebinding: "*.activity", sortable: true },
            { key: "permissionTarget", valuebinding: "*.targetName", sortable: true },
            { key: "permissionType", valuebinding: "*.permissionType", sortable: true },
            { key: "permissionEdit", valuebinding: "*.owner",
                components: {
                    target: editUrl.replace("OWNER", '${"${*.owner}"}')
                                    .replace("ACTIVITY", '${"${*.activity}"}')
                                    .replace("TARGET", '${"${*.target}"}'),
                    linktext: "<spring:message code="edit"/>"
                }
            },
            { key: "permissionDelete", valuebinding: "*.owner",
                components: {
                    target: deleteUrl.replace("OWNER", escape('${"${*.owner}"}'))
                                    .replace("PRINCIPALTYPE", escape('${"${*.principalType}"}'))
                                    .replace("PRINCIPALKEY", escape('${"${*.principalKey}"}'))
                                    .replace("ACTIVITY", escape('${"${*.activity}"}'))
                                    .replace("TARGET", escape('${"${*.target}"}'))
                                    .replace("PERMISSIONTYPE", escape('${"${*.permissionType}"}')),
                    linktext: "<spring:message code="delete"/>"
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
        $("#${n}assignmentTabs").tabs();
        var targetOptions = options;
        targetOptions.dataModel = getPermissionsForTarget();
        var targetPager = up.fluid.pager(".pager-container-target", targetOptions);
        var principalOptions = options;
        principalOptions.dataModel = getPermissionsForPrincipal();
        var principalPager = up.fluid.pager(".pager-container-principal", principalOptions);
    });
    
});
</script>
