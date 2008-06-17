<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">lv_LV</xsl:param>
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
                <input type="submit" name="submit" value="Atcelt" class="uportal-button"/>
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
          <span class="uportal-channel-table-header">Sistēmas profili</span></td>
          <td nowrap="nowrap" valign="bottom" align="left">
          <strong>
            <a href="{$baseActionURL}?action=expandAll&amp;profileType=system">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Parādīt detaļas" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Rādīt detaļas visiem</span>
            </a>
          </strong></td>
<td nowrap="nowrap" valign="bottom" align="left" width="100%">
          <strong>
            <a href="{$baseActionURL}?action=condenseAll&amp;profileType=system">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Paslēpt detaļas" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Nerādīt detaļas nevienam</span>
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
                  <img alt="Paslēpt detaļas" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
                </a>
              </strong>
            </td>
            <td class="uportal-channel-text">Nosaukums:</td>
            <td width="100%">
              <strong>
                <xsl:value-of select="@name"/>
              </strong>
              <xsl:if test="$profileId = @id and $profileType ='system'">
                <br/>[Šis profils ir piekārtots pašreizējai pārlūkprogrammai]</xsl:if>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Apraksts:</td>
            <td width="100%">
              <xsl:value-of select="description"/>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Darbības:</td>
            <td width="100%" nowrap="nowrap">
              <p>
                <a href="{$baseActionURL}?userPreferencesAction=managePreferences&amp;profileId={@id}&amp;profileType=system">
                  <img alt="Rediģēt iestatījumus šim profilam" src="{$baseMediaURL}/profile_user_pref.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Rediģēt iestatījumus šim profilam</a>
                <br/>
                <xsl:if test="$allowSystemProfileMap = 'true' and not($profileId = @id and $profileType ='system')">
                  <a href="{$baseActionURL}?action=map&amp;profileId={@id}&amp;profileType=system">
                    <img alt="Piekārtot šo profilu manai pašreizējai pārlūkprogrmai" src="{$baseMediaURL}/profile_map.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Piekārtot šo profilu manai pašreizējai pārlūkprogrmai</a>
                  <br/>
                </xsl:if>
                <xsl:if test="/profiles/user">
                  <a href="{$baseActionURL}?action=copy&amp;profileId={@id}&amp;profileType=system">
                    <img alt="Kopēt šo profilu manos personīgajos profilos" src="{$baseMediaURL}/profile_duplicate.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Kopēt šo profilu manos personīgajos profilos</a>
                </xsl:if>
              </p>
            </td>
          </tr>
        </xsl:when>
        <xsl:otherwise>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="2" class="uportal-background-content">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=system">
                <img alt="Parādīt detaļas" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
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
                <br/>[Šis profils ir piekārtots pašreizējai pārlūkprogrammai]</xsl:if>
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
          <span class="uportal-channel-table-header">Personīgie profili</span></td>
          <td nowrap="nowrap" valign="bottom" align="left"><strong>
            <a href="{$baseActionURL}?action=expandAll&amp;profileType=user">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Parādīt detaļas" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Rādīt detaļas visiem</span>
            </a>
          </strong></td>
          <td nowrap="nowrap" valign="bottom" align="left" width="100%"><strong>
            <a href="{$baseActionURL}?action=condenseAll&amp;profileType=user">
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="16" height="16" border="0"/>
              <img alt="Paslēpt detaļas" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
              <img alt="" src="{$baseMediaURL}/transparent.gif" width="2" height="2" border="0"/>
              <span class="uportal-text-small">Nerādīt detaļas nevienam</span>
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
                  <img alt="Paslēpt detaļas" src="{$baseMediaURL}/minus001.gif" width="16" height="16" border="0"/>
                </a>
              </strong>
            </td>
            <td class="uportal-channel-text">Nosaukums:</td>
            <td width="100%">
              <strong>
                <xsl:value-of select="@name"/>
              </strong>
              <xsl:if test="$profileId = @id and $profileType ='user'">
                <br/>[Šis profils ir piekārtots pašreizējai pārlūkprogrammai]</xsl:if>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Apraksts:</td>
            <td width="100%">
              <xsl:value-of select="description"/>
            </td>
          </tr>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td>Darbības:</td>
            <td width="100%" nowrap="nowrap">
              <p>
                <a href="{$baseActionURL}?userPreferencesAction=managePreferences&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Rediģēt iestatījumus šim profilam" src="{$baseMediaURL}/profile_user_pref.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Rediģēt iestatījumus šim profilam</a>
                <br/>
                <a href="{$baseActionURL}?action=edit&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Rediģēt šo personīgo profilu" src="{$baseMediaURL}/profile_edit.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Rediģēt šo personīgo profilu</a>
                <br/>
                <xsl:if test="not($profileId = @id and $profileType ='user')">
                  <a href="{$baseActionURL}?action=map&amp;profileId={@id}&amp;profileType=user">
                    <img alt="Piekārtot šo profilu manai pašreizējai pārlūkprogrmai" src="{$baseMediaURL}/profile_map.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Piekārtot šo profilu manai pašreizējai pārlūkprogrmai</a>
                  <br/>
                </xsl:if>
                <a href="{$baseActionURL}?action=copy&amp;profileId={@id}&amp;profileType=user">
                  <img alt="Kopēt šo personīgo profilu" src="{$baseMediaURL}/profile_duplicate.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Kopēt šo personīgo profilu</a>
                <br/>
                <xsl:if test="$allowAdvancedProfileMapping = 'true'">
                  <a href="{$baseActionURL}?action=map_adv&amp;profileId={@id}&amp;profileType=user">
                    <img alt="Augstāka līmeņa profila piekārtošana" src="{$baseMediaURL}/profile_map_adv.gif" width="16" height="16" border="0"/>
                    <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Augstāka līmeņa profila piekārtošana</a>
                  <br/>
                </xsl:if>
                <a href="{$baseActionURL}?action=delete&amp;profileId={@id}&amp;profileType=user" onClick="return confirm('Vai Jūs tiešām gribat izdzēst šo profilu??')">
                  <img alt="Dzēst šo profilu" src="{$baseMediaURL}/profile_delete.gif" width="16" height="16" border="0"/>
                  <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Dzēst šo profilu</a>
              </p>
            </td>
          </tr>
        </xsl:when>
        <xsl:otherwise>
          <tr align="left" valign="top" class="uportal-channel-text">
            <td rowspan="2" class="uportal-background-content">
              <a href="{$baseActionURL}?action=changeView&amp;view=expanded&amp;profileId={@id}&amp;profileType=user">
                <img alt="Parādīt detaļas" src="{$baseMediaURL}/plus001.gif" width="16" height="16" border="0"/>
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
                <br/>[Šis profils ir piekārtots pašreizējai pārlūkprogrammai]</xsl:if>
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
            <img alt="Izveidot jaunu personīgo profilu" src="{$baseMediaURL}/profile_new.gif" width="16" height="16" border="0"/>
            <img alt="" src="{$baseMediaURL}/transparent.gif" width="8" height="8" border="0"/>Izveidot jaunu personīgo profilu</a>
        </td>
      </tr>
    </table>
    <!--End New Personal Profile Table -->
  </xsl:template>
</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
