<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="no"/>
	<!--<xsl:param name="catID" select="/layout/folder[1]/@ID"/>-->
	<xsl:param name="catID" select="'C006'"/>
	<xsl:param name="theme" select="'imm'"/>
	<xsl:param name="css" select="'imm001'"/>
	<xsl:param name="bgcolor" select="'#000000'"/>
	<xsl:param name="uplogo" select="'upimm001.gif'"/>
	<xsl:param name="logo" select="'imm001.gif'"/>
	<xsl:param name="collapsed" select="false"/>
	<xsl:template match="layout">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<title>uPortal</title>
				<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
				<link type="text/css" rel="stylesheet">
					<xsl:attribute name="href">media/org/jasig/portal/layout/treeColumn/theme_<xsl:value-of select="$theme"/>/<xsl:value-of select="$css"/>.css</xsl:attribute>
				</link>
			</head>
			<body style="uportal-background">
				<xsl:attribute name="bgcolor">#<xsl:value-of select="$bgcolor"/></xsl:attribute>
				<table width="100%" border="0" cellspacing="0" cellpadding="5">
					<tr>
						<td bgcolor="#666633" nowrap="nowrap" align="left" valign="top" class="uportal-background-dark">
							<img>
								<xsl:attribute name="src">media/org/jasig/portal/layout/treeColumn/theme_<xsl:value-of select="$theme"/>/<xsl:value-of select="$uplogo"/></xsl:attribute>
							</img>
						</td>
						<td bgcolor="#CCCC99" nowrap="nowrap" align="left" valign="top" class="uportal-background-light">
							<img>
								<xsl:attribute name="src">media/org/jasig/portal/layout/treeColumn/theme_<xsl:value-of select="$theme"/>/<xsl:value-of select="$logo"/></xsl:attribute>
							</img>
						</td>
					</tr>
					<tr>
						<td class="uportal-background-med" align="left" valign="top" nowrap="nowrap">
							<p>
								<xsl:apply-templates/>
							</p>
						</td>
						<td width="100%" class="uportal-background-content" align="left" valign="top">
							<!--Content cell -->
							<xsl:apply-templates select="//folder[@ID=$catID]" mode="contentCell"/>
						</td>
					</tr>
					<tr>
						<td class="uportal-background-dark" nowrap="nowrap">
							<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="15" border="0"/>
						</td>
						<td width="100%" class="uportal-background-light">
							<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="15" border="0"/>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="folder">
		<xsl:if test="count(ancestor::folder) != '0'">
			<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" height="16">
				<xsl:attribute name="width"><xsl:value-of select="count(ancestor::folder)*20"/></xsl:attribute>
			</img>
		</xsl:if>
		<a>
			<xsl:attribute name="href">JavaScript:alert("Developer note:\nClose icon - [<xsl:value-of select="@name"/>] folder\nFeature in Progress")</xsl:attribute>
			<img src="media/org/jasig/portal/layout/treeColumn/minus001.gif" width="16" height="16" alt="Click to close folder" border="0"/>
		</a>
		<!--Need to keep state of the categories and channels in the tree
		<a><xsl:attribute name="href">index.jsp?catTree=close&amp;catID=<xsl:value-of select="@ID"/></xsl:attribute>
		<img src="media/org/jasig/portal/layout/treeColumn/minus.gif" width="16" height="16" alt="Click to close folder" border="0"/>
		</a>-->
		<a class="uportal-navigation-category">
			<xsl:attribute name="href">index.jsp?stylesheetTarget=s&amp;catID=<xsl:value-of select="@ID"/></xsl:attribute>
			<img src="media/org/jasig/portal/layout/treeColumn/category001.gif" width="16" height="16" border="0"/>
			<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="4" height="16" border="0"/>
			<xsl:choose>
				<xsl:when test="@ID=$catID">
					<span class="uportal-navigation-category-selected">
						<xsl:value-of select="translate(@name,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
					</span>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="translate(@name,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
				</xsl:otherwise>
			</xsl:choose>
		</a>
		<br/>
		<xsl:if test="child::column">
			<xsl:apply-templates select="column/channel"/>
		</xsl:if>
		<xsl:apply-templates select="folder"/>
	</xsl:template>
	<xsl:template match="channel">
		<xsl:if test="count(ancestor::folder) != '0'">
			<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" height="16">
				<xsl:attribute name="width"><xsl:value-of select="count(ancestor::folder)*20+16"/></xsl:attribute>
			</img>
		</xsl:if>
		<a class="uportal-navigation-channel">
			<xsl:attribute name="href">JavaScript:alert("Developer note:\nChannel - [<xsl:value-of select="@name"/>] display as root\nFeature in Progress")</xsl:attribute>
			<img src="media/org/jasig/portal/layout/treeColumn/channel001.gif" width="16" height="16" border="0"/>
			<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="4" height="16" border="0"/>
			<xsl:value-of select="@name"/>
		</a>
		<br/>
	</xsl:template>
	<xsl:template match="folder" mode="contentCell">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
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
	</xsl:template>
	<xsl:template name="controlRow">
		<xsl:for-each select="column">
			<xsl:choose>
				<xsl:when test="position()=1 and position()=last()">
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
					<td width="100%" flag="controlrow set to 100%">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=1">
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
					<td flag="controlrow set to @width attribute">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
					<td width="1">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=last()">
					<td flag="controlrow set to @width attribute">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()!=1 and position()!=last()">
					<td flag="controlrow set to @width attribute">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
					<td width="1">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="contentRow">
		<xsl:for-each select="column">
			<xsl:choose>
				<xsl:when test="position()=1 and position()=last()">
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
					<td align="left" valign="top" width="100%">
						<xsl:apply-templates select="channel" mode="contentCell"/>
					</td>
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=1">
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
					<td align="left" valign="top">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<xsl:apply-templates select="channel" mode="contentCell"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
					<td width="1" class="uportal-background-dark">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()=last()">
					<td align="left" valign="top">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<xsl:apply-templates select="channel" mode="contentCell"/>
					</td>
					<td width="5">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="5" height="20"/>
					</td>
				</xsl:when>
				<xsl:when test="position()!=1 and position()!=last()">
					<td align="left" valign="top">
						<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
						<xsl:apply-templates select="channel" mode="contentCell"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
					<td width="1" class="uportal-background-dark">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
					</td>
					<td width="10">
						<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="10" height="20"/>
					</td>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="channel" mode="contentCell">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr valign="bottom">
				<td class="uportal-channel-title" align="left">
					<xsl:value-of select="@name"/>
				</td>
				<td align="right" nowrap="nowrap">
					<xsl:if test="@hasHelp='true'">
						<a>
							<xsl:attribute name="href">index.jsp?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=help</xsl:attribute>
							<img alt="help" src="media/org/jasig/portal/layout/treeColumn/help001.gif" width="16" height="16" border="0"/>
						</a>
					</xsl:if>
					<xsl:if test="@editable='true'">
						<a>
							<xsl:attribute name="href">index.jsp?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=edit</xsl:attribute>
							<img alt="edit" src="media/org/jasig/portal/layout/treeColumn/edit001.gif" width="16" height="16" border="0"/>
						</a>
					</xsl:if>
					<xsl:if test="@minimizable='true'">
						<a>
							<xsl:attribute name="href">index.jsp?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=minimize</xsl:attribute>
							<img alt="minimize" src="media/org/jasig/portal/layout/treeColumn/min001.gif" width="16" height="16" border="0"/>
						</a>
					</xsl:if>
					<xsl:if test="@detachable='true'">
						<a>
							<xsl:attribute name="href">JavaScript:openWin('detach.jsp?tab=<xsl:number count="folder"/>&amp;column=<xsl:number count="column"/>&amp;channel=<xsl:number count="channel"/>', 'detachedWindow', 550, 450)</xsl:attribute>
							<img alt="detach" src="media/org/jasig/portal/layout/treeColumn/detach001.gif" width="16" height="16" border="0"/>
						</a>
					</xsl:if>
					<xsl:if test="@removable='true'">
						<a>
							<xsl:attribute name="href">index.jsp?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=remove</xsl:attribute>
							<img alt="remove" src="media/org/jasig/portal/layout/treeColumn/remove001.gif" width="16" height="16" border="0"/>
						</a>
					</xsl:if>
				</td>
			</tr>
			<tr class="uportal-background-dark">
				<td height="1" colspan="2">
					<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="1"/>
				</td>
			</tr>
			<tr>
				<td class="body" colspan="2">
					<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="5"/>
				</td>
			</tr>
			<tr>
				<td class="body" colspan="2">
					<xsl:if test="@minimized='false'">
						<xsl:copy-of select="."/>
					</xsl:if>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<img src="media/org/jasig/portal/layout/treeColumn/transparent.gif" width="1" height="20"/>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
