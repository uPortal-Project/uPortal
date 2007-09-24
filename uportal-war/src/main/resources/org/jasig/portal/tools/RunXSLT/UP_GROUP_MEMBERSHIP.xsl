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
				<name>MEMBER_SERVICE</name>
				<value>local</value>
			</column>
			<!-- create the new MEMBER_KEY -->
			<column>
				<name>MEMBER_KEY</name>
				<!-- get the value of the current group_id -->
				<xsl:variable name="currentGroup" select="./column[name='GROUP_ID']/value"/>
				<!-- pick up the corresponding row in UP_GROUP table-->
				<xsl:variable name="groupRow" select="document(concat($uripath,'UP_GROUP_20.xml'))       /data/table/rows/row[column[name='GROUP_ID' and value=$currentGroup]]"/>
				<!-- get the entity_id for person object -->
				<xsl:variable name="personEntityType" select="document(concat($uripath,'UP_GROUP_ENTITY_TYPE_20.XML'))       //row[column[name='ENTITY_TYPE_NAME' and value='org.jasig.portal.security.IPerson']]       /column[name='ENTITY_TYPE_ID']/value"/>
				<xsl:variable name="oldKey" select="column[name='MEMBER_KEY']/value"/>
				<!-- construct the new MEMBER_KEY column -->
				<value>
					<xsl:choose>
						<!-- when this member is not a group and entity type is person 
						substitute the user_name value instead of the user_id -->
						<xsl:when test="$groupRow/column[name='ENTITY_TYPE_ID' and value=$personEntityType]            and column[name='MEMBER_IS_GROUP' and value='F']">
							<xsl:variable name="userRow" select="document(concat($uripath,'UP_USER_20.xml'))           //row[column[name='USER_ID' and value=$oldKey]]"/>
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
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->