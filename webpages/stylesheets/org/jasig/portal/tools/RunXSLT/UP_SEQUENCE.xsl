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
	<xsl:template match="/">
		<data>
			<table>
				<name>UP_SEQUENCE</name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row"/>
					<row>
						<column>
							<name>SEQUENCE_NAME</name>
							<value>UP_ENTITY_TYPE</value>
						</column>
						<column>
							<name>SEQUENCE_VALUE</name>
							<value>20</value>
						</column>
						<!-- Reserve some IDs for internal use, just in case -->
					</row>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="row">

		<xsl:choose>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_CHANNEL']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_CHANNEL</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>
							<xsl:value-of select="column[name='SEQUENCE_VALUE']/value+3"/>
						</value>
					</column>
				</row>
			</xsl:when>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_CHAN_TYPE']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_CHAN_TYPE</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>
							<xsl:value-of select="column[name='SEQUENCE_VALUE']/value+2"/>
						</value>
					</column>
				</row>
			</xsl:when>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_SS_STRUCT'] and column[name='SEQUENCE_VALUE' and value='0']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_SS_STRUCT</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>10</value>
					</column>
				</row>
			</xsl:when>
			<xsl:when test="column[name='SEQUENCE_NAME' and value='UP_SS_THEME'] and column[name='SEQUENCE_VALUE' and value='0']">
				<row>
					<column>
						<name>SEQUENCE_NAME</name>
						<value>UP_SS_THEME</value>
					</column>
					<column>
						<name>SEQUENCE_VALUE</name>
						<value>10</value>
					</column>
				</row>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->