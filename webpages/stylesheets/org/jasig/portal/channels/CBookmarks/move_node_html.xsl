<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="bookmark_tree_html.xsl" /> 
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:param name="baseActionURL">default</xsl:param>
	
	<xsl:param name="imagesURL">/stylesheets/org/jasig/portal/channels/CBookmarks/</xsl:param>
	
	<!-- This parameter tells the stylesheet which nodes to put radio buttons next to -->
	<xsl:param name="EditMode">none</xsl:param>
	
	<xsl:template match="/">
		<xsl:call-template name="BookmarkTree"/>
		[Add Bookmark] [View Bookmark]	
	</xsl:template>

</xsl:stylesheet>
