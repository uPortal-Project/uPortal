<?xml version='1.0' encoding='utf-8' ?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"><xsl:output method="html"/>


  <xsl:template match="bookmarks">
      <table border="0" width="100%" cellspacing="5" cellpadding="0">
        <xsl:apply-templates select="bookmark"/>
      </table>
  </xsl:template>

  <xsl:template match="bookmark">
   <tr class="uportal-channel-text">
    <td class="uportal-channel-subtitle-reversed" nowrap="nowrap">
      <a href="{@url}" target="_blank"><xsl:value-of select="@name"/>: </a>
    </td>
    <td width="100%">
     <xsl:value-of select="@comments"/>
    </td>
   </tr>
  </xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
