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
  <xsl:output method="html" indent="yes"/>
  <xsl:variable name="mediaPath">media/skins/universality</xsl:variable>
  <xsl:variable name="skin">java</xsl:variable>

  <xsl:template match="channel">
    <html>
    <head>
      <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css" />
    </head>
    <body>
     <xsl:copy-of select="child::*"/>
    </body></html>
  </xsl:template>

</xsl:stylesheet>
