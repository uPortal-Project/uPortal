<?xml version="1.0"? >
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>
  <xsl:template match="bookmarks">
      <table border="0" width="100%" cellspacing="5" cellpadding="0">
        <xsl:apply-templates select="bookmark"/>
      </table>
  </xsl:template>
  <xsl:template match="bookmark">
   <tr bgcolor="#eeeeee">
    <td>
      <a>
      <xsl:attribute name="href"><xsl:value-of select="@url"/></xsl:attribute>
      <xsl:value-of select="@name"/>
      </a>
    </td>


   <td>
     <xsl:value-of select="@comments"/>
    </td>
   </tr>
  </xsl:template>

</xsl:stylesheet>
