<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences</xsl:variable>

  <xsl:template match="profile">
        <form name="form1" method="post" action="{$baseActionURL}">
          <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td colspan="4" class="uportal-channel-table-header" valign="top">Redi\u0123\u0113t profilu</td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-background-dark" valign="top">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
              </td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-channel-subtitle" valign="top">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
              </td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-label" valign="top">Profila nosaukums:</td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-channel-subtitle" valign="top">
                <input type="text" name="profileName" class="uportal-input-text" size="20" value="{name}" />
              </td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-channel-subtitle" valign="top">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
              </td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-label" valign="top">Profila apraksts:</td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-channel-subtitle" valign="top">
                <textarea name="profileDescription" class="uportal-input-text" cols="50" rows="2">
                  <xsl:value-of select="description" />
                </textarea>
              </td>
            </tr>

            <tr>
              <td colspan="4" class="uportal-channel-subtitle" valign="top">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
              </td>
            </tr>

            <xsl:apply-templates select="themestylesheets" />

            <tr>
              <td class="uportal-text-small" colspan="4">
                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
              </td>
            </tr>

            <tr>
              <td class="uportal-text-small" colspan="4">
		<input type="hidden" name="action" value="completeEdit"/>
                <input type="submit" name="submitSave" value="Save Changes" class="uportal-button" />

                <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />

                <input type="submit" name="submitCancel" value="Atcelt" class="uportal-button" />
              </td>
            </tr>
          </table>
        </form>
  </xsl:template>

  <xsl:template match="themestylesheets">
    <tr>
      <td class="uportal-label" colspan="4" valign="top">T\u0113ma:</td>
    </tr>

    <tr>
      <td class="uportal-background-dark" colspan="4" valign="top">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
      </td>
    </tr>

    <tr>
      <td colspan="4" valign="top">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="10" height="10" />
      </td>
    </tr>

    <tr>
      <td colspan="4" valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
          <tr valign="top">
            <td align="center" class="uportal-channel-subtitle">Atlas\u012Bt</td>

            <td align="center" class="uportal-channel-subtitle">Ier\u012Bces tips</td>

            <td width="100%" class="uportal-channel-subtitle">
              <div align="center">Nosaukums/Apraksts</div>
            </td>

            <td class="uportal-channel-subtitle" align="center">
              <div align="center">Piem\u0113rs</div>
            </td>
          </tr>

          <xsl:apply-templates select="current" />

          <xsl:apply-templates select="alternate" />
        </table>
      </td>
    </tr>

    <tr>
      <td class="uportal-background-med" colspan="4">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1" />
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="current">
    <tr valign="top">
      <td align="center" class="uportal-text-small" valign="middle">
        <input type="radio" name="stylesheetID" value="{id}" checked="checked" />
      </td>

      <td align="center" class="uportal-text-small">
        <img alt="interface image" src="{deviceiconuri}" width="120" height="90" />

        <br />

        <xsl:value-of select="mimetype" />
      </td>

      <td width="100%" class="uportal-channel-text" valign="top">
        <p>
          <strong>
            <xsl:value-of select="name" />
          </strong>

          <br />

          <xsl:value-of select="description" />
        </p>
      </td>

      <xsl:choose>
        <xsl:when test="sampleiconuri =''">
          <td align="center" valign="middle" class="uportal-text-small">Nav piem\u0113ra
          <br />

          Pieejams</td>
        </xsl:when>

        <xsl:when test="sampleuri = ''">
          <td align="center" class="uportal-text-small">
            <img alt="interface image" src="{sampleiconuri}" width="120" height="90" border="0" />
          </td>
        </xsl:when>

        <xsl:otherwise>
          <td align="center" class="uportal-text-small">
          <a href="{sampleuri}" target="_blank">
            <img alt="interface image" src="{sampleiconuri}" width="120" height="90" border="0" />
          </a>

          <br />

          Nospiest, lai palielin\u0101tu</td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>

  <xsl:template match="alternate">
    <tr valign="top">
      <td align="center" class="uportal-text-small" valign="middle">
        <input type="radio" name="stylesheetID" value="{id}" />
      </td>

      <td align="center" class="uportal-text-small">
        <img alt="interface image" src="{deviceiconuri}" width="120" height="90" />

        <br />

        <xsl:value-of select="mimetype" />
      </td>

      <td width="100%" class="uportal-channel-text" valign="top">
        <p>
          <strong>
            <xsl:value-of select="name" />
          </strong>

          <br />

          <xsl:value-of select="description" />
        </p>
      </td>

      <xsl:choose>
        <xsl:when test="sampleiconuri =''">
          <td align="center" valign="middle" class="uportal-text-small">Nav piem\u0113ra
          <br />

          Pieejams</td>
        </xsl:when>

        <xsl:when test="sampleuri = ''">
          <td align="center" class="uportal-text-small">
            <img alt="interface image" src="{sampleiconuri}" width="120" height="90" border="0" />
          </td>
        </xsl:when>

        <xsl:otherwise>
          <td align="center" class="uportal-text-small">
          <a href="{sampleuri}" target="_blank">
            <img alt="interface image" src="{sampleiconuri}" width="120" height="90" border="0" />
          </a>

          <br />

          Nospiest, lai palielin\u0101tu</td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>
</xsl:stylesheet>

