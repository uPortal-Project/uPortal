<?xml version="1.0" encoding="utf-8"?>
<!-- about.xsl, part of the HelloWorld example channel -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" />
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="name_prev">world</xsl:param>

  <xsl:template match="/">
    <a name="helloworld" />
    <xsl:apply-templates />
    <p><a href="{$baseActionURL}?back=true">Back</a></p>
  </xsl:template>

  <xsl:template match="about">
    <p><xsl:value-of select="." /></p>
  </xsl:template>
</xsl:stylesheet>
