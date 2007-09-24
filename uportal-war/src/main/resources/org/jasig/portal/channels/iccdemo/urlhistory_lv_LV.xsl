<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
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
    <p>Šis ir vēstures kanāls - daļa no starpkanālu sakaru demonstrācijas. Šis kanāls parāda pēdējās desmit URL apskates kanālā. Lai apskatītu to no jauna Cviewer kanālā, nospiediet URL. Ievērojiet, ka tā tiks atjaunota Cviewer vēsture.</p>

    <span class="uportal-label">URL vēsture</span>: 
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

    <p>Nospiežot vienu no URL dotajā sarakstā, Chistory nodos URL Cviewer kanālam. To var izdarīt gan iekšēji (caur JNDI tieši uz Cviewer kanālu), gan izmantojot uPortal URL sintaksi (uP_channelTarget) un nododot "url" parametru CURL selector kanālam tā, lai selektora kanāls varētu dot signālu Cviewer klasei. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">Pašreiz URL tiek padotas arēji, izmantojot uPortal_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>Pašreiz URL tiek padotas iekšēji uz CViewer, izmantojot JNDI: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

