<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>


<xsl:template match="header">
<p align="left"><xsl:value-of select="title"/></p>
</xsl:template>


</xsl:stylesheet>
