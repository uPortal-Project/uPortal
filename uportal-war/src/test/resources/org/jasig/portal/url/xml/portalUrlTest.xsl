<?xml version="1.0" ?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xalan="http://xml.apache.org/xalan" 
    xmlns:portal="http://www.jasig.org/uportal/XSL/portal"
    extension-element-prefixes="portal" 
    exclude-result-prefixes="xalan portal" 
    version="1.0">

    <xalan:component prefix="portal" elements="url param">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortalUrlXalanElements" />
    </xalan:component>

    <xsl:template match="doc">
        <out>
            <xsl:apply-templates />
        </out>
    </xsl:template>

    <xsl:template match="element">
        <xsl:variable name="pageNum">42</xsl:variable>
        <a>
            <xsl:variable name="baseUrl">
              <portal:url>
                <portal:param name="foo" value="bar" />
                <portal:param name="foo" value="bor" />
                <portal:param name="page" value="{$pageNum}" />
                <portal:param name="node"><xsl:value-of select="local-name()"/></portal:param>
                <portal:param name="empty"/>
              </portal:url>
            </xsl:variable>
            <xsl:attribute name="href"><xsl:value-of select="$baseUrl"/></xsl:attribute>
            My URL
        </a>
    </xsl:template>
</xsl:stylesheet>