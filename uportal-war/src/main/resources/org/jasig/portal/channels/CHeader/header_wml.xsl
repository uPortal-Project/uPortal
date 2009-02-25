<?xml version='1.0' encoding='utf-8' ?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="authenticated">false</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/layout/nested-categories/deck-of-cards</xsl:variable>

  <xsl:template match="header"> 
    <p align="right"><img src="{$mediaPath}/uPortal-logo.wbmp" width="45" height="30" alt="uPortal 2.0"/></p>
    <p align="right"><small>Hello <xsl:value-of select="full-name"/></small></p>
    <p align="right"><small><xsl:value-of select="timestamp-short"/></small></p>
  </xsl:template>

</xsl:stylesheet>
