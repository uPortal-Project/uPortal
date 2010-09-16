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

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    exclude-result-prefixes="upGroup" 
    version="1.0">
    
    <xsl:param name="USER_ID">guest</xsl:param>
    
    <xsl:template match="doc">
        <out>
            <xsl:attribute name="fname">
                <xsl:value-of select="element/@fname" />
            </xsl:attribute>
            <xsl:if test="upGroup:isChannelDeepMemberOf(element/@fname, 'local.1')">
                <true/>
            </xsl:if>
            <xsl:if test="not(upGroup:isChannelDeepMemberOf(element/@fname, 'local.2'))">
                <false/>
            </xsl:if>
            <xsl:if test="upGroup:isUserDeepMemberOfGroupName($USER_ID, 'Fragment Owners')">
                <true2/>
            </xsl:if>
        </out>
    </xsl:template>
</xsl:stylesheet>