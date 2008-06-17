<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>

<xsl:template match="/">
	<xsl:apply-templates select="missingProperties"/>
</xsl:template>

<xsl:template match="missingProperties">
	<table class="uportal-background-content">
	<xsl:apply-templates select="property"/>
	</table>
</xsl:template>



<xsl:template match="property">
	<tr>
	<td class="uportal-background-content">
	<xsl:value-of select="self::property"/>
	</td>
	</tr>
</xsl:template>
</xsl:stylesheet>

<!--
$Revision$ $Date$
MissingProperties.xsl : an XSLT suitable for rendering as a uPortal channel the XML produced by the
MissingPropertiesServlet.
@author andrew.petro@yale.edu
-->