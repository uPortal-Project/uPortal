<?xml version="1.0" encoding='utf-8'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">render.uP</xsl:param>

<xsl:template match="layout">
  <wml>
    <template>
      <do type="accept" label="Home"><go href="{$baseActionURL}?uP_root=root"></go></do>
    </template>  
    <card id="uPortal">
      <xsl:attribute name="title">
        <xsl:choose>
          <xsl:when test="not(count(content/category) + count(content/channel) = 1)">Home</xsl:when>
          <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <xsl:apply-templates select="header"/>
      <xsl:apply-templates select="content"/>
      <xsl:apply-templates select="footer"/>
      
    </card>
  </wml>
</xsl:template>

<xsl:template match="header">
  <xsl:for-each select="channel">
    <xsl:copy-of select="."/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="content">
<xsl:choose>
  <xsl:when test="not(count(category) + count(channel) = 1)">
    <xsl:if test="category"><p mode="nowrap"><small>Categories:</small></p></xsl:if>
    <xsl:apply-templates select="category"/>
    <xsl:if test="channel"><p mode="nowrap"><small>Channels:</small></p></xsl:if>
    <xsl:apply-templates select="channel"/> 
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="display-channel"/> 
  </xsl:otherwise>
</xsl:choose>
  <do type="prev" label="Back"><prev/></do>
</xsl:template>

<xsl:template match="footer">
</xsl:template>

<xsl:template match="category">
  <p mode="nowrap"><a href="{$baseActionURL}?uP_root={@ID}"><xsl:value-of select="@name"/></a></p>
</xsl:template>

<xsl:template match="channel">
  <p mode="nowrap"><a href="{$baseActionURL}?uP_root={@ID}"><xsl:value-of select="@name"/></a></p>
</xsl:template>

<xsl:template name="display-channel">
  <xsl:copy-of select="channel"/>
</xsl:template>

</xsl:stylesheet>
