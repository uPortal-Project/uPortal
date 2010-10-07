<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="locale">de_DE</xsl:param>

    <xsl:template match="iframe">
        <iframe src="{url}" height="{height}" frameborder="no" width="100%">
            <xsl:if test="name!=''">
                <xsl:attribute name="name">
                        <xsl:value-of select="name"/>
                </xsl:attribute>
            </xsl:if>
            Dieser Browser unterstützt keine Inlineframes.<br/>
            <a href="{url}" target="_blank">Klicken Sie hier um den Inhalt,</a> in einem seperaten Fanster zu sehen.</iframe>
    </xsl:template>
</xsl:stylesheet>
