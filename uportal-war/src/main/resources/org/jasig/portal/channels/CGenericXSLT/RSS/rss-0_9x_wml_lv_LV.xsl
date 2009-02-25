<?xml version='1.0' encoding='utf-8' ?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:param name="startItem">1</xsl:param>

	<!-- You may want to configure this variable to show more items per screen -->
  <xsl:variable name="itemsPerScreen">1</xsl:variable>
  
	<xsl:param name="baseActionURL">actionURLnotPassed</xsl:param>

        <xsl:param name="locale">lv_LV</xsl:param>

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
            <xsl:with-param name="label" select="string('Nākamais')"/>
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
            <xsl:with-param name="label" select="string('Iepriekšējais')"/>
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
