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

Author: Jon Allen, jfa@immagic.com
Author: Justin Tilton, jet@immagic.com
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rss09="http://my.netscape.com/rdf/simple/0.9/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:str="http://hacks.benhammersley.com/rss/streaming/" xmlns:ev="http://purl.org/rss/1.0/modules/event/" xmlns:dc="http://purl.org/dc/elements/1.1/">
  <!--
   -->
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="locale" select="'en_US'"/>
  <xsl:param name="viewNum" select="'all'"/>
  <xsl:param name="bulletImage" select="'bullet.gif'"/>
  <xsl:param name="showInfo" select="'false'"/>
  <xsl:param name="allowShowInfo" select="'false'"/>
  <xsl:param name="renderHTML" select="'true'"/>
  <xsl:param name="selectedContainer" select="1"/>
    <xsl:param name="includeRssTitle">false</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CGenericXSLT/RSS</xsl:variable>
  <!--
   -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  <!--
   -->
  <xsl:template match="rdf:RDF | rss">
    <xsl:apply-templates select="//*[local-name()='channel']"/>
	<xsl:apply-templates select="//*[local-name()='item' ]"/>
    <xsl:if test="/rdf:RDF/*[local-name()='channel']/dc:rights != ''">
      <xsl:call-template name="copyright"/>
    </xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="*[local-name()='channel']">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
	<xsl:if test="$includeRssTitle='true'">
	<tr>
		<td width="100%" class="uportal-channel-title">
			<xsl:value-of select="*[local-name()='title']"/>
		</td>
	</tr>
	</xsl:if>
      <tr>
        <td width="100%" class="uportal-channel-subtitle">
          <xsl:value-of select="*[local-name()='description']"/>
        </td>
        <td>
          <!--<xsl:call-template name="rssImage"/>-->
        </td>
      </tr>
      <xsl:if test="child::dc:* or parent::rss[@version='2.0']">
		<xsl:if test="$allowShowInfo='true'">
        <tr>
          <td>
            <xsl:choose>
              <xsl:when test="$showInfo='true'">
                <a href="{$baseActionURL}?showInfo=false">
                  <img alt="hide document info" title="hide document info" border="0" src="{$mediaPath}/showInfoOff.gif" width="16" height="16"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a href="{$baseActionURL}?showInfo=true">
                  <img alt="show document info" title="show document info" border="0" src="{$mediaPath}/showInfoOn.gif" width="16" height="16"/>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
		</xsl:if>
      </xsl:if>
      <xsl:if test="$showInfo='true' and parent::rss[@version='2.0']">
        <tr>
          <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td>
                  <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                </td>
                <td width="100%">
                  <xsl:if test="language">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[language:&#x20;<xsl:value-of select="language"/>] </p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="copyright">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[copyright:&#x20;<xsl:value-of select="copyright"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="pubDate">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[published:&#x20;<xsl:value-of select="pubDate"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="lastBuildDate">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[last updated:&#x20;<xsl:value-of select="lastBuildDate"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="docs">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[documentation:&#x20;<xsl:value-of select="docs"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="generator">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[generator:&#x20;<xsl:value-of select="generator"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="category">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[category:&#x20;<xsl:value-of select="category"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="managingEditor">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[editor:&#x20;<xsl:value-of select="managingEditor"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="webMaster">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[web master:&#x20;<xsl:value-of select="webMaster"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="ttl">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[time to live:&#x20;<xsl:value-of select="ttl"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="rating">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[PICS rating:&#x20;<xsl:value-of select="rating"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="language">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[language:&#x20;<xsl:value-of select="language"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="skipHours">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[skipped hours:&#x20;<xsl:value-of select="skipHours"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="skipDays">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[skipped days:&#x20;<xsl:value-of select="skipDays"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                  <xsl:if test="cloud">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
                          <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
                        </td>
                        <td width="100%" align="left" valign="top">
                          <p class="uportal-text-small">[cloud:&#x20;<xsl:value-of select="cloud"/>]</p>
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </xsl:if>
    </table>
    <xsl:apply-templates select="*[local-name='item']"/>
  </xsl:template>
  <!--
   -->
  <xsl:template match="dc:*">
    <xsl:if test="$showInfo='true'">
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
          </td>
          <td width="100%" align="left" valign="top">
            <p class="uportal-text-small">
              <xsl:choose>
                <xsl:when test="local-name()='date'"> [date:&#x20;<xsl:call-template name="month">
                    <xsl:with-param name="monthNumber" select="substring(.,6,2)"/>
                  </xsl:call-template>
                  <xsl:value-of select="substring(.,9,2)"/>,&#x20;<xsl:value-of select="substring(.,1,4)"/>] </xsl:when>
                <xsl:otherwise> [<xsl:value-of select="local-name()"/>:&#x20;<xsl:value-of select="."/>] </xsl:otherwise>
              </xsl:choose>
            </p>
          </td>
        </tr>
      </table>
    </xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="ev:*">
    <xsl:if test="$showInfo='true'">
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
          </td>
          <td width="100%" align="left" valign="top">
            <p class="uportal-text-small">
              <xsl:choose>
                <xsl:when test="local-name()='startdate'"> [event&#x20;start&#x20;date:&#x20;<xsl:call-template name="month">
                    <xsl:with-param name="monthNumber" select="substring(.,6,2)"/>
                  </xsl:call-template>
                  <xsl:value-of select="substring(.,9,2)"/>,&#x20;<xsl:value-of select="substring(.,1,4)"/>] </xsl:when>
                <xsl:when test="local-name()='enddate'"> [event&#x20;end&#x20;date:&#x20;<xsl:call-template name="month">
                    <xsl:with-param name="monthNumber" select="substring(.,6,2)"/>
                  </xsl:call-template>
                  <xsl:value-of select="substring(.,9,2)"/>,&#x20;<xsl:value-of select="substring(.,1,4)"/>] </xsl:when>
                <xsl:otherwise> [event&#x20;<xsl:value-of select="local-name()"/>:&#x20;<xsl:value-of select="."/>] </xsl:otherwise>
              </xsl:choose>
            </p>
          </td>
        </tr>
      </table>
    </xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="str:*">
    <xsl:if test="$showInfo='true'">
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="16" height="1" border="0"/>
          </td>
          <td width="100%" align="left" valign="top">
            <p class="uportal-text-small">
              <xsl:choose>
                <xsl:when test="local-name()='type'"> [streaming&#x20;media&#x20;type:&#x20; <xsl:value-of select="."/>] </xsl:when>
                <xsl:otherwise> [<xsl:value-of select="local-name()"/>:&#x20;<xsl:value-of select="."/>] </xsl:otherwise>
              </xsl:choose>
            </p>
          </td>
        </tr>
      </table>
    </xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="*[local-name()='item']">
  <xsl:if test="($viewNum='all') or position() &lt; $viewNum + 1 ">
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td>
          <xsl:choose>
            <xsl:when test="str:*">
              <img alt="interface image" src="{$mediaPath}/streamingBullet.gif" width="16" height="16"/>
            </xsl:when>
            <xsl:when test="ev:*">
              <img alt="interface image" src="{$mediaPath}/eventBullet.gif" width="16" height="16"/>
            </xsl:when>
            <xsl:otherwise>
              <!--<img alt="interface image" src="{$mediaPath}/{$bulletImage}" width="16" height="16"/>-->
            </xsl:otherwise>
          </xsl:choose>
        </td>
        <td width="100%" class="uportal-label">
          <a href="{*[local-name()='link']}" target="_blank">
            <xsl:value-of select="*[local-name()='title']"/>
          </a>
        </td>
      </tr>
      <xsl:if test="*[local-name()='description'] or content:* != ''">
        <tr class="uportal-channel-text">
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
          </td>
          <xsl:choose>
            <xsl:when test="content:*">
              <td width="100%">
                <xsl:copy-of select="*[local-name()='items']"/>
				<xsl:if test="$renderHTML">
                <xsl:value-of disable-output-escaping="yes" select="*[local-name()='encoded']"/>
				</xsl:if>
              </td>
            </xsl:when>
            <xsl:when test="$renderHTML='true'">
              <td width="100%">
                <!--The following choose/when/otherwise test is an optional feature that will allow this stylesheet to choose which of the lines below to use
                when rendering HTML in the description area.  It is optional because it will not be 100% reliable as we can never
                predict what the RSS author will put in the 'description' area.  It tests for the encoded less than sign, which
                is the most common element to be in encoded HTML and least likely to be seen anywhere else.-->
                <!--
                -->
                
                <xsl:choose>
                  <xsl:when test="contains(*[local-name()='description'], '&lt;')">
                
                <!-- The 'value-of' line below will render entity-encoded HTML, and standard HTML will be written as plain text.
                     Any version of uPortal earlier than 2.2 will not allow encoded HTML to be rendered without tweaking at the Java level.
                     uPortal 2.2 and higher have the property, 'org.jasig.portal.serialize.BaseMarkupSerializer.allow_disable_output_escaping=no'
                     which is set to 'no' by default.  Changing this property to 'yes' will allow encoded HTML to render with the 'value-of' statement
                     written below.  Note: when enabling the line below, comment out the 'copy-of' line above, or you will risk listing each 'description' twice. -->
                 
                <xsl:value-of disable-output-escaping="yes" select="*[local-name()='description']"/>
                 
                 
                  </xsl:when>
                  <xsl:otherwise>
                
                <!--
                The 'copy-of' line below will render standard HTML within the 'description' area.  Entity-Encoded HTML will be written as plain text.
                -->
                <xsl:copy-of select="*[local-name()='description']"/>
                
                  </xsl:otherwise>
                </xsl:choose>
                
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td width="100%">
                <xsl:value-of select="*[local-name()='description']"/>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </tr>
      </xsl:if>
      <xsl:if test="$showInfo='true' and ../parent::rss[@version='2.0']">
        <!--  and ../../*=rss[@version='2.0'] -->
        <tr>
          <td>
            <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
          </td>
          <td width="100%">
            <span class="uportal-text-small">
              <xsl:if test="author">[author:&#x20;<xsl:value-of select="author"/>]<br/>
              </xsl:if>
              <xsl:if test="category">[category:&#x20;<xsl:value-of select="category"/>]<br/>
              </xsl:if>
              <xsl:if test="comments">[comments:&#x20;<xsl:value-of select="comments"/>]<br/>
              </xsl:if>
              <xsl:if test="enclosure">[enclosure:&#x20;<xsl:value-of select="enclosure"/>]<br/>
              </xsl:if>
              <xsl:if test="guid">[GUID:&#x20;<xsl:value-of select="guid"/>]<br/>
              </xsl:if>
              <xsl:if test="pubDate">[date:&#x20;<xsl:value-of select="pubDate"/>]<br/>
              </xsl:if>
              <xsl:if test="source">source:&#x20;<xsl:value-of select="source"/>]<br/>
              </xsl:if>
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </span>
          </td>
        </tr>
      </xsl:if>
    </table>
    <xsl:apply-templates select="dc:*"/>
    <xsl:apply-templates select="str:*"/>
    <xsl:apply-templates select="ev:*"/>
	</xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="*[local-name()='textinput']">
    <xsl:if test="not(name(parent::*)='channel')">
      <form action="{*[local-name()='link']}">
        <span class="uportal-label">
          <xsl:value-of select="*[local-name()='description']"/>
        </span>
        <br/>
        <input type="text" name="{*[local-name()='name']}" size="30" class="uportal-input-text"/>
        <br/>
        <input type="submit" name="Submit" value="Submit" class="uportal-button"/>
      </form>
    </xsl:if>
  </xsl:template>
  <!--
 -->
  <xsl:template name="copyright">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="100%" align="center" class="uportal-text-small"> &#xA9;<xsl:value-of select="/rdf:RDF/*[local-name()='channel']/dc:rights"/>
        </td>
      </tr>
    </table>
  </xsl:template>
  <!--
   -->
  <xsl:template name="rssImage">
    <xsl:if test="image or ../*[local-name()='image']">
      <a target="_blank">
        <xsl:attribute name="href">
          <xsl:choose>
            <xsl:when test="parent::rss">
              <xsl:value-of select="image/link"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="../*[local-name()='image']/*[local-name()='link']"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <img border="0">
          <xsl:attribute name="src">
            <xsl:choose>
              <xsl:when test="parent::rss">
                <xsl:value-of select="image/url"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="../*[local-name()='image']/*[local-name()='url']"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:attribute name="alt">
            <xsl:choose>
              <xsl:when test="parent::rss">
                <xsl:value-of select="image/title"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="../*[local-name()='image']/*[local-name()='title']"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:choose>
              <xsl:when test="parent::rss">
                <xsl:value-of select="image/title"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="../*[local-name()='image']/*[local-name()='title']"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </img>
      </a>
    </xsl:if>
  </xsl:template>
  <!--
  -->
  <xsl:template name="month">
    <xsl:param name="monthNumber"/>
    <xsl:if test="$monthNumber = '01'">January&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '02'">February&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '03'">March&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '04'">April&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '05'">May&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '06'">June&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '07'">July&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '08'">August&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '09'">September&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '10'">October&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '11'">November&#x20;</xsl:if>
    <xsl:if test="$monthNumber = '12'">December&#x20;</xsl:if>
  </xsl:template>
  <!--
   -->
  <xsl:template match="@*|text()"/>
  <!--
   -->
</xsl:stylesheet>
