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
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exslt="http://exslt.org/common"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:java="http://xml.apache.org/xalan/java" 
    xmlns:urlGen="http://xml.apache.org/xalan/java/org.jasig.portal.url.xml.XsltPortalUrlProvider"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="java url urlGen exslt">
    
    <xsl:param name="XSLT_PORTAL_URL_PROVIDER" />
    <xsl:param name="CURRENT_REQUEST" />
    
    <xsl:template name="portalUrl">
        <xsl:param name="url" />
        
        <!-- Convert the generic Objects into strongly typed variables for use below -->
        <xsl:variable name="urlProvider" select="urlGen:getUrlProvider($XSLT_PORTAL_URL_PROVIDER)" />
        <xsl:variable name="request" select="urlGen:getHttpServletRequest($CURRENT_REQUEST)" />
        
        <xsl:choose>
            <xsl:when test="exslt:object-type($url) = 'RTF'">
                <xsl:for-each select="exslt:node-set($url)/url:portal-url">
                    <xsl:variable name="portalUrlBuilder" select="java:getPortalUrlBuilder($urlProvider, $request, url:fname, url:layoutId, @type)" />
                    
                    <xsl:for-each select="url:param">
                        <xsl:value-of select="urlGen:addParameter($portalUrlBuilder, @name, @value)" />
                    </xsl:for-each>
                    
                    <xsl:for-each select="url:portlet-url">
                        <xsl:variable name="portletUrlBuilder" select="java:getPortletUrlBuilder($urlProvider, $request, $portalUrlBuilder, url:fname, url:layoutId, @state, @mode, @copyCurrentRenderParameters)" />
                        
                        <xsl:for-each select="url:param">
                            <xsl:value-of select="urlGen:addParameter($portletUrlBuilder, @name, @value)" />
                        </xsl:for-each>
                    </xsl:for-each>
                    
                    <xsl:value-of select="java:getUrlString($portalUrlBuilder)" />
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="portalUrlBuilder" select="java:getPortalUrlBuilder($urlProvider, $request, '', '', '')" />
                <xsl:value-of select="java:getUrlString($portalUrlBuilder)" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>