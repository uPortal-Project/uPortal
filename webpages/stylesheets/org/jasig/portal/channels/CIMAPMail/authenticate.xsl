<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>

<xsl:template match="authenticate">
 <form action="{$baseActionURL}?action=authenticate" method="POST">
  <table border="0" cellspacing="2" cellpadding="3" width="100%">
   <xsl:apply-templates select="loginText"/>
   <xsl:apply-templates select="error"/>
   <tr>
    <td>Username</td>
    <td>
     <input type="text" name="username">
      <xsl:attribute name="value"><xsl:value-of select="username"/></xsl:attribute>
     </input></td>
    <td width="99%"/>
   </tr>
   <tr>
    <td>Password</td>
    <td>
     <input type="password" name="password" size="8"/></td>
    <td width="99%"/>
   </tr>
   <tr align="left">
    <td>
     <input type="submit" name="login" value="Login"/></td>
    <td>
     <input type="reset"/></td>
    <td width="99%"/>
   </tr>
  </table>
 </form>
</xsl:template>

<xsl:template match="loginText">
   <tr><td colspan="3"><left><xsl:value-of select="."/><br/></left></td></tr>
</xsl:template>

<xsl:template match="error">
 <strong><xsl:value-of select="."/></strong>
</xsl:template>
</xsl:stylesheet>
