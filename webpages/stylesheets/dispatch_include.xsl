<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- A fix for dispatching an IXMLChannel in uPortal 1.0: This section 
     of the stylesheet replaces the target jsp file with dispatch.jsp 
     and then appends the two parameters that dispatch.jsp needs:  
     'channelID' and 'method' name. When migrating to uPortal 2.0, the
     include of this section of the stylesheet can be removed. -->

<xsl:variable name="baseActionURL">
  <xsl:choose>
    <xsl:when test="not(starts-with($baseActionURL, 'dispatch.jsp'))">
      <xsl:value-of select="concat('dispatch.jsp', '?', substring-after($baseActionURL, '?'), 'method=render&amp;channelID=', substring-before(substring-after($baseActionURL, 'channelTarget='), '&amp;'))"/>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$baseActionURL"/></xsl:otherwise>
  </xsl:choose>
</xsl:variable>

</xsl:stylesheet> 
