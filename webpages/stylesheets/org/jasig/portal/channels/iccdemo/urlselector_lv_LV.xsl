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
          <xsl:when test="@grouped='false'">Pa\u0161laik tiek par\u0101d\u012Bti bez atkar\u012Bb\u0101m: 
            <input type="submit" name="groupedRendering" class="uportal-button" value="P\u0101rsl\u0113gies uz grup\u0113tu par\u0101d\u012B\u0161anu" />
          </xsl:when>

          <xsl:otherwise>Pa\u0161laik tiek par\u0101d\u012Bti, izmantojot atkar\u012Bb\u0101s (renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="P\u0101rsl\u0113gties uz parasto par\u0101d\u012B\u0161anu" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>\u0160\u012B ir starpkan\u0101lu komunik\u0101cijas demonstr\u0101cija. \u0160is kan\u0101ls darbojas kop\u0101 ar diviem citiem kan\u0101liem, lai var\u0113tu apskat\u012Bt  t\u012Bmek\u013Ca lapas.
      <br />

      \u0160is kan\u0101ls \u013Cauj lietot\u0101jam izv\u0113l\u0113ties URL, lai to apskat\u012Btu. Lietot\u0101ja izv\u0113li t\u0101l\u0101k nodod apskates otrajam kan\u0101lam. Apskates kan\u0101ls izmanto <span class="uportal-channel-code">iek\u013Caut\u0101 kadra (iframe)</span> elementu, lai att\u0113lotu v\u0113lamo t\u012Bmek\u013Ca lapu, un padod URL inform\u0101ciju tre\u0161ajam kan\u0101lam – v\u0113stures kan\u0101lam. V\u0113stures kan\u0101ls vienk\u0101r\u0161i par\u0101da 10 p\u0113d\u0113jo URL sec\u012Bbu, ar kur\u0101m ir sask\u0101ries apskates kan\u0101ls. 
      <br />

      URL izv\u0113les kan\u0101ls gaida vienu sekundi pirms hipersaites pado\u0161anas uz Cviewer, izmantojot <span class="uportal-channel-code">setRuntimeData\(\)</span> metodi. Tas noz\u012Bm\u0113, ka liel\u0101koties Cviewer r\u0101d\u012Bs veco URL un jaun\u0101 URL netiks par\u0101d\u012Bta l\u012Bdz br\u012Bdim, kam\u0113r p\u0101rl\u016Bkprogramm\u0101 neb\u016Bs nospiesta p\u0101rl\u0101d\u0113\u0161anas poga. Tom\u0113r, kad URL izv\u0113les kan\u0101ls lieto par\u0101d\u012B\u0161an\u0101s grupas, port\u0101la r\u0101d\u012B\u0161anas modulis sinhroniz\u0113 visus 3 kan\u0101lus pie <span class="uportal-channel-code">renderXML()</span> robe\u017Eas, t\u0101 dodot garantju, ka Cviewer atkal nepar\u0101d\u012Bs novecoju\u0161o URL.</p>

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

