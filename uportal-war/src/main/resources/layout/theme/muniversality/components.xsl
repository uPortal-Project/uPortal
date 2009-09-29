<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


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


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE LOGIN PUBLIC ================================ -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders the login when unauthenticated.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.login.public">
	<div class="mobile-login">
        <div class="mobile-login-wrap">
            <xsl:copy-of select="/layout/header/channel[@name='Login']" />
        </div>
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
    <div class="mobile-header">
        
        <!--*** start: logo ***-->
        <div class="mobile-logo">
            <img src="{$SKIN_PATH}/images/mportal_logo.gif" title="{$TOKEN[@name='LOGO_TITLE']}" alt="{$TOKEN[@name='LOGO']}" />
        </div>
        <!--*** end: logo ***-->
        
        <!--*** start: login ***-->
        <div class="mobile-authenticated">
            <xsl:if test="$AUTHENTICATED='true'">
                <xsl:copy-of select="/layout/header/channel[@name='Login']" />
            </xsl:if>
        </div>
        <!--*** end: login ***-->
        
    </div>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE CHANNEL CONTENT ============================= -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders channel and portlet content when authenticated.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.channel.content">
    <xsl:if test="count(//mobilenavigation/group) &gt; 0">
        <div class="mobile-content">
            <xsl:apply-templates select="mobilenavigation" />
        </div>
    </xsl:if>
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
    <xsl:variable name="ALL_GROUPS" select="//group" />
    <xsl:for-each select="$ALL_GROUPS">
        <h2 class="mobile-nav-tab" id="{navblock/@name}"><xsl:value-of select="navblock/@name" /></h2>
         <ul class="mobile-nav-link-container">
            <xsl:for-each select="channel">
                <li class="mobile-nav-link">
                    <a href="{$BASE_ACTION_URL}?uP_root={@ID}" title="To view {@name}">
                        <xsl:value-of select="@name" />
                    </a>
                </li>
            </xsl:for-each>
         </ul>
    </xsl:for-each>
</xsl:template>
<!-- ========================================================================= -->





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
    <div class="mobile-header">					
        <div class="mobile-logo">
            <img src="{$SKIN_PATH}/images/mportal_logo_slim.gif" title="{$TOKEN[@name='LOGO_TITLE']}" alt="{$TOKEN[@name='LOGO']}" />
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
	<div class="mobile-channel-title">
    	<h2><xsl:value-of select="mobilenavigation/focused/channel/@name" /></h2>
    </div>
</xsl:template>
<!-- ========================================================================= -->

<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE BACK FOCUSED ================================ -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders a 'Go Back To Main Link' when focused.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.back.focused">
    <div class="mobile-back-to-main">
        <a class="return" title="{$TOKEN[@name='BACK_TO_MAIN_TITLE']}" href="{$BASE_ACTION_URL}?uP_root=root">
            <img src="{$SKIN_PATH}/images/arrow.gif" alt="{$TOKEN[@name='BACK_TO_MAIN_IMG_ALT']}" title="{$TOKEN[@name='BACK_TO_MAIN_IMG_TITLE']}" border="0" />
            <span><xsl:value-of select="$TOKEN[@name='BACK_TO_MAIN_SPAN_TEXT']" /></span>
        </a>
    </div>
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
    <div class="mobile-content">
        <xsl:apply-templates select="mobilenavigation" mode="focused" />
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
<xsl:template match="mobilenavigation/focused/channel" mode="focused">
    <div class="portlet-content-container">
        <xsl:copy-of select="." />
    </div>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: MOBILE SELECT NAVIGATION FOCUSED =================== -->
<!-- ========================================================================= -->
<!--
| YELLOW
| This tempate renders a select drop-down form element when focused.
| Only those with knowledge of xsl should configure this template.
-->
<xsl:template name="mobile.select.navigation.focused">
    <xsl:if test="$USE_SELECT_DROP_DOWN='true'">
        <div class="mobile-select-nav">
            <form method="GET" action="{$BASE_ACTION_URL}">
                <label for="portletNavigationSelect"><xsl:value-of select="$TOKEN[@name='FORM_SELECT_LABEL']" /></label>
                <select name="uP_root" class="mobile-nav-select">
                    <xsl:apply-templates select="mobilenavigation" mode="select" />
                </select>
                <input type="submit" value="{$TOKEN[@name='FORM_SELECT_SUBMIT_VALUE']}"></input>
            </form>
        </div>
    </xsl:if>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: SELECT NAVIGATION FOCUSED - OPTIONS VALUE ========== -->
<!-- ========================================================================= -->
<!-- 
| YELLOW
| The below template defines the "selected" attribute for the <option> tag 
| contained within the <select> drop-down. Only those with knowledge of xsl 
| should configure this template. In short, when a user selects a channel 
| or portlet from the select drop-down and clicks the "GO" button, this template 
| tells the <option> tag to "select" and display the channel chosen by the user.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template match="mobilenavigation/focused/selection/channel" mode="select">
    <xsl:variable name="CHANNEL_ID" select="@ID" />
    <xsl:variable name="CHANNEL_NAME" select="@name" />
    <xsl:variable name="FOCUSED_CHANNEL_ID" select="../../channel/@ID" />
    <xsl:for-each select="$CHANNEL_ID">
        <option value="{$CHANNEL_ID}">
            <xsl:if test="$CHANNEL_ID=$FOCUSED_CHANNEL_ID">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$CHANNEL_NAME" />
        </option>
    </xsl:for-each>
</xsl:template>
<!-- ========================================================================= -->





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