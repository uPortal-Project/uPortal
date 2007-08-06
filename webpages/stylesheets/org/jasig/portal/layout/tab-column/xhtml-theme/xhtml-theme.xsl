<?xml version="1.0" encoding="utf-8"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
	<xsl:param name="skin" select="'spring'"/>
	<xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/xhtml-theme</xsl:variable>
	<xsl:param name="isAjaxEnabled" select="'false'"/>
	<xsl:param name="isLoggedInUser" select="'false'"/>
	
	<xsl:template match="layout_fragment">
		<html xml:lang="en" lang="en">
			<head>
				<title><xsl:value-of select="content/channel/@name"/></title>
				<meta http-equiv="content-type" content="text/html; charset=utf-8" />
				<meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT" />
				<meta http-equiv="pragma" content="no-cache" />
				<link rel="Shortcut Icon" href="favicon.ico" type="image/x-icon" />
				<link type="text/css" rel="stylesheet" href="{$mediaPath}/{$skin}/skin/{$skin}.css" />
				
				<xsl:if test="$isAjaxEnabled">
					<script src="{$mediaPath}/common/dojo/dojo.js" type="text/javascript"/>
					<script src="{$mediaPath}/common/ajax-preferences.js" type="text/javascript"/>
					<script type="text/javascript">
						djConfig.parseWidgets = false;
						djConfig.searchIds = [];
						dojo.require( "dojo.widget.Dialog" );
						dojo.hostenv.writeIncludes(); 
					</script>
				</xsl:if>
				
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

				<xsl:if test="$isAjaxEnabled='true'">
					<link rel="stylesheet" href="{$mediaPath}/common/dojo/src/widget/templates/SplitContainer.css" type="text/css" />
					<link rel="stylesheet" href="{$mediaPath}/common/dojo/src/widget/templates/TabContainer.css" type="text/css" />
					<script src="{$mediaPath}/common/dojo/dojo.js" type="text/javascript"/>
					<script src="{$mediaPath}/common/ajax-preferences.js" type="text/javascript"/>
					<script type="text/javascript">
						djConfig.parseWidgets = false;
						djConfig.searchIds = [];
						dojo.require( "dojo.widget.Dialog" );
						dojo.require( "dojo.widget.ContentPane" );
						dojo.require( "dojo.widget.SplitContainer" );
						dojo.require("portal.widget.PortletDragSource");
						dojo.require("portal.widget.PortletDropTarget");
						dojo.require("portal.widget.PortletDragObject");
						dojo.hostenv.writeIncludes();
					</script>
				</xsl:if>				
				
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
				
				<xsl:choose>
					<xsl:when test="not(//focused) and $isAjaxEnabled='true'">
						<xsl:call-template name="preferences"/>
					</xsl:when>
					<xsl:when test="//focused and $isAjaxEnabled='true'">
						<xsl:call-template name="focusedPreferences"/>
					</xsl:when>
				</xsl:choose>

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
				<span class="title">
					<xsl:element name="channel-title">
						<xsl:attribute name="defaultValue">
							<xsl:value-of select="//focused/channel/@name"/>
						</xsl:attribute>
						<xsl:attribute name="channelSubscribeId">
							<xsl:value-of select="@ID" />
						</xsl:attribute>
					</xsl:element>
				</span>
				<br/>
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
					<a id="add-channel-link" href="{$baseActionURL}">Add to my Layout</a>
				</xsl:if>
			</div>		
		</div>
	</xsl:template>

	<xsl:template match="navigation">
		<!-- TABS -->
		<div id="tabs">
			<ul id="tabList">
				<xsl:for-each select="tab">
					<xsl:choose>
						<xsl:when test="@activeTab='false'">
							<!-- INACTIVE TAB -->
							<li id="tab_{@ID}"><a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}"><span><xsl:value-of select="@name"/></span></a></li>
						</xsl:when>
						<xsl:otherwise>
							<!-- ACTIVE TAB -->
							<script type="text/javascript">
								var tabId = "<xsl:value-of select="@ID"/>";
							</script>
							<xsl:variable name="canMoveTabLeft" select="position() > 1 and not(@dlm:moveAllowed='false') and not(./preceding-sibling::node()[position()=last()]/@dlm:moveAllowed='false' and ./preceding-sibling::node()[position()=last()]/@dlm:precedence > @dlm:precedence)"/>
							<xsl:variable name="canMoveTabRight" select="position()!=last() and not(@dlm:moveAllowed='false')"/>
							<xsl:choose>
								<xsl:when test="$isAjaxEnabled='true' and ($canMoveTabRight or $canMoveTabLeft or not(@dlm:editAllowed='false') or not(@dlm:moveAllowed='false'))">
									<li id="tab_{@ID}" class="editable-tab">
										<a id="activeTabLink" title="edit" href="javascript:;" onclick="toggleEditTabDialog('show')">
											<span id="tabName"><xsl:value-of select="@name"/></span>
										</a>
										<a id="editTabLink" style="display:none; cursor: default" href="javascript:;">
											<span id="editTabName">
												<xsl:if test="$canMoveTabLeft">
													<img src="{$mediaPath}/{$skin}/controls/leftarrow.gif" onclick="moveTab('{@ID}', 'left')" title="Move tab left" alt="Move tab left"/>
												</xsl:if>
												<xsl:choose>
													<xsl:when test="not(@dlm:editAllowed='false')">
														<input id="newTabName" type="text" value="{@name}"/>&#160;
														<input id="tabNameSubmit" type="button" value="Done" onclick="updateTabName('{@ID}')" class="portlet-form-button"/>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="@name"/>&#160;
														<input id="tabNameSubmit" type="button" value="Done" onclick="toggleEditTabDialog('hide')" class="portlet-form-button"/>
													</xsl:otherwise>
												</xsl:choose>
												<xsl:if test="$canMoveTabRight">
													<img src="{$mediaPath}/{$skin}/controls/rightarrow.gif" onclick="moveTab('{@ID}', 'right')" title="Move tab right" alt="Move tab right"/>
												</xsl:if>
												<xsl:if test="not(@dlm:deleteAllowed='false')">
													<img src="{$mediaPath}/{$skin}/controls/remove_tab.gif" onclick="deleteTab('{@ID}')" title="Remove tab" alt="Remove tab"/>
												</xsl:if>
											</span>
										</a>
									</li>
								</xsl:when>
								<xsl:otherwise>
									<li id="tab_{@ID}" class="active-tab">
										<a id="activeTabLink" href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}">
											<span><xsl:value-of select="@name"/></span>
										</a>
									</li>
								</xsl:otherwise>
							</xsl:choose>
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
					<xsl:if test="$isAjaxEnabled='true'">
						<div id="preference-items">
							<ul>
								<li><a href="javascript:;" onClick="showAddChannelDialog();">More Content</a></li>
								<li><a href="javascript:;" onClick="showPageLayoutDialog();">Page Layout</a></li>
								<li><a href="javascript:;" onClick="showChooseSkinDialog();">Skin</a></li>
							</ul>
						</div>
					</xsl:if>
					<table id="columns-table" cellpadding="0" cellspacing="15" width="100%">
						<tr id="portal-page-columns">
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
					<td id="column_{@ID}" width="100%" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=1">
					<td id="column_{@ID}" width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=last()">
					<td id="column_{@ID}" width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
				<xsl:when test="position()!=1 and position()!=last()">
					<td id="column_{@ID}" width="{@width}" valign="top">
						<xsl:apply-templates select="channel"/>
					</td>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="channel">
		<xsl:param name="detachedContent"/>
		<!-- PORTLET TOOLBAR -->
		<div id="portlet_{@ID}" class="portletContainer">
			<div id="toolbar_{@ID}" class="portlet-toolbar">
				<div class="icons">
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
							<a id="togglePortlet_{@ID}" href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=false#{@ID}"><img id="portletToggleImg_{@ID}" src="{$mediaPath}/{$skin}/controls/min.gif" width="19" height="19" alt="unshade" title="unshade" style="float:left;padding-top:2px;padding-right:3px;margin-left:-2px;"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a id="togglePortlet_{@ID}" href="{$baseActionURL}?uP_tcattr=minimized&amp;minimized_channelId={@ID}&amp;minimized_{@ID}_value=true#{@ID}"><img id="portletToggleImg_{@ID}" src="{$mediaPath}/{$skin}/controls/max.gif" width="19" height="19" alt="shade" title="shade" style="float:left;padding-top:2px;padding-right:3px;margin-left:-2px;"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<a name="{@ID}" id="{@ID}"></a>
				<h2>
					<xsl:element name="channel-title">
						<xsl:attribute name="defaultValue">
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<xsl:attribute name="channelSubscribeId">
							<xsl:value-of select="@ID" />
						</xsl:attribute>
					</xsl:element>
				</h2>
			</div>
			<!-- PORTLET CONTENT -->
			<xsl:choose>
			    <xsl:when test="@minimized != 'true' or @isPortlet = 'true'">
					<div id="portletContent_{@ID}" class="portlet clearfix {@fname}">
						<xsl:copy-of select="."/>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div id="portletContent_{@ID}" class="portlet clearfix {@fname}" style="display:none">
						<xsl:copy-of select="."/>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</div>
		
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
		<xsl:if test="not(@dlm:deleteAllowed='false') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
			<a id="removePortlet_{@ID}" href="{$baseActionURL}?uP_remove_target={@ID}" onClick="return confirm('Are you sure you want to remove this channel?')"><img src="{$mediaPath}/{$skin}/controls/remove.gif" width="19" height="19" alt="remove" title="remove" /></a>
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

	<xsl:template name="preferences">
		<div id="dojoMenus" style="display:none;">
			<!-- Add Channel Menu -->
			<div id="contentAddingDialog" class="preferences-edit-window" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>More Content</h2></div>
				<div class="portlet clearfix addchannel" style="margin: 0px;">
					<div class="dojoTabContainer portal-dojo-container" style="width: 500px; height: 250px;">
						<div class="dojoTabLabels-top dojoTabNoLayout">
							<div id="contentAddingBrowseButton" onclick="chooseContentAddingMethod('browse')" class="dojoTab current">
								<div>
									<span>Browse Portlets</span>
								</div>
							</div>
							<div id="contentAddingSearchButton" onclick="chooseContentAddingMethod('search')" class="dojoTab">
								<div>
									<span>Search</span>
								</div>
							</div>
						</div>
						<div class="dojoTabPaneWrapper">
							<div id="contentAddingBrowseTab" class="portal-dojo-pane dojoTabPane" style="padding: 10px; position: relative; width:460px; height: 208px;">
								<h4 id="channelLoading">Loading portlet list . . . </h4>
								<table cellspacing="0" cellpadding="0" border="0">
									<tr>
										<td class="portlet-section-subheader">Category</td>
										<td class="portlet-section-subheader">Portlet</td>
									</tr>
									<tr>
										<td><select id="categorySelectMenu" onchange="browseChannelCategory()" size="14" style="width: 150px; background: url({$mediaPath}/{$skin}/skin/loading.gif) no-repeat center center"></select></td>
										<td><select id="channelSelectMenu" onchange="selectChannel(this.value)" size="14" style="width: 300px; background: url({$mediaPath}/{$skin}/skin/loading.gif) no-repeat center center"></select></td>
									</tr>
								</table>
							</div>
							<div id="contentAddingSearchTab" class="portal-dojo-pane dojoTabPane" style="overflow:auto; padding: 10px; display: none; position: relative; width:460px; height: 208px;">
								<p>
									<label class="portlet-form-field-label" for="addChannelSearchTerm">Search for:</label>&#160;
									<input id="addChannelSearchTerm" type="text" onkeydown="searchChannels()"/>
								</p>
								<br/>
								<h3>Matching portlets</h3>
								<ul id="addChannelSearchResults" style="list-style-type: none; padding: 0px; margin-left: 5px;"></ul>
							</div>
						</div>
					</div>
					<br/>
					<h3 class="portal-section-header">Portlet Details</h3>
					<h4 id="channelTitle" class="portal-section-subheader"></h4>
					<p id="channelDescription"></p>
					<p style="padding-top: 10px;">
						<input id="addChannelId" type="hidden"/>
						<button id="addChannelLink" onclick="addChannel()" class="portlet-form-button">Add to my page</button>&#160;
						<button id="previewChannelLink" class="portlet-form-button">Use it now</button>&#160;
						<button id="contentDialogCloser" class="portlet-form-button">Done</button>
					</p>
				</div>
			</div>
			<div id="pageLayoutDialog" class="preferences-edit-window" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Edit Page Layout</h2></div>
				<div class="portlet clearfix editcolumns">
					<p><label class="portlet-form-field-label">Number of columns:</label>
						<xsl:element name="input">
							<xsl:attribute name="onclick">changeColumns(1);</xsl:attribute>
							<xsl:attribute name="name">columnNum</xsl:attribute>
							<xsl:attribute name="type">radio</xsl:attribute>
							<xsl:if test="count(/layout/content/column)=1">
								<xsl:attribute name="checked">true</xsl:attribute>
							</xsl:if>
						</xsl:element> <label class="portlet-form-field-label">1</label>
						<xsl:element name="input">
							<xsl:attribute name="onclick">changeColumns(2);</xsl:attribute>
							<xsl:attribute name="name">columnNum</xsl:attribute>
							<xsl:attribute name="type">radio</xsl:attribute>
							<xsl:if test="count(/layout/content/column)=2">
								<xsl:attribute name="checked">true</xsl:attribute>
							</xsl:if>
						</xsl:element> <label class="portlet-form-field-label">2</label>
						<xsl:element name="input">
							<xsl:attribute name="onclick">changeColumns(3);</xsl:attribute>
							<xsl:attribute name="name">columnNum</xsl:attribute>
							<xsl:attribute name="type">radio</xsl:attribute>
							<xsl:if test="count(/layout/content/column)=3">
								<xsl:attribute name="checked">true</xsl:attribute>
							</xsl:if>
						</xsl:element> <label class="portlet-form-field-label">3</label>
					</p>
					
					<p><label class="portlet-form-field-label">Column widths:</label></p>
					<br/>
					<div id="columnWidthsAdjuster"></div>
					<br/>
					<p>
						<button class="portlet-form-button" id="layoutDialogCloser">Done</button>
					</p>
				</div>
			</div>	
			<div id="skinChoosingDialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Choose a Skin</h2></div>
				<div class="portlet clearfix chooseskin" style="margin: 0px;">
					<h4 id="skinLoading">Loading portlet list . . . </h4>
					<form onsubmit="return chooseSkin(this);">
						<p class="portlet-form-label">
							Choose a skin for your portal view:
						</p>
						<p id="skinList"></p>
						<p>
							<input type="submit" value="Choose" class="portlet-form-button"/>&#160;
							<input id="skinDialogCloser" type="cancel" value="Cancel" class="portlet-form-button"/>
						</p>
					</form>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			
			var portalUrl = '<xsl:value-of select="$baseActionURL"/>';
			var preferencesUrl = 'ajax/preferences';
			var channelListUrl = 'ajax/channelList';
			var mediaPath = '<xsl:value-of select="$mediaPath"/>';
			var currentSkin = '<xsl:value-of select="$skin"/>';
			var columnCount = <xsl:value-of select="count(/layout/content/column)"/>;
			var skinPath = '<xsl:value-of select="$mediaPath"/>/<xsl:value-of select="$skin"/>';
			
			var contentAddingMenu, pageLayoutMenu, skinChoosingMenu;
			dojo.addOnLoad(initAjaxPortalPreferences);
			
			<xsl:for-each select="/layout/content/column">
				new portal.widget.PortletDropTarget("column_<xsl:value-of select="@ID"/>", [<xsl:for-each select="/layout/content/column">"<xsl:value-of select="@ID"/>dt"<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>]);
				<xsl:for-each select="channel">
					<xsl:if test="not(@dlm:moveAllowed='false')">
						var drag = new portal.widget.PortletDragSource("toolbar_<xsl:value-of select="@ID"/>", "<xsl:value-of select="../@ID"/>dt");
						drag.setDragTarget(dojo.byId('portlet_<xsl:value-of select="@ID"/>'));
						dojo.byId('toolbar_<xsl:value-of select="@ID"/>').style.cursor = "move";
					</xsl:if>
					<xsl:if test="not(@dlm:deleteAllowed='false')">
						a = dojo.byId("removePortlet_" + '<xsl:value-of select="@ID"/>');
						a.href = "javascript:;";
						a.onclick = function(){deleteChannel('<xsl:value-of select="@ID"/>')};
					</xsl:if>
					a = dojo.byId("togglePortlet_" + '<xsl:value-of select="@ID"/>');
					a.href = "javascript:;";
					a.onclick = function(){toggleChannel('<xsl:value-of select="@ID"/>')};
				</xsl:for-each>
			</xsl:for-each>
			
			
		</script>
	</xsl:template>
	
	<xsl:template name="focusedPreferences">
		<div id="dojoMenus" style="display:none;">
			<!-- Add Channel Menu -->
			<div id="focusedContentAddingDialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Add Portlet to My Layout</h2></div>
				<div class="portlet clearfix addchannel" style="margin: 0px;">
					<form onsubmit="return addFocusedChannel(this);">
						<p class="portlet-form-label">
							Choose a page in which to add this portlet:
						</p>
						<p>
							<xsl:for-each select="/layout/navigation/tab">
								<input name="targetTab" value="{@ID}" type="radio"/> <label class="portlet-form-field-label"><xsl:value-of select="@name"/></label><br/>
							</xsl:for-each>
						</p>
						<p>
							<input name="channelId" type="hidden" value="{//focused/channel/@chanID}"/>
							<input type="submit" value="Add" class="portlet-form-button"/>&#160;
							<input id="focusedContentDialogCloser" type="cancel" value="Cancel" class="portlet-form-button"/>
						</p>
					</form>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			var portalUrl = '<xsl:value-of select="$baseActionURL"/>';
			var preferencesUrl = 'ajax/preferences';
			var channelListUrl = 'ajax/channelList';
			var skinPath = '<xsl:value-of select="$mediaPath"/>/<xsl:value-of select="$skin"/>';
			var focusedContentAddingMenu;
			dojo.addOnLoad(initFocusedAjaxPortalPreferences);
		</script>
	</xsl:template>
		
</xsl:stylesheet>
