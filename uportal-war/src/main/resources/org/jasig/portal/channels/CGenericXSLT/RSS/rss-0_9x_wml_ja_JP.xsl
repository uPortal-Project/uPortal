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

Author: Ken Weiner, kweiner@interactivebusiness.com
Author: Peter Kharchenko, pkharchenko@unicon.net
$Revision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:param name="startItem">1</xsl:param>

	<!-- You may want to configure this variable to show more items per screen -->
  <xsl:variable name="itemsPerScreen">1</xsl:variable>
  
	<xsl:param name="baseActionURL">actionURLnotPassed</xsl:param>

        <xsl:param name="locale">ja_JP</xsl:param>

  <xsl:template match="rss">
    <xsl:apply-templates select="channel"/>
    <p align="center">

      <!-- "Next" link -->
      <xsl:if test="count(channel/item) - $startItem &gt;= $itemsPerScreen">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?startItem=<xsl:value-of select="$startItem + $itemsPerScreen"/>
          </xsl:attribute>
          <xsl:call-template name="nav-text">
            <xsl:with-param name="label" select="string('次')"/>
          </xsl:call-template>
        </a>
      </xsl:if>

      <!-- "Prev" link -->
      <xsl:if test="$startItem - $itemsPerScreen &gt; 0">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?startItem=<xsl:value-of select="$startItem - $itemsPerScreen"/>
          </xsl:attribute>
          <xsl:call-template name="nav-text">
            <xsl:with-param name="label" select="string('前')"/>
          </xsl:call-template>
        </a>
      </xsl:if>
    </p>

  </xsl:template>

  <xsl:template match="channel">
    <p align="center"><big><xsl:value-of select="title"/></big></p>
    <xsl:apply-templates select="item[position() &gt;= $startItem and position() &lt; ($startItem + $itemsPerScreen)]"/>
  </xsl:template>

  <xsl:template match="item">
    <p align="center">---Item <xsl:number/> of <xsl:value-of select="count(/rss/channel/item)"/>---</p>
    <p align="left"><a href="{link}"><xsl:value-of select="title"/></a></p>
    <p align="left"><xsl:value-of select="description"/></p>
  </xsl:template>

  <xsl:template name="nav-text">
    <xsl:param name="label"/>
    <xsl:value-of select="$label"/><xsl:text> </xsl:text>
    <xsl:if test="$itemsPerScreen != 1"><xsl:value-of select="$itemsPerScreen"/></xsl:if>
    <xsl:text> </xsl:text>item<xsl:if test="$itemsPerScreen != 1">s</xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
