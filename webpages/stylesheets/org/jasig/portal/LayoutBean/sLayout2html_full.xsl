<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="no"/>
  
<xsl:param name="baseActionURL">render.uP</xsl:param>
<xsl:param name="theme" select="'imm'"/>
<xsl:variable name="mediaPath">media/org/jasig/portal/layout/tabColumn</xsl:variable>

<xsl:template match="layout"><html>
<head>
<title>uPortal 2.0</title>
<link type="text/css" rel="stylesheet" href="{$mediaPath}/theme_{$theme}/{$theme}.css"/>
<script language="JavaScript">
function openBrWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}
</script>
</head>
<body>
<table summary="add summary" width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td>
<table summary="add summary" width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td height="100" valign="top">
<img alt="interface image" src="{$mediaPath}/theme_{$theme}/uportal_logo.gif" border="0" width="133" height="75" />
</td>
<td colspan="2" align="center" width="100%">Header Channel Here -- Theme is <xsl:value-of select="$theme"/></td>
</tr>
</table>
</td>
</tr>
<xsl:apply-templates select="navigation"/>
<xsl:apply-templates select="content"/>
</table>
</body>
</html></xsl:template>

<xsl:template match="navigation"><tr><td>
<table summary="add summary" border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
<xsl:for-each select="tab">
<xsl:choose>

<xsl:when test="following-sibling::tab[1]/@activeTab = 'true'">
<td nowrap="nowrap" class="uportal-background-light"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10"/><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={@ID}" class="uportal-navigation-category"><xsl:value-of select="@name"/></a></td>
<td class="uportal-background-content"><img alt="interface image" src="{$mediaPath}/theme_{$theme}/before_active_tab.GIF" width="11" height="28" /></td>
</xsl:when>

<xsl:when test="position()=last() and @activeTab='false'">
<td nowrap="nowrap" class="uportal-background-light"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" /><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={@ID}" class="uportal-navigation-category"><xsl:value-of select="@name"/></a></td>
<td width="100%"><img alt="interface image" src="{$mediaPath}/theme_{$theme}/after_last_tab_inactive.GIF" width="11" height="28" /></td>
</xsl:when>

<xsl:when test="position()=last() and @activeTab='true'">
<td nowrap="nowrap" class="uportal-background-content"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" /><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={@ID}" class="uportal-navigation-category"><xsl:value-of select="@name"/></a></td>
<td width="100%"><img alt="interface image" src="{$mediaPath}/theme_{$theme}/after_last_tab_active.GIF" width="11" height="28" /></td>
</xsl:when>

<xsl:when test="@activeTab='false'">
<td nowrap="nowrap" class="uportal-background-light"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" /><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={@ID}" class="uportal-navigation-category"><xsl:value-of select="@name"/></a></td>
<td class="uportal-background-med"><img alt="interface image" src="{$mediaPath}/theme_{$theme}/after_inactive_tab.GIF" width="11" height="28" /></td>
</xsl:when>

<xsl:when test="@activeTab='true'">
<td nowrap="nowrap" class="uportal-background-content"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" /><a href="{$baseActionURL}?uP_sparam=activeTab&amp;activeTab={@ID}" class="uportal-navigation-category"><xsl:value-of select="@name"/></a></td>
<td class="uportal-background-med"><img alt="interface image" src="{$mediaPath}/theme_{$theme}/after_active_tab.GIF" width="11" height="28" /></td>
</xsl:when>

</xsl:choose>
</xsl:for-each>
</tr>
</table>
</td>
</tr></xsl:template>

<xsl:template match="content"><tr>
<td>
<table border="0" cellspacing="0" cellpadding="0" class="uportal-background-content" width="100%">
<tr>
<xsl:call-template name="controlRow"/>
</tr>
<tr>
<xsl:call-template name="contentRow"/>
</tr>
<tr>
<xsl:call-template name="controlRow"/>
</tr>
</table>
</td>
</tr>

