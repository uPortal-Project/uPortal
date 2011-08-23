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

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:output method="xml" indent="yes" media-type="text/html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>
  
  <xsl:param name="activeTabFName"></xsl:param>
  <xsl:param name="focusedPortletFName"></xsl:param>
  
  <xsl:variable name="activeTabIndex">
    <xsl:choose>
      <xsl:when test="$activeTabFName and /descendant::folder[@type='tab' and @fname=$activeTabFName][1]/preceding-sibling::folder[@type='tab']">
        <xsl:value-of select="count(/descendant::folder[@type='tab' and @fname=$activeTabFName][1]/preceding-sibling::folder[@type='tab'])+1"/>
      </xsl:when>
      <xsl:when test="$focusedPortletFName and /descendant::portlet[@fname=$focusedPortletFName and ancestor::folder[@type='tab']][1]/ancestor::folder[@type='tab']">
        <xsl:value-of select="count(/descendant::portlet[@fname=$focusedPortletFName and ancestor::folder[@type='tab']][1]/ancestor::folder[@type='tab']/preceding-sibling::folder[@type='tab'])+1"/>
      </xsl:when>
      <xsl:when test="not($focusedPortletFName)">1</xsl:when>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="layout">
    <xsl:for-each select="folder[@type='root']">
      <layout xmlns:dlm="http://www.uportal.org/layout/dlm">
        <xsl:comment> test </xsl:comment>
        <header>
          <xsl:copy-of select="folder[@type='header']/portlet"/>
        </header>
        
        <xsl:call-template name="tabList"/>
        
        <content activeTabIndex="{$activeTabIndex}">
          <xsl:choose>
            <xsl:when test="$focusedPortletFName">
              <focused>
                <xsl:attribute name="in-user-layout">
                  <xsl:choose>
                    <xsl:when test="/descendant::portlet[@fname=$focusedPortletFName and ancestor::folder[@type='tab']]">true</xsl:when>
                    <xsl:otherwise>false</xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
              </focused>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="folder[@type='tab']" />
            </xsl:otherwise>
          </xsl:choose>
          
        </content>
        
        <header>
          <xsl:copy-of select="folder[@type='footer']/portlet"/>
        </header>
      </layout>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="tabList">
    <navigation>
      <xsl:for-each select="folder[@type='tab']">
        <tab id="{@id}" fname="{@fname}">
          <xsl:if test="position() = $activeTabIndex">
            <xsl:attribute name="activeTab">true</xsl:attribute>
          </xsl:if>
          <xsl:copy-of select="name"/>
          
          <xsl:for-each select="./descendant::portlet">
            <tabPortlet id="{@id}" fname="{@fname}"/>
          </xsl:for-each>
        </tab>
      </xsl:for-each>
    </navigation>
  </xsl:template>
  
  <xsl:template match="folder[@type='tab']">
    <xsl:if test="position() = $activeTabIndex">
      <xsl:if test="child::folder">
        <xsl:for-each select="folder">
          <column id="{@id}">
            <xsl:apply-templates/>
          </column>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="child::portlet">
        <column>
          <xsl:apply-templates/>
        </column>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="portlet">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="parameter">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <xsl:template name="generateXPath">
    <xsl:for-each select="ancestor::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>[</xsl:text>
      <xsl:number/>
      <xsl:text>]</xsl:text>
    </xsl:for-each>
    <xsl:text>/</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>[</xsl:text>
    <xsl:number/>
    <xsl:text>]</xsl:text>
  </xsl:template> 
  
</xsl:stylesheet>
