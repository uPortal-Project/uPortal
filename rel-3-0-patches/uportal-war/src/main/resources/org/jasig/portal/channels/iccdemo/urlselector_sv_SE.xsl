<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
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
      <form action="{$baseActionURL}">
        <xsl:choose>
          <xsl:when test="@grouped='false'">Renderar utan gruppering 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Byt till grupperad rendering" />
          </xsl:when>

          <xsl:otherwise>Renderar med gruppering (renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Byt till vanlig rendering" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>Detta är en demonstration av kommunikation mellan kanaler. Denna kanal samarbetar med två andra kanaler för att visa websidor.
      <br />

      Denna kanal (URL-väljaren) låter användaren välja en URL att titta på. Valet skickas till vy-kanalen, som använder en <span class="uportal-channel-code">iframe</span> för att visa web-sidan, och skickar informationen om vilken URL som visas till den tredje kanalen, historik-kanalen, som visar de senaste 10 visade URLarna. 
      <br />

      URL-väljarkanalen väntar en extra sekund, med avsikt, innan URLen skickas till vy-kanalen genom <span class="uportal-channel-code">setRuntimeData()</span>-metoden. Det betyder att vy-kanalen för det mesta visar den gamla URLen, och att den nya URLen inte visas förrän användaren laddar om sidan. När URL-väljaren använder grupperad rendering kommer kanalerna synkroniseras så att samtliga grupperade kanalers <span class="uportal-channel-code">setRuntimeData\(\)</span> är klara innan <span class="uportal-channel-code">renderXML()</span> anropas. På så sätt garanteras att vy-kanalen inte anropas med den gamla URLen.</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          Eller skriv in en URL: <br />
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

