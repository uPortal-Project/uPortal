<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>
<xsl:include href="lookAndFeel.xsl"/>

<xsl:template match="listFolders">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
 <xsl:choose>
  <xsl:when test="errors">bye<xsl:apply-templates select="errors"/></xsl:when>
  <xsl:otherwise>
   <form method="POST" action="{$baseActionURL}?action=listFolders">
    <xsl:choose>
     <xsl:when test="enterFolder"><xsl:apply-templates select="enterFolder"/></xsl:when>
     <xsl:otherwise>
      <xsl:apply-templates select="controls"/>
      <xsl:apply-templates select="pagination"/>
      <table border="0" cellpadding="2" cellspacing="3" width="100%">
       <xsl:apply-templates select="headers"/>
       <xsl:apply-templates select="folders"/>
      </table>
      <xsl:apply-templates select="pagination"/>
      <xsl:apply-templates select="controls"/>
     </xsl:otherwise>
    </xsl:choose>
   </form>
   <xsl:apply-templates select="navigationBar"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="controls">
 <table border="0" cellpadding="2" cellspacing="3" width="100%">
  <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
  <td>
   <xsl:for-each select="button">
    <input type="submit" name="submit"><xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute></input>
   </xsl:for-each>
  </td>
  </tr>
 </table>
</xsl:template>

<xsl:template match="headers">
 <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
 <xsl:for-each select="header">
  <th><xsl:value-of select="."/></th>
 </xsl:for-each>
 </tr>
</xsl:template>

<xsl:template match="folders">
  <xsl:for-each select="folder">
   <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
    <td align="center">
      <xsl:choose>
       <xsl:when test="not(@special)"><input type="checkbox" name="folder"><xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute></input></xsl:when>
       <xsl:otherwise>&#160;</xsl:otherwise>
      </xsl:choose>
    </td>
    <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=listMessages&amp;folder=', .)"/></xsl:attribute><xsl:value-of select="."/></a></td>
    <td align="center"><xsl:value-of select="@messages"/></td>
    <td align="center"><xsl:value-of select="@unread"/></td>
    <td><!-- <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=listFolders&amp;submit=download&amp;downloadfolder=', .)"/></xsl:attribute>download</a>-->download</td>
   </tr>
  </xsl:for-each>
</xsl:template>

<xsl:template match="enterFolder">
 <xsl:choose>
  <xsl:when test="@oldname">
   Rename folder <xsl:value-of select="@oldname"/> to:
   <input type="hidden" name="oldFolderName"><xsl:attribute name="value"><xsl:value-of select="@oldname"/></xsl:attribute></input>
  </xsl:when>
  <xsl:otherwise>New folder name:"</xsl:otherwise>
 </xsl:choose>
 <input type="text" name="newFolderName" size="30" maxlength="64"/>
 <input type="submit" name="submit"><xsl:attribute name="value"><xsl:value-of select="@mode"/></xsl:attribute></input>
 <input type="submit" name="submit" value="Cancel"/>
</xsl:template>

</xsl:stylesheet>
