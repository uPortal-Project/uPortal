<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="bookmark_tree_html.xsl" /> 
	
	<xsl:output method="xml" indent="no"/>
	
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="mediaPath">media/org/jasig/portal/channels/CBookmarks</xsl:param>
	
	<xsl:template match="/">
		
			<xsl:call-template name="BookmarkTree"/>
<table width="100%" cellpadding="2" cellspacing="0">
<tr><td><img src="{$mediaPath}/transparent.gif" height="8" width="8"/></td></tr>
			<tr class="uportal-background-light"><td align="left"><a href="{$baseActionURL}?command=AddBookmark"><img src="{$mediaPath}/bookmark_add.gif" border="0"/></a>
			<a href="{$baseActionURL}?command=DeleteBookmark"><img src="{$mediaPath}/bookmark_delete.gif" border="0"/></a>
      <a href="{$baseActionURL}?command=AddFolder"><img src="{$mediaPath}/folder_add.gif" border="0"/></a>
      <a href="{$baseActionURL}?command=DeleteFolder"><img src="{$mediaPath}/folder_delete.gif" border="0"/></a></td></tr>
      </table>

		
	</xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
