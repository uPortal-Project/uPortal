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
<xsl:stylesheet xmlns="https://source.jasig.org/schemas/uportal/io/user" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="yes"/>
    
    <xsl:template match="subscribed-fragments">
        <subscribed-fragments xmlns="https://source.jasig.org/schemas/uportal/io/subscribed-fragment"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="https://source.jasig.org/schemas/uportal/io/subscribed-fragment https://source.jasig.org/schemas/uportal/io/subscribed-fragment/subscribed-fragment-4.0.xsd"
            version="4.0">
            <xsl:attribute name="username"><xsl:value-of select="@username"/></xsl:attribute>
            
            <xsl:apply-templates />
        </subscribed-fragments>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:element name="{name()}" namespace="https://source.jasig.org/schemas/uportal/io/subscribed-fragment">
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>