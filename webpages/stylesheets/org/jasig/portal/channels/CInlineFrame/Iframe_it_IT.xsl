<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="locale">it_IT</xsl:param>

<xsl:template match="iframe" >
  <iframe src="{url}" height="{height}" frameborder="0" width="100%">dummyText</iframe>
</xsl:template>

</xsl:stylesheet>
