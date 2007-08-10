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

	<xsl:template match="/">
		<data>
			<table>
				<name>
					<xsl:value-of select="data/table/name"/>
				</name>
				<rows>
					<xsl:apply-templates select="data/table/rows/row"/>
				</rows>
			</table>
		</data>
	</xsl:template>
	<xsl:template match="row">
		<xsl:choose>
			<xsl:when test="column[name='CHAN_PARM_VAL'and value='CWebProxy/XHTML.ssl']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>XHTML.ssl</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_edit.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_edit.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_info.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_info.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/num_help.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/num_help.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>

			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_edit.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_edit.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_info.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_info.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>
			<xsl:when test="column[name='CHAN_PARM_VAL'      and value='http://www.mun.ca/cc/portal/cw/servlet_help.html']">
				<row>
					<xsl:copy-of select="column[name='CHAN_ID']"/>
					<xsl:copy-of select="column[name='CHAN_PARM_NM']"/>
					<column>
						<name>CHAN_PARM_VAL</name>
						<value>examples/servlet_help.html</value>
					</column>
					<xsl:copy-of select="column[name='CHAN_PARM_OVRD']"/>
				</row>
			</xsl:when>

			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->