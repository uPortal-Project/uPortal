<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>
<xsl:include href="lookAndFeel.xsl"/>

<xsl:template match="setup">
 <form method="POST" action="{$baseActionURL}?action=setup">
 <table border="0" cellpadding="2" cellspacing="3" width="100%">
  <xsl:apply-templates select="fullheaders"/>
  <tr><td>
  <input type="submit" name="submit" value="Configure"/>
  </td><td>
  <input type="submit" name="submit" value="Cancel"/>
  </td><td width="80%"/></tr>
 </table>
 </form>
</xsl:template>

<xsl:template match="fullheaders">
 <tr><td>
 <input type="checkbox" name="fullheaders">
 <xsl:if test="@fullheaders">
  <xsl:attribute name="checked"><xsl:value-of select="@fullheaders"/></xsl:attribute>
 </xsl:if>
 Show full headers
 </input>
 </td></tr>
</xsl:template>

</xsl:stylesheet>
