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
<div class="fl-widget portlet portal-adm view-links" role="section">
  
  <!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    <h2 class="title" role="heading">Portal Administration Tools</h2>
  </div>
  
  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    	<div class="titlebar">
      		<h3 class="title" role="heading">Portal Entity Administration</h3>
        </div>
        <div class="content">
            <ul>
                <li>
                    <a href="<c:url value="/p/user-administration"/>">Manage Users</a>
                </li>
                <li>
                    <a href="<c:url value="/p/portlet-admin"/>">Manage portlets</a>
                </li>
                <li>
                    <a href="<c:url value="/p/groupsmanager"/>">Manage groups</a>
                </li>
                <li>
                    <a href="<c:url value="/p/permissionsmanager"/>">Manage permissions</a>
                </li>
                <li>
                    <a href="<c:url value="/p/fragment-admin"/>">Manage DLM Fragments</a>
                </li>
            </ul>
        </div>
    </div>
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    	<div class="titlebar">
      		<h3 class="title" role="heading">Portal Administration</h3>
        </div>
        <div class="content">
            <ul>
                <li>
                    <a href="<c:url value="/p/cache-manager"/>">Manage Cache instances</a>
                </li>
                <li>
                    <a href="<c:url value="/p/toggle-resources-aggregation"/>">Toggle JS/CSS Aggregation</a>
                </li>
                <li>
                    <a href="<c:url value="/p/fragment-audit"/>">Audit DLM Fragments</a>
                </li>
            </ul>
        </div>
    </div>
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
    	<div class="titlebar">
      		<h3 class="title" role="heading">Import/Export</h3>
        </div>
        <div class="content">
            <ul>
                <li>
                    <a href="<c:url value="/p/ImportExportPortlet"/>">Import, Export, and Delete Entities</a>
                </li>
            </ul>
        </div>  
    </div>
    
  </div> <!-- end: portlet-content -->
</div> <!-- end: portlet -->
