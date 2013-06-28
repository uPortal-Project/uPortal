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

<!-- Revision: 2007-8-24 gthompson -->

<xsl:stylesheet version="1.0" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="userLayoutRoot">root</xsl:param>
<xsl:param name="focusedTabID">none</xsl:param>
<xsl:param name="defaultTab">1</xsl:param>
<xsl:param name="detached">false</xsl:param>
<xsl:param name="userImpersonating">false</xsl:param>

<!-- Used to build the tabGroupsList:  discover tab groups, add each to the list ONLY ONCE -->
<xsl:key name="tabGroupKey" match="layout/folder/folder[@hidden='false' and @type='regular']" use="@tabGroup"/>
<!-- Used to build the sidebarGroupsList:  discover sidebar groups, add each to the list ONLY ONCE -->
<xsl:key name="sidebarGroupKey" match="layout/folder/folder[@hidden='false' and @type='sidebar']" use="@name"/>

  <xsl:variable name="activeTabIdx">
    <!-- if the activeTab is a number then it is the active tab index -->
    <!-- otherwise it is the ID of the active tab. If it is the ID -->
    <!-- then check to see if that tab is still in the layout and -->
    <!-- if so use its index. if not then default to an index of 1. -->
    <xsl:choose>
      <xsl:when test="$focusedTabID!='none' and /layout/folder/folder[@ID=$focusedTabID and @type='regular' and @hidden='false']">
        <xsl:value-of select="count(/layout/folder/folder[@ID=$focusedTabID]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$defaultTab" />
      </xsl:otherwise> <!-- if not found, use first tab -->
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="activeTabID" select="/layout/folder/folder[@type='regular'and @hidden='false'][position() = $activeTabIdx]/@ID"/>

  <!-- Evaluate the 'activeTabGroup' (optional feature) -->
  <xsl:variable name="activeTabGroup">
    <xsl:choose>
      <xsl:when test="//folder[@ID=$activeTabID]/@tabGroup">
        <xsl:value-of select="//folder[@ID=$activeTabID]/@tabGroup"/>
      </xsl:when>
      <xsl:otherwise>DEFAULT_TABGROUP</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

<xsl:template name="debug-info">
    <!-- This element is not (presently) consumed by the theme transform, but it can be written to the logs easy debugging -->
    <debug>
        <userLayoutRoot><xsl:value-of select="$userLayoutRoot"></xsl:value-of></userLayoutRoot>
        <focusedTabID><xsl:value-of select="$focusedTabID"></xsl:value-of></focusedTabID>
        <defaultTab><xsl:value-of select="$defaultTab"></xsl:value-of></defaultTab>
        <detached><xsl:value-of select="$detached"></xsl:value-of></detached>
        <activeTabIdx><xsl:value-of select="$activeTabIdx"></xsl:value-of></activeTabIdx>
        <activeTabID><xsl:value-of select="$activeTabID"></xsl:value-of></activeTabID>
        <activeTabGroup><xsl:value-of select="$activeTabGroup"></xsl:value-of></activeTabGroup>
        <tabsInTabGroup><xsl:value-of select="count(/layout/folder/folder[@tabGroup=$activeTabGroup and @type='regular' and @hidden='false'])"/></tabsInTabGroup>
        <userImpersonation><xsl:value-of select="$userImpersonating"/></userImpersonation>
    </debug>
</xsl:template>

