<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<!--<xsl:variable name="uripath">file://d:/uPortal/rel-2-0-patches/portal/</xsl:variable>-->
	<xsl:variable name="uripath"/>

	<xsl:template match="/">
		<data>
			<table>
				<name>UP_GROUP_MEMBERSHIP</name>
				<rows>
					<!-- process rows making key change -->
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>
	<xsl:template match="data/table/rows/row">
		<row>
			<!-- copy the group_id column -->
			<xsl:copy-of select="./column[name='GROUP_ID']"/>
        	<column>
				<name>MEMBER_SERVICE</name><value>local</value>
			</column>
			<!-- create the new MEMBER_KEY -->
			<column>
				<name>MEMBER_KEY</name>
				<!-- get the value of the current group_id -->
				<xsl:variable name="currentGroup" select="./column[name='GROUP_ID']/value"/>
				<!-- pick up the corresponding row in UP_GROUP table-->
				<xsl:variable name="groupRow" select="document(concat($uripath,'UP_GROUP_20.xml'))
						/data/table/rows/row[column[name='GROUP_ID' and value=$currentGroup]]"/>
				<!-- get the entity_id for person object -->
				<xsl:variable name="personEntityType" select="document(concat($uripath,'UP_GROUP_ENTITY_TYPE_20.XML'))
						//row[column[name='ENTITY_TYPE_NAME' and value='org.jasig.portal.security.IPerson']]
						/column[name='ENTITY_TYPE_ID']/value"/>
				<xsl:variable name="oldKey" select="column[name='MEMBER_KEY']/value"/>
				<!-- construct the new MEMBER_KEY column -->
				<value>
					<xsl:choose>
						<!-- when this member is not a group and entity type is person 
						substitute the user_name value instead of the user_id -->
						<xsl:when test="$groupRow/column[name='ENTITY_TYPE_ID' and value=$personEntityType] 
										and column[name='MEMBER_IS_GROUP' and value='F']">
							<xsl:variable name="userRow" select="document(concat($uripath,'UP_USER_20.xml'))
										//row[column[name='USER_ID' and value=$oldKey]]"/>
							<xsl:value-of select="$userRow/column[name='USER_NAME']/value"/>
						</xsl:when>
						<!-- otherwise use the old key value -->
						<xsl:otherwise>
							<xsl:value-of select="$oldKey"/>
						</xsl:otherwise>
					</xsl:choose>
				</value>
			</column>
			<!--copy the last column-->
			<xsl:copy-of select="./column[name='MEMBER_IS_GROUP']"/>
		</row>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2002 eXcelon Corp.
<metaInformation>
<scenarios/><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->