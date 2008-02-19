<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!--
 | This file determines the presentation of the main navigation systems of the portal.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  
  <!-- ========== TEMPLATE: BODY COLUMNS ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders the columns of the page body.
  -->
  <xsl:template name="content.row">
    <xsl:for-each select="column">
    	<xsl:variable name="POSITION"> <!-- Determine column place in the layout and add appropriate class. -->
        <xsl:choose>
          <xsl:when test="position()=1 and position()=last()">single</xsl:when>
          <xsl:when test="position()=1">left</xsl:when>
          <xsl:when test="position()=last()">right</xsl:when>
          <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <td id="column_{@ID}" class="portal-page-column {$POSITION}"> <!-- Unique ID needed for drag and drop. -->
        <xsl:attribute name="width"> <!-- Determine column width. -->
          <xsl:choose>
            <xsl:when test="position()=1 and position()=last()">100%</xsl:when>
            <xsl:otherwise>
            	<xsl:value-of select="@width" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <div id="inner-column_{@ID}" class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
        	<xsl:apply-templates select="channel"/> <!-- Render the column's portlets.  -->
        </div>
      </td>
    </xsl:for-each>
  </xsl:template>
  <!-- ============================================ -->
  
  
  <!-- ========== TEMPLATE: LEFT COLUMN ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders the left navigation column of the page body.
  -->
  <xsl:template name="left.column">
  	<td rowspan="2" width="{$LEFT_COLUMN_WIDTH}px" id="portalPageLeftColumn">
      <div id="portalPageLeftColumnInner"> <!-- Inner div for additional presentation/formatting options. -->
        
        <!-- ****** CONTENT LEFT BLOCK ****** -->
        <xsl:call-template name="content.left.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
        <!-- ****** CONTENT LEFT BLOCK ****** -->
      
      </div>
    </td>
  </xsl:template>
  <!-- ============================================ -->
  
		
</xsl:stylesheet>
