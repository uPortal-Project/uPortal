<?xml version='1.0' encoding='utf-8' ?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT/CCssViewer</xsl:variable>
  <xsl:variable name="genericMediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>
  <xsl:param name="locale">de_DE</xsl:param>
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="cssViewer">
    <table width="100%" border="0" cellpadding="4" class="uportal-background-content">
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Elemente</td>
        <td class="uportal-channel-table-header">Beispiel oder Beschreibung</td>
      </tr>
      <xsl:apply-templates select="elements/member"/>
      <tr class="uportal-background-light" align="left" valign="top">
        <td class="uportal-channel-table-header">Klassen</td>
        <td class="uportal-channel-table-header">Beispiel oder Beschreibung</td>
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
            <input type="submit" name="Submit" value="Submit">
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
            <img alt="" src="{$genericMediaPath}/transparent.gif" width="1" height="1"/>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
