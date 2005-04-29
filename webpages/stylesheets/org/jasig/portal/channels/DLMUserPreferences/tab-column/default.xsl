<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><!--
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
--><xsl:stylesheet version="1.0" xmlns:cp="http://www.campuspipeline.com" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="no" method="html"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="activeTab">1</xsl:param>
  <xsl:param name="action">no parameter passed</xsl:param>
  <xsl:param name="position">no parameter passed</xsl:param>
  <xsl:param name="elementID">no parameter passed</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="showLockUnlock">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="cpSetTimeout"/>
  <xsl:param name="cpSetPassword"/>
  <xsl:param name="protocolHostPrefixSecure"/>

  <xsl:variable name="activeTabIdx">
    <!-- if the activeTab is a number then it is the active tab index -->
    <!-- otherwise it is the ID of the active tab. If it is the ID -->
    <!-- then check to see if that tab is still in the layout and -->
    <!-- if so use its index. if not then default to an index of 1. -->

   <xsl:choose>
    <xsl:when test="string( number( $activeTab ) )='NaN'">

     <xsl:choose>
      
      <!-- this is the when before UP 2.3.2.  Changed because of the change of activeTabID path
           and the fact that this when is not in the orig UP code
      <xsl:when test="/layout/folder[@ID=$activeTab and
                                     @type='regular'and
                                     @hidden='false']">
      end orig when -->
      
      <xsl:when test="/layout/folder/folder[@ID=$activeTab and @type='regular'and @hidden='false']">
       <xsl:value-of select="count(/layout/folder/folder[@ID=$activeTab]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
      </xsl:when>
      <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
     </xsl:choose>

    </xsl:when>
	
    <xsl:otherwise> <!-- it is a number and hence an index, so use it -->
     <xsl:value-of select="$activeTab"/>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="activeTabID" select="/layout/folder/folder[@type='regular' and @hidden='false'][position() = $activeTabIdx]/@ID"/>

  <xsl:variable name='IMAGE_test'><xsl:value-of select="$mediaPath"/>/newchan/newchannel.gif</xsl:variable>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/DLMUserPreferences/tab-column</xsl:variable>

<xsl:template match="layout">

  <xsl:for-each select="folder[@type='root']"> <!-- ADDED FROM UP 232 -->
  
    <xsl:call-template name="optionMenu"/>
    <br mediaPath='mboyd the imageTest is {$IMAGE_test} this value.'/>
    
   <table border="0" cellpadding="20" cellspacing="0" class="uportal-background-dark" summary="add summary" width="100%">
       <tr>
            <td>
     <xsl:call-template name="tabRow"/>
            
    <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-content" summary="add summary" width="100%">
       <tr>
            <td>
                <xsl:call-template name="contentRow"/>
            </td>
       </tr>
    </table>
           </td>
       </tr>
    </table>
    <br/>

  </xsl:for-each> <!-- ADDED FROM UP 232 -->

</xsl:template>

<xsl:template name="tabRow">

<!-- DEBUG CODE
<table summary="add summary" border="0" cellspacing="0" cellpadding="0" width="100%">
<xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
    <xsl:for-each select="attribute::*">
    <tr>
        <td><xsl:value-of select="name()"/> = <xsl:value-of select="."/></td>
    </tr>
    </xsl:for-each>
    <tr>
        <td><hr/></td>
    </tr>
</xsl:for-each>
</table>
END DEBUG CODE -->

<table ID="tabs_tda" border="0" cellpadding="0" cellspacing="0" summary="add summary" width="100%" class="uportal-background-dark">
   <tr>

    <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
    
        <xsl:variable name="tabClass">
            <xsl:choose>
                <xsl:when test="$activeTabIdx = position()">uportal-background-content</xsl:when>
                <xsl:otherwise>uportal-background-light</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="anchorClass">
            <xsl:choose>
                <xsl:when test="$activeTabIdx = position()">uportal-navigation-category-selected</xsl:when>
                <xsl:otherwise>uportal-text-small</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="fontClass">
            <xsl:choose>
                <xsl:when test="$activeTabIdx = position()">uportal-text-small</xsl:when>
                <xsl:otherwise>uportal-text-small</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="moveMethod">
            <xsl:choose>
                <xsl:when test="not(position() = (last()-1))">insertBefore_</xsl:when>
                <xsl:otherwise>appendAfter_</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="destTabId">
            <xsl:choose>
                <xsl:when test="not(position() = (last()-1))"><xsl:value-of select="following-sibling::folder[@type='regular' and @hidden='false'][2]/@ID"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="following-sibling::folder[@type='regular' and @hidden='false'][1]/@ID"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="precedingTab" select="preceding-sibling::*[@type = 'regular' and @hidden = 'false'][1]"/>
        <xsl:variable name="followingTab" select="following-sibling::*[@type = 'regular' and @hidden = 'false'][1]"/>

        <td class="{$tabClass}" nowrap="nowrap">        
        <xsl:choose>

<!--<xsl:when test="ancestor-or-self::*[@immutable='true']">
<img alt="This tab is locked" title="This tab is locked" src="{$mediaPath}/lock.gif" width="16" height="16" border="0"/>
</xsl:when>-->

            <xsl:when test="not(position()=1)">            
                <xsl:choose>                    
                    <xsl:when test="(
                                        not(@cp:moveAllowed = 'false') 
                                        and 
                                        (
                                            not($precedingTab/@cp:moveAllowed = 'false') 
                                            or
                                            (
                                                $precedingTab/@cp:moveAllowed = 'false'
                                                and
                                                $precedingTab/@cp:precedence &lt; self::node()/@cp:precedence
                                            )
                                        )
                                    )
                                    or
                                    (
                                        @cp:moveAllowed = 'false' 
                                        and 
                                        $precedingTab/@cp:precedence &gt; self::node()/@cp:precedence
                                        and
                                        not($precedingTab/@cp:moveAllowed = 'false') 
                                    )
                                    and
                                    (
                                        not(@cp:moveAllowed = 'false' )
                                    )">

                                   <a href="{$baseActionURL}?action=moveTab&amp;elementID={@ID}&amp;method_ID=insertBefore_{preceding-sibling::folder[@type='regular' and @hidden='false'][1]/@ID}" onMouseover="window.status=''; return true;">
                                   <img border="0" height="16" hspace="3" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_LEFT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THIS_TAB_LEFT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_TAB_LEFT"/></xsl:attribute></img>
                                   </a>

                    </xsl:when>
                    <xsl:otherwise>
                        <img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <xsl:otherwise>
                <img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
            </xsl:otherwise>
        </xsl:choose>

        </td>
        <td class="{$tabClass}" nowrap="nowrap">
            <A ID="tab" onMouseover="window.status=''; return true;" class="{$anchorClass}">
        <xsl:choose>
            <xsl:when test="$action = 'moveColumn' or $action = 'moveChannel'">
                <xsl:attribute name="href">
                    <xsl:value-of select="$baseActionURL"/>?action=<xsl:value-of select="$action"/>&amp;activeTab=<xsl:value-of select="$activeTabID"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="href">
                    <xsl:value-of select="$baseActionURL"/>?action=selectTab&amp;activeTab=<xsl:value-of select="@ID"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
            <SPAN CLASS="{$fontClass}">
        <xsl:value-of select="@name"/>
            </SPAN>
            </A>
        </td>
        <td class="{$tabClass}" nowrap="nowrap">

        <xsl:choose>
            <xsl:when test="not(position()=last())">
    
                <xsl:choose>
    
<!--<xsl:when test="ancestor-or-self::*[@immutable='true']">
<img alt="This tab is locked" title="This tab is locked" src="{$mediaPath}/lock.gif" width="16" height="16" border="0"/>
</xsl:when>-->
    
                    <xsl:when test="(
                                        not(@cp:moveAllowed = 'false') 
                                        and 
                                        (
                                            not($followingTab/@cp:moveAllowed = 'false') 
                                            or
                                            (
                                                $followingTab/@cp:moveAllowed = 'false' 
                                                and 
                                                $followingTab/@cp:precedence &lt; self::node()/@cp:precedence
                                            )
                                        )
                                    )
                                    or
                                    (
                                        @cp:moveAllowed = 'false' 
                                        and 
                                        $followingTab/@cp:precedence &gt; self::node()/@cp:precedence
                                    )
                                    and
                                    (
                                        not(@cp:moveAllowed = 'false')
                                    )
                                    ">

                                    <a href="{$baseActionURL}?action=moveTab&amp;elementID={@ID}&amp;method_ID={$moveMethod}{$destTabId}" onMouseover="window.status=''; return true;">
                                    <img border="0" height="16" hspace="3" width="16"><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THIS_TAB_RIGHT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_TAB_RIGHT"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_GIF"/></xsl:attribute></img>
                                    </a>
                    </xsl:when>    
                    <xsl:otherwise> 
                        <img height="16" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                    </xsl:otherwise>
    
                </xsl:choose>
    
            </xsl:when>
    
            <xsl:otherwise>
                <img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
            </xsl:otherwise>
        </xsl:choose>
        </td>
        <td><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>

    </xsl:for-each>

    <xsl:choose>
        <xsl:when test="$action = 'newTab'">
        <td nowrap="nowrap" class="uportal-background-highlight">
              <img height="16" hspace="5" vspace="3" width="86"><xsl:attribute name="alt"><xsl:value-of select="$NEW_TAB"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$NEW_TAB"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWTAB_GIF"/></xsl:attribute></img>
        </td>
        </xsl:when>
        <xsl:otherwise>
            <xsl:if test="not($action='moveColumn' or $action='moveChannel')">
              <td>
                <a href="{$baseActionURL}?action=newTab" onMouseover="window.status=''; return true;">
                  <img border="0" height="16" hspace="5" vspace="3" width="86"><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_TAB"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_TAB"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWTAB_GIF"/></xsl:attribute></img>
                </a>
              </td>
            </xsl:if>
        </xsl:otherwise>
    </xsl:choose>

        <td width="100%"><img alt="" height="20" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
      </tr>
