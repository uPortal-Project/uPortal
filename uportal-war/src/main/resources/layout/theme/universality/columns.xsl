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
 | This file determines the presentation of the main navigation systems of the portal.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
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
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url ../../../xsd/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg" 
    version="1.0">

  <!-- ========== TEMPLATE: BODY COLUMNS ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders the columns of the page body.
  -->
  <xsl:template name="columns">
    <xsl:param name="COLUMNS" />
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
  	<div id="portalPageBodyColumns">
    	<xsl:attribute name="class"> <!-- Write appropriate FSS class based on number of columns to produce column layout. -->
    		columns-<xsl:value-of select="$COLUMNS" />
      </xsl:attribute>
      <xsl:for-each select="column">
      	<xsl:variable name="NUMBER">
        	<xsl:value-of select="position()" />
        </xsl:variable>
        <xsl:variable name="POSITION"> <!-- Determine column place in the layout and add appropriate class. -->
        	column-<xsl:value-of select="$NUMBER" />
        </xsl:variable>
        <xsl:variable name="COLUMN_WIDTH">
        	<xsl:choose>
            <xsl:when test="@width = '100%'"></xsl:when>
            <xsl:otherwise><xsl:value-of select="floor(substring-before(@width,'%'))" /></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="MOVABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:moveAllowed='false')">movable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="DELETABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:deleteAllowed='false')">deletable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="EDITABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:editAllowed='false')">editable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="CAN_ADD_CHILDREN">
          <xsl:choose>
            <xsl:when test="not(@dlm:addChildAllowed='false')">canAddChildren</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        
        <div id="column_{@ID}" class="portal-page-column {$POSITION} fl-container-flex{$COLUMN_WIDTH} {$MOVABLE} {$DELETABLE} {$EDITABLE} {$CAN_ADD_CHILDREN}"> <!-- Unique column_ID needed for drag and drop. -->
          <div id="inner-column_{@ID}" class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
            <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
            	<div class="column-permissions"><a class="button portal-column-permissions-link" href="javascript:;"><span class="icon permissions"></span><xsl:value-of select="upMsg:getMessage('edit.column.x.permissions', $USER_LANG, $NUMBER)"/></a></div>
            </xsl:if>
            <xsl:apply-templates select="channel|blocked-channel"/> <!-- Render the column's portlets.  -->
          </div>
        </div>
      </xsl:for-each>
    </div>
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
  </xsl:template>
  <!-- ============================================ -->
  
  
  <!-- ========== TEMPLATE: SIDEBAR ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders the left navigation column of the page body.
  -->
  <xsl:template name="sidebar">
  	<div id="portalSidebar">
    	<xsl:variable name="FSS_SIDEBAR_LOCATION_CLASS">
      	<xsl:call-template name="sidebar.location" /> <!-- Template located in page.xsl. -->
      </xsl:variable>
    	<xsl:attribute name="class">
      	<xsl:choose>
          <xsl:when test="$AUTHENTICATED='true'">
            <xsl:choose>
              <!-- when focused -->
              <xsl:when test="$PORTAL_VIEW='focused'">fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/></xsl:when>
              <!-- when dashboard -->
              <xsl:otherwise>fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- when logged out -->
          <xsl:otherwise>fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/></xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <div id="portalSidebarInner"> <!-- Inner div for additional presentation/formatting options. -->
      	
        <xsl:choose>
          <xsl:when test="$AUTHENTICATED='true'">
          
            <xsl:choose>
              <xsl:when test="$PORTAL_VIEW='focused'">
              
                <!-- Sidebar when a portlet is focused. -->
                <xsl:if test="$USE_SIDEBAR_FOCUSED='true'">
                  <!-- ****** CONTENT SIDEBAR BLOCK ****** -->
                  <xsl:call-template name="content.sidebar.focused.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
                  <!-- ****** CONTENT SIDEBAR FOCUSED BLOCK ****** -->
                </xsl:if>
                
              </xsl:when>
              <xsl:otherwise>
                
                <!-- Sidebar when in dashboard. -->
                <xsl:if test="$USE_SIDEBAR='true'">
                  <!-- ****** SIDEBAR BLOCK ****** -->
                  <xsl:call-template name="content.sidebar.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
                  <!-- ****** SIDEBAR BLOCK ****** -->
                </xsl:if>
                
              </xsl:otherwise>
            </xsl:choose>
            
          </xsl:when>
          <xsl:otherwise>
            
            <!-- Sidebar when logged out. -->
            <xsl:if test="$USE_SIDEBAR_GUEST='true'">
              <!-- ****** CONTENT SIDEBAR GUEST BLOCK ****** -->
              <xsl:call-template name="content.sidebar.guest.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
              <!-- ****** CONTENT SIDEBAR GUEST BLOCK ****** -->
            </xsl:if>
            
          </xsl:otherwise>
        </xsl:choose>
      
      </div>
    </div>
  </xsl:template>
  <!-- ============================================ -->
  
		
</xsl:stylesheet>
