<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <xsl:output method="xml"/>
        <xsl:output indent="yes"/>

	<xsl:template match="fragments">
		<data>
			<xsl:call-template name="up_owner_fragment"/>
			<xsl:call-template name="up_fragments"/>
			<xsl:call-template name="up_fragment_restrictions"/>
			<xsl:call-template name="up_group_fragment"/>
		</data>
	</xsl:template>
	<xsl:template name="up_group_fragment">
		<table>
			<name>UP_GROUP_FRAGMENT</name>
			<rows>
				<xsl:for-each select="fragment">
					<xsl:for-each select="groups/group">
        					<row>
							<column>
								<name>FRAGMENT_ID</name>
								<value>
									<xsl:value-of select="ancestor::fragment/@id"/>
								</value>
							</column>
							<column>
								<name>GROUP_KEY</name>
								<value>
									<xsl:value-of select="@key"/>
								</value>
							</column>
	        				</row>
					</xsl:for-each>
				</xsl:for-each>
			</rows>
		</table>
	</xsl:template>
	<xsl:template name="up_owner_fragment">
		<table>
			<name>UP_OWNER_FRAGMENT</name>
			<rows>
				<xsl:for-each select="fragment">
					<row>
						<column>
							<name>FRAGMENT_ID</name>
							<value>
								<xsl:value-of select="@id"/>
							</value>
						</column>
						<column>
							<name>OWNER_ID</name>
							<value>
								<xsl:value-of select="@owner"/>
							</value>
						</column>

                        <column>
							<name>PUSHED_FRAGMENT</name>
							<xsl:choose>
								<xsl:when test="@type='pulled'">
									<value>N</value>
								</xsl:when>
								<xsl:otherwise>
									<value>Y</value>
								</xsl:otherwise>
							</xsl:choose>
						</column>
                        <column>
							<name>FRAGMENT_ROOT_ID</name>
							<value>1</value>
						</column>
						<column>
							<name>FRAGMENT_DESCRIPTION</name>
							<value>
								<xsl:value-of select="description"/>
							</value>
						</column>
						<column>
							<name>FRAGMENT_NAME</name>
							<value>
								<xsl:value-of select="@name"/>
							</value>
						</column>
					</row>
				</xsl:for-each>
			</rows>
		</table>
	</xsl:template>
	<xsl:template name="up_fragment_restrictions">
		<table>
			<name>UP_FRAGMENT_RESTRICTIONS</name>
			<rows>
				<xsl:for-each select="fragment">
					<xsl:variable name="fragment_id" select="@id"/>
					<xsl:for-each select="restrictions/restriction">
						<row>
							<column>
								<name>FRAGMENT_ID</name>
								<value>
									<xsl:value-of select="$fragment_id"/>
								</value>
							</column>
							<column>
								<name>NODE_ID</name>
								<value>1</value>
							</column>
							<column>
								<name>RESTRICTION_NAME</name>
								<value>
									<xsl:value-of select="@type"/>
								</value>
							</column>
							<column>
								<name>RESTRICTION_TREE_PATH</name>
								<value>
									<xsl:value-of select="@path"/>
								</value>
							</column>
							<column>
								<name>RESTRICTION_VALUE</name>
								<value>
									<xsl:value-of select="@value"/>
								</value>
							</column>
						</row>
					</xsl:for-each>
				</xsl:for-each>
			</rows>
		</table>
	</xsl:template>
	<xsl:template name="up_fragments">
		<table>
			<name>UP_FRAGMENTS</name>
			<rows>
				<xsl:for-each select="fragment">
					<xsl:choose>
						<xsl:when test="./folder">
							<xsl:apply-templates select="./folder[position()=1]"/>
						</xsl:when>
						<xsl:when test="./channel">
							<xsl:apply-templates select="./channel[position()=1]"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</rows>
		</table>
	</xsl:template>

	<xsl:template match="folder">
		<row>
			<xsl:call-template name="common_node_attributes"/>

			<column>
				<name>TYPE</name>
				<value>
					<xsl:value-of select="@type"/>
				</value>
			</column>
		</row>
		<xsl:apply-templates select="folder|channel"/>
	</xsl:template>
	<xsl:template match="channel">
		<row>
			<xsl:call-template name="common_node_attributes"/>
			<column>
				<name>CHAN_ID</name>
				<value>
					<xsl:value-of select="@id"/>
				</value>
			</column>
		</row>
	</xsl:template>
	<xsl:template name="common_node_attributes">
		<column>
			<name>FRAGMENT_ID</name>
			<value>
				<xsl:value-of select="ancestor::fragment/@id"/>
			</value>
		</column>
		<column>
			<name>NODE_ID</name>
			<value>
				<xsl:apply-templates select="." mode="compute_id"/>
			</value>
		</column>
		<column>
			<name>NAME</name>
			<value>
				<xsl:value-of select="@name"/>
			</value>
		</column>
		<column>
			<name>HIDDEN</name>
			<value>
				<xsl:value-of select="@hidden"/>
			</value>
		</column>
		<column>
			<name>IMMUTABLE</name>
			<value>
				<xsl:value-of select="@immutable"/>
			</value>
		</column>
		<column>
			<name>UNREMOVABLE</name>
			<value>
				<xsl:value-of select="@unremovable"/>
			</value>
		</column>
                <xsl:if test="following-sibling::*[self::folder or self::channel]">
		<column>
			<name>NEXT_NODE_ID</name>
			<value>
				<xsl:apply-templates select="following-sibling::*[self::folder or self::channel][position()=1]" mode="compute_id"/>
			</value>
		</column>
                </xsl:if>
                <xsl:if test="preceding-sibling::*[self::folder or self::channel]">
		<column>
			<name>PREV_NODE_ID</name>
			<value>
				<xsl:apply-templates select="preceding-sibling::*[self::folder or self::channel][position()=1]" mode="compute_id"/>
			</value>
		</column>
                </xsl:if>
                <xsl:if test="parent::*[self::folder or self::channel]">
		<column>
			<name>PRNT_NODE_ID</name>
			<value>
				<xsl:apply-templates select="parent::folder" mode="compute_id"/>
			</value>
		</column>
                </xsl:if>
                <xsl:if test="child::*[self::folder or self::channel]">
		<column>
			<name>CHLD_NODE_ID</name>
			<value>
				<xsl:apply-templates select="child::*[self::folder or self::channel][position()=1]" mode="compute_id"/>
			</value>
		</column>
                </xsl:if>
	</xsl:template>
	<!-- the sole purpose of the following two templates is to calculate an unique integer id for a given node -->
	<xsl:template match="folder|channel" mode="compute_id">
		<xsl:choose>
			<xsl:when test="parent::*[self::folder]">
				<!-- establish breadth-first traversal counts on all levels above -->
				<xsl:variable name="bft_count">
					<xsl:apply-templates select="parent::folder" mode="compute_btf_count"/>
				</xsl:variable>
				<xsl:variable name="local_position">
					<xsl:value-of select="count(preceding-sibling::*[self::folder or self::channel]/descendant-or-self::*[self::folder or self::channel])"/>
				</xsl:variable>
				<xsl:value-of select="$bft_count+$local_position+1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="channel|folder" mode="compute_btf_count">
		<xsl:choose>
			<xsl:when test="parent::*[self::folder]">
				<!-- establish breadth-first traversal counts on all levels above -->
				<xsl:variable name="bft_count">
					<xsl:apply-templates select="parent::folder" mode="compute_btf_count"/>
				</xsl:variable>
				<xsl:value-of select="$bft_count+count(preceding-sibling::*[self::folder or self::channel]/descendant-or-self::*[self::folder or self::channel])+1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" url="al.config.xml" htmlbaseurl="" processortype="internal" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo  srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" />
</metaInformation>
-->
