<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="iframe" >
  Denna webbläsare stödjer inte inline frames.<br/> 
  <a href="{url}" target="_blank">Klicka här för att se innehållet</a> i ett separat fönster.
</xsl:template>

</xsl:stylesheet>
