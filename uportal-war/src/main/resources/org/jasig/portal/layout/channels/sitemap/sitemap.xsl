<?xml version="1.0"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no"/>

  <xsl:param name="baseActionURL">baseActionURL not set</xsl:param>
    
  <xsl:template match="/">
      <xsl:apply-templates select="layout"/>
  </xsl:template>
  
  <xsl:template match="layout">
      <xsl:apply-templates select="folder" mode="root"/>
  </xsl:template>
  
  
  <xsl:template match="folder" mode="root">
    <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="group"/>
  </xsl:template>
  
  <xsl:template match="folder" mode="group">
    <xsl:variable name="POSITION" select="position()"/>
    <xsl:if test="($POSITION - 1) mod 4 = 0">
        <div class="fl-col-flex4">
          <xsl:apply-templates select="../folder[@type='regular' and @hidden='false' and position()&lt;$POSITION+4 and (position()=$POSITION or position()&gt;$POSITION)]" mode="tab"/>
        </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="folder" mode="tab">
    <div class="fl-col">
        <h2><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}"><xsl:value-of select="@name"/></a></h2>
        <ul>
            <xsl:apply-templates select="folder" mode="column"/>
        </ul>
    </div>
  </xsl:template>
      
  <xsl:template match="folder" mode="column">
		<xsl:apply-templates select="channel"/>
  </xsl:template>  
  
  <xsl:template match="channel">
		<li><a href="{$baseActionURL}?uP_root={@ID}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>
    
</xsl:stylesheet>