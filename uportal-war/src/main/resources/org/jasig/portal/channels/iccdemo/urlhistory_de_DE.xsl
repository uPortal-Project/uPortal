<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
  <xsl:param name="locale">de_DE</xsl:param>

  <xsl:template match="urlselector">

    <xsl:for-each select="warning">
      <p>
        <span class="uportal-channel-warning">
          <xsl:apply-templates />
        </span>
      </p>
    </xsl:for-each>

    <p>
    <p>Dies ist ein History-Channel, welcher Teil der Interchannel-Kommunikations Demonstration ist. Dieser Channel zeigt die letzten 10 URLs die im Viewerchannel angesehen wurden. Klicken Sie auf eine URL, um es sich erneut im CViewer-Channel anzusehen. Beachten Sie dabei, dass der CViewer die History erneuert.</p>

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

    <p>Das Klicken einer URL in der oben genannten Liste veranlaßt CHistory das URL zum CViewer-Channel zu führen.  Ein Weg dieses zu machen ist intern (durch JNDI Kontexte direkt zum CViewer-Channel).  Ein anderer ist es, die URL Syntax des uPortals zu benutzen (uP_channelTarget) und ein "url"-Parameter zum CURLSelector hinzuführen, so dass der Wählerchannel CViewer signalieren kann. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">URLs z.Z., außen führend mit uP_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>URLs zu CViewer mit JNDI z.Z., intern führend: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

