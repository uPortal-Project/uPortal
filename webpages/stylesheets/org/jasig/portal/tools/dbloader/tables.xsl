<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>
<xsl:param name="upgradeMajor">noUpgrade</xsl:param>
<xsl:param name="upgradeMinor"></xsl:param>

<xsl:template match="/">
  <xsl:apply-templates select="tables/table" mode="drop"/>
  <xsl:apply-templates select="tables/table" mode="create"/>
</xsl:template>

<xsl:template match="table" mode="drop">
<statement type="drop">
DROP TABLE <xsl:value-of select="name"/><xsl:text>
</xsl:text>
</statement><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="table" mode="create">
<xsl:choose>
<xsl:when test="$upgradeMajor = 'noUpgrade' or @sinceMajor &gt; $upgradeMajor or @sinceMinor &gt; $upgradeMinor">
 <statement type="create">
 CREATE TABLE <xsl:value-of select="name"/>
 (
 <xsl:apply-templates select="columns/column">
  <xsl:with-param name="mode">create</xsl:with-param>
 </xsl:apply-templates>
 <xsl:apply-templates select="primary-key"/>
 )
 </statement><xsl:text>
 </xsl:text>
</xsl:when>
<xsl:otherwise>
<!-- if the table is not completely new, check to see if there are new columns -->
 <xsl:if test="$upgradeMajor != 'noUpgrade'">
  <xsl:for-each select="columns/column">
   <xsl:if test="@sinceMajor &gt; $upgradeMajor or @sinceMinor &gt; $upgradeMinor">
    <statement type="create">
     ALTER TABLE <xsl:value-of select="../../name"/> ADD COLUMN <xsl:apply-templates select="."><xsl:with-param name="mode">alter</xsl:with-param></xsl:apply-templates>
    </statement>
   </xsl:if>
  </xsl:for-each>
 </xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="column">
  <xsl:param name="mode"></xsl:param>
  <xsl:text>  </xsl:text>
  <xsl:value-of select="name"/>
  <xsl:text> </xsl:text>
  <xsl:value-of select="type"/>
  <xsl:if test="param">(<xsl:value-of select="param"/>)</xsl:if>
  <xsl:if test="../../not-null = node() or ../../primary-key = node()"> NOT NULL</xsl:if>
    <xsl:if test="$mode = 'create' and (position() != last() or ../../primary-key)"><xsl:text>,
</xsl:text></xsl:if>
</xsl:template>

<xsl:template match="primary-key">
<xsl:if test="position() = 1">  PRIMARY KEY (</xsl:if><xsl:value-of select="."/><xsl:if test="position() != last()">, </xsl:if><xsl:if test="position() = last()">)</xsl:if>
</xsl:template>

</xsl:stylesheet>
