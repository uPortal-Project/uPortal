<?xml version="1.0" ?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xalan="http://xml.apache.org/xalan" 
    xmlns:portal="http://www.jasig.org/uportal/XSL/portal"
    xmlns:portlet="http://www.jasig.org/uportal/XSL/portlet"
    extension-element-prefixes="portal portlet" 
    exclude-result-prefixes="xalan portal portlet" 
    version="1.0">

    <xalan:component prefix="portal" elements="url param">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortalUrlXalanElements" />
    </xalan:component>
    <xalan:component prefix="portlet" elements="url param">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortletUrlXalanElements" />
    </xalan:component>

    <xsl:template match="doc">
        <out>
            <xsl:apply-templates />
        </out>
    </xsl:template>

    <xsl:template match="element">
        <xsl:variable name="pageNum">42</xsl:variable>
        <a>
            <xsl:variable name="bookmarksUrl">
              <portlet:url fname="bookmarks">
                <portlet:param name="foo" value="bar" />
                <portlet:param name="foo" value="bor" />
                <portlet:param name="page" value="{$pageNum}" />
                <portlet:param name="node"><xsl:value-of select="local-name()"/></portlet:param>
                <portlet:param name="empty"/>
                <portal:param name="something">for the portal</portal:param>
              </portlet:url>
            </xsl:variable>
            <xsl:attribute name="href"><xsl:value-of select="$bookmarksUrl"/></xsl:attribute>
            My URL
        </a>
    </xsl:template>
</xsl:stylesheet>