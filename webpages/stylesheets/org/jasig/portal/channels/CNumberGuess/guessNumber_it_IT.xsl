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
<xsl:param name="locale">it_IT</xsl:param>

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      La risposta <xsl:value-of select="guess"/> è sbagliata.
      Ritenta -- Indovina <span class="uportal-channel-strong"><xsl:value-of select="suggest"/></span>!<br />
      Hai fatto <xsl:value-of select="guesses"/> tentativi.
    </xsl:when>
    <xsl:when test="answer">
      L'hai indovinato dopo <span class="uportal-channel-strong"><xsl:value-of select="guesses"/></span> tentativi!
      La risposta esatta era <span class="uportal-channel-strong"><xsl:value-of select="answer"/></span>!<br />
      <p>Riprova con un nuova partita...</p>
    </xsl:when>
    <xsl:otherwise>Questo è il gioco: "Indovina il numero".<br /></xsl:otherwise>
  </xsl:choose> 
  
  Penso che il numero sia compreso tra 
  <xsl:value-of select="minNum"/> e 
  <xsl:value-of select="maxNum"/>.<br />
  Qual è il numero esatto?
    <form action="{$baseActionURL}" method="post">
      <input type="hidden" name="uP_root" value="me"/>
      <input type="text" name="guess" size="4" class="uportal-input-text"/>
      <input type="submit" value="Conferma" class="uportal-button"/>
    </form>
</xsl:template>

</xsl:stylesheet> 
