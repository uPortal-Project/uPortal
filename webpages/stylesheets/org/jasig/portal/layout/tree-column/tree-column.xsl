<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" doctype-system="http://localhost:8080/portal/dtd/structuredLayout.dtd"/>
	<xsl:template match="layout">
            <xsl:for-each select="folder">
		<layout>
			<xsl:apply-templates/>
		</layout>
            </xsl:for-each>
	</xsl:template>
	<xsl:template match="folder">
		<xsl:choose>
			<!--this is a temporary conditional to generate a column [justin] -->
			<xsl:when test="not(string(@name)) or contains(@name,'Column')">
				<column ID="{@ID}" priority="{@priority}" width="{@width}">
					<xsl:apply-templates/>
				</column>
			</xsl:when>
			<xsl:otherwise>
				<folder ID="{@ID}" priority="{@priority}" name="{@name}">
					<xsl:apply-templates/>
				</folder>
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
