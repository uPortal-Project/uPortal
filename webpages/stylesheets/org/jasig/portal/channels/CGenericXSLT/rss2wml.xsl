<?xml version="1.0"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

 <xsl:template match="rss">
  <xsl:apply-templates select="channel"/>
 </xsl:template>


 <xsl:template match="channel">
   <p align="center"><big><xsl:value-of select="title"/></big></p>
   <p>
    <table columns="1">
    <xsl:apply-templates select="item"/>
    </table>
   </p>

 </xsl:template>

 <xsl:template match="item">
   <tr><td>
  <a><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
  <xsl:value-of select="title"/></a>
  </td></tr><tr><td>
   <xsl:value-of select="description"/>
  </td></tr>
 </xsl:template>
</xsl:stylesheet>