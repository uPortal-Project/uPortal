<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<xsl:stylesheet version="1.0" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--=====START: DOCUMENT DESCRIPTION=====-->
<!--
    Version: $Revision$
    @since uPortal 4.2

    General Description:  This file was developed to support Javascript-driven rendering of the
    uPortal UI.  This file collects the necessary user layout data and exports xml that is then
    organized and transformed into JSON using the json-v2.xsl file and a JSON-specific rendering
    pipeline.  This file is based on the columns.xsl file, but has been updated to include
    additional data.
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->

    <!-- Import standard templates for column output -->
    <xsl:import href="columns-imports.xsl" />

    <!-- ROOT template.  Default to NORMAL window state (since DETACHED window state does not apply). -->
    <xsl:template match="layout">
        <xsl:apply-templates select="folder[@type='root']" mode="NORMAL" />
    </xsl:template>

    <!-- NORMAL page template.  Governs the overall structure when the page is non-detached. -->
    <xsl:template match="folder[@type='root']" mode="NORMAL">
        <layout>
            <xsl:call-template name="debug-info"/>

            <xsl:if test="/layout/@dlm:fragmentName">
                <xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
            </xsl:if>

            <xsl:call-template name="regions" />

            <xsl:call-template name="tabList"/>

            <xsl:call-template name="favorites" />

            <xsl:call-template name="favorite-groups" />
        </layout>
    </xsl:template>

    <xsl:template name="regions">
            <regions>
                <xsl:choose>
                    <xsl:when test="$userLayoutRoot = 'root'">
                        <!-- Include all regions when in DASHBOARD mode -->
                        <xsl:for-each select="child::folder[@type!='regular' and @type!='sidebar' and @type!='customize' and channel]"><!-- Ignores empty folders -->
                            <xsl:call-template name="region"/>
                        </xsl:for-each>
                        <!--  Combine 'customize' regions -->
                        <xsl:if test="child::folder[@type='customize' and channel]">
                            <region name="customize">
                                <xsl:for-each select="child::folder[@type='customize' and channel]">
                                    <xsl:copy-of select="channel"/>
                                </xsl:for-each>
                            </region>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- Include all regions EXCEPT 'region-customize' when in FOCUSED mode -->
                        <xsl:for-each select="child::folder[@type!='customize' and @type!='regular' and @type!='sidebar' and channel]"><!-- Ignores empty folders -->
                            <xsl:call-template name="region"/>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </regions>
    </xsl:template>

    <xsl:template name="debug-info">
        <!-- This element is not (presently) consumed by the theme transform, but it can be written to the logs easy debugging -->
        <debug>
            <userLayoutRoot><xsl:value-of select="$userLayoutRoot"></xsl:value-of></userLayoutRoot>
            <hasFavorites><xsl:value-of select="$hasFavorites"/></hasFavorites>
            <activeTabGroup><xsl:value-of select="$activeTabGroup"></xsl:value-of></activeTabGroup>
            <tabsInTabGroup><xsl:value-of select="count(/layout/folder/folder[@tabGroup=$activeTabGroup and @type='regular' and @hidden='false'])"/></tabsInTabGroup>
            <userImpersonation><xsl:value-of select="$userImpersonating"/></userImpersonation>
        </debug>
    </xsl:template>

    <xsl:template match="folder[@type!='root' and @hidden='false']">
        <xsl:attribute name="type">regular</xsl:attribute>
        <xsl:call-template name="folder"/>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="folder">
                <folder>
                    <xsl:copy-of select="@*" />
                    <xsl:for-each select="*">
                        <xsl:if test="self::channel">
                            <xsl:call-template name="channel"/>
                        </xsl:if>
                        <xsl:if test="self::folder">
                            <xsl:call-template name="folder"/>
                        </xsl:if>
                    </xsl:for-each>
                </folder>
    </xsl:template>

    <xsl:template name="channel">
       <channel>
         <xsl:copy-of select="@*" />
         <xsl:apply-templates/>
       </channel>
    </xsl:template>

    <xsl:template name="tab">
        <tab>
            <!-- Copy folder attributes verbatim -->
            <xsl:for-each select="attribute::*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:if test="count(./folder[not(@dlm:addChildAllowed='false')]) >0">
                <xsl:attribute name="dlm:addChildAllowed">true</xsl:attribute>
            </xsl:if>

            <content>
                <xsl:choose>
                    <xsl:when test="$userLayoutRoot = 'root'">
                        <xsl:apply-templates select="folder[@type='regular']"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <focused>
                            <!-- Detect whether a focused channel is present in the user's layout -->
                            <xsl:attribute name="in-user-layout">
                                <xsl:choose>
                                    <xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
                                    <xsl:otherwise>no</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
                        </focused>
                    </xsl:otherwise>
                </xsl:choose>
            </content>
        </tab>
    </xsl:template>

    <xsl:template name="favorites">
        <favorites>
            <xsl:for-each select="/layout/folder/folder[@type='favorites']/folder/channel">
                <favorite>
                    <xsl:apply-templates select="." />
                </favorite>
            </xsl:for-each>
        </favorites>
    </xsl:template>

    <xsl:template name="favorite-groups">
        <favoriteGroups>
            <xsl:for-each select="/layout/folder/folder[@type='favorite_collection']">
                <favoriteGroup>
                    <xsl:copy-of select="@*"/>
                    <xsl:apply-templates select="folder" />
                </favoriteGroup>
            </xsl:for-each>
        </favoriteGroups>
    </xsl:template>

    <xsl:template match="/layout/folder/folder[@type='favorite_collections']/folder">
        <xsl:call-template name="folder" />
    </xsl:template>

</xsl:stylesheet>