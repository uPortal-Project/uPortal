<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="no"/>
	<xsl:param name="toPage" select="2"/>
	<xsl:param name="baseActionURL">actionURLnotPassed</xsl:param>
	<xsl:template match="ChapterText">
		<!--pager-->
		<p class="uportal-crumbtrail" align="right">
			<a name="top">page</a>: 
<xsl:if test="$toPage != 1">
				<a>
					<xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=turn&amp;toPage=<xsl:value-of select="($toPage) - 1"/></xsl:attribute>
					<img src="media/org/jasig/portal/channels/CPageViewer/pagenavarrowleftred.gif" width="11" height="13" border="0"/>
				</a>
				<xsl:text>&#160;</xsl:text>
			</xsl:if>
			<xsl:call-template name="pager"/>
			<xsl:if test="$toPage != count(page)">
				<a>
					<xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=turn&amp;toPage=<xsl:value-of select="($toPage) + 1"/></xsl:attribute>
					<img src="media/org/jasig/portal/channels/CPageViewer/pagenavarrowrightred.gif" width="11" height="13" border="0"/>
				</a>
			</xsl:if>
		</p>
		<!--end pager-->
		<xsl:apply-templates select="page[$toPage]"/>
		<!--back to top and previous-next footer-->
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td colspan="2">
					<a href="#top">
						<br/>
						<img src="media/org/jasig/portal/channels/CPageViewer/backtotop.gif" width="76" height="13" border="0"/>
					</a>
					<br/>
					<br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="uportal-background-dark">
					<img src="media/org/jasig/portal/channels/CPageViewer/transparent.gif" width="1" height="1"/>
				</td>
			</tr>
			<tr>
				<td>
					<xsl:if test="$toPage != 1">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=turn&amp;toPage=<xsl:value-of select="($toPage) - 1"/></xsl:attribute>
							<img src="media/org/jasig/portal/channels/CPageViewer/previous.gif" width="61" height="13" border="0"/>
						</a>
					</xsl:if>
				</td>
				<td align="right">
					<xsl:if test="$toPage != count(page)">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=turn&amp;toPage=<xsl:value-of select="($toPage) + 1"/></xsl:attribute>
							<img src="media/org/jasig/portal/channels/CPageViewer/next.gif" width="39" height="13" border="0"/>
						</a>
					</xsl:if>
				</td>
			</tr>
		</table>
		<br/>
		<!--end back to top and previous-next footer-->
	</xsl:template>
	<xsl:template name="pager">
		<xsl:for-each select="page">
			<a>
				<xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=turn&amp;toPage=<xsl:value-of select="position()"/></xsl:attribute>
				<xsl:choose>
					<xsl:when test="position() = $toPage">
						<b>
							<xsl:value-of select="position()"/>
						</b>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="position()"/>
					</xsl:otherwise>
				</xsl:choose>
			</a>
			<xsl:choose>
				<xsl:when test="position()!=last()">
					<xsl:text>&#160;|&#160;</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>&#160;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="page">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="heading">
		<!--Heading-->
		<p align="left" class="uportal-channel-title">
			<xsl:value-of select="node()"/>
		</p>
		<!--end Heading-->
	</xsl:template>
	<xsl:template match="paragraph">
		<!--Paragraph-->
		<p align="left" class="uportal-channel-text">
			<xsl:value-of select="node()"/>
		</p>
		<xsl:apply-templates select="bullet"/>
		<!--end Paragraph-->
	</xsl:template>
	<xsl:template match="subheading">
		<!--Subheading-->
		<blockquote>
			<p class="uportal-channel-subtitle">
				<xsl:value-of select="node()"/>
			</p>
		</blockquote>
		<!--end Subheading-->
	</xsl:template>
	<xsl:template match="image">
		<!--image with caption-->
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr align="center">
				<td>
					<img>
						<xsl:attribute name="src">media/org/jasig/portal/channels/CPageViewer/<xsl:value-of select="//PageViewer/ID/."/>/<xsl:value-of select="relativeURL"/></xsl:attribute>
					</img>
				</td>
			</tr>
			<tr align="center">
				<td class="uportal-crumbtrail">
					<br/>
					<b>
						<xsl:value-of select="caption"/>
					</b>
				</td>
			</tr>
		</table>
		<!--end image with caption-->
	</xsl:template>
	<xsl:template match="bullet">
		<!--bullet-->
		<ul>
			<li class="uportal-channel-text">
				<xsl:value-of select="node()"/>
			</li>
		</ul>
		<!--end bullet-->
	</xsl:template>
	<xsl:template match="ID"/>
</xsl:stylesheet>
