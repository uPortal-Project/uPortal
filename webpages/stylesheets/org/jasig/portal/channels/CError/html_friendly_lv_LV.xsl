<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <img src="{$baseMediaURL}wrenchworks.gif" width="112" height="119"/>
        </td>
        <td>
          <img src="{$baseMediaURL}transparent.gif" width="16" height="16"/>
        </td>
        <td class="uportal-channel-subtitle" width="100%">K\u013C\u016Bda:<br/><span class="uportal-channel-error">
              <xsl:choose>
                <xsl:when test="@code='4'">Kan\u0101lam iest\u0101jies taimauts</xsl:when>
              <xsl:when test="@code='1'">Kan\u0101ls nesp\u0113j par\u0101d\u012Bt datus</xsl:when>
              <xsl:when test="@code='2'">Kan\u0101ls nesp\u0113j s\u0101kt darboties</xsl:when>
              <xsl:when test="@code='3'">Kan\u0101ls nesp\u0113j pie\u0146emt datus</xsl:when>
              <xsl:when test="@code='0'">Kan\u0101l\u0101 ir visp\u0101r\u0113ja k\u013C\u016Bda</xsl:when>
              <xsl:when test="@code='5'">Kan\u0101ls nesp\u0113j pie\u0146emt PSP (Person\u0101lie sakaru pakalpojumi)</xsl:when>
              <xsl:when test="@code='6'">Jums nav piek\u013Cuves ties\u012Bbu šim kan\u0101lam</xsl:when>
              <xsl:when test="@code='7'">Kan\u0101ls nav pieejams</xsl:when>
              <xsl:when test="@code='-1'">Kan\u0101l\u0101 ir visp\u0101r\u0113j\u0101 uPortal k\u013C\u016Bda</xsl:when></xsl:choose></span>
          <br/>
          <br/>

          <xsl:if test="$allowRefresh='true'">
            <a href="{$baseActionURL}?action=retry">
              <img src="{$baseMediaURL}error_refresh.gif" border="0" width="16" height="16" alt="Refresh the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">Atjaunot kan\u0101lu</span>
            </a>

            <br/>
          </xsl:if>

          <xsl:if test="$allowReinstantiation='true'">
            <a href="{$baseActionURL}?action=restart">
              <img src="{$baseMediaURL}error_reboot.gif" border="0" width="16" height="16" alt="Reboot the channel"/>
              <img src="{$baseMediaURL}transparent.gif" border="0" width="4" height="4"/>
              <span class="uportal-label">P\u0101rstart\u0113t kan\u0101lu</span>
            </a>
          </xsl:if>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
