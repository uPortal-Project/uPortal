<?xml version='1.0'?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="locale">de_DE</xsl:param>

    <xsl:template match="iframe">
        <iframe src="{url}" height="{height}" frameborder="no" width="100%">Dieser Browser unterstützt keine Inlineframes.<br/>
            <a href="{url}" target="_blank">Klicken Sie hier um den Inhalt,</a> in einem seperaten Fanster zu sehen.</iframe>
    </xsl:template>
</xsl:stylesheet>
