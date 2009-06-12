<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!--=====START: DOCUMENT DESCRIPTION=====-->
<!--    
    Date: 08/14/2008
    Author: Matt Polizzotti
    Company: Unicon,Inc.
    uPortal Version: UP3.0.0 and up.
    
    General Description: This file, mobile-tab-column.xsl, was developed in order to 
    enable uPortal 3.0.0 to be viewable by mobile devices. This file exports xml, which 
    is then transformed by the muniversality.xsl file into the HTML markup rendered in a 
    browser. The mobile-column.xsl file was based upon the tab-column.xsl file.
    
    General Changes: The <navigation> and <content> nodes were removed and replaced with a single 
    <mobilenavigation> node. This reorganization allows for all the tabs and their associated 
    channels and portlets to be rendered on a small screen at the same time. From a high level, 
    the new xml output for these changes resemble the following architecture:
    
    <mobilenavigation>
            <group>
                <navblock />
                <channel />
            </group>
            <group>
                <navblock />
                <channel />
                <channel />
                <channel />
            </group>
    </mobilenavigation>
-->
<!--=====END: DOCUMENT DESCRIPTION=====-->


<!--=====START: PARAMETERS & VARIABLES=====-->
    <xsl:param name="activeTab">1</xsl:param>
    <xsl:param name="userLayoutRoot">root</xsl:param>
    <xsl:param name="focusedTabID">none</xsl:param>
    <xsl:param name="useSelectDropDown">true</xsl:param>
    
    <xsl:variable name="activeTabIdx">
        <xsl:choose>
            <xsl:when test="$focusedTabID='none'">
                <xsl:choose>
                    <xsl:when test="string( number( $activeTab ) )='NaN'">
                        <xsl:choose>
                            <xsl:when test="/layout/folder/folder[@ID=$activeTab and @type='regular' and @hidden='false']">
                            	<xsl:value-of select="count(/layout/folder/folder[@ID=$activeTab]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
                            </xsl:when>
                            <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
                        </xsl:choose>
                    </xsl:when>
                    
                    <!-- if the tab index number is greater than the number of tabs, use the first tab -->
                    <xsl:when test="$activeTab &gt; count(/layout/folder/folder[@type='regular' and @hidden='false'])">1</xsl:when>
                    
                    <xsl:otherwise>
                    	<xsl:value-of select="$activeTab"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="/layout/folder/folder[@ID=$focusedTabID and @type='regular' and @hidden='false']">
                    	<xsl:value-of select="count(/layout/folder/folder[@ID=$focusedTabID]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
                    </xsl:when>
                    <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="activeTabID" select="/layout/folder/folder[@type='regular'and @hidden='false'][position() = $activeTabIdx]/@ID"/>
<!--=====END: PARAMETERS & VARIABLES=====-->


<!--=====START: LAYOUT TEMPLATE RULE=====-->
    <xsl:template match="layout">
        <xsl:for-each select="folder[@type='root']">
            <layout>
                <xsl:if test="/layout/@dlm:fragmentName">
                	<xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
                </xsl:if>
                
                <!--header-->
                <header>
                    <xsl:for-each select="child::folder[@type='header']">
                    	<xsl:copy-of select=".//channel"/>
                    </xsl:for-each>
                </header>
                
                <!--mobile navigation-->
                <mobilenavigation>
                	<xsl:attribute name="activeTabID">
                        <xsl:value-of select="$activeTabID" />
                    </xsl:attribute>
                    
                    <xsl:choose>
                    	
                        <!--not focused-->
                        <xsl:when test="$userLayoutRoot = 'root'">
                            <xsl:apply-templates select="folder" />
                        </xsl:when>
                        
                        <!--focused-->
                        <xsl:otherwise>
                            <focused>
                                <xsl:attribute name="in-user-layout">
                                    <xsl:choose>
                                        <xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
                                        <xsl:otherwise>no</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                
                            	<xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
                                
                                <!--select dropdown menu-->
                                <xsl:if test="$useSelectDropDown='true'">
                                    <selection>
                                        <xsl:apply-templates select="folder" mode="select" />
                                    </selection>
                                </xsl:if>
                                
                            </focused>
                        </xsl:otherwise>
                    </xsl:choose>
                </mobilenavigation>
                
                <!--footer-->
                <footer>
                    <xsl:for-each select="child::folder[attribute::type='footer']">
                    	<xsl:copy-of select=".//channel"/>
                    </xsl:for-each>
                </footer>
                
            </layout>    
        </xsl:for-each>
    </xsl:template>
<!--=====END: LAYOUT TEMPLATE RULE=====-->


