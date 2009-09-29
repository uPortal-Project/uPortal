<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!--
 | This file determines the presentation of portlet (and channel) containers.
 | Portlet content is rendered outside of the theme, handled entirely by the portlet itself.
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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:dlm="http://www.uportal.org/layout/dlm"
  xmlns:portal="http://www.jasig.org/uportal/XSL/portal"
  xmlns:portlet="http://www.jasig.org/uportal/XSL/portlet"
  xmlns:layout="http://www.jasig.org/uportal/XSL/layout"
  xmlns:upAuth="xalan://org.jasig.portal.security.xslt.XalanAuthorizationHelper"
  xmlns:upGroup="xalan://org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
  extension-element-prefixes="portal portlet layout" 
  exclude-result-prefixes="xalan portal portlet layout upAuth upGroup" 
  version="1.0">
  
  <xalan:component prefix="portal" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortalUrlXalanElements" />
  </xalan:component>
  <xalan:component prefix="portlet" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortletUrlXalanElements" />
  </xalan:component>
  <xalan:component prefix="layout" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.LayoutUrlXalanElements" />
  </xalan:component>
<!-- ============================================= -->
  
  <!-- ========== TEMPLATE: PORTLET ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the portlet containers: chrome and controls.
  -->
  <xsl:template match="channel">
    <!-- This variable appears to be obsolete.
    <xsl:variable name="portletClassName">
      up-portlet-container <xsl:value-of select="@fname"/> 
    </xsl:variable>-->
    
    <xsl:variable name="PORTLET_LOCKED"> <!-- Test to determine if the portlet is locked in the layout. -->
      <xsl:choose> 
        <xsl:when test="@dlm:moveAllowed='false'">locked</xsl:when> 
        <xsl:otherwise>moveable</xsl:otherwise> 
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
    
    <!-- Tests for optional portlet parameter removeFromLayout which can be used to not render a portlet in the layout.  The main use case for this function is to have a portlet be a quicklink and then remove it from otherwise rendering. -->
    <xsl:if test="not(./parameter[@name='removeFromLayout']/@value='true') and not(./parameter[@name='PORTLET.removeFromLayout']/@value='true')">
    
    <!-- ****** PORTLET CONTAINER ****** -->
    <div id="portlet_{@ID}" class="up-portlet-container {@fname} {$PORTLET_LOCKED} {$PORTLET_CHROME} {$PORTLET_ALTERNATE} {$PORTLET_HIGHLIGHT}"> <!-- Main portlet container.  The unique ID is needed for drag and drop.  The portlet fname is also written into the class attribute to allow for unique rendering of the portlet presentation. -->
          
        <!-- PORTLET CHROME CHOICE -->
        <xsl:choose>
          <!-- ***** REMOVE CHROME ***** -->
          <xsl:when test="parameter[@name = 'showChrome']/@value = 'false'">
              <!-- ****** START: PORTLET CONTENT ****** -->
              <div id="portletContent_{@ID}" class="up-portlet-content"> <!-- Portlet content container. -->
                <div class="up-portlet-content-inner">  <!-- Inner div for additional presentation/formatting options. -->
                  <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
                </div>
              </div>
          </xsl:when>
        
          <!-- ***** RENDER CHROME ***** -->
          <xsl:otherwise>
            <div class="up-portlet-container-inner">
            <!-- ****** PORTLET TOP BLOCK ****** -->
            <xsl:call-template name="portlet.top.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** PORTLET TOP BLOCK ****** -->
            
            <!-- ****** PORTLET TITLE AND TOOLBAR ****** -->
            <div id="toolbar_{@ID}" class="up-portlet-toolbar"> <!-- Portlet toolbar. -->
              <h2> <!-- Portlet title. -->
                <xsl:variable name="portletMaxUrl">
                  <portlet:url layoutId="{@ID}" state="MAXIMIZED"/>
                </xsl:variable>
                <a name="{@ID}" id="{@ID}" href="{$portletMaxUrl}"> <!-- Reference anchor for page focus on refresh and link to focused view of channel. -->
                  UP:CHANNEL_TITLE-{<xsl:value-of select="@ID" />}
                </a>
              </h2>
              <xsl:if test="//layout"> <!-- As long as the portlet is not detached, render the portlet controls. -->
                <xsl:call-template name="controls"/>
              </xsl:if>
            </div>
            
            <!-- ****** PORTLET CONTENT ****** -->
            <div id="portletContent_{@ID}" class="up-portlet-content fl-fix"> <!-- Portlet content container. -->
              <div class="up-portlet-content-inner">  <!-- Inner div for additional presentation/formatting options. -->
                <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
              </div>
            </div>
            
            <!-- ****** PORTLET BOTTOM BLOCK ****** -->
            <xsl:call-template name="portlet.bottom.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** PORTLET BOTTOM BLOCK ****** -->
            </div>
          </xsl:otherwise>
        </xsl:choose>

    </div>
    
    </xsl:if>
  
  </xsl:template>
  <!-- ======================================= -->
  
  
  <!-- ========== TEMPLATE: PORLET FOCUSED ========== -->
  <!-- ============================================== -->
  <!--
   | These two templates render the focused portlet content.
  -->
  <xsl:template match="focused">
  	<div class="portal-page-column single">
    	<div class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
  			<xsl:apply-templates select="channel" mode="focused"/>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template match="channel" mode="focused">
    <div id="portlet_{@ID}" class="up-portlet-container">  <!-- Portlet container. -->
      <div id="toolbar_{@ID}" class="up-portlet-toolbar">  <!-- Render the portlet toolbar. -->
      	<xsl:call-template name="controls"/> <!-- Call the portlet controls into the toolbar. -->
      </div>
      <div id="portletContent_{@ID}" class="up-portlet-content fl-fix">
      	<div class="up-portlet-content-inner">
      		<xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
    		</div>
    	</div>
    </div>
  </xsl:template>
  <!-- ============================================== -->
  
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->
  <!-- This template renders portlet controls.  Each control has a unique class for assigning icons or other specific presentation. -->
  <xsl:template name="controls">
    <div class="up-portlet-controls">
      <xsl:if test="not(@hasHelp='false')"> <!-- Help. -->
        <xsl:variable name="portletHelpUrl">
          <portlet:url layoutId="{@ID}" mode="HELP"/>
        </xsl:variable>
        <a href="{$portletHelpUrl}#{@ID}" title="{$TOKEN[@name='PORTLET_HELP_LONG_LABEL']}" class="up-portlet-control help">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_HELP_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@hasAbout='false')"> <!-- About. -->
        <xsl:variable name="portletAboutUrl">
          <portlet:url layoutId="{@ID}" mode="ABOUT"/>
        </xsl:variable>
      	<a href="{$portletAboutUrl}#{@ID}" title="{$TOKEN[@name='PORTLET_ABOUT_LONG_LABEL']}" class="up-portlet-control about">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_ABOUT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@editable='false')"> <!-- Edit. -->
        <xsl:variable name="portletEditUrl">
          <portlet:url layoutId="{@ID}" mode="EDIT"/>
        </xsl:variable>
        <a href="{$portletEditUrl}#{@ID}" title="{$TOKEN[@name='PORTLET_EDIT_LONG_LABEL']}" class="up-portlet-control edit">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_EDIT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="@printable='true'"> <!-- Print. -->
        <xsl:variable name="portletPrintUrl">
          <portlet:url layoutId="{@ID}" state="PRINT"/>
        </xsl:variable>
        <a href="{$portletPrintUrl}#{@ID}" title="{$TOKEN[@name='PORTLET_PRINT_LONG_LABEL']}" class="up-portlet-control print">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_PRINT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(//focused)"> <!-- Focus. -->
        <xsl:variable name="portletMaxUrl">
          <portlet:url layoutId="{@ID}" state="MAXIMIZED"/>
        </xsl:variable>
        <a href="{$portletMaxUrl}" title="{$TOKEN[@name='PORTLET_MAXIMIZE_LONG_LABEL']}" class="up-portlet-control focus">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_MAXIMIZE_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@dlm:deleteAllowed='false') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
        <xsl:variable name="removePortletUrl">
          <layout:url layoutId="{@ID}" renderInNormal="true" action="true">
            <layout:param name="remove_target" value="{@ID}"/>
          </layout:url>
        </xsl:variable>
        <a id="removePortlet_{@ID}" title="{$TOKEN[@name='PORTLET_REMOVE_LONG_LABEL']}" href="{$removePortletUrl}" class="up-portlet-control remove">
      	  <span><xsl:value-of select="$TOKEN[@name='PORTLET_REMOVE_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="//focused[@in-user-layout='no'] and upGroup:isChannelDeepMemberOf(//focused/channel/@fname, 'local.1')"> <!-- Add to layout. -->
        <a id="focusedContentDialogLink" href="javascript:;" title="{$TOKEN[@name='PORTLET_ADD_LONG_LABEL']}" class="up-portlet-control add">
          <span><xsl:value-of select="$TOKEN[@name='PORTLET_ADD_LABEL']"/></span>
        </a>
      </xsl:if>
    </div>
  </xsl:template>
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->
		
</xsl:stylesheet>
