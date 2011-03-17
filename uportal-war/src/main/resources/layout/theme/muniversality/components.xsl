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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    exclude-result-prefixes="upAuth upGroup upMsg" 
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
    <div data-role="header" data-backbtn="false" data-position="inline">
        <h1>uMobile</h1>
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
                                <xsl:call-template name="portletUrl">
                                    <xsl:with-param name="fname">portal_login_general</xsl:with-param>
                                    <xsl:with-param name="state">MAXIMIZED</xsl:with-param>
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
<xsl:template name="mobile.navigation">
    <xsl:for-each select="//group">
        <xsl:if test="count(channel) > 0">
            <ul data-role="listview" data-inset="true">
                <li data-role="list-divider"><xsl:value-of select="navblock/@name"/></li>
                <xsl:for-each select="channel">
                    <li>
                        <xsl:variable name="portletUrl">
                            <xsl:call-template name="portletUrl">
                                <xsl:with-param name="subscribeId" select="@ID" />
                                <xsl:with-param name="state">MAXIMIZED</xsl:with-param>
                            </xsl:call-template>
                        </xsl:variable>
                        <a href="{$portletUrl}" title="To view {@name}">
                            <!--img class="fl-icon" src="{@iconUrl}"/>-->
                            <xsl:value-of select="@name" />
                        </a>
                        <xsl:variable name="newItemCountClasses">
                            ui-li-count up-new-item-count-{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}
                        </xsl:variable>
                        <span class="{$newItemCountClasses}">{up-portlet-new-item-count(<xsl:value-of select="@ID" />)}</span>
                    </li>
                </xsl:for-each>
            </ul>
        </xsl:if>
    </xsl:for-each>
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
        <xsl:call-template name="portletUrl">
            <xsl:with-param name="fname"><xsl:value-of select="//content/focused/channel/@fname"/></xsl:with-param>
            <xsl:with-param name="state">MINIMIZED</xsl:with-param>
        </xsl:call-template>
    </xsl:variable>
    <xsl:if test="$NATIVE != 'true'">
        <div data-role="header" data-position="inline">
            <a href="{$basePortalUrl}" data-icon="home" data-direction="reverse">
                <xsl:value-of select="upMsg:getMessage('home', $USER_LANG)"/>
            </a>
            <h1><xsl:value-of select="//content/focused/channel/@name" /></h1>
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
    <div class="portlet-content-container">
        <xsl:copy-of select="//content/focused/channel" />
    </div>
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>