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
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="unauthenticated">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>

  <xsl:template match="login-status">
    <xsl:choose>
      <xsl:when test="$unauthenticated='true'">
        <xsl:call-template name="login-form"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="logged-in"/>
      </xsl:otherwise>      
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="logged-in">
    <do type="accept" label="Yes"><go href="Logout"/></do>  
    <do type="options" label="No"><prev/></do>  
    <p>Do you really want to logout?</p>
  </xsl:template>

  <xsl:template name="login-form">
    <do type="accept" label="Submit">
      <go href="Authentication">
        <postfield name="userName" value="$(userName)" />
        <postfield name="password" value="$(password)" />
      </go>
    </do>  
    <do type="options" label="Back">
      <prev/>
    </do>
    
    <xsl:apply-templates/>    
    
    <p> 
      User name: <input name="userName" size="15"/><br/> 
      Password:  <input name="password" size="15"/><br/>
      Press Submit to login...
    </p>
  </xsl:template>
  
  <xsl:template match="failure">
    <p>The user name/password combination entered is not recognized. Please try again!</p>
  </xsl:template>
  
  <xsl:template match="error">
    <p>
      An error occured during authentication.  
      The portal is unable to log you on at this time.
      Try again later.
    </p>
  </xsl:template>
  
  <xsl:template match="full-name">
  </xsl:template>

</xsl:stylesheet>
