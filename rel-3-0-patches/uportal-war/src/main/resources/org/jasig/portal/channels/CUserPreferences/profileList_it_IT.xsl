<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">it_IT</xsl:param>
  <xsl:param name="profileType">system</xsl:param>
  <xsl:param name="profileId">1</xsl:param>
  <xsl:param name="allowSystemProfileMap">true</xsl:param>
  <xsl:param name="allowNewProfiles">true</xsl:param>
  <xsl:param name="allowAdvancedProfileMapping">false</xsl:param>
  <!--<xsl:variable name="baseMediaURL">C:\portal\webpages\media\org\jasig\portal\channels\CUserPreferences</xsl:variable>-->
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CUserPreferences</xsl:variable>
  <xsl:template match="profiles">

        <!--Begin Master Table -->
        <table width="100%" border="0" cellspacing="0" cellpadding="4" class="uportal-background-light">
          <form name="form_profiles" method="post" action="{$baseActionURL}">
            <tr>
              <td>
                <!--Begin Profiles Table -->
                <table width="100%" border="0" cellspacing="10" cellpadding="0" class="uportal-background-light">
                  <tr>
                    <td align="left" valign="top" class="uportal-background-content">
                      <xsl:apply-templates select="system"/>
                    </td>
                    <td align="left" valign="top" class="uportal-background-content">
                      <xsl:apply-templates select="user"/>
                    </td>
                  </tr>
                  <!-- form begin -->
                  <!-- form end -->
                </table>
                <!--End Profiles Table -->
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="10" height="10" border="0"/>
                <input type="submit" name="submit" value="Cancella" class="uportal-button"/>
                <input type="hidden" name="userPreferencesAction" value="managePreferences"/>
              </td>
            </tr>
          </form>
        </table>
        <!--End Master Table -->
<!--      </body>
    </html>-->
  </xsl:template>
  <xsl:template match="system">
    <!--System Header Table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td nowrap="nowrap" valign="bottom" align="left">
          <span class="uportal-channel-table-header">Profili di sistema</span></td>
          <td nowrap="nowrap" valign="bottom" align="left">
          <strong>
            <a href="{$baseActionURL}?action=expandAll&amp;profileType=system">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Mostra dettagli" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Espandi tutto</span>
            </a>
          </strong></td>
