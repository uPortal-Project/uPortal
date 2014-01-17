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
 | This file defines areas or _Regions_ of the page in which non-tab/column
 | portlets may be placed.  Regions with portlets present must display them
 | properly;  regions without portlets must "disappear" gracefully.  ALL of the
 | essential page structure markup (related to regions) MUST be provided by the
 | regions themselves, not by rendered portlets.
 |
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
  xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
  xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
  xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
  xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
  xmlns:c="http://www.w3.org/1999/XSL/Transform"
  xsi:schemaLocation="
      https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
  exclude-result-prefixes="url upAuth upGroup upMsg dlm xsi"
  version="1.0">
  
<xsl:template name="normal.display">
  <html lang="{$USER_LANG}" class="respondr">
    <head>
      <xsl:call-template name="page.title" />
      <xsl:call-template name="page.meta" />
      <xsl:call-template name="skinResources">
        <xsl:with-param name="path" select="$SKIN_RESOURCES_PATH" />
      </xsl:call-template>
      <xsl:if test="$PORTAL_SHORTCUT_ICON != ''">
        <link rel="shortcut icon" href="{$PORTAL_SHORTCUT_ICON}" type="image/x-icon" />
      </xsl:if>
      <xsl:call-template name="page.js" />
      <xsl:call-template name="page.overrides" />
    </head>
    <body class="up dashboard portal fl-theme-mist">
      <div id="wrapper">
        <xsl:call-template name="region.page-top" />
        <header class="portal-header" role="banner">
          <div class="portal-global">
            <div class="container">
              <xsl:call-template name="region.pre-header" />
            </div>
          </div>
          <div class="container">
            <div class="row">
              <xsl:call-template name="region.header-left" />
              <xsl:call-template name="region.header-right" />
            </div>
          </div>
          <xsl:call-template name="region.header-bottom" />
          <xsl:apply-templates select="layout/navigation" />
        </header>
        <xsl:call-template name="region.customize" />
        <div id="portalPageBody" class="portal-content" role="main"><!-- #portalPageBody selector is used with BackgroundPreference framework portlet -->
          <xsl:call-template name="region.pre-content" />
          <div class="container">
            <!-- For editing page permissions in fragment-admin mode  -->
            <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
              <div class="row">
                <div class="col-md-9"></div>
                <div class="col-md-3">
                  <div id="portalEditPagePermissions" class="fl-fix">
                    <a class="button" id="editPagePermissionsLink" href="javascript:;" title="{upMsg:getMessage('edit.page.permissions', $USER_LANG)}">
                      <xsl:value-of select="upMsg:getMessage('edit.page.permissions', $USER_LANG)"/>
                    </a>
                  </div>
                </div>
              </div>
            </xsl:if>
            <!-- Works with up-layout-preferences.js showMessage()  -->
            <div class="row">
              <div id="portalPageBodyMessage" class="col-md-12"></div>
            </div>

            <xsl:choose>
              <xsl:when test="$PORTAL_VIEW='focused'">
                <!-- === FOCUSED VIEW === -->
                <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="layout/content" />
              </xsl:otherwise>
            </xsl:choose>
          </div>
        </div>
        <xsl:call-template name="footer.nav" />
        <xsl:call-template name="footer.legal" />
        <xsl:call-template name="region.page-bottom" />
      </div>

      <xsl:call-template name="page.dialogs" />

      <script type="text/javascript">
        up.analytics = up.analytics || {};
        up.analytics.host = '<xsl:value-of select="$HOST_NAME" />';
        up.analytics.portletData = <portlet-analytics-data/>;
        up.analytics.pageData = <page-analytics-data/>;
      </script>
    </body>
    <script type="text/javascript">
    up.jQuery(document).ready(function(){
      <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
      up.FragmentPermissionsManager(
      "body",
      {
        savePermissionsUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/layout',
        messages: {
        columnX: '<xsl:value-of select="upMsg:getMessage('column.x', $USER_LANG)"/>',
        }
      }
      );
      </xsl:if>
      up.LayoutPreferences(
      "body",
      {
        tabContext: '<xsl:value-of select="$TAB_CONTEXT"/>',
        numberOfPortlets: '<xsl:value-of select="count(content/column/channel)"/>',
        portalContext: '<xsl:value-of select="$CONTEXT_PATH"/>',
        mediaPath: '<xsl:value-of select="$ABSOLUTE_MEDIA_PATH"/>',
        currentSkin: '<xsl:value-of select="$SKIN"/>',
        subscriptionsSupported: '<xsl:value-of select="$subscriptionsSupported"/>',
        layoutPersistenceUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/layout',
        channelRegistryUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/portletList',
        subscribableTabUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/subscribableTabs.json',
        gallerySelector: null,  // Disable the gallery since the page doesn't include it (for now)
        messages: {
          confirmRemoveTab: '<xsl:value-of select="upMsg:getMessage('are.you.sure.remove.tab', $USER_LANG)"/>',
          confirmRemovePortlet: '<xsl:value-of select="upMsg:getMessage('are.you.sure.remove.portlet', $USER_LANG)"/>',
          addTabLabel: '<xsl:value-of select="upMsg:getMessage('my.tab', $USER_LANG)"/>',
          column: '<xsl:value-of select="upMsg:getMessage('column', $USER_LANG)"/>',
          columns: '<xsl:value-of select="upMsg:getMessage('columns', $USER_LANG)"/>',
          fullWidth: '<xsl:value-of select="upMsg:getMessage('full.width', $USER_LANG)"/>',
          narrowWide: '<xsl:value-of select="upMsg:getMessage('narrow.wide', $USER_LANG)"/>',
          even: '<xsl:value-of select="upMsg:getMessage('even', $USER_LANG)"/>',
          wideNarrow: '<xsl:value-of select="upMsg:getMessage('wide.narrow', $USER_LANG)"/>',
          narrowWideNarrow: '<xsl:value-of select="upMsg:getMessage('narrow.wide.narrow', $USER_LANG)"/>',
          searchForStuff: '<xsl:value-of select="upMsg:getMessage('search.for.stuff', $USER_LANG)"/>',
          allCategories: '<xsl:value-of select="upMsg:getMessage('all(categories)', $USER_LANG)"/>',
          persistenceError: '<xsl:value-of select="upMsg:getMessage('error.persisting.layout.change', $USER_LANG)"/>'
        }
      }
      );
     });
  </script>
  </html>
