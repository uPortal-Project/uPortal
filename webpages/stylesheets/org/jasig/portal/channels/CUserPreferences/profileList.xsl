<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="profileType">profileType_false</xsl:param>
  <xsl:param name="profileName">profileName_false</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CUserPreferences/</xsl:variable>
  <!--add to baseMediaURL select 'media/org/jasig/portal/channels/CUserPreferences/'-->
  <xsl:template match="profiles">
    <!--delete head info for final xsl (should come from portal)-->
        <div align="center">
          <center>
            <table border="0" width="100%" cellspacing="0" cellpadding="2">
              <xsl:apply-templates select="user"/>
              <xsl:apply-templates select="system"/>
              <tr>
                <td class="uportal-text" colspan="11">
                  <img alt="interface image" border="0" width="16" height="16">
                    <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
                  </img>
                </td>
              </tr>
              <tr>
                <td class="uportal-text"/>
                <td class="uportal-text" valign="top" align="center">
                  <a>
                    <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=newProfile</xsl:attribute>
                    <img border="0" width="16" height="16" alt="Create new profile">
                      <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_new.gif</xsl:attribute>
                    </img>
                  </a>
                </td>
                <td class="uportal-text" valign="top" align="left" colspan="9">
            Click to create a new Personal profile<br/>
                  <span class="uportal-text-small">(please read help first by
            clicking the help icon in the channel header)</span>
                </td>
              </tr>
            </table>
          </center>
        </div>
  </xsl:template>
  <xsl:template match="user">
    <tr>
      <td colspan="11" class="uportal-background-med" valign="bottom">
        <p class="uportal-channel-title">Personal Profiles</p>
      </td>
    </tr>
    <tr>
      <td class="uportal-background-light">
        <img alt="interface image" border="0" width="1" height="1">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td colspan="6" class="uportal-background-light" valign="bottom" align="center">
        <p class="uportal-channel-subtitle">Action</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="interface image" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">Profile Name</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="interface image" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td width="60%" class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">Profile Description</p>
      </td>
    </tr>
    <xsl:choose>
      <xsl:when test="/profiles/user/profile">
        <xsl:apply-templates mode="user"/>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td colspan="6"/>
          <td class="uportal-text" valign="top">no profiles defined</td>
          <td class="uportal-text" valign="top"/>
          <td width="60%" class="uportal-text" valign="top">Click the copy button next to the desired System Profile to create a custom profile</td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="profile" mode="user">
    <xsl:choose>
      <xsl:when test="$profileName=@name and $profileType='user'">
        <xsl:call-template name="selected_userProfile"/>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td/>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>userPreferencesAction=managePreferences&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit Layout and preferences">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_user_pref.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=edit&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit profile">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_edit.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=copyUserProfile&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Copy personal profile">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_duplicate.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=delete&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Delete profile">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_delete.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Map current browser to this profile">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map_adv&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Advanced mapping properties">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map_adv.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top"/>
          <td class="uportal-text" valign="top">
            <xsl:value-of select="@name"/>
          </td>
          <td class="uportal-text" valign="top"/>
          <td width="60%" class="uportal-text" valign="top">
            <xsl:value-of select="description"/>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="system">
    <tr>
      <td class="uportal-background-med" valign="top" colspan="11">
        <p class="uportal-channel-title">System Profiles</p>
      </td>
    </tr>
    <tr>
      <td class="uportal-background-light">
        <img alt="interface image" border="0" width="1" height="1">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td colspan="6" class="uportal-background-light" valign="bottom" align="center">
        <p class="uportal-channel-subtitle">Action</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="interface image" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">Profile Name</p>
      </td>
      <td class="uportal-background-light" valign="bottom">
        <img alt="interface image" border="0" width="10" height="10">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
        </img>
      </td>
      <td width="60%" class="uportal-background-light" valign="bottom">
        <p class="uportal-channel-subtitle">Profile Description</p>
      </td>
    </tr>
    <xsl:apply-templates mode="system"/>
  </xsl:template>
  <xsl:template match="profile" mode="system">
    <xsl:choose>
      <xsl:when test="$profileName=@name and $profileType='system'">
        <xsl:call-template name="selected_systemProfile"/>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td class="uportal-text"/>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>userPreferencesAction=managePreferences&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit Layout and preferences">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_user_pref.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top" align="center"/>
          <td class="uportal-text" valign="top" align="center">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=copy&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
          <img border="0" width="16" height="16" alt="Copy system profile to my profiles">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_duplicate.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top" align="center"/>
          <td class="uportal-text" valign="top">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
              <img border="0" width="16" height="16" alt="Map current browser to this profile">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map.gif</xsl:attribute>
              </img>
            </a>
          </td>
          <td class="uportal-text" valign="top" align="center"/>
          <td class="uportal-text" valign="top"/>
          <td class="uportal-text" valign="top">
            <xsl:value-of select="@name"/>
          </td>
          <td class="uportal-text" valign="top"/>
          <td width="60%" class="uportal-text" valign="top">
            <xsl:value-of select="description"/>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="selected_userProfile">
    <tr>
      <td class="uportal-text" valign="top">
        <img border="0" width="16" height="16" alt="Selected profile">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>selected_arrow.gif</xsl:attribute>
        </img>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>userPreferencesAction=managePreferences&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit Layout and preferences">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_user_pref.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=edit&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit profile">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_edit.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=copyUserProfile&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Copy personal profile">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_duplicate.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=delete&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Delete profile">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_delete.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Map current browser to this profile">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map_adv&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=user</xsl:attribute>
              <img border="0" width="16" height="16" alt="Advanced mapping properties">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map_adv.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top"/>
      <td class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="@name"/>
      </td>
      <td class="uportal-text" valign="top"/>
      <td width="60%" class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="description"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="selected_systemProfile">
    <tr>
      <td class="uportal-text" valign="top">
        <img border="0" width="16" height="16" alt="Selected profile">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>selected_arrow.gif</xsl:attribute>
        </img>
      </td>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>userPreferencesAction=managePreferences&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
              <img border="0" width="16" height="16" alt="Edit Layout and preferences">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_user_pref.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top" align="center"/>
      <td class="uportal-text" valign="top" align="center">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=copy&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
          <img border="0" width="16" height="16" alt="Copy system profile to my profiles">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_duplicate.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top" align="center"/>
      <td class="uportal-text" valign="top">
        <a>
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>action=map&amp;profileName=<xsl:value-of select="@name"/>&amp;profileType=system</xsl:attribute>
              <img border="0" width="16" height="16" alt="Map current browser to this profile">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/>profile_map.gif</xsl:attribute>
          </img>
        </a>
      </td>
      <td class="uportal-text" valign="top" align="center"/>
      <td class="uportal-text" valign="top"/>
      <td class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="@name"/>
      </td>
      <td class="uportal-text" valign="top"/>
      <td width="60%" class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="description"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
