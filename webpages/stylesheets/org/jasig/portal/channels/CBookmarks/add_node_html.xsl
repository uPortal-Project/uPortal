<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- Import the bookmark tree stylesheet -->
	<xsl:import href="bookmark_tree_html.xsl"/>
	
	<!-- Make sure that XHTML is being output to facilitate caching -->
	<xsl:output method="xml" indent="yes"/>

	<!-- Take the baseActionURL and the location of the images as parameters -->
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">media/org/jasig/portal/channels/CBookmarks/</xsl:param>

	<!-- Find out whether you are adding a folder or a bookmark -->
	<xsl:param name="EditMode">AddBookmark</xsl:param>

	<xsl:template match="/">
			<form action="{$baseActionURL}?command={$EditMode}" method="post">
      						<table border="0" cellpadding="6">
							<tr>
								<td class="uportal-channel-subtitle">Add New <xsl:value-of select="substring-after($EditMode,'Add')"/></td>
							</tr></table>
				<xsl:call-template name="BookmarkTree">
					<xsl:with-param name="TreeMode"><xsl:value-of select="$EditMode"/></xsl:with-param>
				</xsl:call-template>
			
				<hr/>
				
				Please fill out the form below and select the folder in which the new
				<xsl:choose>
					<xsl:when test="$EditMode='AddBookmark'">
						bookmark
					</xsl:when>
					<xsl:when test="$EditMode='AddFolder'">
						folder
					</xsl:when>
				</xsl:choose>
				will appear.
				
				<hr/>
				
				<xsl:choose>
					<xsl:when test="$EditMode='AddFolder'">
						<table border="0">
						
							<tr><td class="uportal-label">Folder Name:</td></tr>
							<tr><td><input type="text" name="FolderTitle" class="uportal-input-text"></input></td></tr>
							<tr><td><input type="submit" name="SubmitButton" value="Add" class="uportal-button"></input>
									<input type="submit" name="SubmitButton" value="Cancel" class="uportal-button"></input>
								</td>
							</tr>
						</table>
					</xsl:when>
					<xsl:when test="$EditMode='AddBookmark'">
						<table border="0">
					
							<tr>
								<td class="uportal-label">Bookmark Title:</td>
								<tr><td><input type="text" name="BookmarkTitle" class="uportal-input-text"></input></td></tr>
							</tr>




							<tr>
								<td  class="uportal-label">URL:</td></tr>
							<tr>	<td><input type="text" name="BookmarkURL" class="uportal-input-text"></input></td>
							</tr>
							<tr>
								<td class="uportal-label">Description:</td></tr>
							<tr>	<td><textarea rows="5" cols="20" name="BookmarkDescription" class="uportal-input-text"></textarea></td>
							</tr>
							<tr>
								<td>
									<input type="submit" name="SubmitButton" value="Add" class="uportal-button"></input>
									<input type="submit" name="SubmitButton" value="Cancel" class="uportal-button"></input>
								</td>
							</tr>
						</table>
					</xsl:when>
				</xsl:choose>
			</form>
	</xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
