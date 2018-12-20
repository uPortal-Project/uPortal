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
    @since 4.3

    General Description:  This file was developed to support Javascript-driven rendering of the
    uPortal UI.  This file collects the necessary user layout data and exports xml that is then
    organized and transformed into JSON using the json-v4-3.xsl file and a JSON-specific rendering
    pipeline.  This file is based on the columns.xsl file, but has been updated to include
    additional data.  For the JSON structure transform, all regions, all tabs, favorites, and favorite
    groups are included.  We don't need to be concerned with maximized mode or detached.  The JS UI
    chooses what elements of the layout are applicable based on the URL in the browser.
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->

    <!-- Import standard templates for column output -->
    <xsl:import href="columns-imports.xsl" />

    <!-- ROOT template.  Default to NORMAL window state (since DETACHED window state does not apply). -->
    <xsl:template match="layout">
        <xsl:apply-templates select="folder[@type='root']" mode="NORMAL" />
    </xsl:template>

    <!-- NORMAL page template. -->
    <xsl:template match="folder[@type='root']" mode="NORMAL">
        <layout>
            <xsl:call-template name="debug-info"/>

            <xsl:if test="/layout/@dlm:fragmentName">
                <xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
            </xsl:if>

            <xsl:call-template name="regions" />

            <xsl:call-template name="tabList"/>

            <xsl:if test="$hasFavorites">
                <xsl:call-template name="jsfavorites" />
                <xsl:call-template name="favorite-groups" />
            </xsl:if>
        </layout>
    </xsl:template>

    <xsl:template name="regions">
            <regions>
                <!-- Include all regions -->
                <xsl:for-each select="child::folder[@type!='regular' and @type!='sidebar' and @type!='favorites' and @type!='favorite_collection' and descendant::channel]"><!-- Ignores empty folders -->
                    <xsl:call-template name="region"/>
                </xsl:for-each>
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

            <xsl:for-each select="*">
                <xsl:if test="self::channel">
                    <xsl:call-template name="channel"/>
                </xsl:if>
                <xsl:if test="self::folder">
                    <xsl:call-template name="folder"/>
                </xsl:if>
            </xsl:for-each>
        </tab>
    </xsl:template>

    <xsl:template name="jsfavorites">
        <favorites>
            <xsl:for-each select="/layout/folder/folder[@type='favorites']/*">
                <xsl:if test="self::channel">
                    <xsl:call-template name="channel"/>
                </xsl:if>
                <xsl:if test="self::folder">
                    <xsl:call-template name="folder"/>
                </xsl:if>
            </xsl:for-each>
        </favorites>
    </xsl:template>

    <xsl:template name="favorite-groups">
        <favoriteGroups>
            <xsl:for-each select="/layout/folder/folder[@type='favorite_collection']">
                <favoriteGroup>
                    <xsl:copy-of select="@*"/>
                    <xsl:for-each select="*">
                        <xsl:if test="self::channel">
                            <xsl:call-template name="channel"/>
                        </xsl:if>
                        <xsl:if test="self::folder">
                            <xsl:call-template name="folder"/>
                        </xsl:if>
                    </xsl:for-each>
                </favoriteGroup>
            </xsl:for-each>
        </favoriteGroups>
    </xsl:template>

    <xsl:template match="/layout/folder/folder[@type='favorite_collections']/folder">
        <xsl:call-template name="folder" />
    </xsl:template>

</xsl:stylesheet>
