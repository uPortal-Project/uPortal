<?xml version="1.0" encoding="utf-8"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
	<xsl:param name="skin" select="'spring'"/>
	<xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/xhtml-theme</xsl:variable>

	<xsl:template match="layout_fragment">
		<html xml:lang="en" lang="en">
			<head>
				<title><xsl:value-of select="content/channel/@name"/></title>
				<meta http-equiv="content-type" content="text/html; charset=utf-8" />
				<meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT" />
				<meta http-equiv="pragma" content="no-cache" />
				<link rel="Shortcut Icon" href="favicon.ico" type="image/x-icon" />
				<link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css" />
			</head>
			<body>
				<table width="100%" border="0" cellspacing="0" cellpadding="10">
					<tr>
						<td>
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
		<html xml:lang="en" lang="en">
			<head>
				<title>uPortal</title>
				<meta http-equiv="content-type" content="text/html; charset=utf-8" />
				<link rel="Shortcut Icon" href="favicon.ico" type="image/x-icon" />
				<link rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css" type="text/css" />
			</head>
			<body>
				<xsl:choose>
					<xsl:when test="not(//focused)">
						<xsl:apply-templates select="header"/>
					</xsl:when>
					<xsl:when test="//focused">
						<xsl:apply-templates select="header" mode="focused"/>
					</xsl:when>
				</xsl:choose>

				<xsl:apply-templates select="content"/>
	
				<xsl:apply-templates select="footer"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="header">
		<!-- PORTAL HEADER -->
		<div id="portal-page-header">
			<div id="logo"><img width="104" height="60" src="{$mediaPath}/{$skin}/institutional/uportal_logo_grid.gif" alt="Logo" /></div>
				
			<div id="welcome">
				<xsl:copy-of select="channel[@name='Login']"/>
				<xsl:copy-of select="channel[@name='Header']"/>
			</div>
			
			<div style="height:55px;"></div>
			
			<xsl:apply-templates select="//navigation"/>
		</div>
	</xsl:template>
	
	<xsl:template match="header" mode="focused">
		<!-- PORTAL HEADER -->
		<div id="portal-page-header" class="focused">
			<div id="logo" style="height:auto;width:auto;font-family:Georgia, 'Times New Roman';">
				<span style="color:yellow;">uPortal</span><br/>
			</div>
			
			<div id="welcome">
				<xsl:copy-of select="channel[@name='Login']"/>
				<xsl:copy-of select="channel[@name='Header']"/>
			</div>
			
			<div id="focused-channel-title" style="float:left;">
				<span class="title"><xsl:value-of select="//focused/channel/@title"/></span><br/>
			</div>
		
			<div class="portlet-toolbar" style="clear:left;">
				<a href="{$baseActionURL}?uP_root=root">Return to Portal</a>&#160;
				
				<xsl:if test="../content/focused/channel/@editable='true'">
					<a href="{$baseActionURL}?uP_edit_target={../content/focused/channel/@ID}#{../content/focused/channel/@ID}">Edit Preferences</a>&#160;
				</xsl:if>
				
				<xsl:if test="../content/focused/channel/@hasHelp='true'">
					<a href="{$baseActionURL}?uP_help_target={../content/focused/channel/@ID}#{../content/focused/channel/@ID}">Help</a>
				</xsl:if>
				
				<xsl:if test="../content/focused/channel/@hasAbout='true'">
					<a href="{$baseActionURL}?uP_about_target={../content/focused/channel/@ID}#{../content/focused/channel/@ID}">About</a>
				</xsl:if>
				
				<xsl:if test="@printable='true'">
					<a href="{$baseActionURL}?uP_print_target={../content/focused/channel/@ID}#{../content/focused/channel/@ID}">Printable View</a>
				</xsl:if>
				
				<xsl:if test="//focused[@in-user-layout='no']">
					<a href="{$baseActionURL}">Add to my Layout</a>
				</xsl:if>
			</div>		
		</div>
	</xsl:template>

	<xsl:template match="navigation">
		<!-- TABS -->
		<div id="tabs">
			<ul>
				<xsl:for-each select="tab">
					<xsl:choose>
						<xsl:when test="@activeTab='false'">
							<!-- INACTIVE TAB -->
							<li><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}"><span><xsl:value-of select="@name"/></span></a></li>
						</xsl:when>
						<xsl:otherwise>
							<!-- ACTIVE TAB -->
							<li id="active-tab"><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}"><span><xsl:value-of select="@name"/></span></a></li>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="content">
		<xsl:choose>
			<xsl:when test="not(//focused)">
				<!-- PORTAL BODY -->
				<div id="portal-page-body">
					<table cellpadding="0" cellspacing="15" width="100%">
						<tr>
							 <xsl:call-template name="contentRow"/>
						</tr>
					</table>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div id="portal-page-body">
					<table cellpadding="0" cellspacing="15" width="100%">
						<tr>
							<td>
								<xsl:apply-templates select="focused"/>
							</td>
						</tr>
					</table>
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="contentRow">
		<xsl:for-each select="column">
			<xsl:choose>
				<xsl:when test="position()=1 and position()=last()">
					<td width="100%" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=1">
					<td width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=last()">
					<td width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()!=1 and position()!=last()">
					<td width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="channel">
		<xsl:param name="detachedContent"/>
		<!-- PORTLET TOOLBAR -->
		<div class="portlet-toolbar">
			<div>
				<xsl:choose>
					<xsl:when test="$detachedContent='true'">
						<xsl:call-template name="detachedChannelControls"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="controls"/>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		<xsl:if test="not(//focused)">
			<xsl:choose>
				<xsl:when test="@minimized='true'">
					<a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false#{@ID}"><img src="{$mediaPath}/{$skin}/controls/min.gif" width="19" height="19" alt="unshade" title="unshade" style="float:left;padding-top:2px;padding-right:3px;margin-left:-2px;"/></a>
				</xsl:when>
				<xsl:otherwise>
					<a href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true#{@ID}"><img src="{$mediaPath}/{$skin}/controls/max.gif" width="19" height="19" alt="shade" title="shade" style="float:left;padding-top:2px;padding-right:3px;margin-left:-2px;"/></a>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
			<a name="{@ID}" id="{@ID}"></a><h2><xsl:value-of select="@title"/></h2>
		</div>
		<!-- PORTLET CONTENT -->
		<xsl:choose>
			<xsl:when test="@minimized != 'true'">
			  <div class="portlet clearfix {@fname}">
				<xsl:copy-of select="."/>
			  </div>
			</xsl:when>
			<xsl:otherwise>
				<br />
			</xsl:otherwise>
		</xsl:choose>
			
	</xsl:template>

	<xsl:template match="footer">
		<!-- PORTAL FOOTER -->
		<div id="portal-page-footer">
			<xsl:copy-of select="channel[@name='Footer']"/>
		</div>
	</xsl:template>

	<xsl:template name="controls">
		<xsl:if test="not(@hasHelp='false')">
			<a href="{$baseActionURL}?uP_help_target={@ID}#{@ID}"><img src="{$mediaPath}/{$skin}/controls/help.gif" width="19" height="19" alt="help" title="help" /></a>
		</xsl:if>
		<xsl:if test="not(@hasAbout='false')">
			<a href="{$baseActionURL}?uP_about_target={@ID}#{@ID}"><img src="{$mediaPath}/{$skin}/controls/about.gif" width="19" height="19" alt="about" title="about" /></a>
		</xsl:if>
		<xsl:if test="not(@editable='false')">
			<a href="{$baseActionURL}?uP_edit_target={@ID}#{@ID}">edit</a>
		</xsl:if>
		<xsl:if test="@printable='true'">
			<a href="{$baseActionURL}?uP_print_target={@ID}#{@ID}"><img src="{$mediaPath}/{$skin}/controls/print.gif" width="19" height="19" alt="print" title="print" /></a>
		</xsl:if>
		<xsl:if test="not(//focused)">
			<a href="{$baseActionURL}?uP_root={@ID}"><img src="{$mediaPath}/{$skin}/controls/focus.gif" width="19" height="19" alt="maximize" title="maximize" /></a>
		</xsl:if>
		<xsl:if test="not(@unremovable='true') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
			<a href="{$baseActionURL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')"><img src="{$mediaPath}/{$skin}/controls/remove.gif" width="19" height="19" alt="remove" title="remove" /></a>
		</xsl:if>
	</xsl:template>

	<xsl:template name="detachedChannelControls">
