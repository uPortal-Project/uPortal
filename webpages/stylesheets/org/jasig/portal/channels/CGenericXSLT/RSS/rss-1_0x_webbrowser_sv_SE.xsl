<?xml version='1.0' encoding='utf-8' ?>

<!--
Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.
   
3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."
   
THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: Susan Bramhall, susan.bramhall@yale.edu
Author: Ken Weiner, kweiner@unicon.net
Version $Revision$
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                              xmlns:rss10="http://purl.org/rss/1.0/">

  <xsl:output indent="yes" method="html"/>

  <xsl:param name="locale">sv_SE</xsl:param>

  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT</xsl:variable>

  <xsl:template match="rdf:RDF" name="documentNode">
    <!--<html>
      <head>
        <title>uPortal 2.0</title>
      </head>

      <body>-->
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr align="left">
        <td width="100%" valign="bottom" class="uportal-channel-subtitle">
          <xsl:value-of select="./rss10:channel/rss10:description" />
        </td>
		<xsl:if test="./rss10:image != ''">
        <td>
          <a target="_blank" href="{./rss10:image/rss10:link}">
            <img border="0" alt="{./rss10:image/rss10:title}" src="{./rss10:image/rss10:url}"/>
          </a>
        </td>
		</xsl:if>
      </tr>
    </table>
    <br />

    <xsl:apply-templates select="/rdf:RDF/rss10:item" />
    <br />

    <xsl:apply-templates select="/rdf:RDF/rss10:textinput" />
      <!--</body>
    </html>-->
  </xsl:template>


  <xsl:template match="rss10:item">
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td>
          <img alt="bullet item" src="{$mediaPath}/bullet.gif" width="16" height="16" />
        </td>

        <td width="100%" class="uportal-channel-subtitle-reversed">
          <a href="{rss10:link}" target="_blank">
            <xsl:value-of select="rss10:title" />
          </a>
        </td>
      </tr>

      <xsl:if test="rss10:description != ''">
      <tr class="uportal-channel-text">
        <td></td>
        <td width="100%">
          <xsl:value-of select="rss10:description" />
        </td>
      </tr>
      </xsl:if>
      
    </table>
  </xsl:template>

  <xsl:template match="rss10:textinput">
    <form action="{rss10:link}">
      <span class="uportal-label">
        <xsl:value-of select="rss10:description" />
      </span>

      <br />

      <input type="text" name="{rss10:name}" size="30" class="uportal-input-text" />

      <br />

      <input type="submit" name="Submit" value="Skicka" class="uportal-button" />
    </form>
  </xsl:template>

</xsl:stylesheet>



