<?xml version='1.0'?>
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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:variable name="perScreen">20</xsl:variable>
    
    <xsl:template match="/">
        
        <style type="text/css">
            .fluid-pager ul {margin:0;}
            .portal-deployments .portal-deployment-items li {line-height:1.7em}
        </style>
        
        <h2>uPortal Deployments</h2>
        
        <div id="portal-deployment-list" class="portal-deployments fl-pager">
            <xsl:call-template name="nav">
                <xsl:with-param name="position">top</xsl:with-param>
            </xsl:call-template>
            <br/>
            
            <ul class="portal-deployment-items">
                <xsl:apply-templates select="rss/channel/item"/>
            </ul>
            
            <br/>
            <xsl:call-template name="nav">
                <xsl:with-param name="position">bottom</xsl:with-param>
            </xsl:call-template>
        </div>

        <script type="text/javascript">
            up.jQuery(document).ready(function(){
                up.fluid.pager("#portal-deployment-list", {
                    listeners: {
                        onModelChange: function(link) {
                            up.jQuery("#portal-deployment-list .portal-deployment-items li").css("display", "none");
                            var i = link.pageIndex*<xsl:value-of select="$perScreen"/>;
                            up.jQuery("#portal-deployment-list .portal-deployment-items li:not(:lt(" + link.pageIndex*<xsl:value-of select="$perScreen"/> +")):lt(" + <xsl:value-of select="$perScreen"/> + ")").css("display", "block");
                            return false;
                        }
                    }
                });
                up.jQuery("#portal-deployment-list .portal-deployment-items a").tooltip();
            });
        </script>
        
    </xsl:template>

    <xsl:template match="item">
        <li>
            <xsl:choose>
                <xsl:when test="link">
                    <xsl:variable name="description">
                        <xsl:choose>
                            <xsl:when test="description"><xsl:value-of select="description"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="title"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <a href="{link}" title="{$description}" target="_blank"><xsl:value-of select="title"/></a>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="title"/></xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>
    
    <xsl:template name="nav">
        <xsl:param name="position">top</xsl:param>
        
        <div id="pager-top" class="flc-pager-{$position}">
            <ul class="fl-pager-ui pager-{$position}">
                <li class="fl-pager-previous flc-pager-previous"><a href="javascript:;">&lt; previous</a></li>
                    <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                        <xsl:call-template name="navLink">
                        <xsl:with-param name="i">1</xsl:with-param>
                        <xsl:with-param name="max">
                            <xsl:value-of select="ceiling(count(rss/channel/item) div $perScreen)"/>
                        </xsl:with-param>
                        </xsl:call-template>
                    </ul>
                <li class="fl-pager-next flc-pager-next"><a href="javascript:">next &gt;</a></li>
            </ul>
        </div>
    </xsl:template>
    
    <xsl:template name="navLink">
        <xsl:param name="i" />
        <xsl:param name="max" />
        <xsl:if test="$i &lt;= $max">
            <li value="{$i}" class="flc-pager-pageLink">
                <a href="javascript:;"><xsl:value-of select="$i"/></a>    
            </li>
        </xsl:if>
        <xsl:if test="$i &lt;= $max">
            <xsl:call-template name="navLink">
                <xsl:with-param name="i">
                    <xsl:value-of select="$i + 1"/>
                </xsl:with-param>
                <xsl:with-param name="max">
                    <xsl:value-of select="$max"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if> 
    </xsl:template>
    
</xsl:stylesheet>