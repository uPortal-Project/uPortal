<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:template match="data">
 <h2>a simple title</h2>
 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="text">
  text tag<br></br>
	<Strong><xsl:value-of select="."/></Strong>
</xsl:template>
</xsl:stylesheet>
