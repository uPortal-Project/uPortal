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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:key name="distinct-parameter" match="name" use="."/>
    
    <xsl:template match="channel-definition">
        <channel-definition script="classpath://org/jasig/portal/io/import-channel_v3-1.crn"> 
            <xsl:apply-templates />
        </channel-definition>
    </xsl:template>

    <xsl:template match="parameters">
        <xsl:copy>
            <!-- Copy all non-portlet parameters -->
            <xsl:for-each select="parameter[not(starts-with(name, 'PORTLET.'))]">
                <xsl:copy>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>

        <xsl:if test="count(parameter[starts-with(name, 'PORTLET.')]) > 0">
            <portletPreferences>
                <!-- Select all portlet preference parameters, then filter for unique names -->
                <xsl:for-each select="parameter[starts-with(name, 'PORTLET.')][generate-id(name)=generate-id(key('distinct-parameter',name))]">
                    <xsl:sort select="substring-after(name, 'PORTLET.')"/>
                    <portletPreference>
                        <name>
                            <xsl:value-of select="substring-after(name, 'PORTLET.')"/>
                        </name>
                        <read-only>
                            <xsl:choose>
                                <xsl:when test="ovrd = 'Y'">false</xsl:when>
                                <xsl:otherwise>true</xsl:otherwise>
                            </xsl:choose>
                        </read-only>
                        <values>
                            <xsl:variable name="PREF_NAME" select="name"/>
                            <xsl:for-each select="/channel-definition/parameters/parameter[name=$PREF_NAME]">
                                <value>
                                    <xsl:value-of select="value"/>
                                </value>
                            </xsl:for-each>
                        </values>
                    </portletPreference>
                </xsl:for-each>
            </portletPreferences>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@*|*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
