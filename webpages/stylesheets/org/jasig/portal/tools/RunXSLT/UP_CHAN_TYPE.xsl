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
				<name>UP_CHAN_TYPE</name>
				<rows>
					<xsl:apply-templates select="//row">
						<xsl:sort select="column[name='TYPE_ID']/value" data-type="number"/>
					</xsl:apply-templates>
				</rows>
			</table>
		</data>
	</xsl:template>

	<xsl:template match="row">
		<xsl:if test="column[name='TYPE']/value='org.jasig.portal.channels.CWebProxy'">
			<row>
				<column>
					<name>TYPE_ID</name>
					<value>
						<xsl:value-of select="column[name='TYPE_ID']/value"/>
					</value>
				</column>
				<column>
					<name>TYPE</name>
					<value>org.jasig.portal.channels.webproxy.CWebProxy</value>
				</column>
				<column>
					<name>TYPE_NAME</name>
					<value>Web Proxy</value>
				</column>
				<column>
					<name>TYPE_DESCR</name>
					<value>Incorporate a dynamic HTML or XML application</value>
				</column>
				<column>
					<name>TYPE_DEF_URI</name>
					<value>/org/jasig/portal/channels/webproxy/CWebProxy.cpd</value>
				</column>
			</row>
		</xsl:if>
		<xsl:if test="column[name='TYPE']/value!='org.jasig.portal.channels.CWebProxy'">
			<xsl:copy-of select="."/>
		</xsl:if>
		<xsl:if test="position()=last()">
			<row>
				<column>
					<name>TYPE_ID</name>
					<value>
						<xsl:value-of select="column[name='TYPE_ID']/value+1"/>
					</value>
				</column>
				<column>
					<name>TYPE_NAME</name>
					<value>Remote Channel Proxy</value>
				</column>
				<column>
					<name>TYPE</name>
					<value>org.jasig.portal.channels.remotechannel.CRemoteChannel</value>
				</column>
				<column>
					<name>TYPE_DESCR</name>
					<value>Uses SOAP to communicate with and present to the user the contents of a channel living in a remote instance of uPortal</value>
				</column>
				<column>
					<name>TYPE_DEF_URI</name>
					<value>/org/jasig/portal/channels/remotechannel/CRemoteChannel.cpd</value>
				</column>
			</row>

			<row>
				<column>
					<name>TYPE_ID</name>
					<value>
						<xsl:value-of select="column[name='TYPE_ID']/value+2"/>
					</value>
				</column>
				<column>
					<name>TYPE_NAME</name>
					<value>Applet</value>
				</column>
				<column>
					<name>TYPE</name>
					<value>org.jasig.portal.channels.CApplet</value>
				</column>
				<column>
					<name>TYPE_DESCR</name>
					<value>Displays a java applet</value>
				</column>
				<column>
					<name>TYPE_DEF_URI</name>
					<value>/org/jasig/portal/channels/CApplet/CApplet.cpd</value>
				</column>
			</row>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->