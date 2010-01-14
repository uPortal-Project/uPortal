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

<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="content">

  <p align="center"><xsl:value-of select="caption"/></p>
  <p align="center">
    <a href="{image/@link}">
      <img src="{image/@src}" alt="{image/@alt-text}" width="{image/@width}" height="{image/@height}" border="{image/@border}"/>
    </a>
  </p>
  <p align="center"><font size="2"><xsl:value-of select="subcaption"/></font></p>

</xsl:template>

</xsl:stylesheet>
