<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_ENTITY_TYPE</name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row">
						<xsl:sort select="column[name='ENTITY_TYPE_ID']/value"/>
					</xsl:apply-templates>
				</rows>
			</table>
		</data>
	</xsl:template>
	<xsl:template match="row">
		<!-- first copy the orginal rows adding the new column -->
		<row>
			<!-- copy the original columns -->
			<xsl:copy-of select="./column"/>
			<!-- add new column -->
			<column>
				<name>DESCRIPTIVE_NAME</name>
				<value>
					<xsl:choose>
						<xsl:when test="./column[2]/value='java.lang.Object'">Generic</xsl:when>
						<xsl:when test="./column[2]/value='org.jasig.portal.security.IPerson'">Person</xsl:when>
						<xsl:when test="./column[2]/value='org.jasig.portal.groups.IEntityGroup'">Group</xsl:when>
						<xsl:when test="./column[2]/value='org.jasig.portal.ChannelDefinition'">Channel</xsl:when>
					</xsl:choose>
				</value>
			</column>
		</row>
		<xsl:if test="position()=last()">
			<!-- then add the new row for grouped entity -->
			<row>
				<column>
					<name>ENTITY_TYPE_ID</name>
					<value>
						<xsl:value-of select="(column[name='ENTITY_TYPE_ID']/value)+1"/>
					</value>
				</column>
				<column>
					<name>ENTITY_TYPE_NAME</name>
					<value>org.jasig.portal.groups.IEntity</value>
				</column>
				<column>
					<name>DESCRIPTIVE_NAME</name>
					<value>Grouped Entity</value>
				</column>
			</row>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->