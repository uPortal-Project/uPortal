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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/error/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <img src="{$baseMediaURL}wrenchworks.gif" width="112" height="119"/>
        </td>
        <td>
          <img src="{$baseMediaURL}transparent.gif" width="16" height="16"/>
        </td>
        <td class="uportal-channel-subtitle" width="100%">Kļūda:<br/><span class="uportal-channel-error">
              <xsl:choose>
                <xsl:when test="@code='4'">Kanālam iestājies taimauts</xsl:when>
              <xsl:when test="@code='1'">Kanāls nespēj parādīt datus</xsl:when>
              <xsl:when test="@code='2'">Kanāls nespēj sākt darboties</xsl:when>
              <xsl:when test="@code='3'">Kanāls nespēj pieņemt datus</xsl:when>
              <xsl:when test="@code='0'">Kanālā ir vispārēja kļūda</xsl:when>
              <xsl:when test="@code='5'">Kanāls nespēj pieņemt PSP (Personālie sakaru pakalpojumi)</xsl:when>
              <xsl:when test="@code='6'">Jums nav piekļuves tiesību šim kanālam</xsl:when>
              <xsl:when test="@code='7'">Kanāls nav pieejams</xsl:when>
              <xsl:when test="@code='-1'">Kanālā ir vispārējā uPortal kļūda</xsl:when></xsl:choose></span>
          <br/>
          <br/>

          <xsl:if test="$allowRefresh='true'">
            <a href="{$baseActionURL}?action=retry">
              <img src="{$baseMediaURL}error_refresh.gif" border="0" width="16" height="16" alt="Refresh the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">Atjaunot kanālu</span>
            </a>

            <br/>
          </xsl:if>

          <xsl:if test="$allowReinstantiation='true'">
            <a href="{$baseActionURL}?action=restart">
              <img src="{$baseMediaURL}error_reboot.gif" border="0" width="16" height="16" alt="Reboot the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">Pārstartēt kanālu</span>
            </a>
          </xsl:if>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
