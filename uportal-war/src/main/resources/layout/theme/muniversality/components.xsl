<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<!-- ============================= -->
<!-- ========== README =========== -->
<!-- ============================= -->
<!-- 
 | Date: 08/14/2008
 | Author: Matt Polizzotti
 | Company: Unicon,Inc.
 | uPortal Version: uP3.0.0 and uP3.0.1
 |
 | This file determines the presentation of UI components of the mobile portal.
 | The file is imported by the base stylesheet muniversality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to muniversality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xmlns:upElemTitle="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanLayoutElementTitleHelper"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg upElemTitle" 
    version="1.0">
    
<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE HEADER ====================================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the header when authenticated.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.header">
    <div class="titlebar portlet-wrapper-titlebar" data-role="header" data-backbtn="false" data-position="inline">
        <h1 class="title">uMobile</h1>
        <xsl:call-template name="mobile.auth.link"/>
    </div>
</xsl:template>
    
<xsl:template name="mobile.auth.link">
    <xsl:choose>
        <xsl:when test="$AUTHENTICATED='true'">
            <a href="{$CONTEXT_PATH}/Logout" title="{upMsg:getMessage('logout', $USER_LANG)}" class="ui-btn-right">
                <span><xsl:value-of select="upMsg:getMessage('logout', $USER_LANG)"/></span>
            </a>
        </xsl:when>
        <xsl:otherwise>
            <a title="{upMsg:getMessage('home', $USER_LANG)}" class="ui-btn-right">
                <xsl:attribute name="href">
                    <xsl:choose>
                        <xsl:when test="$EXTERNAL_LOGIN_URL != ''">
                            <xsl:value-of select="$EXTERNAL_LOGIN_URL"/>
                        </xsl:when>
                        <xsl:otherwise>
                        	<xsl:variable name="portletLoginUrl">
                                <xsl:call-template name="portalUrl">
                                    <xsl:with-param name="url">
                                        <url:portal-url>
                                            <url:fname>login</url:fname>
                                            <url:portlet-url state="MAXIMIZED" />
                                        </url:portal-url>
                                    </xsl:with-param>
                                </xsl:call-template>
                        	</xsl:variable>
                            <xsl:value-of select="$portletLoginUrl"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <span><xsl:value-of select="upMsg:getMessage('login', $USER_LANG)"/></span>
            </a>
        </xsl:otherwise>
    </xsl:choose>        
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILENAVIGATION =================================== -->
<!-- ========================================================================= -->
<!-- 
| YELLOW
| The mobilenavigation template creates a list-based view of the portal's tabs and channels. uPortal's tab names 
| are rendered as <h2> headlines and the tab's associated channels or portlets are rendered as linked <li> 
| list-items within an <ul> list. Only those with knowledge of xsl should configure this template. 
| Template contents can be any valid XSL or XHTML.
-->  
<xsl:template name="mobile.navigation.grid">
    <div class="portal-nav">
        <xsl:for-each select="//navigation/tab/channel">
            <div class="portlet">
                <xsl:variable name="defaultPortletUrl">
                    <xsl:call-template name="portalUrl">
                        <xsl:with-param name="url">
                            <url:portal-url>
                                <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                                <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                            </url:portal-url>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="portletUrl">{up-portlet-link(<xsl:value-of select="@ID" />,<xsl:value-of select="$defaultPortletUrl" />)}</xsl:variable>
                <xsl:variable name="newItemCountClasses">
                    badge new-item up-new-item-count-{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}
                </xsl:variable>
                <xsl:variable name="iconUrl">
                    <xsl:choose>
                        <xsl:when test="parameter[@name='mobileIconUrl'] and parameter[@name='mobileIconUrl']/@value != ''">
                            <xsl:value-of select="parameter[@name='mobileIconUrl']/@value"/>
                        </xsl:when>
                        <xsl:otherwise><xsl:value-of select="$CONTEXT_PATH"/>/media/skins/icons/mobile/default.png</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <a href="{$portletUrl}" title="To view {@name}">
                    <span class="{$newItemCountClasses}">{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}</span>
                    <span class="icon"><img class="portlet-icon" src="{$iconUrl}" alt="{@name}"/></span>
                    <span class="title"><xsl:value-of select="@name" /></span>
                </a>
            </div>
        </xsl:for-each>
    </div>
</xsl:template>

<xsl:template name="mobile.navigation.list">
    <div class="portal-nav portlet-content">
        <ul data-role="listview" class="up-portal-nav">
            <xsl:for-each select="//navigation/tab">
            <li data-role="list-divider"><xsl:value-of select="upElemTitle:getTitle(@ID, $USER_LANG, @name)"/></li>
                <xsl:for-each select="channel">
                    <li>
                        <xsl:variable name="defaultPortletUrl">
                            <xsl:call-template name="portalUrl">
                                <xsl:with-param name="url">
                                    <url:portal-url>
                                        <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                                        <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                                    </url:portal-url>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="portletUrl">{up-portlet-link(<xsl:value-of select="@ID" />,<xsl:value-of select="$defaultPortletUrl" />)}</xsl:variable>
                        <xsl:variable name="iconUrl">
                            <xsl:choose>
                                <xsl:when test="parameter[@name='mobileIconUrl'] and parameter[@name='mobileIconUrl']/@value != ''">
                                    <xsl:value-of select="parameter[@name='mobileIconUrl']/@value"/>
                                </xsl:when>
                                <xsl:otherwise><xsl:value-of select="$CONTEXT_PATH"/>/media/skins/icons/mobile/default.png</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="newItemCountClasses">ui-li-count badge new-item up-new-item-count-{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}</xsl:variable>
                        <a href="{$portletUrl}" title="To view {@name}">
                            <img class="portlet-icon" src="{$iconUrl}" alt="{@name}"/>
                            <h3><xsl:value-of select="@name" /></h3>
                            <p><xsl:value-of select="@description"/></p>
                            <span class="{$newItemCountClasses}">{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}</span>
                        </a>
                    </li>
                </xsl:for-each>
            </xsl:for-each>
        </ul>
    </div>
</xsl:template>

<!-- ======================================================================================================================================================== -->
<!-- ========== FOCUSED VIEW ================================================================================================================================ -->
<!-- ======================================================================================================================================================== -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE HEADER FOCUSED ============================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the header when focused.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.header.focused">
    <xsl:variable name="basePortalUrl">
        <xsl:call-template name="portalUrl" />
    </xsl:variable>
    <xsl:if test="not(//content/focused/@detached = 'true')">
        <div class="titlebar portlet-wrapper-titlebar" data-role="header" data-position="inline">
            <a href="{$basePortalUrl}" data-icon="home" data-direction="reverse">
                <xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/>
            </a>
            <h1 class="title"><xsl:value-of select="//content/focused/channel/@name" /></h1>
        </div>
    </xsl:if>
</xsl:template>
<!-- ========================================================================= -->

<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE CHANNEL CONTENT FOCUSED ===================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders channel and portlet content when focused.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.channel.content.focused">
  <xsl:choose>
    <xsl:when test="//content/focused/blocked-channel">
        <xsl:choose>
            <xsl:when test="parameter[@name='blockImpersonation']/@value = 'true'">
                <div><p><em><xsl:value-of select="upMsg:getMessage('hidden.in.impersonation.view', $USER_LANG)"/></em></p></div>
            </xsl:when>
            <xsl:otherwise>
                <div><p><em><xsl:value-of select="upMsg:getMessage('channel.blocked', $USER_LANG)"/></em></p></div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="//content/focused/channel" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>
