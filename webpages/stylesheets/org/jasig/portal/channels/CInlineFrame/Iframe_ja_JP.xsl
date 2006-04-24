<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="locale">ja_JP</xsl:param>

<xsl:template match="iframe" >
  <iframe src="{url}" height="{height}" frameborder="0" width="100%">ダミー文字列</iframe>
</xsl:template>

</xsl:stylesheet>
