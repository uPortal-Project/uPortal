<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT/footer</xsl:variable>

<xsl:template match="/">
  <table width="100%" border="0" cellspacing="5" cellpadding="5">
    <tr>
      <td align="right"><img src="{$mediaPath}/powered_by_uPortal2_0_beta.gif" border="0" width="156" height="43" /></td>
    </tr>
  </table>
</xsl:template>

</xsl:stylesheet>
