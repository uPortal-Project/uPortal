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

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    
    <xsl:template match="channel-definition">
        <channel-definition script="classpath://org/jasig/portal/io/import-channel_v3-0.crn">
            <xsl:apply-templates />
        </channel-definition>
    </xsl:template>
    
    <xsl:template match="parameter[name = 'portletDefinitionId']">
        <parameter>
            <name>portletApplicationId</name>
            <value>/<xsl:value-of select="substring-before(value, '.')"/></value>
            <description/>
            <ovrd>N</ovrd>
        </parameter>
        <parameter>
            <name>portletName</name>
            <value><xsl:value-of select="substring-after(value, '.')"/></value>
            <description/>
            <ovrd>N</ovrd>
        </parameter>
    </xsl:template>
    
    <xsl:template match="@*|*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
