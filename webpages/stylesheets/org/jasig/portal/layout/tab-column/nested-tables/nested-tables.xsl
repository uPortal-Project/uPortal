<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="baseIdempotentActionURL">render.userLayoutRootNode.uP</xsl:param>

  <xsl:param name="skin" select="'imm'"/>
  <xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/nested-tables</xsl:variable>
  <!-- This template is supposed to render a fragment of the layout. 
       For example, during a detach mode, only the <channel> element
       that's detached is passed along to the structure transformation.
       In general, it should render a fragment that contains not just
       a single channel, but an entire column or a tab, perhaps :) But
       I am lazy, so I'll just flatten out all of the channels into 
       one big column.   -peter.-->
  <xsl:template match="layout_fragment">
    <html>
      <head>
        <title>
          <xsl:value-of select="content/channel/@name"/>
        </title>
        <META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT"></META>
        <META HTTP-EQUIV="pragma" CONTENT="no-cache"></META>
        <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
        <script language="JavaScript">function openBrWindow(theURL,winName,features) {window.open(theURL,winName,features);}</script>
      </head>
      <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" class="uportal-background-content">
        <table width="100%" border="0" cellspacing="0" cellpadding="10">
          <tr class="uportal-background-content">
            <td class="uportal-background-content">
              <xsl:for-each select="content//channel">
                <xsl:apply-templates select=".">
                  <xsl:with-param name="detachedContent" select="'true'"/>
                </xsl:apply-templates>
              </xsl:for-each>
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="layout">
    <html>
      <head>
        <title>uPortal 2.3.5+</title>
        <link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
        <script language="JavaScript">function openBrWindow(theURL,winName,features) {window.open(theURL,winName,features);}</script>
      </head>
      <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
        <xsl:apply-templates select="header"/>
        <xsl:if test="not(//focused)">
          <xsl:apply-templates select="navigation"/>
        </xsl:if>
        <xsl:apply-templates select="content"/>
        <table width="100%" border="0" cellpadding="1" cellspacing="0">
          <tr><td>
            <xsl:apply-templates select="footer"/>
          </td></tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="header">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr class="uportal-background-med">
        <td colspan="2" rowspan="1">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="1"/>
        </td>
      </tr>
      <tr class="uportal-background-med">
        <td nowrap="nowrap">
          <a href="http://www.ja-sig.org">
            <img alt="uPortal by JA-SIG small Logo" title="uPortal by JA-SIG small Logo" src="{$mediaPath}/{$skin}/institutional/uportal_logo_small.gif" border="0"/>
          </a>
        </td>
        <td nowrap="nowrap" align="right" width="100%" class="uportal-channel-text">
          <xsl:copy-of select="channel[@name='Header']"/>
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="1"/>
        </td>
      </tr>
      <tr>
        <xsl:copy-of select="channel[@fname='session-locales-selector']"/>
      </tr>
    </table>
    <xsl:copy-of select="channel[@name='Login']"/>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td colspan="1" rowspan="1">
          <img alt="uPortal by JA-SIG large logo" title="uPortal by JA-SIG large Logo" src="{$mediaPath}/{$skin}/institutional/uportal_logo_grid.gif" width="600" height="50"/>
        </td>
      </tr>
      <tr>
        <td colspan="1" rowspan="1">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="20" height="20"/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="navigation">
    <table summary="add summary" border="0" cellspacing="0" cellpadding="0" width="100%">
      <tr>
        <xsl:for-each select="tab">
          <xsl:choose>
            <xsl:when test="following-sibling::tab[1]/@activeTab = 'true'">
              <td nowrap="nowrap" class="uportal-background-light">
                <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="10"/>
                <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                  <xsl:value-of select="@name"/>
                </a>
              </td>
              <td width="11" class="uportal-background-content">
                <img alt="" src="{$mediaPath}/{$skin}/navigation/before_active_tab.gif" width="11" height="28"/>
              </td>
            </xsl:when>
            <xsl:when test="position()=last() and @activeTab='false'">
              <td nowrap="nowrap" class="uportal-background-light">
                <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="10"/>
                <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                  <xsl:value-of select="@name"/>
                </a>
              </td>
              <td width="100%">
                <img alt="" src="{$mediaPath}/{$skin}/navigation/after_last_tab_inactive.gif" width="11" height="28"/>
              </td>
            </xsl:when>
            <xsl:when test="position()=last() and @activeTab='true'">
              <td nowrap="nowrap" class="uportal-background-content">
                <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="10"/>
                <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                  <xsl:value-of select="@name"/>
                </a>
              </td>
              <td width="100%">
                <img alt="" src="{$mediaPath}/{$skin}/navigation/after_last_tab_active.gif" width="11" height="28"/>
              </td>
            </xsl:when>
            <xsl:when test="@activeTab='false'">
              <td nowrap="nowrap" class="uportal-background-light">
                <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="10"/>
                <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                  <xsl:value-of select="@name"/>
                </a>
              </td>
              <td width="11" class="uportal-background-light">
                <img alt="" src="{$mediaPath}/{$skin}/navigation/after_inactive_tab.gif" width="11" height="28"/>
              </td>
            </xsl:when>
            <xsl:when test="@activeTab='true'">
              <td nowrap="nowrap" class="uportal-background-content">
                <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="10"/>
                <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}" class="uportal-navigation-category">
                  <xsl:value-of select="@name"/>
                </a>
              </td>
              <td width="11" class="uportal-background-light">
                <img alt="" src="{$mediaPath}/{$skin}/navigation/after_active_tab.gif" width="11" height="28"/>
              </td>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </tr>
    </table>
  </xsl:template>
  <xsl:template match="content">
    <xsl:choose>
      <xsl:when test="not(//focused)">
        <table border="0" cellspacing="0" cellpadding="0" class="uportal-background-content" width="100%">
          <tr>
            <xsl:call-template name="controlRow"/>
          </tr>
          <tr class="uportal-background-content">
            <xsl:call-template name="contentRow"/>
          </tr>
          <tr>
            <xsl:call-template name="controlRow"/>
          </tr>
        </table>
      </xsl:when>
      <xsl:otherwise>

        <xsl:apply-templates select="focused"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="controlRow">
    <xsl:for-each select="column">
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
          <td width="100%">
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()=1">
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
          <td width="{@width}">
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="21" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()=last()">
          <td width="{@width}">

            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()!=1 and position()!=last()">
          <td width="{@width}">
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="21" height="20"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="contentRow">
    <xsl:for-each select="column">
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
          <td align="left" valign="top" width="100%">
            <xsl:apply-templates select="channel"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()=1">
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td style="background-image:url({$mediaPath}/{$skin}/skin/v_rule.gif); background-repeat:repeat-y;">
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="21" height="1"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()=last()">
          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="10" height="20"/>
          </td>
        </xsl:when>
        <xsl:when test="position()!=1 and position()!=last()">
          <td align="left" valign="top" width="{@width}">
            <xsl:apply-templates select="channel"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
          <td style="background-image:url({$mediaPath}/{$skin}/skin/v_rule.gif); background-repeat:repeat-y;">
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="21" height="1"/>
          </td>
          <td>
            <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
          </td>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="channel">
    <xsl:param name="detachedContent"/>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr align="left" valign="bottom" class="uportal-background-content">
        <td class="uportal-channel-title">
          <xsl:value-of select="@title"/>
        </td>
        <td align="right" nowrap="nowrap" class="uportal-background-content">
          <xsl:choose>
            <xsl:when test="$detachedContent='true'">
              <xsl:call-template name="detachedChannelControls"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="controls"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <tr class="uportal-background-dark">
        <td height="1" colspan="2">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="1"/>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="uportal-background-content">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="5"/>
        </td>
      </tr>
      <tr class="uportal-background-content">
        <td class="uportal-channel-text" colspan="2">
          <xsl:if test="@minimized != 'true'">
            <xsl:copy-of select="."/>
          </xsl:if>
        </td>
      </tr>
      <tr class="uportal-background-content">
        <td colspan="2" class="uportal-background-content">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="20"/>
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template match="footer">
    <xsl:for-each select="channel">
      <xsl:copy-of select="."/>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="controls">
    <xsl:if test="not(@hasHelp='false')">
      <a href="{$baseActionURL}?uP_help_target={@ID}">
        <img alt="help" title="help" src="{$mediaPath}/{$skin}/controls/help.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(@hasAbout='false')">
      <a href="{$baseActionURL}?uP_about_target={@ID}">
        <img alt="about" title="about" src="{$mediaPath}/{$skin}/controls/about.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(@editable='false')">
      <a href="{$baseActionURL}?uP_edit_target={@ID}">
        <img alt="edit" title="edit" src="{$mediaPath}/{$skin}/controls/edit.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="@printable='true'">
      <a href="{$baseActionURL}?uP_print_target={@ID}">
        <img alt="print" title="print" src="{$mediaPath}/{$skin}/controls/print.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(//focused)">
      <a href="{$baseActionURL}?uP_root={@ID}">
        <img alt="focus" title="focus" src="{$mediaPath}/{$skin}/controls/focus.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(//focused)">
      <xsl:choose>
        <xsl:when test="@minimized='true'">
          <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false">
            <img alt="maximize" title="maximize" src="{$mediaPath}/{$skin}/controls/max.gif" width="16" height="16" border="0"/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true">
            <img alt="minimize" title="minimize" src="{$mediaPath}/{$skin}/controls/min.gif" width="16" height="16" border="0"/>
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <a href="#" onClick="openBrWindow('{$baseIdempotentActionURL}?uP_detach_target={@ID}','detachedChannel','toolbar=yes,location=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width=640,height=480')">
      <img alt="detach" title="detach" src="{$mediaPath}/{$skin}/controls/detach.gif" width="16" height="16" border="0"/>
    </a>
    <xsl:if test="not(@unremovable='true') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
      <a href="{$baseActionURL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')">
        <img alt="remove" title="remove" src="{$mediaPath}/{$skin}/controls/remove.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
  </xsl:template>
  <xsl:template name="detachedChannelControls">
    <xsl:if test="not(@hasHelp='false')">
      <a href="{$baseActionURL}?uP_help_target={@ID}">
        <img alt="help" title="help" src="{$mediaPath}/{$skin}/controls/help.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(@hasAbout='false')">
      <a href="{$baseActionURL}?uP_about_target={@ID}">
        <img alt="about" title="about" src="{$mediaPath}/{$skin}/controls/about.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="not(@editable='false')">
      <a href="{$baseActionURL}?uP_edit_target={@ID}">
        <img alt="edit" title="edit" src="{$mediaPath}/{$skin}/controls/edit.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
    <xsl:if test="@printable='true'">
      <a href="{$baseActionURL}?uP_print_target={@ID}">
        <img alt="print" title="print" src="{$mediaPath}/{$skin}/controls/print.gif" width="16" height="16" border="0"/>
      </a>
    </xsl:if>
  </xsl:template>
  <xsl:template match="focused">
    <xsl:apply-templates select="channel" mode="focused"/>
  </xsl:template>
  <xsl:template match="channel" mode="focused">
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-background-content">
      <tr>
        <td width="80">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_01.gif" width="80" height="21" border="0" usemap="#focused_01_Map"/>
        </td>
        <td width="100%" height="21" colspan="2" style="background-image:url({$mediaPath}/{$skin}/focused/focused_03.gif); background-repeat:repeat-x;">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="21"/>
        </td>
        <td width="45">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_04.gif" width="45" height="21"/>
        </td>
      </tr>
      <tr>
        <td width="80">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_05.gif" width="80" height="33" border="0" usemap="#focused_05_Map"/>
        </td>
        <td width="100%" align="left" valign="bottom" class="uportal-channel-title" nowrap="nowrap">
          <xsl:value-of select="@title"/>
        </td>
        <td align="right" valign="bottom" nowrap="nowrap">
          <xsl:call-template name="controls"/>
        </td>
        <td width="45">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_08.gif" width="45" height="33"/>
        </td>
      </tr>
      <tr>
        <td width="80">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_09.gif" width="80" height="1"/>
        </td>
        <td colspan="2" class="uportal-background-dark">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="1"/>
        </td>
        <td width="45">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_12.gif" width="45" height="1"/>
        </td>
      </tr>
      <tr>
        <td width="80">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_13.gif" width="80" height="15"/>
        </td>
        <td colspan="2">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="1" height="15"/>
        </td>
        <td width="45">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_16.gif" width="45" height="15"/>
        </td>
      </tr>
      <tr>
        <td width="80" style="background-image:url({$mediaPath}/{$skin}/focused/focused_17.gif); background-repeat:repeat-y;">
          <img alt="" src="{$mediaPath}/{$skin}/skin/transparent.gif" width="80" height="1"/>
        </td>
        <td align="left" valign="top" colspan="2" class="uportal-channel-text">
            <xsl:copy-of select="."/>
        </td>
        <td width="45" style="background-image:url({$mediaPath}/{$skin}/focused/focused_20.gif); background-repeat:repeat-y;">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_20.gif" width="45" height="1"/>
        </td>
      </tr>
      <tr>
        <td width="80">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_24.gif" width="80" height="50"/>
        </td>
        <td height="50" colspan="2" style="background-image:url({$mediaPath}/{$skin}/focused/focused_26.gif); background-repeat:repeat-x;">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_26.gif" width="1" height="50"/>
        </td>
        <td width="45">
          <img alt="" src="{$mediaPath}/{$skin}/focused/focused_27.gif" width="45" height="50"/>
        </td>
      </tr>
    </table>
    <map id="focused_01_Map" name="focused_01_Map">
      <area shape="circle" alt="Return to portal" coords="36,34,19" href="{$baseActionURL}?uP_root=root"/>
    </map>
    <map id="focused_05_Map" name="focused_05_Map">
      <area shape="circle" alt="Return to portal" coords="36,13,19" href="{$baseActionURL}?uP_root=root"/>
    </map>
  </xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->