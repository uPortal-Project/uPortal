<?xml version='1.0'?>
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
    <xsl:param name="locale">sv_SE</xsl:param>
    
    <xsl:template match="iframe">
        <iframe src="{url}" height="{height}" frameborder="0" width="100%">
            <xsl:if test="name!=''">
                <xsl:attribute name="name">
                        <xsl:value-of select="name"/>
                </xsl:attribute>
            </xsl:if>
            Denna webläsare stödjer inte inline frames.<br/>
            <a href="{url}" target="_blank">Klicka här för att öppna sidan</a> i ett eget fönster.</iframe>
    </xsl:template>
</xsl:stylesheet>
