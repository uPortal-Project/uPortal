<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="guest">false</xsl:param>

<xsl:template match="header">
<p align="left">Header Channel :)<br/><xsl:value-of select="title"/>
<xsl:if test="$guest='false'"><br/><a href="logout.jsp">[Logout]</a></xsl:if>
</p>
</xsl:template>


</xsl:stylesheet>
