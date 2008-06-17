<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
  <xsl:param name="locale">it_IT</xsl:param>

  <xsl:template match="urlselector">

    <xsl:for-each select="warning">
      <p>
        <span class="uportal-channel-warning">
          <xsl:apply-templates />
        </span>
      </p>
    </xsl:for-each>

    <p>
    <p>La cronologia di questo canale è contenuta in un "inter-channel" per dimostrare la comunicazione tra canali. Questo canale mantiene le ultime 10 URL che sono state visualizzate nel canale di visualizzazione. Seleziona una URL per rivisualizzarne il contenuto nel canale CViewer. Nota che questa azione fa in modo che il canale CViewer aggiorni anche la cronologia.</p>

    <span class="uportal-label">Cronologia</span>: 
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

    <p>La selezione una delle URL nella lista sottostante attiva il CHistory che trasmette l'URL al canale CViewer.  Un modo per fare questo internamente (attraverso i JNDI contexts, direttamente sul canale CViewer). Un altro modo è quello di utilizzare la sintassi URL di Portal \(uP_channelTarget\) passando il parametro "url" al CURLSelector, così il selettore dei canali può segnalarlo al CViewer. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">In questo caso l'URL è trasmessa esternamente, utilizzando uP_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>In questo caso l'URL è trasmessa iternamente al CViewer, utilizzando JNDI: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

