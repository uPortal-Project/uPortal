<?xml version='1.0' encoding='utf-8' ?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="authenticated">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:template match="header">
    <xsl:if test="$authenticated != 'false'">
      <a href="{$baseActionURL}?uP_root=root" class="uportal-navigation-category">Home </a> | 
      <a href="{$baseActionURL}?uP_fname=layout-sitemap" class="uportal-navigation-category"> Site Map </a> |
      <xsl:if test="chan-mgr-chanid">
      <a href="{$baseActionURL}?uP_fname={chan-mgr-chanid}" class="uportal-navigation-category"> Channel Admin </a> | 
      </xsl:if>
      <a href="{$baseActionURL}?uP_fname={preferences-chanid}" class="uportal-navigation-category"> Preferences </a> | <a href="Logout" class="uportal-navigation-category"> Logout </a>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
