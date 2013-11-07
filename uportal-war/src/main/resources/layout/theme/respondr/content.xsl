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
 | This file determines the presentation of rows and portlet containers.
 | Portlet content is rendered outside of the theme, handled entirely by the portlet itself.
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
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg dlm xsi" 
    version="1.0">

  <!-- ========== TEMPLATE: CONTENT ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the main content of the page (stuff organized into rows).
  -->
  <xsl:template match="content">
    <!-- Handles dashboard mode -->
    <xsl:for-each select="column">
      <xsl:call-template name="row" /> <!-- A bit wordy, but prefer to use a template named row over one with match=column for clarity. -->
    </xsl:for-each>
    <!-- Handles focused mode -->
    <xsl:apply-templates select="focused/channel">
      <xsl:with-param name="WIDTH_CSS_CLASS">col-md-12</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ========== TEMPLATE: TAB ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders a tab.
  -->
  <xsl:template name="row">
    <div class="row">
      <!-- Tests for optional portlet parameter removeFromLayout which can be used to not render a portlet in the layout.  The main use case for this function is to have a portlet be a quicklink and then remove it from otherwise rendering. -->
      <!-- xsl:apply-templates select="channel|blocked-channel and not(parameter[@name='removeFromLayout']/@value='true') and not(parameter[@name='PORTLET.removeFromLayout']/@value='true')" -->
      <xsl:apply-templates select="channel|blocked-channel">
          <xsl:with-param name="WIDTH_CSS_CLASS">
            <!-- NOTE:  This implementation assumes equal widths in a row of portlets;  we need to introduce a system to choose varying widths. -->
            <!-- NOTE:  This implementation also assumes that the actual number of portlets in the row can be evenly divided by 12;  works for 1, 2, 3, and 4;  need to enforce that or change this implementation. -->
            <xsl:value-of select="concat('col-md-', string(12 div count(channel)))" />
          </xsl:with-param>
      </xsl:apply-templates>
    </div>
  </xsl:template>

  <!-- ========== TEMPLATE: PORTLET ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the portlet containers: chrome and controls.
  -->
  <xsl:template match="channel|blocked-channel">
    <xsl:param name="WIDTH_CSS_CLASS"></xsl:param>

    <xsl:variable name="PORTLET_LOCKED"> <!-- Test to determine if the portlet is locked in the layout. -->
      <xsl:choose> 
        <xsl:when test="@dlm:moveAllowed='false'">locked</xsl:when> 
        <xsl:otherwise>movable</xsl:otherwise> 
      </xsl:choose> 
    </xsl:variable>

    <xsl:variable name="DELETABLE">
      <xsl:choose>
        <xsl:when test="not(@dlm:deleteAllowed='false')">deletable</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_CHROME"> <!-- Test to determine if the portlet has been given the highlight flag. -->
      <xsl:choose>
        <xsl:when test="./parameter[@name='showChrome']/@value='false'">no-chrome</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_HIGHLIGHT"> <!-- Test to determine if the portlet has been given the highlight flag. -->
      <xsl:choose>
        <xsl:when test="./parameter[@name='highlight']/@value='true'">highlight</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_ALTERNATE"> <!-- Test to determine if the portlet has been given the alternate flag. -->
      <xsl:choose>
        <xsl:when test="./parameter[@name='alternate']/@value='true'">alternate</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- ****** PORTLET CONTAINER ****** -->
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
    <div class="{$WIDTH_CSS_CLASS}">
    <section id="portlet_{@ID}" class="{@fname} {$PORTLET_LOCKED} {$DELETABLE} {$PORTLET_CHROME} {$PORTLET_ALTERNATE} {$PORTLET_HIGHLIGHT}"> <!-- Main portlet container.  The unique ID is needed for drag and drop.  The portlet fname is also written into the class attribute to allow for unique rendering of the portlet presentation. -->

        <!-- PORTLET CHROME CHOICE -->
        <xsl:choose>
          <!-- ***** REMOVE CHROME ***** -->
          <xsl:when test="parameter[@name = 'showChrome']/@value = 'false'">
              <!-- ****** START: PORTLET CONTENT ****** -->
              <div id="portletContent_{@ID}" class="up-portlet-content-wrapper"> <!-- Portlet content container. -->
                <div class="up-portlet-content-wrapper-inner">  <!-- Inner div for additional presentation/formatting options. -->
                  <xsl:call-template name="portlet-content"/>
                </div>
              </div>
          </xsl:when>

          <!-- ***** RENDER CHROME ***** -->
          <xsl:otherwise>
            <div class="up-portlet-wrapper-inner">

            <!-- ****** PORTLET TITLE AND TOOLBAR ****** -->
            <div id="toolbar_{@ID}" class="fl-widget-titlebar up-portlet-titlebar round-top"> <!-- Portlet toolbar. -->
              <!--
              Portlet Title
              -->
              <h2 class="portlet-title round-top">
                <xsl:variable name="portletMaxUrl">
                  <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                            <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                        </url:portal-url>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:variable>
                <!-- Reference anchor for page focus on refresh and link to focused view of channel. -->
                <a id="{@ID}" href="{$portletMaxUrl}"> 
                  {up-portlet-title(<xsl:value-of select="@ID" />)}
                </a>

                <xsl:call-template name="controls"/>
              </h2>
            </div>

            <!-- ****** PORTLET CONTENT ****** -->
            <div id="portletContent_{@ID}" class="fl-widget-content fl-fix up-portlet-content-wrapper round-bottom"> <!-- Portlet content container. -->
              <div class="up-portlet-content-wrapper-inner">  <!-- Inner div for additional presentation/formatting options. -->
                <xsl:call-template name="portlet-content"/>
              </div>
            </div>

            </div>
          </xsl:otherwise>
        </xsl:choose>

    </section>
    </div>
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->

  </xsl:template>
  <!-- ======================================= -->
  
  <!-- ========== TEMPLATE: PORLET CONTENT ========== -->
  <!-- ============================================== -->
  <!--
   | Renders the actual portlet content
  -->
  <xsl:template name="portlet-content">
    <xsl:choose>
        <xsl:when test="name() = 'blocked-channel'">
            <xsl:choose>
                <xsl:when test="parameter[@name='blockImpersonation']/@value = 'true'">
                    <div><p><em><xsl:value-of select="upMsg:getMessage('hidden.in.impersonation.view', $USER_LANG)"/></em></p></div>
                </xsl:when>
                <xsl:otherwise>
                    <div><p><em><xsl:value-of select="upMsg:getMessage('channel.blocked', $USER_LANG)"/></em></p></div>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ========== TEMPLATE: PORLET FOCUSED ========== -->
  <!-- ============================================== -->
  <!--
   | These two templates render the focused portlet content.
  -->
  <xsl:template match="focused">
    <div id="portalPageBodyColumns" class="columns-1">
        <div class="portal-page-column column-1">
            <div class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
            <xsl:apply-templates select="channel|blocked-channel"/>
          </div>
        </div>
    </div>
  </xsl:template>
  <!-- ============================================== -->
  
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->

  <!-- This template renders portlet controls.  Each control has a unique class for assigning icons or other specific presentation. -->
  <xsl:template name="controls">
    <div class="btn-group">
      <a class="btn btn-link dropdown-toggle" data-toggle="dropdown" href="#">Options <span class="caret"></span></a>
      <ul class="dropdown-menu">
    <!--
      Porlet Controls Display Order:
      help, remove, maximize, minimize, info, print, settings, ...
    -->
      <xsl:variable name="hasHelp">
          <xsl:if test="parameter[@name='hasHelp'] and parameter[@name='hasHelp']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="hasAbout">
          <xsl:if test="parameter[@name='hasAbout'] and parameter[@name='hasAbout']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="editable">
          <xsl:if test="parameter[@name='editable'] and parameter[@name='editable']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="printable">
          <xsl:if test="parameter[@name='printable'] and parameter[@name='printable']/@value = 'true'">true</xsl:if>
      </xsl:variable>

      <!-- Help Icon -->
      <xsl:if test="$hasHelp='true'">
        <xsl:variable name="portletHelpUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="HELP" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletHelpUrl}#{@ID}" title="{upMsg:getMessage('view.help.for.portlet', $USER_LANG)}" class="up-portlet-control help"><xsl:value-of select="upMsg:getMessage('help', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Remove Icon -->
      <xsl:if test="not(@dlm:deleteAllowed='false') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
        <xsl:variable name="removePortletUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url type="action">
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:param name="remove_target" value="{@ID}"/>
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a id="removePortlet_{@ID}" title="{upMsg:getMessage('are.you.sure.remove.portlet', $USER_LANG)}" href="{$removePortletUrl}" class="up-portlet-control remove"><xsl:value-of select="upMsg:getMessage('remove', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Focus Icon -->
      <xsl:if test="not(//focused) and not(//layout_fragment) and @windowState!='minimized'">
        <xsl:variable name="portletMaxUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletMaxUrl}" title="{upMsg:getMessage('enter.maximized.mode.for.this.portlet', $USER_LANG)}" class="up-portlet-control focus"><xsl:value-of select="upMsg:getMessage('maximize', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Return from Focused Icon -->
      <xsl:if test="//focused">
        <xsl:variable name="portletReturnUrl">
          <xsl:choose>
            <xsl:when test="@transient='true'">
              <xsl:call-template name="portalUrl" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                  <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url state="NORMAL" copyCurrentRenderParameters="true" />
                  </url:portal-url>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <li>
          <a href="{$portletReturnUrl}" title="{upMsg:getMessage('return.to.dashboard.view', $USER_LANG)}" class="up-portlet-control return"><xsl:value-of select="upMsg:getMessage('return.to.dashboard', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <xsl:if test="$USE_PORTLET_MINIMIZE_CONTENT='true'">
          <xsl:if test="not(//focused) and not(//layout_fragment)">
            <xsl:choose>
              <!-- Return from Minimized. -->
              <xsl:when test="@windowState='minimized'">
                <xsl:variable name="portletReturnUrl">
                  <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                            <url:portlet-url state="NORMAL" copyCurrentRenderParameters="true" />
                        </url:portal-url>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:variable>
                <li>
                  <a href="{$portletReturnUrl}" title="{upMsg:getMessage('return.to.dashboard.view', $USER_LANG)}" class="up-portlet-control show-content"><xsl:value-of select="upMsg:getMessage('return.to.dashboard', $USER_LANG)"/></a>
                </li>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="portletMinUrl">
                  <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                            <url:portlet-url state="MINIMIZED" copyCurrentRenderParameters="true" />
                        </url:portal-url>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:variable>
                <li>
                  <a href="{$portletMinUrl}" title="{upMsg:getMessage('enter.minimized.mode.for.this.portlet', $USER_LANG)}" class="up-portlet-control hide-content"><xsl:value-of select="upMsg:getMessage('minimize', $USER_LANG)"/></a>
                </li>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
      </xsl:if>

      <!-- About Icon -->
      <xsl:if test="$hasAbout='true'">
        <xsl:variable name="portletAboutUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="ABOUT" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletAboutUrl}#{@ID}" title="{upMsg:getMessage('view.information.about.portlet', $USER_LANG)}" class="up-portlet-control about"><xsl:value-of select="upMsg:getMessage('view.information.about.portlet', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Edit Icon -->
      <xsl:if test="$editable='true'">
        <xsl:variable name="portletEditUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="EDIT" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletEditUrl}#{@ID}" title="{upMsg:getMessage('edit.portlet', $USER_LANG)}" class="up-portlet-control edit"><xsl:value-of select="upMsg:getMessage('edit', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Print Icon -->
      <xsl:if test="$printable='true'">
        <xsl:variable name="portletPrintUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="PRINT" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletPrintUrl}#{@ID}" title="{upMsg:getMessage('print.portlet', $USER_LANG)}" class="up-portlet-control print"><xsl:value-of select="upMsg:getMessage('print', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Add to Layout Icon -->
      <xsl:if test="//focused[@in-user-layout='no'] and upGroup:isChannelDeepMemberOf(//focused/channel/@fname, 'local.1')"> <!-- Add to layout. -->
        <a id="focusedContentDialogLink" href="javascript:;" title="{upMsg:getMessage('add.this.portlet.to.my.layout', $USER_LANG)}" class="up-portlet-control add">
            <xsl:if test="$USE_PORTLET_CONTROL_ICONS='true'">
                <span class="icon"></span>
            </xsl:if>
            <span class="label"><xsl:value-of select="upMsg:getMessage('add.to.my.layout', $USER_LANG)"/></span>
        </a>
      </xsl:if>
        <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
          <li>
            <a class="up-portlet-control permissions portlet-permissions-link" href="javascript:;" title="{upMsg:getMessage('edit.permissions.for.this.portlet', $USER_LANG)}"><xsl:value-of select="upMsg:getMessage('edit.permissions', $USER_LANG)"/></a>
          </li>
        </xsl:if>
        </ul>
    </div>
  </xsl:template>
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->

  <!-- ========== TEMPLATE: WELCOME ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders dynamic welcome message
  -->
  <xsl:template name="welcome">
    <xsl:if test="$USER_ID!='guest'">
      <xsl:copy-of select="//channel/parameter[@name = 'role' and @value = 'welcomeMessage']/parent::*"/>
    </xsl:if>
  </xsl:template>
  <!-- ======================================= -->
</xsl:stylesheet>
