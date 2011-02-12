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

<xsl:stylesheet version="1.0" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!--=====START: DOCUMENT DESCRIPTION=====-->
<!--    
    Date: 08/14/2008
    Author: Matt Polizzotti
    Company: Unicon,Inc.
    uPortal Version: UP3.0.0 and up.
    
    General Description: This file, mobile-tab-column.xsl, was developed in order to 
    enable uPortal 3.0.0 to be viewable by mobile devices. This file exports xml, which 
    is then transformed by the muniversality.xsl file into the HTML markup rendered in a 
    browser. The mobile-column.xsl file was based upon the tab-column.xsl file.
    
    General Changes: The <navigation> and <content> nodes were removed and replaced with a single 
    <mobilenavigation> node. This reorganization allows for all the tabs and their associated 
    channels and portlets to be rendered on a small screen at the same time. From a high level, 
    the new xml output for these changes resemble the following architecture:
    
    <mobilenavigation>
            <group>
                <navblock />
                <channel />
            </group>
            <group>
                <navblock />
                <channel />
                <channel />
                <channel />
            </group>
    </mobilenavigation>
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->


<!--=====START: PARAMETERS & VARIABLES=====-->
    <xsl:param name="userLayoutRoot">root</xsl:param>
    <xsl:param name="focusedTabID">none</xsl:param>
    <xsl:param name="useSelectDropDown">true</xsl:param>
    
    <xsl:variable name="activeTabIDx">
        <xsl:choose>
            <xsl:when test="$focusedTabID!='none' and /layout/folder/folder[@ID=$focusedTabID and @type='regular' and @hidden='false']">
            	<xsl:value-of select="count(/layout/folder/folder[@ID=$focusedTabID]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="activeTabID" select="/layout/folder/folder[@type='regular'and @hidden='false'][position() = $activeTabIDx]/@ID"/>
<!--=====END: PARAMETERS & VARIABLES=====-->


<!--=====START: LAYOUT TEMPLATE RULE=====-->
    <xsl:template match="layout">
        <xsl:for-each select="folder[@type='root']">
            <layout>
                <xsl:if test="/layout/@dlm:fragmentName">
                	<xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
                </xsl:if>
                
                <!--header-->
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
      
      						<xsl:for-each select="child::folder[@type='header']">
          						<xsl:copy-of select=".//channel"/>
      						</xsl:for-each> 
      						<!-- END display channel-headers for each channel visible on the page -->  
        				</xsl:when>
      					<xsl:otherwise>
      						<!-- display only focused channel-header -->
      						<channel-header ID="{$userLayoutRoot}"/>
      					</xsl:otherwise>  
     				</xsl:choose>
                </header>
                
                <!--mobile navigation-->
                <mobilenavigation>
                	<xsl:attribute name="activeTabID">
                        <xsl:value-of select="$activeTabID" />
                    </xsl:attribute>
                    
                    <xsl:apply-templates select="folder" mode="navigation" />
                    
                </mobilenavigation>

                <!--focused-->
                <xsl:if test="$userLayoutRoot != 'root'">
                    <content>
                        <focused>
                            <xsl:attribute name="in-user-layout">
                                <xsl:choose>
                                    <xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
                                    <xsl:otherwise>no</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            
                            <xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
                            
                        </focused>
                    </content>
                </xsl:if>
                
                <!--footer-->
                <footer>
                    <xsl:for-each select="child::folder[attribute::type='footer']">
                    	<xsl:copy-of select=".//channel"/>
                    </xsl:for-each>
                </footer>
                
            </layout>    
        </xsl:for-each>
    </xsl:template>
<!--=====END: LAYOUT TEMPLATE RULE=====-->


<!--=====START: FOLDER TEMPLATE RULE (MOBILE)=====-->
<!--
    Code Description:
    1] Select all folders under the 'root' folder that are of 'regular' type and are not 'hidden'.
    2] Create a <group> node.
    3] Create variable $tab-folder. $tab-folder holds a reference to each folder under the 'root' folder that are of 'regular' type and are not 'hidden'.
    4] For each folder referenced in $tab-folder create a <navblock> node, add attributes and close the </navblock> node.
    5] If a folder referenced in $tab-folder contains child nodes, create variable $column-folder. $column-folder holds a reference to $tab-folder child nodes, or column folders. 
    6] For each child node referenced in $column-folder, check if the child node contains channels. If channels are found apply the appropriate templates.
    7] Close the </group> node.
-->
    <xsl:template match="folder" mode="navigation">
        <!--1]-->
        <xsl:if test="./@type='regular' and @hidden='false'">
            <!--2]-->
            <group>
                <xsl:attribute name="groupName">
                    <xsl:value-of select="@name"/>
                </xsl:attribute>
                <!--3]-->
                <xsl:variable name="tab-folder" select="." />
                <!--4]-->
                <xsl:for-each select="$tab-folder">
                        <navblock>
                            <xsl:copy-of select="@*"/>
                            <xsl:choose>
                                <xsl:when test="$activeTabID = @ID">
                                    <xsl:attribute name="activeTab">true</xsl:attribute>
                                    <xsl:attribute name="activeTabPosition"><xsl:value-of select="$activeTabID"/></xsl:attribute>
                                </xsl:when>
                                
                                <xsl:otherwise>
                                    <xsl:attribute name="activeTab">false</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                        </navblock>
                        <!--5]-->
                        <xsl:if test="child::folder">
                            <xsl:variable name="column-folder" select="folder[@name]" />
                            <!--6]-->
                            <xsl:for-each select="$column-folder">
                                <xsl:if test="child::channel">
                                    <xsl:apply-templates mode="navigation" />
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:if>
                </xsl:for-each>
            <!--7]-->
            </group>
        </xsl:if>
    </xsl:template>
<!--=====END: FOLDER TEMPLATE RULE (MOBILE)=====-->


<!--=====START: CHANNEL-NAVIGATION TEMPLATE RULE=====-->
    <xsl:template match="channel" mode="navigation">
        <xsl:if test="not(parameter[@name='hideFromMobile']/@value = 'true')">
            <channel-nav>
                <xsl:copy-of select="@*"/>
                <xsl:copy-of select="child::*"/>
            </channel-nav>
        </xsl:if>
    </xsl:template>
<!--=====END: CHANNEL-NAVIGATION TEMPLATE RULE=====-->

<!--=====START: CHANNEL TEMPLATE RULE=====-->
<xsl:template match="channel">
    <xsl:if test="not(parameter[@name='hideFromMobile']/@value = 'true')">
        <xsl:copy-of select="."/>
    </xsl:if>
</xsl:template>
<!--=====END: CHANNEL TEMPLATE RULE=====-->


<!--=====START: PARAMETER TEMPLATE RULE=====-->
    <xsl:template match="parameter">
      <xsl:copy-of select="."/>
    </xsl:template>
<!--=====END: PARAMETER TEMPLATE RULE=====-->

</xsl:stylesheet>