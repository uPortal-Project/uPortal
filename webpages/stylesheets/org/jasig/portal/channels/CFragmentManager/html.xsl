<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (c) 2004 The JA-SIG Collaborative.  All rights reserved.
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

Author: 
Justin Tilton, jet@immagic.com
Jon Allen, jfa@immagic.com
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no"/>
    <xsl:param name="baseActionURL" select="'render.userLayoutRootNode.uP'"/>
    <xsl:param name="locale" select="'en_US'"/>
    <!--~-->
    <!--actions: 'defaultView', 'properties', 'publish', 'new' -->
    <!--~-->
    <xsl:param name="uPcFM_action" select="'defaultView'"/>
    <xsl:param name="uPcFM_selectedID" select="''"/>
    <!-- Param removed under assumption of non-use
    <xsl:param name="selectedID" select="'noSelectedID'"/>
     Param removed under assumption of non-use -->
    <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CFragmentManager'"/>
    <!--~-->
    <!--root template-->
    <!--~-->
    <xsl:template match="/">
        <xsl:apply-templates select="fragments"/>
    </xsl:template>
    <!--~-->
    <!--fragments template - draws the outer table-->
    <!--~-->
    <xsl:template match="fragments">
        <table class="uportal-background-light" cellpadding="0" cellspacing="10" border="1" width="100%">
            <tr>
                <td class="uportal-background-content" valign="top" align="left">
                    <table cellpadding="2" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                                <img src="{$mediaPath}/new.gif" width="16" height="16" alt=""/>
                                <a href="{$baseActionURL}?uPcFM_action=new&amp;uPcFM_selectedID=">
                                    <span class="uportal-channel-table-header">Create new fragment</span>
                                </a>
                            </td>
                            <!-- Expand/Contract Categories of Fragments to be used after Fragment Categories are added -->
                            <!-- <td align="right" valign="bottom" nowrap class="uportal-label"><a href="openall.html">Expand</a>/<a href="#">Condense</a> All Categories</td> -->
                            <!-- Expand/Contract Categories of Fragments to be used after Fragment Categories are added -->
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                    <tr>
                                        <td height="2">
                                            <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                    <xsl:apply-templates select="category"/>
                </td>
                <td valign="top" align="left">
                    <xsl:choose>
                        <xsl:when test="$uPcFM_action='properties'">
                            <xsl:call-template name="properties"/>
                        </xsl:when>
                        <xsl:when test="$uPcFM_action='publish'">
                            <xsl:call-template name="permissions"/>
                        </xsl:when>
                        <xsl:when test="$uPcFM_action='new'">
                            <xsl:call-template name="new"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="defaultView"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!--category template - draws the tree (static now)-->
    <!--~-->
    <xsl:template match="category">
        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
            <tr class="uportal-channel-text" valign="top" align="left">
                <td class="uportal-navigation-category">
                    <!-- Non functioning categories - add logic to open/close and indent folders when available -->
                    <img src="{$mediaPath}/openFile.gif" width="16" height="16" border="0" alt=""/>
                </td>
                <td width="100%" valign="bottom">
                    <a href="#" class="uportal-navigation-category-selected">Fragments</a>
                </td>
            </tr>
            <tr valign="top" align="left">
                <td height="5" colspan="5">
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <!--~-->
        <!--fragment template - draws the leaf nodes and expands and contracts selectedID-->
        <!--~-->
        <xsl:apply-templates select="fragment">
            <xsl:sort select="name"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="fragment">
        <xsl:choose>
            <xsl:when test="$uPcFM_selectedID=ID">
                <!-- Open State of Fragment List -->
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
                    <tr valign="top" align="left">
                        <td>
                            <img height="1" width="20" src="{$mediaPath}/transparent.gif" alt=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <span class="uportal-navigation-category-selected">
                                <xsl:value-of select="name"/>
                            </span>
                        </td>
                    </tr>
                </table>
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
                    <tr>
                        <td>
                            <img height="1" width="20" src="{$mediaPath}/transparent.gif" alt=""/>
                        </td>
                        <td>
                            <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt=""/>
                        </td>
                        <td width="100%" align="left" valign="top">
                            <table cellpadding="2" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <a href="{$baseActionURL}?uPcFM_action=properties&amp;uPcFM_selectedID={$uPcFM_selectedID}">
                                            <img src="{$mediaPath}/properties.gif" alt="properties" border="0"/>
                                        </a>
                                    </td>
                                    <td width="100%">
                                        <span class="uportal-label">
                                            <a href="{$baseActionURL}?uPcFM_action=properties&amp;uPcFM_selectedID={$uPcFM_selectedID}" class="uportal-channel-subtitle-reversed">Fragment properties</a>
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="{$baseActionURL}?uPcFM_action=edit&amp;uPcFM_selectedID={$uPcFM_selectedID}">
                                            <img src="{$mediaPath}/editContent.gif" alt="edit" border="0"/>
                                        </a>
                                    </td>
                                    <td width="100%">
                                        <span class="uportal-label">
                                            <a href="{$baseActionURL}?uPcFM_action=edit&amp;uPcFM_selectedID={$uPcFM_selectedID}" class="uportal-channel-subtitle-reversed">Edit content</a>
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="{$baseActionURL}?uPcFM_action=publish&amp;uPcFM_selectedID={$uPcFM_selectedID}">
                                            <img src="{$mediaPath}/publish.gif" alt="publish" border="0"/>
                                        </a>
                                    </td>
                                    <td width="100%">
                                        <span class="uportal-label">
                                            <a href="{$baseActionURL}?uPcFM_action=publish&amp;uPcFM_selectedID={$uPcFM_selectedID}" class="uportal-channel-subtitle-reversed">Publish fragment</a>
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="{$baseActionURL}?uPcFM_action=delete&amp;uPcFM_deleteID={$uPcFM_selectedID}&amp;uPcFM_selectedID=noSelectedID">
                                            <img src="{$mediaPath}/delete.gif" alt="delete" border="0"/>
                                        </a>
                                    </td>
                                    <td width="100%">
                                        <span class="uportal-label">
                                            <a href="{$baseActionURL}?uPcFM_action=delete&amp;uPcFM_deleteID={$uPcFM_selectedID}&amp;uPcFM_selectedID=noSelectedID" class="uportal-channel-subtitle-reversed">Delete fragment</a>
                                        </span>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td height="5" colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <!-- Open State of Fragment List -->
            </xsl:when>
            <xsl:otherwise>
                <!-- Closed State of Fragment List -->
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                        <td>
                            <img height="1" width="20" src="{$mediaPath}/transparent.gif" alt=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a href="{$baseActionURL}?uPcFM_action=properties&amp;uPcFM_selectedID={ID}" class="uportal-navigation-channel">
                                <xsl:value-of select="name"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td height="5" colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <!-- Closed State of Fragment List -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--~-->
    <!--properties template - form for updating the metadata for a fragment-->
    <!--~-->
    <xsl:template name="properties">
        <table cellpadding="2" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                    <span class="uportal-channel-table-header">Fragment Managment: <xsl:value-of select="//fragment[ID=$uPcFM_selectedID]/name"/> properties</span>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td>
                                <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="6" class="uportal-background-highlight">
            <tr>
                <td>
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                        <tr>
                            <td class="uportal-channel-strong" align="left" valign="top">
                                <form name="frag_prop_form" action="{$baseActionURL}" method="post">
                                    <!-- Open Contents of info table -->
                                    <table cellspacing="0" cellpadding="5" width="100%" border="0" class="uportal-background-content">
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Choose the fragment type:
                                                    <!-- The correct radio option will be selected. -->
                                                </span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="uportal-background-content" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <input type="radio" name="fragment_type" value="pulled">
                                                    <xsl:if test="//fragment[ID=$uPcFM_selectedID]/type='pulled'">
                                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                                    </xsl:if>
                                                </input>
                                                <span class="uportal-channel-subtitle-reversed">Pulled fragment</span>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="uportal-background-content" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <input type="radio" name="fragment_type" value="pushed">
                                                    <xsl:if test="//fragment[ID=$uPcFM_selectedID]/type='pushed'">
                                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                                    </xsl:if>
                                                </input>
                                                <span class="uportal-channel-subtitle-reversed">Pushed fragment</span>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment display name:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- The Name of the Fragment will go here -->
                                                <input name="frag_prop_name" type="text" class="uportal-input-text" value="{//fragment[ID=$uPcFM_selectedID]/name}" size="30" maxlength="1000"/>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment functional name:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- The Name of the Fragment will go here -->
                                                <input name="frag_prop_fname" type="text" class="uportal-input-text" value="{//fragment[ID=$uPcFM_selectedID]/fname}" size="30" maxlength="1000"/>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment description:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- A Description of the Fragment will go here -->
                                                <textarea name="frag_prop_desc" cols="30" rows="4" wrap="on" class="uportal-input-text">
                                                    <xsl:value-of select="//fragment[ID=$uPcFM_selectedID]/description"/>
                                                </textarea>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <input name="frag_prop_submit" type="submit" class="uportal-button" value="Submit properties"/>
                                            </td>
                                        </tr>
                                    </table>
                                    <!-- Open Contents of info table -->
                                </form>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!--new template - form for adding the metadata for a new fragment-->
    <!--~-->
    <xsl:template name="new">
        <table cellpadding="2" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                    <span class="uportal-channel-table-header">Fragment Managment: New Fragment information</span>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td>
                                <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="6" class="uportal-background-highlight">
            <tr>
                <td>
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                        <tr>
                            <td class="uportal-channel-strong" align="left" valign="top">
                                <form name="frag_prop_new_form" action="{$baseActionURL}" method="post">
                                    <!-- Closed Contents of info table -->
                                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <span class="uportal-text">Select a fragment option on the left to begin working</span>
                                            </td>
                                        </tr>
                                    </table>
                                    <!-- Closed Contents of info table -->
                                    <!-- Open Contents of info table -->
                                    <table cellspacing="0" cellpadding="5" width="100%" border="0" class="uportal-background-content">
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Choose the fragment type:
                                                    <!-- The correct radio option will be selected. -->
                                                </span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="uportal-background-content" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <input type="radio" name="fragment_type" value="pulled" checked="checked"/>
                                                <span class="uportal-channel-subtitle-reversed">Pulled fragment</span>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="uportal-background-content" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <input type="radio" name="fragment_type" value="pushed"/>
                                                <span class="uportal-channel-subtitle-reversed">Pushed fragment</span>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment display name:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- The Name of the Fragment will go here -->
                                                <input name="fragment_name" type="text" class="uportal-input-text" value="" size="30" maxlength="1000"/>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment functional name:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- The Name of the Fragment will go here -->
                                                <input name="frag_prop_fname" type="text" class="uportal-input-text" value="" size="30" maxlength="1000"/>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <span class="uportal-label">Fragment description:</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td>
                                                <!-- A Description of the Fragment will go here -->
                                                <textarea name="fragment_desc" cols="30" rows="4" wrap="on" class="uportal-input-text"/>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <!-- TR On only with New Fragments -->
                                        <tr align="left" valign="top">
                                            <td align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                            <td nowrap="nowrap">
                                                <input name="fragment_add_column" type="checkbox" value="true" checked="checked"/>
                                                <span class="uportal-channel-subtitle-reversed">Add a column to the new fragment tab (recommended)</span>
                                            </td>
                                            <td width="100%" align="left" valign="top" nowrap="nowrap">
                                                <img height="10" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3">
                                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                        <!-- TR On only with New Fragments -->
                                        <tr align="left" valign="top">
                                            <td colspan="3">
                                                <input name="frag_prop_submit" type="submit" class="uportal-button" value="Submit properties"/>
                                            </td>
                                        </tr>
                                    </table>
                                    <!-- Open Contents of info table -->
                                </form>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!--permissions template - insert permissions servant here-->
    <!--~-->
    <xsl:template name="defaultView">
        <table cellpadding="2" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                    <span class="uportal-channel-table-header">Fragment Management</span>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td>
                                <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="6" class="uportal-background-highlight">
            <tr>
                <td>
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                        <tr>
                            <td class="uportal-channel-strong" align="left" valign="top">
                                <!-- Closed Contents of info table -->
                                <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                                    <tr>
                                        <td align="left" valign="top" nowrap="nowrap">
                                            <span class="uportal-text">Choose a fragment or a fragment option from the list on the left to begin working</span>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!--defaultView template - displays a friendly message if no action is selected-->
    <!--~-->
    <xsl:template name="permissions">
        <table cellpadding="2" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                    <span class="uportal-channel-table-header">Fragment Management: Publish</span>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td>
                                <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="6" class="uportal-background-highlight">
            <tr>
                <td>
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                        <tr>
                            <td class="uportal-channel-strong" align="left" valign="top">
                                <!-- Closed Contents of info table -->
                                <table width="100%" border="0" cellspacing="0" cellpadding="5" class="uportal-background-content">
                                    <tr>
                                        <td align="left" valign="top" nowrap="nowrap">
                                            <span class="uportal-text">Insert Groups Manager Servant Here</span>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
