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

<!-- Portlet -->
<div class="fl-widget portlet imp-exp view-delete" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading"><spring:message code="delete.portlet.entities"/></h2>
        <div class="toolbar" role="toolbar">
            <ul>
                <li><a class="button" href="<portlet:renderURL/>"><spring:message code="import"/></a></li>
                <li><a class="button" href="<portlet:renderURL><portlet:param name="action" value="export"/></portlet:renderURL>"><spring:message code="export"/></a></li>
            </ul>
        </div>
    </div>
    
    <!-- Portlet Content -->
    <div class="fl-widget-content content portlet-content" role="main">   
        
        <!-- Messages -->
        <div class="portlet-msg-error portlet-msg error" role="alert">
            <div class="titlebar">
                <h3 class="title">Warning</h3>
            </div>
            <div class="content">
                <p>Deleting some entities can do very bad things to your portal.  By default, all delete operations are disabled;  use this feature with caution.</p>
            </div>
        </div>
        
        <!-- Note -->
        <div class="note" role="note">
            <p>Select an entity to delete.</p>
        </div>
        
        <div class="portlet-form">
            <form id="${n}form" method="POST">
                <table class="purpose-layout">
                    <tr>
                        <td class="label">
                            <label class="portlet-form-label" for="entityType"><spring:message code="type"/>:</label>
                        </td>
                        <td>
                            <select id="entityType" name="entityType">
                                <option>[<spring:message code="select.type"/>]</option>
                                <c:forEach items="${supportedTypes}" var="type">
                                    <option value="${fn:escapeXml(type.typeId)}"><spring:message code="${type.titleCode}"/></option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <label class="portlet-form-label" for="sysid"><spring:message code="id"/>:</label>
                        </td>
                        <td>
                            <input type="text" id="sysid" name="sysid"/>
                        </td>
                    </tr>
                </table>
                <div class="buttons">
                    <input class="button primary" type="submit" value="Delete"/>
                </div>
            </form>
        </div>
    
    </div> <!-- end: portlet-content -->
</div> <!-- end:portlet -->

<script type="text/javascript">
    up.jQuery(document).ready(function () {
        var $ = up.jQuery;
        
        $("#${n}form").submit(function () {
           var form, entityType, sysId, href;
           
           form = this;
           entityType = form.entityType.value;
           sysId = form.sysid.value;
           $.ajax({
               url: "<c:url value="/api/entity/"/>" + entityType + "/" + sysId,
               type: "DELETE"
           });
           
           return false;
        });
    });
</script>
