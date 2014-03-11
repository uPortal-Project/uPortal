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

<!--
 | This file defines areas or _Regions_ of the page in which non-tab/column
 | portlets may be placed.  Regions with portlets present must display them
 | properly;  regions without portlets must "disappear" gracefully.  ALL of the
 | essential page structure markup (related to regions) MUST be provided by the
 | regions themselves, not by rendered portlets.
 |
 | The file is imported by the base stylesheet respondr.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to respondr.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<!-- ============================================= -->
<!-- ========== STYLESHEET DELCARATION =========== -->
<!-- ============================================= -->
<!--
 | RED
 | This statement defines this document as XSL and declares the Xalan extension
 | elements used for URL generation and permissions checks.
 |
 | If a change is made to this section it MUST be copied to all other XSL files
 | used by the theme
-->
<xsl:stylesheet
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg dlm xsi"
    version="1.0">

    <!-- ========== TEMPLATE: HIDDEN-TOP ========== -->
    <!-- ======================================== -->
    <!--
     | This template renders portlets at the very top of the page, across the entire width.
    -->
    <xsl:template name="region.hidden-top">
        <xsl:if test="//region[@name='hidden-top']/channel">
            <div id="region-page-top-hidden" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='hidden-top']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: PAGE-TOP ========== -->
    <!-- ======================================== -->
    <!--
     | This template renders portlets at the very top of the page, across the entire width.
    -->
    <xsl:template name="region.page-top">
        <xsl:if test="//region[@name='page-top']/channel">
            <div id="region-page-top" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='page-top']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: PRE-HEADER ========== -->
    <!-- ========================================== -->
    <!--
     | This template renders portlets in the top-right greeting area.
    -->
    <xsl:template name="region.pre-header">
        <xsl:if test="//region[@name='pre-header']/channel">
            <div id="region-pre-header" class="portal-user">
                <xsl:for-each select="//region[@name='pre-header']/channel">
                    <xsl:call-template name="regions.portlet.decorator" />
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: HEADER-LEFT ========== -->
    <!-- =========================================== -->
    <!--
     | This template renders portlets in the top-left logo area.
     | Even if empty this region must take up column space on the left.
    -->
    <xsl:template name="region.header-left">
        <xsl:choose>
            <xsl:when test="//region[@name='header-left']/channel">
                <div id="region-header-left" class="col-sm-8 text-left">
                    <xsl:for-each select="//region[@name='header-left']/channel">
                        <xsl:call-template name="regions.portlet.decorator" />
                    </xsl:for-each>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <div id="empty-region-header-left" class="col-sm-8 text-left"></div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ========== TEMPLATE: HEADER-RIGHT ========== -->
    <!-- ============================================ -->
    <!--
     | This template renders portlets in the top-right search area.
     | Even if empty this region must take up column space on the right.
    -->
    <xsl:template name="region.header-right">
        <xsl:choose>
            <xsl:when test="//region[@name='header-right']/channel">
                <div id="region-header-right" class="col-sm-4 text-right">
                    <xsl:for-each select="//region[@name='header-right']/channel">
                        <xsl:call-template name="regions.portlet.decorator" />
                    </xsl:for-each>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <div id="empty-region-header-right" class="col-sm-4 text-right"></div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ========== TEMPLATE: HEADER-BOTTOM ========== -->
    <!-- ========================================= -->
    <!--
     | This template renders portlets at the bottom of the header area.
    -->
    <xsl:template name="region.header-bottom">
        <xsl:if test="//region[@name='header-bottom']/channel">
            <div id="region-header-bottom" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='header-bottom']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: PRE-CONTENT ========== -->
    <!-- =========================================== -->
    <!--
     | This template renders portlets in the area just above content (columns or focused portlet).
    -->
    <xsl:template name="region.pre-content">
        <xsl:if test="//region[@name='pre-content']/channel">
            <div id="region-pre-content" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='pre-content']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: PAGE-BOTTOM ========== -->
    <!-- =========================================== -->
    <!--
     | This template renders portlets at the very top of the page, across the entire width.
    -->
    <xsl:template name="region.page-bottom">
        <xsl:if test="//region[@name='page-bottom']/channel">
            <div id="region-page-bottom" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='page-bottom']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: HIDDEN-BOTTOM ========== -->
    <!-- =========================================== -->
    <!--
     | This template renders portlets at the very top of the page, across the entire width.
    -->
    <xsl:template name="region.hidden-bottom">
        <xsl:if test="//region[@name='hidden-bottom']/channel">
            <div id="region-page-bottom-hidden" class="container">
                <div class="row">
                    <div class="col-sm-12">
                        <xsl:for-each select="//region[@name='hidden-bottom']/channel">
                            <xsl:call-template name="regions.portlet.decorator" />
                        </xsl:for-each>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ========== TEMPLATE: CUSTOMIZE ========== -->
    <!-- ======================================= -->
    <!--
     | This template renders portlets in the top-left logo area.
    -->
    <xsl:template name="region.customize">
        <xsl:if test="upAuth:hasPermission('UP_SYSTEM', 'CUSTOMIZE', 'ALL')">
            <xsl:if test="//region[@name='customize']/channel">
                <div id="region-customize" class="container">
                    <div id="customizeOptionsWrapper">
                        <div id="customizeOptions" class="collapse">
                                <xsl:for-each select="//region[@name='customize']/channel">
                                    <xsl:call-template name="regions.portlet.decorator" />
                                </xsl:for-each>
                        </div>
                        <button type="button" class="btn btn-default" data-toggle="collapse" data-target="#customizeOptions">CUSTOMIZE <i class="fa"></i></button>
                    </div>
                </div>
            </xsl:if>
        </xsl:if>
    </xsl:template>


    <!-- ========== TEMPLATE: REGIONS PORTLET DECORATOR ========== -->
    <!-- ========================================================= -->
    <!--
     | This template decorates a portlet that appears in a region (in lieu of chrome).
    -->
    <xsl:template name="regions.portlet.decorator">
        <section id="portlet_{@ID}" class="up-portlet-wrapper {@fname}">
            <xsl:if test="@portletMode!='edit' and @portletMode!='config' and @windowState!='minimized'">
                <xsl:call-template name="regions.hover-menu"/>
            </xsl:if>
            <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
        </section>
    </xsl:template>


    <!-- ========== TEMPLATE: REGIONS HOVER MENU ========== -->
    <!-- ========================================================= -->
    <!--
     | For portlets in regions, the markup in this template provides access to 
     | some functions that normally appear in portlet chrome (e.g. EDIT and CONFIG).
    -->
    <xsl:template name="regions.hover-menu">
        <xsl:variable name="editable">
            <xsl:if test="parameter[@name='editable']/@value = 'true'">true</xsl:if>
        </xsl:variable>
        <xsl:variable name="canConfigure">
            <!-- This option is special in that it evaluates both whether (1) the portlet supports CONFIG mode and (2) this user is allowed to access it. -->
            <xsl:if test="parameter[@name='configurable']/@value = 'true' and upAuth:hasPermission('UP_PORTLET_PUBLISH', 'PORTLET_MODE_CONFIG', 'PORTLET_ID.@chanID')">true</xsl:if>
        </xsl:variable>
        <xsl:if test="$editable='true' or $canConfigure='true'">
            <ul class="hover-chrome">
                <xsl:if test="$editable='true'">
                    <xsl:variable name="portletEditUrl">
                        <xsl:call-template name="portalUrl">
                            <xsl:with-param name="url">
                                <url:portal-url>
                                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                                    <url:portlet-url mode="EDIT" copyCurrentRenderParameters="true" />
                                </url:portal-url>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>
                    <li class="hover-option">
                        <a href="{$portletEditUrl}#{@ID}" title="{upMsg:getMessage('edit.portlet', $USER_LANG)}" class="up-portlet-control edit"><i class="fa fa-edit"></i></a>
                    </li>
                </xsl:if>
                <xsl:if test="$canConfigure='true'">
                    <xsl:variable name="portletConfigureUrl">
                        <xsl:call-template name="portalUrl">
                            <xsl:with-param name="url">
                                <url:portal-url>
                                    <url:fname><xsl:value-of select="@fname"/></url:fname>
                                    <url:portlet-url mode="CONFIG" copyCurrentRenderParameters="true" />
                                </url:portal-url>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>
                    <li class="hover-option">
                        <a href="{$portletConfigureUrl}" title="{upMsg:getMessage('configure.portlet', $USER_LANG)}" class="up-portlet-control configure"><i class="fa fa-gears"></i></a>
                    </li>
                </xsl:if>
            </ul>
            <script type="text/javascript">
                up.jQuery(document).ready(function() {
                    $('section.<xsl:value-of select="@fname" />').hover(function() {
                        $(this).find('.hover-chrome').stop(true, true).slideDown('medium');
                    }, 
                    function() {
                        $(this).find('.hover-chrome').stop(true,true).slideUp('medium');
                    });
                });
            </script>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
