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

Author: Ken Weiner, kweiner@interactivebusiness.com
$Revision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="unauthenticated">false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
<xsl:variable name="mediaPath" select="'media/org/jasig/portal/channels/CLogin'"/>
  <xsl:template match="login-status">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <xsl:choose>
        <xsl:when test="$unauthenticated='true'">
          <form action="Login" method="post">
            <input type="hidden" name="action" value="login"/>
            <xsl:call-template name="buildTable"/>
          </form>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="buildTable"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$unauthenticated='true'">
      </xsl:if>
    </table>
  </xsl:template>
  <xsl:template match="failure">
    <tr class="uportal-background-light">
      <td width="100%" class="uportal-channel-warning" colspan="1" rowspan="1">ユーザID またはパスワードが間違っています．もう一度入力してください．<span class="uportal-channel-warning"></span>
      </td>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="error">
    <tr class="uportal-background-light">
      <td width="100%" class="uportal-channel-warning" colspan="1" rowspan="1">認証中にエラーが発生しました．ポータルにログインすることは現在できません．しばらくしてから再度お試しください．<span class="uportal-channel-warning"></span>
      </td>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="full-name">
  </xsl:template>
  <xsl:template name="buildTable">
    <tr>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
    <tr>
      <td colspan="1" rowspan="1">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td colspan="4" class="uportal-background-shadow" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td rowspan="4" colspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
            </td>
            <td class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
          <tr>
            <td colspan="3" nowrap="nowrap" class="uportal-background-light" rowspan="1">
              <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <tr class="uportal-background-light">
                  <td class="uportal-channel-text" nowrap="nowrap" colspan="1" rowspan="1">
                    <strong>
                      <xsl:choose>
                        <xsl:when test="$unauthenticated='true'">名古屋大学ポータルへようこそ </xsl:when>
                        <xsl:otherwise><xsl:value-of select="full-name"/>さん，ようこそ </xsl:otherwise>
                      </xsl:choose>
                    </strong>
                  </td>
                </tr>
              </table>
            </td>
            <td nowrap="nowrap" class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td nowrap="nowrap" class="uportal-background-med" rowspan="2" colspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td nowrap="nowrap" class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td width="100%" class="uportal-channel-text" colspan="1" rowspan="1">
              <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <tr class="uportal-background-light">
                  <td width="100%" class="uportal-channel-text" nowrap="nowrap" colspan="1" rowspan="1">
                    <!--Right Content Cell [1]-->
                    <xsl:choose>
                      <xsl:when test="$unauthenticated='true'">
                        <span class="uportal-label">ユーザID：<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input class="uportal-input-text" type="text" name="userName" size="15">
                            <xsl:attribute name="value">
                              <xsl:value-of select="/login-status/failure/@attemptedUserName"/>
                            </xsl:attribute></input>パスワード：<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input class="uportal-input-text" type="password" name="password" size="15"/><img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input type="submit" value="ログイン" name="Login" class="uportal-button"/></span>
                      </xsl:when>
                      <xsl:otherwise>現在，ログインしています．</xsl:otherwise>
                    </xsl:choose>
                  </td>
                  <td class="uportal-text-small" nowrap="nowrap" colspan="1" rowspan="1">
                    <xsl:choose>
                      <xsl:when test="$unauthenticated='true'">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </xsl:when>
                      <xsl:otherwise>

                        <SCRIPT LANGUAGE="JavaScript1.2">


var months=new Array(13);
months[1]='1月';
months[2]='2月';
months[3]='3月';
months[4]='4月';
months[5]='5月';
months[6]='6月';
months[7]='7月';
months[8]='8月';
months[9]='9月';
months[10]='10月';
months[11]='11月';
months[12]='12月';
var time=new Date();
var lmonth=months[time.getMonth() + 1];
var date=time.getDate();
var year=time.getYear();
if (year &lt; 1000) {year = 1900 + year;} 

document.write(year + '年' + lmonth + date + '日');
              </SCRIPT>
                      </xsl:otherwise>
                    </xsl:choose>
                  </td>
                </tr>
                <!--Right Message Row -->
                <xsl:if test="$unauthenticated='true'">
                  <xsl:apply-templates/>
                </xsl:if>
              </table>
            </td>
          </tr>
          <tr class="uportal-background-shadow">
            <td colspan="4" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
          <tr class="uportal-background-med">
            <td colspan="5" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
        </table>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
    <tr>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>

