<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!-- ========================================================================= -->
<!-- ========== README ======================================================= -->
<!-- ========================================================================= -->
<!--
 | Date: 08/14/2008
 | Author: Matt Polizzotti
 | Company: Unicon,Inc.
 | uPortal Version: uP3.0.0 and uP3.0.1
 |
 | This file determines the focused page layout and presentation of the mobile portal.
 | The file is imported by the base stylesheet muniversality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to muniversality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: FOCUSED LAYOUT ===================================== -->
<!-- ========================================================================= -->
<!-- 
| RED
| The layout template (focused mode) defines the structure of the portal when content is focused. In 
| other words, when a user clicks a 'channel or portlet link' and the contents of that channel or 
| portlet are focused or rendered in the mobile device. Template contents can be any valid XSL or XHTML.
--> 
<xsl:template match="layout" mode="focused">
    <div class="mobile-focused">
        <xsl:call-template name="mobile.header.focused" />
        <xsl:call-template name="mobile.back.focused" />
        <xsl:call-template name="mobile.select.navigation.focused" />
        <xsl:call-template name="mobile.channel.title.focused" />
        <xsl:call-template name="mobile.channel.content.focused" />
        <xsl:call-template name="mobile.footer" />
    </div>
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>