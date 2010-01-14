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
  <xsl:output indent="yes"/>
  
  <xsl:template match="error">
    <div class="portlet-msg-error">
      <xsl:value-of select="."/>
    </div>
  </xsl:template>
  
  <xsl:template match="news">
    
    <div class="news-feed">
    	<div class="news-source">
        <xsl:apply-templates select="image"/>
        <p><xsl:value-of select="desc"/></p>
      </div>
      <div class="news-items">
        <xsl:apply-templates select="items"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="image">
    <a target="_blank" href="{link}">
      <img src="{url}" alt="{description}" class="news-feed-img"/>
    </a>
  </xsl:template>
  
  <xsl:template match="items">
    <ul>
      <xsl:apply-templates select="item"/>
    </ul>
  </xsl:template>
  
  <xsl:template match="item">
    <li>
      <a target="_blank" href="{link}" class="news-item-title"><xsl:value-of select="title"/></a>
      <span class="news-item-excerpt"><xsl:apply-templates select="description"/></span>
    </li>
  </xsl:template>
  
  <xsl:template match="description">
    <xsl:apply-templates select="@*|node()" mode="copy"/>
  </xsl:template>
  
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
