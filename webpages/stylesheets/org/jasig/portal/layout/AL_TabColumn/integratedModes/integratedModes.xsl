<?xml version="1.0" encoding="utf-8"?>
<!--
Copyright (c) 2004 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."

THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: Justin Tilton, jet@immagic.com
        Jon Allen, jfa@immagic.com
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="yes"/>
    <!--<xsl:output method="html" indent="no"/>-->
    <xsl:param name="uP_productAndVersion">uPortal X.X.X</xsl:param>
    <!--These variables and parameters are used in all modes-->
    <xsl:param name="baseActionURL" select="'render.userLayoutRootNode.uP'"/>
    <!--modes: view (default), preferences, fragment-->
    <xsl:variable name="mode" select="/layout/@mode"/>
    <xsl:variable name="current_structure" select="/layout/@current_structure"/>
    <!-- <xsl:variable name="mediaPath">/home/immdca13/workspace/portal/webpages/media/org/jasig/portal/layout/AL_TabColumn/integratedModes</xsl:variable> -->
    <xsl:param name="mediaPath">media/org/jasig/portal/layout/AL_TabColumn/integratedModes</xsl:param>
    <xsl:param name="skin" select="'immII'"/>
    <xsl:variable name="mediaPathSkin" select="concat($mediaPath,'/',$skin,'/skin')"/>
    <!--<xsl:variable name="mediaPathBorder" select="concat($mediaPath,'/',$skin)"/>-->
    <xsl:variable name="mediaPathHeader" select="concat($mediaPath,'/',$skin,'/institutional')"/>
    <xsl:variable name="mediaPathMainBorder" select="concat($mediaPath,'/',$skin,'/mainBorder')"/>
    <xsl:variable name="mediaPathColumnBorder" select="concat($mediaPath,'/',$skin,'/columnBorder')"/>
    <xsl:variable name="mediaPathIcons" select="concat($mediaPath,'/',$skin,'/icons')"/>
    <xsl:param name="errorMessage" select="'no errorMessage passed'"/>
    <!-- <xsl:param name="errorMessage"> newNodID= <xsl:value-of select="/layout/@newNodeID"/> focusedTabID=<xsl:value-of select="/layout/@focusedTabID"/></xsl:param> -->
    <xsl:variable name="authenticated" select="/layout/@authenticated"/>
    <xsl:param name="authorizedChannelPublisher" select="'false'"/>
    <xsl:param name="authorizedFragmentPublisher" select="'false'"/>
    <xsl:param name="userName" select="'Guest'"/>
    <!--These variables and parameters are used in fragment mode-->
    <!-- <xsl:param name="currentFragmentID" select="'default_layout'"/> -->
    <!--These variables and parameters are used in preferences mode-->
    <xsl:param name="moveID" select="/layout/@selectedID"/>
    <xsl:param name="selectedID" select="/layout/@selectedID"/>
    <xsl:param name="focusedTabID" select="/layout/@focusedTabID"/>
    <xsl:param name="targetRestriction" select="/layout/@targetRestriction"/>
    <xsl:param name="targetAction" select="/layout/@targetAction"/>
    <!-- Commented out due to lack of evidence that it is used anywhere
	<xsl:variable name="unauthenticated" select="/layout/@unauthenticated"/>
	-->
    <xsl:variable name="userLayoutRoot" select="/layout/@userLayoutRoot"/>
    <xsl:param name="channelPublishID" select="'no channelPublishID passed'"/>
    <!-- <xsl:param name="uP_fragmentPublishID" select="'no fragmentPublishID passed'"/> -->
    <!--  <xsl:param name="uP_fragmentRootID" select="'no fragmentRootID passed'"/> -->
    <!--  Used for detached content  -->
    <xsl:template match="layout_fragment">
        <html>
            <head>
                <title>
                    <xsl:value-of select="content/channel/@name"/>
                </title>
                <META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT"/>
                <META HTTP-EQUIV="pragma" CONTENT="no-cache"/>
                <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css"/>
                <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}_portlet.css"/>
                <script language="JavaScript">function openBrWindow(theURL,winName,features) {window.open(theURL,winName,features);}</script>
            </head>
            <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" class="uportal-background-content">
                <table width="100%" border="0" cellspacing="0" cellpadding="10">
                    <tr class="uportal-background-content">
                        <td class="uportal-background-content">
                            <xsl:for-each select="content//channel">
                                <xsl:apply-templates select=".">
                                    <xsl:with-param name="detachedContent" select="'true'"/>
                                </xsl:apply-templates>
                            </xsl:for-each>
                        </td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="layout">
        <html>
            <head>
                <title><xsl:value-of select="$uP_productAndVersion"/></title>
                <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css"/>
                <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}_portlet.css"/>
                <xsl:call-template name="scripts"/>
            </head>
            <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="scripts">
        <script language="JavaScript"><![CDATA[

     function openBrWindow(theURL,winName,features)
       {window.open(theURL,winName,features);}

    ]]></script>
    </xsl:template>
    <xsl:template match="header">
        <!-- BEGIN: Masthead and Header Channel -->
        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-content">
            <tr>
                <td align="left" valign="top">
                    <img src="{$mediaPathHeader}/mainlogo.gif" alt="" title=""/>
                </td>
                <td width="100%">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <!--BEGIN: Temporary Header Channel: Home,SiteMap,Prefs,Logout -->
                <xsl:if test="$authenticated='true' ">
                    <td align="right" valign="top">
                        <table border="0" cellspacing="10" cellpadding="0">
                            <tr>
                                <td>
                                    <a href="{$baseActionURL}?uP_root=root&amp;uP_reload_layout=true&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true&amp;uP_sparam=mode&amp;mode=view">
                                        <xsl:if test="not($mode='view')">
                                            <xsl:attribute name="onclick">return confirm('Have you saved your changes?\nAre you sure you want to exit preferences?')</xsl:attribute>
                                        </xsl:if>
                                        <img src="{$mediaPathIcons}/home.gif" width="24" height="24" alt="Home" title="Home" border="0"/>
                                    </a>
                                </td>
                                <td>
                                    <a href="{$baseActionURL}?uP_fname=layout-sitemap&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                                        <img src="{$mediaPathIcons}/sitemap.gif" width="24" height="24" alt="View Sitemap" title="View Sitemap" border="0"/>
                                    </a>
                                </td>
                                <xsl:if test="$authorizedChannelPublisher='true'">
                                    <td>
                                        <a href="{$baseActionURL}?uP_fname=portal/channelmanager/general">
                                            <img width="24" height="24" alt="Channel Manager" title="Channel Manager" src="{$mediaPathIcons}/channelmanager.gif" border="0"/>
                                        </a>
                                    </td>
                                </xsl:if>
                                <xsl:if test="$userLayoutRoot='root'">
                                    <xsl:choose>
                                        <xsl:when test="$mode='view' and $authenticated='true'">
                                            <td>
                                                <a href="{$baseActionURL}?uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}">
                                                    <img width="24" height="24" alt="Turn on Preferences" title="Turn on Preferences" src="{$mediaPathIcons}/preferences.gif" border="0"/>
                                                </a>
                                            </td>
                                        </xsl:when>
                                        <xsl:when test="$mode='preferences'">
                                            <td>
                                                <a href="{$baseActionURL}?uP_sparam=mode&amp;mode=view&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                                                    <img width="24" height="24" alt="Turn off Preferences" title="Turn off Preferences" src="{$mediaPathIcons}/preferencesoff.gif" border="0"/>
                                                </a>
                                            </td>
                                            <td>
                                                <a href="{$baseActionURL}?uP_sparam=uP_save&amp;uP_save=all">
                                                    <img width="24" height="24" alt="Save Changes" title="Save Changes" src="{$mediaPathIcons}/save.gif" border="0"/>
                                                </a>
                                            </td>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:if>
                                <td>
                                    <a href="Logout" class="uportal-navigation-category">
                                        <img src="{$mediaPathIcons}/logout.gif" width="24" height="24" alt="Logout" title="Logout" border="0"/>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="10">
                                    <p class="uportal-channel-strong">Welcome <xsl:value-of select="$userName"/>
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </xsl:if>
                <!--END: Temporary Header Channel: Home,Prefs,Logout -->
            </tr>
        </table>
        <!-- END: Masthead and Header Channel -->
    </xsl:template>
    <xsl:template match="content">
        <xsl:variable name="numCols" select="count(column)"/>
        <table width="100%" border="0" cols="{$numCols}" cellspacing="9" cellpadding="0">
            <tr>
                <xsl:apply-templates>
                    <xsl:with-param name="type" select="'column'"/>
                </xsl:apply-templates>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="channel">
        <!-- Begin Channel Table -->
        <table width="100%" cols="1" border="0" align="center" cellpadding="0" cellspacing="0">
            <xsl:choose>
                <xsl:when test="$selectedID=@ID">
                    <tr>
                        <td>
                            <img src="{$mediaPathMainBorder}/topleftcorner.gif" width="20" height="9"/>
                        </td>
                        <td width="100%" style="background-image: url({$mediaPathMainBorder}/topborder.gif); background-repeat:repeat-x;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathMainBorder}/toprightcorner.gif" width="19" height="9"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="background-image: url({$mediaPathMainBorder}/headerleftborderselected.gif); background-repeat:repeat-y;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                        </td>
                        <td nowrap="nowrap" class="uportal-background-selected">
                            <span class="uportal-channel-title">
                                <a name="{@ID}">
                                    <!-- 
                                        | This element will be replaced by the 
                                        | CharacterCachingChannelIncorporationFilter
                                        | with the dynamic channel title, or with the
                                        | value of the name attribute of the channel element
                                        | we matched to get here, in the case where there is no
                                        | dynamic channel title.
                                        +-->
                                    <xsl:element name="channel-title">
                                        <xsl:attribute name="defaultValue">
                                            <xsl:value-of select="@name"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="channelSubscribeId">
                                            <xsl:value-of select="@ID" />
                                        </xsl:attribute>
                                    </xsl:element>	
                                </a>
                                <xsl:text/>(selected)</span>
                        </td>
                        <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/headerrightborderselected.gif); background-repeat:repeat-y;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="{$mediaPathMainBorder}/headerbottomleft.gif" width="20" height="8"/>
                        </td>
                        <td style="background-image: url({$mediaPathMainBorder}/headerbottomborder.gif); background-repeat:repeat-x;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathMainBorder}/headerbottomright.gif" width="19" height="8"/>
                        </td>
                    </tr>
                </xsl:when>
                <xsl:otherwise>
                    <tr>
                        <td>
                            <img src="{$mediaPathMainBorder}/topleftcorner.gif" width="20" height="9"/>
                        </td>
                        <td width="100%" style="background-image: url({$mediaPathMainBorder}/topborder.gif); background-repeat:repeat-x;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathMainBorder}/toprightcorner.gif" width="19" height="9"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="background-image: url({$mediaPathMainBorder}/headerleftborder.gif); background-repeat:repeat-y;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                        </td>
                        <td nowrap="nowrap" class="uportal-background-semidark">
                            <span class="uportal-channel-title">
                                <a name="{@ID}">
                                    <!-- 
                                        | This element will be replaced by the 
                                        | CharacterCachingChannelIncorporationFilter
                                        | with the dynamic channel title, or with the
                                        | value of the name attribute of the channel element
                                        | we matched to get here, in the case where there is no
                                        | dynamic channel title.
                                        +-->
                                    <xsl:element name="channel-title">
                                        <xsl:attribute name="defaultValue">
                                            <xsl:value-of select="@name"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="channelSubscribeId">
                                            <xsl:value-of select="@ID" />
                                        </xsl:attribute>
                                    </xsl:element>	
                                </a>
                            </span>
                        </td>
                        <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/headerrightborder.gif); background-repeat:repeat-y;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="{$mediaPathMainBorder}/headerbottomleft.gif" width="20" height="8"/>
                        </td>
                        <td style="background-image: url({$mediaPathMainBorder}/headerbottomborder.gif); background-repeat:repeat-x;">
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathMainBorder}/headerbottomright.gif" width="19" height="8"/>
                        </td>
                    </tr>
                </xsl:otherwise>
            </xsl:choose>
            <tr>
                <td style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;" valign="bottom">
                    <img src="{$mediaPathMainBorder}/channellinesbottom.gif" width="20" height="1"/>
                </td>
                <td nowrap="nowrap" class="uportal-background-light">
                    <xsl:call-template name="channelRestrictions"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathMainBorder}/channeltopleft.gif" width="20" height="19"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/channeltopborder.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="19"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/channeltopright.gif" width="19" height="19"/>
                </td>
            </tr>
            <tr>
                <td style="background-image: url({$mediaPathMainBorder}/channelleftborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td width="100%">
                                <!-- Don't render any content for a minimized channel -->
                                <xsl:if test="@minimized != 'true'">
                                    <xsl:copy-of select="."/>
                                </xsl:if>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                        </tr>
                    </table>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/channelrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathMainBorder}/bottomleftcorner.gif" width="20" height="19"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/bottomborder.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/bottomrightcorner.gif" width="19" height="19"/>
                </td>
            </tr>
        </table>
        <!-- End Channel Table -->
        <xsl:variable name="channelID" select="@ID"/>
        <xsl:if test="not($channelID=../channel[last()]/@ID)">
            <!-- Begin Channel Spacer Table -->
            <table width="100%" border="0" cellspacing="0" cellpadding="4">
                <tr>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
            </table>
            <!-- End Channel Spacer Table -->
        </xsl:if>
    </xsl:template>
    <xsl:template match="footer"/>
    <xsl:template match="focusedContent">
        <table width="100%" cols="2" border="0" cellspacing="9" cellpadding="0">
            <tr>
                <td valign="top" width="100%" class="uportal-background-dark">
                    <xsl:apply-templates select="*"/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="navigation">
        <!-- Begin Tab Block -->
        <table width="100%" cols="1" border="0" cellspacing="0" cellpadding="0" class="uportal-background-content">
            <!--Determined that Form is unneccessary-->
            <!--			<form name="formRename" method="post" action="{$baseActionURL}">-->
            <tr>
                <xsl:apply-templates>
                    <xsl:with-param name="type" select="'tab'"/>
                </xsl:apply-templates>
                <td width="100%" valign="bottom" class="uportal-background-content">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                        </tr>
                        <tr class="uportal-background-shadow">
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <!--			</form>-->
        </table>
        <!-- End Tab Block -->
    </xsl:template>
    <xsl:template match="inactiveTab">
        <td valign="bottom">
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td colspan="5" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="5">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="4">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="3">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td nowrap="nowrap" class="uportal-background-med">
                        <table border="0" cellspacing="0" cellpadding="4">
                            <tr>
                                <td nowrap="nowrap">
                                    <xsl:choose>
                                        <xsl:when test="$targetAction='Tab Move' or $targetAction='Channel Move' or $targetAction='Column Move'">
                                            <a class="uportal-navigation-category" href="{$baseActionURL}?uP_request_move_targets={$selectedID}&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={$selectedID}&amp;uP_sparam=targetRestriction&amp;targetRestriction={$targetRestriction}">
                                                <xsl:value-of select="@name"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$targetAction='New Tab' or $targetAction='New Column'">
                                            <a class="uportal-navigation-category" href="{$baseActionURL}?uP_request_add_targets=folder&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={$selectedID}&amp;uP_sparam=targetRestriction&amp;targetRestriction={$targetRestriction}">
                                                <xsl:value-of select="@name"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$targetAction='New Channel'">
                                            <a class="uportal-navigation-category" href="{$baseActionURL}?uP_request_add_targets=channel&amp;uP_sparam=channelPublishID&amp;channelPublishID={$channelPublishID}&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={$selectedID}&amp;uP_sparam=targetRestriction&amp;targetRestriction={$targetRestriction}">
                                                <xsl:value-of select="@name"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <a class="uportal-navigation-category" href="{$baseActionURL}?uP_sparam=focusedTabID&amp;focusedTabID={@ID}&amp;uP_sparam=mode&amp;mode={$mode}">
                                                <xsl:value-of select="@name"/>
                                            </a>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr class="uportal-background-shadow">
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                </tr>
            </table>
        </td>
    </xsl:template>
    <xsl:template match="focusedTab">
        <td valign="bottom">
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td colspan="5" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="5">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="4">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="3">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td nowrap="nowrap" class="uportal-background-dark">
                        <!--Tab name block-->
                        <table border="0" cellspacing="0" cellpadding="4">
                            <tr>
                                <form name="formRename" method="post" action="{$baseActionURL}">
                                    <xsl:choose>
                                        <xsl:when test="$mode='view'">
                                            <td nowrap="nowrap" class="navigation-selected">
                                                <xsl:value-of select="@name"/>
                                            </td>
                                        </xsl:when>
                                        <xsl:when test="$mode='preferences'">
                                            <td nowrap="nowrap">
                                                <xsl:call-template name="tabRestrictions"/>
                                            </td>
                                        </xsl:when>
                                    </xsl:choose>
                                </form>
                            </tr>
                        </table>
                        <!--End Tab name block-->
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-dark">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr class="uportal-background-dark">
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                </tr>
            </table>
        </td>
    </xsl:template>
    <xsl:template match="selectedTab">
        <td valign="bottom">
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td colspan="5" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="5">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="4" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="4">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content" colspan="3">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td width="2" class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td nowrap="nowrap" class="uportal-background-selected">
                        <!-- Begin Selected Tab Content Table -->
                        <table border="0" cellspacing="0" cellpadding="4">
                            <tr>
                                <td class="uportal-background-selected" nowrap="nowrap">
                                    <span class="uportal-navigation-category-selected">
                                        <xsl:value-of select="@name"/>
                                    </span>
                                    <img src="{$mediaPathSkin}/transparent.gif" width="8" height="8"/>
                                    <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                                        <img src="{$mediaPathIcons}/cancelmoveicon.gif" width="20" height="17" alt="Cancel Tab Move" title="Cancel Tab Move" border="0"/>
                                    </a>
                                </td>
                            </tr>
                        </table>
                        <!-- End Selected Tab Content Table -->
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-med">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-selected">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-light">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-content">
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                </tr>
                <tr class="uportal-background-dark">
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td>
                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                    </td>
                    <td class="uportal-background-shadow">
                        <img src="{$mediaPathSkin}/transparent.gif" width="2" height="1"/>
                    </td>
                </tr>
            </table>
        </td>
    </xsl:template>
    <xsl:template match="move_target">
        <xsl:param name="type"/>
        <xsl:choose>
            <xsl:when test="$type='tab'">
                <!-- Move target Icon for Tabs -->
                <td align="center" valign="bottom" class="uportal-background-content">
                    <table border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                            <td valign="middle" width="100%">
                                <a href="{$baseActionURL}?uP_move_target={$moveID}&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                                    <img alt="Click to select target location" title="Click to select target location" src="{$mediaPathIcons}/airplanetarget.gif" width="16" height="16" border="0"/>
                                </a>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="5"/>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                        </tr>
                        <tr class="uportal-background-shadow">
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="5" height="1"/>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                            <td>
                                <img src="{$mediaPathSkin}/transparent.gif" width="5" height="1"/>
                            </td>
                        </tr>
                    </table>
                </td>
                <!-- End Move Target Icon -->
            </xsl:when>
            <xsl:when test="$type='column'">
                <td valign="top">
                    <a href="{$baseActionURL}?uP_move_target={$moveID}&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                        <img alt="Click to move column here" title="Click to move column here" src="{$mediaPathIcons}/add_column_ani.gif" width="22" height="18" border="0"/>
                    </a>
                </td>
            </xsl:when>
            <xsl:when test="$type='channel'">
                <table width="100%" border="0" cellspacing="0" cellpadding="4">
                    <tr>
                        <td align="center">
                            <a href="{$baseActionURL}?uP_move_target={$moveID}&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                                <img alt="Click to move channel here" title="Click to move channel here" src="{$mediaPathIcons}/add_channel_ani.gif" width="22" height="18" border="0"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="add_target">
        <!-- Add target Icon in the Tabline -->
        <xsl:if test="$targetRestriction='tab'">
            <td align="center" valign="bottom" class="uportal-background-content">
                <table border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td valign="middle">
                            <!-- &amp;uP_sparam=selectedID&amp;selectedID= -->
                            <a href="{$baseActionURL}?uP_add_target=folder&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_cancel_targets=true&amp;uP_sparam=newNodeID&amp;newNodeID=">
                                <img alt="Click to add new tab here" title="Click to add new tab here" src="{$mediaPathIcons}/airplanetarget.gif" width="16" height="16" border="0"/>
                            </a>
                        </td>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="5"/>
                        </td>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr class="uportal-background-shadow">
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="5" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPathSkin}/transparent.gif" width="5" height="1"/>
                        </td>
                    </tr>
                </table>
            </td>
        </xsl:if>
        <!-- End Add Target Icon in the Tabline -->
        <!-- Add target Icon in the Columns -->
        <xsl:if test="$targetRestriction='column'">
            <td align="center" valign="top">
                <table border="0" cellspacing="0" cellpadding="5" class="uportal-background-neutral-light">
                    <tr>
                        <td valign="top">
                            <a href="{$baseActionURL}?uP_add_target=folder&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true&amp;uP_sparam=newNodeID&amp;newNodeID=">
                                <img alt="Click to add new column here" title="Click to add new column here" src="{$mediaPathIcons}/add_column_ani.gif" width="22" height="18" border="0"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </td>
        </xsl:if>
        <!-- End Add Target Icon in the Columns -->
        <!-- Add target Icon in the Channels -->
        <xsl:if test="$targetRestriction='channel'">
            <table width="100%" border="0" cellspacing="0" cellpadding="4">
                <tr>
                    <td align="center">
                        <a href="{$baseActionURL}?uP_add_target=channel&amp;targetNextID={@nextID}&amp;targetParentID={@parentID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true&amp;uP_sparam=newNodeID&amp;newNodeID=">
                            <img alt="Click to add new channel here" title="Click to add new channel here" src="{$mediaPathIcons}/add_channel_ani.gif" width="22" height="18" border="0"/>
                        </a>
                    </td>
                </tr>
            </table>
        </xsl:if>
        <!-- End Add Target Icon in the Channels -->
    </xsl:template>
    <xsl:template match="@name" mode="view">
        <td nowrap="nowrap" class="uportal-navigation-category-selected">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>
    <xsl:template match="@name" mode="preferences">
        <td nowrap="nowrap">
            <a class="uportal-navigation-category" href="#">
                <xsl:value-of select="."/>
            </a>
        </td>
    </xsl:template>
    <xsl:template match="login">
        <table width="100%" cols="1" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="5" height="5"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td width="100%">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="5" height="5"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/topleftcornerselected.gif" width="20" height="9"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/topborderselected.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/toprightcornerselected.gif" width="19" height="9"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/headerleftborderselected.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                </td>
                <td nowrap="nowrap" class="uportal-background-selected">
                    <span class="uportal-channel-title">Welcome Guest - Please Login</span>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/headerrightborderselected.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/headerbottomleftselected.gif" width="20" height="8"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/headerbottomborderselected.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/headerbottomrightselected.gif" width="19" height="8"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td nowrap="nowrap" class="uportal-background-light">
                    <xsl:copy-of select="channel"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td valign="bottom" style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                    <table border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td valign="bottom">
                                <img src="{$mediaPathMainBorder}/iconbarlinesbottom.gif" width="20" height="6"/>
                            </td>
                        </tr>
                    </table>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/prefsbottom.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathMainBorder}/prefsbottomright.gif" width="19" height="6"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="actions">
        <table width="100%" cols="1" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="9" height="9"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td width="100%">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="9" height="9"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/topleftcornerselected.gif" width="20" height="9"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/topborderselected.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/toprightcornerselected.gif" width="19" height="9"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/headerleftborderselected.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                </td>
                <td nowrap="nowrap" class="uportal-background-selected">
                    <span class="uportal-channel-title">
                        <!-- temporary fix until fragment mode added -->
                        <xsl:choose>
                            <xsl:when test=" $current_structure = 'fragment' ">Fragment Content Actions</xsl:when>
                            <xsl:otherwise>User Preferences Actions</xsl:otherwise>
                        </xsl:choose>
                    </span>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/headerrightborderselected.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="19" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/headerbottomleftselected.gif" width="20" height="8"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/headerbottomborderselected.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathMainBorder}/headerbottomrightselected.gif" width="19" height="8"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td align="left" class="uportal-background-light">
                    <xsl:call-template name="actionList"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td nowrap="nowrap" class="uportal-background-light">
                    <img src="{$mediaPathSkin}/transparent.gif" width="4" height="4"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <xsl:choose>
                <xsl:when test="not($errorMessage='no errorMessage passed')">
                    <xsl:call-template name="messageRow"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="statusCheck"/>
                </xsl:otherwise>
            </xsl:choose>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td valign="bottom" style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                    <table border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td valign="bottom">
                                <img src="{$mediaPathMainBorder}/iconbarlinesbottom.gif" width="20" height="6"/>
                            </td>
                        </tr>
                    </table>
                </td>
                <td style="background-image: url({$mediaPathMainBorder}/prefsbottom.gif); background-repeat:repeat-x;">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                    <img src="{$mediaPathMainBorder}/prefsbottomright.gif" width="19" height="6"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template name="statusCheck"/>
    <xsl:template name="messageRow">
        <!-- Message Row -->
        <tr>
            <td>
                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
            </td>
            <td style="background-image: url({$mediaPathMainBorder}/iconbarlinesleft.gif); background-repeat:repeat-y;">
                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
            </td>
            <td nowrap="nowrap" class="uportal-background-light">
                <xsl:call-template name="message"/>
            </td>
            <td class="uportal-background-dark" style="background-image: url({$mediaPathMainBorder}/iconbarrightborder.gif); background-repeat:repeat-y;">
                <img src="{$mediaPathSkin}/transparent.gif" width="11" height="8"/>
            </td>
            <td>
                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
            </td>
        </tr>
        <!-- End Message Row -->
    </xsl:template>
    <xsl:template name="message">
        <xsl:param name="messageString" select="$errorMessage"/>
        <!-- Message Table -->
        <table border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3" class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3" class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark">
                    <span class="uportal-channel-title">
                        <!-- ~ -->
                        <!--  Can be used for debugging - push messages out to the interface -->
                        <!-- ~ -->
                        <xsl:value-of select="$messageString"/>
                    </span>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2" class="uportal-background-dark">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-content">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="4">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td>
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="2">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
            <tr>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3" class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3" class="uportal-background-med">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
                <td colspan="3">
                    <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                </td>
            </tr>
        </table>
        <!-- End Message Table -->
    </xsl:template>
    <xsl:template match="column">
        <xsl:choose>
            <xsl:when test="$mode='preferences'">
                <td valign="top" width="{@width}%">
                    <!-- Begin Column Table -->
                    <table width="100%" cols="1" border="0" align="center" cellpadding="0" cellspacing="0">
                        <xsl:choose>
                            <xsl:when test="@ID=$selectedID">
                                <tr>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/topleftcornerGselected.gif" width="20" height="9"/>
                                    </td>
                                    <td width="100%" style="background-image: url({$mediaPathColumnBorder}/topborderGselected.gif); background-repeat:repeat-x;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/toprightcornerGselected.gif" width="20" height="9"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-image: url({$mediaPathColumnBorder}/headerleftborderselectedG.gif); background-repeat:repeat-y;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td nowrap="nowrap" class="uportal-background-selected">
                                        <span class="uportal-channel-title">Selected Column</span>
                                    </td>
                                    <td style="background-image: url({$mediaPathColumnBorder}/headerrightborderselectedG.gif); background-repeat:repeat-y;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/headerbottomleftGselected.gif" width="20" height="2"/>
                                    </td>
                                    <td style="background-image: url({$mediaPathColumnBorder}/headerbottomborderGselected.gif); background-repeat:repeat-x;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/headerbottomrightGselected.gif" width="20" height="2"/>
                                    </td>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/topleftcornerG.gif" width="20" height="8"/>
                                    </td>
                                    <td width="100%" style="background-image: url({$mediaPathColumnBorder}/topborderG.gif); background-repeat:repeat-x;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/toprightcornerG.gif" width="20" height="8"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-image: url({$mediaPathColumnBorder}/headerleftborderG.gif); background-repeat:repeat-y;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td nowrap="nowrap" class="uportal-background-neutral-dark">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td class="uportal-background-dark" style="background-image: url({$mediaPathColumnBorder}/headerrightborderG.gif); background-repeat:repeat-y;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/headerbottomleftG.gif" width="20" height="1"/>
                                    </td>
                                    <td style="background-image: url({$mediaPathColumnBorder}/headerbottomborderG.gif); background-repeat:repeat-x;">
                                        <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                                    </td>
                                    <td>
                                        <img src="{$mediaPathColumnBorder}/headerbottomrightG.gif" width="20" height="1"/>
                                    </td>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                        <tr>
                            <td class="uportal-background-neutral-light">
                                <img src="{$mediaPathColumnBorder}/iconbarlinesleftG.gif" width="20" height="38"/>
                            </td>
                            <td nowrap="nowrap" class="uportal-background-neutral-light">
                                <table border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <xsl:call-template name="columnRestrictions"/>
                                    </tr>
                                </table>
                            </td>
                            <td class="uportal-background-dark" style="background-image: url({$mediaPathColumnBorder}/iconbarrightborderG.gif); background-repeat:repeat-y;">
                                <img src="{$mediaPathSkin}/transparent.gif" width="20" height="1"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <img src="{$mediaPathColumnBorder}/channeltopleftG.gif" width="20" height="13"/>
                            </td>
                            <td style="background-image: url({$mediaPathColumnBorder}/channeltopborderG.gif); background-repeat:repeat-x;">
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="13"/>
                            </td>
                            <td>
                                <img src="{$mediaPathColumnBorder}/channeltoprightG.gif" width="20" height="13"/>
                            </td>
                        </tr>
                        <tr>
                            <td style="background-image: url({$mediaPathColumnBorder}/channelleftborderG.gif); background-repeat:repeat-y;">
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                            <td valign="top" class="uportal-background-dark">
                                <xsl:apply-templates>
                                    <xsl:with-param name="type" select="'channel'"/>
                                </xsl:apply-templates>
                            </td>
                            <td class="uportal-background-dark" style="background-image: url({$mediaPathColumnBorder}/channelrightborderG.gif); background-repeat:repeat-y;">
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="1"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <img src="{$mediaPathColumnBorder}/bottomleftcornerG.gif" width="20" height="20"/>
                            </td>
                            <td style="background-image: url({$mediaPathColumnBorder}/bottomborderG.gif); background-repeat:repeat-x;">
                                <img src="{$mediaPathSkin}/transparent.gif" width="1" height="8"/>
                            </td>
                            <td>
                                <img src="{$mediaPathColumnBorder}/bottomrightcornerG.gif" width="20" height="20"/>
                            </td>
                        </tr>
                    </table>
                    <!-- End Column Table -->
                </td>
            </xsl:when>
            <xsl:otherwise>
                <td valign="top" width="{@width}%" class="uportal-background-dark">
                    <xsl:apply-templates/>
                </td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="calculateShrinkColumn">
        <xsl:if test="number(@width)&gt;5">
            <xsl:call-template name="calculateAdjustColumn">
                <xsl:with-param name="increment" select="-5"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template name="calculateExpandColumn">
        <xsl:if test="number(@width)&lt;95">
            <xsl:call-template name="calculateAdjustColumn">
                <xsl:with-param name="increment" select="5"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template name="calculateAdjustColumn">
        <!-- The following algorithm computes a new width value that,
	         when normalized with the other widths by the structure stylesheet,
	         will produce the desired width increase increment.
	         For example, if you start with 2 columns, each 50%, and you want to
	         increase one of the columns by 5%, this algorithm would set that
	         column width to 61.11.  When the new values (61.11 and 50) are normalized,
	         you end up with the desired effect: 2 columns at 55% and 45%.
	    -->
        <xsl:param name="increment">0</xsl:param>
        <xsl:variable name="width" select="@width"/>
        <xsl:variable name="sum" select="sum(../column/@width)"/>
        <xsl:variable name="newWidth" select="$width + (($increment * $sum) div (100 - $width - $increment))"/>
        <xsl:variable name="thisId" select="@ID"/>
        <xsl:choose>
            <xsl:when test="number($increment) &lt; 0">
                <input name="shrinkColumn" type="image" src="{$mediaPathIcons}/columnshrink.gif" width="28" height="25" border="0" alt="Shrink this column by 5%" title="Shrink this column by 5%"/>
            </xsl:when>
            <xsl:otherwise>
                <input name="expandColumn" type="image" src="{$mediaPathIcons}/columnexpand.gif" width="28" height="25" border="0" alt="Expand this column by 5%" title="Expand this column by 5%"/>
            </xsl:otherwise>
        </xsl:choose>
        <input type="hidden" name="uP_sparam" value="mode"/>
        <input type="hidden" name="mode" value="{$mode}"/>
        <input type="hidden" name="uP_sparam" value="focusedTabID"/>
        <input type="hidden" name="focusedTabID" value="{$focusedTabID}"/>
        <input type="hidden" name="uP_sparam" value="targetRestriction"/>
        <input type="hidden" name="targetRestriction" value="no targetRestriction parameter"/>
        <input type="hidden" name="uP_sfattr" value="width"/>
        <input type="hidden" name="width_folderId" value="{@ID}"/>
        <input type="hidden" name="width_{@ID}_value" value="{$newWidth}"/>
        <xsl:for-each select="../column">
            <xsl:if test="not($thisId = @ID)">
                <input type="hidden" name="uP_sfattr" value="width"/>
                <input type="hidden" name="width_folderId" value="{@ID}"/>
                <input type="hidden" name="width_{@ID}_value" value="{@width}"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="normalizeColumnWidths">
        <form name="form_normalizeColumnWidths" method="post" action="{$baseActionURL}">
            <td>
                <input name="normalizeColumnWidths" type="image" src="{$mediaPathIcons}/columnnormalize.gif" width="28" height="25" border="0" alt="Reset column widths" title="Reset column widths"/>
                <input type="hidden" name="uP_sparam" value="mode"/>
                <input type="hidden" name="mode" value="{$mode}"/>
                <input type="hidden" name="uP_sparam" value="focusedTabID"/>
                <input type="hidden" name="focusedTabID" value="{$focusedTabID}"/>
                <input type="hidden" name="uP_sparam" value="targetRestriction"/>
                <input type="hidden" name="targetRestriction" value="no targetRestriction parameter"/>
                <xsl:for-each select="../column">
                    <xsl:choose>
                        <xsl:when test="not(round(100 mod number((count(../column))))=0) and position()=1">
                            <input type="hidden" name="uP_sfattr" value="width"/>
                            <input type="hidden" name="width_folderId" value="{@ID}"/>
                            <input type="hidden" name="width_{@ID}_value" value="{round(100 div number((count(../column))))+1}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <input type="hidden" name="uP_sfattr" value="width"/>
                            <input type="hidden" name="width_folderId" value="{@ID}"/>
                            <input type="hidden" name="width_{@ID}_value" value="{round(100 div number((count(../column))))}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </td>
        </form>
    </xsl:template>
    <!-- - -->
    <!-- - Checkthe immutabe and unremovable attributes and draw the icons accordingly -->
    <!-- - -->
    <xsl:template name="tabRestrictions">
        <xsl:choose>
            <!-- Check if the node is immutable before allowing rename the tab -->
            <xsl:when test="@immutable='false'">
                <input name="uP_target_name" type="text" class="uportal-input-text" value="{@name}">
                    <xsl:attribute name="size">
                        <xsl:value-of select="string-length(@name)"/>
                    </xsl:attribute>
                </input>
                <input type="hidden" name="uP_rename_target" value="{@ID}"/>
                <input type="hidden" name="uP_sparam" value="mode"/>
                <input type="hidden" name="mode" value="{$mode}"/>
                <input type="hidden" name="uP_sparam" value="focusedTabID"/>
                <input type="hidden" name="focusedTabID" value="{$focusedTabID}"/>
                <input name="renameTab" type="image" src="{$mediaPathIcons}/submit.gif" width="22" height="18" border="0" alt="Submit new tab name" title="Submit new tab name"/>
            </xsl:when>
            <xsl:otherwise>
                <span class="uportal-navigation-category-selected">
                    <xsl:value-of select="@name"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Check if the parent node is immutable before allowing move -->
        <xsl:choose>
            <xsl:when test="count(/layout/navigation/*) = 1">
                <img alt="Move tab is disabled - no place to move" title="Move tab is disabled - no place to move" src="{$mediaPathIcons}/moveicondisabled.gif" width="22" height="18" border="0"/>
            </xsl:when>
            <xsl:when test="/layout/@immutable='false'">
                <img src="{$mediaPathSkin}/transparent.gif" width="8" height="8"/>
                <a href="{$baseActionURL}?uP_request_move_targets={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={@ID}&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab&amp;uP_sparam=targetAction&amp;targetAction=Tab Move">
                    <img alt="Move this tab" title="Move this tab" src="{$mediaPathIcons}/moveicon.gif" width="22" height="18" border="0"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <img alt="Move tab is disabled" title="Move tab is disabled" src="{$mediaPathIcons}/moveicondisabled.gif" width="22" height="18" border="0"/>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Language Preferences Removed until Later version of uPortal -->
        <!-- <a href="#"> -->
        <!-- <img alt="Change language for this tab" title="Change language for this tab" src="{$mediaPathIcons}/languagesG.gif" width="22" height="18" border="0"/> -->
        <!-- </a> -->
        <!-- Check if the node is unremovable before allowing delete -->
        <xsl:choose>
            <!--  temporary fix for tab only fragments -->
            <xsl:when test=" $current_structure = 'fragment' ">
                <img alt="Remove tab is disabled - use fragment manager" title="Remove tab is disabled - use fragment manager" src="{$mediaPathIcons}/canicondisabled.gif" width="22" height="18" border="0"/>
            </xsl:when>
            <xsl:when test="/layout/@immutable='false' and @unremovable='false'">
                <a href="{$baseActionURL}?uP_remove_target={@ID}&amp;uP_sparam=mode&amp;mode={$mode}" onClick="return confirm('Are you sure you want to remove this tab?')">
                    <img alt="Remove this tab" title="Remove this tab" src="{$mediaPathIcons}/canicon.gif" width="22" height="18" border="0"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <img alt="Remove tab is disabled" title="Remove tab is disabled" src="{$mediaPathIcons}/canicondisabled.gif" width="22" height="18" border="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- - -->
    <!-- - Checkthe immutabe and unremovable attributes and draw the icons accordingly -->
    <!-- - -->
    <xsl:template name="columnRestrictions">
        <xsl:choose>
            <xsl:when test="$selectedID=@ID and $targetAction='Column Move'">
                <td>
                    <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
                        <img src="{$mediaPathIcons}/columncancelmove.gif" width="28" height="25" alt="cancel move action" title="cancel move action" border="0"/>
                    </a>
                </td>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="/layout/navigation/focusedTab/@immutable='false'">
                        <td>
                            <a href="{$baseActionURL}?uP_request_move_targets={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={@ID}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=column&amp;uP_sparam=targetAction&amp;targetAction=Column Move">
                                <img src="{$mediaPathIcons}/columnmove.gif" width="28" height="25" alt="Move this column" title="Move this column" border="0"/>
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td>
                            <img src="{$mediaPathIcons}/columnmovedisabled.gif" width="28" height="25" alt="Move column is disabled" title="Move column is disabled" border="0"/>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Language Button Removed until later version of uPortal
			<td>
			<a href="#">
			<img src="{$mediaPathIcons}/columnlanguages.gif" width="28" height="25" alt="Change language for this column" title="Change language for this column" border="0"/>
			</a>
			</td> -->
        <xsl:choose>
            <xsl:when test="@unremovable='false'">
                <td>
                    <a href="{$baseActionURL}?uP_remove_target={@ID}&amp;uP_sparam=mode&amp;mode={$mode}" onClick="return confirm('Are you sure you want to remove this column?')">
                        <img src="{$mediaPathIcons}/columncan.gif" width="28" height="25" alt="Delete this column" title="Delete this column" border="0"/>
                    </a>
                </td>
            </xsl:when>
            <xsl:otherwise>
                <td>
                    <img src="{$mediaPathIcons}/columncandisabled.gif" width="28" height="25" alt="Delete column is disabled" title="Delete column is disabled" border="0"/>
                </td>
            </xsl:otherwise>
        </xsl:choose>
        <!-- The conditional below turns off any column width adjustments on immutable tabs -->
        <xsl:if test="/layout/navigation/focusedTab/@immutable='false'">
            <xsl:if test="count(../column)&gt;1">
                <xsl:if test="number(@width)&gt;5">
                    <form name="formResizeColumn" method="post" action="{$baseActionURL}">
                        <td>
                            <xsl:call-template name="calculateShrinkColumn"/>
                        </td>
                    </form>
                </xsl:if>
                <xsl:if test="number(@width)&lt;95">
                    <form name="formResizeColumn" method="post" action="{$baseActionURL}">
                        <td>
                            <xsl:call-template name="calculateExpandColumn"/>
                        </td>
                    </form>
                </xsl:if>
                <xsl:call-template name="normalizeColumnWidths"/>
                <td valign="middle" nowrap="nowrap">
                    <img src="{$mediaPathSkin}/transparent.gif" width="5" height="1"/>
                    <span class="uportal-channel-strong">
                        <xsl:value-of select="@width"/>% </span>
                </td>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Remove -->
    <!-- - -->
    <xsl:template name="channel.action.remove">
        <a href="{$baseActionURL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')">
            <img alt="remove" title="remove" src="{$mediaPathIcons}/contentcan.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Cancel Move Targets -->
    <!-- - -->
    <xsl:template name="channel.action.cancelMove">
        <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">
            <img src="{$mediaPathIcons}/contentcancelmove.gif" width="26" height="23" alt="Cancel Channel Move" title="Cancel Channel Move" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Request Move Targets -->
    <!-- - -->
    <xsl:template name="channel.action.requestMoveTargets">
        <a href="{$baseActionURL}?uP_request_move_targets={@ID}&amp;uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=selectedID&amp;selectedID={@ID}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=channel&amp;uP_sparam=targetAction&amp;targetAction=Channel Move">
            <img src="{$mediaPathIcons}/contentmove.gif" width="26" height="23" alt="Move this channel" title="Move this channel" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Set Language on Node -->
    <!-- - -->
    <xsl:template name="channel.action.language">
        <a href="#">
            <img src="{$mediaPathIcons}/contentlanguages.gif" width="26" height="23" alt="Change language for this channel" title="Change language for this channel" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Move not allowed -->
    <!-- - -->
    <xsl:template name="channel.action.moveLocked">
        <img src="{$mediaPathIcons}/contentmovedisabled.gif" width="26" height="23" alt="The channel cannot be moved" title="The channel cannot be moved" border="0"/>
    </xsl:template>
    <!-- - -->
    <!-- - Channel Remove not allowed -->
    <!-- - -->
    <xsl:template name="channel.action.removeLocked">
        <img src="{$mediaPathIcons}/contentcandisabled.gif" width="26" height="23" alt="The channel cannot be removed" title="The channel cannot be removed" border="0"/>
    </xsl:template>
    <!-- - -->
    <!-- - Channel about -->
    <!-- - -->
    <xsl:template name="channel.action.about">
        <a href="{$baseActionURL}?uP_about_target={@ID}#{@ID}">
            <img alt="about" title="about" src="{$mediaPathIcons}/contentabout.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel return to root=root -->
    <!-- - -->
    <xsl:template name="channel.action.root">
        <a href="{$baseActionURL}?uP_root=root">
            <img alt="return" title="return" src="{$mediaPathIcons}/return.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel help  -->
    <!-- - -->
    <xsl:template name="channel.action.help">
        <a href="{$baseActionURL}?uP_help_target={@ID}#{@ID}">
            <img alt="help" title="help" src="{$mediaPathIcons}/contenthelp.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel edit -->
    <!-- - -->
    <xsl:template name="channel.action.edit">
        <a href="{$baseActionURL}?uP_edit_target={@ID}#{@ID}">
            <img alt="edit" title="edit" src="{$mediaPathIcons}/contentedit.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel print -->
    <!-- - -->
    <xsl:template name="channel.action.print">
        <a href="{$baseActionURL}?uP_print_target={@ID}#{@ID}">
            <img alt="print" title="print" src="{$mediaPathIcons}/contentprint.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel focus -->
    <!-- - -->
    <xsl:template name="channel.action.focus">
        <a href="{$baseActionURL}?uP_root={@ID}">
            <img alt="focus" title="focus" src="{$mediaPathIcons}/contentfocus.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel maximize  -->
    <!-- - -->
    <xsl:template name="channel.action.maximize">
        <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false#{@ID}">
            <img alt="maximize" title="maximize" src="{$mediaPathIcons}/contentmaximize.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel minimize -->
    <!-- - -->
    <xsl:template name="channel.action.minimize">
        <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true#{@ID}">
            <img alt="minimize" title="minimize" src="{$mediaPathIcons}/contentminimize.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Channel detach  -->
    <!-- - -->
    <xsl:template name="channel.action.detach">
        <a href="#" onClick="openBrWindow('{$baseActionURL}?uP_detach_target={@ID}','detachedChannel','toolbar=yes,location=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width=640,height=480')">
            <img alt="detach" title="detach" src="{$mediaPathIcons}/contentdetach.gif" width="26" height="23" border="0"/>
        </a>
    </xsl:template>
    <!-- - -->
    <!-- - Check the attributes and parameters and draw the icons accordingly -->
    <!-- - -->
    <xsl:template name="channelRestrictions">
        <xsl:choose>
            <xsl:when test="$mode='preferences' and $userLayoutRoot='root'">
                <!-- Conditionals for Preferences Mode -->
                <xsl:choose>
                    <xsl:when test="$selectedID=@ID and $targetAction='Channel Move'">
                        <xsl:call-template name="channel.action.cancelMove"/>
                    </xsl:when>
                    <xsl:when test="not(../@immutable='false')">
                        <xsl:call-template name="channel.action.moveLocked"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="channel.action.requestMoveTargets"/>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- Language preferences removed until later version of uPortal -->
                <xsl:choose>
                    <xsl:when test="not(../@immutable='false') or not(../@unremovable='false')">
                        <xsl:call-template name="channel.action.removeLocked"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="channel.action.remove"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="not($userLayoutRoot='root')">
                <!-- <xsl:call-template name="focusedControls"/> -->
                <xsl:call-template name="channel.action.root"/>
                <xsl:if test="not(@hasHelp='false')">
                    <xsl:call-template name="channel.action.help"/>
                </xsl:if>
                <xsl:if test="not(@hasAbout='false')">
                    <xsl:call-template name="channel.action.about"/>
                </xsl:if>
                <xsl:if test="not(@editable='false')">
                    <xsl:call-template name="channel.action.edit"/>
                </xsl:if>
                <xsl:if test="@printable='true'">
                    <xsl:call-template name="channel.action.print"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <!-- <xsl:call-template name="controls"/> -->
                <xsl:if test="not(@hasHelp='false')">
                    <xsl:call-template name="channel.action.help"/>
                </xsl:if>
                <xsl:if test="not(@hasAbout='false')">
                    <xsl:call-template name="channel.action.about"/>
                </xsl:if>
                <xsl:if test="not(@editable='false')">
                    <xsl:call-template name="channel.action.edit"/>
                </xsl:if>
                <xsl:if test="@printable='true'">
                    <xsl:call-template name="channel.action.print"/>
                </xsl:if>
                <xsl:call-template name="channel.action.focus"/>
                <xsl:choose>
                    <xsl:when test="@minimized='true'">
                        <xsl:call-template name="channel.action.maximize"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="channel.action.minimize"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="channel.action.detach"/>
                <xsl:choose>
                    <xsl:when test="not(../@immutable='false') or not(../@unremovable='false')">
                        <xsl:call-template name="channel.action.removeLocked"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="channel.action.remove"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="detachedChannelControls">
        <xsl:if test="not(@hasHelp='false')">
            <xsl:call-template name="channel.action.help"/>
        </xsl:if>
        <xsl:if test="not(@hasAbout='false')">
            <xsl:call-template name="channel.action.about"/>
        </xsl:if>
        <xsl:if test="not(@editable='false')">
            <xsl:call-template name="channel.action.edit"/>
        </xsl:if>
        <xsl:if test="@printable='true'">
            <xsl:call-template name="channel.action.print"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="actionList">
        <xsl:choose>
            <xsl:when test=" $current_structure = 'fragment' ">
                <span class="uportal-label">
                    <xsl:if test="not($targetRestriction='no targetRestriction parameter')">
                        <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Cancel <xsl:value-of select="$targetAction"/>
                        </a>
                        <span> |<xsl:text>&#160;</xsl:text>
                        </span>
                    </xsl:if>
                    <!--                     <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Cancel <xsl:value-of select="$targetAction"/>
                    </a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span> -->
                    <!-- <a href="{$baseActionURL}?uP_sparam=mode&amp;mode=view&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Return to Fragment Manager</a>
            <span> |<xsl:text>&#160;</xsl:text>
            </span> -->
                    <!-- <a href="{$baseActionURL}?uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab&amp;uP_sparam=targetAction&amp;targetAction=New Tab">New Tab</a>
            <span> |<xsl:text>&#160;</xsl:text>
            </span> -->
                    <a href="{$baseActionURL}?uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetRestriction&amp;targetRestriction=column&amp;uP_sparam=targetAction&amp;targetAction=New Column">New Column</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_fname=contentsubscriber&amp;uPcCS_action=init&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Add Content</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <!-- <a href="{$baseActionURL}?uP_fname=skinselector&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Skins</a>
            <span> |<xsl:text>&#160;</xsl:text>
            </span> -->
                    <!-- <a href="{$baseActionURL}?uP_fname=user-locales-selector&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Languages</a>
            <xsl:if test="$authorizedFragmentPublisher='true'">
                <span> |<xsl:text>&#160;</xsl:text>
                </span> -->
                    <a href="{$baseActionURL}?uP_sparam=uP_save&amp;uP_save=all">Save Fragment Layout </a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_fname=fragment-manager&amp;uPcFM_action=default&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true&amp;uP_sparam=lastSessionTabID&amp;lastSessionTabID={$focusedTabID}">Return to Fragment Manager</a>
                    <!-- </xsl:if> -->
                </span>
            </xsl:when>
            <xsl:otherwise>
                <span class="uportal-label">
                    <xsl:if test="not($targetRestriction='no targetRestriction parameter')">
                        <a href="{$baseActionURL}?uP_sparam=mode&amp;mode={$mode}&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Cancel <xsl:value-of select="$targetAction"/>
                        </a>
                        <span> |<xsl:text>&#160;</xsl:text>
                        </span>
                    </xsl:if>
                    <a href="{$baseActionURL}?uP_sparam=mode&amp;mode=view&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Turn Preferences Off</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=focusedTabID&amp;focusedTabID={$focusedTabID}&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab&amp;uP_sparam=targetAction&amp;targetAction=New Tab">New Tab</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetRestriction&amp;targetRestriction=column&amp;uP_sparam=targetAction&amp;targetAction=New Column">New Column</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_fname=contentsubscriber&amp;uPcCS_action=init&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Add Content</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_fname=skinselector&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Skins</a>
                    <span> |<xsl:text>&#160;</xsl:text>
                    </span>
                    <a href="{$baseActionURL}?uP_fname=user-locales-selector&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true">Languages</a>
                    <xsl:if test="$authorizedFragmentPublisher='true'">
                        <span> |<xsl:text>&#160;</xsl:text>
                        </span>
                        <a href="{$baseActionURL}?uP_fname=fragment-manager&amp;uPcFM_action=default&amp;uP_sparam=targetRestriction&amp;targetRestriction=no targetRestriction parameter&amp;uP_sparam=targetAction&amp;targetAction=no targetAction parameter&amp;uP_sparam=selectedID&amp;selectedID=&amp;uP_cancel_targets=true&amp;uP_sparam=lastSessionTabID&amp;lastSessionTabID={$focusedTabID}">Fragments</a>
                    </xsl:if>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
