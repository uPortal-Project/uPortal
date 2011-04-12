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
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../../../../../../main/resources/xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url">
    
    <xsl:include href="classpath:/layout/theme/urlTemplates.xsl"/>
    
    <xsl:param name="TEST">NOT_SET</xsl:param>
    
    <xsl:template match="doc">
        <out>
            <xsl:choose>
                <xsl:when test="$TEST = 'defaultUrl'">
                    <xsl:apply-templates mode="defaultUrl" />
                </xsl:when>
                <xsl:when test="$TEST = 'layoutUrlById'">
                    <xsl:apply-templates mode="layoutUrlById" />
                </xsl:when>
                <xsl:when test="$TEST = 'portletUrlById'">
                    <xsl:apply-templates mode="portletUrlById" />
                </xsl:when>
                <xsl:when test="$TEST = 'multiPortletUrlById'">
                    <xsl:apply-templates mode="multiPortletUrlById" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message>WARNING: test mode <xsl:value-of select="$TEST"/> is not supported.</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </out>
    </xsl:template>

    <xsl:template match="element" mode="defaultUrl">
        <xsl:variable name="defaultPortalUrl">
          <xsl:call-template name="portalUrl" />
        </xsl:variable>
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$defaultPortalUrl"/></xsl:attribute>My URL
        </a>
    </xsl:template>

    <xsl:template match="element" mode="layoutUrlById">
        <xsl:variable name="pageNum">42</xsl:variable>
        
        <xsl:variable name="removePortletUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url type="action">
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:param name="remove_target" value="{@ID}"/>
                    <url:param name="save" value="{$pageNum}"/>
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$removePortletUrl"/></xsl:attribute>My URL
        </a>
    </xsl:template>

    <xsl:template match="element" mode="portletUrlById">
        <xsl:variable name="pageNum">42</xsl:variable>
        
        <xsl:variable name="maximizePortletUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:param name="pageNum" value="{$pageNum}"/>
                    <url:portlet-url state="maximized">
                        <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                        <url:param name="tmp" value="blah"/>
                    </url:portlet-url>
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <a><xsl:attribute name="href"><xsl:value-of select="$maximizePortletUrl"/></xsl:attribute>My URL</a>
    </xsl:template>

    <xsl:template match="element" mode="multiPortletUrlById">
        <xsl:variable name="pageNum">42</xsl:variable>
        
        <xsl:variable name="maximizePortletUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:param name="pageNum" value="{$pageNum}"/>
                    <url:portlet-url>
                        <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                        <url:param name="tmp" value="blah"/>
                    </url:portlet-url>
                    <url:portlet-url state="minimized">
                        <url:fname>my-portlet</url:fname>
                        <url:param name="event" value="param"/>
                    </url:portlet-url>
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <a><xsl:attribute name="href"><xsl:value-of select="$maximizePortletUrl"/></xsl:attribute>My URL</a>
    </xsl:template>
</xsl:stylesheet>