<!--=====START: FOLDER TEMPLATE RULE (MOBILE)=====-->
<!--
    Code Description:
    1] Select all folders under the 'root' folder that are of 'regular' type and are not 'hidden'.
    2] Create a <group> node.
    3] Create variable $tab-folder. $tab-folder holds a reference to each folder under the 'root' folder that are of 'regular' type and are not 'hidden'.
    4] For each folder referenced in $tab-folder create a <navblock> node, add attributes and close the </navblock> node.
    5] If a folder referenced in $tab-folder contains child nodes, create variable $column-folder. $column-folder holds a reference to $tab-folder child nodes, or column folders. 
    6] For each child node referenced in $column-folder, check if the child node contains channels. If channels are found apply the appropriate templates.
    7] Close the </group> node.
-->
    <xsl:template match="folder">
        <!--1]-->
        <xsl:if test="./@type='regular' and @hidden='false'">
            <!--2]-->
            <group>
                <xsl:attribute name="groupName">
                    <xsl:value-of select="@name"/>
                </xsl:attribute>
                <!--3]-->
                <xsl:variable name="tab-folder" select="." />
                <!--4]-->
                <xsl:for-each select="$tab-folder">
                        <navblock>
                            <xsl:attribute name="tabName">
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                            
                            <xsl:attribute name="ID">
                                <xsl:value-of select="@ID"/>
                            </xsl:attribute>
                            
                            <xsl:attribute name="immutable">
                                <xsl:value-of select="@immutable"/>
                            </xsl:attribute>
                            
                            <xsl:attribute name="unremovable">
                                <xsl:value-of select="@unremovable"/>
                            </xsl:attribute>
                            
                            <xsl:if test="@dlm:moveAllowed = 'false'">
                                <xsl:attribute name="dlm:moveAllowed">false</xsl:attribute>
                            </xsl:if>
                            
                            <xsl:if test="@dlm:deleteAllowed = 'false'">
                                <xsl:attribute name="dlm:deleteAllowed">false</xsl:attribute>
                            </xsl:if>
                            
                            <xsl:if test="@dlm:editAllowed = 'false'">
                                <xsl:attribute name="dlm:editAllowed">false</xsl:attribute>
                            </xsl:if>
                            
                            <xsl:if test="@dlm:addChildAllowed = 'false'">
                                <xsl:attribute name="dlm:addChildAllowed">false</xsl:attribute>
                            </xsl:if>
                            
                            <xsl:if test="@dlm:precedence > 0">
                                <xsl:attribute name="dlm:precedence"><xsl:value-of select="@dlm:precedence"/></xsl:attribute>
                            </xsl:if>
                            
                            <xsl:choose>
                                <xsl:when test="$activeTabID = @ID">
                                    <xsl:attribute name="activeTab">true</xsl:attribute>
                                    <xsl:attribute name="activeTabPosition"><xsl:value-of select="$activeTabID"/></xsl:attribute>
                                </xsl:when>
                                
                                <xsl:otherwise>
                                    <xsl:attribute name="activeTab">false</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            
                            <xsl:attribute name="priority">
                                <xsl:value-of select="@priority"/>
                            </xsl:attribute>
                            
                            <xsl:attribute name="name">
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                        </navblock>
                        <!--5]-->
                        <xsl:if test="child::folder">
                            <xsl:variable name="column-folder" select="folder[@name]" />
                            <!--6]-->
                            <xsl:for-each select="$column-folder">
                                <xsl:if test="child::channel">
                                    <xsl:apply-templates />
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:if>
                </xsl:for-each>
            <!--7]-->
            </group>
        </xsl:if>
    </xsl:template>
<!--=====END: FOLDER TEMPLATE RULE (MOBILE)=====-->


<!--=====START: SELECT DROPDOWN MENU (MOBILE)=====-->
<!--
    Code Description:
    This template appears within the focused view and only appears when $useSelectDropDown is set to true.
    This template selects all the channels and portlets of regular type that are not hidden. When handled by 
    muniversality.xsl these channels and porlets are rendered into a <select> dropdown menu.
-->
    <xsl:template match="folder" mode="select">
        <xsl:if test="./@type='regular' and @hidden='false'">
            <xsl:variable name="tab-folder" select="." />
            <xsl:for-each select="$tab-folder">
                <xsl:if test="child::folder">
                    <xsl:variable name="column-folder" select="folder[@name]" />
                    <xsl:for-each select="$column-folder">
                        <xsl:if test="child::channel">
                            <xsl:apply-templates />
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
<!--=====END: SELECT DROPDOWN MENU (MOBILE)=====-->


<!--=====START: CHANNEL TEMPLATE RULE=====-->
    <xsl:template match="channel">
        <xsl:copy-of select="."/>
    </xsl:template>
<!--=====END: CHANNEL TEMPLATE RULE=====-->


<!--=====START: PARAMETER TEMPLATE RULE=====-->
    <xsl:template match="parameter">
      <xsl:copy-of select="."/>
    </xsl:template>
<!--=====END: PARAMETER TEMPLATE RULE=====-->

</xsl:stylesheet>