<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal kan\u0101liem ir pieeja lietot\u0101ja atrib\u016Btiem
      caur <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> objektu.
      Atrib\u016Btu nosaukumi tiek defin\u0113ti 
      <a href="http://www.educause.edu/eduperson/">eduPerson objektu klas\u0113</a> versija 1.0.
    </p>
    <p>
      uPortal izstr\u0101d\u0101t\u0101jiem ir j\u0101nor\u0101da šo standarta atrib\u016Btu
      lok\u0101lajiem v\u0101rdiem to personu direktorij\u0101 vai datu b\u0101z\u0113. Atbilst\u012Bbas atrodas
      datn\u0113 <span class="uportal-channel-code">properties/PersonDirs.xml</span>.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Atrib\u016Bta nosaukums</th>
        <th>Atrib\u016Bta v\u0113rt\u012Bba</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Pieejamie atrib\u016Bti:</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="defined"/>
      <tr class="uportal-background-light">
        <td colspan="2">Nepieejamie atrib\u016Bti:</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="undefined"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute" mode="defined">
    <xsl:if test="value">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <xsl:choose>
        <xsl:when test="name='jpegPhoto'">
            <td><img src="{$downloadWorkerURL}?attribute={name}" /></td>
        </xsl:when>
        <xsl:otherwise>
            <td><xsl:value-of select="value"/></td>
        </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="attribute" mode="undefined">
    <xsl:if test="not(value)">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <td>[Nav pieejams]</td>
      </tr>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet> 
