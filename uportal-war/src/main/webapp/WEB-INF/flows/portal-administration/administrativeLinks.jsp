<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

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
