<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!-- ============================= -->
<!-- ========== README =========== -->
<!-- ============================= -->
<!-- 
 | The theme is written in XSL. For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
 | Baseline XSL skill is strongly recommended before modifying this file.
 |
 | This file has two purposes:
 | 1. To instruct the portal how to compile and configure the theme.
 | 2. To provide theme configuration and customization.
 |
 | As such, this file has a mixture of code that should not be modified, and code that exists explicitly to be modified.
 | To help make clear what is what, a RED-YELLOW-GREEN legend has been added to all of the sections of the file.
 |
 | RED: Stop! Do not modify.
 | YELLOW: Warning, proceed with caution.  Modifications can be made, but should not generally be necessary and may require more advanced skill.
 | GREEN: Go! Modify as desired.
 |
 | One of the intents of the theme structure is to provide one place for configuration and customization.
 | All configuration and customization should be done in this file, leaving all other theme files untouched.
 | Following this guideline will minimize impacts to your configuration and customization migration to future releases of uPortal.
 |
 | NEED LOCALIZATION NOTES AND INSTRUCTION.
-->


<!-- ============================================= -->
<!-- ========== STYLESHEET DELCARATION =========== -->
<!-- ============================================= -->
<!-- 
 | RED
 | This statement defines this document as XSL.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- ============================================= -->


	<!-- ============================= -->
  <!-- ========== IMPORTS ========== -->
  <!-- ============================= -->
  <!-- 
   | RED
   | Imports are the XSL files that build the theme.
   | Import statments and the XSL files they refer to should not be modified.
   | For customization of the theme, use the Varaiables and Parameters and Templates sections below.
  -->
  <!-- <xsl:import href="page.xsl" /> Templates for page structure -->
  <!-- <xsl:import href="navigation.xsl" /> Templates for navigation structure -->
  <!-- <xsl:import href="components.xsl" /> Templates for UI components (login, web search, etc.) -->
  <!-- <xsl:import href="columns.xsl" /> Templates for column structure -->
  <!-- <xsl:import href="content.xsl" /> Templates for content structure (i.e. portlets) -->
  <!-- <xsl:import href="preferences.xsl" /> Templates for preferences-specific structures -->
  <!-- -->
  <xsl:import href="page.xsl" />
  <xsl:import href="navigation.xsl" />
  <xsl:import href="components.xsl" />
  <xsl:import href="columns.xsl" />
  <xsl:import href="content.xsl" />
  <xsl:import href="preferences.xsl" />
  <!-- ============================= -->
  
  
  <!-- ========================================= -->
  <!-- ========== OUTPUT DELCARATION =========== -->
  <!-- ========================================= -->
  <!-- 
   | RED
   | This statement instructs the XSL how to output.
  -->
  <xsl:output method="xml" indent="no" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" omit-xml-declaration="yes" />
  <!-- ========================================= -->
  
  
  <!-- ============================================== -->
  <!-- ========== VARIABLES and PARAMETERS ========== -->
  <!-- ============================================== -->
  <!-- 
   | YELLOW - GREEN
   | These variables and parameters provide flexibility and customization of the user interface.
   | Changing the values of the variables and parameters signals the theme to reconfigure use and location of user interface components.
   | All text used within the theme is localized.  See notes above for customizing text.
  -->
  
  
  <!-- ****** SKIN SETTINGS ****** -->
  <!-- 
   | YELLOW
   | Skin Settings can be used to change the location of skin files.
  -->
  <xsl:param name="SKIN" select="'uportal3'"/>
  <xsl:variable name="MEDIA_PATH">media/skins/universality</xsl:variable>
  <xsl:variable name="SKIN_PATH" select="concat($MEDIA_PATH,'/',$SKIN)"/>
  <xsl:variable name="SCRIPT_PATH">media/skins/universality/common/javascript</xsl:variable>
  <xsl:variable name="PORTAL_SHORTCUT_ICON">/favicon.ico</xsl:variable>
  
  
  <!-- ****** LOCALIZATION SETTINGS ****** -->
  <!-- 
   | GREEN
   | Locatlization Settings can be used to change the localization of the theme.
  -->
	<xsl:param name="MESSAGE_DOC_URL">messages.xml</xsl:param> <!-- Name of the localization file. -->
	<xsl:param name="USER_LANG">en</xsl:param> <!-- Sets the default user language. -->
  
  
  <!-- ****** PORTAL SETTINGS ****** -->
  <!-- 
   | YELLOW
   | Portal Settings should generally not be (and not need to be) modified.
  -->
  <xsl:param name="USER_NAME">John Doe</xsl:param>
  <xsl:param name="UP_VERSION">uPortal X.X.X</xsl:param>
  <xsl:param name="BASE_ACTION_URL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="PORTAL_VIEW">
  	<xsl:choose>
  		<xsl:when test="//layout_fragment">detached</xsl:when>
      <xsl:when test="//focused">focused</xsl:when>
      <xsl:otherwise>dashboard</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:param name="USE_AJAX" select="'false'"/>
  <xsl:param name="JS_LIBRARY_CLASS">
  	<xsl:choose>
  		<xsl:when test="$USE_AJAX='true'">tundra</xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:param name="AUTHENTICATED" select="'false'"/>
  <xsl:param name="LOGIN_STATE">
  	<xsl:choose>
  		<xsl:when test="$AUTHENTICATED='true'">logged-in</xsl:when>
      <xsl:otherwise>not-logged-in</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:variable name="TOKEN" select="document($MESSAGE_DOC_URL)/theme-messages/tokens[lang($USER_LANG)]/token"/> <!-- Tells the theme how to find appropriate localized token. --> 
  
  
  <!-- ****** INSTITUTION SETTINGS ****** -->
  <!-- 
   | GREEN
   | Institution Settings can be used to change intitution-specific parts of the theme.
   | Refer to localization notes above for changing text and labels used in the theme.
  -->
  <xsl:variable name="HELP_URL">http://www.uportal.org/</xsl:variable>
  <xsl:variable name="LOGIN_HELP_URL">http://www.uportal.org/</xsl:variable>
  <xsl:variable name="CAS_LOGIN_URL">https://login.institution.edu/cas/login?service=https://portal.domain.edu/uPortal/Login</xsl:variable>
  <xsl:variable name="CAS_NEW_USER_URL">http://www.uportal.org/</xsl:variable>
  
  
  <!-- ****** NAVIGATION SETTINGS ****** -->
  <!-- 
   | GREEN
   | Navigation Settings can be used to change the navigation.
  -->
  <xsl:param name="USE_FLYOUT_MENUS" select="'false'"/> <!-- Sets the use of flyout menus.  Values are 'true' or 'false'. -->
  
  
  <!-- ****** LAYOUT SETTINGS ****** -->
  <!-- 
   | GREEN
   | Layout Settings can be used to change the main layout.
  -->
  <xsl:param name="USE_LEFT_COLUMN" select="'true'"/> <!-- Sets the use of a left sidebar.  This sidebar can contain UI components (navigation, quicklinks, etc.) and custom institution content (blocks), but not portlets.  Values are 'true' or 'false'. -->
  <xsl:param name="LEFT_COLUMN_CLASS">
  	<xsl:choose>
  		<xsl:when test="$USE_LEFT_COLUMN='true'">left-column</xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  
  <!-- ============================================ -->
  
  <!-- Debug Template
  <xsl:template match="/">
  	<h1>Debugging</h1>
  </xsl:template> -->
  
  <!-- =============================== -->
  <!-- ========== TEMPLATES ========== -->
  <!-- =============================== -->
  <!-- 
   | GREEN
   | Templates included in this file are for the purpose of customizing the portal.
   | PAGE CSS and PAGE JAVASCRIPT are provided as a means to override the theme defaults.
   | For those templates, the theme defaults are provided in the comments.
   | To override those templates, uncomment the defaults and make desired modifications.
   | Block templates are not overrides, but are content areas (blocks) called from the theme.
   | Some blocks have content (or call other templates) by default, and others are empty by default.
   | If custom content is desired, write in the contents into the appropriate block.
   | Changes to templates may require a restart of the portal server.
   | Template contents can be any valid XSL or XHTML.
  -->
  
  
  <!-- ========== TEMPLATE: PAGE CSS ========== -->
  <!-- ======================================== -->
	<!-- 
   | GREEN
   | This template renders the CSS links in the page <head>.
   | Cascading Stylesheets (CSS) that provide the visual style and presentation of the portal.
   | Refer to [http://www.w3.org/Style/CSS/] for CSS definition and syntax.
   | CSS files are located in the uPortal skins directory: webpages/media/skins.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="page.css">
    <!-- uPortal print CSS -->
    <link media="print" type="text/css" href="{$SKIN_PATH}/print.css" rel="stylesheet"/> 
    <!-- Yahoo! User Interface Library (YUI) CSS to establish a common, cross-browser base rendering.  See http://developer.yahoo.com/yui/ for more details. --> 
    <link media="all" type="text/css" href="{$MEDIA_PATH}/common/reset-fonts-grids.css" rel="stylesheet"/>
    <link media="all" type="text/css" href="{$MEDIA_PATH}/common/base-min.css" rel="stylesheet"/>
    <!-- uPortal theme/layout CSS -->
    <link media="all" type="text/css" href="{$SKIN_PATH}/layout.css" rel="stylesheet"/>
    <!-- uPortal skin CSS -->
    <link media="all" type="text/css" href="{$SKIN_PATH}/{$SKIN}.css" rel="stylesheet"/>
    
    <xsl:if test="$USE_AJAX='true'">
     <link rel="stylesheet" href="{$MEDIA_PATH}/common/javascript/jquery/themes/{$SKIN}/jqueryui.all.css" type="text/css" media="screen" title="Flora (Default)"></link>
    </xsl:if>
    
    <!-- Add Conditional Comments for IE to load IE specific CSS -->
		<xsl:comment>[if IE]&gt; 
		&lt;style type="text/css"&gt;@import url("<xsl:value-of select="$SKIN_PATH" />/ie.css");&lt;/style&gt; 
		&lt;![endif]</xsl:comment>
  </xsl:template>
  <!-- ======================================== -->
  
  
  <!-- ========== TEMPLATE: PAGE JAVASCRIPT ========== -->
  <!-- =============================================== -->
  <!-- 
   | YELLOW
   | This template renders the Javascript links in the page <head>.
   | Javascript provides AJAX and enhanced client-side interaction to the portal.
   | Javascript files are located in the uPortal skins directory: webpages/media/skins/javascript.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="page.js">
    <xsl:if test="$USE_AJAX='true'">
     <script src="{$SCRIPT_PATH}/jquery/jquery-1.2.3.min.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/jquery.dimensions.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.dialog.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.resizable.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.mouse.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.draggable.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.droppable.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.sortable.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/ui.tabs.js"></script>
     <script type="text/javascript" src="{$SCRIPT_PATH}/jquery/interface.js"></script>
     <script src="{$SCRIPT_PATH}/ajax-preferences-jquery.js" type="text/javascript"/>
    </xsl:if>
  </xsl:template>
  
  <!-- =============================================== -->
  
  
  <!-- ========== TEMPLATE: HEADER BLOCK ========== -->
  <!-- ============================================ -->
  <!-- 
   | GREEN
   | This template renders content into the page header.
   | Reordering the template calls and/or xhtml contents will change the order in the page markup.
   | Commenting out a template call will prevent that component's markup fom being written into the page markup.
   | Thus, to not use the quicklinks, simply comment out the quicklinks template call.
   | These components can be placed into other blocks, if desired.
   | To place a component into another block, copy the template call from this block and paste it into another block; then comment out the template call in this block 
   | Custom content can be inserted as desired.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="header.block">
  	<!-- Portal Page Bar -->
    <xsl:call-template name="portal.page.bar"/>
    <!-- Portal Page Bar -->
    
    <!-- Skip Navigation -->
    <div id="portalSkipNav">
      <a href="#mainNavigation" title="{$TOKEN[@name='SKIP_TO_NAV_TITLE']}" id="skipToNav">
        <xsl:value-of select="$TOKEN[@name='SKIP_TO_NAV']"/>
      </a>
      <a href="#startContent" title="{$TOKEN[@name='SKIP_TO_CONTENT_TITLE']}" id="skipToContent">
        <xsl:value-of select="$TOKEN[@name='SKIP_TO_CONTENT']"/>
      </a>
    </div>
    <!-- Skip Navigation -->
    
    <!-- Logo -->
    <xsl:call-template name="logo"/>
    <!-- Logo -->
    
    <!-- ****** LOGIN ****** -->
    <!--
     | Use one of the login options: the login template (uP3 preferred), the login channel (from uP2.6), or CAS login.
     | By default, the login is rendered into the left column below.
    -->
    <!-- Login
    <xsl:call-template name="login"/> -->
    <!-- Login -->
    
    <!-- Login Channel -->
    <xsl:if test="$AUTHENTICATED='true'">
    	<xsl:call-template name="login.channel"/>
    </xsl:if>
    <!-- Login Channel -->
    
    <!-- CAS Login
    <xsl:call-template name="cas.login"/> -->
    <!-- CAS Login -->
    <!-- ****** LOGIN ****** -->
    
    <!-- Welcome
    <xsl:call-template name="welcome"/> -->
    <!-- Welcome -->
    
    <!-- Web Search -->
    <xsl:call-template name="web.search"/>
    <!-- Web Search -->
    
    <!-- Quicklinks -->
    <xsl:call-template name="quicklinks"/>
    <!-- Quicklinks -->
    
    <!-- Main Navigation, by default rendered in the left column below. -->
    <xsl:apply-templates select="//navigation">
      <xsl:with-param name="CONTEXT" select="'header'"/>
    </xsl:apply-templates>
    <!-- Main Navigation -->
    
    <!-- SAMPLE:
    <div id="portalHeaderBlock">
    	<p>CUSTOM CONTENTS.</p>
    </div>
    -->
  </xsl:template>
  <!-- ============================================ -->
    
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR TITLE BLOCK ========== -->
  <!-- =========================================================== -->
  <!-- 
   | GREEN
   | This template renders content for the page bar title.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portal.page.bar.title.block">
  	<!-- <h2><xsl:copy-of select="$TOKEN[@name='PORTAL_PAGE_TITLE']"/></h2> -->
  </xsl:template>
  <!-- =========================================================== -->
    
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINKS BLOCK ========== -->
  <!-- =========================================================== -->
  <!-- 
   | GREEN
   | This template renders content for the page bar title links.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portal.page.bar.links.block">
  	<!-- Home Link -->
  	<xsl:call-template name="portal.page.bar.link.home"/>
    <!-- Home Link -->
    
    <!-- Admin Link -->
  	<xsl:call-template name="portal.page.bar.link.admin"/>
    <!-- Admin Link -->
    
    <!-- Preferences Link -->
  	<xsl:call-template name="portal.page.bar.link.customize"/>
    <!-- Preferences Link -->
    
    <!-- Sitemap Link -->
  	<xsl:call-template name="portal.page.bar.link.sitemap"/>
    <!-- Sitemap Link -->
    
    <!-- Help Link -->
  	<xsl:call-template name="portal.page.bar.link.help"/>
    <!-- Help Link -->
  </xsl:template>
  <!-- =========================================================== -->
    
  
  <!-- ========== TEMPLATE: LOGO BLOCK ========== -->
  <!-- ========================================== -->
  <!-- 
   | GREEN
   | This template renders content for the logo.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="logo.block">
  	<img src="{$SKIN_PATH}/images/portal_logo.png" alt="{$TOKEN[@name='LOGO']}"/>
    <!-- Text only: 
    <span><xsl:value-of select="$TOKEN[@name='LOGO']"/></span> -->
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: HEADER FOCUSED BLOCK ========== -->
  <!-- ==================================================== -->
  <!-- 
   | GREEN
   | This template renders custom content into the page header of the focused view.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="header.focused.block">
    <!-- Portal Page Bar -->
    <xsl:call-template name="portal.page.bar"/>
    <!-- Portal Page Bar -->
    
    <!-- Skip Navigation -->
    <div id="portalSkipNav">
      <a href="#mainNavigation" title="{$TOKEN[@name='SKIP_TO_NAV_TITLE']}" id="skipToNav">
        <xsl:value-of select="$TOKEN[@name='SKIP_TO_NAV']"/>
      </a>
      <a href="#startContent" title="{$TOKEN[@name='SKIP_TO_CONTENT_TITLE']}" id="skipToContent">
        <xsl:value-of select="$TOKEN[@name='SKIP_TO_CONTENT']"/>
      </a>
    </div>
    <!-- Skip Navigation -->
    
    <!-- Logo
    <xsl:call-template name="logo"/> -->
    <!-- Logo -->
    
    <!-- SAMPLE:
    <div id="portalHeaderFocusedBlock">
    	<p>CUSTOM CONTENTS.</p>
    </div>
    -->
  </xsl:template>
  <!-- ==================================================== -->
    
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR TITLE FOCUSED BLOCK ========== -->
  <!-- =================================================================== -->
  <!-- 
   | GREEN
   | This template renders content for the page bar title of the focused view.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portal.page.bar.title.focused.block">
  	<img src="{$SKIN_PATH}/images/logo_focused.png" alt="{$TOKEN[@name='LOGO']}"/>
  </xsl:template>
  <!-- =================================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINKS FOCUSED BLOCK ========== -->
  <!-- =================================================================== -->
  <!-- 
   | GREEN
   | This template renders content for the page bar title links of the focused view.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portal.page.bar.links.focused.block">
  	<!-- Home Link -->
  	<xsl:call-template name="portal.page.bar.link.home"/>
    <!-- Home Link -->
    
    <!-- Admin Link
  	<xsl:call-template name="portal.page.bar.link.admin"/> -->
    <!-- Admin Link -->
    
    <!-- Preferences Link
  	<xsl:call-template name="portal.page.bar.link.customize"/> -->
    <!-- Preferences Link -->
    
    <!-- Sitemap Link
  	<xsl:call-template name="portal.page.bar.link.sitemap"/> -->
    <!-- Sitemap Link -->
    
    <!-- Help Link -->
  	<xsl:call-template name="portal.page.bar.link.help"/>
    <!-- Help Link -->
  </xsl:template>
  <!-- =================================================================== -->
  
  
  <!-- ========== TEMPLATE: FOCUSED LOGO BLOCK ========== -->
  <!-- ================================================== -->
  <!-- 
   | GREEN
   | This template renders content for the focused logo.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="logo.focused.block">
  	<!-- Text:
    <span><xsl:value-of select="$TOKEN[@name='LOGO']"/></span> -->
    <!-- Image: 
    <img src="{$SKIN_PATH}/images/portal_logo_focused.png" alt="{$TOKEN[@name='LOGO']}"/> -->
  </xsl:template>
  <!-- ================================================== -->
  
  
  <!-- ========== TEMPLATE: CONTENT TOP BLOCK ========== -->
  <!-- ================================================= -->
  <!-- 
   | GREEN
   | This template renders custom content into the page body above the content layout table.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="content.top.block">
  	<!-- SAMPLE:
    <div id="portalContentTopBlock">
    	<p>CUSTOM CONTENTS.</p>
    </div>
    -->
  </xsl:template>
  <!-- ================================================= -->
  
  
  <!-- ========== TEMPLATE: CONTENT BOTTOM BLOCK ========== -->
  <!-- ==================================================== -->
  <!-- 
   | GREEN
   | This template renders custom content into the page body below the content layout table.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="content.bottom.block">
  	<!-- SAMPLE:
    <div id="portalContentBottomBlock">
    	<p>CUSTOM CONTENTS.</p>
    </div>
    -->
  </xsl:template>
  <!-- ==================================================== -->
  
  
  <!-- ========== TEMPLATE: CONTENT TITLE BLOCK ========== -->
  <!-- =================================================== -->
  <!-- 
   | GREEN
   | This template renders content into the page body in the top row of the content layout table.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="content.title.block">
  	<!-- PAGE TITLE -->
    <xsl:call-template name="page.title"/>
    <!-- PAGE TITLE -->
    
    <!-- CUSTOMIZE LINKS: For these links to function, AJAX must be enabled by setting the USE_AJAX parameter above to 'true'. -->
    <xsl:call-template name="customize.links"/>
    <!-- CUSTOMIZE LINKS -->
  </xsl:template>
  <!-- =================================================== -->
  
  
  <!-- ========== TEMPLATE: CONTENT TITLE FOCUSED BLOCK ========== -->
  <!-- =========================================================== -->
  <!-- 
   | GREEN
   | This template renders content into the page body in the top row of the content layout table of the focused view.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="content.title.focused.block">
  	<!-- PAGE TITLE -->
    <xsl:call-template name="page.title"/>
    <!-- PAGE TITLE -->
    
    <!-- BACK TO HOME -->
    <xsl:call-template name="back.to.home"/>
    <!-- BACK TO HOME -->
  </xsl:template>
  <!-- =========================================================== -->
  
  
  <!-- ========== TEMPLATE: CONTENT LEFT BLOCK ========== -->
  <!-- ================================================== -->
  <!-- 
   | GREEN
   | This template renders content into the page body in the left column of the content layout table.
   | The left navigation column must be enabled for this content to render.
   | Enable the left navigation column by setting USE_LEFT_COLUMN to 'true' in the Variables and Parameters section above.
   | Reordering the template calls will change the order in the page markup.
   | Commenting out a template call will prevent that component's markup fom being written into the page markup.
   | Thus, to not use the quicklinks, simply comment out the quicklinks template call.
   | These components can be placed into other blocks, if desired.
   | To place a component into another block, copy the template call from this block and paste it into another block; then comment out the template call in this block 
   | Custom content can be inserted as desired.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="content.left.block">
		<!-- ****** LOGIN ****** -->
    <!--
     | Use one of the login options: the login template (uP3 preferred), the login channel (from uP2.6), or CAS login.
    -->
    <!-- Login
    <xsl:call-template name="login"/> -->
    <!-- Login -->
    
    <!-- Login Channel -->
    <xsl:if test="$AUTHENTICATED='false'">
    	<xsl:call-template name="login.channel"/>
    </xsl:if>
    <!-- Login Channel -->
    
    <!-- CAS Login
    <xsl:call-template name="cas.login"/> -->
    <!-- CAS Login -->
    <!-- ****** LOGIN ****** -->
    
    <!-- Web Search, by default rendered in the header above.
    <xsl:call-template name="web.search"/> -->
    <!-- Web Search -->
    
    <!-- Quicklinks, by default rendered in the header above.
    <xsl:call-template name="quicklinks"/> -->
    <!-- Quicklinks -->
    
    <!-- Main Navigation -->
    <xsl:apply-templates select="//navigation">
      <xsl:with-param name="CONTEXT" select="'left'"/>
    </xsl:apply-templates>
    <!-- Main Navigation -->
    
    <!-- SAMPLE:
    <div id="portalContentLeftBlock">
    	<p>CUSTOM CONTENTS.</p>
    </div>
    -->
  </xsl:template>
  <!-- ================================================== -->
  
  
  <!-- ========== TEMPLATE: FOOTER BLOCK ========== -->
  <!-- ============================================ -->
  <!-- 
   | GREEN
   | This template renders custom content into the page footer.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="footer.block">
    <!-- Footer Links -->
    <div id="portalPageFooterLinks">
      <a href="http://www.ja-sig.org/" target="_blank" title="{$TOKEN[@name='JASIG_LONG_LABEL']}">
      	<xsl:value-of select="$TOKEN[@name='JASIG_LABEL']"/>
      </a>
      <xsl:call-template name="portal.pipe"/>
			<a href="http://www.uportal.org/" target="_blank" title="{$TOKEN[@name='UPORTAL_LONG_LABEL']}">
      	<xsl:value-of select="$TOKEN[@name='UPORTAL_LABEL']"/>
      </a>
      <xsl:call-template name="portal.pipe"/>
			<a href="http://www.uportal.org/download.html" target="_blank" title="{$TOKEN[@name='UPORTAL_DOWNLOAD_LONG_LABEL']}">
      	<xsl:value-of select="$TOKEN[@name='UPORTAL_DOWNLOAD_LABEL']"/>
      </a>
      <xsl:call-template name="portal.pipe"/>
			<a href="http://www.uportal.org/community/index.html" target="_blank" title="{$TOKEN[@name='UPORTAL_COMMUNITY_LONG_LABEL']}">
      	<xsl:value-of select="$TOKEN[@name='UPORTAL_COMMUNITY_LABEL']"/>
      </a>
    </div>
    
    <!-- uPortal Product Version -->
    <div id="portalProductAndVersion">
    	<p><a href="http://www.uportal.org" title="Powered by {$UP_VERSION}" target="_blank">Powered by <xsl:value-of select="$UP_VERSION"/></a></p>
    	<!-- It's a good idea to leave this in the markup, that way anyone who may be supporting your portal can get to this information quickly by simply using a browser.  If you don't want the statement to visibly render in the page, use CSS to make it invisible. -->
    </div>
    
    <!-- Copyright -->
    <div id="portalCopyright">
    	<p><a href="http://www.uportal.org" title="uPortal" target="_blank">uPortal</a> is licensed under the <a href="http://www.opensource.org/licenses/bsd-license.php" title="New BSD License" target="_blank">New BSD License</a> as approved by the Open Source Initiative (OSI), an <a href="http://www.opensource.org/docs/osd" title="OSI-certified" target="_blank">OSI-certified</a> ("open") and <a href="http://www.gnu.org/licenses/license-list.html" title="Gnu/FSF-recognized" target="_blank">Gnu/FSF-recognized</a> ("free") license.</p>
    </div>
    
    <!-- Icon Set Attribution -->
    <div id="silkIconsAttribution">
      <p><a href="http://www.famfamfam.com/lab/icons/silk/" title="Silk icon set 1.3" target="_blank">Silk icon set 1.3</a> courtesy of Mark James.</p>
      <!-- Silk icon set 1.3 by Mark James [ http://www.famfamfam.com/lab/icons/silk/ ], which is licensed under a Creative Commons Attribution 2.5 License. [ http://creativecommons.org/licenses/by/2.5/ ].  This icon set is free for use under the CCA 2.5 license, so long as there is a link back to the author's site.  If the Silk icons are used, this reference must be present in the markup, though not necessarily visible in the rendered page.  If you don't want the statement to visibly render in the page, use CSS to make it invisible. -->
    </div>
    
  </xsl:template>
  <!-- ============================================ -->
  
  <!-- ========== TEMPLATE: PORTLET TOP BLOCK ========== -->
  <!-- ================================================= -->
  <!-- 
   | GREEN
   | This template renders custom content on the portlet container top for additional decoration, primarily for rounded corners.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portlet.top.block">
  	<!--
    <div class="portlet-top">
      <div class="portlet-top-inner">
      </div>
    </div>
    -->
  </xsl:template>
  <!-- ================================================= -->
  
  
  <!-- ========== TEMPLATE: PORTLET BOTTOM BLOCK ========== -->
  <!-- ==================================================== -->
  <!-- 
   | GREEN
   | This template renders custom content on the portlet container bottom for additional decoration, primarily for rounded corners.
   | Template contents can be any valid XSL or XHTML.
  -->
  <xsl:template name="portlet.bottom.block">
  	<!--
    <div class="portlet-bottom">
      <div class="portlet-bottom-inner">
      </div>
    </div>
    -->
  </xsl:template>
  <!-- ==================================================== -->
		
</xsl:stylesheet>
