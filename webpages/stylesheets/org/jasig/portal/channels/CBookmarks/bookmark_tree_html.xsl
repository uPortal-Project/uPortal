<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">media/org/jasig/portal/channels/CBookmarks/</xsl:param>

	<xsl:template name="BookmarkTree" match="xbel">
		<xsl:param name="TreeMode">View</xsl:param>
		<!-- TreeMode: <xsl:value-of select="$TreeMode"/><br/> -->
		<table border="0">
			<xsl:apply-templates select="/xbel/folder">
				<xsl:sort select="title"/>
				<xsl:with-param name="TreeMode"><xsl:value-of select="$TreeMode"/></xsl:with-param>
			</xsl:apply-templates>
			
			<xsl:apply-templates select="/xbel/bookmark">
				<xsl:sort select="title"/>
				<xsl:with-param name="TreeMode"><xsl:value-of select="$TreeMode"/></xsl:with-param>
			</xsl:apply-templates>
			
			<xsl:if test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">
				<tr>
					<td><input type="radio" name="FolderRadioButton" value="RootLevel" checked="true"/></td>
					<td>Root Level</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	
	<xsl:template match="folder">
		<xsl:param name="TreeMode">View</xsl:param>
		
		<tr>
			<!-- Display a radio button or checkbox if in edit mode -->
			<td>
				<xsl:choose>
					<xsl:when test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">
						<input type="radio" name="FolderRadioButton" value="{@id}"/>
					</xsl:when>
					<xsl:when test="$TreeMode='DeleteFolder'">
						<input type="checkbox" name="FolderCheckbox#{@id}"/>
					</xsl:when>
				</xsl:choose>
			</td>
			
			<td>
				<!-- Indent the folder -->
				<img src="{$imagesURL}trans20x20.gif" width="{(count(ancestor::*) - 1) * 10}" height="20"/>
				
				<!-- Display an open or closed folder icon and the folder title -->
				<xsl:choose>
					<xsl:when test="@folded='yes'">
						<a href="{$baseActionURL}command=unfold&amp;ID={@id}">
							<img src="{$imagesURL}closedFolder.gif" width="20" height="20" border="0" alt="Closed Folder"/>
							<xsl:value-of select="title"/>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<a href="{$baseActionURL}command=fold&amp;ID={@id}">
							<img src="{$imagesURL}openFolder.gif" width="20" height="20" border="0" alt="Open Folder"/>
							<xsl:value-of select="title"/>
						</a>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
			
		<!-- Recurse through the subtrees if the folder is open -->
		<xsl:if test="@folded='no'">
			<xsl:apply-templates select="folder">
				<xsl:sort select="title"/>
				<xsl:with-param name="TreeMode"><xsl:value-of select="$TreeMode"/></xsl:with-param>
			</xsl:apply-templates>
			
			<xsl:apply-templates select="bookmark">
				<xsl:sort select="title"/>
				<xsl:with-param name="TreeMode"><xsl:value-of select="$TreeMode"/></xsl:with-param>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

	<xsl:template match="bookmark">
		<xsl:param name="TreeMode">View</xsl:param>
		<tr>
			<!-- Display a checkbox if in edit mode -->
			<td>
				<xsl:choose>
					<xsl:when test="$TreeMode='DeleteBookmark'">
						<input type="checkbox" name="BookmarkCheckbox#{@id}"/>
					</xsl:when>
				</xsl:choose>
			</td>
			
			<td>
				<!-- Indent the bookmark -->
				<img src="{$imagesURL}trans20x20.gif" width="{(count(ancestor::*) - 1) * 10}" height="20"/>
				
				<a href="{@href}"><xsl:value-of select="title"/></a>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
