<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_PERMISSION</name>
				<rows>
					<!-- process rows making key change -->
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="data/table/rows/row">
		<row>
			<xsl:for-each select="column">
				<xsl:choose>
					<!-- in case of PRINCIPAL split into two columns -->
					<xsl:when test="name='PRINCIPAL'">
						<!--<column>
							<name>PRINCIPAL_TYPE</name>
							<value>
								<xsl:value-of select="substring-before(./value,'.')"/>
							</value>
						</column>
						<column>
							<name>PRINCIPAL_KEY</name>
							<value>
								<xsl:value-of select="substring-after(./value,'.')"/>
							</value>
						</column>-->
						<column><name>PRINCIPAL</name><value>
						<xsl:value-of select="substring-before(./value,'.')"/>.local.<xsl:value-of select="substring-after(./value,'.')"/>
						</value></column>
					</xsl:when>
					<xsl:otherwise>
						<!-- all the other columns are unchanged-->
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</row>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->