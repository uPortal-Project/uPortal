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
  <xsl:param name="locale">ja_JP</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      
      uPortal チャネルは，<span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> オブジェクトを通じてユーザ属性にアクセスできます．
      属性名は， 
      <a href="http://www.educause.edu/eduperson/">eduPerson object class</a> バージョン1.0 で定義されています．
    </p>
    <p>
      uPortal のインプリメンタは，人に関するディレクトリまたはデータベースでのローカル情報にこれらの標準的な名前をマップします．
      
      マッピングは，<span class="uportal-channel-code">properties/PersonDirs.xml</span> ファイルに含まれています．
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>属性名</th>
        <th>属性値</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">利用可能な属性：</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="defined"/>
      <tr class="uportal-background-light">
        <td colspan="2">利用不可能な属性：</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="undefined"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute" mode="defined">
    <xsl:if test="value">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <xsl:choose>
        <xsl:when test="name='jpegPhoto'">
            <td><img src="{$downloadWorkerURL}?attribute={name}" /></td>
        </xsl:when>
        <xsl:otherwise>
            <td><xsl:value-of select="value"/></td>
        </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="attribute" mode="undefined">
    <xsl:if test="not(value)">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <td>[利用不可]</td>
      </tr>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet> 
