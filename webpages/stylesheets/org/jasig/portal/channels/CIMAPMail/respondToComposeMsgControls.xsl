<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>
<xsl:include href="lookAndFeel.xsl"/>

<xsl:template match="respondToComposeMsgControls">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
</xsl:template>
</xsl:stylesheet>
