<?xml version="1.0" encoding="UTF-8"?>
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

Author: 
Jon Allen, jfa@immagic.com
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL" select="'render.userLayoutRootNode.uP'"/>
  <xsl:param name="locale" select="'en_US'"/>
  <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CContentSubscriber'"/>
  <xsl:param name="channelState" select="'browse'"/>
  <!--~-->
  <!-- parameters for content search -->
  <!--~-->
  <xsl:param name="searchChannels" select="'yes'"/>
  <xsl:param name="searchFragments" select="'yes'"/>
  <xsl:param name="searchNames" select="'yes'"/>
  <xsl:param name="searchDescriptions" select="'yes'"/>
  <xsl:param name="searchContains" select="'contains'"/>
  <xsl:param name="searchTerms" select="''"/>
  <!--~-->
  <!-- end of xsl parameter declarations -->
  <!--~-->
  <xsl:template match="/">
    <table cellspacing="0" cellpadding="5" width="100%" border="0">
      <tr class="uportal-background-content">
        <td align="left" valign="top">
          <xsl:call-template name="tabLine"/>
        </td>
      </tr>
    </table>
  </xsl:template>
  <!--~-->
  <!-- tab line table template - draws the browse/search buttons. -->
  <!--~-->
  <xsl:template name="tabLine">
    <xsl:choose>
      <xsl:when test="$channelState='search'">
        <!--~-->
        <!-- Begin - Search front tab. -->
        <!--~-->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
            <td>
              <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td colspan="4">
                    <img height="1" width="1" src="$mediaPath/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med" nowrap="nowrap">
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                    <a href="{$baseActionURL}?channelState=browse">
                      <span class="uportal-text-small">Browse</span>
                    </a>
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="10" class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
            <td>
              <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light" nowrap="nowrap">
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                    <span class="uportal-text-small">Search</span>
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="11" class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
            <td width="100%" valign="bottom">
              <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td width="100%" class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <xsl:call-template name="searchTable"/>
        <!--~-->
        <!-- End - Search front tab. -->
        <!--~-->
      </xsl:when>
      <xsl:otherwise>
        <!--~-->
        <!-- Begin - Browse front tab. -->
        <!--~-->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
            <td>
              <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light" nowrap="nowrap">
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                    <span class="uportal-text-small">Browse</span>
                    <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="10" class="uportal-background-light">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
            <td>
              <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="4">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="3">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td colspan="2">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med" nowrap="nowrap">
                    <a href="#">
                      <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                      <a href="{$baseActionURL}?channelState=search">
                        <span class="uportal-text-small">Search</span>
                      </a>
                      <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                    </a>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-semidark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                  <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
                <tr>
                  <td colspan="11" class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
            <td width="100%" valign="bottom">
              <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td width="100%" class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <!--~-->
        <!-- End - Browse front tab. -->
        <!--~-->
        <xsl:apply-templates select="registry"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--~-->
  <!-- End - tab line table template. -->
  <!--~-->
  <!--~-->
  <!-- Begin - search content table template. -->
  <!--~-->
  <xsl:template name="searchTable">
    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td class="uportal-background-med">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td width="100%">
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-shadow">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
      <tr>
        <td class="uportal-background-med">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-dark">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-dark">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-dark">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-dark">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
      <tr>
        <td class="uportal-background-med">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-dark">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <table class="uportal-background-content" cellpadding="2" cellspacing="0" border="0" width="100%">
            <tr>
              <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                <span class="uportal-channel-table-header">Search Content <img src="{$mediaPath}/transparent.gif" width="16" height="1" alt="" title=""/>
                </span>
              </td>
              <td width="100%" align="right" valign="bottom" nowrap="nowrap" class="uportal-text-small">
                <img src="{$mediaPath}/transparent.gif" width="1" height="1" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                  <tr>
                    <td>
                      <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
          <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
            <tr class="uportal-channel-text">
              <td>
                <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt=""/>
              </td>
              <td align="left" valign="top">
                <span class="uportal-label">Search for</span>:<br/>
                <table border="0" cellpadding="0" cellspacing="0">
                  <tr>
                    <td>
                      <input name="channels" type="checkbox" value="channels" checked="checked"/>
                    </td>
                    <td>
                      <span class="uportal-text-small">Channels</span>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <input name="fragments" type="checkbox" value="fragments" checked="checked"/>
                    </td>
                    <td class="uportal-text-small">
                      <span>Fragments</span>
                    </td>
                  </tr>
                </table>
              </td>
              <td>
                <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td align="left" valign="top">
                <span class="uportal-label">whos</span>
                <br/>
                <table border="0" cellpadding="0" cellspacing="0">
                  <tr>
                    <td>
                      <input name="channels2" type="checkbox" value="channels" checked="checked"/>
                    </td>
                    <td>
                      <span class="uportal-text-small">Name</span>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <input name="fragments2" type="checkbox" value="fragments" checked="checked"/>
                    </td>
                    <td class="uportal-text-small">
                      <span>Description</span>
                    </td>
                  </tr>
                </table>
              </td>
              <td>
                <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td align="center" valign="top">
                <br/>
                <select name="select5" class="uportal-input-text">
                  <option value="contains" selected="selected">contains</option>
                  <option value="is">is</option>
                  <option value="startsWith">starts with</option>
                  <option value="endsWith">ends with</option>
                </select>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td align="center" valign="top">
                <br/>
                <input name="textfield" type="text" class="uportal-input-text" value="search term(s)" size="25"/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td width="100%" align="left" valign="top">
                <br/>
                <input name="Submit" type="submit" class="uportal-button" value="Search"/>
              </td>
            </tr>
            <tr class="uportal-background-content" valign="top" align="left">
              <td colspan="10">
                <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                  <tr>
                    <td>
                      <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
        <td class="uportal-background-content">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-shadow">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
      <tr>
        <td class="uportal-background-med">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-content">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-content">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-content">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-shadow">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
      <tr>
        <td class="uportal-background-med">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td>
          <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
        <td class="uportal-background-shadow">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
      <tr>
        <td colspan="7" class="uportal-background-shadow">
          <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
        </td>
      </tr>
    </table>
  </xsl:template>
  <!--~-->
  <!-- End - search content table template. -->
  <!--~-->
  <!--~-->
  <!-- Begin browse content framework - draws the content subscriber frame.  includes the form and cancel button. -->
  <!--~-->
  <xsl:template match="registry">
    <table cellspacing="0" cellpadding="0" width="100%" border="0">
      <tr class="uportal-background-content">
        <td align="left" valign="top">
          <!--~-->
          <!-- Begin Outline Table Template.  Draws the padded border around the channel content.  Includes the "cancel" button. -->
          <!--~-->
          <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
              <td class="uportal-background-med">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td width="100%">
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-shadow">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td class="uportal-background-med">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-dark">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-dark">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-dark">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-dark">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td class="uportal-background-med">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-dark">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <table class="uportal-background-content" cellpadding="2" cellspacing="0" border="0" width="100%">
                  <tr>
                    <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                      <span class="uportal-channel-table-header">Select Category<img src="{$mediaPath}/transparent.gif" width="16" height="1" alt="" title=""/>
                      </span>
                    </td>
                    <td width="100%" align="right" valign="bottom" nowrap="nowrap" class="uportal-text-small">
                      <strong>
                        <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID=all">Expand</a>/<a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID=all">Condense</a> All Categories</strong>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2">
                      <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                          <td height="2">
                            <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                <xsl:apply-templates select="category"/>
                <xsl:apply-templates select="channel"/>
                <xsl:apply-templates select="fragments"/>
              </td>
              <td class="uportal-background-content">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-shadow">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td class="uportal-background-med">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-content">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-content">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-content">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-shadow">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td class="uportal-background-med">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td>
                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
              <td class="uportal-background-shadow">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
            <tr>
              <td colspan="7" class="uportal-background-shadow">
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>
  <!--~-->
  <!-- end of table set for browsed framework -->
  <!--~-->
  <!--~-->
  <!-- begin table for content item: chooses between expanded and contracted -->
  <!--~-->
  <xsl:template match="category">
    <xsl:choose>
      <xsl:when test="@view='expanded'">
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <strong>
                <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={@ID}">
                  <img src="{$mediaPath}/expanded.gif" width="16" height="16" border="0" alt="" title=""/>
                </a>
              </strong>
            </td>
            <xsl:variable name="indentWidth">
              <xsl:value-of select="count(ancestor::*)*10"/>
            </xsl:variable>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <strong>
                <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={@ID}">
                  <img src="{$mediaPath}/folder_open.gif" width="16" height="16" border="0" alt="" title=""/>
                  <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                  <xsl:value-of select="@name"/>
                </a>
              </strong>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <xsl:apply-templates select="category"/>
        <xsl:apply-templates select="channel"/>
      </xsl:when>
      <xsl:otherwise>
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <strong>
                <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={@ID}">
                  <img src="{$mediaPath}/collapsed.gif" width="16" height="16" border="0" alt="" title=""/>
                </a>
              </strong>
            </td>
            <xsl:variable name="indentWidth">
              <xsl:value-of select="count(ancestor::*)*10"/>
            </xsl:variable>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={@ID}">
                <strong>
                  <img src="{$mediaPath}/folder_closed.gif" width="16" height="16" border="0" alt="" title=""/>
                  <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                  <xsl:value-of select="@name"/>
                </strong>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--~-->
  <!-- end of table for closed category name -->
  <!--~-->
  <!--~-->
  <!-- begin table for closed content item: contains the content name and divider line -->
  <!--~-->
  <xsl:template match="channel">
    <xsl:choose>
      <xsl:when test="@view='expanded'">
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <xsl:variable name="indentWidth">
              <xsl:value-of select="count(ancestor::*)*10"/>
            </xsl:variable>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-text">
              <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_channelID={@ID}">
                <xsl:value-of select="@title"/>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <table cellpadding="5" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
          <!-- Form not needed until language selector is reintroduced
          <form name="subscribe_channel_form" action="{$baseActionURL}" method="post">
          -->
          <tr>
            <td width="100%">
              <table cellpadding="5" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Type:</td>
                  <td width="100%">Individual Channel</td>
                </tr>
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Description:</td>
                  <td width="100%">
                    <xsl:value-of select="@description"/>
                  </td>
                </tr>
                <!-- Language Selector temporarily removed until i18n is more inclusive
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td>Settings:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="0" cellpadding="2">
                      <tr>
                        <td class="uportal-label">Language:</td>
                      </tr>
                      <tr>
                        <td>
                          <select name="select">
                            <option value="defualtlang" selected="selected">Use my defualt language</option>
                            <option value="EnglishC">English (Canadian)</option>
                            <option value="EnglishUK">English (United Kingdom)</option>
                            <option value="EnglishUS">English (United States)</option>
                            <option value="French">French</option>
                            <option value="German">German</option>
                            <option value="Japanese">Japanese</option>
                            <option value="Russian">Russian</option>
                            <option value="Spanish">Spanish</option>
                            <option value="Swedish">Swedish</option>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                Language Selector temporarily removed until i18n is more inclusive  -->
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Actions:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="3" cellpadding="3">
                      <!-- Preview of Channel Held until Later Release
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="#" target="_blank">
                            <img src="{$mediaPath}/preview.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td>
                          <a href="#" target="_blank">Preview this channel in a new window</a>
                        </td>
                      </tr>  Preview of Channel Held until Later Release -->
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="{$baseActionURL}?uP_root=root&amp;channelPublishID={@chanID}&amp;uP_request_add_targets=channel&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Channel&amp;uP_sparam=targetRestriction&amp;targetRestriction=channel">
                            <img src="{$mediaPath}/addContent.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td width="100%">
                          <a href="{$baseActionURL}?uP_root=root&amp;channelPublishID={@chanID}&amp;uP_request_add_targets=channel&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Channel&amp;uP_sparam=targetRestriction&amp;targetRestriction=channel"> Subscribe to this channel</a>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
          <!-- Form not needed until language selector is reintroduced
          </form>
          -->
        </table>
        <table class="uportal-background-content" cellpadding="3" cellspacing="0" border="0" width="100%">
          <tr>
            <td>
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <xsl:variable name="indentWidth">
              <xsl:value-of select="count(ancestor::*)*10"/>
            </xsl:variable>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_channelID={@ID}">
                <img src="{$mediaPath}/file.gif" width="16" height="16" border="0" alt="" title=""/>
                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                <xsl:value-of select="@title"/>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--~-->
  <!-- end of table for content item -->
  <!--~-->
  <!--~-->
  <!-- begin table for fragment title -->
  <!--~-->
  <xsl:template match="fragments">
    <xsl:choose>
      <xsl:when test="category/@view='expanded'">
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/expanded.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="10" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <img src="{$mediaPath}/folder_open.gif" width="16" height="16" border="0" alt="" title=""/>
              <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={category/@ID}">
                <strong> Fragments </strong>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <xsl:apply-templates select="category/fragment"/>
      </xsl:when>
      <xsl:otherwise>
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/collapsed.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="10" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <img src="{$mediaPath}/folder_closed.gif" width="16" height="16" border="0" alt="" title=""/>
              <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
              <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={category/@ID}">
                <strong> Fragments </strong>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--~-->
  <!-- end table for fragment title -->
  <!--~-->
  <!--~-->
  <!-- begin table for fragment list -->
  <!--~-->
  <xsl:template match="fragment">
    <xsl:choose>
      <xsl:when test="@view='expanded'">
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="20" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-text">
              <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_fragmentID={@ID}">
                <xsl:value-of select="@title"/>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <table cellpadding="5" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
          <tr>
            <td>
              <table cellpadding="5" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Type:</td>
                  <td width="100%">Fragment</td>
                </tr>
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Description:</td>
                  <td width="100%">
                    <xsl:value-of select="./description"/>
                  </td>
                </tr>
                <!-- Language Selector temporarily removed until i18n is more inclusive 
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td>Settings:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="0" cellpadding="2">
                      <tr>
                        <td class="uportal-label">Language:</td>
                      </tr>
                      <tr>
                        <td>
                          <select name="select">
                            <option value="defualtlang" selected="selected">Use my defualt language</option>
                            <option value="EnglishC">English (Canadian)</option>
                            <option value="EnglishUK">English (United Kingdom)</option>
                            <option value="EnglishUS">English (United States)</option>
                            <option value="French">French</option>
                            <option value="German">German</option>
                            <option value="Japanese">Japanese</option>
                            <option value="Russian">Russian</option>
                            <option value="Spanish">Spanish</option>
                            <option value="Swedish">Swedish</option>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                Language Selector temporarily removed until i18n is more inclusive -->
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td nowrap="nowrap">Actions:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="3" cellpadding="3">
                      <!-- Preview of Channel Held until Later Release
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="#" target="_blank">
                            <img src="{$mediaPath}/preview.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td>
                          <a href="#" target="_blank">Preview this fragment in a new window</a>
                        </td>
                      </tr>  Preview of Channel Held until Later Release -->
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="{$baseActionURL}?uP_root=root&amp;fragmentRootID={rootNodeID}&amp;fragmentPublishID={@ID}&amp;uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Tab&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab">
                            <img src="{$mediaPath}/addContent.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td width="100%">
                          <a href="{$baseActionURL}?uP_root=root&amp;fragmentRootID={rootNodeID}&amp;fragmentPublishID={@ID}&amp;uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Tab&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab"> Subscribe to this fragment</a>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <table class="uportal-background-content" cellpadding="3" cellspacing="0" border="0" width="100%">
          <tr>
            <td>
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
          <tr class="uportal-channel-text" valign="top" align="left">
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
            </td>
            <td class="uportal-navigation-category">
              <img src="{$mediaPath}/transparent.gif" width="20" height="1" border="0" alt="" title=""/>
            </td>
            <td width="100%" valign="bottom" class="uportal-navigation-category">
              <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_fragmentID={@ID}">
                <img src="{$mediaPath}/file.gif" width="16" height="16" border="0" alt="" title=""/>
                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                <xsl:value-of select="./name"/>
              </a>
            </td>
          </tr>
          <tr class="uportal-background-content" valign="top" align="left">
            <td colspan="5">
              <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--~-->
  <!-- end table for fragment list -->
  <!--~-->
</xsl:stylesheet>
