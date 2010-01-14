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
      <xsl:apply-templates select="layout"/>
  </xsl:template>
  
  <xsl:template match="layout">
      <xsl:apply-templates select="folder" mode="root"/>
  </xsl:template>
  
  
  <xsl:template match="folder" mode="root">
  	<xsl:variable name="POSITION" select="position()" />
  	<div class="fl-col-flex4">
    	<xsl:if test="$POSITION &lt; 5">
				<xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
      </xsl:if>
    </div>
    <xsl:if test="$POSITION &gt; 4">
      <div class="fl-col-flex4">
        <xsl:if test="$POSITION &lt; 9">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
        </xsl:if>
      </div>
    </xsl:if>
    <xsl:if test="$POSITION &gt; 8">
      <div class="fl-col-flex4">
        <xsl:if test="$POSITION &lt; 13">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
        </xsl:if>
      </div>
    </xsl:if>
    <xsl:if test="$POSITION &gt; 12">
      <div class="fl-col-flex4">
        <xsl:if test="$POSITION &lt; 17">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
        </xsl:if>
      </div>
    </xsl:if>
    <xsl:if test="$POSITION &gt; 16">
      <div class="fl-col-flex4">
        <xsl:if test="$POSITION &lt; 21">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
        </xsl:if>
      </div>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="folder" mode="tab">
      <div class="fl-col">
        <h2><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}"><xsl:value-of select="@name"/></a></h2>
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