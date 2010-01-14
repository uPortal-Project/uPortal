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
	<xsl:param name="startItem">1</xsl:param>

	<!-- You may want to configure this variable to show more items per screen -->
  <xsl:variable name="itemsPerScreen">1</xsl:variable>
  
	<xsl:param name="baseActionURL">actionURLnotPassed</xsl:param>

        <xsl:param name="locale">ja_JP</xsl:param>

  <xsl:template match="rss">
    <xsl:apply-templates select="channel"/>
    <p align="center">

      <!-- "Next" link -->
      <xsl:if test="count(channel/item) - $startItem &gt;= $itemsPerScreen">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?startItem=<xsl:value-of select="$startItem + $itemsPerScreen"/>
          </xsl:attribute>
          <xsl:call-template name="nav-text">
            <xsl:with-param name="label" select="string('次')"/>
          </xsl:call-template>
        </a>
      </xsl:if>

      <!-- "Prev" link -->
      <xsl:if test="$startItem - $itemsPerScreen &gt; 0">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$baseActionURL"/>?startItem=<xsl:value-of select="$startItem - $itemsPerScreen"/>
          </xsl:attribute>
          <xsl:call-template name="nav-text">
            <xsl:with-param name="label" select="string('前')"/>
          </xsl:call-template>
        </a>
      </xsl:if>
    </p>

  </xsl:template>

  <xsl:template match="channel">
    <p align="center"><big><xsl:value-of select="title"/></big></p>
    <xsl:apply-templates select="item[position() &gt;= $startItem and position() &lt; ($startItem + $itemsPerScreen)]"/>
  </xsl:template>

  <xsl:template match="item">
    <p align="center">---Item <xsl:number/> of <xsl:value-of select="count(/rss/channel/item)"/>---</p>
    <p align="left"><a href="{link}"><xsl:value-of select="title"/></a></p>
    <p align="left"><xsl:value-of select="description"/></p>
  </xsl:template>

  <xsl:template name="nav-text">
    <xsl:param name="label"/>
    <xsl:value-of select="$label"/><xsl:text> </xsl:text>
    <xsl:if test="$itemsPerScreen != 1"><xsl:value-of select="$itemsPerScreen"/></xsl:if>
    <xsl:text> </xsl:text>item<xsl:if test="$itemsPerScreen != 1">s</xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