<td nowrap="nowrap" valign="bottom" align="left" width="100%">
          <strong>
            <a href="{$baseActionURL}?action=condenseAll&amp;profileType=system">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Nascondi dettagli" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Contrai tutto</span>
            </a>
          </strong>
        </td>
      </tr>
      <tr>
        <td colspan="3">
          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
            <tr>
              <td>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="1" height="2"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <!--End System Header Table -->
    <xsl:apply-templates select="profile" mode="system"/>
  </xsl:template>
  <xsl:template match="profile" mode="system">
    <!--Begin Sys. Profile Instance Table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <xsl:choose>
        <xsl:when test="$profileId = @id and $profileType ='system'">
          <xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="class">uportal-background-content</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="@view = 'expanded'">
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="4" align="left" valign="top" class="uportal-background-content">
              <strong>
                <a href="{$baseActionURL}?action=changeView&amp;view=condensed&amp;profileId={@id}&amp;profileType=system">
                  <img alt="Nascondi dettagli" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
                </a>
              </strong>
            </td>
            <td class="uportal-channel-text">Nome:</td>
            <td width="100%">
              <strong>
                <xsl:value-of select="@name"/>
              </strong>
              <xsl:if test="$profileId = @id and $profileType ='system'">
                <br/>[Il profilo è associato al browser corrente]</xsl:if>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Descrizione:</td>
            <td width="100%">
              <xsl:value-of select="description"/>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Azioni:</td>
            <td width="100%" nowrap="nowrap">
              <p>
                <a href="{$baseActionURL}?userPreferencesAction=managePreferences&amp;profileId={@id}&amp;profileType=system">
                  <img alt="Modifica le preferenze di questo profilo" src="{$baseMediaURL}/profile_user_pref.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Modifica le preferenze di questo profilo</a>
                <br/>
                <xsl:if test="$allowSystemProfileMap = 'true' and not($profileId = @id and $profileType ='system')">
                  <a href="{$baseActionURL}?action=map&amp;profileId={@id}&amp;profileType=system">
                    <img alt="Associa questo profilo al corrente browser" src="{$baseMediaURL}/profile_map.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Associa questo profilo al corrente browser</a>
                  <br/>
                </xsl:if>
                <xsl:if test="/profiles/user">
                  <a href="{$baseActionURL}?action=copy&amp;profileId={@id}&amp;profileType=system">
                    <img alt="Copia questo profilo nei miei profili personali" src="{$baseMediaURL}/profile_duplicate.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Copia questo profilo nei miei profili personali</a>
                </xsl:if>
              </p>
            </td>
          </tr>
        </xsl:when>
        <xsl:otherwise>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="2" class="uportal-background-content">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=system">
                <img alt="Mostra dettagli" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              </a>
            </td>
            <td width="100%">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=system">
                <strong>
                  <xsl:value-of select="@name"/>
                </strong>
              </a>
              <xsl:if test="$profileId = @id and $profileType ='system'">
                <br/>[Il profilo è associato al browser corrente]</xsl:if>
            </td>
          </tr>
        </xsl:otherwise>
      </xsl:choose>
      <tr align="left" valign="top" class="uportal-background-content">
        <td colspan="2">
          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
            <tr>
              <td>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="1" height="1"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <!--End Sys. Profile Instance Table -->
  </xsl:template>
  <xsl:template match="user">
    <!--Begin Personal Profile Header Table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="2">
      <tr>
        <td nowrap="nowrap" valign="bottom" align="left">
          <span class="uportal-channel-table-header">Profili personali</span></td>
          <td nowrap="nowrap" valign="bottom" align="left"><strong>
            <a href="{$baseActionURL}?action=expandAll&amp;profileType=user">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Mostra dettagli" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Espandi tutto</span>
            </a>
          </strong></td>
          <td nowrap="nowrap" valign="bottom" align="left" width="100%"><strong>
            <a href="{$baseActionURL}?action=condenseAll&amp;profileType=user">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Nascondi dettagli" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Contrai tutto</span>
            </a>
          </strong>
        </td>
      </tr>
      <tr>
        <td colspan="3">
          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
            <tr>
              <td>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="1" height="2"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <!--End Personal Profile Header Table -->
    <xsl:apply-templates select="profile" mode="user"/>
    <xsl:if test="$allowNewProfiles = 'true'">
      <xsl:call-template name="newProfile"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="profile" mode="user">
    <!--Begin Personal Profile Instance Table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <xsl:choose>
        <xsl:when test="$profileId = @id and $profileType ='user'">
          <xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="class">uportal-background-content</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="@view = 'expanded'">
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="4" align="left" valign="top" class="uportal-background-content">
              <strong>
                <a href="{$baseActionURL}?action=changeView&amp;view=condensed&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Nascondi dettagli" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
                </a>
              </strong>
            </td>
            <td class="uportal-channel-text">Nome:</td>
            <td width="100%">
              <strong>
                <xsl:value-of select="@name"/>
              </strong>
              <xsl:if test="$profileId = @id and $profileType ='user'">
                <br/>[Il profilo è associato al browser corrente]</xsl:if>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Descrizione:</td>
            <td width="100%">
              <xsl:value-of select="description"/>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Azioni:</td>
            <td width="100%" nowrap="nowrap">
              <p>
                <a href="{$baseActionURL}?userPreferencesAction=managePreferences&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Modifica le preferenze di questo profilo" src="{$baseMediaURL}/profile_user_pref.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Modifica le preferenze di questo profilo</a>
                <br/>
                <a href="{$baseActionURL}?action=edit&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Modifica questo profilo personale" src="{$baseMediaURL}/profile_edit.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Modifica questo profilo personale</a>
                <br/>
                <xsl:if test="not($profileId = @id and $profileType ='user')">
                  <a href="{$baseActionURL}?action=map&amp;profileId={@id}&amp;profileType=user">
                    <img alt="Associa questo profilo al corrente browser" src="{$baseMediaURL}/profile_map.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Associa questo profilo al corrente browser</a>
                  <br/>
                </xsl:if>
                <a href="{$baseActionURL}?action=copy&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Fai una copia di questo profilo personale" src="{$baseMediaURL}/profile_duplicate.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Fai una copia di questo profilo personale</a>
                <br/>
                <xsl:if test="$allowAdvancedProfileMapping = 'true'">
                  <a href="{$baseActionURL}?action=map_adv&amp;profileId={@id}&amp;profileType=user">
                    <img alt="Modifica avanzata dei profili" src="{$baseMediaURL}/profile_map_adv.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Modifica avanzata dei profili</a>
                  <br/>
                </xsl:if>
                <a href="{$baseActionURL}?action=delete&amp;profileId={@id}&amp;profileType=user" onClick="return confirm('Sei sicuro di voler eliminare questo profilo??')">
                  <img alt="Elimina il profilo" src="{$baseMediaURL}/profile_delete.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Elimina il profilo</a>
              </p>
            </td>
          </tr>
        </xsl:when>
        <xsl:otherwise>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="2" class="uportal-background-content">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=user">
                <img alt="Mostra dettagli" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              </a>
            </td>
            <td width="100%">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=user">
                <strong>
                  <xsl:value-of select="@name"/>
                </strong>
              </a>
              <xsl:if test="$profileId = @id and $profileType ='user'">
                <br/>[Il profilo è associato al browser corrente]</xsl:if>
            </td>
          </tr>
        </xsl:otherwise>
      </xsl:choose>
      <tr align="left" valign="top" class="uportal-background-content">
        <td colspan="2">
          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
            <tr>
              <td>
                <img alt="" src="{$baseMediaURL}/transparent.gif" width="1" height="1"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <!--End Personal Profile Instance Table -->
  </xsl:template>
  <xsl:template name="newProfile">
    <!--Begin New Personal Profile Table -->
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <tr class="uportal-background-content">
        <td class="uportal-background-light">
          <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
        </td>
      </tr>
      <tr class="uportal-background-content">
        <td class="uportal-channel-text">
          <a href="{$baseActionURL}?action=newProfile">
            <img alt="Crea un nuovo profilo personale" src="{$baseMediaURL}/profile_new.gif" width="16" height="16" border="0"/>
            <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Crea un nuovo profilo personale</a>
        </td>
      </tr>
    </table>
    <!--End New Personal Profile Table -->
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
