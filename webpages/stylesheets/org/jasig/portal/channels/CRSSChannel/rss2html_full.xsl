<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

 <xsl:template match="rss">
 <xsl:apply-templates select="channel"/>
 </xsl:template>


 <xsl:template match="channel">
  <table cellspacing="1" cellpadding="4" border="0" width="100%">
    <tr>

    <td valign="middle" >
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
    <em><xsl:value-of select="description"/></em>
     <xsl:apply-templates select="item"/>
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
  <form  method="get">
    <xsl:attribute name="action">
      <xsl:value-of select="link"/>
    </xsl:attribute>
  <p><xsl:value-of select="description"/></p>
    <input type="text">
      <xsl:attribute name="name">
        <xsl:value-of select="name"/>
      </xsl:attribute>
    </input>
    <input type="submit">
      <xsl:attribute name="value">
        <xsl:value-of select="title"/>
      </xsl:attribute>
    </input>
  </form>
 </xsl:template>

<xsl:template match="image">
  <a>
  <xsl:attribute name="href">
   <xsl:value-of select="link"/>
  </xsl:attribute>
  <img hspace="10" align="right" border="no">
         <xsl:attribute name="src">
         <xsl:value-of select="url"/>
        </xsl:attribute>
        <xsl:apply-templates select="description"/>
        <xsl:apply-templates select="width"/>
        <xsl:apply-templates select="height"/>
   </img>
   </a>
 </xsl:template>

<xsl:template match="description">
        <xsl:attribute name="alt">
         <xsl:value-of select="."/>
        </xsl:attribute>
 </xsl:template>
<xsl:template match="width">
        <xsl:attribute name="width">
         <xsl:value-of select="."/>
        </xsl:attribute>
 </xsl:template>
<xsl:template match="height">
        <xsl:attribute name="height">
         <xsl:value-of select="."/>
        </xsl:attribute>
 </xsl:template>

</xsl:stylesheet>