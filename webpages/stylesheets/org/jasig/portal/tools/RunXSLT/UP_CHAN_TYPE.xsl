<?xml version="1.0"?>
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