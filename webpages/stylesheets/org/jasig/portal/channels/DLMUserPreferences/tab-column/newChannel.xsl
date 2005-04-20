<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
  <xsl:output indent="no" method="html"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="catID">top</xsl:param>
  <xsl:param name="errorID">no parameter passed</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/DLMUserPreferences/tab-column</xsl:variable>

<xsl:template match="/">
    <!--    $activeTab:<xsl:value-of select="$activeTab"/><br/>
    $action:<xsl:value-of select="$action"/><br/>
    $position:<xsl:value-of select="$position"/><br/>
    $elementID:<xsl:value-of select="$elementID"/><br/>-->
    <!--Begin top table -->

<script language="JavaScript">
var hasSubs = false;
</script>

<table border="0" cellpadding="10" cellspacing="0" class="uportal-background-content" width="100%">
   <tr class="uportal-background-light">
        <td class="uportal-channel-text">
        <span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$STEPS_FOR_ADDING_A_NEW_CHANNEL"/></span><br/>
        <!--Begin Steps table -->
        <table border="0" cellpadding="0" cellspacing="7" width="100%">
            <xsl:choose>
              <xsl:when test="//registry">
           <tr>
                <td align="left" valign="top">
                <table border="0" cellpadding="2" cellspacing="0">
                   <tr valign="top">
                        <td class="uportal-text12-bold">1.</td>
                        <td nowrap="nowrap"><span class="uportal-channel-text"><xsl:value-of select="$SELECT_A_CATEGORY"/></span></td>
                   </tr>
                </table>

                    <!--Category Selection Table -->
                    <xsl:choose>
                      <xsl:when test="$catID = 'top' or $catID = 'all'">

                <table border="0" cellpadding="2" cellspacing="0">
                <form action="{$baseActionURL}" method="post" name="formSelectCategory">
                            <input name="action" type="hidden" value="newChannel"/>
                   <tr>
                        <td nowrap="nowrap">
                                <img alt="" height="16" src="{$mediaPath}/transparent.gif" width="16"/>
                                <img alt="" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_ARROW_RIGHT_IMAGE_GIF"/></xsl:attribute></img>
                        </td>
                        <td nowrap="nowrap">
                           <span class="uportal-text-small">
                                <select class="uportal-input-text" name="selectedCategory">
                                  <xsl:for-each select="/registry/category">
                                    <xsl:sort select="@name"/>
                                    <option value="{@ID}">
                                      <xsl:value-of select="@name"/>
                                    </option>
                                  </xsl:for-each>
                                    <option value=" ">-------------------</option>
                                  <xsl:choose>
                                    <xsl:when test="$catID = 'all'">
                                      <option value="all" selected="selected"><xsl:value-of select="$SELECT_ALL"/></option>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <option value="all"><xsl:value-of select="$SELECT_ALL"/></option>
                                      <option value=" " selected="selected"/>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </select>
                           </span>
                        </td>
                        <td nowrap="nowrap">
                                <input class="uportal-input-text" name="selectCategory" type="submit"><xsl:attribute name="value"><xsl:value-of select="$GO"/></xsl:attribute></input>
                        </td>
                   </tr>
                </form>
                </table>
                      </xsl:when>
                      <xsl:otherwise>
                <table border="0" cellpadding="0" cellspacing="0">
                        <xsl:for-each select="/registry//category[@ID=$catID]">
                        <tr><td valign="top">
                          <xsl:for-each select="ancestor-or-self::category">
                <table border="0" cellpadding="2" cellspacing="0">
                <form name="formSelectCategory" method="post" action="{$baseActionURL}">
                                <input name="action" type="hidden" value="newChannel"/>
                   <tr>
                        <td nowrap="nowrap">
                            <img alt="" height="16" src="{$mediaPath}/transparent.gif">
                              <xsl:attribute name="width">
                                <xsl:value-of select="(count(ancestor::category)+1)*16"/>
                              </xsl:attribute>
                            </img>
                            <xsl:choose>
                              <xsl:when test="position() = last()">
                                <img alt="interface image" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_ARROW_RIGHT_IMAGE_GIF"/></xsl:attribute></img>
                              </xsl:when>
                              <xsl:otherwise>
                                <img alt="interface image" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_ARROW_DOWN_IMAGE_GIF"/></xsl:attribute></img>
                              </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td nowrap="nowrap">
                            <span class="uportal-text-small">
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
                                <option value=" ">--------------------</option>
                                <option value="all"><xsl:value-of select="$SELECT_ALL"/></option>
                              </xsl:if>
                            </select>
                            </span>
                        </td>
                        <td nowrap="nowrap">
                             <input class="uportal-input-text" name="selectCategory" type="submit"><xsl:attribute name="value"><xsl:value-of select="$GO"/></xsl:attribute></input>
                        </td>
                   </tr>
                </form>
                </table>

                          </xsl:for-each>
                </td></tr>
                          <xsl:if test="child::category">

                
                <tr><td class="uportal-background-med" valign="top">
                
                <script language="JavaScript">
		 hasSubs = true;
		</script>
                <table border="0" cellpadding="2" cellspacing="0" width="100%">
                   <tr><td class="uportal-background-light" colspan="3"><img alt="" height="1" src="{$mediaPath}/transparent.gif" width="16"/></td></tr>
                   <tr valign="top">
                        <td class="uportal-background-light"><img alt="" height="16" src="{$mediaPath}/transparent.gif" width="16"/></td>
                        <td class="uportal-text12-bold">1a.</td>
                        <td nowrap="nowrap" width="100%">
                        <span class="uportal-channel-text"><xsl:value-of select="$SELECT_A_SUBCATEGORY"/></span><br/>
                        <span ID="selected_cat" class="uportal-text-small"><xsl:value-of select="$SELECT_A_SUBCATEGORY_FROM_"/><xsl:value-of select="//category[@ID=$catID]/@name"/>".<br/>
                           <b><xsl:value-of select="$_OR_"/></b><br/><xsl:value-of select="$SELECT_A_CHANNEL_FROM_STEP_2_"/></span></td>
                   </tr>
                </table>
                <table border="0" cellpadding="2" cellspacing="0">
                <form action="{$baseActionURL}" method="post" name="formSelectCategory">
                <input name="action" type="hidden" value="newChannel"/>
                   <tr>
                        <td class="uportal-background-light"><img alt="" height="16" src="{$mediaPath}/transparent.gif" width="16"/></td>
                        <td nowrap="nowrap">
                            <img alt="" height="16" src="{$mediaPath}/transparent.gif" width="23"/>
                            <img alt="" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_ARROW_RIGHT_IMAGE_GIF"/></xsl:attribute></img>
                        </td>
                        <td align="left" nowrap="nowrap" valign="top">
                            <span class="uportal-text-small">
                            <select name="selectedCategory" class="uportal-input-text">
                              <xsl:for-each select="category">
                                <xsl:sort select="@name"/>
                                <option value="{@ID}">
                                  <xsl:value-of select="@name"/>
                                  <!--[subcategories:<xsl:value-of select="count(descendant::category)"/>, total channels:<xsl:value-of select="count(descendant::channel)"/>-->
                                </option>
                              </xsl:for-each>
                                <option value=" ">--------------------</option>
                                <option value=" " selected="selected"><xsl:value-of select="$SELECT_SUBCATEGORY_"/></option>
                            </select>
                            </span>
                        </td>
                        <td nowrap="nowrap">
                             <input class="uportal-input-text" name="selectCategory" type="submit"><xsl:attribute name="value"><xsl:value-of select="$GO"/></xsl:attribute></input>
                        </td>
                   </tr>
                </form>
                </table>
                </td></tr>
                          </xsl:if>
                        </xsl:for-each>
             
          </table>
                      </xsl:otherwise>
                    </xsl:choose>
                    <!--End Category Selection Table -->
                </td>
                <td><img alt="" height="1" src="{$mediaPath}/transparent.gif" width="20"/></td>
                <td valign="top" width="100%">

                    <xsl:if test="$catID != 'top'">

                <table border="0" cellpadding="2" cellspacing="0" width="100%">
                <form action="{$baseActionURL}" method="post" name="formSelectChannel">
                <input name="action" type="hidden" value="newChannel"/>
                   <tr valign="top">
                        <td class="uportal-text12-bold">2.</td>
                        <td colspan="2" width="100%">
                                <span class="uportal-channel-text"><xsl:value-of select="$SELECT_A_CHANNEL"/></span><br/>
                                <span ID="desc" class="uportal-text-small"><xsl:value-of select="$CATEGORY"/><b>
                                <xsl:choose>
                                  <xsl:when test="$catID = 'all'"><xsl:value-of select="$ALL"/></xsl:when>
                                  <xsl:otherwise>
                                    <xsl:value-of select="//category[@ID=$catID]/@description"/>
                                    <script language="JavaScript">
	                              if (hasSubs)
	                               { document.writeln('**'); }
	                            </script>
                                  </xsl:otherwise>
                                </xsl:choose>
                                </b>
                                </span>
                                <!-- ORIGINAL SELECT CHANNEL HEADER
                                <span class="uportal-channel-text">Select a channel<xsl:choose>
                                <xsl:when test="$catID = 'all'"> from "All categories"</xsl:when>
                                <xsl:otherwise> from the "<xsl:value-of select="//category[@ID=$catID]/@name"/>" category
                                <br/><span class="uportal-text-small" ID="desc">Description: <xsl:value-of select="//category[@ID=$catID]/@description"/></span>
                                </xsl:otherwise></xsl:choose>
                                </span>
                                -->
                        </td>
                   </tr>
                   <tr>
                        <td><img alt="" height="1" src="{$mediaPath}/transparent.gif" width="1"/></td>
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
                                      <xsl:if test="not(/registry//channel[not(@ID=following::channel/@ID)])"><xsl:value-of select="$THIS_CATEGORY_CONTAINS_NO_CHANNELS"/></xsl:if>
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
                                      <xsl:if test="not(/registry//category[@ID=$catID]/channel)"><xsl:value-of select="$THIS_CATEGORY_CONTAINS_NO_CHANNELS"/></xsl:if>
                                    </option>
                                  </xsl:otherwise>
                                </xsl:choose>
                              </select><br/>
                              <script language="JavaScript">
                                if (hasSubs)
                                 { document.writeln('<span class="uportal-text-small"><xsl:value-of select="$LIST_DOES_NOT_INCLUDE_CHANNELS_IN_SUBCATEGORIES"/></span>'); }
                              </script>
                        </td>
                            <!--End Channel Listing -->
                   </tr>
<!-- Put this back when implementing "channel preview"
                          <tr valign="top">
                            <td><strong>3.</strong></td>
                            <td>Get more informaton about the selected channel:<input type="submit" name="channelMoreInfo" value="?"/> [optional]</td>
                          </tr>
-->
                   <tr><td colspan="3"> </td></tr>

                   <tr>
                        <td><span class="uportal-text12-bold">3.</span></td>
                        <td nowrap="nowrap"><span class="uportal-channel-text"><xsl:value-of select="$ADD_THE_SELECTED_CHANNEL"/></span></td>
                        <td width="100%"><input class="uportal-input-text" name="addChannel" type="submit"><xsl:attribute name="value"><xsl:value-of select="$ADD_CHANNEL"/></xsl:attribute></input></td>
                   </tr>
                </form>
                </table>
                    </xsl:if>
                </td>
           </tr>
              </xsl:when>
              <xsl:otherwise>

           <tr><td colspan="3"><hr SIZE="1"/></td></tr>
           <tr><td class="uportal-channel-warning" colspan="3"><b><xsl:value-of select="$NO_CHANNEL_REGISTRY_DATA_IS_AVAILABLE_AT_THIS_TIME___"/></b></td></tr>

              </xsl:otherwise>
            </xsl:choose>

           <tr><td colspan="3"><hr SIZE="1"/></td></tr>
           <tr><td class="uportal-channel-text" colspan="3"><a href="{$baseActionURL}?action=cancel"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></td></tr>
        </table>
          <!--End Steps Table -->
        </td>
      </tr>
    </table>
    <!--End top Table -->
    <SCRIPT LANGUAGE="JavaScript">
      helpTag = "prtl.chan.new";
    </SCRIPT>
  </xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->