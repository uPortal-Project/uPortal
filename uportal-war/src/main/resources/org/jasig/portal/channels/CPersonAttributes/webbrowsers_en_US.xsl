<?xml version="1.0"?>
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

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal channels have access to user attributes
      via the <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> object.
      Attribute names are defined in the 
      <a href="http://www.educause.edu/eduperson/">eduPerson object class</a> version 1.0.
    </p>
    <p>
      uPortal implementors are to map these standard attribute names to
      local names in their person directory or database.  Mappings are contained in
      the <span class="uportal-channel-code">properties/PersonDirs.xml</span> file.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Att. Name</th>
        <th>Att. Value</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Available attributes:</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="defined"/>
      <tr class="uportal-background-light">
        <td colspan="2">Unavailable attributes:</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="undefined"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute" mode="defined">
    <xsl:for-each select="value">
      <tr>
        <td>
          <xsl:if test="position() = 1">
            <xsl:value-of select="../name"/>
          </xsl:if>
        </td>
        <xsl:choose>
        <xsl:when test="../name='jpegPhoto'">
            <td><img src="{$downloadWorkerURL}?attribute={../name}" /></td>
        </xsl:when>
        <xsl:otherwise>
            <td><xsl:value-of select="."/></td>
        </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="attribute" mode="undefined">
    <xsl:if test="not(value)">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <td>[Not available]</td>
      </tr>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet> 
