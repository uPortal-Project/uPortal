<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

<xsl:param name="channelID">default</xsl:param>
<xsl:param name="bookmarkID">default</xsl:param>

<xsl:param name="newBookmark">false</xsl:param>

<xsl:template match="bookmark">
<form action="index.jsp">

<input type="hidden" name="channelTarget"><xsl:attribute name="value"><xsl:value-of select="$channelID"/></xsl:attribute></input>	
<xsl:choose>
<xsl:when test="$newBookmark='false'">
<input type="hidden" name="action" value="saveBookmark"/>
<input type="hidden" name="bookmark"><xsl:attribute name="value"><xsl:value-of select="$bookmarkID"/></xsl:attribute></input>
</xsl:when>
<xsl:otherwise>
<input type="hidden" name="action" value="addBookmark"/>
</xsl:otherwise>
</xsl:choose>

<table width="100%">
  <tr>
    <td><b>Name</b></td>
    <td><input type="text" name="name"><xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute></input></td>
  </tr>
  <tr>
    <td><b>URL</b></td>
    <td><input type="text" name="url"><xsl:attribute name="value"><xsl:value-of select="@url"/></xsl:attribute></input></td>
  </tr>
  <tr>
    <td><b>Comments</b></td>
    <td><input type="text" name="comments"><xsl:attribute name="value"><xsl:value-of select="@comments"/></xsl:attribute></input></td>
  </tr>
</table>


<input type="submit" name="submit" value="Save"/>

</form>

</xsl:template>
</xsl:stylesheet>
