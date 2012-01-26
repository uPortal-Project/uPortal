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

<xsl:stylesheet
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:upElemTitle="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanLayoutElementTitleHelper"
    xsi:schemaLocation="https://source.jasig.org/schemas/uportal/layout/portal-url ../../../../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="xsi url upMsg upElemTitle"
    version="1.0">
    
  <xsl:import href="../../../../../layout/theme/urlTemplates.xsl" />
  
  <xsl:output method="xml" />
  <xsl:strip-space elements="*"/>
  
  <xsl:param name="USER_LANG" />
  
  <!-- The following 2 parameters are for URL building. -->
  <xsl:param name="CURRENT_REQUEST" />
  <xsl:param name="XSLT_PORTAL_URL_PROVIDER" />
  
  <!-- Whether to use tab groups or not -->
  <xsl:param name="USE_TAB_GROUPS" select="false" />
  
  <!-- Maximum number of tabs in a row when no tab groups are used -->
  <xsl:param name="TAB_WRAP_COUNT" select="4" />

  <!-- Used to build the tabGroupsList:  discover tab groups, add each to the list ONLY ONCE -->
  <xsl:key name="tabGroupKey" match="layout/folder/folder[@hidden='false' and @type='regular']" use="@tabGroup"/>
    
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$USE_TAB_GROUPS"><xsl:call-template name="tabGroupLayout" /></xsl:when>
      <xsl:otherwise><xsl:call-template name="noTabGroupLayout" /></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="tabGroupLayout">
    <!-- Tab group layout:
     |Tabgroup1
     |   Tab1            Tab2          Tab3         (<== no limit to tab count)
     |     -portlet1     -portlet4     -portlet6
     |     -portlet2     -portlet5     -portlet7
     |     -portlet3                   -...
     |Tabgroup1
     |   Tab4            ...
     |     -portlet8
     |     -portlet9
     |     -...
     |....
     +-->
    <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']"><!-- These are standard tabs -->
      <!-- Process only the first tab in each Tab Group (avoid duplicates) -->
      <xsl:if test="self::node()[generate-id() = generate-id(key('tabGroupKey',@tabGroup)[1])]">
        <xsl:variable name="CURRENT_TAB_GROUP" select="@tabGroup" />
        <xsl:variable name="TABGROUP_LABEL">
          <xsl:choose>
            <xsl:when test="@name='DEFAULT_TABGROUP'"><xsl:value-of select="upMsg:getMessage('navigation.tabgroup.default', $USER_LANG)" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="upMsg:getMessage($CURRENT_TAB_GROUP, $USER_LANG)" /></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <div class="fl-container-flex">
          <h2><xsl:value-of select="$TABGROUP_LABEL" /></h2>
          <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false' and @tabGroup=$CURRENT_TAB_GROUP]">
            <xsl:apply-templates select="." mode="tab" />
          </xsl:for-each>
        </div>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="noTabGroupLayout">
    <!-- 
     | Tab layout:
     | Tab1          Tab2          Tab3          Tab4         (<== limited to $TAB_WRAP_COUNT)
     |   -portlet1     -portlet5     -portlet7     -portlet8
     |   -portlet2     -portlet6                   -portlet9
     |   -portlet3                                 -portlet10
     |   -portlet4
     |
     | Tab5 ....
     +-->

    <!-- Here we can't use muenchian grouping, because due to some bug key ignores "match" argument, hence calculations using position() are unusable -->
    <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
      <xsl:if test="(position() mod $TAB_WRAP_COUNT)=1">
        <xsl:variable name="ROW_NUM" select="ceiling(position() div $TAB_WRAP_COUNT)" />
        <div class="fl-container-flex fl-col-flex4">
          <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
            <xsl:if test="ceiling(position() div $TAB_WRAP_COUNT) = $ROW_NUM">
              <xsl:apply-templates select="." mode="tab" />
            </xsl:if>
          </xsl:for-each>
        </div>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="folder" mode="tab">
    <xsl:variable name="tabLinkUrl">
      <xsl:call-template name="portalUrl">
        <xsl:with-param name="url">
          <url:portal-url>
            <url:layoutId><xsl:value-of select="@ID" /></url:layoutId>
          </url:portal-url>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <div class="fl-col">
      <div><a href="{$tabLinkUrl}"><xsl:value-of select="upElemTitle:getTitle(@ID, $USER_LANG, @name)"/></a></div>
      <ul><xsl:apply-templates select="folder" mode="column" /></ul>
    </div>
  </xsl:template>
      
  <xsl:template match="folder" mode="column">
		<xsl:apply-templates select="channel"/>
  </xsl:template>

  <xsl:template match="channel">
    <xsl:variable name="portletLinkUrl">
      <xsl:call-template name="portalUrl">
        <xsl:with-param name="url">
          <url:portal-url>
            <url:layoutId><xsl:value-of select="@ID" /></url:layoutId>
            <url:portlet-url state="MAXIMIZED" />
          </url:portal-url>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <li><a href="{$portletLinkUrl}"><xsl:value-of select="@name" /></a></li>
  </xsl:template>
    
</xsl:stylesheet>