</xsl:template>

<xsl:template name="auxiliary.display">
  <html lang="{$USER_LANG}" class="respondr">
    <head>
      <xsl:call-template name="page.title" />
      <xsl:call-template name="page.meta" />
      <xsl:call-template name="skinResources">
        <xsl:with-param name="path" select="$SKIN_RESOURCES_PATH" />
      </xsl:call-template>
      <xsl:if test="$PORTAL_SHORTCUT_ICON != ''">
        <link rel="shortcut icon" href="{$PORTAL_SHORTCUT_ICON}" type="image/x-icon" />
      </xsl:if>
      <xsl:call-template name="page.js" />
      <xsl:call-template name="page.overrides" />
    </head>
    <body class="up dashboard portal fl-theme-mist">
      <div id="wrapper">
        <div class="portal-sticky-header">
        <header class="portal-header" role="banner">
          <div class="portal-global">
            <div class="container">
              <div id="region-pre-header" class="portal-user">
                <xsl:copy-of select="//channel[@fname = 'return-to-dashboard-launcher']"/>
                <xsl:copy-of select="//channel[@fname = 'portal-greeting']"/>
                <xsl:copy-of select="//channel[@fname = 'logout-launcher']"/>
              </div>
            </div>
          </div>
        </header>
        </div>
        <div class="portal-sticky-content" role="main">
          <div class="portal-sticky-container">
            <!-- Works with up-layout-preferences.js showMessage()  -->
            <div class="row">
              <div id="portalPageBodyMessage" class="col-md-12"></div>
            </div>

            <xsl:choose>
              <xsl:when test="$PORTAL_VIEW='focused'">
                <!-- === FOCUSED AUXILIARY VIEW === -->
                <!-- No Titlebar is desired, just copy portlet contents to the page -->
                <xsl:copy-of select="//content/focused/channel" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="layout/content" />
              </xsl:otherwise>
            </xsl:choose>
          </div>
        </div>
      </div>
      <script type="text/javascript">
        up.analytics = up.analytics || {};
        up.analytics.host = '<xsl:value-of select="$HOST_NAME" />';
        up.analytics.portletData = <portlet-analytics-data/>;
        up.analytics.pageData = <page-analytics-data/>;
      </script>
    </body>
    <script type="text/javascript">
    up.jQuery(document).ready(function(){
      <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
      up.FragmentPermissionsManager(
      "body",
      {
        savePermissionsUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/layout',
        messages: {
        columnX: '<xsl:value-of select="upMsg:getMessage('column.x', $USER_LANG)"/>',
        }
      }
      );
      </xsl:if>
      up.LayoutPreferences(
      "body",
      {
        tabContext: '<xsl:value-of select="$TAB_CONTEXT"/>',
        numberOfPortlets: '<xsl:value-of select="count(content/column/channel)"/>',
        portalContext: '<xsl:value-of select="$CONTEXT_PATH"/>',
        mediaPath: '<xsl:value-of select="$ABSOLUTE_MEDIA_PATH"/>',
        currentSkin: '<xsl:value-of select="$SKIN"/>',
        subscriptionsSupported: '<xsl:value-of select="$subscriptionsSupported"/>',
        layoutPersistenceUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/layout',
        channelRegistryUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/portletList',
        subscribableTabUrl: '<xsl:value-of select="$CONTEXT_PATH"/>/api/subscribableTabs.json',
        gallerySelector: null,  // Disable the gallery since the page doesn't include it (for now)
        messages: {
          confirmRemoveTab: '<xsl:value-of select="upMsg:getMessage('are.you.sure.remove.tab', $USER_LANG)"/>',
          confirmRemovePortlet: '<xsl:value-of select="upMsg:getMessage('are.you.sure.remove.portlet', $USER_LANG)"/>',
          addTabLabel: '<xsl:value-of select="upMsg:getMessage('my.tab', $USER_LANG)"/>',
          column: '<xsl:value-of select="upMsg:getMessage('column', $USER_LANG)"/>',
          columns: '<xsl:value-of select="upMsg:getMessage('columns', $USER_LANG)"/>',
          fullWidth: '<xsl:value-of select="upMsg:getMessage('full.width', $USER_LANG)"/>',
          narrowWide: '<xsl:value-of select="upMsg:getMessage('narrow.wide', $USER_LANG)"/>',
          even: '<xsl:value-of select="upMsg:getMessage('even', $USER_LANG)"/>',
          wideNarrow: '<xsl:value-of select="upMsg:getMessage('wide.narrow', $USER_LANG)"/>',
          narrowWideNarrow: '<xsl:value-of select="upMsg:getMessage('narrow.wide.narrow', $USER_LANG)"/>',
          searchForStuff: '<xsl:value-of select="upMsg:getMessage('search.for.stuff', $USER_LANG)"/>',
          allCategories: '<xsl:value-of select="upMsg:getMessage('all(categories)', $USER_LANG)"/>',
          persistenceError: '<xsl:value-of select="upMsg:getMessage('error.persisting.layout.change', $USER_LANG)"/>'
        }
      }
      );
     });
  </script>
  </html>
</xsl:template>

</xsl:stylesheet>