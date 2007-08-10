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

Author: Ken Weiner, kweiner@unicon.net
$Revision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="authenticated">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:template match="header">
    <xsl:if test="$authenticated != 'false'">
      <a href="{$baseActionURL}?uP_root=root" class="uportal-navigation-category">Home </a> | 
      <a href="{$baseActionURL}?uP_fname=layout-sitemap" class="uportal-navigation-category"> Site Map </a> |
      <xsl:if test="chan-mgr-chanid">
      <a href="{$baseActionURL}?uP_fname={chan-mgr-chanid}" class="uportal-navigation-category"> Channel Admin </a> | 
      </xsl:if>
      <a href="{$baseActionURL}?uP_fname={preferences-chanid}" class="uportal-navigation-category"> Preferences </a> | <a href="Logout" class="uportal-navigation-category"> Logout </a>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
