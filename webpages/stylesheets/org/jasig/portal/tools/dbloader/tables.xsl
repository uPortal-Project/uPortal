<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

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
<statement type="create">
CREATE TABLE <xsl:value-of select="name"/>
(
<xsl:apply-templates select="columns/column"/>
<xsl:apply-templates select="primary-key"/>
)
</statement><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="column">
  <xsl:text>  </xsl:text>
  <xsl:value-of select="name"/>
  <xsl:text> </xsl:text>
  <xsl:value-of select="type"/>
  <xsl:if test="param">(<xsl:value-of select="param"/>)</xsl:if>
  <xsl:if test="../../primary-key = node()"> NOT NULL</xsl:if>
    <xsl:if test="position() != last() or ../../primary-key"><xsl:text>,
</xsl:text></xsl:if>
</xsl:template>

<xsl:template match="primary-key">
<xsl:if test="position() = 1">  PRIMARY KEY (</xsl:if><xsl:value-of select="."/><xsl:if test="position() != last()">, </xsl:if><xsl:if test="position() = last()">)</xsl:if>
</xsl:template>

</xsl:stylesheet>
