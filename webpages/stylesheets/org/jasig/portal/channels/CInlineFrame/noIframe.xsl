<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <xsl:template match="IFrame" >
This browser does not support inline frames.  
<a>
<xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
<xsl:attribute name="target">Iframe_window</xsl:attribute>
Click this link to view content
</a> in a separate window.
 </xsl:template>

</xsl:stylesheet>
