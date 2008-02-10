<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!--
 | This file determines the presentation of the main navigation systems of the portal.
 | The file is imported by the base stylesheet universalty.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universalty.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
  
  <!-- ========== TEMPLATE: NAVIGATION ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the main navigation.
   | This template can be rendered into the header or the left navigation column, determined by the parameters set in universalty.xsl.
  -->
  <xsl:template match="navigation">
  	<xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the navigation. -->
    
    <div id="portalNavigation">  <!-- Div for presentation/formatting options. -->
    	<div id="portalNavigationInner" class="{$CONTEXT}">  <!-- Inner div for additional presentation/formatting options. -->
      
        <a name="startContent"><xsl:comment>Comment to keep from collapsing</xsl:comment></a>  <!-- Skip navigation target. -->
        
        <ul id="portalNavigationList">
        	<xsl:attribute name="class"> <!-- If rendered in the header, write in the class to format as a floated list to create tabs. -->
          	<xsl:choose>
            	<xsl:when test="$CONTEXT='header'">horizontal-list-floated</xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:for-each select="tab">
            <xsl:variable name="NAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
              <xsl:choose>
                <xsl:when test="position()=1 and position()=last()">single</xsl:when>
                <xsl:when test="position()=1">first</xsl:when>
                <xsl:when test="position()=last()">last</xsl:when>
                <xsl:otherwise></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="NAV_ACTIVE"> <!-- Determine which navigation option is the active (current selection) and add a css hook. -->
              <xsl:choose>
                <xsl:when test="@activeTab='true'">active</xsl:when>
                <xsl:otherwise></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <li id="portalNavigation_{@ID}" class="portal-navigation {$NAV_POSITION} {$NAV_ACTIVE}"> <!-- Each navigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
              <xsl:if test="@activeTab='false' and $USE_FLYOUT_MENUS='true'"> <!-- If using flyout menus, add needed hooks for hiding and displaying submenus. -->
                <xsl:attribute name="onmouseover">showSubnav('<xsl:value-of select="@ID"/>')</xsl:attribute>
                <xsl:attribute name="onmouseout">hideSubnav('<xsl:value-of select="@ID"/>', event)</xsl:attribute>
              </xsl:if>
              <a id="tabLink_{@ID}" href="{$BASE_ACTION_URL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" title="{@name}" class="portal-navigation-link">  <!-- Navigation item link. -->
                <span class="portal-navigation-label"><xsl:value-of select="@name"/></span>
              </a>
              <xsl:if test="@activeTab='false' and $USE_FLYOUT_MENUS='true'"> <!-- If using flyout menus, call template for rendering submenus. -->
                <xsl:call-template name="subnavigation">
                  <xsl:with-param name="context" select="'flyout'"/>
                </xsl:call-template>
              </xsl:if>
              <xsl:if test="@activeTab='true' and $CONTEXT='left'"> <!-- If navigation is being rendered in the left column rather than as tabs, call template for rendering active menu item's submenu. -->
                <xsl:call-template name="subnavigation">
                  <xsl:with-param name="CONTEXT" select="'subnav'"/>
                </xsl:call-template>
              </xsl:if>
            </li>
          </xsl:for-each>
        </ul>
      
    	</div>  
    </div>
  </xsl:template>
  <!-- ========================================== -->


  <!-- ========== TEMPLATE: SUBNAVIGATION ========== -->
  <!-- ============================================= -->
  <!--
   | This template renders subnavigation which may appear in different contexts.
   | The context parameter is used to know whether the subnavigation is a flyout menu, the expanded display of the selected left navigation menu item, or as a separate navigation list of the page contents when using tabs.
   | These options are determined by the parameters set in universalty.xsl.
  -->
  <xsl:template name="subnavigation">
    <xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the subnavigation. -->
    
    <div id="portalSubnavigation_{@ID}"> <!-- Unique ID is needed for the flyout menus javascript. -->
      <xsl:attribute name="class"> <!-- Write in CSS classes depending on context. -->
        <xsl:choose>
        	<xsl:when test="$CONTEXT='flyout'">portal-flyout-container</xsl:when>
        	<xsl:otherwise>portal-subnav-container</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="style"> <!-- Write in CSS classes for flyout menus, for the submenu contents to not be rendered on page load. -->
        <xsl:choose>
        	<xsl:when test="$CONTEXT='flyout'">display: none;</xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <div id="portalSubnavigationInner">  <!-- Inner div for additional presentation/formatting options. -->
        <ul class="portal-subnav-list"> <!-- List of the subnavigation menu items. -->
          <xsl:for-each select="tabChannel">
            <xsl:variable name="SUBNAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
              <xsl:choose>
                <xsl:when test="position()=1 and position()=last()">single</xsl:when>
                <xsl:when test="position()=1">first</xsl:when>
                <xsl:when test="position()=last()">last</xsl:when>
                <xsl:otherwise></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <li id="uPfname_{@fname}" class="portal-subnav {$SUBNAV_POSITION}"> <!-- Each subnavigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
              <a href="{$BASE_ACTION_URL}?uP_root={@ID}" title="{@name}" class="portal-subnav-link">  <!-- Navigation item link. -->
                <span class="portal-subnav-label"><xsl:value-of select="@name"/></span>
              </a>
            </li>
          </xsl:for-each>
        </ul>
    	</div> 
    </div>
    
    <!-- ????? NEED TO PROVIDE A TARGET FOR THE IFRAME TO BE VALID WITH SSL ????? -->
    <xsl:if test="$USE_FLYOUT_MENUS='true'">  <!-- IE fix. If using flyout menus, render an iframe behind the submenu to ensure the submenu renders on top of all other elements. -->
    	<iframe id="navFrame_{@ID}" style="display: none;" frameborder="0"></iframe>
    </xsl:if>
    
  </xsl:template>
  <!-- ============================================= -->
	
  
  <!-- ========== TEMPLATE: FLYOUT MENU SCRIPTS ========== -->
  <!-- =================================================== -->
  <!--
   | This template renders scripts specific to the flyout menus.
  -->
  <xsl:template name="flyout.menu.scripts">
		<script src="{$SCRIPT_PATH}/flyout-nav.js" type="text/javascript"></script>
    <script type="text/javascript">
			var tabList = document.getElementById('portalNavigation');
			var yoffset = portalNavigationList.clientHeight;
			<!--var yoffset = findPosY(portalNavigationList) + portalNavigationList.clientHeight; removed findPosY since the tabs are absolutely positioned-->
			var xoffset = 9;
			<xsl:for-each select="/layout/navigation/tab">
				<xsl:choose>
					<xsl:when test="not(@activeTab='true')">
						var menu = document.getElementById('navMenu_<xsl:value-of select="@ID"/>');
						menu.style.top = yoffset + 'px';
						menu.style.left = xoffset + 'px';
						
						var iframe = document.getElementById('navFrame_<xsl:value-of select="@ID"/>');
						iframe.style.top = menu.style.top;
						iframe.style.left = menu.style.left;
						iframe.style.height = menu.clientHeight + 'px';
						iframe.style.width = menu.clientWidth + 'px';
						
						xoffset += document.getElementById('tabLink_<xsl:value-of select="@ID"/>').clientWidth;
					</xsl:when>
					<xsl:otherwise>
						xoffset += document.getElementById('activeTabLink').clientWidth;						
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>		
    </script>
  </xsl:template>
  <!-- =================================================== -->
	
		
</xsl:stylesheet>
