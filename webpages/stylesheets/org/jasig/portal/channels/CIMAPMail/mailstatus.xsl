<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 <xsl:param name="baseActionURL">default</xsl:param>

 <xsl:output method="html"/>
 <xsl:template match="mailstatus">
  <table align="center">
   <tr><td align="center">You have <xsl:value-of select="unread"/> unread messages(s)</td></tr>
   <tr>
    <td>
     <table align="center">
      <tr>
       <td align="center"><a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?uP_root=me&amp;action=listMessages&amp;page=last</xsl:attribute>Read</a></td>
       <td align="center"><a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?uP_root=me&amp;action=composeMessage</xsl:attribute>Compose</a></td>
       <td align="center"><a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?action=mailStatus</xsl:attribute>Check</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </xsl:template>
</xsl:stylesheet>