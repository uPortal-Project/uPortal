<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal kanāliem ir pieeja lietotāja atribūtiem
      caur <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> objektu.
      Atribūtu nosaukumi tiek definēti 
      <a href="http://www.educause.edu/eduperson/">eduPerson objektu klasē</a> versija 1.0.
    </p>
    <p>
      uPortal izstrādātājiem ir jānorāda šo standarta atribūtu
      lokālajiem vārdiem to personu direktorijā vai datu bāzē. Atbilstības atrodas
      datnē <span class="uportal-channel-code">properties/PersonDirs.xml</span>.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Atribūta nosaukums</th>
        <th>Atribūta vērtība</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Pieejamie atribūti:</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="defined"/>
      <tr class="uportal-background-light">
        <td colspan="2">Nepieejamie atribūti:</td>
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
