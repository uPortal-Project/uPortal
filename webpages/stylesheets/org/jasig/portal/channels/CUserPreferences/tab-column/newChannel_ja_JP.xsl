<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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

Author: Jultin Tilton, jet@immagic.com
$Revision$
-->
  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="catID">top</xsl:param>
  <xsl:param name="errorID">no parameter passed</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences/tab-column</xsl:variable>

  <xsl:template match="/">
    <!--    $activeTab:<xsl:value-of select="$activeTab"/><br/>
    $action:<xsl:value-of select="$action"/><br/>
    $position:<xsl:value-of select="$position"/><br/>
    $elementID:<xsl:value-of select="$elementID"/><br/>-->
    <!--Begin top table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-content">
      <tr class="uportal-background-light">
        <td class="uportal-channel-text">
          <p>
            <span class="uportal-channel-subtitle-reversed">新しいチャネルを追加するステップ：</span>
          </p>
          <!--Begin Steps table -->
          <table width="100%" border="0" class="uportal-channel-text">
            <xsl:choose>
              <xsl:when test="//registry">
                <tr>
                  <td align="left" valign="top">
                    <table width="100%" border="0" class="uportal-channel-text">
                      <tr valign="top">
                        <td>
                          <strong>1.</strong>
                        </td>
                        <td width="100%">カテゴリを選択しブラウズ：</td>
                      </tr>
                    </table>
                    <!--Category Selection Table -->
                    <xsl:choose>
                      <xsl:when test="$catID = 'top' or $catID = 'all'">
                        <table width="100%" border="0">
                          <form name="formSelectCategory" method="post" action="{$baseActionURL}">
                            <input type="hidden" name="action" value="newChannel"/>
                            <tr>
                              <td nowrap="nowrap" align="left" valign="top">
                                <img alt="" src="{$mediaPath}/transparent.gif" width="16" height="16"/>
                                <img alt="right arrow" src="{$mediaPath}/arrow_right_image.gif" width="16" height="16"/>
                                <select name="selectedCategory" class="uportal-input-text">
                                  <xsl:for-each select="/registry/category">
                                    <xsl:sort select="@name"/>
                                    <option value="{@ID}">
                                      <xsl:value-of select="@name"/>
                                    </option>
                                  </xsl:for-each>
                                  <option value=" ">__________</option>
                                  <xsl:choose>
                                    <xsl:when test="$catID = 'all'">
                                      <option value="all" selected="selected">すべて選択</option>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <option value="all">すべて選択</option>
                                      <option value=" " selected="selected"/>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </select>
                                <input type="submit" name="selectCategory" value="go" class="uportal-button"/>
                              </td>
                            </tr>
                          </form>
                        </table>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:for-each select="/registry//category[@ID=$catID]">
                          <xsl:for-each select="ancestor-or-self::category">
                            <table width="100%" border="0">
                              <form name="formSelectCategory" method="post" action="{$baseActionURL}">
                                <input type="hidden" name="action" value="newChannel"/>
                                <tr>
                                  <td nowrap="nowrap" align="left" valign="top">
                                    <img alt="" src="{$mediaPath}/transparent.gif" height="16">
                                      <xsl:attribute name="width">
                                        <xsl:value-of select="(count(ancestor::category)+1)*16"/>
                                      </xsl:attribute>
                                    </img>
                                    <xsl:choose>
                                      <xsl:when test="position() = last()">
                                        <img alt="right arrow" src="{$mediaPath}/arrow_right_image.gif" width="16" height="16"/>
                                      </xsl:when>
                                      <xsl:otherwise>
                                        <img alt="down arrow" src="{$mediaPath}/arrow_down_image.gif" width="16" height="16"/>
                                      </xsl:otherwise>
                                    </xsl:choose>
                                    <select name="selectedCategory" class="uportal-input-text">
                                      <xsl:for-each select="ancestor::*[1]/category">
                                        <xsl:sort select="@name"/>
                                        <option value="{@ID}">
                                          <xsl:if test="@ID=$catID or descendant::category[@ID=$catID]">
                                            <xsl:attribute name="selected">selected</xsl:attribute>
                                          </xsl:if>
                                          <xsl:value-of select="@name"/>
                                          <!--[subcategories:<xsl:value-of select="count(descendant::category)"/>, total channels:<xsl:value-of select="count(descendant::channel)"/>-->
                                        </option>
                                      </xsl:for-each>
                                      <xsl:if test="position() = 1">
                                        <option value=" ">_____________</option>
                                        <option value="all">すべて選択</option>
                                      </xsl:if>
                                    </select>
                                    <input type="submit" name="selectCategory" value="go" class="uportal-button"/>
                                  </td>
                                </tr>
                              </form>
                            </table>
                          </xsl:for-each>
                          <xsl:if test="child::category">
                            <table width="100%" border="0" class="uportal-channel-text">
                              <tr>
                                <td colspan="2">
                                  <hr/>
                                </td>
                              </tr>
                              <tr valign="top">
                                <td>
                                  <strong>1a.</strong>
                                </td>
                                <td width="100%">サブカテゴリを選択 of "<xsl:value-of select="//category[@ID=$catID]/@name"/>" or select a channel from step 2:</td>
                              </tr>
                            </table>
                            <table width="100%" border="0">
                              <form name="formSelectCategory" method="post" action="{$baseActionURL}">
                                <input type="hidden" name="action" value="newChannel"/>
                                <tr>
                                  <td nowrap="nowrap" align="left" valign="top">
                                    <img alt="" src="{$mediaPath}/transparent.gif" height="16" width="16"/>
                                    <select name="selectedCategory" class="uportal-input-text">
                                      <xsl:for-each select="category">
                                        <xsl:sort select="@name"/>
                                        <option value="{@ID}">
                                          <xsl:value-of select="@name"/>
                                          <!--[subcategories:<xsl:value-of select="count(descendant::category)"/>, total channels:<xsl:value-of select="count(descendant::channel)"/>-->
                                        </option>
                                      </xsl:for-each>
                                      <option value=" ">____________________</option>
                                      <option value=" " selected="selected">サブカテゴリを選択</option>
                                    </select>
                                    <input type="submit" name="selectCategory" value="go" class="uportal-button"/>
                                  </td>
                                </tr>
                              </form>
                            </table>
                          </xsl:if>
                        </xsl:for-each>
                      </xsl:otherwise>
                    </xsl:choose>
                    <!--End Category Selection Table -->
                  </td>
                  <td>
                    <img alt="" src="{$mediaPath}/transparent.gif" width="32" height="16"/>
                  </td>
                  <td width="100%">
                    <xsl:if test="$catID != 'top'">
                      <table width="100%" border="0" class="uportal-channel-text">
                        <form name="formSelectChannel" method="post" action="{$baseActionURL}">
                          <input type="hidden" name="action" value="newChannel"/>
                          <tr valign="top">
                            <td>
                              <strong>2.</strong>
                            </td>
                            <td width="100%">Select a channel<xsl:choose>
                                <xsl:when test="$catID = 'all'"> from "All categories"</xsl:when>
                                <xsl:otherwise> from the "<xsl:value-of select="//category[@ID=$catID]/@name"/>" category
                                <br/><span class="uportal-text-small">Description: <xsl:value-of select="//category[@ID=$catID]/@description"/></span>
                                </xsl:otherwise></xsl:choose>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <img alt="" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                            </td>
                            <!--Begin Channel Listing -->
                            <td width="100%">
                              <select name="selectedChannel" size="5" class="uportal-input-text">
                                <xsl:choose>
                                  <xsl:when test="$catID = 'all'">
                                    <xsl:for-each select="/registry//channel[not(@ID=following::channel/@ID)]">
                                      <xsl:sort select="@name"/>
                                      <option value="{@ID}">
                                        <xsl:value-of select="@name"/>
                                      </option>
                                    </xsl:for-each>
                                    <option>
                                      <xsl:if test="not(/registry//channel[not(@ID=following::channel/@ID)])">--This category contains no channels--</xsl:if>
                                    </option>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    <xsl:for-each select="/registry//category[@ID=$catID]/channel">
                                      <xsl:sort select="@name"/>
                                      <option value="{@ID}">
                                        <xsl:value-of select="@name"/>
                                      </option>
                                    </xsl:for-each>
                                    <option>
                                      <xsl:if test="not(/registry//category[@ID=$catID]/channel)">--This category contains no channels--</xsl:if>
                                    </option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select>
                            </td>
                            <!--End Channel Listing -->
                          </tr>
                          <!-- Put this back when implementing "channel preview"
                          <tr valign="top">
                            <td>
                              <strong>3.</strong>
                            </td>
                            <td>Get more informaton about the selected channel:<input type="submit" name="channelMoreInfo" value="?" class="uportal-button"/> [optional]</td>
                          </tr>
                          -->
                          <tr valign="top">
                            <td>
                              <strong>3.</strong>
                            </td>
                            <td>選択されたチャネルを追加：<input type="submit" name="addChannel" value="追加" class="uportal-button"/></td>
                          </tr>
                        </form>
                      </table>
                    </xsl:if>
                  </td>
                </tr>
              </xsl:when>
              <xsl:otherwise>
                <tr>
                  <td colspan="3">
                    <hr/>
                  </td>
                </tr>
                <tr>
                  <td colspan="3" class="uportal-channel-warning">
                    <b>この時点ではチャネルレジストリデータが利用可能ではありません...</b>
                  </td>
                </tr>
              </xsl:otherwise>
            </xsl:choose>
            <tr>
              <td colspan="3">
                <hr/>
              </td>
            </tr>
            <tr>
              <td colspan="3">
                <img alt="bullet point" src="{$mediaPath}/bullet.gif" width="16" height="16"/>
                <a href="{$baseActionURL}?action=cancel">キャンセルして戻る</a>
              </td>
            </tr>
          </table>
          <!--End Steps Table -->
        </td>
      </tr>
    </table>
    <!--End top Table -->
  </xsl:template>
</xsl:stylesheet>






<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
