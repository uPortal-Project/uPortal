<?xml version='1.0' encoding='utf-8' ?><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="activeTab">t01</xsl:param>

<xsl:template match="layout"><layout>
<xsl:call-template name="tabList"/>
<xsl:apply-templates/>
</layout></xsl:template>

<xsl:template name="tabList"><navigation>
<xsl:for-each select="folder">
   <tab>
    <xsl:attribute name="ID"><xsl:value-of select="@ID"/></xsl:attribute>
	<xsl:choose>
	<xsl:when test="$activeTab=@ID">
	<xsl:attribute name="activeTab">true</xsl:attribute>
	</xsl:when>
	<xsl:otherwise>
	<xsl:attribute name="activeTab">false</xsl:attribute>
	</xsl:otherwise>
	</xsl:choose>
    <xsl:attribute name="priority"><xsl:value-of select="@priority"/></xsl:attribute>
    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
   </tab>
</xsl:for-each>
</navigation>
</xsl:template>

<xsl:template match="folder"><xsl:if test="$activeTab=@ID">
<content>
<xsl:choose>
<xsl:when test="child::folder">
<xsl:for-each select="folder">
  <column>
    <xsl:attribute name="ID"><xsl:value-of select="@ID"/></xsl:attribute>
    <xsl:attribute name="priority"><xsl:value-of select="@priority"/></xsl:attribute>
    <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
    <xsl:apply-templates/>
  </column>
</xsl:for-each>
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
</content>
</xsl:if></xsl:template>

<xsl:template match="channel"><xsl:if test="@minimized = 'false'">
<xsl:copy-of select="."/>
</xsl:if></xsl:template>

<xsl:template match="parameter">
<xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios ><scenario name="slayout" url="file://c:\ProudTab\slayout.xml" htmlbaseurl="file://c:\ProudTab\slayout.xml"/></scenarios>
</metaInformation>
-->
