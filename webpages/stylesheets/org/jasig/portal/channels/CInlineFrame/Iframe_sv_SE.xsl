<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="locale">sv_SE</xsl:param>
    
    <xsl:template match="iframe">
        <iframe src="{url}" height="{height}" frameborder="0" width="100%">Denna webläsare stödjer inte inline frames.<br/>
            <a href="{url}" target="_blank">Klicka här för att öppna sidan</a> i ett eget fönster.</iframe>
    </xsl:template>
</xsl:stylesheet>
