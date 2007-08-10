<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">



<xsl:template match="iframe" >
<xsl:for-each select="warning">
 <p>
 <span class="uportal-channel-warning">
  <xsl:apply-templates/>
</span>
</p>
</xsl:for-each>
  Pašreizējais URL: <xsl:value-of select="url"/><br/>
  <iframe src="{url}" height="{height}" frameborder="no" width="100%">dummyText</iframe>
</xsl:template>

</xsl:stylesheet>
