<?xml version="1.0"?>
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
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="yes"/>

  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>

  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>

  <xsl:template match="/rss">
    <xsl:apply-templates select="channel"/>
  </xsl:template>

  <xsl:template match="channel">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr align="left">
        <td width="100%" valign="bottom" class="uportal-channel-subtitle">
          <xsl:value-of select="description" />
        </td>

        <td>
          <!-- Only display image if there is one -->
          <xsl:choose>
            <xsl:when test="image">
              <a href="{image/link}" target="_blank">
                <img alt="{image/title}: {image/description}" src="{image/url}" border="0"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <img alt="" src="{$mediaPath}/transparent.gif" width="1" height="1" border="0"/>
            </xsl:otherwise>
          </xsl:choose>          
        </td>
      </tr>
    </table>

    <br/>

    <xsl:apply-templates select="item"/>

    <br/>

    <xsl:apply-templates select="textinput"/>
  </xsl:template>

  <xsl:template match="item">
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td>
          <img alt="bullet point" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
        </td>

        <td width="100%" class="uportal-channel-subtitle-reversed">
          <a href="{link}" target="_blank">
            <xsl:value-of select="title"/>
          </a>
        </td>
      </tr>

      <xsl:if test="description != ''">
        <tr class="uportal-channel-text">
          <td> </td>

          <td width="100%">
            <xsl:value-of select="description"/>
          </td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>

  <xsl:template match="textinput">
    <form action="{link}">
      <span class="uportal-label">
        <xsl:value-of select="description"/>
      </span>

      <br/>

      <input type="text" name="{name}" size="30" class="uportal-input-text"/>

      <br/>

      <input type="submit" name="Submit" value="Submit" class="uportal-button"/>
    </form>
  </xsl:template>
</xsl:stylesheet>

