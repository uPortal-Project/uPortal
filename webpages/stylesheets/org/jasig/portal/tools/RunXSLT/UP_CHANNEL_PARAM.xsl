<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<data>
			<table>
				<name>
					<xsl:value-of select="data/table/name"/>
				</name>
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
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>XHTML.ssl</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_edit.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_edit.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_info.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_info.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_help.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_help.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>

			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_edit.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_edit.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_info.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_info.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_help.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_help.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>

			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2002 eXcelon Corp.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="..\..\..\..\..\..\..\build\RunXSLT\UP_CHANNEL_PARAM_20.XML" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->