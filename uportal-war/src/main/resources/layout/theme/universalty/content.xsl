<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!--
 | This file determines the presentation of portlet (and channel) containers.
 | Portlet content is rendered outside of the theme, handled entirely by the portlet itself.
 | The file is imported by the base stylesheet xhtml-theme.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to xhtml-theme.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dlm="http://www.uportal.org/layout/dlm">
  
  
  <!-- ========== TEMPLATE: PORTLET ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the portlet containers: chrome and controls.
  -->
  <xsl:template match="channel">
    
    <!-- Tests for optional portlet parameter removeFromLayout which can be used to not render a portlet in the layout.  The main use case for this function is to have a portlet be a quicklink and then remove it from otherwise rendering. -->
    <xsl:if test="not(./parameter[@name='removeFromLayout']/@value='true') and not(./parameter[@name='PORTLET.removeFromLayout']/@value='true')">
    
    <!-- ****** PORTLET CONTAINER ****** -->
      <div id="portlet_{@ID}" class="portlet-container {@fname} dojoDndItem"> <!-- Main portlet container.  The unique ID is needed for drag and drop.  The portlet fname is also written into the class attribute to allow for unique rendering of the portlet presentation. -->
    
      <!-- ****** PORTLET TOP BLOCK ****** -->
      <xsl:call-template name="portlet.top.block"/> <!-- Calls a template of institution custom content from xhtml-theme.xsl. -->
      <!-- ****** PORTLET TOP BLOCK ****** -->
      
      <!-- ****** PORTLET TITLE AND TOOLBAR ****** -->
      <div id="toolbar_{@ID}" class="portlet-toolbar dojoDndHandle"> <!-- Portlet toolbar. -->
        <a name="{@ID}" id="{@ID}"></a> <!-- ?????? WHAT IS THIS FOR ??????-->
        <h2> <!-- Portlet title. -->
          <a href="{$BASE_ACTION_URL}?uP_root={@ID}">
            UP:CHANNEL_TITLE-{<xsl:value-of select="@ID" />}
          </a>
        </h2>
        <xsl:if test="//layout"> <!-- As long as the portlet is not detached, render the portlet controls. -->
        	<xsl:call-template name="controls"/>
        </xsl:if>
      </div>
      
      <!-- ****** PORTLET CONTENT ****** -->
      <div id="portletContent_{@ID}" class="portlet-content"> <!-- Portlet content container. -->
      	<div class="portlet-conent-inner">  <!-- Inner div for additional presentation/formatting options. -->
      		<xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
        </div>
      </div>
      
      <!-- ****** PORTLET BOTTOM BLOCK ****** -->
      <xsl:call-template name="portlet.bottom.block"/> <!-- Calls a template of institution custom content from xhtml-theme.xsl. -->
      <!-- ****** PORTLET BOTTOM BLOCK ****** -->
    
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
  	<xsl:apply-templates select="channel" mode="focused"/>
  </xsl:template>
  
  <xsl:template match="channel" mode="focused">
    <div id="portlet_{@ID}" class="portletContainer">  <!-- Portlet container. -->
      <div id="toolbar_{@ID}" class="portlet-toolbar">  <!-- Render the portlet toolbar. -->
      	<xsl:call-template name="controls"/> <!-- Call the portlet controls into the toolbar. -->
      </div>
      <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
    </div>
  </xsl:template>
  <!-- ============================================== -->
  
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->
  <!-- This template renders portlet controls.  Each control has a unique class for assigning icons or other specific presentation. -->
  <xsl:template name="controls">
    <div class="portlet-controls">
      <xsl:if test="not(@hasHelp='false')"> <!-- Help. -->
      	<a href="{$BASE_ACTION_URL}?uP_help_target={@ID}#{@ID}" title="{$TOKEN[@name='PORTLET_HELP_LONG_LABEL']}" class="portlet-control-help">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_HELP_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@hasAbout='false')"> <!-- About. -->
      	<a href="{$BASE_ACTION_URL}?uP_about_target={@ID}#{@ID}" title="{$TOKEN[@name='PORTLET_ABOUT_LONG_LABEL']}" class="portlet-control-about">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_ABOUT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@editable='false')"> <!-- Edit. -->
      	<a href="{$BASE_ACTION_URL}?uP_edit_target={@ID}#{@ID}" title="{$TOKEN[@name='PORTLET_EDIT_LONG_LABEL']}" class="portlet-control-edit">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_EDIT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="@printable='true'"> <!-- Print. -->
      	<a href="{$BASE_ACTION_URL}?uP_print_target={@ID}#{@ID}" title="{$TOKEN[@name='PORTLET_PRINT_LONG_LABEL']}" class="portlet-control-print">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_PRINT_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(//focused)"> <!-- Focus. -->
      	<a href="{$BASE_ACTION_URL}?uP_root={@ID}" title="{$TOKEN[@name='PORTLET_MAXIMIZE_LONG_LABEL']}" class="portlet-control-focus">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_MAXIMIZE_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="not(@dlm:deleteAllowed='false') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
      	<a id="removePortlet_{@ID}" title="{$TOKEN[@name='PORTLET_REMOVE_LONG_LABEL']}" href="{$BASE_ACTION_URL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')" class="portlet-control-remove">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_REMOVE_LABEL']"/></span>
        </a>
      </xsl:if>
      <xsl:if test="//focused[@in-user-layout='no']"> <!-- Add to layout. -->
      	<a href="{$BASE_ACTION_URL}" title="{$TOKEN[@name='PORTLET_ADD_LONG_LABEL']}" class="portlet-control-add">
        	<span><xsl:copy-of select="$TOKEN[@name='PORTLET_ADD_LABEL']"/></span>
        </a>
      </xsl:if>
    </div>
  </xsl:template>
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->
		
</xsl:stylesheet>
