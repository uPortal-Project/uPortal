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
 | This file determines the presentation of UI components of the portal.
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
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg" 
    version="1.0">
    
    <!-- ========== TEMPLATE: PORTAL PIPE ========== -->
    <!-- =========================================== -->
    <!--
     | This template renders skip-to navigation.
    -->
    <xsl:template name="skip.nav">
		<div id="portalSkipNav">
	      <a href="#mainNavigation" title="{upMsg:getMessage('skip.to.page.navigation', $USER_LANG)}" id="skipToNav" accesskey="N">
	        <xsl:value-of select="upMsg:getMessage('skip.to.page.navigation', $USER_LANG)"/>
	      </a>
	      <a href="#pageContent" title="{upMsg:getMessage('skip.to.page.content', $USER_LANG)}" id="skipToContent" accesskey="C">
	        <xsl:value-of select="upMsg:getMessage('skip.to.page.content', $USER_LANG)"/>
	      </a>
	    </div>
    </xsl:template>

  <!-- ========== TEMPLATE: PORTAL PIPE ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders a pipe ( | ), generally used to separate links.
  -->
  <xsl:template name="portal.pipe">
		<span class="portal-pipe">|</span> 
  </xsl:template>
  <!-- =========================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR ========== -->
  <!-- =============================================== -->
  <!--
   | This template renders the portal page bar.
  -->
  <xsl:template name="portal.page.bar">  
    <div id="portalPageBar">
      <xsl:choose>
        <xsl:when test="//focused">
          <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
          <xsl:call-template name="portal.page.bar.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
          <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
        </xsl:when>
        <xsl:otherwise>
          <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
          <xsl:call-template name="portal.page.bar.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
          <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
        </xsl:otherwise>
      </xsl:choose>
      <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
      <div id="portalPageBarLinks">
      	<ul class="utilities">
	      	<xsl:choose>
	          <xsl:when test="//focused">
	            <!-- ****** PORTAL PAGE BAR LINKS FOCUSED BLOCK ****** -->
	            <xsl:call-template name="portal.page.bar.links.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
	            <!-- ****** PORTAL PAGE BAR LINKS FOCUSED BLOCK ****** -->
	          </xsl:when>
	          <xsl:otherwise>
	            <!-- ****** PORTAL PAGE BAR LINKS BLOCK ****** -->
	            <xsl:call-template name="portal.page.bar.links.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
	            <!-- ****** PORTAL PAGE BAR LINKS BLOCK ****** -->
	          </xsl:otherwise>
	        </xsl:choose>
        </ul>
      </div>
      <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
    </div>
  </xsl:template>
  <!-- =============================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR TITLE ========== -->
  <!-- ===================================================== -->
  <!--
   | This template renders the portal page bar title.
  -->
  <xsl:template name="portal.page.bar.title">
  	<xsl:choose>
      <xsl:when test="//focused">
        <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
        <xsl:call-template name="portal.page.bar.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
      </xsl:when>
      <xsl:otherwise>
        <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
        <xsl:call-template name="portal.page.bar.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>
  <!-- ===================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK HOME ========== -->
  <!-- ========================================================== -->
  <!--
   | This template renders the home link into the portal page bar title.
  -->
  <xsl:template name="portal.page.bar.link.home">
      <li class="link-home">
          <xsl:variable name="homeUrl">
            <xsl:call-template name="portalUrl"/>
          </xsl:variable>
	      <a href="{$homeUrl}">
	        <xsl:attribute name="title">
	          <xsl:choose>
	            <!-- Use the Back to Home label for focused view -->
	            <xsl:when test="//focused"><xsl:value-of select="upMsg:getMessage('back.to.home.long', $USER_LANG)"/></xsl:when>
	            <!-- Otherwise, just Home label -->
	            <xsl:otherwise><xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/></xsl:otherwise>
	          </xsl:choose>
	        </xsl:attribute>
	        <span>
	          <xsl:choose>
	            <!-- Use the Back to Home label for focused view -->
	            <xsl:when test="//focused"><xsl:value-of select="upMsg:getMessage('back.to.home.long', $USER_LANG)"/></xsl:when>
	            <!-- Otherwise, just Home label -->
	            <xsl:otherwise><xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/></xsl:otherwise>
	          </xsl:choose>
	        </span>
	      </a>
      </li>
  </xsl:template>
  <!-- ========================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK ADMIN ========== -->
  <!-- ========================================================== -->
  <!--
   | This template renders the admin menu into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.admin">
  	<xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin')">
    	<li class="link-admin">
    	  <xsl:variable name="portletAdminUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                    <url:portal-url>
                        <url:fname>portlet-admin</url:fname>
                        <url:portlet-url state="MAXIMIZED" />
                    </url:portal-url>
                </xsl:with-param>
            </xsl:call-template>
    	  </xsl:variable>
    	  <a href="{$portletAdminUrl}" title="{upMsg:getMessage('go.to.portlet.manager', $USER_LANG)}">
          <span><xsl:value-of select="upMsg:getMessage('portlet.manager', $USER_LANG)"/></span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ========================================================== -->
  

  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK SITEMAP ========== -->
  <!-- ============================================================ -->
  <!--
   | This template renders the sitemap link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.sitemap">
    <xsl:if test="$AUTHENTICATED='true'">
    	<li class="link-sitemap">
    	  <xsl:variable name="layoutSitemapUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                    <url:portal-url>
                        <url:fname>layout-sitemap</url:fname>
                        <url:portlet-url state="MAXIMIZED" />
                    </url:portal-url>
                </xsl:with-param>
            </xsl:call-template>
    	  </xsl:variable>
    	  <a href="{$layoutSitemapUrl}" title="{upMsg:getMessage('go.to.site.map', $USER_LANG)}">
          <span><xsl:value-of select="upMsg:getMessage('site.map', $USER_LANG)"/></span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ============================================================ -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK HELP ========== -->
  <!-- ========================================================= -->
  <!--
   | This template renders the help link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.help">
  	<li class="link-help">
      <a href="{$HELP_URL}" title="{upMsg:getMessage('view.help.for.portal', $USER_LANG)}" target="_blank">
        <span><xsl:value-of select="upMsg:getMessage('help', $USER_LANG)"/></span>
      </a>
    </li>
  </xsl:template>
  <!-- ========================================================= -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK LOGOUT ========== -->
  <!-- ========================================================= -->
  <!--
   | This template renders the logout link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.logout">
    <xsl:if test="$AUTHENTICATED='true'">
     <li class="link-logout">
        <a href="{$CONTEXT_PATH}/Logout" title="{upMsg:getMessage('log.off.and.exit', $USER_LANG)}">
          <span><xsl:value-of select="upMsg:getMessage('sign.out', $USER_LANG)"/></span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ========================================================= -->
  
  
  <!-- ========== TEMPLATE: LOGO ========== -->
  <!-- ==================================== -->
  <!--
   | This template renders the portal logo.
  -->
  <xsl:template name="logo">  
    <div id="portalLogo">
      <xsl:variable name="homeUrl">
        <xsl:call-template name="portalUrl"/>
      </xsl:variable>
      <a href="{$homeUrl}" title="{upMsg:getMessage('go.to.home', $USER_LANG)}">
        <xsl:choose>
          <xsl:when test="//focused">
            <!-- ****** LOGO FOCUSED BLOCK ****** -->
            <xsl:call-template name="logo.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** LOGO FOCUSED BLOCK ****** -->
          </xsl:when>
          <xsl:otherwise>
            <!-- ****** LOGO BLOCK ****** -->
            <xsl:call-template name="logo.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** LOGO BLOCK ****** -->
          </xsl:otherwise>
        </xsl:choose>
      </a>
    </div>
  </xsl:template>
  <!-- ==================================== -->
  
  
  <!-- ========== TEMPLATE: LOGIN ========== -->
  <!-- ===================================== -->
  <!--
   | This template renders the login form.
  -->
  <xsl:template name="login">
    <div id="portalLogin" class="fl-widget">
      <div class="fl-widget-inner">
        <div class="fl-widget-titlebar">
          <h2><xsl:value-of select="upMsg:getMessage('sign.in', $USER_LANG)"/></h2>
        </div>
        <xsl:choose>
          <xsl:when test="$EXTERNAL_LOGIN_URL != ''">
            <!-- If an external SSO system is configured, render the external login link -->
            <xsl:call-template name="external.login"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Otherwise render the local login form -->
            <xsl:call-template name="local.login"/>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
    <div id="guestLocaleSelection">
        <xsl:copy-of select="//channel[@fname = 'user-locales-selector']"/>
    </div>
  </xsl:template>
  <!-- ===================================== -->
  
  
  <!-- ========== TEMPLATE: LOGIN CHANNEL ========== -->
  <!-- ============================================= -->
  <!--
   | This template renders the CLogin channel.
  -->
  <xsl:template name="local.login">
    <xsl:copy-of select="//channel[@fname='login']"/>
  </xsl:template>
  <!-- ============================================= -->
  
  
  <!-- ========== TEMPLATE: CAS LOGIN ========== -->
  <!-- ========================================= -->
  <!--
   | This template renders links for CAS login.
  -->
  <xsl:template name="external.login">  
    <div id="portalCASLogin" class="fl-widget-content">
      <a id="portalCASLoginLink" class="button" href="{$EXTERNAL_LOGIN_URL}" title="{upMsg:getMessage('sign.in.via.cas', $USER_LANG)}">
        <span><xsl:value-of select="upMsg:getMessage('sign.in', $USER_LANG)"/><!--&#160;<span class="via-cas"><xsl:value-of select="upMsg:getMessage('with.cas', $USER_LANG)"/></span>--></span>
      </a>
      <p><xsl:value-of select="upMsg:getMessage('new.user.question', $USER_LANG)"/>&#160; 
        <a id="portalCASLoginNewLink" href="{$CAS_NEW_USER_URL}" title="{upMsg:getMessage('create.new.portal.account', $USER_LANG)}">
          <xsl:value-of select="upMsg:getMessage('new.user', $USER_LANG)"/>
        </a>.
      </p>
    </div>
  </xsl:template>
  <!-- ========================================= -->
  
  
  <!-- ========== TEMPLATE: WELCOME ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders links for CAS login.
  -->
  <xsl:template name="welcome">  
    <xsl:if test="$AUTHENTICATED='true'"> <!-- Welcome only displays if the user is logged in. -->
      <div id="portalWelcome">
        <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
        <xsl:choose>
          <xsl:when test="$userImpersonating = 'true'">
            <xsl:value-of select="upMsg:getMessage('you.are.idswapped.as', $USER_LANG)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="upMsg:getMessage('you.are.signed.in.as', $USER_LANG)"/>
          </xsl:otherwise>
        </xsl:choose>
        &#160;<span class="user-name"><xsl:value-of select="$USER_NAME"/></span>
        <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ======================================= -->
  
  
  <!-- ========== TEMPLATE: ADMINISTRATION LINKS ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the administration links navigation component, a prominent, short list of links to administrative functions.
  -->
  <xsl:template name="administration.links">
  	<xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin') or upGroup:isUserDeepMemberOfGroupName($USER_ID, 'Fragment Owners')">
      <div id="portalAdminLinks" class="fl-widget">
        <div class="fl-widget-inner">
        	<div class="fl-widget-titlebar">
          	<h2><xsl:value-of select="upMsg:getMessage('administration', $USER_LANG)"/></h2>
          </div>
          <div class="fl-widget-content">
            <ul class="fl-listmenu">
              <xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin')">
                <li id="portalAdminLinksPortletAdmin">
                  <xsl:variable name="portletAdminUrl">
                    <xsl:call-template name="portalUrl">
                        <xsl:with-param name="url">
                            <url:portal-url>
                                <url:fname>portlet-admin</url:fname>
                                <url:portlet-url state="MAXIMIZED" />
                            </url:portal-url>
                        </xsl:with-param>
                    </xsl:call-template>
                  </xsl:variable>
                  <a href="{$portletAdminUrl}" title="{portlet.manager}">
                    <span><xsl:value-of select="upMsg:getMessage('go.to.portlet.manager', $USER_LANG)"/></span>
                  </a>
                </li>
              </xsl:if>
            </ul>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ========================================== -->
  
  <!-- ========== TEMPLATE: QUICKLINKS ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the quicklinks navigation component, a prominent, short list of links to high priority portlets regardless of the portlet's placement within the layout.
  -->
  <xsl:template name="quicklinks">
  	<xsl:if test="count(/layout/navigation/descendant::tabChannel[@quicklink > 0]) > 0"> <!-- Write out markup only if one or more quicklinks exist. -->
      <div id="portalQuicklinks" class="fl-widget">
      	<div class="fl-widget-inner">
        	<div class="fl-widget-titlebar">
        		<h2><xsl:value-of select="upMsg:getMessage('quicklinks', $USER_LANG)"/></h2>
          </div>
        	<div class="fl-widget-content">
            <ul class="fl-listmenu">  <!-- Navigation list. -->
              <xsl:apply-templates select="/layout/navigation/descendant::tabChannel[@quicklink > 0]" mode="quicklink"> <!-- Selects from the XML only those portlets with the matching quicklink parameter. -->
                <xsl:sort select="@quicklink" order="ascending" /> <!-- Sorts the subsequent array in ascending order by the value of the quicklink parameter. -->
              </xsl:apply-templates>
            </ul>
      		</div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ========================================== -->
	
  
  <!-- ========== TEMPLATE: QUICKLINKS LIST ========== -->
  <!-- =============================================== -->
  <!-- 
   | This template renders quicklinks navigation list.
  -->
  <xsl:template match="tabChannel" mode="quicklink">
    <xsl:variable name="qLinkID" select="@qID" /> <!-- Pull the unique ID from the portlet parameter in the XML if one exists. -->
    <xsl:variable name="POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">single</xsl:when>
        <xsl:when test="position()=1">first</xsl:when>
        <xsl:when test="position()=last()">last</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <li id="{$qLinkID}" class="{$POSITION}"> <!-- Each subnavigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
      <xsl:variable name="subNavUrl">
        <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <a href="{$subNavUrl}" title="{@title}">  <!-- Navigation item link. -->
        <span>
          {up-portlet-title(<xsl:value-of select="@ID" />)}
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- =============================================== -->
	
  
  <!-- ========== TEMPLATE: WEB SEARCH ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the web search component, a search form that forwards the search query to a search portlet.
  -->
  <xsl:template name="web.search">
    <div id="webSearchContainer" class="fl-widget">
      <div class="fl-widget-inner">
      	<div class="fl-widget-titlebar">
      		<h2><label for="webSearchInput"><xsl:value-of select="upMsg:getMessage('web.search', $USER_LANG)"/></label></h2>
        </div>
        <div class="fl-widget-content">
            <xsl:variable name="searchUrl">
                <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url type="ACTION">
                            <url:fname>search</url:fname>
                            <url:portlet-url state="MAXIMIZED" />
                        </url:portal-url>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <form method="post" action="{$searchUrl}" id="webSearchForm">
              <input id="webSearchInput" value="" name="query" type="text" />
              <input id="webSearchSubmit" type="submit" name="submit" value="{upMsg:getMessage('search', $USER_LANG)}" />
            </form>
        </div>
      </div>
    </div>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: BREADCRUMB ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the page breadcrumb.
  -->
  <xsl:template name="breadcrumb">
      <div id="portalPageBodyBreadcrumb">
      <xsl:variable name="basePortalUrl">
        <xsl:call-template name="portalUrl"/>
      </xsl:variable>
      <a href="{$basePortalUrl}" title="{upMsg:getMessage('go.to.home', $USER_LANG)}"><xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/></a>
      <span class="breadcrumb-separator">&gt;</span>
      <xsl:for-each select="/layout/navigation/tab">
        <xsl:if test="@activeTab='true'">
          <xsl:variable name="tabUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                    <url:portal-url>
                        <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    </url:portal-url>
                </xsl:with-param>
            </xsl:call-template>
          </xsl:variable>
          <a href="{$tabUrl}">
            <xsl:attribute name="title"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:value-of select="@name"/>
          </a>
        </xsl:if>
      </xsl:for-each>
      <span class="breadcrumb-separator">&gt;</span>
    </div>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: PAGE TITLE ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the page title.
  -->
  <xsl:template name="page.title">
  	<a name="pageContent" class="skip-link" title="Reference anchor: the starting point of the page content"> <!-- Skip navigation target. -->
      <h1 id="portalPageBodyTitle">
        <xsl:choose>
          <xsl:when test="//focused"> <!-- When focused, include the focused portlet title -->
            {up-portlet-title(<xsl:value-of select="//focused/channel/@ID" />)}
          </xsl:when>
          <xsl:otherwise> <!-- Otherwise, just the current tab name -->
            <xsl:value-of select="/layout/navigation/tab[@activeTab='true']/@name"/>
          </xsl:otherwise>
        </xsl:choose>
      </h1>
    </a>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: BACK TO HOME ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders Back To Home link form the focused view.
  -->
  <xsl:template name="back.to.home">
  	<xsl:if test="//focused">
      <xsl:variable name="homeUrl">
        <xsl:call-template name="portalUrl"/>
      </xsl:variable>
      <a href="{$homeUrl}" id="portalBackToHome" title="{upMsg:getMessage('back.to.home.long', $USER_LANG)}">
        <span><xsl:value-of select="upMsg:getMessage('back.to.home', $USER_LANG)"/></span>
      </a>
  	  <xsl:if test="//focused[@in-user-layout='no'] and $USE_AJAX='true' and $AUTHENTICATED">
  	    <a href="javascript:;" id="focusedContentDialogLink" title="{upMsg:getMessage('back.to.home.long', $USER_LANG)}">
  	      <span><xsl:value-of select="upMsg:getMessage('back.to.home', $USER_LANG)"/></span>
  	    </a>
 	    </xsl:if>
  	</xsl:if>
  </xsl:template>
  <!-- ============================================ -->
  
  <!-- ====== TEMPLATE: CUSTOMIZE MESSAGE ======= -->
  <!-- ========================================== -->
  <!--
   | This template renders the customize page message. This message should only 
   | render when no portlets have been added to a users layout.
  -->
  <xsl:template name="page.customize.message">
      <h1><xsl:value-of select="upMsg:getMessage('customize.this.page', $USER_LANG)"/></h1>
      <p><xsl:value-of select="upMsg:getMessage('customize.this.page.description', $USER_LANG)"/></p>
  </xsl:template>
  <!-- ========================================== -->
  
</xsl:stylesheet>
