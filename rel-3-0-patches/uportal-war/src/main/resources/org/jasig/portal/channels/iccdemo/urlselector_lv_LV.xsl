<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>

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
          <xsl:when test="@grouped='false'">Pašlaik tiek parādīti bez atkarībām: 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Pārslēgies uz grupētu parādīšanu" />
          </xsl:when>

          <xsl:otherwise>Pašlaik tiek parādīti, izmantojot atkarībās (renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Pārslēgties uz parasto parādīšanu" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>Šī ir starpkanālu komunikācijas demonstrācija. Šis kanāls darbojas kopā ar diviem citiem kanāliem, lai varētu apskatīt  tīmekļa lapas.
      <br />

      Šis kanāls ļauj lietotājam izvēlēties URL, lai to apskatītu. Lietotāja izvēli tālāk nodod apskates otrajam kanālam. Apskates kanāls izmanto <span class="uportal-channel-code">iekļautā kadra (iframe)</span> elementu, lai attēlotu vēlamo tīmekļa lapu, un padod URL informāciju trešajam kanālam – vēstures kanālam. Vēstures kanāls vienkārši parāda 10 pēdējo URL secību, ar kurām ir saskāries apskates kanāls. 
      <br />

      URL izvēles kanāls gaida vienu sekundi pirms hipersaites padošanas uz Cviewer, izmantojot <span class="uportal-channel-code">setRuntimeData\(\)</span> metodi. Tas nozīmē, ka lielākoties Cviewer rādīs veco URL un jaunā URL netiks parādīta līdz brīdim, kamēr pārlūkprogrammā nebūs nospiesta pārlādēšanas poga. Tomēr, kad URL izvēles kanāls lieto parādīšanās grupas, portāla rādīšanas modulis sinhronizē visus 3 kanālus pie <span class="uportal-channel-code">renderXML()</span> robežas, tā dodot garantju, ka Cviewer atkal neparādīs novecojušo URL.</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          Vai ierakstiet URL: <br />
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

