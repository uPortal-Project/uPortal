<?xml version='1.0'?>
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

<xsl:param name="baseActionURL">Noklusējums</xsl:param>
<xsl:param name="locale">lv_LV</xsl:param>
<xsl:variable name="imageDir" select="'media/org/jasig/portal/channels/CUserPreferences'"/>

<xsl:template match="/">
  <table border="0" cellspacing="0" cellpadding="5" align="center">
    <tr><td>Lai pārvietotu izvēlētos elementus, izvēlēties mapi un tad nospiest "Pārvietot".</td></tr>
    <tr><td align="center">
      <table><tr><td>
        <form action="{$baseActionURL}" method="post">
      	  <input type="hidden" name="action" value="moveTo"/>
      	  <p align="center"><input type="submit" name="move" value="Pārvietot"/></p>
      	    <input type="radio" name="destination" value="top"/>
            <img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
            Mani kanāli<br/>      
            <xsl:apply-templates select="layout"/>      
      	  <p align="center"><input type="submit" name="move" value="Pārvietot"/></p>
        </form>
      </td></tr></table>
    </td></tr>
  </table>
</xsl:template>

<xsl:template match="folder">
  <!-- Indent according to position in hierarchy-->
  <xsl:for-each select="ancestor::*">
    <img src="{$imageDir}/transparent1x1.gif" width="20" height="1"/>
  </xsl:for-each>

  <input type="radio" name="destination" value="{@ID}"/>
  <img src="{$imageDir}/folder_closed.gif" border="0" width="13" height="10"/>
  <xsl:value-of select="@name"/><br/>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
