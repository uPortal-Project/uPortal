<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="bookmark_tree_html.xsl"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="imagesURL">media/org/jasig/portal/channels/CBookmarks/</xsl:param>
  <!-- This parameter tells the stylesheet which nodes to put radio buttons next to -->
  <xsl:param name="EditMode">none</xsl:param>
  <xsl:template match="/">
    <xsl:call-template name="BookmarkTree"/>[Add Bookmark] [View Bookmark]</xsl:template>
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