</xsl:template>





<xsl:template name="controlRow"><xsl:for-each select="column">
<xsl:choose>

<xsl:when test="position()=1 and position()=last()">
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td width="100%"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()=1">
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td width="{@width}"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()=last()">
<td width="{@width}"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()!=1 and position()!=last()">
<td width="{@width}"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

</xsl:choose>
</xsl:for-each></xsl:template>

<xsl:template name="contentRow"><xsl:for-each select="column">
<xsl:choose>

<xsl:when test="position()=1 and position()=last()">
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td align="left" valign="top" width="100%"><xsl:apply-templates select="channel"/></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()=1">
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td align="left" valign="top" width="{@width}"><xsl:apply-templates select="channel"/></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td class="uportal-background-med"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()=last()">
<td align="left" valign="top" width="{@width}"><xsl:apply-templates select="channel"/></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

<xsl:when test="position()!=1 and position()!=last()">
<td align="left" valign="top" width="{@width}"><xsl:apply-templates select="channel"/></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
<td class="uportal-background-med"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>
<td><img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="20" /></td>
</xsl:when>

</xsl:choose>
</xsl:for-each></xsl:template>

<xsl:template match="channel"><table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr align="left" valign="bottom">
<td class="uportal-channel-title"><xsl:value-of select="@name"/></td>
<td align="right" nowrap="nowrap">

<xsl:if test="@hasHelp='true'">
<a href="{$baseActionURL}?uP_help_target={@ID}"><img alt="help" src="{$mediaPath}/help.gif" width="16" height="16" border="0"/></a>
</xsl:if>

<xsl:if test="@hasAbout='true'">
<a href="{$baseActionURL}?uP_about_target={@ID}"><img alt="about" src="{$mediaPath}/about.gif" width="16" height="16" border="0"/></a>
</xsl:if>

<xsl:if test="@editable='true'">
<a href="{$baseActionURL}?uP_edit_target={@ID}"><img alt="edit" src="{$mediaPath}/edit.gif" width="16" height="16" border="0"/></a>
</xsl:if>

<xsl:if test="@printable='true'">
<a href="{$baseActionURL}?uP_print_target={@ID}"><img alt="print" src="{$mediaPath}/print.gif" width="16" height="16" border="0"/></a>
</xsl:if>

<xsl:if test="@minimizable='true'">
<xsl:choose>
<xsl:when test="@minimized='true'">
<a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false"><img alt="maximize" src="{$mediaPath}/max.gif" width="16" height="16" border="0"/></a>
</xsl:when>
<xsl:otherwise>
<a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true"><img alt="minimize" src="{$mediaPath}/min.gif" width="16" height="16" border="0"/></a>
</xsl:otherwise>
</xsl:choose>
</xsl:if>

<xsl:if test="@detachable='true'">
<a href="#" onClick="openBrWindow('{$baseActionURL}?uP_detach_target={@ID}','detachedChannel','toolbar=yes,location=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width=640,height=480')"><img alt="detach" src="{$mediaPath}/detach.gif" width="16" height="16" border="0"/></a>
</xsl:if>

<xsl:if test="@removable='true'">
<a href="{$baseActionURL}?uP_remove_target={@ID}"><img alt="remove" src="{$mediaPath}/remove.gif" width="16" height="16" border="0"/></a>
</xsl:if>

</td>
</tr>

<tr class="uportal-background-med">
<td height="1" colspan="2"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" /></td>
</tr>

<tr>
<td colspan="2"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="5" /></td>
</tr>

<tr>
<td class="uportal-channel-text" colspan="2"><xsl:if test="@minimized != 'true'"><xsl:copy-of select="."/></xsl:if></td>
</tr>

<tr>
<td colspan="2"><img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="20" /></td>
</tr>
</table></xsl:template></xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios ><scenario name="theme" url="file://c:\ProudTab\theme.xml" htmlbaseurl=""/></scenarios>
</metaInformation>
-->
