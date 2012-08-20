<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="https://source.jasig.org/schemas/uportal/io/portlet-definition" version="1.0">

    <xsl:output indent="yes"/>
    
    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
    
    <xsl:template match="channel-definition">
        <portlet-definition xmlns="https://source.jasig.org/schemas/uportal/io/portlet-definition"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/portlet-definition https://source.jasig.org/schemas/uportal/io/portlet-definition/portlet-definition-4.0.xsd"
            version="4.0">

            <xsl:choose>
            	<!-- Upgrade a CInlineFrame channel to an IFrame portlet -->
                <xsl:when test="class = 'org.jasig.portal.channels.CInlineFrame'">
                	<xsl:apply-templates select="title|name|fname|desc"/>
                	<!-- Matches the Inline_Frame.portlet-type.xml name -->
                	<type>Inline Frame</type>
                	<xsl:apply-templates select="timeout"/>
                    <portlet-descriptor xmlns:up="https://source.jasig.org/schemas/uportal">
                        <up:isFramework>true</up:isFramework>
                        <up:portletName>IFrame</up:portletName>
                    </portlet-descriptor>
            
		            <xsl:apply-templates select="categories|groups|users"/>
		            <xsl:apply-templates select="parameters/parameter[name != 'url' and name != 'height' and name != 'name']"/>
		            <xsl:apply-templates select="hasedit|hashelp|hasabout|secure|locale"/>
                    
					<xsl:for-each select="parameters/parameter[name = 'url' or name = 'height' or name = 'name']">
	                    <portlet-preference>
	                        <name><xsl:value-of select="name"/></name>
                        	<value><xsl:value-of select="value"/></value>
	                    </portlet-preference>
					</xsl:for-each>
		            <xsl:apply-templates select="portletPreference"/>
                </xsl:when>
                
                <!-- Not converting a known IChannel type to a portlet, use the generic rules -->                
                <xsl:otherwise>
            		<xsl:apply-templates select="title|name|fname|desc|timeout|type"/>

		            <!-- Only add portlet-descriptor if there is enough data. No descriptor means it isn't a portlet and XSD validation should fail -->
		            <xsl:choose>
		                <xsl:when test="parameters/parameter[name = 'portletName']/value">
		                    <portlet-descriptor xmlns:up="https://source.jasig.org/schemas/uportal">
		                        <xsl:choose>
		                            <xsl:when test="parameters/parameter[name = 'portletApplicationId']">
		                                <up:webAppName>
		                                    <xsl:value-of select="parameters/parameter[name = 'portletApplicationId']/value"/>
		                                </up:webAppName>
		                            </xsl:when>
		                            <xsl:otherwise>
		                                <up:isFramework>true</up:isFramework>
		                            </xsl:otherwise>
		                        </xsl:choose>
		                        <up:portletName>
		                            <xsl:value-of select="parameters/parameter[name = 'portletName']/value"/>
		                        </up:portletName>
		                    </portlet-descriptor>
		                </xsl:when>
		                <xsl:otherwise>
		                    <portlet-descriptor xmlns:up="https://source.jasig.org/schemas/uportal">
		                        <up:isFramework>true</up:isFramework>
		                        <up:portletName>UPGRADED_CHANNEL_IS_NOT_A_PORTLET</up:portletName>
		                    </portlet-descriptor>
		                </xsl:otherwise>
		            </xsl:choose>
            
		            <xsl:apply-templates select="categories|groups|users|parameters"/>
		            <xsl:apply-templates select="hasedit|hashelp|hasabout|secure|locale"/>
		            <xsl:apply-templates select="portletPreferences"/>
                </xsl:otherwise>
            </xsl:choose>
        </portlet-definition>
    </xsl:template>
    
    
    <xsl:template match="type">
        <type>
            <xsl:choose>
                <xsl:when test=". = 'Simple Web Proxy Portlet'">Web Proxy Portlet</xsl:when>
                <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
            </xsl:choose>
        </type>
    </xsl:template>
    
    <!-- Collection wrapper elements that aren't carried forward -->
    <xsl:template match="categories|groups|users|parameters|portletPreferences|values">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- Elements being removed -->
    <xsl:template match="ovrd|parameter[name = 'portletApplicationId' or name = 'portletName' or name = 'isFrameworkPortlet']"/>
    
    <!-- Best attempt at boolean string parsing -->
    <xsl:template match="read-only">
        <readOnly>
            <xsl:choose>
                <xsl:when test="'true' = translate(., $uppercase, $lowercase)">true</xsl:when>
                <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
        </readOnly>
    </xsl:template>
    
    <xsl:template match="portletPreference">
        <portlet-preference>
            <xsl:apply-templates/>
        </portlet-preference>
    </xsl:template>

    <!-- Convert direct properties to parameters -->
    <xsl:template match="hasedit|hashelp|hasabout|secure|locale">
        <parameter>
            <name>
	      <xsl:choose>
		<xsl:when test="name() = 'hasedit'">editable</xsl:when>
		<xsl:when test="name() = 'hasabout'">hasAbout</xsl:when>
		<xsl:when test="name() = 'hashelp'">hasHelp</xsl:when>
                <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
	      </xsl:choose>
            </name>
            <value>
                <xsl:choose>
                    <xsl:when test=". = 'Y'">true</xsl:when>
                    <xsl:otherwise>false</xsl:otherwise>
                </xsl:choose>
            </value>
        </parameter>
    </xsl:template>

    <!-- Copied elements can't use xsl:copy since we're adding a namespace -->
    <xsl:template match="*">
        <xsl:element name="{name()}" namespace="https://source.jasig.org/schemas/uportal/io/portlet-definition">
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
