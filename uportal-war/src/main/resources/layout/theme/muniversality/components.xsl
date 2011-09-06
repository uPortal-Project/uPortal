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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:upAuthentication="xalan://org.jasig.portal.security.xslt.XalanAuthenticationHelper">


<!-- ======================================================================================================================================================== -->
<!-- ========== PUBLIC VIEW ================================================================================================================================= -->
<!-- ======================================================================================================================================================== -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE WRAPPER GUEST =============================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate adds a 'mobile-guest' class name wrapper.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.wrapper.guest">
	<xsl:attribute name="class">mobile-guest</xsl:attribute>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE HEADER PUBLIC =============================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the header when unauthenticated.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.header.public">
	<div class="mobile-header">
        
        <!--*** start: logo ***-->
        <div class="mobile-logo">
            <img src="{$SKIN_PATH}/images/mportal_logo.gif" title="{$TOKEN[@name='LOGO_TITLE']}" alt="{$TOKEN[@name='LOGO']}" />
        </div>
        <!--*** end: logo ***-->
        
        <!--*** start: welcome ***-->
        <h4>
            <xsl:value-of select="$TOKEN[@name='WELCOME_PRE']" />
            <xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_HEADER']" />
            <xsl:value-of select="$TOKEN[@name='WELCOME_POST']" />
        </h4>
        <!--*** end: welcome ***-->
        
    </div>
</xsl:template>
<!-- ========================================================================= -->



<!-- ======================================================================================================================================================== -->
<!-- ========== AUTHENTICATED VIEW ========================================================================================================================== -->
<!-- ======================================================================================================================================================== -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE WRAPPER ===================================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate adds a 'mobile' class name wrapper.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.wrapper">
	<xsl:attribute name="class">mobile</xsl:attribute>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE HEADER ====================================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the header when authenticated.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.header">
    <div class="flc-screenNavigator-navbar fl-navbar fl-table">
        <div class="fl-table-row">
            <h1 class="fl-table-cell"><xsl:value-of select="$TOKEN[@name='PORTAL_NAME']"/></h1>
            <div class="fl-table-cell up-mobile-nav">
                <xsl:call-template name="mobile.auth.link"/>
            </div>
        </div>
    </div>
</xsl:template>
    
<xsl:template name="mobile.auth.link">
    <xsl:choose>
        <xsl:when test="$AUTHENTICATED='true'">
            <a id="up-page-auth-button" href="Logout" title="{$TOKEN[@name='LOGOUT_LONG_LABEL']}" class="fl-button">
                <span class="fl-button-inner"><xsl:value-of select="$TOKEN[@name='LOGOUT_LABEL']"/></span>
            </a>
        </xsl:when>
        <xsl:otherwise>
            <a id="up-page-auth-button" title="{$TOKEN[@name='LOGIN_LONG_LABEL']}" class="fl-button">
                <xsl:attribute name="href">
                    <xsl:choose>
                        <xsl:when test="$EXTERNAL_LOGIN_URL != ''">
                            <xsl:value-of select="$EXTERNAL_LOGIN_URL"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$BASE_ACTION_URL"/>?uP_fname=portal_login_general
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <span class="fl-button-inner"><xsl:value-of select="$TOKEN[@name='LOGIN_LABEL']"/></span>
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
<xsl:template match="mobilenavigation">
    <div class="fl-panel">
        <div class="fl-note fl-bevel-white fl-font-size-80 right">
            <xsl:if test="$AUTHENTICATED='true'">
                Welcome <xsl:value-of select="$USER_NAME"/>            
            </xsl:if>
        </div>
    </div>
    <xsl:variable name="ALL_GROUPS" select="//group" />
    <xsl:for-each select="$ALL_GROUPS">
        <xsl:if test="count(channel-nav) > 0">
            <div id="tab-section_{navblock/@ID}" class="fl-container fl-container-autoHeading">
                <h3><xsl:value-of select="navblock/@name"/></h3>
                <ul class="fl-list-menu fl-list-brief">
                    <xsl:for-each select="channel-nav">
                        <li>
                            <a class="flc-screenNavigator-backButton" href="{$BASE_ACTION_URL}?uP_sparam=activeTab&amp;amp;activeTab={../navblock/@ID}&amp;amp;uP_root={@ID}" title="To view {@name}">
                                <xsl:value-of select="@name" />
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </div>
        </xsl:if>
    </xsl:for-each>
