<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- Import the bookmark tree stylesheet -->
	<xsl:import href="bookmark_tree_html.xsl"/>
	
	<!-- Make sure that XHTML is being output to facilitate caching -->
	<xsl:output method="xml" indent="yes"/>

	<!-- Take the baseActionURL and the location of the images as parameters -->
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">/media/org/jasig/portal/channels/CBookmarks/</xsl:param>

	<!-- Find out whether you are adding a folder or a bookmark -->
	<xsl:param name="EditMode">AddBookmark</xsl:param>

	<xsl:template match="/">
		<div>
			<form action="{$baseActionURL}?command={$EditMode}" method="post">
				<xsl:call-template name="BookmarkTree">
					<xsl:with-param name="TreeMode"><xsl:value-of select="$EditMode"/></xsl:with-param>
				</xsl:call-template>
			
				<hr/>
				
				Please fill in the information below and select the folder that the new
				<xsl:choose>
					<xsl:when test="$EditMode='AddBookmark'">
						bookmark
					</xsl:when>
					<xsl:when test="$EditMode='AddFolder'">
						folder
					</xsl:when>
				</xsl:choose>
				should appear in.
				
				<hr/>
				
				<xsl:choose>
					<xsl:when test="$EditMode='AddFolder'">
						<table border="0">
							<tr>
								<td colspan="2">Add New Folder</td>
							</tr>
							<tr>
								<td>Name:</td>
								<td><input type="text" name="FolderTitle"></input></td>
							</tr>
							<tr>
								<td colspan="2">
									<input type="submit" name="SubmitButton" value="Add"></input>
									<input type="submit" name="SubmitButton" value="Cancel"></input>
								</td>
							</tr>
						</table>
					</xsl:when>
					<xsl:when test="$EditMode='AddBookmark'">
						<table border="0">
							<tr>
								<td colspan="2">Add New Bookmark</td>
							</tr>
							<tr>
								<td>Title:</td>
								<td><input type="text" name="BookmarkTitle"></input></td>
							</tr>
							<tr>
								<td>URL:</td>
								<td><input type="text" name="BookmarkURL"></input></td>
							</tr>
							<tr>
								<td>Description:</td>
								<td><textarea rows="5" cols="20" name="BookmarkDescription"></textarea></td>
							</tr>
							<tr>
								<td colspan="2">
									<input type="submit" name="SubmitButton" value="Add"></input>
									<input type="submit" name="SubmitButton" value="Cancel"></input>
								</td>
							</tr>
						</table>
					</xsl:when>
				</xsl:choose>
			</form>
		</div>
	</xsl:template>

</xsl:stylesheet>
