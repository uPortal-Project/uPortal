<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="navigationBar">
 <table border="0" cellpadding="2" cellspacing="3" width="100%">
 <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'?action=listMessages&amp;folder=', @inbox, '&amp;page=last')"/></xsl:attribute>Inbox</a></td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'?action=listFolders&amp;folder=1')"/></xsl:attribute>Folders</a></td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'?action=composeMessage')"/></xsl:attribute>Compose</a></td>
 <td>Addresses</td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL,'?action=setup&amp;returnTo=', @returnMethod)"/></xsl:attribute>Set-ups</a></td>
 <td><a><xsl:attribute name="href"><xsl:value-of select="beta"/></xsl:attribute>Help</a></td>
 </tr>
 </table>
</xsl:template>

<xsl:template match="headerBar">
<table border="0" cellpadding="2" cellspacing="3" width="100%">
<tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
 <td>
  <font size="4">
   <xsl:value-of select="."/>
   <xsl:if test="//@filtered"> (filtered)</xsl:if>
   </font>
   <xsl:if test="@unread"> - <font size="3"><xsl:value-of select="@unread"/> unread messages(s)</font></xsl:if>
 </td>
 <xsl:if test="@newmail">
  <td><font size="4">You have new mail</font></td>
 </xsl:if>
 <td align="right"><font size="4"><xsl:value-of select="@caption"/></font> <xsl:value-of select="@version"/></td>
</tr>
</table>
</xsl:template>

<xsl:template match="pagination">
<table border="0" cellpadding="2" cellspacing="3" width="100%">
 <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
 <td align="left">
 <xsl:if test="@first"><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=', @action, '&amp;page=', @first)"/></xsl:attribute>First</a> | </xsl:if>
 <xsl:if test="@prev"><a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=', @action, '&amp;page=', @prev)"/></xsl:attribute>Prev</a> | </xsl:if>
 showing <xsl:value-of select="@start"/> - <xsl:value-of select="@end"/> of <xsl:value-of select="@total"/>
 <xsl:if test="@next"> | <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=', @action, '&amp;page=', @next)"/></xsl:attribute>Next</a></xsl:if>
 <xsl:if test="@last"> | <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=', @action, '&amp;page=', @last)"/></xsl:attribute>Last</a></xsl:if>
 </td>
 </tr>
</table>
</xsl:template>

<xsl:template match="errors">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
 <Strong>Errors:</Strong><br/>
 <xsl:for-each select="error">
  <xsl:value-of select="."/><br/>
 </xsl:for-each>
</xsl:template>

<xsl:template match="hidden">
<input type="hidden">
<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
</input>
</xsl:template>

</xsl:stylesheet>
