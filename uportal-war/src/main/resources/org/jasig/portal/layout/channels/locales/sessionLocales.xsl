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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="localesParam">uP_locales</xsl:param>

  <xsl:template match="/">
      <xsl:apply-templates select="locales"/>
  </xsl:template>

  <xsl:template match="locales">
    <form action="{$baseActionURL}" method="post">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr class="uportal-background-dark">
        <td align="right" nowrap="nowrap" class="uportal-background-dark" colspan="1" rowspan="1">
          <select class="uportal-button" name="{$localesParam}">
              <xsl:apply-templates select="locale"/>
          </select>
          <input type="submit" value="Go" name="Go" class="uportal-button"/>
        </td>
      </tr>
    </table>
    </form>
  </xsl:template>

  <xsl:template match="locale">
      <option value="{@code}">
          <xsl:if test="@selected='true'">
              <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="@displayName"/>
      </option>
  </xsl:template> 

</xsl:stylesheet>

