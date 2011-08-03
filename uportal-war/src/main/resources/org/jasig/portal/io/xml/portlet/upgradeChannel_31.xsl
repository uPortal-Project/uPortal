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
    xmlns:fname="http://xml.apache.org/xalan/java/org.jasig.portal.dao.usertype.FunctionalNameType"
    extension-element-prefixes="fname"
    exclude-result-prefixes="fname"
    version="1.0">
    
    <xsl:output indent="yes"/>
    
    <xsl:template match="channel-definition">
        <channel-definition script="classpath://org/jasig/portal/io/import-channel_v3-2.crn"> 
            <xsl:apply-templates />
        </channel-definition>
    </xsl:template>
    
    <xsl:template match="fname">
        <xsl:copy><xsl:value-of select="fname:makeValid(.)"/></xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*|*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
