<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="guest">false</xsl:param>

<xsl:template match="header">
  <table border="0">
    <tr class="uportal-background-light">
      <td nowrap="nowrap" class="uportal-channel-text">
        <xsl:choose>
        <xsl:when test="$guest='true'">
          Welcome to uPortal 2.0 Please sign in...
        </xsl:when>
        <xsl:otherwise>
          <a href="{$baseActionURL}?uP_root=root&amp;please-fix-these-links!">Home</a> |
          <a href="{$baseActionURL}?uP_root=chan91&amp;please-fix-these-links!">Publish</a> |
          <a href="{$baseActionURL}?uP_root=chan90&amp;please-fix-these-links!">Subscribe</a> |
          <a href="{$baseActionURL}?uP_root=chan92&amp;please-fix-these-links!">User Preferences</a>
        </xsl:otherwise>        
        </xsl:choose>
      </td>
    </tr>
    <tr class="uportal-background-med">
      <td nowrap="nowrap" class="uportal-channel-code">
        <xsl:value-of select="timestamp-long"/>
      </td>
    </tr>
  </table>
</xsl:template>


</xsl:stylesheet>
