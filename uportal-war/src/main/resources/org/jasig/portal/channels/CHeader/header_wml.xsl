<?xml version='1.0' encoding='utf-8' ?>
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

  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="authenticated">false</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/layout/nested-categories/deck-of-cards</xsl:variable>

  <xsl:template match="header"> 
    <p align="right"><img src="{$mediaPath}/uPortal-logo.wbmp" width="45" height="30" alt="uPortal 2.0"/></p>
    <p align="right"><small>Hello <xsl:value-of select="full-name"/></small></p>
    <p align="right"><small><xsl:value-of select="timestamp-short"/></small></p>
  </xsl:template>

</xsl:stylesheet>
