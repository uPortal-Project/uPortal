<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<!--
 | This file determines the presentation of the main navigation systems of the portal.
 | The file is imported by the base stylesheet respondr.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to respondr.xsl for the list of parameters and imported XSL files.
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
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.apereo.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.apereo.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.apereo.portal.security.xslt.XalanMessageHelper"
    xmlns:upElemTitle="http://xml.apache.org/xalan/java/org.apereo.portal.security.xslt.XalanLayoutElementTitleHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg upElemTitle dlm xsi"
    version="1.0">

<xsl:param name="TAB_CONTEXT" select="'header'"/><!-- Sets the location of the navigation. Values are 'header' or 'sidebar'. -->
<xsl:param name="CONTEXT" select="'header'"/>
<xsl:param name="subscriptionsSupported">false</xsl:param>
<xsl:param name="USE_FLYOUT_MENUS">false</xsl:param> <!-- Moved to parameter in renderingPipelineContext.xml with configuration in portal.properties. -->
<xsl:param name="useTabGroups">true</xsl:param>
<xsl:param name="PORTAL_VIEW">
  <xsl:choose>
    <xsl:when test="//layout_fragment">detached</xsl:when>
    <xsl:when test="//focused">focused</xsl:when>
    <xsl:otherwise>dashboard</xsl:otherwise>
  </xsl:choose>
</xsl:param>
<xsl:variable name="USE_TAB_GROUPS">
  <xsl:choose>
    <!-- Shut off Tab Groups automatically if there's only one -->
    <xsl:when test="count(/layout/navigation/tabGroupsList/tabGroup) &lt; 2">false</xsl:when>
    <xsl:otherwise><xsl:value-of select="$useTabGroups"/></xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="ACTIVE_TAB_GROUP" select="/layout/navigation/tabGroupsList/@activeTabGroup"/>
<!-- USE_SUBNAVIGATION_ROW
 | Sets the use of the sub navigation row, which lists out links to the portlets on the active tab.
 | Values are 'true' or 'false'