</table>
</xsl:template>


<xsl:template name="contentRow">
<table border="0" cellpadding="0" cellspacing="0" class="uportal-background-content" width="100%">
      <xsl:call-template name="controlRow"/>
   <tr>
        <xsl:choose>
          <xsl:when test="/layout/folder/folder[attribute::ID=$activeTabID]/folder">
            <xsl:for-each select="/layout/folder/folder[attribute::ID=$activeTabID]/descendant::folder">
            
              <xsl:call-template name="contentColumns"/>

              <xsl:if test="position()=last()">

                <xsl:call-template name="closeContentRow"/>

              </xsl:if>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="/layout/folder/folder[attribute::ID=$activeTabID]">
              <xsl:call-template name="noColumnsView"/>
              <xsl:call-template name="closeContentRow"/>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
  </tr>
        <xsl:call-template name="controlRow"/>
</table>
</xsl:template>


<xsl:template name="controlRow">
<!--Begin Control Row -->
   <tr>
    <xsl:choose>
        <xsl:when test="/layout/folder/folder[attribute::ID=$activeTabID]/folder">
            <xsl:for-each select="/layout/folder/folder[attribute::ID=$activeTabID]/folder">

        <td width="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="20"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="10"><img alt="" height="20" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td WIDTH=""><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>

            </xsl:for-each>

        <td width="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="20"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="10"><img alt="" height="20" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>

        </xsl:when>
        <xsl:otherwise>

        <td width="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="20"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="10"><img alt="" height="20" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="100%"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td width="20"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td><img alt="" height="20" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>

        </xsl:otherwise>
    </xsl:choose>
   </tr>
<!--End Control Row -->
</xsl:template>


<xsl:template name="optionMenu">
<!--Begin Option Menu-->
<table myName="mboyd" border="0" cellpadding="10" cellspacing="0" class="uportal-background-content" width="100%">
      <xsl:if test="/layout[@cp:fragmentName]">
      <tr class="uportal-background-light">
        <td class="uportal-channel-title">
            <xsl:choose>               
                <xsl:when test="/layout/@cp:fragmentName = 'All Users'"><xsl:value-of select="$LAYOUT_FRAGMENT"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$LAYOUT_TEMPLATE"/></xsl:otherwise>
           </xsl:choose>

	  <xsl:value-of select="/layout/@cp:fragmentName"/>

        </td>
      </tr>
      </xsl:if>
      
    <xsl:if test="/layout[@cp:isTemplateUser]">
   <tr class="uportal-background-light">
        <td class="uportal-channel-title"><xsl:value-of select="$LAYOUT_TEMPLATE"/><xsl:value-of select="/layout/@cp:templateLoginID"/>
        </td>
   </tr>
    </xsl:if>

   <tr class="uportal-background-light">
        <td class="uportal-channel-text">
          <xsl:choose>
            <xsl:when test="$action='selectTab'">
              <xsl:call-template name="optionMenuModifyTab"/>
            </xsl:when>
            <xsl:when test="$action='selectColumn'">
              <xsl:call-template name="optionMenuModifyColumn"/>
            </xsl:when>
            <xsl:when test="$action='selectChannel'">
              <xsl:call-template name="optionMenuModifyChannel"/>
            </xsl:when>
            <xsl:when test="$action='newTab'">
              <xsl:call-template name="optionMenuNewTab"/>
            </xsl:when>
            <xsl:when test="$action='newColumn'">
              <xsl:call-template name="optionMenuNewColumn"/>
            </xsl:when>
            <xsl:when test="$action='moveColumn'">
              <xsl:call-template name="optionMenuMoveColumn"/>
            </xsl:when>
            <xsl:when test="$action='moveChannel'">
              <xsl:call-template name="optionMenuMoveChannel"/>
            </xsl:when>
            <xsl:when test="$action='error'">
              <xsl:call-template name="optionMenuError"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="optionMenuDefault"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
   </tr>
</table>
<!--End Option Menu-->
</xsl:template>


<xsl:template name="contentColumns">
        <td WIDTH="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>        
<xsl:call-template name="newColumn"/>
        <td WIDTH="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
<!--Begin Content Column -->
        <td align="center" valign="top">
    <xsl:if test="($action = 'selectColumn' or $action = 'moveColumn') and $elementID=@ID">
        <xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
    </xsl:if>
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
           <!--Begin [select Column]row -->

    <xsl:choose>
        <xsl:when test="@cp:moveAllowed = 'false'">
           <tr class="uportal-background-light"><td align="center" class="uportal-text-small" height="22"><xsl:value-of select="$COLUMN_IS_UNMOVABLE"/></td></tr>
        </xsl:when>
        <xsl:otherwise>
           <tr class="uportal-background-light"><td align="center" class="uportal-text-small" height="22"> </td></tr>
        </xsl:otherwise>
    </xsl:choose>

           <tr>
                <td align="center" class="uportal-background-light" nowrap="nowrap" width="100%">
    <xsl:choose>
        <xsl:when test="($action = 'selectColumn' or $action = 'moveColumn') and $elementID=@ID">
            <img alt="" height="20" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
        </xsl:when>
        
        <xsl:otherwise>
            <xsl:choose>
<!-- 
<xsl:when test="not(position()=1) and ancestor-or-self::*[@immutable='true']">
<img alt="This column is locked" title="This column is locked" src="{$mediaPath}/lock.gif" width="16" height="16" border="0"/>
</xsl:when> 
-->
                <xsl:when test="not(position()=1)">
                    <xsl:choose>
                        <xsl:when test="not(@cp:moveAllowed = 'false' or preceding-sibling::*[1][@cp:moveAllowed = 'false'])">
                        <a class="uportal-text-small" href="{$baseActionURL}?action=moveColumnHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={preceding-sibling::folder[@type='regular' and @hidden='false'][1]/@ID}" onMouseover="window.status=''; return true;">
                        <img HSPACE="3" VSPACE="2" border="0" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_LEFT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_LEFT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_LEFT"/></xsl:attribute></img>
                        </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="not(count(/layout/folder/folder[attribute::ID=$activeTabID]/descendant::folder) = 0)">
                <a href="{$baseActionURL}?action=selectColumn&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <img VSPACE="2" border="0" height="16" width="86"><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_SELECT_THIS_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_SELECT_THIS_COLUMN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_SELECTCOLUMN_GIF"/></xsl:attribute></img>
                </a>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="not(position()=last())">
                    <xsl:choose>
<!--
                        <xsl:when test="ancestor-or-self::*[@immutable='true']">
                            <img alt="This column is locked" title="This column is locked" src="{$mediaPath}/lock.gif" width="16" height="16"/>
                        </xsl:when>
