<?xml version='1.0' encoding='utf-8' ?>
<!--
Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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
$Revision$
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="no"/>
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="unauthenticated">true</xsl:param>
    <xsl:param name="locale">en_US</xsl:param>
    <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CLogin'"/>
    <!-- ~ -->
    <!-- ~ Match on root element then check if the user is NOT authenticated-->
    <!-- ~ -->
    <xsl:template match="/">
        <xsl:if test="$unauthenticated='true'">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user is not authenticated insert login form-->
    <!-- ~ -->
    <xsl:template match="login-status">
        <form action="Login" method="post">
            <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <tr class="uportal-background-light">
                    <td width="100%" class="uportal-channel-text" nowrap="nowrap">
                        <input type="hidden" name="action" value="login"/>
                        <span class="uportal-label">Name:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/>
                            <input class="uportal-input-text" type="text" name="userName" size="15" value="{failure/@attemptedUserName}"/>
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1"/>Password:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/>
                            <input class="uportal-input-text" type="password" name="password" size="15"/>
                            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="8" height="1"/>
                            <input type="submit" value="Login" name="Login" class="uportal-button"/>
                        </span>
                    </td>
                </tr>
                <xsl:apply-templates/>
            </table>
        </form>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login fails present error message box-->
    <!-- ~ -->
    <xsl:template match="failure">
        <xsl:call-template name="message">
            <xsl:with-param name="messageString" select="'The user name/password combination entered is not recognized. Please try again.'"/>
        </xsl:call-template>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login encounters an error present error message box-->
    <!-- ~ -->
    <xsl:template match="error">
        <xsl:call-template name="message">
            <xsl:with-param name="messageString" select="'An error occured during authentication. The portal is unable to log you on at this time. Try again later.'"/>
        </xsl:call-template>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ error message box-->
    <!-- ~ -->
    <xsl:template name="message">
        <xsl:param name="messageString"/>
        <tr class="uportal-background-light">
            <td width="100%" class="uportal-channel-text" nowrap="nowrap">
                <table border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3" class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3" class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-dark">
                            <span class="uportal-channel-title">
                                <xsl:value-of select="$messageString"/>
                            </span>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2" class="uportal-background-dark">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-content">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="4">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="2">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3" class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3" class="uportal-background-med">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                        <td colspan="3">
                            <img src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