<xsl:template match="layout">
  <xsl:for-each select="folder[@type='root']">

  <xsl:choose>
    <xsl:when test="$userLayoutRoot != 'root' and $detached = 'true'">
      <layout_fragment>
        <xsl:call-template name="debug-info"/>
        <xsl:call-template name="tabList"/>
        <content>
          <!-- Detect whether a detached channel is present in the user's layout ? -->
          <xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
        </content>
      </layout_fragment>
    </xsl:when>
    <xsl:otherwise>

  <layout>
    <xsl:call-template name="debug-info"/>

    <xsl:if test="/layout/@dlm:fragmentName">
        <xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
    </xsl:if>

    <header>
      <xsl:choose>
        <xsl:when test="$userLayoutRoot = 'root'">
        	<!-- BEGIN display channel-headers for each channel visible on the page -->
            <xsl:for-each select="child::folder[@type='header']/descendant::channel">
        		<channel-header ID="{@ID}"/>
      		</xsl:for-each>
      		<xsl:for-each select="folder[@ID = $activeTabID and @type='regular' and @hidden='false']/descendant::channel">
        		<channel-header ID="{@ID}"/>
      		</xsl:for-each>
      		<xsl:for-each select="child::folder[attribute::type='footer']/descendant::channel">
        		<channel-header ID="{@ID}"/>
      		</xsl:for-each>
      		<!-- END display channel-headers for each channel visible on the page -->  
        </xsl:when>
        <xsl:otherwise>
		<!-- display only focused channel-header -->
		<channel-header ID="{$userLayoutRoot}"/>
        </xsl:otherwise>
      </xsl:choose>
      
      <!-- Allows header portlets to appear in the output, even in focused mode -->
      <xsl:for-each select="child::folder[@type='header']">
        <xsl:copy-of select=".//channel"/>
      </xsl:for-each> 
      
    </header>

    <xsl:call-template name="tabList"/>

    <content>
      <xsl:choose>
        <xsl:when test="$userLayoutRoot = 'root'">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']"/>
        </xsl:when>
        <xsl:otherwise>
          <focused>
          	<!-- Detect whether a focused channel is present in the user's layout -->
          	<xsl:attribute name="in-user-layout">
          		<xsl:choose>
          			<xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
          			<xsl:otherwise>no</xsl:otherwise>
          		</xsl:choose>
          	</xsl:attribute>
            <xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
          </focused>
        </xsl:otherwise>
      </xsl:choose>
    </content>

    <xsl:call-template name="sidebarList"/>

    <footer>
      <xsl:for-each select="child::folder[attribute::type='footer']">
	      <xsl:copy-of select=".//channel"/>
      </xsl:for-each>
    </footer>

  </layout>

    </xsl:otherwise>
  </xsl:choose>

  </xsl:for-each>
</xsl:template>

<xsl:template name="tabList">
  <navigation>
    <!-- The tabGroups (optional feature) -->
    <tabGroupsList>
      <xsl:attribute name="activeTabGroup">
        <xsl:value-of select="$activeTabGroup"/>
      </xsl:attribute>
      <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']"><!-- These are standard tabs -->
        <!-- Process only the first tab in each Tab Group (avoid duplicates) -->
        <xsl:if test="self::node()[generate-id() = generate-id(key('tabGroupKey',@tabGroup)[1])]">
          <tabGroup name="{@tabGroup}" firstTabId="{@ID}">
            <xsl:value-of select="@tabGroup"/>
          </tabGroup>
        </xsl:if>
      </xsl:for-each>
    </tabGroupsList>
    <!-- The tabs -->
    <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
      <tab>
        <!-- Copy folder attributes verbatim -->
        <xsl:for-each select="attribute::*">
          <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
        </xsl:for-each>
        <xsl:if test="count(./folder[not(@dlm:addChildAllowed='false')]) >0">
          <xsl:attribute name="dlm:hasColumnAddChildAllowed">true</xsl:attribute>
        </xsl:if>
        <!-- Add 'activeTab' and 'activeTabPosition' attributes as appropriate -->
        <xsl:choose>
          <xsl:when test="$activeTabID = @ID">
            <xsl:attribute name="activeTab">true</xsl:attribute>
            <xsl:attribute name="activeTabPosition"><xsl:value-of select="$activeTabID"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="activeTab">false</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:for-each select="./descendant::channel">
          <tabChannel name="{@name}" title="{@title}" ID="{@ID}" fname="{@fname}" description="{@description}">
            <xsl:choose>
              <xsl:when test="parameter[@name='PORTLET.quicklink']">
                <xsl:attribute name="quicklink">
                  <xsl:value-of select="parameter[@name='PORTLET.quicklink']/@value"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:when test="parameter[@name='quicklink']">
                <xsl:attribute name="quicklink">
                  <xsl:value-of select="parameter[@name='quicklink']/@value"/>
                </xsl:attribute>
              </xsl:when>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="parameter[@name='PORTLET.qID']">
                <xsl:attribute name="qID">
                  <xsl:value-of select="parameter[@name='PORTLET.qID']/@value"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:when test="parameter[@name='qID']">
                <xsl:attribute name="qID">
                  <xsl:value-of select="parameter[@name='qID']/@value"/>
                </xsl:attribute>
              </xsl:when>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="parameter[@name='PORTLET.removeFromLayout']">
                <xsl:attribute name="removeFromLayout">
                  <xsl:value-of select="parameter[@name='PORTLET.removeFromLayout']/@value"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:when test="parameter[@name='removeFromLayout']">
                <xsl:attribute name="removeFromLayout">
                  <xsl:value-of select="parameter[@name='removeFromLayout']/@value"/>
                </xsl:attribute>
              </xsl:when>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="parameter[@name='PORTLET.alternativeMaximixedLink']">
                <xsl:attribute name="alternativeMaximixedLink">
                  <xsl:value-of select="parameter[@name='PORTLET.alternativeMaximixedLink']/@value"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:when test="parameter[@name='alternativeMaximixedLink']">
                <xsl:attribute name="alternativeMaximixedLink">
                  <xsl:value-of select="parameter[@name='alternativeMaximixedLink']/@value"/>
                </xsl:attribute>
              </xsl:when>
            </xsl:choose>
          </tabChannel>
        </xsl:for-each>
      </tab>
    </xsl:for-each>
  </navigation>
