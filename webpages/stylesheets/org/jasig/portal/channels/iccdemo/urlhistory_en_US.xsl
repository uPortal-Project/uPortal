<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>

  <xsl:template match="urlselector">

    <xsl:for-each select="warning">
      <p>
        <span class="uportal-channel-warning">
          <xsl:apply-templates />
        </span>
      </p>
    </xsl:for-each>

    <p>
    <p>This is a history channel, part of the inter-channel communication demonstration. This channel shows the last 10 URLs that have been viewed in the viewer channel. Click on a URL to view it again in the CViewer channel. Note that this will, in turn, cause CViewer to update the history.</p>

    <span class="uportal-label">URL history</span>: 
    <xsl:for-each select="url">
    <br />

    <xsl:value-of select="position()" />.
    <a class="uportal-channel-code">
      <xsl:attribute name="href">
        <xsl:choose>
          <xsl:when test="$passExternally='true'">
            <xsl:value-of select="$baseActionURL" />?uP_channelTarget=<xsl:value-of select="$CURLSelectId" />&amp;url=<xsl:value-of select="." />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$baseActionURL" />?urlN=<xsl:value-of select="position()" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <xsl:value-of select="." />
    </a>
    </xsl:for-each>
    </p>

    <p>Hitting one of the URLs in the above list will cause CHistory to pass the URL to the CViewer channel.  One way of doing this is internally (through JNDI contexts, directly to the CViewer channel).  Another is by using uPortal's URL syntax (uP_channelTarget) and passing a "url" parameter to the CURLSelector, so that the selector channel could then signal CViewer. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">Currently passing URLs externally, using uP_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>Currently passing URLs internally to CViewer, using JNDI: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