-->
                        <xsl:when test="not(position() = (last()-1))">
                            <xsl:choose>
                            <xsl:when test="not(@cp:moveAllowed = 'false' or following-sibling::*[1][@cp:moveAllowed = 'false'])">
                                 <a class="uportal-text-small" href="{$baseActionURL}?action=moveColumnHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={following-sibling::folder[@type='regular' and @hidden='false'][2]/@ID}" onMouseover="window.status=''; return true;">
                                 <img HSPACE="3" VSPACE="2" border="0" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_RIGHT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_RIGHT"/></xsl:attribute></img>
                                 </a>
                            </xsl:when>    
                            <xsl:otherwise> 
                                <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                            </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>

                        <xsl:choose>
                            <xsl:when test="not(@cp:moveAllowed = 'false' or following-sibling::*[1][@cp:moveAllowed = 'false'])">
                                 <a class="uportal-text-small" href="{$baseActionURL}?action=moveColumnHere&amp;sourceID={@ID}&amp;method=appendAfter&amp;elementID={following-sibling::folder[@type='regular' and @hidden='false'][1]/@ID}" onMouseover="window.status=''; return true;">
                                 <img HSPACE="3" VSPACE="2" border="0" height="16" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_RIGHT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_COLUMN_RIGHT"/></xsl:attribute></img>
                                 </a>
                            </xsl:when>    
                            <xsl:otherwise> 
                                <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                            </xsl:otherwise>
                        </xsl:choose>

                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:otherwise>
                    <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                </xsl:otherwise>

            </xsl:choose>
        </xsl:otherwise>
    </xsl:choose>
                </td>
           </tr>
        <!--End [select Column] row -->
        </table>

    <xsl:choose>
        <xsl:when test="not(descendant::channel) and not(@cp:addChildAllowed = 'false')">

                <xsl:call-template name="newChannel"/>

        </xsl:when>
        <xsl:otherwise>
            <xsl:for-each select="descendant::channel">

                <xsl:if test="not(parent::folder/@cp:addChildAllowed = 'false' or self::*[@cp:moveAllowed = 'false'] or following-sibling::*[@hidden='false'][@cp:moveAllowed='false'])">
                    <xsl:call-template name="newChannel"/>
                </xsl:if>

                <xsl:choose>
                    <xsl:when test="$elementID = @ID and $action != 'newChannel'">
		    <TABLE ALIGN="CENTER" BORDER="0" CELLPADDING="0" CELLSPACING="0" ID="OuterTable" WIDTH="100%">
		      <TR ALIGN="CENTER" class="uportal-background-highlight">
		        <TD>
		        <xsl:call-template name="selectChannel"/>
		        </TD>
		      </TR>
		    </TABLE>
		    </xsl:when>
		    <xsl:otherwise>
		        <xsl:call-template name="selectChannel"/>
		    </xsl:otherwise>
		</xsl:choose>                

                <xsl:if test="position()=last()">
                    <xsl:choose>
                        <xsl:when test="not(parent::folder/@cp:addChildAllowed = 'false')">
                            <xsl:call-template name="closeContentColumn"/>
                        </xsl:when>
                        <xsl:otherwise>

        <table border="0" cellpadding="0" cellspacing="10" width="100%">
           <tr><td align="center" class="uportal-text-small"><xsl:value-of select="$NO_NEW_CHANNELS_ALLOWED_"/></td></tr>
        </table>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:if>
            </xsl:for-each>
        </xsl:otherwise>
    </xsl:choose>
    </td>
    <!--End Content Column -->
</xsl:template>

<xsl:template name="noColumnsView">
        <td WIDTH="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>        
<xsl:call-template name="newColumn"/>
        <td WIDTH="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
<!--Begin Content Column -->
        <td align="center" valign="top">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
           <!--Begin [select Column]row -->

           <tr class="uportal-background-light"><td align="center" class="uportal-text-small" height="22"><xsl:value-of select="$TAB_HAS_NO_COLUMNS"/></td></tr>

           <tr>
                <td align="center" class="uportal-background-light" nowrap="nowrap" width="100%">
                    <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>

                    <img alt="" height="16" hspace="3" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                </td>
           </tr>
        <!--End [select Column] row -->
        </table>

    <xsl:if test="not(descendant::channel) and                   not(@cp:addChildAllowed = 'false')">
         <xsl:call-template name="newChannel"/>
    </xsl:if>
    </td>
    <!--End No Columns -->
</xsl:template>

<xsl:template name="closeContentRow">
<!-- Close Content Row-->
        <td><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
    <xsl:choose>
        <xsl:when test="$action = 'newColumn' and $position='after'">
        <td ALIGN="CENTER" class="uportal-background-highlight" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=newColumn&amp;method=appendAfter&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0" height="96" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute></img>
          </a>
        </td>
        </xsl:when>
        <xsl:when test="$action = 'moveColumn' and not(@ID=$elementID)">
        <td ALIGN="CENTER" class="uportal-background-highlight" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=moveColumnHere&amp;method=appendAfter&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_MOVECOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_COLUMN_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_COLUMN_TO_THIS_LOCATION"/></xsl:attribute></img>
          </a>
        </td>
        </xsl:when>
        <xsl:when test="$action = 'moveColumn' and @ID=$elementID">
        <td class="uportal-background-light"><img alt="" height="20" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        </xsl:when>
        <xsl:otherwise>
            <xsl:choose>
                <xsl:when test="not(/layout/folder/folder[@ID=$activeTabID and @cp:addChildAllowed = 'false'])">
        <td ALIGN="CENTER" class="uportal-background-light" valign="top" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=newColumn&amp;method=appendAfter&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0" height="96" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute></img>
          </a>
        </td>
                </xsl:when>
                <xsl:otherwise>
        <td class="uportal-background-light" valign="top"><img alt="" height="100" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                </xsl:otherwise>
            </xsl:choose>        
        </xsl:otherwise>
    </xsl:choose>
    <td><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
    <!-- Close Content Row-->
</xsl:template>


<xsl:template name="controlColumn">
        <td WIDTH="10"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
</xsl:template>



<xsl:template name="newColumn">

    <xsl:choose>

        <xsl:when test="$action = 'newColumn' and $position='before' and $elementID=@ID">                
        <td ALIGN="CENTER" class="uportal-background-highlight" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=newColumn&amp;method=insertBefore&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0" height="96" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute></img>
          </a>
        </td>
        </xsl:when>

        <xsl:when test="$action = 'moveColumn' and not(@ID=$elementID  or preceding-sibling::folder[1]/@ID=$elementID )">
<!-- or following-sibling::*[position() < follow-sibling::*[@ID=$elementID]/position()]  )"> -->
            <xsl:choose>

                <xsl:when test="not(@cp:moveAllowed = 'false' or following-sibling::folder[@hidden='false'][@cp:moveAllowed='false'])">
                
        <td ALIGN="CENTER" class="uportal-background-highlight" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=moveColumnHere&amp;method=insertBefore&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_MOVECOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_COLUMN_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_COLUMN_TO_THIS_LOCATION"/></xsl:attribute></img>
          </a>
        </td>
                </xsl:when>

                <xsl:otherwise>
        <td class="uportal-background-light" valign="top" width="20"><img alt="" height="20" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:when>

        <xsl:when test="$action = 'moveColumn' and (@ID=$elementID or preceding-sibling::folder[1]/@ID=$elementID)">
        <td class="uportal-background-light" valign="top" width="20"><img alt="" height="20" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        </xsl:when>

        <xsl:otherwise>

            <xsl:choose>
                <xsl:when test="not(@cp:moveAllowed = 'false' or following-sibling::folder[@hidden='false'][@cp:moveAllowed='false'] or /layout/folder/folder[@ID=$activeTabID][@cp:addChildAllowed = 'false'])">


        <td ALIGN="CENTER" class="uportal-background-light" valign="top" width="20">
          <a class="uportal-text-small" href="{$baseActionURL}?action=newColumn&amp;method=insertBefore&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
            <img border="0" height="96" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCOLUMN_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_COLUMN_HERE"/></xsl:attribute></img>
          </a>
        </td>

                </xsl:when>
                <xsl:otherwise>
        <td class="uportal-background-light" valign="top"><img alt="" height="100" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:otherwise>

    </xsl:choose>
</xsl:template>


<xsl:template name="newChannel">
    <!--Begin [new channel] Button -->
    <xsl:choose>      

        <xsl:when test="$action = 'newChannel' and $position='before' and $elementID=@ID">
          <TABLE ALIGN="CENTER" BORDER="0" CELLPADDING="0" CELLSPACING="10" WIDTH="100%">
	    <TR>
             <TD ALIGN="CENTER" class="uportal-background-highlight">
               <A CLASS="uportal-text-small" HREF="{$baseActionURL}?action=newChannel&amp;position=before&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <IMG BORDER="0" height="16" width="76"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCHANNEL_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute></IMG>
               </A>
             </TD>
            </TR>
          </TABLE>
        </xsl:when>

        <xsl:when test="$action = 'moveChannel' and not(@ID=$elementID or preceding-sibling::channel[1]/@ID=$elementID)">
          <TABLE ALIGN="CENTER" BORDER="0" CELLPADDING="0" CELLSPACING="10" WIDTH="100%">
	    <TR>
             <TD ALIGN="CENTER" class="uportal-background-highlight">
               <A CLASS="uportal-text-small" HREF="{$baseActionURL}?action=moveChannelHere&amp;method=insertBefore&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <IMG BORDER="0" HEIGHT="26" WIDTH="79"><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_CHANNEL_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_CHANNEL_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_MOVECHANNEL_GIF"/></xsl:attribute></IMG>
               </A>
             </TD>
            </TR>
          </TABLE>
        </xsl:when>
          
          
        <xsl:when test="$action = 'moveChannel' and (@ID=$elementID or preceding-sibling::channel[1]/@ID=$elementID)">
              <IMG HEIGHT="20" WIDTH="20" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></IMG>
        </xsl:when>

        <xsl:otherwise>            
              <A CLASS="uportal-text-small" HREF="{$baseActionURL}?action=newChannel&amp;position=before&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <IMG BORDER="0" HSPACE="10" VSPACE="10" height="16" width="76"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCHANNEL_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_HERE"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_HERE"/></xsl:attribute></IMG>
              </A>
        </xsl:otherwise>

    </xsl:choose>
    <!--End [new channel] Button -->
