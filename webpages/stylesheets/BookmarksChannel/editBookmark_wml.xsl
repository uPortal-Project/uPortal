<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="channelID">default</xsl:param>
<xsl:param name="bookmarkID">default</xsl:param>

<xsl:param name="newBookmark">false</xsl:param>

<xsl:template match="bookmark">
<p>
<input type="text" title="Name" name="bname"><xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute></input>
<input type="text" title="URL" name="burl"><xsl:attribute name="value"><xsl:value-of select="@url"/></xsl:attribute></input>
<input type="text" title="Comments" name="bcomments"><xsl:attribute name="value"><xsl:value-of select="@comments"/></xsl:attribute></input>
</p>

<xsl:choose>
<xsl:when test="$newBookmark='false'">
     <do type="prev" label="Save">
      <go>
        <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>channelTarget=<xsl:value-of select="$channelID"/>&amp;action=saveBookmark&amp;bookmark=<xsl:value-of select="$bookmarkID"/>&amp;name=<xsl:text disable-output-escaping="yes">$(bname)</xsl:text>&amp;url=<xsl:text disable-output-escaping="yes">$(burl)</xsl:text>&amp;comments=<xsl:text disable-output-escaping="yes">$(bcomments)</xsl:text></xsl:attribute>
      </go>
     </do>
</xsl:when>
<xsl:otherwise>
     <do type="prev" label="Save">
      <go>
        <xsl:attribute name="href">
          <xsl:value-of select="string($baseActionURL)"/>channelTarget=<xsl:value-of select="$channelID"/>&amp;action=addBookmark&amp;bookmark=<xsl:value-of select="$bookmarkID"/>&amp;name=<xsl:text disable-output-escaping="yes">$(bname)</xsl:text>&amp;url=<xsl:text disable-output-escaping="yes">$(burl)</xsl:text>&amp;comments=<xsl:text disable-output-escaping="yes">$(bcomments)</xsl:text></xsl:attribute>
      </go>
     </do>
</xsl:otherwise>
</xsl:choose>

</xsl:template>
</xsl:stylesheet>
