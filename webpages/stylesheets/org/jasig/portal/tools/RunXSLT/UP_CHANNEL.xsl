<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<!--<xsl:variable name="uripath">file://d:/uPortal/rel-2-0-patches/portal/</xsl:variable>
	
	<xsl:variable name="uripath">c:\\itsproj\portal\uPortal21\portal\build\RunXSLT\
	</xsl:variable>--><xsl:variable name="uripath"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_CHANNEL</name>
				<rows>
					<xsl:apply-templates select="//row">
						<xsl:sort select="column[name='CHAN_ID']/value"/>
					</xsl:apply-templates>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="row">
		<xsl:choose>
			<xsl:when test="column[name='CHAN_NAME']/value='Minesweeper'">
				<row>
					<xsl:apply-templates select="column" mode="Applet"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_CLASS' and value='org.jasig.portal.channels.CWebProxy']">
				<row>
					<xsl:apply-templates select="column" mode="webproxy"/>
				</row>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="position()=last()">
			<row>
				<column>
					<name>CHAN_ID</name>
					<value>
						<xsl:value-of select="document(concat($uripath,'UP_SEQUENCE_20.XML'))       /data/table/rows/row[column[name='SEQUENCE_NAME' and value='UP_CHANNEL']]/column[name='SEQUENCE_VALUE']/value+1"/>
					</value>
				</column>
				<column>
					<name>CHAN_TITLE</name>
					<value>CURLSelector</value>
				</column>
				<column>
					<name>CHAN_NAME</name>
					<value>ICC URL selector channel</value>
				</column>
				<column>
					<name>CHAN_DESC</name>
					<value>Interchannel communications demo URL selector channel</value>
				</column>
				<column>
					<name>CHAN_CLASS</name>
					<value>org.jasig.portal.channels.iccdemo.CURLSelector</value>
				</column>
				<column>
					<name>CHAN_TYPE_ID</name>
					<value>-1</value>
				</column>
				<column>
					<name>CHAN_PUBL_ID</name>
					<value>0</value>
				</column>
				<column>
					<name>CHAN_PUBL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_APVL_ID</name>
					<value>2</value>
				</column>
				<column>
					<name>CHAN_APVL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_TIMEOUT</name>
					<value>10000</value>
				</column>
				<column>
					<name>CHAN_EDITABLE</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_HELP</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_ABOUT</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_FNAME</name>
					<value>portal/iccdemo/urlselector</value>
				</column>
			</row>
			<row>
				<column>
					<name>CHAN_ID</name>
					<value>
						<xsl:value-of select="document(concat($uripath,'UP_SEQUENCE_20.XML'))       /data/table/rows/row[column[name='SEQUENCE_NAME' and value='UP_CHANNEL']]/column[name='SEQUENCE_VALUE']/value+2"/>
					</value>
				</column>
				<column>
					<name>CHAN_TITLE</name>
					<value>CViewer</value>
				</column>
				<column>
					<name>CHAN_NAME</name>
					<value>ICC Viewer channel</value>
				</column>
				<column>
					<name>CHAN_DESC</name>
					<value>Interchannel communications demo Viewer channel</value>
				</column>
				<column>
					<name>CHAN_CLASS</name>
					<value>org.jasig.portal.channels.iccdemo.CViewer</value>
				</column>
				<column>
					<name>CHAN_TYPE_ID</name>
					<value>-1</value>
				</column>
				<column>
					<name>CHAN_PUBL_ID</name>
					<value>0</value>
				</column>
				<column>
					<name>CHAN_PUBL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_APVL_ID</name>
					<value>2</value>
				</column>
				<column>
					<name>CHAN_APVL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_TIMEOUT</name>
					<value>10000</value>
				</column>
				<column>
					<name>CHAN_EDITABLE</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_HELP</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_ABOUT</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_FNAME</name>
					<value>portal/iccdemo/viewer</value>
				</column>
			</row>
			<row>
				<column>
					<name>CHAN_ID</name>
					<value>
						<xsl:value-of select="document(concat($uripath,'UP_SEQUENCE_20.XML'))       /data/table/rows/row[column[name='SEQUENCE_NAME' and value='UP_CHANNEL']]/column[name='SEQUENCE_VALUE']/value+3"/>
					</value>
				</column>
				<column>
					<name>CHAN_TITLE</name>
					<value>CHistory</value>
				</column>
				<column>
					<name>CHAN_NAME</name>
					<value>ICC History channel</value>
				</column>
				<column>
					<name>CHAN_DESC</name>
					<value>Interchannel communications demo History channel</value>
				</column>
				<column>
					<name>CHAN_CLASS</name>
					<value>org.jasig.portal.channels.iccdemo.CHistory</value>
				</column>
				<column>
					<name>CHAN_TYPE_ID</name>
					<value>-1</value>
				</column>
				<column>
					<name>CHAN_PUBL_ID</name>
					<value>0</value>
				</column>
				<column>
					<name>CHAN_PUBL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_APVL_ID</name>
					<value>2</value>
				</column>
				<column>
					<name>CHAN_APVL_DT</name>
					<value>SYSDATE</value>
				</column>
				<column>
					<name>CHAN_TIMEOUT</name>
					<value>10000</value>
				</column>
				<column>
					<name>CHAN_EDITABLE</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_HELP</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_HAS_ABOUT</name>
					<value>N</value>
				</column>
				<column>
					<name>CHAN_FNAME</name>
					<value>portal/iccdemo/history</value>
				</column>
			</row>
		</xsl:if>
	</xsl:template>

	<xsl:template match="column" mode="Applet">
		<xsl:choose>
			<xsl:when test="name='CHAN_TYPE_ID'">
				<column>
					<name>CHAN_TYPE_ID</name>
					<value>
						<xsl:value-of select="document(concat($uripath,'NEW_UP_CHAN_TYPE.XML'))           /data/table/rows/row[column[name='TYPE' and value='org.jasig.portal.channels.CApplet']]        /column[name='TYPE_ID']/value"/>
					</value>
				</column>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="column" mode="webproxy">
		<xsl:choose>
			<xsl:when test="name='CHAN_CLASS'">
				<column>
					<name>CHAN_CLASS</name>
					<value>org.jasig.portal.channels.webproxy.CWebProxy</value>
				</column>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2002 eXcelon Corp.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="..\..\..\..\..\..\..\build\RunXSLT\UP_CHANNEL_20.XML" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->