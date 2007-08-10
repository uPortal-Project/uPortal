<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
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
      <form action="{$baseActionURL}">
        <xsl:choose>
          <xsl:when test="@grouped='false'">Gegenwärtiges Übertragen ohne Abhängigkeiten: 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Schalten Sie zur  Gruppenübertragung" />
          </xsl:when>

          <xsl:otherwise>Gegenwärtiges Übertragen mit Benutzen von Abhängigkeiten(renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Schalten Sie zur normalen Übertragung" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>Dies ist eine Demonstration der Interchannel-Kommunikation. Der gegenwärtige Channel arbeitet in Kooperation mit zwei anderen Channels um Webseiten anzuzeigen.
      <br />

      Dieser Channel(Selektor) erlaubt es dem Benutzer eine URL zur Ansicht zu wählen. Die Wahl des Benutzers wird dann zum Ansichtschannel geführt. Der Ansichtschannel benutzt ein <span class="uportal-channel-code">iframe</span>-Element um die gewünschten Webseiten anzuzeigen, und führt dann die gegenwärtigen URL Informationen zum dritten Channel - dem History-Channel. Der History-Channel zeigt einfach die Sequenz der letzten 10 URLs, die beim Ansichtschannel angeklickt wurden. 
      <br />

      Der URL-Selektor-Channel wartet eine Sekunde zusätzlich, bevor die URL zum CViewer geführt wird in der <span class="uportal-channel-code">setRuntimeData()</span> Methode. Das bedeutet, dass die meiste Zeit, CViewer die alte URL überträgt, und die neue URL wird erst angezeigt wenn der Benutzer den 'Neu Laden'-Knopf des Browsers anklickt. Wenn jedoch der URL Selector Channel die Gruppenübertragung verwendet, synchronisiert die Portalübertragungsmaschine alle drei Channel bei der <span class="uportal-channel-code">renderXML()</span> Grenze, und somit eine Garantie zur Verfügung stellt, daß CViewer nicht mit dem überholten URL überträgt.</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          Oder tippen Sie in ein URL manuell: <br />
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

