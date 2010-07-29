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
  <xsl:output method="xml" indent="no"/>

  <xsl:param name="baseActionURL">baseActionURL not set</xsl:param>
    
  <xsl:template match="/">
      <xsl:apply-templates select="layout"/>
  </xsl:template>
  
  <xsl:template match="layout">
      <xsl:apply-templates select="folder" mode="root"/>
  </xsl:template>
  
  
  <xsl:template match="folder" mode="root">
    <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="group"/>
  </xsl:template>
  
  <xsl:template match="folder" mode="group">
    <xsl:variable name="POSITION" select="position()"/>
    <xsl:if test="($POSITION - 1) mod 4 = 0">
        <div class="fl-col-flex4">
          <xsl:apply-templates select="../folder[@type='regular' and @hidden='false' and position()&lt;$POSITION+4 and (position()=$POSITION or position()&gt;$POSITION)]" mode="tab">
            <xsl:with-param name="MAX_PREVIOUS_INDEX" select="$POSITION - 1"/>
          </xsl:apply-templates>
        </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="folder" mode="tab">
    <xsl:param name="MAX_PREVIOUS_INDEX">1</xsl:param>
    <div class="fl-col">
        <h2><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={$MAX_PREVIOUS_INDEX + position()}"><xsl:value-of select="@name"/></a></h2>
        <ul>
            <xsl:apply-templates select="folder" mode="column"/>
        </ul>
    </div>
  </xsl:template>
      
  <xsl:template match="folder" mode="column">
		<xsl:apply-templates select="channel"/>
  </xsl:template>  
  
  <xsl:template match="channel">
		<li><a href="{$baseActionURL}?uP_root={@ID}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>
    
</xsl:stylesheet>