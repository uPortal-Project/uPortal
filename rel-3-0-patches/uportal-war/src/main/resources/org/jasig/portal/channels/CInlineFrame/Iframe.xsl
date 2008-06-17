<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="iframe" >
    <iframe src="{url}" height="{height}" frameborder="0" width="100%">
        This browser does not support inline frames.<br/> 
        <a href="{url}" target="_blank">Click here to view content</a> in a separate window.
    </iframe>
</xsl:template>

</xsl:stylesheet>
