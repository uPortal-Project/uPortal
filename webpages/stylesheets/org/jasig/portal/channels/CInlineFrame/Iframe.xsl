<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <xsl:template match="IFrame" >
<iframe>
<xsl:attribute name="src"><xsl:value-of select="url"/></xsl:attribute>
<xsl:attribute name="height"><xsl:value-of select="height"/></xsl:attribute>
<xsl:attribute name="frameborder">no</xsl:attribute>
<xsl:attribute name="width">100%</xsl:attribute>
</iframe> 
 </xsl:template>

</xsl:stylesheet>
