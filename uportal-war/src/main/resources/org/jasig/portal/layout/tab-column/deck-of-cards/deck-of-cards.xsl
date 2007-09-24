<?xml version='1.0' encoding='utf-8' ?>

<!--
Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.
   
3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."
   
THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: Ken Weiner, kweiner@unicon.net
$Revision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>

  <xsl:template match="layout">
    <wml>
      <template>
        <!-- A quick way to return to the top level of the layout -->
        <do type="accept" label="Home">
          <go href="{$baseActionURL}?uP_root=root#layout"></go>
        </do>
      </template>

      <!-- Splash screen -->
      <!-- I thought about having a splash screen so we wouldn't have to display the header on every
           page, but it caused problems with screen refreshing -Ken -->
      <!--
      <card id="header" title="Welcome">
        <do type="accept" label="Continue"><go href="#layout"></go></do>
        <xsl:apply-templates select="header//channel[@name != 'Login']" mode="render-channel"/>
      </card>
      -->

      <!-- Navigate tabs -->
      <card id="layout" title="uPortal">

        <!-- Display all the header channels except the login channel -->
        <xsl:apply-templates select="header//channel[@name != 'Login']" mode="render-channel"/>

        <!-- It would be nice to toggle the label, but I'm not sure how (please help!) -->
        <do type="options" label="Login/Logout"><go href="#login"></go></do>

        <!-- Render the list of tabs.  -->
        <xsl:apply-templates select="navigation"/>
        
        <!-- Render the channels in the current tab. -->
        <xsl:apply-templates select="content"/>

        <!-- Display the footer, if any -->
        <xsl:apply-templates select="footer"/>
      </card>

      <!-- Login screen/logout link -->
      <card id="login" title="Login">
        <!-- should maybe select fname instead of name, come back and fix later -->
        <xsl:apply-templates select="header/channel[@name = 'Login']" mode="render-channel"/>    
      </card>
    
    </wml>
  </xsl:template>

  <xsl:template match="header">
    <xsl:for-each select="channel">
      <xsl:copy-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="navigation">
    <p mode="nowrap"><small>Tabs:</small></p>
    <xsl:apply-templates select="tab"/>
    <do type="prev" label="Back"><prev/></do>
  </xsl:template>

  <xsl:template match="content">
    <xsl:choose>
      <xsl:when test="count(.//channel) &gt; 1">
        <p mode="nowrap"><small>Channels on <xsl:value-of select="/layout/navigation/tab[@activeTab='true']/@name"/>:</small></p>
        <xsl:apply-templates select=".//channel"/>
      </xsl:when>
      <xsl:otherwise>
        <p mode="nowrap"><small>Channel:</small></p>      
        <xsl:apply-templates select=".//channel" mode="render-channel"/>
      </xsl:otherwise>
    </xsl:choose>
    <do type="prev" label="Back"><prev/></do>
  </xsl:template>

  <xsl:template match="footer">
    <xsl:for-each select="channel">
      <xsl:copy-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="tab">
    <p mode="nowrap"><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={position()}#layout">
      <xsl:value-of select="@name"/>
    </a></p>
  </xsl:template>

  <xsl:template match="channel">
    <p mode="nowrap"><a href="{$baseActionURL}?uP_root={@ID}#layout"><xsl:value-of select="@title"/></a></p>
  </xsl:template>

  <xsl:template match="channel" mode="render-channel">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
