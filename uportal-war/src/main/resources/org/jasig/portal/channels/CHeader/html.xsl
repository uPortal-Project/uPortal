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
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="authenticated">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:template match="header">
    <xsl:if test="$authenticated != 'false'">
      <a href="{$baseActionURL}?uP_root=root" class="uportal-navigation-category">Home </a> | 
      <a href="{$baseActionURL}?uP_fname=layout-sitemap" class="uportal-navigation-category"> Site Map </a> |
      <xsl:if test="chan-mgr-chanid">
      <a href="{$baseActionURL}?uP_fname={chan-mgr-chanid}" class="uportal-navigation-category"> Channel Admin </a> | 
      </xsl:if>
      <a href="{$baseActionURL}?uP_fname={preferences-chanid}" class="uportal-navigation-category"> Preferences </a> | <a href="Logout" class="uportal-navigation-category"> Logout </a>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
