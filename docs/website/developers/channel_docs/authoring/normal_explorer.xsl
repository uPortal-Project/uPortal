<?xml version="1.0" encoding="utf-8"?>
<!-- normal_explorer.xsl, part of the HelloWorld example channel -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" />
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="name_prev">world</xsl:param>

  <xsl:template match="/">
    <p />
    <a name="helloworld" />
    <xsl:apply-templates />
    <form action="{$baseActionURL}#helloworld" method="post">
      Enter your name:
      <input type="text" name="name" size="15"
            class="uportal-input-text"
            value="{$name_prev}" />
      <input type="submit" name="submit" value="submit"
            class="uportal-button" />
      <input type="submit" name="clear" value="clear"
            class="uportal-button" />
    </form>
    <p>This is the <b>explorer</b> stylesheet.</p>
  </xsl:template>

  <xsl:template match="name">
    <p>Hello <xsl:value-of select="." />!</p>
  </xsl:template>
</xsl:stylesheet>
