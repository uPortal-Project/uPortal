<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>
  
 <xsl:param name="baseActionURL">default</xsl:param>

 <xsl:template match="rss">
 <html><body>
 <xsl:apply-templates select="channel"/>
 </body></html>
 </xsl:template>


 <xsl:template match="channel">
  <table cellspacing="1" cellpadding="4" bgcolor="#FFFFFF" border="0" width="100%">
    <tr>

    <td valign="middle" align="left" bgcolor="#EEEEEE">
       <xsl:apply-templates select="image"/>
      <font color="#000000" face="Arial,Helvetica"><b>
        <a>
        <xsl:attribute name="href">
         <xsl:value-of select="link"/>
        </xsl:attribute>
        <xsl:value-of select="title"/>
        </a></b>
      </font>
    </td></tr>

    <tr>
    <td>
    <center><xsl:value-of select="description"/></center>
     <xsl:apply-templates select="item"/>
    </td>
    </tr>

    <tr>
    <td>
     <xsl:apply-templates select="textinput"/>
    </td>
    </tr>

   </table>
 </xsl:template>

 <xsl:template match="item">
   <li><a>
    <xsl:attribute name="href">
      <xsl:value-of select="link"/>
    </xsl:attribute>
    <xsl:value-of select="title"/>
   </a><br/>
  <xsl:value-of select="description"/>
 </li>
 </xsl:template>

<xsl:template match="textinput">
  <xsl:value-of select="description"/><br/>
  <xsl:variable name="action-url" select="link"/>
  <xsl:variable name="button-name" select="title"/>
  <xsl:variable name="param-name" select="name"/>
  <xsl:choose>
    <xsl:when test="string-length(normalize-space(link))=0">
      <form action="{$baseActionURL}" method="get">
        <input type="text" name="{$param-name}" size="20"/>
        <input type="submit" value="{$button-name}"/>
      </form>  
    </xsl:when>
    <xsl:otherwise>
      <form action="{$action-url}" method="get">
        <input type="text" name="{$param-name}" size="20"/>
        <input type="submit" value="{$button-name}"/>
      </form>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="image">
  <a>
  <xsl:attribute name="href">
   <xsl:value-of select="link"/>
  </xsl:attribute>
  <img hspace="10">
        <xsl:attribute name="src">
         <xsl:value-of select="url"/>
        </xsl:attribute>
        <xsl:attribute name="alt">
         <xsl:value-of select="description"/>
        </xsl:attribute>
        <xsl:attribute name="width">
         <xsl:value-of select="width"/>
        </xsl:attribute>
        <xsl:attribute name="height">
         <xsl:value-of select="height"/>
        </xsl:attribute>
   </img>
   </a>
 </xsl:template>

</xsl:stylesheet>
