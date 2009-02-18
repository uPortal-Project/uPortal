<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!-- 
 | This file determines the base page layout and presentation of the portal.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  
  <!-- ========== TEMPLATE: PAGE ========== -->
  <!-- ==================================== -->
  <!-- 
   | This template defines the base HTML definitions for the output document.
   | This template defines the base page of both the default portal page view (xml: layout) and the view when a portlet has been detached (xml: layout_fragment).
   | The main layout of the page has three subsections: header (TEMPLATE: PAGE HEADER), content (TEMPLATE: PAGE BODY), and footer (TEMPLATE: PAGE FOOTER), defined below.
  -->
  <xsl:template match="layout | layout_fragment">
  	<xsl:variable name="COUNT_PORTLET_COLUMNS" select="count(content/column)"/>
    <xsl:variable name="PAGE_COLUMN_CLASS">layout-<xsl:value-of select="$COUNT_PORTLET_COLUMNS"/>-columns</xsl:variable>
    <xsl:variable name="SIDEBAR_CLASS">
      <xsl:choose>
        <xsl:when test="$USE_SIDEBAR='true' and $AUTHENTICATED='true' and not(//focused)">sidebar</xsl:when>
        <xsl:when test="$USE_SIDEBAR_GUEST='true' and not($AUTHENTICATED='true')">sidebar</xsl:when>
        <xsl:when test="$USE_SIDEBAR_FOCUSED='true' and //focused">sidebar</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <title>
          <xsl:choose>
            <xsl:when test="/layout_fragment">
            	<xsl:value-of select="$TOKEN[@name='PORTAL_PAGE_TITLE']" />: <xsl:value-of select="content/channel/@name"/>
            </xsl:when>
            <xsl:otherwise>
            	<xsl:value-of select="$TOKEN[@name='PORTAL_PAGE_TITLE']" />
            </xsl:otherwise>
          </xsl:choose>
        </title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <xsl:if test="/layout_fragment">
        	<meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT" />
        	<meta http-equiv="pragma" content="no-cache" />
        </xsl:if>
        <meta name="description" content="{$TOKEN[@name='PORTAL_META_DESCRIPTION']}" />
        <meta name="keywords" content="{$TOKEN[@name='PORTAL_META_KEYWORDS']}" />
        <xsl:if test="$PORTAL_SHORTCUT_ICON != ''">
        	<link rel="shortcut icon" href="{$PORTAL_SHORTCUT_ICON}" type="image/x-icon" />
        </xsl:if>
        
        <!-- ****** CSS ****** -->
        <xsl:call-template name="page.css"/> <!-- Sets CSS links -->
        <!-- ****** CSS ****** -->
        
        <!-- ****** JAVASCRIPT ****** -->
        <xsl:call-template name="page.js"/> <!-- Sets javascript links -->
        <!-- ****** JAVASCRIPT ****** -->
      </head>
      
      <body id="portal" class="{$LOGIN_STATE} {$PORTAL_VIEW} {$SIDEBAR_CLASS} {$FLUID_THEME_CLASS}">
        <div id="portalPage" class="{$PAGE_COLUMN_CLASS} fl-container-flex">  <!-- Main div for presentation/formatting options. -->
        	<div id="portalPageInner">  <!-- Inner div for additional presentation/formatting options. -->
            <xsl:choose>
              <xsl:when test="/layout_fragment"> <!-- When detached. -->
              
                <xsl:for-each select="content//channel">
                  <xsl:apply-templates select=".">
                  	<xsl:with-param name="detachedContent" select="'true'"/>
                  </xsl:apply-templates>
                </xsl:for-each>
              
              </xsl:when>
              <xsl:otherwise> <!-- Otherwise, default. -->
								
                <xsl:apply-templates select="header"/>
                <xsl:apply-templates select="content"/>
                <xsl:apply-templates select="footer"/>
                <xsl:if test="$USE_FLYOUT_MENUS='true'">
                  <xsl:call-template name="flyout.menu.scripts"/> <!-- If flyout menus are enabled, writes in necessary Javascript to function. -->
                </xsl:if>
                <xsl:if test="($USE_AJAX='true' and $AUTHENTICATED='true') or $USE_FLYOUT_MENUS='true'">
                  <xsl:call-template name="preferences"/>
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
   | This template renders the page header for the default view.
  -->
  <xsl:template match="header">
    <div id="portalPageHeader" class="fl-container-flex">  <!-- Div for presentation/formatting options. -->
    	<div id="portalPageHeaderInner">  <!-- Inner div for additional presentation/formatting options. -->
    
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

    	</div>
    </div>
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
    
    <div id="portalPageBody" class="fl-container-flex">  <!-- Div for presentation/formatting options. -->
    	<div id="portalPageBodyInner">  <!-- Inner div for additional presentation/formatting options. -->
      
        <!-- ****** BODY LAYOUT ****** -->
        
        <!-- ****** CONTENT TOP BLOCK ****** -->
        <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** CONTENT TOP BLOCK ****** -->
        
        <div id="portalPageBodyLayout">
        	<xsl:attribute name="class"> <!-- Write appropriate FSS class based on use of sidebar and number of columns to produce column layout. -->
          	<xsl:choose>
            	<xsl:when test="$AUTHENTICATED='true'"> <!-- Logged in -->
              	<xsl:choose>
                  <xsl:when test="$PORTAL_VIEW='focused'"> <!-- Focused View -->
                    <xsl:choose>
                      <xsl:when test="$USE_SIDEBAR_FOCUSED='true'">
                        fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH_FOCUSED" />
                      </xsl:when>
                      <xsl:otherwise>
                        fl-col-flex
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise> <!-- Dashboard View -->
                    <xsl:choose>
                      <xsl:when test="$USE_SIDEBAR='true'">
                        fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH" />
                      </xsl:when>
                      <xsl:otherwise>
                        fl-col-flex<xsl:value-of select="$COLUMNS" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>

              </xsl:when>
              <xsl:otherwise> <!-- Guest View -->
              
                <xsl:choose>
                  <xsl:when test="$USE_SIDEBAR_GUEST='true'">
                    fl-col-mixed-<xsl:value-of select="$SIDEBAR_WIDTH_GUEST" />
                  </xsl:when>
                  <xsl:otherwise>
                    fl-col-flex<xsl:value-of select="$COLUMNS" />
                  </xsl:otherwise>
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
                  <div class="fl-col-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                    <xsl:call-template name="page.title.focused"/> <!-- Template located below. -->
                    <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
                  </div>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="page.title.focused"/> <!-- No Sidebar. Template located below. -->
                  <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
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
                      <div class="fl-col-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                        <xsl:call-template name="page.title"/>
                        <xsl:call-template name="columns"/> <!-- Template located in columns.xsl. -->
                      </div>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="page.title"/> <!-- No Sidebar. Template located below. -->
                      <xsl:call-template name="columns"/> <!-- Template located in columns.xsl. -->
                    </xsl:otherwise>
                  </xsl:choose>
                  
                </xsl:when>
                <xsl:otherwise>
                  
                  <!-- Signed Out -->
                  <xsl:choose>
                    <xsl:when test="$USE_SIDEBAR_GUEST='true'"> <!-- Sidebar. -->
                      <xsl:call-template name="sidebar"/> <!-- Template located in columns.xsl. -->
                      <div class="fl-col-flex-{$FSS_SIDEBAR_LOCATION_CLASS}">
                        <xsl:call-template name="page.title"/>
                        <xsl:call-template name="columns"/> <!-- Template located in columns.xsl. -->
                      </div>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="page.title"/> <!-- No Sidebar. Template located below. -->
                      <xsl:call-template name="columns"/> <!-- Template located in columns.xsl. -->
                    </xsl:otherwise>
                  </xsl:choose>
                  
                </xsl:otherwise>
              </xsl:choose>
              
            </xsl:otherwise>
          </xsl:choose>
        
          <!-- ****** CONTENT BOTTOM BLOCK ****** -->
          <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
          <!-- ****** CONTENT BOTTOM BLOCK ****** -->
          
      	</div> <!-- End portalPageBodyLayout -->
        
    	</div> <!-- End portalPageBodyInner -->
    </div> <!-- End portalPageBody -->
  
  </xsl:template>
  <!-- ========================================= -->


  <!-- ========== TEMPLATE: PAGE TITLE ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page title.
  -->
  <xsl:template name="page.title">
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
  <xsl:template name="page.title.focused">
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
    	<!-- ????? THIS CHANNEL IS OBSOLETE WITH THE FOOTER BLOCK IMPLEMENTATION ?????
      <xsl:copy-of select="channel[@name='Footer']"/>
      -->
      
      <!-- ****** FOOTER BLOCK ****** -->
      <xsl:call-template name="footer.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
      <!-- ****** FOOTER BLOCK ****** -->
      
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
		
</xsl:stylesheet>