<!--  removed per Rutgers request due to a bug where it was keeping user in mode state.
		<xsl:if test="not(@hasHelp='false')">
			<a href="{$baseActionURL}?uP_help_target={@ID}"><img src="{$mediaPath}/{$skin}/controls/help.gif" width="19" height="19" alt="help" title="help" /></a>
		</xsl:if>
		<xsl:if test="not(@hasAbout='false')">
			<a href="{$baseActionURL}?uP_about_target={@ID}"><img src="{$mediaPath}/{$skin}/controls/about.gif" width="19" height="19" alt="about" title="about" /></a>
		</xsl:if>
		<xsl:if test="not(@editable='false')">
			<a href="{$baseActionURL}?uP_edit_target={@ID}"><img src="{$mediaPath}/{$skin}/controls/edit.gif" width="19" height="19" alt="edit" title="edit" /></a>
		</xsl:if>
		<xsl:if test="@printable='true'">
			<a href="{$baseActionURL}?uP_print_target={@ID}"><img src="{$mediaPath}/{$skin}/controls/print.gif" width="19" height="19" alt="print" title="print" /></a>
		</xsl:if>
		-->
	</xsl:template>

	<xsl:template match="focused">
		<xsl:apply-templates select="channel" mode="focused"/>
	</xsl:template>

	<xsl:template match="channel" mode="focused">
		<xsl:copy-of select="."/>
	</xsl:template>

</xsl:stylesheet>
