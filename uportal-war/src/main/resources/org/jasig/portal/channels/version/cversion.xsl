<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

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
