<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_SEQUENCE</name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row"/>
					<row>
						<column>
							<name>SEQUENCE_NAME</name>
							<value>UP_ENTITY_TYPE</value>
						</column>
						<column>
							<name>SEQUENCE_VALUE</name>
							<value>20</value>
						</column>
						<!-- Reserve some IDs for internal use, just in case -->
					</row>
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
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_SS_STRUCT'] and column[name='SEQUENCE_VALUE' and value='0']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_SS_STRUCT</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>10</value>
					</column>
				</row>
			</xsl:when>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_SS_THEME'] and column[name='SEQUENCE_VALUE' and value='0']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_SS_THEME</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>10</value>
					</column>
				</row>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2002 eXcelon Corp.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="..\..\..\..\..\..\..\build\RunXSLT\UP_SEQUENCE_20.XML" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->