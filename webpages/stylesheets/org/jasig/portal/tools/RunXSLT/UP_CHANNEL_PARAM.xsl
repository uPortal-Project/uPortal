<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<data>
			<table>
				<name><xsl:value-of select="data/table/name"/></name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>
	<xsl:template match="row">
		<xsl:choose>
			<xsl:when test="column[name='CHAN_PARM_VAL'and value='CWebProxy/XHTML.ssl']">
			<row>
			<xsl:copy-of select="column[name='CHAN_ID']"/>
			<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
        	<column><name>CHAN_PARM_VAL</name><value>XHTML.ssl</value></column>
			<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
			</row>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->