<?xml version="1.0"?>

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

Author: Ken Weiner, kweiner@interactivebusiness.com
Version $Revision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="locale">lv_LV</xsl:param>

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      J\u016Bsu min\u0113jums <xsl:value-of select="guess"/> ir nepareizs.
      M\u0113\u0123in\u0101t v\u0113lreiz <span class="uportal-channel-strong"><xsl:value-of select="suggest"/></span>!<br />
      J\u016Bs esat izteicis <xsl:value-of select="guesses"/> min\u0113jumus.
    </xsl:when>
    <xsl:when test="answer">
      Jums ir izdevies p\u0113c <span class="uportal-channel-strong"><xsl:value-of select="guesses"/></span> m\u0113\u0123in\u0101jumiem!
      Pareiz\u0101 atbilde ir <span class="uportal-channel-strong"><xsl:value-of select="answer"/></span>!<br />
      <p>L\u016Bdzu, sp\u0113l\u0113jiet v\u0113lreiz...</p>
    </xsl:when>
    <xsl:otherwise>\u0160\u012B ir skait\u013Cu min\u0113šanas sp\u0113le.<br /></xsl:otherwise>
  </xsl:choose> 
  
  Iedom\u0101jieties skaitli no 
  <xsl:value-of select="minNum"/> l\u012Bdz 
  <xsl:value-of select="maxNum"/>.<br />
  K\u0101ds ir j\u016Bsu min\u0113jums?
    <form action="{$baseActionURL}" method="post">
      <input type="hidden" name="uP_root" value="me"/>
      <input type="text" name="guess" size="4" class="uportal-input-text"/>
      <input type="submit" value="Iesniegt" class="uportal-button"/>
    </form>
</xsl:template>

</xsl:stylesheet> 
