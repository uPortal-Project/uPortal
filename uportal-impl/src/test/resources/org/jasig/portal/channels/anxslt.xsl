<?xml version='1.0'?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
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
anxslt.xsll : a stand-in XSLT that CAbstractXSLTTest uses to test CAbstractXSLT error handling behavior.
@since uPortal 2.5
-->