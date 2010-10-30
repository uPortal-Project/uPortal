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
<div class="fl-widget portlet imp-exp view-export" role="section">
	
    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Export Portlet Entities</h2>
        <div class="toolbar" role="toolbar">
            <ul>
                <li><a class="button" href="<portlet:renderURL><portlet:param name="view" value="import"/></portlet:renderURL>">Import</a></li>
                <li><a class="button" href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a></li>
            </ul>
        </div>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">
		
        <!-- Note -->
        <div class="portlet-note" role="note">
            <p>Select an entity to export. You can allow/disallow entity types using Portlet Preferences.  See uPortal's portlet.xml file for details.</p>
        </div>
        
        <div class="portlet-form">
            <form method="POST" action="<portlet:actionURL><portlet:param name="action" value="doExport"/><portlet:param name="view" value="status"/></portlet:actionURL>">
                
                <table class="purpose-layout">
                	<tr>
                    <td class="label">
                    	<label class="portlet-form-label" for="entityType">Type:</label>
                    </td>
                    <td>
                        <select id="entityType" name="entityType">
                            <option>[Select Type]</option>
                            <c:forEach items="${supportedTypes}" var="type">
                                <option value="${fn:escapeXml(type)}">${fn:escapeXml(type)}</option>
                            </c:forEach>
                        </select>
                    </td>
                    </tr>
                    <tr>
                        <td class="label">
                        	<label class="portlet-form-label" for="sysid">Id:</label>
                        </td>
                        <td>
                        	<input type="text" id="sysid" name="sysid"/>
                        </td>
                    </tr>
                </table>
                <div class="buttons">
                    <input class="button primary" type="submit" value="Export"/>
                </div>
            </form>
        </div>
        
	</div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->
