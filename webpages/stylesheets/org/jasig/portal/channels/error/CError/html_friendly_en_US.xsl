<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <img src="{$baseMediaURL}wrenchworks.gif" width="112" height="119"/>
        </td>
        <td>
          <img src="{$baseMediaURL}transparent.gif" width="16" height="16"/>
        </td>
        <td class="uportal-channel-subtitle" width="100%">Error:<br/><span class="uportal-channel-error">
              <xsl:choose>
                <xsl:when test="@code='4'">The Channel timed out</xsl:when>
              <xsl:when test="@code='1'">This channel failed to render</xsl:when>
              <xsl:when test="@code='2'">This channel failed to initialize</xsl:when>
              <xsl:when test="@code='3'">This channel failed to accept needed data</xsl:when>
              <xsl:when test="@code='0'">This channel experienced a general error</xsl:when>
              <xsl:when test="@code='5'">This channel failed to accept PCS</xsl:when>
              <xsl:when test="@code='6'">You are not authorized to view this channel</xsl:when>
              <xsl:when test="@code='7'">This channel is not available</xsl:when>
              <xsl:when test="@code='-1'">This channel experienced a general uPortal error</xsl:when></xsl:choose></span>
          <br/>
          <br/>

          <xsl:if test="$allowRefresh='true'">
            <a href="{$baseActionURL}?action=retry">
              <img src="{$baseMediaURL}error_refresh.gif" border="0" width="16" height="16" alt="Refresh the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">Refresh the Channel</span>
            </a>

            <br/>
          </xsl:if>

          <xsl:if test="$allowReinstantiation='true'">
            <a href="{$baseActionURL}?action=restart">
              <img src="{$baseMediaURL}error_reboot.gif" border="0" width="16" height="16" alt="Reboot the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">Reboot the Channel</span>
            </a>
          </xsl:if>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
