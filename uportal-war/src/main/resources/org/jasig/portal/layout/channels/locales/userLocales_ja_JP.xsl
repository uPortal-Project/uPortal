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

  <xsl:param name="baseActionURL">baseActionURL not set</xsl:param>
    
  <xsl:template match="/">
      [Translate to ja_JP!] Please indicate your language preference:<br/>
      <xsl:apply-templates select="locales"/>
  </xsl:template>
  
  <xsl:template match="locales">
      <form action="{$baseActionURL}" method="post">
          <xsl:apply-templates select="locale"/>
          <input type="submit" name="submit" value="Submit" class="uportal-button"/>
      </form>
  </xsl:template>
  
  <xsl:template match="locale">
      <input type="radio" name="locale" value="{@code}" class="uportal-button">
      <xsl:if test="@selected='true'">
          <xsl:attribute name="checked">checked</xsl:attribute>
      </xsl:if>
      </input>
      <xsl:value-of select="@displayName"/><br/>
  </xsl:template>  
    
</xsl:stylesheet>