-->
<xsl:param name="USE_SUBNAVIGATION_ROW" select="false" />

  <!-- ========== TEMPLATE: NAVIGATION ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the main navigation construct.
   | This template can be rendered into the header or the sidebar, determined by the parameters set in respondr.xsl.
  -->
  <xsl:template match="navigation">
    <xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the navigation. -->
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
      <nav class="portal-nav">
        <div id="sidebar" class="sidebar-offcanvas container-fluid">
            <div id="portalNavigation" class="fl-widget" aria-selected="false">
                <!-- Optional Tab Groups -->
                <xsl:if test="$USE_TAB_GROUPS='true'">
                    <!--
                    <span id="activeTabGroup" style="display:none;"><xsl:value-of select="/layout/navigation/tabGroupsList/@activeTabGroup"/></span>
                    <span id="useTabGroup" style="display:none;"><xsl:value-of select="$USE_TAB_GROUPS"/></span>
                    <h3 class="fl-offScreen-hidden">Tab Groups</h3>
                    -->
                    <div id="portalNavigationTabGroup" class="fl-widget-inner header">
                        <!-- <span id="activeTabGroup" style="display:none;"><xsl:value-of select="/layout/navigation/tabGroupsList/@activeTabGroup"/></span> -->
                        <ul id="portalNavigationTabGroupsList" class="menu fl-tabs list-group list-group-horizontal">
                            <xsl:for-each select="tabGroupsList/tabGroup">
                                <xsl:variable name="TABGROUP_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
                                    <xsl:choose>
                                        <xsl:when test="position()=1 and position()=last()">single singleTabGroup</xsl:when>
                                        <xsl:when test="position()=1">first</xsl:when>
                                        <xsl:when test="position()=last()">last</xsl:when>
                                        <xsl:otherwise></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <xsl:variable name="TABGROUP_ACTIVE">
                                    <xsl:choose>
                                        <xsl:when test="/layout/navigation/tabGroupsList/@activeTabGroup=.">active</xsl:when>
                                        <xsl:otherwise></xsl:otherwise>
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
                                <li class="{$TABGROUP_POSITION} {$TABGROUP_ACTIVE} portal-navigation-tabgroup fl-tabs-active list-group-item portal-navigation">
                                    <a href="{$TABGROUP_URL}" title="{.}" class="portal-tabGroup-link portal-navigation-link">
                                        <span class="portal-tabGroup-label portal-navigation-label"><xsl:value-of select="$TABGROUP_LABEL"/></span></a>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </div>
                </xsl:if>
              <div id="portalNavigationInner" class="fl-widget-inner header">
                  <ul id="portalNavigationList" class="menu fl-tabs flc-reorderer-column list-group list-group-horizontal">
                     <xsl:apply-templates select="tab[$USE_TAB_GROUPS!='true' or @tabGroup=$ACTIVE_TAB_GROUP]">
                       <xsl:with-param name="CONTEXT">header</xsl:with-param>
                     </xsl:apply-templates>

                    <!-- invite the user to add a tab if permission to do so
                    and navigation element is flagged as allowing tab-adding -->
                     <xsl:if test="@allowAddTab = 'true' and upAuth:hasPermission('UP_SYSTEM', 'ADD_TAB', 'ALL') and not($PORTAL_VIEW='focused')">
                        <li class="portal-navigation-add-item list-group-item">
                            <a href="#" title="{upMsg:getMessage('add.tab', $USER_LANG)}" class="portal-navigation-add">
                              <i class="fa fa-plus-circle"></i>
                            </a>
                        </li>
                     </xsl:if>
                  </ul>
              </div>
            </div>
        </div>
      </nav>
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
        <xsl:when test="@activeTab='true'">active fl-tabs-active</xsl:when>
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
        <xsl:when test="not(@dlm:addChildAllowed='false')">canAddChildren</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_TRANSIENT">
      <xsl:choose>
        <xsl:when test="@transient='true'">disabled</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDITABLE"><!--Determine whether the activeTab is editable. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false') or $IS_FRAGMENT_ADMIN_MODE='true'">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true'">flc-inlineEditable</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDIT_TEXT"><!--Determine whether the activeTab is editable. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false') or $IS_FRAGMENT_ADMIN_MODE='true'">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true'">flc-inlineEdit-text</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="NAV_INLINE_EDIT_TITLE"><!--Determine whether the activeTab is editable. Class name is leveraged by the fluid inline editor component.-->
        <xsl:choose>
            <xsl:when test="$AUTHENTICATED='true'">
                <xsl:choose>
                    <xsl:when test="not(@dlm:editAllowed='false') or $IS_FRAGMENT_ADMIN_MODE='true'">
                        <xsl:choose>
                            <xsl:when test="@activeTab='true'">Click to edit tab name</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="USE_FLYOUT">
      <xsl:choose>
        <xsl:when test="$USE_FLYOUT_MENUS='true'">useflyout</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <li id="portalNavigation_{@ID}" class="portal-navigation {$NAV_POSITION} {$NAV_ACTIVE} {$NAV_MOVABLE} {$NAV_EDITABLE} {$NAV_DELETABLE} {$NAV_CAN_ADD_CHILDREN} {$FRAGMENT_OWNER_CSS} {$USE_FLYOUT} list-group-item"> <!-- Each navigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
      <xsl:variable name="tabLinkUrl">
        <!-- For transient tabs, don't try to calculate an URL.  It display an exception in the logs. Use a safe URL. -->
        <xsl:choose>
            <xsl:when test="@transient='true'">javascript:;</xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                        </url:portal-url>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <a id="tabLink_{@ID}" href="{$tabLinkUrl}" title="{@name}" class="portal-navigation-link {$NAV_INLINE_EDITABLE} {$NAV_TRANSIENT}">
        <span title="{$NAV_INLINE_EDIT_TITLE}" class="portal-navigation-label {$NAV_INLINE_EDIT_TEXT}">
          <xsl:value-of select="upElemTitle:getTitle(@ID, $USER_LANG, @name)"/>
        </span>
        <i class="fa fa-chevron-right visible-xs"></i>
      </a> <!-- Navigation item link. -->
      <xsl:if test="$AUTHENTICATED='true' and not($PORTAL_VIEW='focused')">
        <xsl:if test="not(@dlm:moveAllowed='false') or $IS_FRAGMENT_ADMIN_MODE='true'">
          <a href="#" class="nav-tab-controls portal-navigation-gripper {$NAV_ACTIVE}" title="{upMsg:getMessage('move.this.tab', $USER_LANG)}">
            <i class="fa fa-align-justify"></i>
            <span><xsl:value-of select="upMsg:getMessage('move', $USER_LANG)"/></span>
          </a> <!-- Drag & drop gripper handle. -->
        </xsl:if>
      </xsl:if>
      <xsl:if test="$AUTHENTICATED='true' and @activeTab='true' and $NAV_POSITION != 'single' and not($PORTAL_VIEW='focused')">
        <xsl:if test="not(@dlm:deleteAllowed='false') or $IS_FRAGMENT_ADMIN_MODE='true'">
          <a href="#" class="nav-tab-controls portal-navigation-delete" title="{upMsg:getMessage('remove.this.tab', $USER_LANG)}">
            <i class="fa fa-times"></i>
            <span><xsl:value-of select="upMsg:getMessage('remove', $USER_LANG)"/></span>
          </a><!-- Remove tab icon. -->
        </xsl:if>
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


  <!-- ========== TEMPLATE: SUBNAVIGATION ========== -->
  <!-- ============================================= -->
  <!--
   | This template renders subnavigation which may appear in different contexts.
   | The context parameter is used to know whether the subnavigation is a flyout menu, the expanded display of the selected sidebar navigation menu item, or as a separate navigation list of the page contents when using tabs.
   | These options are determined by the parameters set in respondr.xsl.
  -->
  <xsl:template name="subnavigation">
    <xsl:param name="CONTEXT"/>  <!-- Catches the context parameter to know how to render the subnavigation. -->
    <xsl:param name="TAB_POSITION"/> <!-- Provides the position of the tab -->

    <xsl:element name="a"> <!-- Navigation dropdown toogle. -->
      <xsl:attribute name="id">portalSubnavigationToggle_<xsl:value-of select="@ID"/></xsl:attribute>
      <xsl:attribute name="href">javascript:void(0);</xsl:attribute>
      <xsl:attribute name="class">dropdown-toggle portal-navigation-dropdown</xsl:attribute>
      <xsl:attribute name="data-toggle">dropdown</xsl:attribute>
      <xsl:attribute name="role">button</xsl:attribute>
      <xsl:attribute name="aria-expanded">false</xsl:attribute>
      <xsl:attribute name="aria-haspopup">true</xsl:attribute>
      <xsl:attribute name="aria-controls">portal-subnavigation_<xsl:value-of select="@ID"/></xsl:attribute>
      <xsl:attribute name="tabindex">0</xsl:attribute>
      <xsl:element name="span"> <!-- Navigation dropdown toogle caret -->
       <xsl:attribute name="class">caret</xsl:attribute>
      </xsl:element>
      <span class="sr-only">
       <xsl:value-of select="upMsg:getMessage('toggle.menu', $USER_LANG)"/>
      </span>
    </xsl:element>

    <ul class="dropdown-menu portal-subnav-list"> <!-- List of the subnavigation menu items. -->
      <xsl:attribute name="id">portal-subnavigation_<xsl:value-of select="@ID"/></xsl:attribute>
      <xsl:attribute name="role">menu</xsl:attribute>
      <xsl:for-each select="tabChannel">
        <xsl:variable name="SUBNAV_POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
          <xsl:choose>
            <xsl:when test="position()=1 and position()=last()">single</xsl:when>
            <xsl:when test="position()=1">first</xsl:when>
            <xsl:when test="position()=last()">last</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <li id="uPfname_{@fname}_{@ID}" class="portal-subnav {$SUBNAV_POSITION}" role="menuitem"> <!-- Each subnavigation menu item.  The really* unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. *(when the same fname portlet is in two different tabs) -->
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
          <xsl:element name="a"> <!-- Navigation item link. -->
            <xsl:attribute name="title"><xsl:value-of select="@description" /></xsl:attribute>
            <xsl:choose>
              <xsl:when test="@alternativeMaximizedLink and string-length(@alternativeMaximizedLink) > 0">
                <xsl:attribute name="href"><xsl:value-of select="@alternativeMaximizedLink" /></xsl:attribute>
                <xsl:attribute name="target">_blank</xsl:attribute>
                <xsl:attribute name="rel">noopener noreferrer</xsl:attribute>
                <xsl:attribute name="class">portal-subnav-link externalLink</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="href"><xsl:value-of select="$portletSubNavLink" /></xsl:attribute>
                <xsl:attribute name="class">portal-subnav-link</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            <span class="portal-subnav-label"><xsl:value-of select="@title"/></span>
          </xsl:element>
        </li>
      </xsl:for-each>
    </ul>
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


  <!-- ========== TEMPLATE: SKIPNAV ========== -->
  <!-- =================================================== -->
  <!--
   | This template renders the "Skip to page content" button at the top of the page.
  -->
  <xsl:template name="skipnav">
    <a class="portal-header-skip-nav btn" href="#portalPageBody"><xsl:value-of select="upMsg:getMessage('skip.to.page.content', $USER_LANG)"/></a>
  </xsl:template>
  <!-- =================================================== -->

</xsl:stylesheet>
