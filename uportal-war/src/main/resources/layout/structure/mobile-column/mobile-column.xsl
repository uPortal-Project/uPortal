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
    Author: Matt Polizzotti, Jennifer Bourey
    Version: $Revision$
    
    General Description: This file, mobile-tab-column.xsl, was developed in order to 
    enable uPortal 3.0.0 to be viewable by mobile devices. This file exports xml, which 
    is then transformed by the muniversality.xsl file into the HTML markup rendered in a 
    browser. The mobile-column.xsl file was based upon the tab-column.xsl file.
    
    General Changes: The <navigation> and <content> nodes were removed and replaced with a single 
    <mobilenavigation> node. This reorganization allows for all the tabs and their associated 
    channels and portlets to be rendered on a small screen at the same time. From a high level, 
    the new xml output for these changes resemble the following architecture:
    
    Navigation mode layout structure:
    
    <layout>
        <header>
            <channel-header />
            <channel-header />
        </header>
        <content>
            <navigation>
                <channel />
                <channel />
                <channel />
            </navigation>
        </content>
    </layout>
    
    
    Focused portlet mode layout structure:
    
    <layout>
        <header>
            <channel-header />
            <channel-header />
        </header>
        <content>
            <focused>
                <channel />
            </focused>
        </content>
    </layout>
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->


<!--=====START: PARAMETERS & VARIABLES=====-->
<xsl:param name="userLayoutRoot">root</xsl:param>
<xsl:param name="detached">false</xsl:param>
<xsl:param name="userImpersonating">false</xsl:param>
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
           					<xsl:for-each select="child::folder/descendant::channel">
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
                <content>
                    <xsl:apply-templates select="folder" mode="navigation" />
                    <xsl:choose>
                        <!-- focused -->
                        <xsl:when test="$userLayoutRoot != 'root'">
                            <focused>
                                <xsl:attribute name="in-user-layout">
                                    <xsl:choose>
                                        <xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
                                        <xsl:otherwise>no</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:attribute name="detached">
                                    <xsl:value-of select="$detached"/>
                                </xsl:attribute>
                                
                                <xsl:apply-templates select="//channel[@ID = $userLayoutRoot]"/>
                                
                            </focused>
                        </xsl:when>
                        
                        <!-- non-focused -->
                        <xsl:otherwise>
                            <navigation>
                                <xsl:for-each select="child::folder[@type!='footer' and @type!='header']">
                                    <tab ID="{@ID}" name="{@name}">
                                        <xsl:apply-templates select=".//channel"/>
                                    </tab>
                                </xsl:for-each>
                            </navigation>
                        </xsl:otherwise>
                    </xsl:choose>
                </content>

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

<!--=====START: CHANNEL TEMPLATE RULE=====-->
<xsl:template match="channel">
  <xsl:choose>
    <xsl:when test="not(parameter[@name='hideFromMobile']/@value = 'true') and $userImpersonating = 'true' and parameter[@name='blockImpersonation']/@value = 'true'">
        <blocked-channel>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="child::*"/>
        </blocked-channel>
    </xsl:when>
    <xsl:when test="not(parameter[@name='hideFromMobile']/@value = 'true')">
      <xsl:copy-of select="."/>
    </xsl:when>
  </xsl:choose>
</xsl:template>
<!--=====END: CHANNEL TEMPLATE RULE=====-->


<!--=====START: PARAMETER TEMPLATE RULE=====-->
    <xsl:template match="parameter">
      <xsl:copy-of select="."/>
    </xsl:template>
<!--=====END: PARAMETER TEMPLATE RULE=====-->

</xsl:stylesheet>