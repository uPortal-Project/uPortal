<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="guest">false</xsl:param>
<xsl:variable name="mediaPath">media/org/jasig/portal/layout/nested-categories/deck-of-cards</xsl:variable>

<xsl:template match="header">
  <xsl:if test="$guest='false'">
    <do type="options" label="Logout"><go href="logout.jsp"></go></do>
  </xsl:if>
  
  <p><img src="{$mediaPath}/uPortal-logo.wbmp" width="45" height="30" alt="uPortal 2.0"/></p>
  <p><small>Hello <xsl:value-of select="full-name"/></small></p>
  <p><small><xsl:value-of select="timestamp-short"/></small></p>
</xsl:template>


</xsl:stylesheet>
