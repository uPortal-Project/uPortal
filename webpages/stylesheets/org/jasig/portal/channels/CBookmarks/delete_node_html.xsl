<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="bookmark_tree_html.xsl" /> 
	
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">media/org/jasig/portal/channels/CBookmarks/</xsl:param>
		
	<!-- This parameter tells the stylesheet which nodes to put radio buttons next to -->
	<xsl:param name="EditMode">DeleteBookmark</xsl:param>
	
	<xsl:template match="/">
		<div>
			<form action="{$baseActionURL}command={$EditMode}" method="post">
				<xsl:call-template name="BookmarkTree">
					<xsl:with-param name="TreeMode"><xsl:value-of select="$EditMode"/></xsl:with-param>
				</xsl:call-template>
				Please select the
				<xsl:choose>
					<xsl:when test="$EditMode='DeleteBookmark'">
						bookmarks
					</xsl:when>
					<xsl:when test="$EditMode='DeleteFolder'">
						folders
					</xsl:when>
				</xsl:choose>
				you wish to delete.<br/>
				<input type="submit" name="SubmitButton" value="Delete"/>
				<img src="{$imagesURL}trans20x20.gif" width="10" height="20"/>
				<input type="submit" name="SubmitButton" value="Cancel"/>
			</form>
		</div>
	</xsl:template>

</xsl:stylesheet>
