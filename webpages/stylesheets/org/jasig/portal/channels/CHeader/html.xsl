<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="guest">false</xsl:param>
  <xsl:template match="header">
    <xsl:if test="$guest != 'true'">
      <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=activeTab&amp;activeTab=1" class="uportal-navigation-category">Home </a> | <xsl:if test="publish-chanid">
        <a href="{$baseActionURL}?uP_root={publish-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none" class="uportal-navigation-category"> Publish </a> | </xsl:if>
      <a href="{$baseActionURL}?uP_root={subscribe-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none" class="uportal-navigation-category"> Subscribe </a> | <a href="{$baseActionURL}?uP_root={preferences-chanid}&amp;uP_sparam=activeTab&amp;activeTab=none" class="uportal-navigation-category"> Preferences </a> | <a href="logout.jsp" class="uportal-navigation-category"> Logout </a>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/><MapperInfo  srcSchemaPath="" srcSchemaRoot="" destSchemaPath="" destSchemaRoot="" />
</metaInformation>
-->
