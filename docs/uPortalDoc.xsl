<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="document">
  <html>
  <head>
    <title><xsl:value-of select="@title"/></title>
  </head>
  <body bgcolor="#ffffff" text="#996666" link="#ff0000" alink="#ff9999" vlink="#663333">
  	<table width="90%" border="0" height="85" cellpadding="5">
  	  <tr bgcolor="#cccccc"> 
  	    <td width="20%" bgcolor="#ffffff" height="37"><img src="images/uportal.gif" width="133" height="75"/></td>
  	    <td width="80%" height="37" valign="center"> 
  	      <h1><font face="Arial, Helvetica, sans-serif" color="#ff8040">&#160;<xsl:value-of select="@title"/></font></h1>
  	    </td>
  	  </tr>
  	  <tr bgcolor="#cecfce"> 
  	    <td width="20%" height="9">&#160;</td>
  	    <td width="80%" height="9">&#160;</td>
  	  </tr>
  	</table>
  	<ul><xsl:apply-templates select="section" mode="summary"/></ul>
  	<xsl:apply-templates/>
  </body>
  </html>
</xsl:template>

<xsl:template match="section" mode="summary">
  <li><a href="#{@title}"><xsl:value-of select="@title"/></a></li>
</xsl:template>

<xsl:template match="section">
  <a name="{@title}"><!--anchor--></a>
  <table width="90%" border="0" cellpadding="5">
    <tr bgcolor="#3333ff"> 
      <td colspan="2"> 
        <h2><font color="#ff8000">&#160;<xsl:value-of select="@title"/></font></h2>       
      </td>
    </tr>
    <tr> 
      <td width="6%" bgcolor="#ff9966" height="211">&#160;</td>
      <td width="94%" height="211"> 
        <ul><xsl:apply-templates/></ul>
      </td>
    </tr>
  </table>
  <br/>
</xsl:template>

<xsl:template match="itemgroup">
  <p><h3><xsl:value-of select="@title"/></h3></p>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="item">
  <li><xsl:apply-templates/></li>
</xsl:template>

<xsl:template match="a">
  <a href="{@href}"><xsl:apply-templates/></a>
</xsl:template>

<xsl:template match="img">
  <img src="{@src}" height="{@height}" width="{@width}" border="{@border}"/>
</xsl:template>

<xsl:template match="pre">
  <pre><xsl:apply-templates/></pre>
</xsl:template>

<xsl:template match="code">
  <code><xsl:apply-templates/></code>
</xsl:template>

<xsl:template match="p">
  <p><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="ul">
  <ul><xsl:apply-templates/></ul>
</xsl:template>

<xsl:template match="li">
  <li><xsl:apply-templates/></li>
</xsl:template>

<xsl:template match="br">
  <br/>
</xsl:template>

</xsl:stylesheet>


