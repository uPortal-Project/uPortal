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
          <a href="{$baseActionURL}?uP_root=root">Home</a> |
          <xsl:if test="publish-chanid">
            <a href="{$baseActionURL}?uP_root={publish-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none">Publish</a> |
          </xsl:if>
          <a href="{$baseActionURL}?uP_root={subscribe-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none">Subscribe</a> |
          <a href="{$baseActionURL}?uP_root={preferences-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none">User Preferences</a>
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
