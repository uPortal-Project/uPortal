<?xml version="1.0" encoding="utf-8"?>
<!--

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

-->

<!--
 | This file determines the presentation of the main navigation systems of the portal.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<!-- ============================================= -->
<!-- ========== STYLESHEET DELCARATION =========== -->
<!-- ============================================= -->
<!-- 
 | RED
 | This statement defines this document as XSL and declares the Xalan extension
 | elements used for URL generation and permissions checks.
 |
 | If a change is made to this section it MUST be copied to all other XSL files
 | used by the theme
-->
<xsl:stylesheet 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:upElemTitle="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanLayoutElementTitleHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg upElemTitle" 
    version="1.0">
      
  <!-- ========== TEMPLATE: NAVIGATION ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the main navigation construct.
   | This template can be rendered into the header or the sidebar, determined by the parameters set in universality.xsl.
  -->
  <xsl:template match="navigation">
  	<xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the navigation. -->
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
    <xsl:choose>
    	<xsl:when test="$CONTEXT = 'header'">  <!-- When the context is 'header' render the main navigation as tabs. -->
      	
        <div id="portalNavigation">
          <!-- Optional Tab Groups -->
          <xsl:if test="$USE_TAB_GROUPS='true'">
            <h3 class="fl-offScreen-hidden">Tab Groups</h3>
            <div id="portalNavigationTabGroup">
              <span id="activeTabGroup" style="display:none;"><xsl:value-of select="/layout/navigation/tabGroupsList/@activeTabGroup"/></span>
              <ul id="portalNavigationTabGroupsList">
                <xsl:for-each select="tabGroupsList/tabGroup">
                  <xsl:variable name="TABGROUP_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
                    <xsl:choose>
                      <xsl:when test="position()=1 and position()=last()">singleTabGroup</xsl:when>
                      <xsl:when test="position()=1">firstTabGroup</xsl:when>
                      <xsl:when test="position()=last()">lastTabGroup</xsl:when>
                      <xsl:otherwise></xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="TABGROUP_CSS">
                    <xsl:choose>
                      <xsl:when test="/layout/navigation/tabGroupsList/@activeTabGroup=.">tabGroupListActive <xsl:value-of select="$TABGROUP_POSITION"/></xsl:when>
                      <xsl:otherwise><xsl:value-of select="$TABGROUP_POSITION"/></xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="TABGROUP_LABEL">
                    <xsl:choose>
                      <xsl:when test="@name='DEFAULT_TABGROUP'"><xsl:value-of select="upMsg:getMessage('navigation.tabgroup.default', $USER_LANG)"/></xsl:when>
                      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="TABGROUP_URL">
                    <xsl:call-template name="portalUrl">
                      <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@firstTabId"/></url:layoutId>
                        </url:portal-url>
                    </xsl:with-param>
                    </xsl:call-template>
                  </xsl:variable>
                  <li class="{$TABGROUP_CSS}">
                    <a href="{$TABGROUP_URL}" title="{.}"><span class="portal-tabGroup-label"><xsl:value-of select="$TABGROUP_LABEL"/></span></a>
                  </li>
                </xsl:for-each>
              </ul>
            </div>
          </xsl:if>
          <!-- Tabs -->
          <div id="portalNavigationInner" class="{$CONTEXT}">
          	<a name="mainNavigation" class="skip-link" title="Reference anchor: main nagivation"><xsl:comment>Comment to keep from collapsing</xsl:comment></a>  <!-- Skip navigation target. -->
            <ul id="portalNavigationList" class="fl-tabs flc-reorderer-column">
             <xsl:apply-templates select="tab[$USE_TAB_GROUPS!='true' or @tabGroup=$ACTIVE_TAB_GROUP]">
               <xsl:with-param name="CONTEXT" select="$CONTEXT"/>
             </xsl:apply-templates>
            </ul>
            
            <xsl:if test="$USE_SUBNAVIGATION_ROW='true'">
              <div id="portalNavigationSubrow">
                <xsl:call-template name="subnavigation">
                  <xsl:with-param name="CONTEXT" select="'subnav'"/>
                  <xsl:with-param name="TAB_POSITION" select="count(tab[@activeTab='true']/preceding-sibling::tab) + 1"/>
                </xsl:call-template>
              </div>
            </xsl:if>
            <xsl:if test="$AUTHENTICATED='true' and $USE_ADD_TAB='true' and not(//focused)">
                <a href="javascript:;" title="{upMsg:getMessage('add.tab', $USER_LANG)}" class="portal-navigation-add"><xsl:value-of select="upMsg:getMessage('add.tab', $USER_LANG)"/></a>
            </xsl:if>
          </div>
        </div>
      
      </xsl:when>
      <xsl:otherwise>  <!-- Otherwise, render the main navigation as a widget (generally assumes the context is the sidebar). -->
      
      	<div id="portalNavigation" class="fl-widget">
        	<div id="portalNavigationInner" class="fl-widget-inner {$CONTEXT}">
          	<div class="fl-widget-titlebar">
                <h2>
                    <a name="mainNavigation" class="skip-link" title="Reference anchor: main nagivation"><xsl:value-of select="upMsg:getMessage('navigation', $USER_LANG)"/></a>  <!-- Skip navigation target. -->
                    <xsl:value-of select="upMsg:getMessage('navigation', $USER_LANG)"/>
                </h2>
                <xsl:if test="$AUTHENTICATED='true' and $USE_ADD_TAB='true' and not(//focused)">
                    <a href="javascript:;" title="upMsg:getMessage('add.tab', $USER_LANG)" class="portal-navigation-add"><xsl:value-of select="upMsg:getMessage('add.tab', $USER_LANG)"/></a>
                </xsl:if>
          	</div>
            <div class="fl-widget-content">
              <ul id="portalNavigationList" class="fl-listmenu flc-reorderer-column">
                 <xsl:apply-templates select="tab[$USE_TAB_GROUPS!='true' or @tabGroup=$ACTIVE_TAB_GROUP]">
                   <xsl:with-param name="CONTEXT" select="$CONTEXT"/>
                 </xsl:apply-templates>
              </ul>
            </div>
          </div>
        </div>
      
      </xsl:otherwise>
    </xsl:choose>
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->   
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: NAVIGATION TABS ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the contents of the main navigation.
  -->
  <xsl:template match="tab">
  	<xsl:param name="CONTEXT"/>  <!-- Catches the context parameter. -->

    <xsl:variable name="NAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
      <xsl:choose>
        <xsl:when test="last() = 1">single</xsl:when>
        <xsl:when test="position() = 1">first</xsl:when>
        <xsl:when test="position() = last()">last</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_ACTIVE"> <!-- Determine which navigation option is the active (current selection) and add a css hook. -->
      <xsl:choose>
        <xsl:when test="@activeTab='true' and $CONTEXT='header'">active fl-tabs-active</xsl:when>
        <xsl:when test="@activeTab='true' and $CONTEXT='sidebar'">active fl-activemenu</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_MOVABLE"> <!-- Determine whether the navigation tab is movable and add a css hook. -->
      <xsl:choose>
        <xsl:when test="not(@dlm:moveAllowed='false')">movable</xsl:when>
        <xsl:otherwise>locked</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_DELETABLE">
      <xsl:choose>
        <xsl:when test="not(@dlm:deleteAllowed='false')">deletable</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_EDITABLE">
      <xsl:choose>
        <xsl:when test="not(@dlm:editAllowed='false')">editable</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_CAN_ADD_CHILDREN">
      <xsl:choose>
        <xsl:when test="not(@dlm:deleteAllowed='false')">canAddChildren</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDITABLE"><!--Determine which navigation tab has edit permissions and is the active tab. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false')">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true' and $CONTEXT='header'">flc-inlineEditable</xsl:when>
                            <xsl:when test="@activeTab='true' and $CONTEXT='sidebar'">flc-inlineEditable</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDIT_TEXT"><!--Determine which navigation tab has edit permissions and is the active tab. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false')">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true' and $CONTEXT='header'">flc-inlineEdit-text</xsl:when>
                            <xsl:when test="@activeTab='true' and $CONTEXT='sidebar'">flc-inlineEdit-text</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDIT_TITLE"><!--Determine which navigation tab has edit permissions and is the active tab. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false')">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true' and $CONTEXT='header'">Click to edit tab name</xsl:when>
                            <xsl:when test="@activeTab='true' and $CONTEXT='sidebar'">Click to edit tab name</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <li id="portalNavigation_{@ID}" class="portal-navigation {$NAV_POSITION} {$NAV_ACTIVE} {$NAV_MOVABLE} {$NAV_EDITABLE} {$NAV_DELETABLE} {$NAV_CAN_ADD_CHILDREN}"> <!-- Each navigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
      <xsl:variable name="tabLinkUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
              <url:portal-url>
                  <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
              </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
      </xsl:variable>
      <a id="tabLink_{@ID}" href="{$tabLinkUrl}" title="{@name}" class="portal-navigation-link {$NAV_INLINE_EDITABLE}">  <!-- Navigation item link. -->
        <span title="{$NAV_INLINE_EDIT_TITLE}" class="portal-navigation-label {$NAV_INLINE_EDIT_TEXT}"><xsl:value-of select="upElemTitle:getTitle(@ID, $USER_LANG, @name)"/></span>
      </a>
      <xsl:if test="$AUTHENTICATED='true' and not($PORTAL_VIEW='focused') and not(dlm:moveAllowed='false')">
          <a href="javascript:;" class="portal-navigation-gripper {$NAV_ACTIVE}" title="{upMsg:getMessage('move.this.tab', $USER_LANG)}"><span><xsl:value-of select="upMsg:getMessage('move', $USER_LANG)"/></span></a> <!-- Drag & drop gripper handle. -->
      </xsl:if>
      <xsl:if test="$AUTHENTICATED='true' and @activeTab='true' and $NAV_POSITION != 'single' and not($PORTAL_VIEW='focused')">
          <xsl:if test="not(@dlm:deleteAllowed='false')">
            <a href="javascript:;" class="portal-navigation-delete" title="{upMsg:getMessage('remove.this.tab', $USER_LANG)}"><span><xsl:value-of select="upMsg:getMessage('remove', $USER_LANG)"/></span></a><!-- Remove tab icon. -->
          </xsl:if>
      </xsl:if>
      <xsl:if test="@activeTab='true' and $CONTEXT='sidebar'"> <!-- If navigation is being rendered in the sidebar rather than as tabs, call template for rendering active menu item's submenu. -->
        <xsl:call-template name="subnavigation">
          <xsl:with-param name="CONTEXT" select="'subnav'"/>
          <xsl:with-param name="TAB_POSITION" select="position()"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="$USE_FLYOUT_MENUS='true'"> <!-- If using flyout menus, call template for rendering submenus. -->
        <xsl:call-template name="subnavigation">
          <xsl:with-param name="CONTEXT" select="'flyout'"/>
          <xsl:with-param name="TAB_POSITION" select="position()"/>
        </xsl:call-template>
      </xsl:if>
    </li>
  
  </xsl:template>
  <!-- ========================================== -->
  
    
  <!-- ========== TEMPLATE: PORTLET NAVIGATION ========== -->
  <!-- ================================================== -->
  <!--
   | This template renders portlet navigation when a portlet is focused.
  -->
  <xsl:template name="portlet.navigation">
    <div id="portletNavigation" class="fl-widget">
    	<div class="fl-widget-inner">
      	<div class="fl-widget-titlebar">
      		<h2><xsl:value-of select="upMsg:getMessage('jump.to', $USER_LANG)"/>:</h2>
        </div>
        <div class="fl-widget-content">
        	<ul class="fl-listmenu">
          	<li id="portletNavigationLinkHome">
                <xsl:variable name="homeUrl">
                  <xsl:call-template name="portalUrl"/>
                </xsl:variable>
            	<a href="{$homeUrl}" title="{upMsg:getMessage('go.back.to.home', $USER_LANG)}">
              	<span>
                	<xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/>
                </span>
              </a>
            </li>
          </ul>
          <xsl:for-each select="//navigation/tab">
            <xsl:variable name="TAB_POSITION" select="position()"/>
            <h3><xsl:value-of select="@name"/></h3>
            <ul class="fl-listmenu">
              <xsl:for-each select="tabChannel">
                <li>
                  <xsl:variable name="tabLinkUrl">
                    <xsl:call-template name="portalUrl">
                        <xsl:with-param name="url">
                          <url:portal-url>
                              <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                          </url:portal-url>
                        </xsl:with-param>
                    </xsl:call-template>
                  </xsl:variable>
                  <a href="{$tabLinkUrl}" title="{@name}">  <!-- Navigation item link. -->
                    <span><xsl:value-of select="@name"/></span>
                  </a>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:for-each>
    		</div>
      </div>  
    </div>
  </xsl:template>
  <!-- ================================================== -->


  <!-- ========== TEMPLATE: SUBNAVIGATION ========== -->
  <!-- ============================================= -->
  <!--
   | This template renders subnavigation which may appear in different contexts.
   | The context parameter is used to know whether the subnavigation is a flyout menu, the expanded display of the selected sidebar navigation menu item, or as a separate navigation list of the page contents when using tabs.
   | These options are determined by the parameters set in universality.xsl.
  -->
  <xsl:template name="subnavigation">
    <xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the subnavigation. -->
    <xsl:param name="TAB_POSITION"/> <!-- Provides the position of the tab -->
    
    <div> <!-- Unique ID is needed for the flyout menus javascript. -->
      <xsl:attribute name="id">
        <xsl:choose>
          <xsl:when test="$CONTEXT='flyout'">portalFlyoutNavigation_<xsl:value-of select="@ID"/></xsl:when>
          <xsl:otherwise>portalSubnavigation_<xsl:value-of select="@ID"/></xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
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
      
      <div>  <!-- Inner div for additional presentation/formatting options. -->
        <xsl:attribute name="id">
          <xsl:choose>
            <xsl:when test="$CONTEXT='flyout'">portalFlyoutNavigationInner_<xsl:value-of select="@ID"/></xsl:when>
            <xsl:otherwise>portalSubnavigationInner_<xsl:value-of select="@ID"/></xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$CONTEXT='flyout'">portal-flyout-container-inner</xsl:when>
            <xsl:otherwise>portal-subnav-container-inner</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <ul class="portal-subnav-list"> <!-- List of the subnavigation menu items. -->
        	<xsl:choose>
          	<xsl:when test="$CONTEXT='flyout'">
            
              <xsl:for-each select="tabChannel">
                <xsl:variable name="SUBNAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
                  <xsl:choose>
                    <xsl:when test="position()=1 and position()=last()">single</xsl:when>
                    <xsl:when test="position()=1">first</xsl:when>
                    <xsl:when test="position()=last()">last</xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <li id="portalSubnavLink_{@ID}" class="portal-subnav {$SUBNAV_POSITION} {@fname}"> <!-- Each subnavigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
                  <xsl:variable name="portletSubNavLink">
                    <xsl:call-template name="portalUrl">
                        <xsl:with-param name="url">
                            <url:portal-url>
                                <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                                <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                            </url:portal-url>
                        </xsl:with-param>
                    </xsl:call-template>
                  </xsl:variable>
                  <a href="{$portletSubNavLink}" title="{@description}" class="portal-subnav-link">  <!-- Navigation item link. -->
                      <span class="portal-subnav-label"><xsl:value-of select="@title"/></span>
                  </a>
                </li>
              </xsl:for-each>
              
            </xsl:when>
            <xsl:otherwise>
            	
              <xsl:for-each select="//navigation/tab[@activeTab='true']/tabChannel">
                <xsl:variable name="SUBNAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
                  <xsl:choose>
                    <xsl:when test="position()=1 and position()=last()">single</xsl:when>
                    <xsl:when test="position()=1">first</xsl:when>
                    <xsl:when test="position()=last()">last</xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <li id="uPfname_{@fname}" class="portal-subnav {$SUBNAV_POSITION}"> <!-- Each subnavigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
                  <xsl:variable name="portletSubNavLink">
                    <xsl:call-template name="portalUrl">
                        <xsl:with-param name="url">
                            <url:portal-url>
                                <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                                <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                            </url:portal-url>
                        </xsl:with-param>
                    </xsl:call-template>
                  </xsl:variable>
                  <a href="{$portletSubNavLink}" title="{@description}" class="portal-subnav-link">  <!-- Navigation item link. -->
                    <span class="portal-subnav-label"><xsl:value-of select="@title"/></span>
                  </a>
                </li>
              </xsl:for-each>
              
            </xsl:otherwise>
          </xsl:choose>
        </ul>
    	</div> 
    </div>
    
  </xsl:template>
  <!-- ================================================== -->
  
  
  
  <!-- ========== TEMPLATE: FLYOUT MENU SCRIPTS ========== -->
  <!-- =================================================== -->
  <!--
   | This template renders scripts specific to the flyout menus.
  -->
  <xsl:template name="flyout.menu.scripts">
    <script type="text/javascript">
      up.jQuery(document).ready(function(){
        // initialize the flyout menus and add onmouseover and onmouseout events to 
        // all the navigation elements with subnavigation flyouts
        var flyouts = new Array();
        var flyoutOptions = { flyoutMenu: '.portal-flyout-container' };
        up.jQuery("ul.fl-tabs li.portal-navigation").each( function() {
          flyouts.push(uportal.flyoutmenu(this, flyoutOptions));
        });
      });
    </script>
  </xsl:template>
  <!-- =================================================== -->
	
		
</xsl:stylesheet>
