<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
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
      <form action="{$baseActionURL}">
        <xsl:choose>
          <xsl:when test="@grouped='false'">Rendering corrente senza dipendenze: 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Passa alla modalità di rendering a gruppi" />
          </xsl:when>

          <xsl:otherwise>Rendering corrente con dipendenze(renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Passa alla modalità di rendering completo" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>Questo è un esempio di funzionamento dell'inter-channel communications. Questo canale lavora in cooperazione con altri due canali per la visuaalizzazione di pagine Web.
      <br />

      Questo canale (selector) permette agli utenti di selezionare una URL da visualizzare. La scelta dell'utente viene passata al (secondo) canale di visualizzazione (Viewer channel). Il canale di visualizzazione utilizza un elemento <span class="uportal-channel-code">iframe</span> per mostrare la pagina desiderata, e trasmette le informazioni sull'URL al terzo canale - il canale della Cronologia (History Channel). Il canale della cronologia mostra semplicemente la sequenza delle ultime 10 URL mostrate nel canale di visualizzazione. 
      <br />

      Il canale di selezione delle URL attende un secondo, prima di passare l'URL al CViewer nel <span class="uportal-channel-code">setRuntimeData()</span> metodo. Questa attesa ha il seguente scopo: la maggior parte del tempo, il CViewer mostra la vecchia URL, e la nuova URL non viene visualizzata finchè l'utente non selezione il bottone di aggiornamento del contenuto del browser. Comunque, quando il canale di selezione della URL utilizza il rendering a gruppi, il motore di rendering sincronizza tutti e tre i canali nel <span class="uportal-channel-code">renderXML()</span>, questo garantisce che il CViewer non mostrerà la vecchia URL.</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          O inserisci l'URL manualmente: <br />
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

