<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/error/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table border="0" width="100%" cellspacing="0" cellpadding="4">
      <tr>
        <td colspan="2" nowrap="nowrap" align="center" class="uportal-background-med">
          <p class="uportal-channel-title">Kļūdas paziņojums</p>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Kanāla ID:</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:value-of select="channel/id"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Paziņojums:</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(message) or message = ''">Paziņojums nav pieejams</xsl:if>
          <xsl:value-of select="message"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Kļūdas tips:</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:choose>
            <xsl:when test="@code='4'">Kanālam iestājies taimauts (kods 4)</xsl:when>
            <xsl:when test="@code='1'">Kanāls nespēj parādīt datus (kods 1)</xsl:when>
            <xsl:when test="@code='2'">Kanāls nespēj sākt darboties (kods 2)</xsl:when>
            <xsl:when test="@code='3'">Kanāls nespēj pieņemt darbības datus (kods 3)</xsl:when>
            <xsl:when test="@code='0'">Vispārējā kļūda (kods 0)</xsl:when>
            <xsl:when test="@code='5'">Kanāls nespēj pieņemt PSP (Personālie sakaru pakalpojumi) (kods 5)</xsl:when>
            <xsl:when test="@code='6'">Lietotājam nav piekļuves tiesību (kods 6)</xsl:when>
            <xsl:when test="@code='7'">Kanāls nav pieejams (kods 7)</xsl:when>
            <xsl:when test="@code='-1'">uPortal kļūda (kods -1)</xsl:when>
          </xsl:choose>
        </td>
      </tr>
      <xsl:apply-templates select="throwable"/>
      <tr>
        <td valign="top" align="right" class="uportal-background-med">
          <xsl:if test="$allowRefresh='true'">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="string($baseActionURL)"/>?action=retry</xsl:attribute>
              <img border="0" width="16" height="16" alt="Izmēģināt kanālu no jauna">
                <xsl:attribute name="src">
                  <xsl:value-of select="string($baseMediaURL)"/>error_refresh.gif</xsl:attribute>
              </img>
            </a>
            <img alt="" border="0" width="10" height="10">
              <xsl:attribute name="src">
                <xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
            </img>
          </xsl:if>
          <xsl:if test="$allowReinstantiation='true'">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="string($baseActionURL)"/>?action=restart</xsl:attribute>
              <img border="0" width="16" height="16" alt="Pārstartēt kanālu">
                <xsl:attribute name="src">
                  <xsl:value-of select="string($baseMediaURL)"/>error_reboot.gif</xsl:attribute>
              </img>
            </a>
            <img alt="" border="0" width="10" height="10">
              <xsl:attribute name="src">
                <xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
            </img>
          </xsl:if>
          <xsl:if test="throwable">
            <xsl:choose>
              <xsl:when test="$showStackTrace='true' and */stack">
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="string($baseActionURL)"/>?action=toggle_stack_trace</xsl:attribute>
                  <img border="0" width="16" height="16" alt="Hide stack trace">
                    <xsl:attribute name="src">
                      <xsl:value-of select="string($baseMediaURL)"/>error_hide_trace.gif</xsl:attribute>
                  </img>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="string($baseActionURL)"/>?action=toggle_stack_trace</xsl:attribute>
                  <img border="0" width="16" height="16" alt="Parādīt steka trasi">
                    <xsl:attribute name="src">
                      <xsl:value-of select="string($baseMediaURL)"/>error_show_trace.gif</xsl:attribute>
                  </img>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </td>
        <td valign="middle" align="center" class="uportal-background-med"/>
      </tr>
      <tr>
        <td/>
      </tr>
    </table>

    <xsl:if test="$showStackTrace='true'">
      <xsl:call-template name="stackTrace"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="throwable">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
        <p class="uportal-channel-error">Problēmas tips:</p>
      </td>
      <td width="100%" valign="top" align="left" class="uportal-channel-error">
        <xsl:choose>
          <xsl:when test="@renderedAs='java.lang.Throwable'">Kanāls neparādās</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.InternalTimeoutException'">Iekšējais taimauts</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.AuthorizationException'">Autorizācijas problēma</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.MissingResourceException'">Resursa trūkums</xsl:when>
        </xsl:choose>
      </td>
    </tr>
    <xsl:if test="@renderedAs='org.jasig.portal.InternalTimeoutException'">
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Taimauta limits</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(timeout/@value)">Taimauta limits nav pieejams</xsl:if>
          <xsl:value-of select="timeout/@value"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="@renderedAs='org.jasig.portal.MissingResourceException'">
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Resursa apraksts</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(resource/description) or resource/description = ''">Resursa apraksts nav pieejams</xsl:if>
          <xsl:value-of select="resource/description"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">Resursa URI</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(resource/uri) or resource/uri = ''">Resursa URI nav pieejams</xsl:if>
          <xsl:value-of select="resource/uri"/>
        </td>
      </tr>
    </xsl:if>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
        <p class="uportal-channel-error">Kļūdas paziņojums</p>
      </td>
      <td width="100%" valign="top" align="left" class="uportal-channel-error">
        <xsl:if test="not(message) or message = ''">Kļūdas paziņojums nav pieejams</xsl:if>
        <xsl:value-of select="message"/>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template name="stackTrace">
    <br/>
    <table border="0" width="100%" cellspacing="0" cellpadding="4">
      <tr>
        <td valign="top" align="center" class="uportal-background-med">
          <p class="uportal-channel-title">Steka trase</p>
        </td>
      </tr>
      <tr>
        <td valign="top" align="left" class="uportal-channel-code">
          <span class="uportal-channel-code">
            <pre><xsl:value-of select="throwable/stack"/></pre>
          </span>
        </td>
      </tr>
      <tr>
        <td valign="top" align="left" class="uportal-background-med">
          <img border="0" src="transparent.gif" width="1" height="1"/>
        </td>
      </tr>
    </table>
  </xsl:template>
  
</xsl:stylesheet>
