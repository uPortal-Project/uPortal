<?xml version="1.0" encoding="utf-8"?>
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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rss09="http://my.netscape.com/rdf/simple/0.9/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/" exclude-result-prefixes="rss09">

  <xsl:output method="html" indent="yes"/>

  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>

  <xsl:param name="locale">sv_SE</xsl:param>

  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>

  <xsl:template match="/rdf:RDF">
    <xsl:apply-templates select="rss09:channel"/>
  </xsl:template>

  <xsl:template match="rss09:channel">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr align="left">
        <td width="100%" valign="bottom" class="uportal-channel-subtitle">
          <xsl:value-of select="rss09:description"/>
        </td>

        <td>
          <a href="{../rss09:image/rss09:link}" target="_blank">
            <img alt="{../rss09:image/rss09:title}" src="{../rss09:image/rss09:url}" border="0"/>
          </a>
        </td>
      </tr>
    </table>

    <br/>

    <xsl:apply-templates select="../rss09:item"/>

    <br/>

    <xsl:apply-templates select="../rss09:textinput"/>
  </xsl:template>

  <xsl:template match="rss09:item">
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td>
          <img alt="bullet item" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
        </td>

        <td width="100%" class="uportal-channel-subtitle-reversed">
          <a href="{rss09:link}" target="_blank">
            <xsl:value-of select="rss09:title"/>
          </a>
        </td>
      </tr>

      <xsl:if test="rss09:description != ''">
        <tr class="uportal-channel-text">
          <td> </td>

          <td width="100%">
            <xsl:value-of select="rss09:description"/>
          </td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>

  <xsl:template match="rss09:textinput">
    <form action="{rss09:link}">
      <span class="uportal-label">
        <xsl:value-of select="rss09:description"/>
      </span>

      <br/>

      <input type="text" name="{rss09:name}" size="30" class="uportal-input-text"/>

      <br/>

      <input type="submit" name="Submit" value="Skicka" class="uportal-button"/>
    </form>
  </xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
