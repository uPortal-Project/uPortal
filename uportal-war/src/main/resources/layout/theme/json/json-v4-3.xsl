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
<!-- ========================================================================= -->
<!-- ========== README ======================================================= -->
<!-- ========================================================================= -->
<!--
 | The theme is written in XSL. For more information on XSL, refer to
 | [http://www.w3.org/Style/XSL/].  Baseline XSL skill is strongly recommended before modifying
 | this file.
 |
 | This purpose of this file is to provide structure and theme data for use in
 | javascript-driven rendering of the uPortal UI
 |
 | As such, this file has a mixture of code that should not be modified, and code that exists
 | explicitly to be modified.  To help make clear what is what, a RED-YELLOW-GREEN legend has
 | been added to all of the sections of the file.
 |
 | RED: Stop! Do not modify.
 | YELLOW: Warning, proceed with caution.  Modifications can be made, but should not generally be
 |         necessary and may require more advanced skill.
 | GREEN: Go! Modify as desired.
-->

<!-- ========================================================================= -->
<!-- ========== DOCUMENT DESCRIPTION ========================================= -->
<!-- ========================================================================= -->
<!--
 | Date: 04/01/2015
 | Company: Unicon, Inc.
 | Since : uPortal Version 4.3.0
 |
 | General Description: This file, json-v4-3.xsl, was developed for sending layout data to the UI
 | for use in rendering the uPortal UI using Javascript.  The original json.xsl file is now
 | considered 'version 1' and this new file is 'version 4.3'. The original endpoint,
 | "/uPortal/layout.json", has been preserved for backwards compatibility.  However, the same
 | content is now also available at: "/uPortal/api/v41/dlm/layout.json".  A new endpoint,
 | "/uPortal/api/v4-3/dlm/layout.json" to match the uPortal release (new naming convention)
 | , which utilizes this file, has been added.
-->


<!-- ========================================================================= -->
<!-- ========== STYLESHEET DECLARATION ======================================= -->
<!-- ========================================================================= -->
<!--
 | RED
 | This statement defines this document as XSL.
-->
<xsl:stylesheet
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xmlns:upElemTitle="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanLayoutElementTitleHelper"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg upElemTitle"
    version="1.0">

<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== IMPORTS ====================================================== -->
<!-- ========================================================================= -->
<!--
| RED
| Imports are the XSL files that build the theme.
| Import statments and the XSL files they refer to should not be modified.
-->
<xsl:import href="../resourcesTemplates.xsl" />  <!-- Templates for Skin Resource generation -->
<xsl:import href="../urlTemplates.xsl" />        <!-- Templates for URL generation -->
<!-- ========================================================================= -->


<!-- ========================================= -->
<!-- ========== OUTPUT DELCARATION =========== -->
<!-- ========================================= -->
<!--
    | RED
    | This statement instructs the XSL how to output.
-->
<xsl:output method="xml" indent="yes" media-type="text/html"/>
<!-- ========================================= -->

<!-- ============================================== -->
<!-- ========== VARIABLES and PARAMETERS ========== -->
<!-- ============================================== -->
<!--
| YELLOW - GREEN
| These variables and parameters provide flexibility and customization of the user interface.
| Changing the values of the variables and parameters signals the theme to reconfigure use
| and location of user interface components. Most text used within the theme is localized.
-->


<!-- ****** XSL UTILITY PARAMETERS ****** -->
<!--
| RED
| Parameters used by XSL->Java Callbacks
-->
<xsl:param name="CURRENT_REQUEST" />
<xsl:param name="RESOURCES_ELEMENTS_HELPER" />
<xsl:param name="XSLT_PORTAL_URL_PROVIDER" />


<!-- ****** SKIN SETTINGS ****** -->
<!--
| YELLOW
| Skin Settings can be used to change the location of skin files.
-->
<xsl:param name="CONTEXT_PATH">/NOT_SET</xsl:param>
<xsl:variable name="PORTAL_SHORTCUT_ICON" select="concat($CONTEXT_PATH,'/favicon.ico')" />
<!-- ======================================== -->


<!-- ****** LOCALIZATION SETTINGS ****** -->
<!--
| GREEN
| Localization Settings can be used to change the localization of the theme.
-->
<xsl:param name="MESSAGE_DOC_URL">messages.xml</xsl:param>
<xsl:param name="USER_LANG">en</xsl:param>
<!-- ======================================== -->


<!-- ****** PORTAL SETTINGS ****** -->
<!--
| YELLOW
| Portal Settings should generally not be (and not need to be) modified.
-->
<xsl:param name="AUTHENTICATED" select="'false'"/>
<xsl:param name="HOST_NAME"></xsl:param>
<xsl:param name="USER_ID">guest</xsl:param>
<xsl:param name="userName">Guest User</xsl:param>
<xsl:param name="USER_NAME"><xsl:value-of select="$userName"/></xsl:param>
<xsl:param name="USE_SELECT_DROP_DOWN">true</xsl:param>
<xsl:param name="uP_productAndVersion">uPortal</xsl:param>
<xsl:param name="UP_VERSION"><xsl:value-of select="$uP_productAndVersion"/></xsl:param>
<xsl:param name="baseActionURL">render.uP</xsl:param>
<xsl:variable name="BASE_ACTION_URL"><xsl:value-of select="$baseActionURL"/></xsl:variable>
<xsl:param name="EXTERNAL_LOGIN_URL"></xsl:param>

<xsl:variable name="IS_FRAGMENT_ADMIN_MODE">
    <xsl:choose>
        <xsl:when test="//channel[@fname = 'fragment-admin-exit']">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
</xsl:variable>
<xsl:variable name="ADD_TAB_PERMISSION">
    <xsl:choose>
        <xsl:when test="upAuth:hasPermission('UP_SYSTEM', 'ADD_TAB', 'ALL')">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<!-- ========================================================================= -->
<!-- ========== TEMPLATE: ROOT =============================================== -->
<!-- ========================================================================= -->
<!--
| RED
| This is the root xsl template and it defines the overall structure of the html markup.
| Focused and Non-focused content is controlled through an xsl:choose statement.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template match="/">
    <layout><json/>{
    "user": "<xsl:value-of select="$USER_ID"/>",
    "authenticated": "<xsl:value-of select='$AUTHENTICATED'/>",
    "hostname": "<xsl:value-of select='$HOST_NAME'/>",
    "fragmentAdmin" : "<xsl:value-of select='$IS_FRAGMENT_ADMIN_MODE'/>",
    "locale": "<xsl:value-of select="$USER_LANG"/>",
    "layout": {
        "globals": {
            <xsl:apply-templates select="layout/debug" />
        },
        "regions": [
            <xsl:apply-templates select="layout/regions" />
        ],
        "navigation": {
            <xsl:apply-templates select="layout/navigation" />
        },
        "favorites": [
            <xsl:apply-templates select="layout/favorites" />
        ],
        "favoriteGroups": [
            <xsl:apply-templates select="layout/favoriteGroups" />
        ]
    }
}<json/></layout>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: PORTLET =============================================== -->
<!-- ========================================================================= -->
<!--
| RED
| This template defines the method for expressing the presence of a portlet within a layout.
-->
<xsl:template match="channel">
    <xsl:variable name="portletUrl"><xsl:value-of select="$CONTEXT_PATH"/>/api/v4-3/portlet/<xsl:value-of select="@fname"/>.html</xsl:variable>
    <xsl:variable name="iconUrl">
        <xsl:choose>
            <xsl:when test="parameter[@name='mobileIconUrl'] and parameter[@name='mobileIconUrl']/@value != ''">
                <xsl:value-of select="parameter[@name='mobileIconUrl']/@value"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$CONTEXT_PATH"/>/media/skins/icons/mobile/default.png</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
                                        {
                                            "_objectType": "portlet",
                                            "url": "<xsl:value-of select="$portletUrl"/>",
                                            "iconUrl": "<xsl:value-of select="$iconUrl"/>",
                                            <xsl:for-each select="@*[local-name() != 'hidden' and local-name() != 'immutable' and local-name() != 'unremovable']">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>",
                                            </xsl:for-each>
                                            "parameters": {
                                                <xsl:for-each select="parameter">
                                                    "<xsl:value-of select="@name"/>": "<xsl:value-of select="@value"/>"<xsl:if test="position() != last()">,</xsl:if>
                                                </xsl:for-each>
                                            }
                                        }<xsl:if test="position() != last()">,</xsl:if>
</xsl:template>
<!-- ========================================================================= -->
<!-- ========== TEMPLATE: DEBUG ============================================== -->
<!-- ========================================================================= -->
<!--
| RED
-->
<xsl:template match="debug">
    <xsl:for-each select="*">
         "<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
</xsl:template>
<!-- ========================================================================= -->
<!-- ========== TEMPLATE: REGIONS ============================================ -->
<!-- ========================================================================= -->
<!--
| RED
-->

<!-- Collapse the regions with the same name using the Muenchian Method http://www.jenitennison.com/xslt/grouping/muenchian.xml
     TODO: If we upgrade to XSLT 2.0, we can instead use for-each-group, see http://www.xml.com/pub/a/2003/11/05/tr.html -->
<xsl:key name="regions-by-name" match="region" use="@name" />
<xsl:template match="regions">
    <xsl:for-each select="region[count(. | key('regions-by-name', @name)[1]) = 1]">
        {
            <xsl:for-each select="@*">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>",
            </xsl:for-each>
            "content": [
            <xsl:for-each select="key('regions-by-name', @name)">
                <xsl:for-each select="*">
                    <xsl:call-template name="folderContent"/>
                </xsl:for-each>
                <xsl:if test="position() != last()">,</xsl:if>
            </xsl:for-each>
            ]
        }<xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- ========================================================================= -->
<!-- ========== TEMPLATE: NAVIGATION ========================================= -->
<!-- ========================================================================= -->
<!--
| RED
-->
<xsl:template match="navigation">
            <xsl:for-each select="@*">
                <xsl:variable name="attrValue">
                    <xsl:choose>
                        <xsl:when test="local-name(.) = 'allowAddTab' and . = 'true' and $ADD_TAB_PERMISSION = 'true'">true</xsl:when>
                        <xsl:when test="local-name(.) = 'allowAddTab'">false</xsl:when>
                        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                "<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="$attrValue"/>",
            </xsl:for-each>
            "tabGroupsList": {
                <xsl:for-each select="tabGroupsList/@*">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>",
                </xsl:for-each>
                "tabGroups": [
                    <xsl:for-each select="tabGroupsList/tabGroup">
                    {
                        <xsl:for-each select="@*">
                            "<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
                        </xsl:for-each>
                    }<xsl:if test="position() != last()">,</xsl:if>
                    </xsl:for-each>
                ]
            },
            "tabs": [
                <xsl:for-each select="tab">
                {
                    <xsl:for-each select="@*">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="."/>",
                    </xsl:for-each>
                    "content": [
                    <xsl:for-each select="*">
                        <xsl:call-template name="folderContent"/>
                    </xsl:for-each>
                    ]
                }<xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
            ]
</xsl:template>
<!-- ========================================================================= -->
<!-- ========== TEMPLATE: CONTENT ============================================ -->
<!-- ========================================================================= -->
<!--
| RED
-->
<xsl:template match="folder">
                            {
                                 "_objectType": "folder",
                                <xsl:for-each select="@*">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="normalize-space(.)"/>",
                                </xsl:for-each>
                                "content": [
                                    <xsl:for-each select="*">
                                        <xsl:call-template name="folderContent"/>
                                    </xsl:for-each>
                                ]
                            }<xsl:if test="position() != last()">,</xsl:if>
</xsl:template>

<xsl:template name="folderContent">
    <xsl:if test="self::folder">
        <xsl:apply-templates select="self::folder" />
    </xsl:if>
    <xsl:if test="self::channel">
        <xsl:apply-templates select="self::channel"/>
    </xsl:if>
    <xsl:if test="position() != last()">,</xsl:if>
</xsl:template>
<!-- ========================================================================= -->
<!-- ========== TEMPLATE: FAVORITES ========================================== -->
<!-- ========================================================================= -->
<!--
| RED
-->
<xsl:template match="favorites">
    <xsl:for-each select="*">
        <xsl:call-template name="folderContent"/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="favoriteGroups">
            <xsl:apply-templates select="favoriteGroup" />
</xsl:template>

<xsl:template match="favoriteGroup">
    {
        <xsl:for-each select="@*">"<xsl:value-of select ="local-name()"/>": "<xsl:value-of select="normalize-space(.)"/>",
        </xsl:for-each>
        "content": [
        <xsl:for-each select="*">
            <xsl:call-template name="folderContent"/>
        </xsl:for-each>
        ]
    }<xsl:if test="position() != last()">,</xsl:if>
</xsl:template>

<!-- ========================================================================= -->

</xsl:stylesheet>