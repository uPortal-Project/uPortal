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
    @since 4.2

    General Description:  This file was created in order to help minize duplication of code 
    between columns.xsl and columns-js.xsl (which was derived from columns.xsl).  It contains 
    items that are common between the two.
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->

    <xsl:param name="userLayoutRoot">root</xsl:param>
    <xsl:param name="userImpersonating">false</xsl:param>

    <!-- Check if we have favorites or not -->
    <xsl:variable name="hasFavorites">
        <xsl:choose>
            <xsl:when test="layout/folder/folder[@type='favorites']">true</xsl:when>
            <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- Used to build the tabGroupsList:  discover tab groups, add each to the list ONLY ONCE -->
    <xsl:key name="tabGroupKey" match="layout/folder/folder[@hidden='false' and @type='regular']" use="@tabGroup"/>

    <xsl:variable name="activeTabGroup">DEFAULT_TABGROUP</xsl:variable>

    <!-- 
     | Regions and Roles
     | =================
     | The <regions> section allows non-regular, non-sidebar portlets to appear in the
     | output page, even in focused mode.  In Universality this is done with a 'role' 
     | attribute on the portlet publication record.
     |
     | In Respondr, this is done through regions: folders with a type attribute _other than_
     | 'root', 'regular', or 'sidebar' (for legacy support).  Any folder type beyond these
     | three automatically becomes a region.  Respondr is responsible for recognizing
     | region-based portlets and placing them appropriately on the page.  Note that a region
     | name can appear multiple times in the output;  this approach allows multiple
     | fragments to place portlets in the same region.
     |
     | Regions behave normally in dashboard (normal) and focused (maximized) mode;  in
     | DETACHED window state, only a few regions are processed, and then ONLY IF THE STICKY
     | HEADER option is in effect.  The list of regions included with a sticky-header is:
     | hidden-top, page-top, page-bottom, hidden-bottom.  The remaining regions are not
     | present in the DOM and therefore their portlets MUST NOT be added to the rendering
     | queue. 
     +-->
    <xsl:template name="region">
        <region name="{@type}" title="{@name}">
            <xsl:copy-of select="child::*"/>
        </region>
    </xsl:template>

    <xsl:template name="tabList">
        <navigation>
            <!-- Signals that add-tab prompt is appropriate in the context of this navigation
                 user might or might not actually have permission to add a tab, which is evaluated later (in the theme) -->
            <xsl:attribute name="allowAddTab">true</xsl:attribute>
            <!-- The tabGroups (optional feature) -->
            <tabGroupsList>
                <xsl:attribute name="activeTabGroup">
                    <xsl:value-of select="$activeTabGroup"/>
                </xsl:attribute>
                <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']"><!-- These are standard tabs -->
                    <!-- Process only the first tab in each Tab Group (avoid duplicates) -->
                    <xsl:if test="self::node()[generate-id() = generate-id(key('tabGroupKey',@tabGroup)[1])]">
                        <tabGroup name="{@tabGroup}" firstTabId="{@ID}">
                            <xsl:value-of select="@tabGroup"/>
                        </tabGroup>
                    </xsl:if>
                </xsl:for-each>
            </tabGroupsList>
            <!-- The tabs -->
            <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
                <xsl:call-template name="tab" />
            </xsl:for-each>
        </navigation>
    </xsl:template>

    <!-- List of Favorites
     |   =================
     |   A list of favorited channels. 
     |   To be utilized to establish if "add to favorites" 
     |   or "remove from favorites" shows in the options menu -->
    <xsl:template name="favorites">
        <favorites>
            <xsl:for-each select="/layout/folder/folder[@type='favorites']/folder/channel">
                <favorite fname='{@fname}'/>
            </xsl:for-each>
            <xsl:for-each select="/layout/folder/folder[@type='favorite_collection']/folder/channel">
                <favorite fname='{@fname}'/>
            </xsl:for-each>
        </favorites>
    </xsl:template>

    <xsl:template match="channel">
        <xsl:choose>
            <xsl:when test="$userImpersonating = 'true' and parameter[@name='blockImpersonation']/@value = 'true'">
                <blocked-channel>
                    <xsl:copy-of select="@*"/>
                    <xsl:copy-of select="child::*"/>
                </blocked-channel>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="parameter">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>
