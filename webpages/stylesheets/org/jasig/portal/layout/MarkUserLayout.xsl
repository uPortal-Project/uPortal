<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="targetId">undefined</xsl:param>
	<xsl:param name="operation">undefined</xsl:param>

	<xsl:template match="layout">
		<layout>
			<xsl:choose>
				<xsl:when test="$operation='move'">
					<xsl:apply-templates select="." mode="move"/>
				</xsl:when>
				<xsl:when test="$operation='add'">
					<xsl:apply-templates select="." mode="add"/>
				</xsl:when>
			</xsl:choose>
		</layout>
	</xsl:template>

	<!-- add templates -->

	<xsl:template match="layout|folder" mode="add">
		<!-- copy the node and the attributes -->
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy/>
			</xsl:for-each>

			<xsl:if test="@immutable='false'">
				<add_target parentID="{@ID}" nextID="{child::*[self::folder or self::channel][position()=1]/@ID}"/>
			</xsl:if>

			<xsl:for-each select="folder|channel">
				<xsl:apply-templates mode="add" select="."/>
				<xsl:if test="@immutable='false'">
					<add_target parentID="{../@ID}" nextID="{following-sibling::*[self::folder or self::channel][position()=1]/@ID}"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>


	<xsl:template match="channel" mode="add">
		<xsl:copy-of select="."/>
	</xsl:template>


	<!-- move templates -->
	<xsl:template match="layout|folder" mode="move">
		<!-- copy the node and the attributes -->
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy/>
			</xsl:for-each>
			<xsl:variable name="firstChildId">
				<xsl:value-of select="child::*[self::folder or self::channel][position()=1]/@ID"/>
			</xsl:variable>
			<xsl:if test="@immutable='false' and not($firstChildId=$targetId)">
				<move_target parentID="{@ID}" nextID="{$firstChildId}"/>
			</xsl:if>

			<xsl:for-each select="folder|channel">
				<xsl:apply-templates mode="move" select="."/>
				<xsl:variable name="nextElementId">
					<xsl:value-of select="following-sibling::*[self::folder or self::channel][position()=1]/@ID"/>
				</xsl:variable>
				<xsl:if test="@immutable='false' and not($nextElementId=$targetId or @ID=$targetId)">
					<move_target parentID="{../@ID}" nextID="{$nextElementId}"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="channel" mode="move">
		<xsl:copy-of select="."/>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2003 Copyright Sonic Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="no" name="add scenario" userelativepaths="yes" externalpreview="no" url="plain_layout.xml" htmlbaseurl="" outputurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" ><parameterValue name="targetId" value="'101'"/><parameterValue name="targetDepth" value="'3'"/><parameterValue name="operation" value="'add'"/></scenario><scenario default="yes" name="move scenario" userelativepaths="yes" externalpreview="no" url="plain_layout.xml" htmlbaseurl="" outputurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" ><parameterValue name="targetId" value="'14'"/><parameterValue name="targetDepth" value="'3'"/><parameterValue name="operation" value="'move'"/></scenario></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->