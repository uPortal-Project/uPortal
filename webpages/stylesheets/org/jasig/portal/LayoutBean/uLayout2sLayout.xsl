<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-system="http://localhost:8080/portal/dtd/structuredLayout.dtd"/>

<xsl:template match="layout">
<layout>

<!-- this is temporary -->
  <header>
   <title>guest, welcome !</title>
   <description>XML version
   </description>
   <image>
    <link>http://localhost:8080/portal/</link>
    <url>images/logo_75_teal.gif</url>
    <description>uPortal logo</description>
    <width>132</width>
    <height>74</height>
   </image>
  </header>

<xsl:apply-templates/>
</layout>
</xsl:template>

<!-- process categories
   The folder that is the first child of the layout
   becomes "tab", and the rest become "comulns"
  -->
<xsl:template match="folder">
 <xsl:choose>
  <xsl:when test="name(parent::node())='layout'">
   <tab>
    <xsl:attribute name="ID"><xsl:value-of select="@ID"/></xsl:attribute>
    <xsl:attribute name="priority"><xsl:value-of select="@priority"/></xsl:attribute>
    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
    <xsl:apply-templates/>
   </tab>
  </xsl:when>
  <xsl:otherwise>
   <column>
    <xsl:attribute name="ID"><xsl:value-of select="@ID"/></xsl:attribute>
    <xsl:attribute name="priority"><xsl:value-of select="@priority"/></xsl:attribute>
    <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
    <xsl:apply-templates/>
   </column>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="channel">
<xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="parameter">
<xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>