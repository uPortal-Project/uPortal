<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>

<xsl:template match="displayMessage">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
 <xsl:choose>
  <xsl:when test="errors"><xsl:apply-templates select="errors"/></xsl:when>
  <xsl:otherwise>
   <form method="POST" action="{$baseActionURL}?action=displayMessage">
    <xsl:apply-templates select="hidden"/>
    <xsl:apply-templates select="controls"/>
    <table>
        <tr valign="top">
         <td><xsl:apply-templates select="headers"/></td>
         <td width="10%"/>
         <td valign="top" width="40%" align="right"><xsl:apply-templates select="attachments"/></td>
        </tr>
    </table>
    <hr/>
    <xsl:apply-templates select="msgbody"/>
    <xsl:apply-templates select="controls"/>
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

<xsl:template match="addresses">
 <xsl:for-each select="address">
 <td width="99%" align="left">
  <xsl:choose>
   <xsl:when test="personal">
    <xsl:attribute name="title"><xsl:value-of select="concat('&lt;', email, '>')"/></xsl:attribute><xsl:value-of select="personal"/>
   </xsl:when>
   <xsl:otherwise><xsl:value-of select="email"/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="position() != last()">,</xsl:if>
 </td>
 </xsl:for-each>
</xsl:template>

<xsl:template match="headers">
<table width="100%">
 <xsl:for-each select="header">
  <tr>
  <xsl:choose>
   <xsl:when test="addresses"><td width="1%" align="right"><xsl:value-of select="@name"/>:</td><xsl:apply-templates select="addresses"/></xsl:when>
   <xsl:otherwise><td align="right"><xsl:value-of select="@name"/>:</td><td width="99%" wrap="no"><xsl:value-of select="."/></td></xsl:otherwise>
  </xsl:choose>
  </tr>
 </xsl:for-each>
</table>
</xsl:template>

<xsl:template match="attachments">
<strong>Attachments:</strong><br></br>
<xsl:for-each select="attachment">
<!--
  <a target="_blank">
  <xsl:attribute name="href">
  <xsl:value-of select="concat($baseActionURL, '&amp;action=displayMessage&amp;submit=download&amp;msg=', @msg, '&amp;attachment=', @attachment)"/></xsl:attribute>
  <xsl:value-of select="."/>
  </a>
  -->
  <xsl:value-of select="."/><br/>
</xsl:for-each>
</xsl:template>

<xsl:template match="msgbody">
 <xsl:copy-of select="msgtext"/>
</xsl:template>

</xsl:stylesheet>

