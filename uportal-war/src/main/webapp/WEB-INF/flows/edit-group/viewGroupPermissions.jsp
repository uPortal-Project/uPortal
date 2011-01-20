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
            
            	<!-- NEW DEVELOPMENT - No group permissions message. -->
                <!-- This needs to be hooked up to a real test, and then the choose > otherwise statement needs to wrap the default table display below.
                <c:choose>
                	<c:when test="$permissions == 0">
                    	<p><spring:message code="no.group.permissions"/> <a href="{$add_permission_url}"><spring:message code="add.permission"/></a>.</p>
                    </c:when>
                    <c:otherwise>
                    	Permissions list goes here.
                    </c:otherwise>
                </c:choose> -->
                  

                <div id="${n}assignmentTabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
                    <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
                        <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active">
                            <a href="#${n}principalTab" shape="rect"><span><spring:message code="permissions.for.name" arguments="${group.name}"/></span></a>
                        </li>
                        <li class="ui-state-default ui-corner-top">
                            <a href="#${n}targetTab" shape="rect"><span><spring:message code="permissions.on.name" arguments="${group.name}"/></span></a>
                        </li>
                    </ul>

                    <div id="${n}principalTab" class="pager-container-principal ui-tabs-panel ui-widget-content ui-corner-bottom">
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
                                        <th id="${n}permissionActivity" class="flc-pager-sort-header"><a rsf:id="permissionActivity" title="Click to sort" href="javascript:;"><spring:message code="activity"/></a></th>
                                        <th id="${n}permissionTarget" class="flc-pager-sort-header"><a rsf:id="permissionTarget" title="Click to sort" href="javascript:;"><spring:message code="target"/></a></th>
                                        <th id="${n}permissionEdit" rsf:id="permissionEdit"><spring:message code="edit"/></th>
                                    </tr>
                                </thead>
                                <tbody id="${n}permissionsBody">
                                    <tr rsf:id="row:">
                                        <td headers="${n}permissionOwner"><span rsf:id="permissionOwner"></span></td>
                                        <td headers="${n}permissionActivity"><span rsf:id="permissionActivity"></span></td>
                                        <td headers="${n}permissionTarget"><span rsf:id="permissionTarget"></span></td>
                                        <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                                    </tr>
                                </tbody>
                            </table>
                        
                        </div>
                    </div>

                    <div id="${n}targetTab" class="pager-container-target ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide">
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
                                        <th id="${n}permissionPrincipal" class="flc-pager-sort-header"><a rsf:id="permissionPrincipal" title="Click to sort" href="javascript:;"><spring:message code="principal"/></a></th>
                                        <th id="${n}permissionOwner" class="flc-pager-sort-header"><a rsf:id="permissionOwner" title="Click to sort" href="javascript:;"><spring:message code="owner"/></a></th>
                                        <th id="${n}permissionActivity" class="flc-pager-sort-header"><a rsf:id="permissionActivity" title="Click to sort" href="javascript:;"><spring:message code="activity"/></a></th>
                                        <th id="${n}permissionEdit" rsf:id="permissionEdit"><spring:message code="edit"/></th>
                                    </tr>
                                </thead>
                                <tbody id="${n}permissionsBody">
                                    <tr rsf:id="row:">
                                        <td headers="${n}permissionPrincipal"><span rsf:id="permissionPrincipal"></span></td>
                                        <td headers="${n}permissionOwner"><span rsf:id="permissionOwner"></span></td>
                                        <td headers="${n}permissionActivity"><span rsf:id="permissionActivity"></span></td>
                                        <td headers="${n}permissionEdit"><a href="" rsf:id="permissionEdit"></a></td>
                                    </tr>
                                </tbody>
                            </table>
                        
                        </div>
                    </div>


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
            url: "<c:url value="/api/assignments/target/${ group.key }.json?includeInherited=true"/>",
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

    var getPermissionsForPrincipal = function() {
        var rslt;
        $.ajax({
             url: "<c:url value="/api/assignments/principal/${ group.key }.json?includeInherited=true"/>",
             async: false,
             cache: false,
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

    var principalOptions = {
            columnDefs: [
                { key: "permissionOwner", valuebinding: "*.ownerName", sortable: true },
                { key: "permissionActivity", valuebinding: "*.activityName", sortable: true },
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

    // Initialize the pager
    var options = {
        columnDefs: [
            { key: "permissionOwner", valuebinding: "*.ownerName", sortable: true },
            { key: "permissionPrincipal", valuebinding: "*.principalName", sortable: true },
            { key: "permissionActivity", valuebinding: "*.activityName", sortable: true },
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
        $("#${n}assignmentTabs").tabs();
        var targetOptions = options;
        targetOptions.dataModel = getPermissionsForTarget();
        var targetPager = up.fluid.pager(".pager-container-target", targetOptions);
        principalOptions.dataModel = getPermissionsForPrincipal();
        var principalPager = up.fluid.pager(".pager-container-principal", principalOptions);
    });
    
});
</script>
