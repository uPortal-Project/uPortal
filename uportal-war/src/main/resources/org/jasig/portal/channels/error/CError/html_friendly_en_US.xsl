<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <!--<xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/error/CError/</xsl:variable>-->
  <xsl:variable name="baseMediaURL">media/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
  
  	<xsl:variable name="MESSAGE">
    	<xsl:choose>
        <xsl:when test="@code='4'">The Channel timed out</xsl:when>
        <xsl:when test="@code='1'">This channel failed to render</xsl:when>
        <xsl:when test="@code='2'">This channel failed to initialize</xsl:when>
        <xsl:when test="@code='3'">This channel failed to accept needed data</xsl:when>
        <xsl:when test="@code='0'">This channel experienced a general error</xsl:when>
        <xsl:when test="@code='5'">This channel failed to accept PCS</xsl:when>
        <xsl:when test="@code='6'">You are not authorized to view this channel</xsl:when>
        <xsl:when test="@code='7'">This channel is not available</xsl:when>
        <xsl:when test="@code='-1'">This channel experienced a general uPortal error</xsl:when>
        <xsl:otherwise>This channel experienced a general uPortal error</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
  	<div class="portlet-msg-error">
			<h2>Error</h2>
      <p><xsl:value-of select="$MESSAGE" /></p>

      <xsl:if test="$allowRefresh='true'">
        <a href="{$baseActionURL}?action=retry">
          <!--<img src="{$baseMediaURL}skins/icons/arrow_refresh.png" border="0" alt="Refresh the channel"/>-->
          <span class="uportal-label">Refresh the Channel</span>
        </a><br />
      </xsl:if>

      <xsl:if test="$allowReinstantiation='true'">
        <a href="{$baseActionURL}?action=restart">
          <!--<img src="{$baseMediaURL}skins/icons/arrow_redo.png" border="0" alt="Reboot the channel"/>-->
          <span class="uportal-label">Reboot the Channel</span>
        </a>
      </xsl:if>
      
  	</div> 
           
  </xsl:template>
</xsl:stylesheet>
