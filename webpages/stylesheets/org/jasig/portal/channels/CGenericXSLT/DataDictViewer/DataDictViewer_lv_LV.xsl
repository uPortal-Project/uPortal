<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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

Author: Debra Rundle, rundle@princeton.edu
Version $Revision$
-->
<xsl:param name="locale">lv_LV</xsl:param>

<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>

<xsl:template match="text()|@*"><xsl:value-of select="."/></xsl:template>

<xsl:template match="tables">
      <xsl:apply-templates select="table"/>
</xsl:template>

<xsl:template match="table">
    <xsl:choose>
      <xsl:when test="desc != 'Not used'">
      <table width="100%" border="1" cellpadding="4" class="uportal-background-content">
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Tabulas nosaukums</td>
        <td class="uportal-channel-table-header"> </td>
        <td class="uportal-channel-table-header"> </td>
        <td class="uportal-channel-table-header"> </td>
        <td class="uportal-channel-table-header">Apraksts</td>
      </tr>
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="name"/>
          </td>
          <td class="uportal-channel-text"> </td>
          <td class="uportal-channel-text"> </td>
          <td class="uportal-channel-text"> </td>
          <td class="uportal-channel-text" width="100%">
            <xsl:value-of select="desc"/>
          </td>
        </tr>
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Slejas nosaukums</td>
        <td class="uportal-channel-table-header">Atsl\u0113ga</td>
        <td class="uportal-channel-table-header">Tips</td>
        <td class="uportal-channel-table-header">Izm\u0113rs</td>
        <td class="uportal-channel-table-header">Apraksts</td>
      </tr>
      <xsl:apply-templates select="columns/column"/>
      </table>
      <br/><br/>
      </xsl:when>
    </xsl:choose>
 </xsl:template>

<xsl:template match="columns/column">
    <xsl:choose>
      <xsl:when test="desc != ''">
        <tr align="left" valign="top">
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="name"/>
          </td>
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="key"/>
          </td>
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="type"/>
          </td>
          <td class="uportal-channel-text" nowrap="nowrap">
            <xsl:value-of select="param"/>
          </td>
          <td class="uportal-channel-text" width="100%">
            <xsl:value-of select="desc"/>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
