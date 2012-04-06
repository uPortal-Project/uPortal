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
 | This file determines the base page layout and presentation of the portal.
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
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg" 
    version="1.0">
  
  <!-- ========== TEMPLATE: PAGE ========== -->
  <!-- ==================================== -->
  <!-- 
   | This template defines the base HTML definitions for the output document.
   | This template defines the base page of both the default portal page view (xml: layout) and the view when a portlet has been detached (xml: layout_fragment).
   | The main layout of the page has three subsections: header (TEMPLATE: PAGE HEADER), content (TEMPLATE: PAGE BODY), and footer (TEMPLATE: PAGE FOOTER), defined below.
  -->
  <xsl:template match="layout | layout_fragment">
  	<xsl:variable name="COUNT_PORTLET_COLUMNS">
    	<xsl:choose>
      	<xsl:when test="$PORTAL_VIEW='focused'">1</xsl:when>
        <xsl:otherwise>
        	<xsl:value-of select="count(content/column)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="PAGE_COLUMN_CLASS">layout-<xsl:value-of select="$COUNT_PORTLET_COLUMNS"/>-columns</xsl:variable>
    <xsl:variable name="SIDEBAR_CLASS">
      <xsl:choose>
        <xsl:when test="$USE_SIDEBAR='true' and $AUTHENTICATED='true' and not(//focused)">sidebar sidebar-<xsl:value-of select="$SIDEBAR_LOCATION" />-<xsl:value-of select="$SIDEBAR_WIDTH" /></xsl:when>
        <xsl:when test="$USE_SIDEBAR_GUEST='true' and not($AUTHENTICATED='true')">sidebar sidebar-<xsl:value-of select="$SIDEBAR_LOCATION_GUEST" />-<xsl:value-of select="$SIDEBAR_WIDTH_GUEST" /></xsl:when>
        <xsl:when test="$USE_SIDEBAR_FOCUSED='true' and //focused">sidebar sidebar-<xsl:value-of select="$SIDEBAR_LOCATION_FOCUSED" />-<xsl:value-of select="$SIDEBAR_WIDTH_FOCUSED" /></xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="FRAGMENT_ADMIN_CLASS">
    	<xsl:choose>
        <xsl:when test="//channel[@fname = 'fragment-admin-exit']">fragment-admin-mode</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <title>
          <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
          <xsl:choose>
            <xsl:when test="//focused">
            	<xsl:value-of select="upMsg:getMessage('portal.name', $USER_LANG)" />: {up-portlet-title(<xsl:value-of select="//focused/channel/@ID" />)}
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="upMsg:getMessage('portal.name', $USER_LANG)" />: <xsl:value-of select="/layout/navigation/tab[@activeTab='true']/@name"/>
            </xsl:otherwise>
          </xsl:choose>
          <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
        </title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <xsl:if test="/layout_fragment">
        	<meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT" />
        	<meta http-equiv="pragma" content="no-cache" />
        </xsl:if>
        <meta name="description" content="{upMsg:getMessage('portal.page.meta.description', $USER_LANG)}" />
        <meta name="keywords" content="{upMsg:getMessage('portal.page.meta.keywords', $USER_LANG)}" />
        <xsl:if test="$PORTAL_SHORTCUT_ICON != ''">
        	<link rel="shortcut icon" href="{$PORTAL_SHORTCUT_ICON}" type="image/x-icon" />
        </xsl:if>
        
        <xsl:call-template name="skinResources">
            <xsl:with-param name="path" select="$SKIN_RESOURCES_PATH" />
        </xsl:call-template>
        <script type="text/javascript">
            var up = up || {};
            up.jQuery = jQuery.noConflict(true);
            up.fluid = fluid;
            fluid = null;
            fluid_1_4 = null;
        </script>
        <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
        <xsl:for-each select="/layout/header/channel-header">
        	 <xsl:copy-of select="."/>
        </xsl:for-each>
       
      </head>
      
      <body id="portal" class="up {$FLUID_THEME_CLASS}">
        <div id="portalPage" class="{$LOGIN_STATE} {$PORTAL_VIEW} fl-container-flex">  <!-- Main div for presentation/formatting options. -->
        	<div id="portalPageInner" class="{$PAGE_COLUMN_CLASS} {$SIDEBAR_CLASS} {$FRAGMENT_ADMIN_CLASS}">  <!-- Inner div for additional presentation/formatting options. -->
            <xsl:choose>
              <xsl:when test="/layout_fragment"> <!-- When detached. -->
              
                <xsl:for-each select="content//channel">
                  <xsl:apply-templates select=".">
                  	<xsl:with-param name="detachedContent" select="'true'"/>
                  </xsl:apply-templates>
                </xsl:for-each>
              
              </xsl:when>
              <xsl:otherwise> <!-- Otherwise, default. -->
				
                <xsl:call-template name="alert.block"/>			
                <xsl:apply-templates select="header"/>
                <xsl:call-template name="main.navigation"/>
                <xsl:if test="not(//focused)">
                    <xsl:call-template name="gallery"/>
                </xsl:if>
                <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
                    <div id="portalEditPagePermissions" class="fl-fix">
                    	<a class="button" id="editPagePermissionsLink" href="javascript:;" title="{upMsg:getMessage('edit.page.permissions', $USER_LANG)}">
                            <xsl:value-of select="upMsg:getMessage('edit.page.permissions', $USER_LANG)"/>
                        </a>
                    </div>
                </xsl:if>
                <xsl:apply-templates select="content"/>
                <xsl:apply-templates select="footer"/>
                <xsl:if test="$USE_FLYOUT_MENUS='true'">
                  <xsl:call-template name="flyout.menu.scripts"/> <!-- If flyout menus are enabled, writes in necessary Javascript to function. -->
                </xsl:if>
                <xsl:if test="($USE_AJAX='true' and $AUTHENTICATED='true')">
                  <xsl:call-template name="preferences"/>
                </xsl:if>
                <xsl:if test="$SIDEBAR_CLASS"> <!-- Script to fix content height when a sidebar is present. -->
                    <xsl:call-template name="js.content.height"/>
                </xsl:if>
                
              </xsl:otherwise>
            </xsl:choose>
          </div> 
        </div>
      </body>
    </html>
  </xsl:template>
  <!-- ==================================== -->
  
	
  <!-- ========== TEMPLATE: PAGE HEADER ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page header.
  -->
  <xsl:template match="header">
    <div id="portalPageHeader" class="fl-container-flex">  <!-- Div for presentation/formatting options. -->
    	<div id="portalPageHeaderInner">  <!-- Inner div for additional presentation/formatting options. -->
    
	        <xsl:choose>
	          <xsl:when test="$AUTHENTICATED != 'true'">
	            <!-- ****** HEADER GUEST BLOCK ****** -->
	            <xsl:call-template name="header.guest.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
	            <!-- ****** HEADER GUEST BLOCK ****** -->
	          </xsl:when>
	          <xsl:otherwise>
	          	<xsl:choose>
		          <xsl:when test="//focused">
		            <!-- ****** HEADER FOCUSED BLOCK ****** -->
		            <xsl:call-template name="header.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
		            <!-- ****** HEADER FOCUSED BLOCK ****** -->
		          </xsl:when>   
		          <xsl:otherwise>
		            <!-- ****** HEADER BLOCK ****** -->
		            <xsl:call-template name="header.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
		            <!-- ****** HEADER BLOCK ****** -->
		          </xsl:otherwise>
		        </xsl:choose>
	          </xsl:otherwise>
	        </xsl:choose>

    	</div>
    </div>
  </xsl:template>
  <!-- =========================================== -->
  
	
  <!-- ========== TEMPLATE: NAVIGATION ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page navigation.
  -->
  <xsl:template name="main.navigation">
    <!-- ****** MAIN NAVIGATION BLOCK ****** -->
    <xsl:call-template name="main.navigation.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
    <!-- ****** MAIN NAVIGATION BLOCK ****** -->
  </xsl:template>
  <!-- =========================================== -->
	
  
  <!-- ========== TEMPLATE: PAGE BODY ========== -->
  <!-- ========================================= -->
  <!--
   | This template renders the content section of the page in the form of columns of portlets.
   | A sidebar column may be enabled by setting the "USE_SIDEBAR" paramater (see VARIABLES & PARAMETERS in universality.xsl) to "true".
   | This sidebar currently cannot contain portlets or channels, but only a separate set of user interface components.
  -->
  <xsl:template match="content">
    <xsl:variable name="COLUMNS">
    	<xsl:choose>
      	<xsl:when test="$PORTAL_VIEW='focused'">1</xsl:when>
        <xsl:otherwise><xsl:value-of select="count(column)"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div id="portalPageBody" class="fl-container-flex fl-fix">  <!-- Div for presentation/formatting options. -->
    	<div id="portalPageBodyInner">  <!-- Inner div for additional presentation/formatting options. -->
      
        <!-- ****** BODY LAYOUT ****** -->
        <div id="portalPageBodyLayout">
          <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
        	<xsl:attribute name="class"> <!-- Write appropriate FSS class based on use of sidebar and number of columns to produce column layout. -->
          	<xsl:choose>
            	<xsl:when test="$AUTHENTICATED='true'"> <!-- Logged in -->
              	<xsl:choose>
                  <xsl:when test="$PORTAL_VIEW='focused'"> <!-- Focused View -->
                    <xsl:choose>
                      <xsl:when test="$USE_SIDEBAR_FOCUSED='true'">fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH_FOCUSED" /></xsl:when>
                      <xsl:otherwise>fl-container-flex</xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise> <!-- Dashboard View -->
                    <xsl:choose>
                      <xsl:when test="$USE_SIDEBAR='true'">fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH" /></xsl:when>
                      <xsl:otherwise>fl-container-flex</xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>

              </xsl:when>
              <xsl:otherwise> <!-- Guest View -->
              
                <xsl:choose>
                  <xsl:when test="$USE_SIDEBAR_GUEST='true'">fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH_GUEST" /></xsl:when>
                  <xsl:otherwise>fl-container-flex</xsl:otherwise>
                </xsl:choose>
                
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          
          <!-- ****** SIDEBAR, PAGE TITLE, & COLUMNS ****** -->
          <!-- Useage of the sidebar and subsequent UI components are set by parameters in universality.xsl. -->
          <xsl:variable name="FSS_SIDEBAR_LOCATION_CLASS">
            <xsl:call-template name="sidebar.location" /> <!-- Template located below. -->
          </xsl:variable>
          
          <xsl:choose>
            <xsl:when test="$PORTAL_VIEW='focused'">
            
              <!-- === FOCUSED VIEW === -->
              <xsl:choose>
                <xsl:when test="$USE_SIDEBAR_FOCUSED='true'"> <!-- Sidebar. -->
                  <xsl:call-template name="sidebar"/> <!-- Template located in columns.xsl. -->
                  <div class="fl-container-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                  	<!-- ****** CONTENT TOP BLOCK ****** -->
                    <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                    <!-- ****** CONTENT TOP BLOCK ****** -->
                    <xsl:call-template name="page.title.row.focused"/> <!-- Template located below. -->
                    <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
                    <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                    <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                    <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                  </div>
                </xsl:when>
                <xsl:otherwise>
                	<!-- ****** CONTENT TOP BLOCK ****** -->
                  <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                  <!-- ****** CONTENT TOP BLOCK ****** -->
                  <xsl:call-template name="page.title.row.focused"/> <!-- No Sidebar. Template located below. -->
                  <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
                  <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                  <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                  <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                </xsl:otherwise>
              </xsl:choose>
              
            </xsl:when>
            <xsl:otherwise>
            
              <!-- === DASHBOARD VIEW === -->
              <xsl:choose>
                <xsl:when test="$AUTHENTICATED='true'">
                  
                  <!-- Signed In -->
                  <xsl:choose>
                    <xsl:when test="$USE_SIDEBAR='true'"> <!-- Sidebar. -->
                      <xsl:call-template name="sidebar"/> <!-- Template located in columns.xsl. -->
                      <div class="fl-container-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                      	<!-- ****** CONTENT TOP BLOCK ****** -->
                        <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                        <!-- ****** CONTENT TOP BLOCK ****** -->
                        <xsl:call-template name="page.customize.row"/>
                        <xsl:call-template name="page.title.row"/>
                        <xsl:call-template name="columns"> <!-- Template located in columns.xsl. -->
                            <xsl:with-param name="COLUMNS" select="$COLUMNS" />
                        </xsl:call-template>
                        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                        <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                      </div>
                    </xsl:when>
                    <xsl:otherwise>
                    	<!-- ****** CONTENT TOP BLOCK ****** -->
                      <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                      <!-- ****** CONTENT TOP BLOCK ****** -->
                      <xsl:call-template name="page.customize.row"/>
                      <xsl:call-template name="page.title.row"/> <!-- No Sidebar. Template located below. -->
                      <xsl:call-template name="columns"> <!-- Template located in columns.xsl. -->
                        <xsl:with-param name="COLUMNS" select="$COLUMNS" />
                      </xsl:call-template>
                      <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                      <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                      <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                    </xsl:otherwise>
                  </xsl:choose>
                  
                </xsl:when>
                <xsl:otherwise>
                  
                  <!-- Signed Out -->
                  <xsl:choose>
                    <xsl:when test="$USE_SIDEBAR_GUEST='true'"> <!-- Sidebar. -->
                      <xsl:call-template name="sidebar"/> <!-- Template located in columns.xsl. -->
                      <div class="fl-container-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                      	<!-- ****** CONTENT TOP BLOCK ****** -->
                        <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                        <!-- ****** CONTENT TOP BLOCK ****** -->
                        <xsl:call-template name="page.title.row"/>
                        <xsl:call-template name="columns"> <!-- Template located in columns.xsl. -->
                            <xsl:with-param name="COLUMNS" select="$COLUMNS" />
                        </xsl:call-template>
                        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                        <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                      </div>
                    </xsl:when>
                    <xsl:otherwise>
                    	<!-- ****** CONTENT TOP BLOCK ****** -->
                      <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                      <!-- ****** CONTENT TOP BLOCK ****** -->
                      <xsl:call-template name="page.title.row"/> <!-- No Sidebar. Template located below. -->
                      <xsl:call-template name="columns"> <!-- Template located in columns.xsl. -->
                        <xsl:with-param name="COLUMNS" select="$COLUMNS" />
                      </xsl:call-template>
                      <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                      <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                      <!-- ****** CONTENT BOTTOM BLOCK ****** -->
                    </xsl:otherwise>
                  </xsl:choose>
                  
                </xsl:otherwise>
              </xsl:choose>
              
            </xsl:otherwise>
          </xsl:choose>

          <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
      	</div> <!-- End portalPageBodyLayout -->
        
    	</div> <!-- End portalPageBodyInner -->
    </div> <!-- End portalPageBody -->
    
  </xsl:template>
  <!-- ========================================= -->

  <!-- ======= TEMPLATE: CUSTOMIZE MESSAGE ======= -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the customize page message.
  -->
  <xsl:template name="page.customize.row">
    <xsl:if test="count(column/channel) = 0">
        <div id="portalPageBodyCustomizeMessageRow">
            <div id="portalPageBodyCustomizeMessageRowContents">
                <xsl:call-template name="content.customize.message.block"/>
            </div>
        </div>
    </xsl:if>
  </xsl:template>
  <!-- =========================================== -->

  <!-- ========== TEMPLATE: PAGE TITLE ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page title.
  -->
  <xsl:template name="page.title.row">
		<div id="portalPageBodyTitleRow"> <!-- This row contains the page title (label of the currently selected main navigation item), and optionally user layout customization hooks, custom institution content (blocks), or return to dashboard link (if in the focused view). -->
      <div id="portalPageBodyTitleRowContents"> <!-- Inner div for additional presentation/formatting options. -->
        <!-- ****** CONTENT TITLE BLOCK ****** -->
        <xsl:call-template name="content.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** CONTENT TITLE BLOCK ****** -->
      </div>
    </div>
  </xsl:template>
  <!-- =========================================== -->
  
  
  <!-- ========== TEMPLATE: PAGE TITLE FOCUSED ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page title when focused.
  -->
  <xsl:template name="page.title.row.focused">
		<div id="portalPageBodyTitleRow"> <!-- This row contains the page title (label of the currently selected main navigation item), and optionally user layout customization hooks, custom institution content (blocks), or return to dashboard link (if in the focused view). -->
      <div id="portalPageBodyTitleRowContents"> <!-- Inner div for additional presentation/formatting options. -->
        <!-- ****** CONTENT TITLE FOCUSED BLOCK ****** -->
        <xsl:call-template name="content.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** CONTENT TITLE FOCUSED BLOCK ****** -->
      </div>
    </div>
  </xsl:template>
  <!-- =========================================== -->
  
  
  <!-- ========== TEMPLATE: PAGE FOOTER ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page footer.
   | The footer channel is located at: webpages\stylesheets\org\jasig\portal\channels\CGenericXSLT\footer\footer_webbrowser.xsl.
  -->
  <xsl:template match="footer">
    <div id="portalPageFooter" class="fl-container-flex">
    	<div id="portalPageFooterInner">
      
        <!-- ****** FOOTER BLOCK ****** -->
        <xsl:call-template name="footer.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** FOOTER BLOCK ****** -->
      
      </div>
    </div>
  </xsl:template>
  <!-- =========================================== -->


  <!-- ========== TEMPLATE: SIDEBAR LOCATION ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders the FSS class appropraite to the sidebar location.
  -->
  <xsl:template name="sidebar.location">
    <xsl:choose>
      <xsl:when test="$AUTHENTICATED='true'">
        <xsl:choose>
          <xsl:when test="$PORTAL_VIEW='focused'">
            <xsl:value-of select="$SIDEBAR_LOCATION_FOCUSED"/> <!-- location when focused -->
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$SIDEBAR_LOCATION"/> <!-- location when dashboard -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$SIDEBAR_LOCATION_GUEST"/> <!-- location when logged out -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ============================================ --> 
    
  
  <!-- ==================================================== -->
  <!-- ========== TEMPLATE: JAVASCRIPT CONTENT HEIGHT ========== -->
  <!-- ==================================================== -->
  <!-- 
   | YELLOW
   | This template outputs a script to ensure that when a sidebar is used, all columns of the content contaier are the same height (fixes collapsed content area due to floated sidebar, which sometimes has a greater height). The issue only occurs when there is a floated sidebar, so the script is set to run only when a sidebar is present.
  -->
  <xsl:template name="js.content.height">
		<script type="text/javascript" language="javascript">
            // Run function when document is ready (fully loaded).
            up.jQuery(document).ready(function(){
                // set function for determining and setting height.
                contentHeight = function() {
                    // delcare and set vars.
                    var contentLoc, sidebarLoc, contentHt, sidebarHt;
                    var contentLoc = up.jQuery('#portalPageBodyLayout');
                    var sidebarLoc = up.jQuery('#portalSidebarInner');
                    var contentHt = contentLoc.height();
                    var sidebarHt = sidebarLoc.height();
                    // compare and set height.
                    if (contentHt > sidebarHt) {
                        sidebarLoc.height(contentHt);
                    }
                }
				contentHeight();
            });
        </script>
  </xsl:template>
  <!-- ==================================================== -->

</xsl:stylesheet>
