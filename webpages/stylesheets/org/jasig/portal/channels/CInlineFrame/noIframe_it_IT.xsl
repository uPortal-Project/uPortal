<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="locale">it_IT</xsl:param>

<xsl:template match="iframe" >
  Questo browser non supporta gli inline frames.<br/> 
  <a href="{url}" target="_blank">Seleziona qui per vedere il contenuto</a> in una nuova finestra.
</xsl:template>

</xsl:stylesheet>
