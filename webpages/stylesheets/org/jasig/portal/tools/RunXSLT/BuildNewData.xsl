<?xml version="1.0"?>
<!--
Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.
   
3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."
   
THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: Susan Bramhall, susan.bramhall@yale.edu
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--<xsl:variable name="uripath">c:\\itsproj\portal\uPortal21\portal\build\RunXSLT\
	</xsl:variable>-->
	<xsl:variable name="uripath"/>
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
			<xsl:when test="name='UP_GROUP'">
				<xsl:choose>
					<xsl:when test="$moveGroupsPermissions='yes'">
						<xsl:copy-of select="document(concat($uripath,'NEW_UP_GROUP.XML'))//table"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="name='UP_GROUP_MEMBERSHIP'">
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
				<xsl:copy-of select="document(concat($uripath,'UP_SS_STRUCT_20.XML'))//table"/>
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