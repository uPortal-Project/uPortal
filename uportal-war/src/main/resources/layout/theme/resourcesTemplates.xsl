<?xml version="1.0" encoding="UTF-8" ?>
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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:resources="http://xml.apache.org/xalan/java/org.jasig.portal.web.skin.ResourcesElementsXsltcHelper"
    exclude-result-prefixes="resources">

    <xsl:param name="RESOURCES_ELEMENTS_PROVIDER" />
    <xsl:param name="CURRENT_REQUEST" />
    
    <xsl:template name="skinResources">
        <xsl:param name="path" />
 
        <xsl:variable name="resourceHelper" select="resources:getElmenentsProvider($RESOURCES_ELEMENTS_HELPER)" />
        <xsl:variable name="request" select="resources:getHttpServletRequest($CURRENT_REQUEST)" />
 
        <xsl:copy-of select="resources:getResourcesXmlFragment($resourceHelper, $request, $path)" />
    </xsl:template>
    
    <xsl:template name="skinParameter">
        <xsl:param name="path" />
        <xsl:param name="name" />
        
        <xsl:variable name="resourceHelper" select="resources:getElmenentsProvider($RESOURCES_ELEMENTS_HELPER)" />
        <xsl:variable name="request" select="resources:getHttpServletRequest($CURRENT_REQUEST)" />

        <xsl:value-of select="resources:getResourcesParameter($resourceHelper, $request, $path, $name)" />
    </xsl:template>

</xsl:stylesheet>