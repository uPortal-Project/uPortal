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
Version $Revision$
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT/CCssViewer</xsl:variable>
  <xsl:variable name="genericMediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="cssViewer">
    <table width="100%" border="0" cellpadding="4" class="uportal-background-content">
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Elementi</td>
        <td class="uportal-channel-table-header">Piemērs vai apraksts</td>
      </tr>
      <xsl:apply-templates select="elements/member"/>
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Klases</td>
        <td class="uportal-channel-table-header">Piemērs vai apraksts</td>
      </tr>
      <xsl:apply-templates select="classes/member"/>
    </table>
  </xsl:template>
  <xsl:template match="elements/member">
    <xsl:choose>
      <xsl:when test="demoUsing = 'description'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="element"/>
          </td>
          <td class="uportal-channel-text" width="100%">
            <xsl:value-of select="description"/>
          </td>
        </tr>
      </xsl:when>
      <xsl:when test="demoUsing = 'anchor'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="element"/>
          </td>
          <td>
            <a href="#">
              <xsl:attribute name="class">
                <xsl:value-of select="substring-after(element,'A.')"/>
              </xsl:attribute>
              <xsl:value-of select="//pangram"/>
            </a>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:output indent="yes" method="html"/>
  <xsl:template match="classes/member">
    <xsl:choose>
      <xsl:when test="demoUsing='textBlock'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="class"/>
          </td>
          <td>
            <xsl:attribute name="class">
              <xsl:value-of select="substring-after(class,'.')"/>
            </xsl:attribute>
            <xsl:value-of select="//pangram"/>
          </td>
        </tr>
      </xsl:when>
      <xsl:when test="demoUsing='button'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="class"/>
          </td>
          <td class="uportal-text">
            <input type="submit" name="Submit" value="Iesniegt">
              <xsl:attribute name="class">
                <xsl:value-of select="substring-after(class,'.')"/>
              </xsl:attribute>
            </input>
          </td>
        </tr>
      </xsl:when>
      <xsl:when test="demoUsing='inputText'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="class"/>
          </td>
          <td class="uportal-text">
            <input type="text" name="textfield">
              <xsl:attribute name="class">
                <xsl:value-of select="substring-after(class,'.')"/>
              </xsl:attribute>
            </input>
          </td>
        </tr>
      </xsl:when>
      <xsl:when test="demoUsing='tableCell'">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="class"/>
          </td>
          <td>
            <xsl:attribute name="class">
              <xsl:value-of select="substring-after(class,'.')"/>
            </xsl:attribute>
            <img alt="interface image" src="{$genericMediaPath}/transparent.gif" width="1" height="1"/>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