</xsl:template>

<xsl:template name="sidebarList">
  <sidebar>
    <!-- To define sidebar elements - hidden from navigation but shown in sidebar and herited from DLM and ordered by precedence on tab-->
    <xsl:for-each select="/layout/folder/folder[@type='sidebar' and @hidden='false' and generate-id() = generate-id(key('sidebarGroupKey',@name)[1])]">
      <xsl:sort select="number(@dlm:precedence)" order="descending"/>
      <sidebarGroup name="{@name}">
        <xsl:for-each select="key('sidebarGroupKey',@name)">
          <xsl:sort select="number(@dlm:precedence)" order="descending"/>
            <xsl:for-each select="descendant::channel">
              <xsl:sort select="number(@dlm:precedence)" order="descending"/>
              <sidebarChannel name="{@name}" title="{@title}" ID="{@ID}" fname="{@fname}" description="{@description}">
                <xsl:choose>
                  <xsl:when test="parameter[@name='PORTLET.alternativeMaximixedLink']">
                    <xsl:attribute name="alternativeMaximixedLink">
                      <xsl:value-of select="parameter[@name='PORTLET.alternativeMaximixedLink']/@value"/>
                    </xsl:attribute>
                  </xsl:when>
                  <xsl:when test="parameter[@name='alternativeMaximixedLink']">
                    <xsl:attribute name="alternativeMaximixedLink">
                      <xsl:value-of select="parameter[@name='alternativeMaximixedLink']/@value"/>
                    </xsl:attribute>
                  </xsl:when>
                </xsl:choose>
              </sidebarChannel>
            </xsl:for-each>
        </xsl:for-each>
      </sidebarGroup>
    </xsl:for-each>
  </sidebar>
</xsl:template>

<xsl:template match="folder[@hidden='false']">
  <xsl:if test="$activeTabID = @ID">
    <xsl:if test="child::folder">
      <xsl:for-each select="folder">
        <column>
            <xsl:attribute name="ID">
              <xsl:value-of select="@ID"/>
            </xsl:attribute>
            <xsl:attribute name="priority">
              <xsl:value-of select="@priority"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="@width"/>
            </xsl:attribute>
            <xsl:if test="@dlm:moveAllowed = 'false'">
              <xsl:attribute name="dlm:moveAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:deleteAllowed = 'false'">
              <xsl:attribute name="dlm:deleteAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:editAllowed = 'false'">
              <xsl:attribute name="dlm:editAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:addChildAllowed = 'false'">
              <xsl:attribute name="dlm:addChildAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:precedence > 0">
              <xsl:attribute name="dlm:precedence">
                <xsl:value-of select="@dlm:precedence"/>
              </xsl:attribute>
            </xsl:if>
          <xsl:apply-templates/>
        </column>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="child::channel">
      <column>
        <xsl:apply-templates/>
      </column>
    </xsl:if>
  </xsl:if>
</xsl:template>

<xsl:template match="channel">
  <xsl:choose>
    <xsl:when test="$userImpersonating = 'true' and parameter[@name='blockImpersonation']/@value = 'true'">
        <blocked-channel>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="child::*"/>
        </blocked-channel>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="."/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="parameter">
  <xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. --><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->
