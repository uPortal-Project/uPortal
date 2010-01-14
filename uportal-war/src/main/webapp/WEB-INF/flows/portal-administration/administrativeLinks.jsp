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
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Portal Administration Tools</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">User Administration</h3>
      <div class="portlet-section-body">
        <ul>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=IdentitySwapper">Swap identity</a>
            </li>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=AttributeSwapper">Swap attributes</a>
            </li>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=passwordmgr">Manage passwords</a>
            </li>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=reset-user-layout">Reset a user's layout</a>
            </li>
        </ul>
	  </div>
	</div>
	
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Portal Entity Administration</h3>
      <div class="portlet-section-body">
        <ul>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=portlet-admin">Manage portlets</a>
            </li>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=groupsmanager">Manage groups</a>
            </li>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=permissionsmanager">Manage permissions</a>
            </li>
        </ul>
      </div>
    </div>
    
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Portal Administration</h3>
      <div class="portlet-section-body">
        <ul>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=cache-manager">Manage Cache instances</a>
            </li>
        </ul>
      </div>
    </div>
    
    <div class="portlet-section" role="region">
      <h3 class="portlet-section-header" role="heading">Import/Export</h3>
      <div class="portlet-section-body">
        <ul>
            <li>
                <a href="render.userLayoutRootNode.uP?uP_fname=ImportExportPortlet">Import, Export, and Delete Entities</a>
            </li>
        </ul>
      </div>  
    </div>
    
  </div>

</div>
