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
    <p>\u0160is ir v\u0113stures kan\u0101ls - da\u013Ca no starpkan\u0101lu sakaru demonstr\u0101cijas. \u0160is kan\u0101ls par\u0101da p\u0113d\u0113j\u0101s desmit URL apskates kan\u0101l\u0101. Lai apskat\u012Btu to no jauna Cviewer kan\u0101l\u0101, nospiediet URL. Iev\u0113rojiet, ka t\u0101 tiks atjaunota Cviewer v\u0113sture.</p>

    <span class="uportal-label">URL v\u0113sture</span>: 
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

    <p>Nospie\u017Eot vienu no URL dotaj\u0101 sarakst\u0101, Chistory nodos URL Cviewer kan\u0101lam. To var izdar\u012Bt gan iek\u0161\u0113ji (caur JNDI tie\u0161i uz Cviewer kan\u0101lu), gan izmantojot uPortal URL sintaksi (uP_channelTarget) un nododot "url" parametru CURL selector kan\u0101lam t\u0101, lai selektora kan\u0101ls var\u0113tu dot sign\u0101lu Cviewer klasei. 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">Pa\u0161reiz URL tiek padotas ar\u0113ji, izmantojot uPortal_channelTarget: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>Pa\u0161reiz URL tiek padotas iek\u0161\u0113ji uz CViewer, izmantojot JNDI: 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

