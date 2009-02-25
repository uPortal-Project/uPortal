<?xml version='1.0'?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">



<xsl:template match="iframe" >
<xsl:for-each select="warning">
 <p>
 <span class="uportal-channel-warning">
  <xsl:apply-templates/>
</span>
</p>
</xsl:for-each>
  現在の URL ： <xsl:value-of select="url"/><br/>
  <iframe src="{url}" height="{height}" frameborder="no" width="100%">dummyText</iframe>
</xsl:template>

</xsl:stylesheet>
