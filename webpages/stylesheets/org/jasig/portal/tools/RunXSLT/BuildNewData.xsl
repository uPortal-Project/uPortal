<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="uripath">
	</xsl:variable>
	<xsl:variable name="moveGroupsPermissions">yes</xsl:variable>

	<xsl:template match="/">
		<data>
			<xsl:apply-templates select="//table"/>
		</data>
	</xsl:template>

	<xsl:template match="table">
		<xsl:choose>
			<xsl:when test="name='UP_SEQUENCE'">
				<xsl:copy-of select="document(concat($uripath,'NEW_UP_SEQUENCE.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHAN_TYPE'">
				<xsl:copy-of select="document(concat($uripath,'NEW_UP_CHAN_TYPE.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHANNEL'">
				<xsl:copy-of select="document(concat($uripath,'NEW_UP_CHANNEL.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHANNEL_PARAM'">
				<xsl:copy-of select="document(concat($uripath,'NEW_UP_CHANNEL_PARAM.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHAN_TYPE'">
				<xsl:copy-of select="document(concat($uripath,'NEW_UP_ENTITY_TYPE.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_PERMISSION'">
				<xsl:choose>
					<xsl:when test="$moveGroupsPermissions='yes'">
						<xsl:copy-of select="document(concat($uripath,'NEW_UP_PERMISSION.XML'))//table"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="name=UP_GROUP">
				<xsl:choose>
					<xsl:when test="$moveGroupsPermissions='yes'">
						<xsl:copy-of select="document(concat($uripath,'UP_GROUP_20.XML'))//table"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="name=UP_GROUP_MEMBERSHIP">
				<xsl:choose>
					<xsl:when test="$moveGroupsPermissions='yes'">
						<xsl:copy-of select="document(concat($uripath,'NEW_UP_GROUP_MEMBERSHIP.XML'))//table"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- tables with data carried forward but without changes -->
			<xsl:when test="name='UPC_GROUP_MGR'">
				<xsl:copy-of select="document(concat($uripath,'UPC_GROUP_MGR_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UPC_PERM_MGR'">
				<xsl:copy-of select="document(concat($uripath,'UPC_PERM_MGR_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHANNEL'">
				<xsl:copy-of select="document(concat($uripath,'UP_CHANNEL_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHANNEL_PARAM'">
				<xsl:copy-of select="document(concat($uripath,'UP_CHANNEL_PARAM_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_CHAN_TYPE'">
				<xsl:copy-of select="document(concat($uripath,'UP_CHAN_TYPE_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_LAYOUT_PARAM'">
				<xsl:copy-of select="document(concat($uripath,'UP_LAYOUT_PARAM_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_LAYOUT_STRUCT'">
				<xsl:copy-of select="document(concat($uripath,'UP_LAYOUT_STRUCT_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_MIME_TYPE'">
				<xsl:copy-of select="document(concat($uripath,'UP_MIME_TYPE_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_PERSON_DIR'">
				<xsl:copy-of select="document(concat($uripath,'UP_PERSON_DIR_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_MAP'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_MAP_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_STRUCT'">
				<xsl:copy-of select="document(concat($uripath,'Up_ss_struct_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_STRUCT_PAR'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_STRUCT_PAR_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_THEME'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_THEME_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_THEME_PARM'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_THEME_PARM_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_USER_ATTS'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_USER_ATTS_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_SS_USER_PARM'">
				<xsl:copy-of select="document(concat($uripath,'UP_SS_USER_PARM_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_USER'">
				<xsl:copy-of select="document(concat($uripath,'UP_USER_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_USER_LAYOUT'">
				<xsl:copy-of select="document(concat($uripath,'UP_USER_LAYOUT_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_USER_PARAM'">
				<xsl:copy-of select="document(concat($uripath,'UP_USER_PARAM_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_USER_PROFILE'">
				<xsl:copy-of select="document(concat($uripath,'UP_USER_PROFILE_20.XML'))//table"/>
			</xsl:when>
			<xsl:when test="name='UP_USER_UA_MAP'">
				<xsl:copy-of select="document(concat($uripath,'UP_USER_UA_MAP_20.XML'))//table"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->