<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="baseActionUrl">noBaseActionUrlSpecified</xsl:param>
<xsl:param name="baseMediaUrl">noBaseMediaUrlSpecified</xsl:param>
<xsl:param name="selectedSection">toc</xsl:param>


<xsl:template match="content">
<table cellpadding="0" cellspacing="5">
 <tr>
  <td width="15%" valign="top" class="uportal-background-light"><xsl:call-template name="toc"/></td>
  <td width="85%" valign="top" class="uportal-background-content"><xsl:call-template name="currentSection"/></td></tr>
</table> 
</xsl:template>

<xsl:template name="toc">
<table cellpadding="0" cellspacing="5">
<tr><td class="uportal-channel-title"> Contents </td></tr>
<xsl:for-each select="section">
 <xsl:choose>
  <xsl:when test="$selectedSection = position()">
<tr class="uportal-background-highlight"><td class="uportal-channel-subtitle">
 <xsl:value-of select="title"/></td></tr>
  </xsl:when>
  <xsl:otherwise>
<tr><td class="uportal-channel-subtitle"><a><xsl:attribute name="href">
  <xsl:value-of select="$baseActionUrl"/>?section=<xsl:value-of 
  select="position()"/>
 </xsl:attribute><xsl:value-of select="title"/></a></td></tr>
  </xsl:otherwise>
 </xsl:choose>
</xsl:for-each>
</table>
</xsl:template>

<xsl:template name="currentSection">
<xsl:for-each select="section">
 <xsl:if test="$selectedSection = position()">
  <table cellpadding="5" cellspacing="0">
   <xsl:if test="title">
    <tr><td class="uportal-channel-title"><xsl:value-of select="title" /></td></tr>
   </xsl:if>
   <xsl:for-each select="p">
    <tr><td class="uportal-channel-text">
     <xsl:for-each select="child::node()">
      <xsl:choose>
       <xsl:when test="name() = 'em'"><span class="uportal-copyright"><xsl:value-of select="."/></span></xsl:when>
       <xsl:when test="name() = 'strong'"><span class="uportal-channel-strong"><xsl:value-of select="."/></span></xsl:when>
       <xsl:when test="name() = 'code'"><span style="font-family: monospace;"><pre><xsl:value-of select="."/></pre></span></xsl:when>
       <xsl:when test="name() = 'image'"><img><xsl:attribute name="src"><xsl:value-of select="$baseMediaUrl"/><xsl:value-of select="."/></xsl:attribute></img></xsl:when>
       <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose>
     </xsl:for-each>
    </td></tr>
   </xsl:for-each>
  </table>
 </xsl:if>
</xsl:for-each>
</xsl:template>

</xsl:stylesheet>