<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="iframe" >
  Dieser Browser unterstützt keine Inline-Frames.<br/> 
  <a href="{url}" target="_blank">Klicken Sie hier, um den Inhalt</a>in einem anderen Fenster anzusehen.
</xsl:template>

</xsl:stylesheet>