</xsl:template>


<xsl:template name="selectChannel">
    <!--Begin [select channel] Table -->

    <TABLE ALIGN="CENTER" BORDER="0" CELLPADDING="5" CELLSPACING="0" WIDTH="100%">
       <TR><TD ALIGN="CENTER"><IMG HEIGHT="1" WIDTH="1" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></IMG></TD></TR>
       <TR>
        <TD ALIGN="CENTER" CLASS="uportal-channel-text"><B>
        <A HREF="{$baseActionURL}?action=selectChannel&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                
                  <xsl:if test="@name = ''"><xsl:value-of select="$NO_CHANNEL_NAME"/></xsl:if>
                  <xsl:value-of select="@name"/>
                  
        </A>
        </B></TD>
       </TR>
    </TABLE>


	<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" HEIGHT="110" WIDTH="120">
	   <TR>
	        <TD style="background-image:url(media/org/jasig/portal/channels/CUserPreferences/tab-column/channel/chan_box_bg.gif)" HEIGHT="90" VALIGN="TOP">
	        <DIV ALIGN="RIGHT">
		<A HREF="{$baseActionURL}?action=selectChannel&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
		  <IMG BORDER="0" HEIGHT="16" VSPACE="2" WIDTH="16"><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_SELECT_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_SELECT_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_CHAN_SELECT_GIF"/></xsl:attribute></IMG>
		</A>
    <xsl:choose>
<!-- <xsl:when test="not(@unremovable='true') and not(ancestor-or-self::*[@immutable='true'])"> -->
        <xsl:when test="not(@cp:deleteAllowed = 'false')">
		<A HREF="{$baseActionURL}?action=deleteChannel&amp;elementID={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')" onMouseover="window.status=''; return true;">
		  <IMG BORDER="0" HEIGHT="16" HSPACE="2" VSPACE="2" WIDTH="16"><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_REMOVE_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_REMOVE_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_CHAN_REMOVE_GIF"/></xsl:attribute></IMG>
		</A>
        </xsl:when>
<!-- </xsl:when> -->
        <xsl:otherwise>
		<IMG BORDER="0" HEIGHT="16" HSPACE="2" VSPACE="2" WIDTH="16" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_CHAN_REMOVE_NA_GIF"/></xsl:attribute></IMG>
        </xsl:otherwise>
    </xsl:choose>
		</DIV>

		<DIV ALIGN="CENTER">
    <xsl:choose>
<!--
        <xsl:when test="not(position()=1) and ancestor-or-self::*[@immutable='true']">
		<IMG SRC="{$mediaPath}/transparent.gif" WIDTH="1" HEIGHT="9"/><BR/>
		<IMG ALT="This channel is locked" title="This channel is locked" src="{$mediaPath}/locked/chan03.gif" WIDTH="26" HEIGHT="25" BORDER="0" VSPACE="1"/>
        </xsl:when>
