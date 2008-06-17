<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:variable name="mediaPath">media/skins/universality</xsl:variable>
  <xsl:variable name="skin">java</xsl:variable>

  <xsl:template match="channel">
    <html>
    <head>
      <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css" />
    </head>
    <body>
     <xsl:copy-of select="child::*"/>
    </body></html>
  </xsl:template>

</xsl:stylesheet>
