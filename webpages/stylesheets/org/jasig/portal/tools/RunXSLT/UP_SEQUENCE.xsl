<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_SEQUENCE</name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="row">

		<xsl:choose>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_CHANNEL']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_CHANNEL</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>
							<xsl:value-of select="column[name='SEQUENCE_VALUE']/value+3"/>
						</value>
					</column>
				</row>
			</xsl:when>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_CHAN_TYPE']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_CHAN_TYPE</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>
							<xsl:value-of select="column[name='SEQUENCE_VALUE']/value+2"/>
						</value>
					</column>
				</row>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->