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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:include href="classpath:/layout/theme/urlTemplates.xsl"/>

    <xsl:template match="doc">
        <out>
            <xsl:apply-templates />
        </out>
    </xsl:template>

    <xsl:template match="element">
        <xsl:variable name="pageNum">42</xsl:variable>
        
        <xsl:variable name="baseUrl">
            <xsl:call-template name="portalUrl"/>
        </xsl:variable>
        <a><xsl:attribute name="href"><xsl:value-of select="$baseUrl"/></xsl:attribute>My URL</a>

        <xsl:variable name="complexUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="parameters">
                    <portal-param name="foo" value="bar" />
                    <portal-param name="foo" value="bor" />
                    <portal-param name="page" value="{$pageNum}" />
                    <portal-param name="node" value="element" />
                    <portal-param name="empty" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <a><xsl:attribute name="href"><xsl:value-of select="$complexUrl"/></xsl:attribute>My URL</a>
    </xsl:template>
</xsl:stylesheet>