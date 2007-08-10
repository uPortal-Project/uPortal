<?xml version="1.0" encoding="utf-8"?>

<!-- ========== LEGAL, NOTES, AND INSTRUCTIONS ========== -->
<!--
Copyright (c) 2006 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."

THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.


Description: Stylesheet for version number reporting channel.

This is the world's simplest stylesheet.  It grabs the XSLT parameter named "version"
and displays its value.

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
