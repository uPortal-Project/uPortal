<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>
  
  <xsl:template match="error">
    <div class="uportal-background-content">
      <xsl:value-of select="."/>
    </div>
  </xsl:template>
  
  <xsl:template match="news">
    
    <div class="uportal-background-content">
      <p class="uportal-channel-subtitle">

        <xsl:apply-templates select="image"/>
        <!-- <a target="_blank" href="{link}"> --><xsl:value-of select="desc"/><!-- </a> -->
      </p>
      <div class="news-items">
        <xsl:apply-templates select="items"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="image">
    <a target="_blank" href="{link}">
      <img src="{url}" alt="{description}" class="news-feed-img" style="float: right; border-style: none;"/>
    </a>
  </xsl:template>
  
  <xsl:template match="items">
    <ul style="clear:right;list-style-image:url(media/org/jasig/portal/channels/CGenericXSLT/bullet.gif);margin-left: 0px;">
      <xsl:apply-templates select="item"/>
    </ul>
  </xsl:template>
  
  <xsl:template match="item">
    <li class="uportal-channel-text">
      <a target="_blank" href="{link}" class="uportal-channel-subtitle-reversed"><xsl:value-of select="title"/></a>
      <xsl:apply-templates select="description"/>
    </li>
  </xsl:template>
  
  <xsl:template match="description">
    <br/><xsl:apply-templates select="@*|node()" mode="copy"/>
  </xsl:template>
  
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
