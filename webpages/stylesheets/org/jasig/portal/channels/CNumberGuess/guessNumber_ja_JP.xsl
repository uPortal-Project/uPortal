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
<xsl:param name="locale">ja_JP</xsl:param>

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="suggest">
      残念! <xsl:value-of select="guess"/> じゃないよ．
      もうちょっと <span class="uportal-channel-strong"><xsl:value-of select="suggest"/></span> だ!<br />
      <xsl:value-of select="guesses"/>回目は失敗だ．
    </xsl:when>
    <xsl:when test="answer">
      当り! <span class="uportal-channel-strong"><xsl:value-of select="guesses"/></span>回目のトライで当てたね!
      答えは <span class="uportal-channel-strong"><xsl:value-of select="answer"/></span> でした!<br />
      <p>もう1回やろうよ...</p>
    </xsl:when>
    <xsl:otherwise>数当てゲームやろうよ．<br /></xsl:otherwise>
  </xsl:choose> 
  
  僕は今， 
  <xsl:value-of select="minNum"/> と 
  <xsl:value-of select="maxNum"/> の間の数を頭に思い浮かべてるんだけど，いくつだと思う?<br />
  
    <form action="{$baseActionURL}" method="post">
      <input type="hidden" name="uP_root" value="me"/>
      <input type="text" name="guess" size="4" class="uportal-input-text"/>
      <input type="submit" value="送信" class="uportal-button"/>
    </form>
</xsl:template>

</xsl:stylesheet> 
