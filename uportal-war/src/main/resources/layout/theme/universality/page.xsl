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
    <xsl:variable name="PAGE_COLUMN_CLASS"><xsl:value-of select="$COUNT_PORTLET_COLUMNS"/>-column</xsl:variable>
    
    <html xml:lang="en" lang="en">
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
      
      <body id="portal" class="{$LOGIN_STATE} {$PORTAL_VIEW}">
        <div id="portalPage" class="{$PAGE_COLUMN_CLASS} {$LEFT_COLUMN_CLASS}">  <!-- Main div for presentation/formatting options. -->
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
                <xsl:if test="$USE_AJAX='true' and $AUTHENTICATED='true'">
                  <xsl:call-template name="preferences"/>
                </xsl:if>
                <xsl:if test="($USE_AJAX='true' or $USE_FLYOUT_MENUS='true') and not(//focused)">
                  <xsl:call-template name="flyout.menu.scripts"/> <!-- If flyout menus are enabled, writes in necessary Javascript to function. -->
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
    <div id="portalPageHeader">  <!-- Div for presentation/formatting options. -->
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
   | A left navigation column may be enabled by setting the "USE_LEFT_COLUMN" paramater (see VARIABLES & PARAMETERS above) to "true".
   | This left navigation column currently cannot contain portlets or channels, but only a limited set of user interface components.
  -->
  <xsl:template match="content">
    <xsl:variable name="columns" select="count(column)"/>
    
    <div id="portalPageBody">  <!-- Div for presentation/formatting options. -->
    	<div id="portalPageBodyInner">  <!-- Inner div for additional presentation/formatting options. -->
      
        <!-- ****** BODY LAYOUT ****** -->
        <!-- Use of a table for layout is technically incorrect, however, the portal is a representation of lists of data within columns, and is therefore perhaps technically a table.  Regardless of the proper use of the table element here, the table element is the only consistent means of producing cross-browser columns given the complexity of the portal, and is the lesser of two evils; either a (perhaps) incorrect use of a table for layout or significant hacks and headaches in the CSS/browser testing to achieve the same thing with divs and floats (which are also incorrectly used for layout) and browser-specific css (also a frowned-upon practice). -->
        
        <a name="startContent"><xsl:comment>Comment to keep from collapsing</xsl:comment></a>  <!-- Skip navigation target. -->
        
        <!-- ****** CONTENT TOP BLOCK ****** -->
        <xsl:call-template name="content.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** CONTENT TOP BLOCK ****** -->
        
        <table id="portalPageBodyLayout" width="100%">
          <tr>
          
            <!-- ****** LEFT COLUMN ****** -->
            <!-- Useage of the left column and subsequent UI components are set by parameters in universality.xsl. -->
            <xsl:if test="$USE_LEFT_COLUMN='true'">
              <xsl:call-template name="left.column"/> <!-- Template located in columns.xsl. -->
            </xsl:if>
            <!-- ****** LEFT COLUMN ****** -->
            
            <!-- ****** TITLE ROW ****** -->
            <td valign="top" colspan="{$columns}" id="portalPageBodyTitleRow"> <!-- This row contains the page title (label of the currently selected main navigation item), and optionally user layout customization hooks, custom institution content (blocks), or return to dashboard link (if in the focused view). -->
            
              <div id="portalPageBodyTitleRowContents"> <!-- Inner div for additional presentation/formatting options. -->
                
                <xsl:choose>
                  <xsl:when test="//focused">
                    <!-- ****** CONTENT TITLE BLOCK ****** -->
                    <xsl:call-template name="content.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                    <!-- ****** CONTENT TITLE BLOCK ****** -->
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- ****** CONTENT TITLE BLOCK ****** -->
                    <xsl:call-template name="content.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
                    <!-- ****** CONTENT TITLE BLOCK ****** -->
                  </xsl:otherwise>
                </xsl:choose>
                
              </div>
            </td>
          </tr>
          
          <!-- ****** COLUMNS ****** -->
          <tr id="portalPageBodyColumns">
            <xsl:choose>
              <xsl:when test="//focused"> <!-- If the page is focused, there is only one column and an alternate rendering of the contents. -->
                <td valign="top" class="portal-page-column-focused">
                  <xsl:apply-templates select="//focused"/> <!-- Templates located in content.xsl. -->
                </td>
              </xsl:when>
              <xsl:otherwise> <!-- Otherwise, the page is not focused and the dashboard view of columns of portlets applies. -->
                <xsl:call-template name="content.row"/> <!-- Template located in columns.xsl. -->
              </xsl:otherwise>
            </xsl:choose>
          </tr>
        </table>
        
        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
        <xsl:call-template name="content.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** CONTENT BOTTOM BLOCK ****** -->
      
    	</div>
    </div>
  
  </xsl:template>
  <!-- ========================================= -->
  
  
  <!-- ========== TEMPLATE: PAGE FOOTER ========== -->
  <!-- =========================================== -->
  <!-- 
   | This template renders the page footer.
   | The footer channel is located at: webpages\stylesheets\org\jasig\portal\channels\CGenericXSLT\footer\footer_webbrowser.xsl.
  -->
  <xsl:template match="footer">
    <div id="portalPageFooter">
    	<!-- ????? THIS CHANNEL IS OBSOLETE WITH THE FOOTER BLOCK IMPLEMENTATION ?????
      <xsl:copy-of select="channel[@name='Footer']"/>
      -->
      
      <!-- ****** FOOTER BLOCK ****** -->
      <xsl:call-template name="footer.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
      <!-- ****** FOOTER BLOCK ****** -->
      
    </div>
  </xsl:template>
  <!-- =========================================== -->
  
		
</xsl:stylesheet>