</xsl:template>
<!-- ========================================================================= -->

<xsl:template name="mobile.navigation">
    <xsl:if test="count(//mobilenavigation/group) &gt; 0">
        <div class="up-mobile-navigation-container fl-panel up-mobile-nav">
            <xsl:apply-templates select="mobilenavigation"/>
        </div>        
    </xsl:if>
</xsl:template>

<xsl:template name="mobile.navigation.focused">
    <xsl:if test="count(//mobilenavigation/group) &gt; 0">
        <div class="up-mobile-navigation-container fl-panel up-mobile-nav" style="display:none;">
            <xsl:apply-templates select="mobilenavigation"/>
        </div>        
    </xsl:if>
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
    <div class="flc-screenNavigator-navbar fl-navbar fl-table">
        <div class="fl-table-row">
            
            <div class="fl-table-cell up-mobile-focus">
                <a id="up-page-back-button" href="{$BASE_ACTION_URL}?uP_root=root" class="fl-button fl-backButton">
                    <span class="fl-button-inner"><xsl:value-of select="$TOKEN[@name='FOCUSED_BACK']"/></span>
                </a>
            </div>

            <xsl:call-template name="mobile.channel.title.focused"/>

            <div class="fl-table-cell up-mobile-nav" style="display:none">
                <xsl:call-template name="mobile.auth.link"/>
            </div>
            
        </div>
    </div>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE CHANNEL TITLE FOCUSED ======================= -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the channel's title.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.channel.title.focused">
    <h1 class="fl-table-cell up-mobile-nav" style="display:none"><xsl:value-of select="$TOKEN[@name='PORTAL_NAME']"/></h1>
    <h1 class="fl-table-cell up-mobile-focus">
        <xsl:value-of select="content/focused/channel/@name" />
    </h1>
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
    <div class="up-mobile-focused-content up-mobile-focus">
        <xsl:apply-templates select="content" mode="focused" />
    </div>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: FOCUSED CONTENT ==================================== -->
<!-- ========================================================================= -->
<!-- 
| RED
| This template copies the portlet or channel content into the portal 
| to be rendered when content is focused. This template should not 
| need alteration and only those with knowledge of xsl should configure
| this template. Template contents can be any valid XSL or XHTML.
-->
<xsl:template match="content/focused/channel" mode="focused">
    <div class="portlet-content-container">
        <xsl:copy-of select="." />
    </div>
</xsl:template>
<!-- ========================================================================= -->


<xsl:template name="mobile.navigation.script">
    <script type="text/javascript">
        up.jQuery(document).ready(function(){
            up.jQuery("#up-page-back-button").click(function(){
                up.jQuery(".up-mobile-focus").hide();
                up.jQuery(".up-mobile-nav").show();
                return false;
            }).attr("href","javascript:;");
        });
    </script>
</xsl:template>


<!-- ======================================================================================================================================================== -->
<!-- ========== COMMON ====================================================================================================================================== -->
<!-- ======================================================================================================================================================== -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE FOOTER ====================================== -->
<!-- ========================================================================= -->
<!-- 
| YELLOW
| The footer template currently contains the portal's copyright information. This area can be 
| customized to contain any number of links or institution identifiers. This template renders 
| in all areas of the portal (unauthenticated, focused and non-focused). Modifications to the 
| footer should be made in muniversality.xsl under the footer template. Only those with knowledge 
| of xsl should configure this template. Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="mobile.footer">
	<xsl:call-template name="footer" />
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>