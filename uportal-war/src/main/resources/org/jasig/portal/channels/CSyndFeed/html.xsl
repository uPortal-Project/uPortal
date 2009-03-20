<?xml version='1.0' encoding='utf-8' ?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>
  
  <xsl:template match="error">
    <div class="portlet-msg-error">
      <xsl:value-of select="."/>
    </div>
  </xsl:template>
  
  <xsl:template match="news">
    
    <div class="news-feed">
    	<div class="news-source">
        <xsl:apply-templates select="image"/>
        <p><xsl:value-of select="desc"/></p>
      </div>
      <div class="news-items">
        <xsl:apply-templates select="items"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="image">
    <a target="_blank" href="{link}">
      <img src="{url}" alt="{description}" class="news-feed-img"/>
    </a>
  </xsl:template>
  
  <xsl:template match="items">
    <ul>
      <xsl:apply-templates select="item"/>
    </ul>
  </xsl:template>
  
  <xsl:template match="item">
    <li>
      <a target="_blank" href="{link}" class="news-item-title"><xsl:value-of select="title"/></a>
      <span class="news-item-excerpt"><xsl:apply-templates select="description"/></span>
    </li>
  </xsl:template>
  
  <xsl:template match="description">
    <xsl:apply-templates select="@*|node()" mode="copy"/>
  </xsl:template>
  
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
