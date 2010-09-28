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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exslt="http://exslt.org/common"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:java="http://xml.apache.org/xalan/java" 
    xmlns:url="http://xml.apache.org/xalan/java/org.jasig.portal.url.xml.XsltPortalUrlProvider"
    exclude-result-prefixes="java url exslt">
    
    <xsl:param name="XSLT_PORTAL_URL_PROVIDER" />
    <xsl:param name="CURRENT_REQUEST" />
    
    <xsl:template name="portalUrl">
        <xsl:param name="parameters" />
        
        <xsl:variable name="urlProvider" select="url:getUrlProvider($XSLT_PORTAL_URL_PROVIDER)" />
        <xsl:variable name="request" select="url:getHttpServletRequest($CURRENT_REQUEST)" />
    
        <xsl:variable name="url" select="java:getDefaultUrl($urlProvider, $request)" />

        <xsl:if test="exslt:object-type($parameters) = 'RTF'">
            <xsl:variable name="parametersNodeSet" select="exslt:node-set($parameters)" />
            <xsl:for-each select="$parametersNodeSet/portal-param">
                <xsl:value-of select="java:addPortalParameter($url, @name, @value)"/>
            </xsl:for-each>
        </xsl:if>
        
        <xsl:value-of select="java:getUrlString($url)" />
    </xsl:template>

    <xsl:template name="portletUrl">
        <xsl:param name="fname" />
        <xsl:param name="subscribeId" />
        <xsl:param name="windowId" />
        <xsl:param name="type" />
        <xsl:param name="parameters" />
        <xsl:param name="state" />
        <xsl:param name="mode" />

        <xsl:variable name="urlProvider" select="url:getUrlProvider($XSLT_PORTAL_URL_PROVIDER)" />
        <xsl:variable name="request" select="url:getHttpServletRequest($CURRENT_REQUEST)" />
        
        <xsl:variable name="url" select="java:getPortletUrl($urlProvider, $request, $type)" />
        <xsl:choose>
            <xsl:when test="$fname">
                <xsl:value-of select="java:setTargetFname($url, $fname)" />
            </xsl:when>
            <xsl:when test="$subscribeId">
                <xsl:value-of select="java:setTargetSubscribeId($url, $subscribeId)" />
            </xsl:when>
            <xsl:when test="$windowId">
                <xsl:value-of select="java:setTargetWindowId($url, $windowId)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">Either 'fname', 'subscribeId', or 'windowId' parameter must be passed to the portletUrl template</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
        
        <xsl:value-of select="java:setWindowState($url, $state)" />
        <xsl:value-of select="java:setPortletMode($url, $mode)" />

        <xsl:if test="exslt:object-type($parameters) = 'RTF'">
            <xsl:variable name="parametersNodeSet" select="exslt:node-set($parameters)" />
            <xsl:for-each select="$parametersNodeSet/portal-param">
                <xsl:value-of select="java:addPortalParameter($url, @name, @value)"/>
            </xsl:for-each>
        
            <xsl:for-each select="$parametersNodeSet/portlet-param">
                <xsl:value-of select="java:addPortletParameter($url, @name, @value)"/>
            </xsl:for-each>
        </xsl:if>
        
        <xsl:value-of select="java:getUrlString($url)" />
    </xsl:template>

    <xsl:template name="layoutUrl">
        <xsl:param name="folderId" />
        <xsl:param name="parameters" />
        <xsl:param name="action" />

        <xsl:variable name="urlProvider" select="url:getUrlProvider($XSLT_PORTAL_URL_PROVIDER)" />
        <xsl:variable name="request" select="url:getHttpServletRequest($CURRENT_REQUEST)" />
        
        <xsl:variable name="url" select="java:getFolderUrlByNodeId($urlProvider, $request, $folderId)" />

        <xsl:value-of select="java:setAction($url, $action)" />

        <xsl:if test="exslt:object-type($parameters) = 'RTF'">
            <xsl:variable name="parametersNodeSet" select="xalan:nodeset($parameters)" />

            <xsl:for-each select="$parametersNodeSet/layout-param">
                <xsl:value-of select="java:addLayoutParameter($url, @name, @value)"/>
            </xsl:for-each> 

            <xsl:for-each select="$parametersNodeSet/portal-param">
                <xsl:value-of select="java:addPortalParameter($url, @name, @value)"/>
            </xsl:for-each>
        </xsl:if>

        <xsl:value-of select="java:getUrlString($url)" />
    </xsl:template>
</xsl:stylesheet>