-->
        <xsl:when test="not(position()=1 or preceding-sibling::*[1][@cp:moveAllowed = 'false'] or self::*[@cp:moveAllowed = 'false'])">
		  <IMG HEIGHT="9" WIDTH="1" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></IMG><BR/>
		<A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={preceding-sibling::channel[not(@hidden='true')][1]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
		  <IMG BORDER="0" HEIGHT="16" VSPACE="1" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_UP_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_UP"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_UP"/></xsl:attribute></IMG>
		</A>
        </xsl:when>
        <xsl:otherwise>
		<IMG HEIGHT="9" WIDTH="1" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></IMG><BR/>
		<IMG HEIGHT="16" VSPACE="1" WIDTH="16" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_UP_NA_GIF"/></xsl:attribute></IMG>
        </xsl:otherwise>
    </xsl:choose>
		</DIV>

		<DIV>
    <xsl:choose>

<!--
        <xsl:when test="not(../../folder[1]/@ID = parent::folder/@ID) and ancestor-or-self::*[@immutable='true']">
		<IMG ALT="This channel is locked" title="This channel is locked" src="{$mediaPath}/locked/chan09.gif" WIDTH="24" HEIGHT="26" BORDER="0" VSPACE="1"/>
        </xsl:when>
-->

        <xsl:when test="                         not(                                ../../folder[1]/@ID = parent::folder/@ID                                 or                                 self::*[@cp:moveAllowed = 'false']                                 or                                parent::folder/preceding-sibling::folder[1][@cp:addChildAllowed = 'false']                                or                                following-sibling::*[@cp:moveAllowed = 'false']                                                            )">
            <xsl:choose>

               <xsl:when test="parent::folder/preceding-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/channel[1]/@ID">
		<A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=appendAfter&amp;elementID={parent::folder/preceding-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/channel[last()]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
		  <IMG BORDER="0" HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16"><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_BOTTOM_OF_THE_PREVIOUS_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_BOTTOM_OF_THE_PREVIOUS_COLUMN"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_LEFT_GIF"/></xsl:attribute></IMG>
		</A>
               </xsl:when>

               <xsl:otherwise>
		<A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={parent::folder/preceding-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
		  <IMG BORDER="0" HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_LEFT_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_TOP_OF_THE_PREVIOUS_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_TOP_OF_THE_PREVIOUS_COLUMN"/></xsl:attribute></IMG>
		</A>
               </xsl:otherwise>

            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
		<IMG HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_LEFT_NA_GIF"/></xsl:attribute></IMG>
        </xsl:otherwise>
    </xsl:choose>

		<IMG HEIGHT="16" VSPACE="1" WIDTH="50" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></IMG>

    <xsl:choose>
<!--
        <xsl:when test="not(../../folder[position()=last()]/@ID = parent::folder/@ID) and ancestor-or-self::*[@immutable='true']">
	        <img alt="This channel is locked" title="This channel is locked" src="{$mediaPath}/locked/chan11.gif" width="25" height="26" border="0" VSPACE="1"/>
        </xsl:when>
-->
        <xsl:when test="                        not(                               ../../folder[position()=last()]/@ID = parent::folder/@ID                               or                                self::*[@cp:moveAllowed = 'false']                                or                               parent::folder/following-sibling::folder[1][@cp:addChildAllowed = 'false']                               or                               following-sibling::*[@cp:moveAllowed = 'false']                            )">
                
            <xsl:choose>

               <xsl:when test="parent::folder/following-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/channel[1]/@ID">
	        <A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=appendAfter&amp;elementID={parent::folder/following-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/channel[last()]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
	          <IMG BORDER="0" HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16"><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_BOTTOM_OF_THE_NEXT_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_BOTTOM_OF_THE_NEXT_COLUMN"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_GIF"/></xsl:attribute></IMG>
	        </A>
               </xsl:when>

               <xsl:otherwise>
	        <A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={parent::folder/following-sibling::folder[not(@cp:addChildAllowed = 'false')][1]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
	          <IMG BORDER="0" HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_TOP_OF_THE_NEXT_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_TO_THE_TOP_OF_THE_NEXT_COLUMN"/></xsl:attribute></IMG>
	        </A>
               </xsl:otherwise>

            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
	        <IMG HEIGHT="16" HSPACE="9" VSPACE="1" WIDTH="16" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_RIGHT_NA_GIF"/></xsl:attribute></IMG>
        </xsl:otherwise>
    </xsl:choose>

	        </DIV>

	        <DIV ALIGN="CENTER">
    <xsl:choose>
<!--
        <xsl:when test="not(position()=last()) and ancestor-or-self::*[@immutable='true']">
	        <img alt="This channel is locked" title="This channel is locked" src="{$mediaPath}/locked/chan16.gif" width="26" height="24" border="0" VSPACE="1"/>
        </xsl:when>
-->
        <xsl:when test="not(position()=last() or following-sibling::*[1][@cp:moveAllowed = 'false'] or self::*[@cp:moveAllowed = 'false'])">
            <xsl:choose>
               <xsl:when test="not(position() = (last()-1))">
	        <A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=insertBefore&amp;elementID={following-sibling::channel[not(@hidden='true')][2]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
	          <IMG BORDER="0" HEIGHT="16" VSPACE="1" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_DOWN_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_DOWN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_DOWN"/></xsl:attribute></IMG>
	        </A>
               </xsl:when>
               <xsl:otherwise>
	        <A HREF="{$baseActionURL}?action=moveChannelHere&amp;sourceID={@ID}&amp;method=appendAfter&amp;elementID={following-sibling::channel[not(@hidden='true')][1]/@ID}" class="uportal-text-small" onMouseover="window.status=''; return true;">
	          <IMG BORDER="0" HEIGHT="16" VSPACE="1" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_DOWN_GIF"/></xsl:attribute><xsl:attribute name="ALT"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_DOWN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THIS_CHANNEL_DOWN"/></xsl:attribute></IMG>
	        </A>
               </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
	          <IMG HEIGHT="16" VSPACE="1" WIDTH="16" alt="" title=""><xsl:attribute name="SRC"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_CHANNEL_ARROW_DOWN_NA_GIF"/></xsl:attribute></IMG>
        </xsl:otherwise>
    </xsl:choose>

	        </DIV>
	        </TD>
	   </TR>
	   <TR><TD HEIGHT="20"><img height="20" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></TD></TR>
	</TABLE>

</xsl:template>


<xsl:template name="closeContentColumn">
    <!--Begin [new channel] Table -->
    <table border="0" cellpadding="0" cellspacing="10" width="100%">
      <tr align="center">
        <xsl:choose>
          <xsl:when test="$action = 'newChannel' and $position='after' and $elementID=@ID">
            <td class="uportal-background-highlight">
              <a class="uportal-text-small" href="{$baseActionURL}?action=newChannel&amp;position=after&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <img border="0" height="16" width="76"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCHANNEL_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute></img>
              </a>
            </td>
          </xsl:when>
          <xsl:when test="$action = 'moveChannel' and not(@ID=$elementID)">
            <td class="uportal-background-highlight">
              <a class="uportal-text-small" href="{$baseActionURL}?action=moveChannelHere&amp;method=appendAfter&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <img border="0"><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_CHANNEL_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_MOVE_THE_SELECTED_CHANNEL_TO_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_MOVECHANNEL_GIF"/></xsl:attribute></img>
              </a>
            </td>
          </xsl:when>
          <xsl:when test="$action = 'moveChannel' and @ID=$elementID">
            <td><img alt="" height="20" title="" width="20"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
          </xsl:when>
          <xsl:otherwise>
            <td>
              <a class="uportal-text-small" href="{$baseActionURL}?action=newChannel&amp;position=after&amp;elementID={@ID}" onMouseover="window.status=''; return true;">
                <img border="0" height="16" width="76"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_NEWCHANNEL_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CLICK_TO_ADD_A_NEW_CHANNEL_IN_THIS_LOCATION"/></xsl:attribute></img>
              </a>
            </td>
          </xsl:otherwise>
        </xsl:choose>
      </tr>
    </table>
    <!--End [new channel] Table -->
</xsl:template>


<xsl:template name="optionMenuDefault">
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MODIFYING_PREFERENCES"/></span>
<table border="0" cellpadding="2" cellspacing="7" width="100%">
   <tr>
        <td class="uportal-channel-text">Navigate to a tab, or select an element on the current tab by clicking one of the grey buttons below. For example, click one of the
        <img alt="New Channel" title="New Channel" width="76" height="16"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_NEWCHANNEL_GIF"/></xsl:attribute></img>
        <img alt="" title="" src="{$mediaPath}/transparent.gif" width="6" height="6"/>
         buttons to add a new channel in that location.</td>
   </tr>
   <tr><td><hr size="1"/></td></tr>
   
    <tr>
      <td class="uportal-channel-text"><a href="{$baseActionURL}?action=manageSkins" onMouseover="window.status=''; return true;"><img alt="" title="" src="{$mediaPath}/bullet.gif" width="9" height="9" hspace="7" border="0"/>Choose a skin</a></td>
    </tr>
   <tr>
    <td valign="top" class="uportal-channel-text">
      <img border="0" height="9" hspace="7" title="" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img>
      <a href="{$baseActionURL}?action=resetLayout" onClick="return confirm('You are about to replace your current layout with a default layout.  You cannot undo these changes.  Do you want to continue?')"><xsl:value-of select="$REVERT_TO_DEFAULT_LAYOUT"/></a>
    </td>
  </tr>
<!--
Disabled since DLM does not currently support multiple profiles. 
The challenge is in matching up the current profile being used by
a user with a profile for a fragment. So for now only a single 
profile is supported for fragments and users.

     <tr><td class="uportal-channel-text" width="100%"><a href="{$baseActionURL}?userPreferencesAction=manageProfiles" onMouseover="window.status=''; return true;"><img alt="Manage Profiles [advanced]" title="Manage Profiles [advanced]" src="{$mediaPath}/bullet.gif" width="9" height="9" hspace="7" border="0"/>Manage Profiles [advanced]</a></td></tr> 
-->
   
</table>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.main.layout";
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuModifyTab">
<!-- Begin Mod Tab Options -->
<xsl:variable name="tabName" select="/layout/folder/folder[@ID=$activeTabID]/@name"/>
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MODIFYING_THIS_TAB"/></span><br/>

<table border="0" cellpadding="1" cellspacing="7" width="100%">
<xsl:if test="not(/layout[@cp:fragmentName] or /layout[@cp:isTemplateUser])">
   <tr><td colspan="2" width="100%"><span class="uportal-channel-text"><a href="{$baseActionURL}?action=setActiveTab&amp;tab={$activeTabID}" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$MAKE_THIS_THE_DEFAULT_ACTIVE_TAB"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$MAKE_THIS_THE_DEFAULT_ACTIVE_TAB"/></xsl:attribute></img><xsl:value-of select="$MAKE_THIS_THE_DEFAULT_ACTIVE_TAB_"/></a></span></td></tr>
</xsl:if>

<!-- <xsl:if test="not(/layout/folder/folder[@ID=$activeTabID]/@immutable = 'true')"> -->

    <xsl:if test="not(/layout/folder/folder[@ID=$activeTabID]/@cp:editAllowed = 'false') or (/layout[@cp:fragmentName] or /layout[@cp:isTemplateUser])">
   <script language="JavaScript">
    var count = 0;
    function doNameCheck()
    {
       count++;
       
       if( count == 1 )
       {
           if ( document.formRenameTab.tabName.value == '' )
           {
              document.formRenameTab.tabName.focus();
              alert( "You must provide a tab name." );
              count = 0;
              return;
           }
           
           found = false;
           len   = document.formRenameTab.tabName.value.length;
           
           for ( i = 0; i &lt; len; i++ )
           {
               ch = document.formRenameTab.tabName.value.charAt( i );
               if ( ch != ' ' 
                  &amp;&amp; ch != '\t'
                  &amp;&amp; ch != '\r'
                  &amp;&amp; ch != '\n'
                  &amp;&amp; ch != '\f' )
               {
                   found = true;
                   break;
               }
           }
        
           if ( !found )
           {
              document.formRenameTab.tabName.focus();
              document.formRenameTab.tabName.value = '';
              alert( "You must enter at least one alpha-numeric character in the tab name field." );
              count = 0;
              return;
           }
    
           document.formRenameTab.submit();
    
        }
        else
        {
           document.location.href = '<xsl:value-of select="$baseActionURL"/>';
        }
       
    }
   </script>
   <tr>
        <td colspan="2">
            <form action="{$baseActionURL}" method="post" name="formRenameTab">
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td nowrap="nowrap"><img height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$RENAME_THE_TAB"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$RENAME_THE_TAB"/></xsl:attribute></img><span class="uportal-channel-text"><xsl:value-of select="$HEADING_RENAME_THE_TAB"/></span><img alt="" border="0" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                  <td>
                    <span class="uportal-text-small"><input class="uportal-input-text" maxlength="60" name="tabName" size="30" type="text" value="{$tabName}"/></span>
                    <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                  </td>
                  <td>
                    <span class="uportal-channel-text"><input CLASS="uportal-input-text" name="RenameTab" onClick="doNameCheck()" type="button"><xsl:attribute name="value"><xsl:value-of select="$RENAME"/></xsl:attribute></input></span>

                    <input name="action" type="hidden" value="renameTab"/>
                    <input name="elementID" type="hidden" value="{$activeTabID}"/>
                  </td>
                </tr>
              </table>
            </form>
          </td>
        </tr>
    </xsl:if>

<!-- conditionally add in permission elements for distributed layout owners -->
        <xsl:if test="/layout[@cp:fragmentName]">
   <tr><td colspan="2"><span class="uportal-channel-text"><img height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$SET_ACTIONS_ALLOWED_BY_USERS"/></span></td></tr>
   <tr>
        <td><img alt="" height="5" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td>
        <form action="{$baseActionURL}" method="post" name="formChangePermissions">
        <table border="0" cellpadding="2" cellspacing="0">
           <tr>
                <td nowrap="nowrap">
                  <input class="uportal-input-text" name="moveAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/folder/folder[@ID=$activeTabID][@cp:moveAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$MOVE_TAB"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input class="uportal-input-text" name="editAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/folder/folder[@ID=$activeTabID][@cp:editAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$EDIT_PROPERTIES"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input class="uportal-input-text" name="addChildAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/folder/folder[@ID=$activeTabID][@cp:addChildAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$ADD_COLUMNS"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input class="uportal-input-text" name="deleteAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/folder/folder[@ID=$activeTabID][@cp:deleteAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$DELETE_TAB"/></span>
                </td>
                <td>
                  <img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                  <span class="uportal-channel-text"><input class="uportal-input-text" name="ChangeTab"><xsl:attribute name="value"><xsl:value-of select="$SET_ACTIONS"/></xsl:attribute><xsl:attribute name="type"><xsl:value-of select="$SUBMIT"/></xsl:attribute></input></span>
                  <input name="action" type="hidden" value="changePermissions"/>
                  <input name="elementID" type="hidden" value="{$activeTabID}"/>
                </td>
           </tr>
        </table>
        </form>
        </td>
   </tr>
        </xsl:if>
<!-- </cp:change> -->
<!-- Add the lock/unlock icon if the user is allowed to make things immutable
      <xsl:if test="$showLockUnlock = 'true'">
   <tr>
          <xsl:choose>
            <xsl:when test="/layout/folder/folder[@ID=$activeTabID]/@unremovable = 'true'">
        <td class="uportal-channel-text"><a href="{$baseActionURL}?action=unlockTab&amp;elementID={$activeTabID}" onMouseover="window.status=''; return true;"><img alt="Unlock this tab" title="Unlock this tab" src="{$mediaPath}/bullet.gif" width="9" height="9" hspace="7" border="0"/>Unlock this tab</a></td>
            </xsl:when>
            <xsl:when test="/layout/folder/folder[@ID=$activeTabID]/@unremovable = 'false'">
        <td class="uportal-channel-text"><a href="{$baseActionURL}?action=lockTab&amp;elementID={$activeTabID}" onMouseover="window.status=''; return true;"><img alt="Lock this tab" title="Lock this tab" src="{$mediaPath}/bullet.gif" width="9" height="9" hspace="7" border="0"/>Lock this tab</a></td>
            </xsl:when>
          </xsl:choose>
   </tr>
      </xsl:if>
-->

<!-- <xsl:if test="not(/layout/folder/folder[@ID=$activeTabID]/@unremovable = 'true')"> -->

        <xsl:if test="not(/layout/folder/folder[@ID=$activeTabID]/descendant-or-self::*[@cp:deleteAllowed = 'false'])">
        
   <tr>
        <td colspan="2">

<SCRIPT LANGUAGE="JavaScript">
function confirmTabDelete()
{
    if ( confirm( "Are you sure you want to delete this tab?" ) )
    {
        loc = '<xsl:value-of select="$baseActionURL"/>?action=deleteTab&amp;elementID=<xsl:value-of select="$activeTabID"/>';
        document.location = loc;
    }
}
</SCRIPT>

<!--<a href="{$baseActionURL}?action=deleteTab&amp;elementID={$activeTabID}" onMouseover="window.status=''; return true;"><img alt="Delete this tab" title="Delete this tab" src="{$mediaPath}/bullet.gif" width="9" height="9" hspace="7" border="0"/>Delete this tab</a>-->
   <span class="uportal-channel-text"><a href="javascript:confirmTabDelete();" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$DELETE_THIS_TAB"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$DELETE_THIS_TAB"/></xsl:attribute></img><xsl:value-of select="$DELETE_THIS_TAB"/></a></span>

        </td>
   </tr>

        </xsl:if>

<!-- </xsl:if> -->

   <tr><td colspan="2"><hr size="1"/></td></tr>
   <tr><td colspan="2"><span class="uportal-channel-text"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></span></td></tr>
</table>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.tab.mod";
</SCRIPT>
<!-- End Mod Tab Options -->
</xsl:template>


<xsl:template name="optionMenuModifyColumn">
<!-- Begin Mod Column Options -->

<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MODIFYING_THIS_COLUMN"/></span><br/>
<table border="0" cellpadding="0" cellspacing="7" width="100%">
   <tr>
   	<td colspan="2" width="100%">
        <span class="uportal-channel-text"><img height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$SET_COLUMN_WIDTHS"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$SET_COLUMN_WIDTHS"/></xsl:attribute></img><xsl:value-of select="$HEADING_SET_COLUMN_WIDTHS"/></span><br/>
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
           <tr>
           	<td><img alt="" height="9" hspace="7" title="" width="9"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
           	<td><span class="uportal-channel-text"><xsl:value-of select="$COLUMN_WIDTHS_CAN_BE_SET_AS_A_PERCENTAGE_OF_THE_TOTAL_SCREEN"/></span></td>
	   </tr>
	</table>
   	</td>
   </tr>
   <tr>
        <td><img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td>
        <form action="{$baseActionURL}" method="post" name="formColumnWidth">
         <input name="action" type="hidden" value="columnWidth"/>
        <table border="0" cellpadding="5" cellspacing="0">
           <tr>
              <xsl:for-each select="/layout/folder/folder[@ID = $activeTabID]/descendant::folder">
              
                <td align="center" class="uportal-text-small" nowrap="nowrap">
                
                  <xsl:choose>
                      <xsl:when test="@cp:editAllowed = 'false'">
                          <img alt="" height="5" title="" width="30"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><br/>
                          <xsl:value-of select="@width"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <input class="uportal-input-text" maxlength="" name="columnWidth_{@ID}" size="5" type="text" value="{@width}"/>      
                      </xsl:otherwise>
                  </xsl:choose>

                <br/>
                  <xsl:choose>
                    <xsl:when test="$elementID=@ID">
                       <strong>Column</strong> 
                    </xsl:when>
                    <xsl:otherwise><xsl:value-of select="$COLUMN"/></xsl:otherwise>
                  </xsl:choose>
                </td>
                <td nowrap="nowrap"><img alt="" height="5" title="" width="5"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
              </xsl:for-each>

                <td nowrap="nowrap">
                   <span class="uportal-channel-text"><input class="uportal-input-text" name="submitModifyColumn"><xsl:attribute name="value"><xsl:value-of select="$SAVE_WIDTHS"/></xsl:attribute><xsl:attribute name="type"><xsl:value-of select="$SUBMIT"/></xsl:attribute></input></span>
                </td>
           </tr>
        </table>
        </form>
        </td>
   </tr>

<!-- If ancestor is immutable - the column cannot be moved-->
<!-- <xsl:if test="not(/layout/descendant::folder[@ID=$elementID]/ancestor::*[@immutable='true'])"> -->

<!--
<xsl:if test="not(/layout/folder/folder[@ID = $activeTabID]/descendant::folder[@ID = $elementID][@cp:moveAllowed = 'false'])">
   <tr>
        <td colspan="2" class="uportal-channel-text">
            <a href="{$baseActionURL}?action=moveColumn&amp;elementID={$elementID}"><img alt="Move this column to a different location" title="Move this column to a different location" src="{$mediaPath}/bullet.gif" hspace="7" width="9" height="9" border="0"/>Move this column to a different location</a>
        </td>
   </tr>
</xsl:if>
-->

<!-- </xsl:if> -->
<!-- conditionally add in permission elements for distributed layout owners -->
<xsl:if test="/layout[@cp:fragmentName]">
   <tr><td colspan="2"><span class="uportal-channel-text"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$ADJUST_ACTIONS_ALLOWED_BY_USERS"/></span></td></tr>
   <tr>
        <td><img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td>

  <form action="{$baseActionURL}" method="post" name="formChangePermissions">
        <table border="0" cellpadding="2" cellspacing="0">
           <tr>
                <td nowrap="nowrap">
                  <input name="moveAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::folder[@ID=$elementID][@cp:moveAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$MOVE_COLUMN"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input name="editAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::folder[@ID=$elementID][@cp:editAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$EDIT_PROPERTIES"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input name="addChildAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::folder[@ID=$elementID][@cp:addChildAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$ADD_CHANNELS"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input name="deleteAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::folder[@ID=$elementID][@cp:deleteAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$DELETE_COLUMN"/></span>
                </td>
                <td>
                   <img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                   <span class="uportal-channel-text"><input class="uportal-input-text" name="ChangeColumn"><xsl:attribute name="value"><xsl:value-of select="$SAVE_ACTIONS"/></xsl:attribute><xsl:attribute name="type"><xsl:value-of select="$SUBMIT"/></xsl:attribute></input></span>
                   <input name="action" type="hidden" value="changePermissions"/>
                   <input name="elementID" type="hidden" value="{$elementID}"/>
                </td>
           </tr>
        </table>
  </form>
        </td>
   </tr>
</xsl:if>
<!-- </cp:change> -->
<!-- If ancestor or self is unremovable - the column cannot be deleted-->

<!-- <xsl:if test="not(/layout/descendant::folder[@ID=$elementID]/ancestor-or-self::*[@unremovable='true'])"> -->

<xsl:if test="not(/layout/descendant::folder[@ID = $elementID]/descendant-or-self::*[@cp:deleteAllowed = 'false'])">
   <tr>
        <td class="uportal-channel-text" colspan="2">

<SCRIPT LANGUAGE="JavaScript">
function confirmColumnDelete()
{
if ( confirm( "Are you sure you want to delete this column?" ) )
{
loc = '<xsl:value-of select="$baseActionURL"/>?action=deleteColumn&amp;elementID=<xsl:value-of select="$elementID"/>';
document.location = loc;
}
}
</SCRIPT>

<!-- <a href="{$baseActionURL}?action=deleteColumn&amp;elementID={$elementID}"><img alt="Delete this column" title="Delete this column" src="{$mediaPath}/bullet.gif" hspace="7" width="9" height="9" border="0"/>Delete this column</a> -->
        <a href="javascript:confirmColumnDelete();"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$DELETE_THIS_COLUMN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$DELETE_THIS_COLUMN"/></xsl:attribute></img><xsl:value-of select="$DELETE_THIS_COLUMN"/></a>
        
        </td>
   </tr>
</xsl:if>
<!-- </xsl:if> -->

   <tr><td class="uportal-channel-text" colspan="2"><hr size="1"/></td></tr>
   <tr><td colspan="2"><span class="uportal-channel-text"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></span></td></tr>
</table>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.col.mod";
</SCRIPT>
<!-- End Mod Column Options -->
</xsl:template>


<xsl:template name="optionMenuModifyChannel">
<xsl:variable name="channelName" select="/layout/folder/descendant::*[@ID = $elementID]/@name"/>
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MODIFYING_THIS_CHANNEL"/></span><br/>
<table border="0" cellpadding="0" cellspacing="7" width="100%">
<form action="{$baseActionURL}" method="post" name="formModifyChannel">
      <!-- We aren't going to allow renaming a channel at the moment...
      <xsl:if test="not(/layout/descendant::channel[@ID=$elementID]/@immutable = 'true')">
        <tr>
          <td class="uportal-channel-text">
            <img alt="Rename this channel" title="Rename this channel" src="{$mediaPath}/bullet.gif" width="9" height="9" border="0" />
          </td>
          <td width="100%" class="uportal-channel-text">
            <a href="#" onMouseover="window.status=''; return true;">Rename this channel:</a>
            <img src="{$mediaPath}/transparent.gif" width="10" height="10" border="0" />
            <input type="hidden" name="action" value="renameChannel" />
            <input type="hidden" name="elementID" value="{$elementID}" />
            <input type="text" name="channelName" class="uportal-input-text" value="{$channelName}" size="30" />
            <img src="{$mediaPath}/transparent.gif" width="10" height="10" border="0" />
            <input type="submit" name="RenameTab" value="Rename" />
          </td>
        </tr>
      </xsl:if>
      End of channel rename section-->
<!-- If ancestor is immutable - the channel cannot be moved-->
<!--
      <xsl:if test="not(/layout/descendant::*[@ID=$elementID]/ancestor::folder[@immutable='true'])">
   <tr><td colspan="2" width="100%" class="uportal-channel-text"><a href="{$baseActionURL}?action=moveChannel&amp;elementID={$elementID}" onMouseover="window.status=''; return true;"><img alt="Move this channel" title="Move this channel" src="{$mediaPath}/bullet.gif" hspace="7" width="9" height="9" border="0"/>Move this channel to a different location</a></td></tr>
      </xsl:if>
-->
      <xsl:if test="//channel[@ID=$elementID]/parameter/@override = 'yes'">

   <tr><td class="uportal-channel-text" colspan="2" width="100%"><a href="{$baseActionURL}?action=selectChannel&amp;subAction=modifyChannelParams&amp;elementID={$elementID}" onMouseover="window.status=''; return true;"><img alt="Modify this channels parameters" border="0" height="9" hspace="7" title="Modify this channels parameters" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$MODIFY_THIS_CHANNELS_PARAMETERS"/></a></td></tr>

      </xsl:if>

<!-- conditionally add in permission elements for distributed layout owners -->
      <xsl:if test="/layout[@cp:fragmentName]">
   <tr><td class="uportal-channel-text" colspan="2"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$ADJUST_ACTIONS_ALLOWED_BY_USERS"/></td></tr>
   <tr>
        <td><img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td>
        <form action="{$baseActionURL}" method="post" name="formChangePermissions">
        <table border="0" cellpadding="2" cellspacing="0">
           <tr>
                <td nowrap="nowrap">
                  <input name="moveAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::channel[@ID=$elementID][@cp:moveAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$MOVE_CHANNEL"/></span>
                </td>
                <td nowrap="nowrap">
                  <img alt="" height="5" title="" width="15"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img><input name="deleteAllowed" type="checkbox" value="true"><xsl:if test="not(/layout/descendant::channel[@ID=$elementID][@cp:deleteAllowed='false'])"><xsl:attribute name="checked">true</xsl:attribute></xsl:if></input>
                </td>
                <td class="uportal-background-med" nowrap="nowrap">
                  <span class="uportal-text-small"><xsl:value-of select="$DELETE_CHANNEL"/></span>
                </td>
                <td>
                   <img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                   <span class="uportal-channel-text"><input class="uportal-input-text" name="ChangeColumn"><xsl:attribute name="value"><xsl:value-of select="$SET_ACTIONS"/></xsl:attribute><xsl:attribute name="type"><xsl:value-of select="$SUBMIT"/></xsl:attribute></input></span>
                   <input name="action" type="hidden" value="changePermissions"/>
                   <input name="elementID" type="hidden" value="{$elementID}"/>
                </td>
           </tr>
        </table>
        </form>
        </td>
   </tr>
      </xsl:if>
<!-- </cp:change> -->

      <!-- If ancestor or self is unremovable - the channel cannot be deleted-->
      
<!-- <xsl:if test="not(/layout/descendant::*[@ID=$elementID]/ancestor-or-self::*[@unremovable='true'])"> -->
<xsl:if test="not(/layout/descendant::*[@ID=$elementID][@cp:deleteAllowed = 'false'])">
   <tr><td class="uportal-channel-text" colspan="2" width="100%"><a href="{$baseActionURL}?action=deleteChannel&amp;elementID={$elementID}" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$DELETE_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$DELETE_THIS_CHANNEL"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$DELETE_THIS_CHANNEL"/></a></td></tr>
</xsl:if>
<!-- </xsl:if> -->

   <tr><td class="uportal-channel-text" colspan="2"><hr size="1"/></td></tr>
   <tr><td class="uportal-channel-text" colspan="2" width="100%"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></td></tr>
</form>
</table>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.chan.mod";
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuNewTab">
<SCRIPT LANGUAGE="JavaScript">

var count = 0;

function validateForm()
{
   count++;
   
   if( count == 1 )
   {
       if ( document.formNewTab.tabName.value == '' )
       {
          document.formNewTab.tabName.focus();
          alert( "You must provide a tab name." );
          count = 0;
          return;
       }
       
       found = false;
       len   = document.formNewTab.tabName.value.length;
       
       for ( i = 0; i &lt; len; i++ )
       {
           ch = document.formNewTab.tabName.value.charAt( i );
           if ( ch != ' ' 
              &amp;&amp; ch != '\t'
              &amp;&amp; ch != '\r'
              &amp;&amp; ch != '\n'
              &amp;&amp; ch != '\f' )
           {
               found = true;
               break;
           }
       }
    
       if ( !found )
       {
          document.formNewTab.tabName.focus();
          document.formNewTab.tabName.value = '';
          alert( "You must enter at least one alpha-numeric character in the tab name field." );
          count = 0;
          return;
       }
       document.formNewTab.submit();
    }
    else
    {
       document.location.href = '<xsl:value-of select="$baseActionURL"/>';
    }
   
}
</SCRIPT>

<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$STEPS_FOR_ADDING_THIS_NEW_TAB"/></span><br/>
<table border="0" cellpadding="1" cellspacing="7" width="100%">
<form action="{$baseActionURL}" method="post" name="formNewTab">
   <tr>
        <td align="right" class="uportal-text12-bold">1.</td>
        <td>
        <table border="0" cellpadding="0" cellspacing="0">
           <tr>
           	<td nowrap="nowrap">
           	<span class="uportal-channel-text"><xsl:value-of select="$NAME_THE_TAB"/></span><img alt="" border="0" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
           	<td>
            	  <span class="uportal-channel-text"><input class="uportal-input-text" maxlength="35" name="tabName" size="30" type="text"/></span>
           	</td>
          </tr>
      </table>
      </td>
   </tr>
   <tr>
        <td align="right" class="uportal-text12-bold">2.</td>
        <td class="uportal-channel-text"><xsl:value-of select="$SELECT_A_POSITION_FOR_THE_TAB"/></td>
   </tr>
   <tr>
        <td><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td class="uportal-channel-text">
        <table border="0" cellpadding="4" cellspacing="0" width="100%">
           <tr>

            <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">

                <xsl:choose>
                    <xsl:when test="not(@cp:moveAllowed = 'false' or following-sibling::folder[@type='regular' and @hidden='false'][@cp:moveAllowed='false'])">
                <td nowrap="nowrap"><input name="method_ID" type="radio" value="insertBefore_{@ID}"/></td>
                    </xsl:when>
                    <xsl:otherwise>
                <td nowrap="nowrap"><img alt="" height="10" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                    </xsl:otherwise>
                </xsl:choose>
                
                <td class="uportal-background-med" nowrap="nowrap">
                  <img alt="" height="5" title="" width="5"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                  <span class="uportal-text-small"><xsl:value-of select="@name"/></span>
                  <img alt="" height="5" title="" width="5"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img>
                </td>

            </xsl:for-each>

                <td width="100%">
                <input checked="" name="method_ID" type="radio" value="appendAfter_{/layout/folder/folder[@type='regular' and @hidden='false'][position() = last()]/@ID}"/>
                </td>
           </tr>
        </table>     
        </td>
   </tr>
   <tr><td colspan="2"><input name="action" type="hidden" value="addTab"/></td></tr>
   <tr>
        <td align="right" class="uportal-text12-bold">3.</td>
        <td>
        <table border="0" cellpadding="0" cellspacing="0">
           <tr>
           	<td>
           	   <span class="uportal-channel-text"><xsl:value-of select="$SUBMIT_THE_CHOICES"/></span>
           	</td>
           	<td>
           	   <A HREF="javascript:validateForm();" onMouseover="window.status=''; return true;"><img border="0" height="18" hspace="10" width="62"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_SUBMIT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$SUBMIT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$SUBMIT"/></xsl:attribute></img></A>
           	</td>
           </tr>
        </table>
        </td>
   </tr>
   <tr><td colspan="2"><hr size="1"/></td></tr>
   <tr>
        <td colspan="2" width="100%"><span class="uportal-channel-text"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></span></td>
   </tr>
</form>
</table>

<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.tab.new";
  setTimeout('document.formNewTab.tabName.focus();', 1000);
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuNewColumn">

<SCRIPT LANGUAGE="JavaScript">

var count = 0;

function validateForm()
{
    count++;

    if( count == 1 )
    {
        document.formNewColumn.submit();
    }
    else
    {
        document.location.href = '<xsl:value-of select="$baseActionURL"/>';
    }

}
</SCRIPT>

<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$STEPS_FOR_ADDING_THIS_NEW_COLUMN"/></span><br/>
<table border="0" cellpadding="0" cellspacing="7" width="100%">
 <form action="{$baseActionURL}" method="post" name="formNewColumn">
 <input name="action" type="hidden" value="columnWidth"/>
   <tr>
        <td valign="top"><span class="uportal-text12-bold" valign="top">1.</span></td>
        <td><span class="uportal-channel-text"><xsl:value-of select="$HEADING_SET_COLUMN_WIDTHS"/></span><br/>
        <span class="uportal-channel-text"><xsl:value-of select="$COLUMN_WIDTHS_CAN_BE_SET_AS_A_PERCENTAGE_OF_THE_TOTAL_SCREEN"/></span>
        </td>
   </tr>
   <tr>
        <td><img alt="" height="16" title="" width="16"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
        <td>
        <table border="0" cellpadding="5" cellspacing="0">
           <tr>

              <xsl:for-each select="/layout/folder/folder[@ID = $activeTabID]/descendant::folder">

                <xsl:if test="$position='before' and $elementID=@ID">
                <td align="center" class="uportal-text-small" nowrap="nowrap">
                    <input class="uportal-input-text" maxlength="" name="columnWidth_{@ID}" size="5" type="text" value=""/>
                    <br/>
                    <strong><xsl:value-of select="$NEW_COLUMN"/></strong>
                </td>
                <td nowrap="nowrap"><img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                </xsl:if>

                <td align="center" class="uportal-input-text" nowrap="nowrap">
                  <span class="uportal-text-small">
                  <input class="uportal-input-text" maxlength="" name="columnWidth_{@ID}" size="5" type="text" value="{@width}"/>
                  <br/><xsl:value-of select="$COLUMN"/></span>
                </td>
                <td nowrap="nowrap"><img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                
                <xsl:if test="$position='after' and $elementID=@ID">
                <td align="center" class="uportal-input-text" nowrap="nowrap">
                    <span class="uportal-text-small">
                    <input class="uportal-input-text" maxlength="" name="columnWidth_{@ID}" size="5" type="text" value=""/>
                    <br/>
                    <strong><xsl:value-of select="$NEW_COLUMN"/></strong>
                    </span>
                </td>
                <td nowrap="nowrap"><img alt="" height="5" title="" width="10"><xsl:attribute name="src"><xsl:value-of select='$mediaPath'/><xsl:value-of select="$IMAGE_SRC_TRANSPARENT_GIF"/></xsl:attribute></img></td>
                </xsl:if>

              </xsl:for-each>

           </tr>
        </table>
        </td>
   </tr>
   <tr>
        <td class="uportal-text12-bold">2.</td>
        <td>
        <table border="0" cellpadding="0" cellspacing="0">
           <tr>
           	<td><span class="uportal-channel-text"><xsl:value-of select="$SUBMIT_THE_CHOICES"/></span></td>
           	<td><A HREF="javascript:validateForm();" onMouseover="window.status=''; return true;"><img border="0" height="18" hspace="15" width="62"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_SUBMIT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$SUBMIT"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$SUBMIT"/></xsl:attribute></img></A></td>
           </tr>
        </table>

        <!-- <input type="submit" name="submitNewColumn" value="Submit"/> -->

   	</td>
   </tr>
   <tr><td colspan="2"><hr size="1"/></td></tr>
   <tr><td class="uportal-channel-text" colspan="2" width="100%"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></td></tr>
</form>
</table>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.col.new";
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuMoveColumn">
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MOVING_THIS_COLUMN"/></span><br/>
<table border="0" cellpadding="0" cellspacing="7" width="100%">
   <tr><td class="uportal-channel-text"><a href="#" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW"/></xsl:attribute></img><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW_OR_SELECT_A_DIFFERENT_TAB_ON_WHICH_TO_PLACE_THIS_COLUMN"/></a></td></tr>
   <tr><td><hr size="1"/></td></tr>
   <tr><td class="uportal-channel-text" width="100%"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></td></tr>
</table>

<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.col.move";
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuMoveChannel">
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$OPTIONS_FOR_MOVING_THIS_CHANNEL"/></span><br/>
<table border="0" cellpadding="0" cellspacing="7" width="100%">
   <tr><td class="uportal-channel-text"><a href="#" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW"/></xsl:attribute></img><xsl:value-of select="$SELECT_ONE_OF_THE_HIGHLIGHTED_LOCATIONS_BELOW_OR_SELECT_A_DIFFERENT_TAB_ON_WHICH_TO_PLACE_THIS_CHANNEL"/></a></td></tr>
   <tr><td><hr size="1"/></td></tr>
   <tr><td class="uportal-channel-text" width="100%"><a href="{$baseActionURL}?action=cancel" onMouseover="window.status=''; return true;"><img border="0" height="9" hspace="7" width="9"><xsl:attribute name="alt"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="title"><xsl:value-of select="$CANCEL_AND_RETURN"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$mediaPath"/><xsl:value-of select="$IMAGE_SRC_BULLET_GIF"/></xsl:attribute></img><xsl:value-of select="$CANCEL_AND_RETURN"/></a></td></tr>
</table>

<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.chan.move";
</SCRIPT>
</xsl:template>


<xsl:template name="optionMenuError">
<span class="uportal-channel-subtitle-reversed"><xsl:value-of select="$THE_FOLLOWING_ERROR_WAS_REPORTED"/></span><br/>
<xsl:value-of select="$errorMessage"/>
<SCRIPT LANGUAGE="JavaScript">
  helpTag = "prtl.main";
</SCRIPT>
</xsl:template>

</xsl:stylesheet>