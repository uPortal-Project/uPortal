<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="/">
  <center><xsl:apply-templates select="applet"/></center>
</xsl:template>

<xsl:template match="applet">
  <applet code="{@code}"
          codebase="{@codebase}"
          width="{@width}"
          height="{@height}"
          align="{@align}"
          archive="{@archive}">
  <xsl:apply-templates/>
  </applet>
</xsl:template>

<xsl:template match="param">
  <param name="{@name}" value="{@value}"/>
</xsl:template>

</xsl:stylesheet> 
