<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="bookmark_tree_html.xsl" /> 
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">/stylesheets/org/jasig/portal/CBookmarks/</xsl:param>
	
	<xsl:template match="/">
		<div>
			<xsl:call-template name="BookmarkTree"/>
			<a href="{$baseActionURL}?command=AddBookmark">[Add Bookmark]</a>
			<img src="{$imagesURL}trans20x20.gif" width="10" height="20"/>
			<a href="{$baseActionURL}?command=AddFolder">[Add Folder]</a>
			<br/>
			<a href="{$baseActionURL}?command=DeleteBookmark">[Delete Bookmark]</a>
			<img src="{$imagesURL}trans20x20.gif" width="10" height="20"/>
			<a href="{$baseActionURL}?command=DeleteFolder">[Delete Folder]</a>
		</div>
	</xsl:template>

</xsl:stylesheet>
