<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
  <xsl:param name="locale">sv_SE</xsl:param>

  <xsl:template match="urlselector">

    <xsl:for-each select="warning">
      <p>
        <span class="uportal-channel-warning">
          <xsl:apply-templates />
        </span>
      </p>
    </xsl:for-each>

    <p>
    <p>Det här är en historik-kanal, en del av demonstrationen av kanal-till-kanal-kommunikation. Den visar de senaste 10 länkarna som visats i vy-kanalen. Klicka på en länk för att visa den igen i vy-kanalen. Notera att detta kommer återigen uppdatera historiken över länkar.</p>

    <span class="uportal-label">URL historik</span>: 
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

    <p>Genom att välja en av länkarna ovan,  kommer historik-kanalen skicka URLen till vy-kanalen. Ett sätt att göra detta är internt (via JNDI-context direkt till vy-kanalen) eller genom URL-parameter (uP_channelTarget) till URL-väljaren som i sin tur signalerar vy-kanalen. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">Skickar URL externt via uP_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>Skickar URL internt via JNDI: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

