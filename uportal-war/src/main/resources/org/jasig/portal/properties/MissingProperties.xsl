<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>

	<xsl:template match="/">
		<div class="uportal-channel-table-caption">This channel lists properties that PropertiesManager was asked about but
	for which PropertiesManager doesn't know a value.</div>


		<xsl:apply-templates select="missingProperties"/>
	</xsl:template>

	<xsl:template match="missingProperties">
		<xsl:choose>
			<xsl:when test="property">
				<div class="uportal-background-content">
					<xsl:apply-templates select="property"/>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="uportal-text">No properties are known to be missing.</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>



	<xsl:template match="property">
			<div class="uportal-text">
				<xsl:value-of select="self::property"/>
			</div>
	</xsl:template>
</xsl:stylesheet>
<!--
$Revision$ $Date$
MissingProperties.xsl : an XSLT suitable for rendering as a uPortal channel the XML produced by the
MissingPropertiesServlet.
@author andrew.petro@yale.edu
--><!-- Stylus Studio meta-information - (c)1998-2003. Sonic Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="no" name="missingPropertiesExample" userelativepaths="yes" externalpreview="no" url="missingPropertiesExample.xml" htmlbaseurl="" outputurl="" processortype="internal" profilemode="0" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/><scenario default="yes" name="noMissingPropertiesExample" userelativepaths="yes" externalpreview="no" url="noMissingPropertiesExample.xml" htmlbaseurl="" outputurl="" processortype="internal" profilemode="0" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->