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
	<!--<xsl:variable name="uripath">file://c:/itsproj/portal/uportal21/portal/build/RunXSLT/</xsl:variable>-->
	<xsl:variable name="uripath"/>
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_PERMISSION</name>
				<rows>
					<!-- process rows making key change -->
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="data/table/rows/row">
		<row>
			<xsl:for-each select="column">
				<xsl:choose>
					<!--
  in case of PRINCIPAL split into two columns for the
  group entity type
-->
					<xsl:when test="name='PRINCIPAL' and substring-before(./value,'.')='2'">
						<column>
							<name>PRINCIPAL_TYPE</name>
							<value>
								<xsl:value-of select="substring-before(./value,'.')"/>
							</value>
						</column>
						<column>
							<name>PRINCIPAL_KEY</name>
							<value>
								<xsl:variable name="oldKey" select="substring-after(./value,'.')"/>
								<xsl:variable name="userRow" select="document(concat($uripath,'UP_USER_20.XML'))//row[column[name='USER_ID' and value=$oldKey]]"/>
								<xsl:value-of select="$userRow/column[name='USER_NAME']/value"/>
							</value>
						</column>
					</xsl:when>
					<!--
  in case of PRINCIPAL split into two columns for
  all other group entity types
-->
					<xsl:when test="name='PRINCIPAL'">
						<column>
							<name>PRINCIPAL_TYPE</name>
							<value>
								<xsl:value-of select="substring-before(./value,'.')"/>
							</value>
						</column>
						<column>
							<name>PRINCIPAL_KEY</name>
							<value>local.<xsl:value-of select="substring-after(./value,'.')"/></value>
						</column>
					</xsl:when>

					<!-- target needs local. inserted when Groups manager is the owner -->
					<xsl:when test="name='TARGET' and ../column[name='OWNER' and value='org.jasig.portal.channels.groupsmanager.CGroupsManager']">
						<column>
							<name>TARGET</name>
							<value>local.<xsl:value-of select="value"/></value>
						</column>
					</xsl:when>
					<xsl:otherwise>
						<!-- all the other columns are unchanged-->
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</row>
	</xsl:template>
</xsl:stylesheet>