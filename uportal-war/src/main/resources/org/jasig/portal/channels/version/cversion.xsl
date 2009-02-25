<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!-- ========== END LEGAL, NOTES, AND INSTRUCTIONS ========== -->


<!-- Defines this document as an XSL stylesheet, conforming to the XSL 1.0 specification and XSL namespace as defined in the reference http://www.w3.org/1999/XSL/Transform. Final output will be HTML (as specified in the output:method attribute) and the indent attribute set to "yes" simply attempts to keep the HTML hierarchical structure. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!--
	The only parameter used by this stylesheet is the version string.
	 -->
    <xsl:param name="version" select="'unknown"/>


	<!-- Using XML as output type for XHTML compliance -->
	<xsl:output method = "xml"  version="1.0" encoding="utf-8" omit-xml-declaration="yes" indent="yes"  />

    <xsl:template match="/">
        <span><xsl:value-of select="{$version}"/></span>
    </xsl:template>


</xsl:stylesheet>
