<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

  <xsl:template match="bookmarks">
     <p>
      <table columns="1">
        <xsl:apply-templates select="bookmark"/>
      </table>
     </p>
  </xsl:template>

  <xsl:template match="bookmark">
   <tr>
    <td>
      <a>
      <xsl:attribute name="href"><xsl:value-of select="@url"/></xsl:attribute>
      <xsl:value-of select="@name"/> 
      </a>
     <xsl:text disable-output-escaping="yes">: </xsl:text>
     <xsl:value-of select="@comments"/>
    </td>
   </tr>
  </xsl:template>
</xsl:stylesheet>