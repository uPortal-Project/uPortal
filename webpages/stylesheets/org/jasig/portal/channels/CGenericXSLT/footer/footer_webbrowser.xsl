<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="locale">en_US</xsl:param>
<xsl:param name="uP_productAndVersion">uPortal X.X.X</xsl:param>
  <xsl:template match="/">
    <table width="100%" border="0" cellspacing="5" cellpadding="5">
      <tr>
        <td align="right" class="uportal-label">
          <a href="http://www.udel.edu/uPortal">Powered by <xsl:value-of select="$uP_productAndVersion"/></a>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
