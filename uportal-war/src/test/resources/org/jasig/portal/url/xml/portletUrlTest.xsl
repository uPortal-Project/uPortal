<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    
    <xsl:include href="classpath:/layout/theme/urlTemplates.xsl"/>

    <xsl:template match="doc">
        <out>
            <xsl:apply-templates />
        </out>
    </xsl:template>

    <xsl:template match="element">
        <xsl:variable name="pageNum">42</xsl:variable>
        
        <xsl:variable name="bookmarksUrl">
            <xsl:call-template name="portletUrl">
                <xsl:with-param name="fname">bookmarks</xsl:with-param>
                <xsl:with-param name="parameters">
                    <portlet-param name="foo" value="bar" />
                    <portlet-param name="foo" value="bor" />
                    <portlet-param name="page" value="{$pageNum}" />
                    <portlet-param name="node" value="{local-name()}" />
                    <portlet-param name="empty" />
                    <portal-param name="something" value="for the portal" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$bookmarksUrl"/></xsl:attribute>My URL
        </a>
        
        <xsl:variable name="bookmarksBySubIdUrl">
            <xsl:call-template name="portletUrl">
                <xsl:with-param name="subscribeId">n1</xsl:with-param>
                <xsl:with-param name="parameters">
                    <portlet-param name="foo" value="bar" />
                    <portlet-param name="foo" value="bor" />
                    <portlet-param name="page" value="{$pageNum}" />
                    <portlet-param name="node" value="{local-name()}" />
                    <portlet-param name="empty" />
                    <portal-param name="something" value="for the portal" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$bookmarksBySubIdUrl"/></xsl:attribute>My URL
        </a>
        
        <xsl:variable name="bookmarksByWinIdUrl">
            <xsl:call-template name="portletUrl">
                <xsl:with-param name="windowId">123.321</xsl:with-param>
                <xsl:with-param name="type">action</xsl:with-param>
                <xsl:with-param name="state">MAXIMIZED</xsl:with-param>
                <xsl:with-param name="mode">edit</xsl:with-param>
                <xsl:with-param name="parameters">
                    <portlet-param name="foo" value="bar" />
                    <portlet-param name="foo" value="bor" />
                    <portlet-param name="page" value="{$pageNum}" />
                    <portlet-param name="node" value="{local-name()}" />
                    <portlet-param name="empty" />
                    <portal-param name="something" value="for the portal" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$bookmarksByWinIdUrl"/></xsl:attribute>My URL
        </a>
    </xsl:template>
</xsl:stylesheet>