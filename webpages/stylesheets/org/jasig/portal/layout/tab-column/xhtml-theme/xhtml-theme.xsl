<?xml version="1.0" encoding="utf-8"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
	<xsl:param name="skin" select="'spring'"/>
	<xsl:variable name="mediaPath">media/org/jasig/portal/layout/tab-column/xhtml-theme</xsl:variable>
	<xsl:param name="isAjaxEnabled" select="'false'"/>
	
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
				<xsl:if test="$isAjaxEnabled='true'">
					<script src="{$mediaPath}/common/dojo/dojo.js" type="text/javascript"/>
					<script src="{$mediaPath}/common/dojo/src/debug.js" type="text/javascript"/>
					<script src="{$mediaPath}/common/ajax-preferences.js" type="text/javascript"/>
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
							<li id="tab_{@ID}" class="activeTab"><a id="activeTabLink" href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab={position()}">
								<xsl:choose>
									<xsl:when test="$isAjaxEnabled='true'"><span id="editableTab" dojoType="inlineEditBox" value="{@name}"><xsl:value-of select="@name"/></span></xsl:when>
									<xsl:otherwise><span><xsl:value-of select="@name"/></span></xsl:otherwise>
								</xsl:choose>
							</a></li>
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
								<li><a href="javascript:;" onClick="showEditColumnsDialog();">Page Layout</a></li>
								<li><a href="javascript:;" onClick="dlg2.show();">Tabs Layout</a></li>
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
		<div id="portlet_{@ID}">
			<xsl:element name="div">
				<xsl:attribute name="id">toolbar_<xsl:value-of select="@ID"/></xsl:attribute>
				<xsl:attribute name="class">portlet-toolbar</xsl:attribute>
				<xsl:if test="$isAjaxEnabled='true'">
					<xsl:attribute name="style">cursor: move</xsl:attribute>
				</xsl:if>
				<div id="icons">
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
			</xsl:element>
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
		<xsl:if test="not(@unremovable='true') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
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
			<div id="dialog0" class="preferences-edit-window" dojoType="dialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Add Channel</h2></div>
				<div class="portlet clearfix addchannel" style="margin: 0px;">
					<div id="addChannelTabContainer" dojoType="TabContainer" style="width: 500px; height: 250px;">
						<div id="tab1" dojoType="ContentPane" label="Browse" >
							<p>
								<select id="categorySelectMenu" onchange="selectChannelCategory()" size="12" style="width: 150px;"></select>
								<select id="channelSelectMenu" onchange="selectChannelChannel(this.value)" size="12" style="width: 300px;"></select>
							</p>
						</div>
						<div id="tab2" dojoType="ContentPane" label="Search">
							<p>
								Search for: <input id="addChannelSearchTerm" type="text" onkeydown="searchChannels()"/>
							</p>
							<h3>Results</h3>
							<ul id="addChannelSearchResults" style="list-style-type: none; padding: 0px; margin-left: 0px;"></ul>
						</div>
					</div>
					<br/>
					<h3 id="channelTitle"></h3>
					<p id="channelDescription"></p>
					<p style="padding-top: 10px;">
						<input id="addChannelId" type="hidden"/>
						<button onclick="addChannel()" class="uportal-button">Add to my page</button>&#160;
						<button id="previewChannelLink" class="uportal-button">Use it now</button>&#160;
						<button id="hider0" class="uportal-button">Done</button>
					</p>
				</div>
			</div>
			<!-- Edit Columns Menu -->
			<div id="dialog1" class="preferences-edit-window" dojoType="dialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Edit Columns</h2></div>
				<div class="portlet clearfix editcolumns">
					<table class="edit-preferences-window">
						<tr id="layout-edit-columns">
							<xsl:for-each select="/layout/content/column">
								<td id="layoutColumn_{@ID}" width="{@width}">
									<div class="container-title">Column <xsl:value-of select="position()"/></div>
									<div style="padding-top: 7x; padding-bottom: 7px;">
										<a onclick="moveColumn('{@ID}', 'left')" href="javascript:;" title="Move column left"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/leftarrow.gif"/></a>
										<a onclick="moveColumn('{@ID}', 'right')" href="javascript:;" title="Move column right"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/rightarrow.gif"/></a>
										<a onclick="deleteColumn('{@ID}')" href="javascript:;" title="Remove column"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/remove.gif"/></a>
									</div>
									<div id="columnContents_{@ID}"></div>
								</td>
							</xsl:for-each>
						</tr>
					</table>
					<div><button onclick="addColumn()" class="uportal-button">Add Column</button>&#160;<button class="uportal-button" id="hider1">Done</button></div>
				</div>
			</div>	
			<!-- Edit Tabs Menu -->
			<div id="dialog2" class="preferences-edit-window" dojoType="dialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Edit Tabs</h2></div>
				<div class="portlet clearfix edittabs">
					<table class="edit-preferences-window">
						<tr id="layout-edit-tabs">
							<xsl:for-each select="/layout/navigation/tab">
								<td id="layoutTab_{@ID}">
									<div class="container-title"><xsl:value-of select="@name"/></div>
									<div style="padding-top: 7x; padding-bottom: 7px;">
										<a onclick="moveTab('{@ID}', 'left')" href="javascript:;" title="Move tab left"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/leftarrow.gif"/></a>
										<a onclick="moveTab('{@ID}', 'right')" href="javascript:;" title="Move tab right"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/rightarrow.gif"/></a>
										<xsl:if test="@activeTab='false'">
											<a onclick="deleteTab('{@ID}')" href="javascript:;" title="Remove tab"><img class="controlIcon" src="{$mediaPath}/{$skin}/controls/remove.gif"/></a>
										</xsl:if>
									</div>
								</td>
							</xsl:for-each>
						</tr>
					</table>
					<div><button onclick="addTab()" class="uportal-button">Add Tab</button>&#160;<button class="uportal-button" id="hider2">Done</button></div>
				</div>
			</div>	
		</div>
		<script type="text/javascript">
			
			dojo.require( "dojo.widget.*" );
			dojo.require( "dojo.event.*" );
			dojo.require("portal.widget.PortletDragSource");
			dojo.require("portal.widget.PortletDropTarget");
			dojo.require("portal.widget.PortletDragObject");
			dojo.hostenv.writeIncludes(); 
			
			var portalUrl = '<xsl:value-of select="$baseActionURL"/>';
			var preferencesUrl = 'ajax/preferences';
			var channelListUrl = 'ajax/channelList';
			var columnCount = <xsl:value-of select="count(/layout/content/column)"/>;
			var skinPath = '<xsl:value-of select="$mediaPath"/>/<xsl:value-of select="$skin"/>';
			
			var dlg0, dlg1, dlg2;
			dojo.addOnLoad(init);
			
			<xsl:for-each select="/layout/content/column">
				new portal.widget.PortletDropTarget("column_<xsl:value-of select="@ID"/>", [<xsl:for-each select="/layout/content/column">"<xsl:value-of select="@ID"/>dt"<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>]);
				<xsl:for-each select="channel">
					var drag = new portal.widget.PortletDragSource("toolbar_<xsl:value-of select="@ID"/>", "<xsl:value-of select="../@ID"/>dt");
					drag.setDragTarget(dojo.byId('portlet_<xsl:value-of select="@ID"/>'));
					<xsl:if test="not(@unremovable='true') and not(//focused) and /layout/navigation/tab[@activeTab='true']/@immutable='false'">
						a = dojo.byId("removePortlet_" + '<xsl:value-of select="@ID"/>');
						a.href = "javascript:;";
						a.onclick = function(){deleteChannel('<xsl:value-of select="@ID"/>')};
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
			
		</script>
	</xsl:template>
	
	<xsl:template name="focusedPreferences">
		<div id="dojoMenus" style="display:none;">
			<!-- Add Channel Menu -->
			<div id="dialog0" dojoType="dialog" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
				<div class="portlet-toolbar"><h2>Add Channel to My Layout</h2></div>
				<div class="portlet clearfix addchannel" style="margin: 0px;">
					<form onsubmit="return addFocusedChannel(this);">
						<p>
							Choose a page in which to add this channel:
						</p>
						<p>
							<xsl:for-each select="/layout/navigation/tab">
								<input name="targetTab" value="{@ID}" type="radio"/> <xsl:value-of select="@name"/><br/>
							</xsl:for-each>
						</p>
						<p>
							<input name="channelId" type="hidden" value="{//focused/channel/@chanID}"/>
							<input type="submit" value="Add" class="uportal-button"/>&#160;
							<input id="hider0" type="cancel" value="Cancel" class="uportal-button"/>
						</p>
					</form>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			dojo.require( "dojo.widget.*" );
			dojo.require( "dojo.event.*" );
			dojo.hostenv.writeIncludes(); 
			var portalUrl = '<xsl:value-of select="$baseActionURL"/>';
			var preferencesUrl = 'ajax/preferences';
			var channelListUrl = 'ajax/channelList';
			var skinPath = '<xsl:value-of select="$mediaPath"/>/<xsl:value-of select="$skin"/>';
			var dlg0;
			dojo.addOnLoad(initFocused);
		</script>
	</xsl:template>
	
	
</xsl:stylesheet>
