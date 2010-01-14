<?xml version="1.0"?>
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

