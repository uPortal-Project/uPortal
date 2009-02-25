<?xml version='1.0'?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="iframe" >
  This browser does not support inline frames.<br/> 
  <a href="{url}" target="_blank">Click here to view content</a> in a separate window.
</xsl:template>

</xsl:stylesheet>
