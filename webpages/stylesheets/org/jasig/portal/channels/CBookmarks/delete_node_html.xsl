<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:import href="bookmark_tree_html.xsl" /> 
	
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="imagesURL">media/org/jasig/portal/channels/CBookmarks/</xsl:param>
		
	<!-- This parameter tells the stylesheet which nodes to put radio buttons next to -->
	<xsl:param name="EditMode">DeleteBookmark</xsl:param>
	
	<xsl:template match="/">
		
			<form action="{$baseActionURL}?command={$EditMode}" method="post">
      <table border="0" cellpadding="6">
							<tr>
								<td class="uportal-channel-subtitle">Delete <xsl:value-of select="substring-after($EditMode,'Delete')"/>s</td>
							</tr></table>
				<xsl:call-template name="BookmarkTree">
					<xsl:with-param name="TreeMode"><xsl:value-of select="$EditMode"/></xsl:with-param>
				</xsl:call-template>
				<hr/>Please select the
				<xsl:choose>
					<xsl:when test="$EditMode='DeleteBookmark'">
						bookmarks
					</xsl:when>
					<xsl:when test="$EditMode='DeleteFolder'">
						folders
					</xsl:when>
				</xsl:choose>
				you wish to delete.<hr/>
				<input type="submit" name="SubmitButton" value="Delete" class="uportal-button"/>
				<input type="submit" name="SubmitButton" value="Cancel" class="uportal-button"/>
			</form>
		
	</xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
