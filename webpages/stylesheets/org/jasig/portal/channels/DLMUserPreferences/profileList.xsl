<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="yes" method="html"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="profileType">profileType_false</xsl:param>
  <xsl:param name="profileId">profileId_false</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CUserPreferences/</xsl:variable>
  <!--add to baseMediaURL select 'media/org/jasig/portal/channels/CUserPreferences/'-->

<xsl:template match="profiles">
    <!--delete head info for final xsl (should come from portal)-->
        <div align="center">
          <center>
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
           <tr class="uportal-background-light">
                <td align="center" class="uportal-text">
                  <a onMouseover="window.status=''; return true;">
                    <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=newProfile</xsl:attribute>
                    <img border="0" height="16" vspace="2" width="16">
                      <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_NEW_GIF"/></xsl:attribute>
                    <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_CREATE_NEW_PROFILE"/></xsl:attribute></img>
                  </a>
                </td>
                <td align="left" class="uportal-text"><xsl:value-of select="$CLICK_TO_CREATE_A_NEW_PERSONAL_PROFILE"/><span class="uportal-text-small"><xsl:value-of select="$PLEASE_READ_HELP_FIRST_BY_CLICKING_THE_HELP_ICON_IN_THE_CHANNEL_HEADER"/></span>
                </td>
           </tr>
           <tr><td colspan="2"><img border="0" height="15" src="{$baseMediaURL}transparent.gif" width="15"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td></tr>
        </table>

        <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
    <xsl:apply-templates select="user"/>
    <xsl:apply-templates select="system"/>
        </TABLE>
        
        <BR/>
        <BR/>
        </center>
        </div>
<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
   <TR><TD CLASS="uportal-background-med"><IMG HEIGHT="1" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
   <TR><TD CLASS="uportal-background-light"><IMG HEIGHT="24" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
   <TR><TD CLASS="bg1"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="3"/></TD></TR>
</TABLE>
</xsl:template>


<xsl:template match="user">
   <TR><TD CLASS="uportal-head14-bold" COLSPAN="11"><xsl:value-of select="$PERSONAL_PROFILES"/></TD></TR>
   <TR><TD CLASS="uportal-background-med" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
   <TR CLASS="uportal-background-light">
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD ALIGN="CENTER" CLASS="uportal-text-small" COLSPAN="6"><xsl:value-of select="$ACTION"/></TD>
        <TD><IMG HEIGHT="20" HSPACE="5" SRC="/cps/images/misc/divider.gif" WIDTH="2"/></TD>
        <TD CLASS="uportal-text-small"><xsl:value-of select="$PROFILE_NAME"/></TD>
        <TD><IMG HEIGHT="20" HSPACE="5" SRC="/cps/images/misc/divider.gif" WIDTH="2"/></TD>
        <TD CLASS="uportal-text-small"><xsl:value-of select="$PROFILE_DESCRIPTION"/></TD>
   </TR>

    <xsl:choose>

        <xsl:when test="/profiles/user/profile">
            <xsl:apply-templates mode="user"/>
        </xsl:when>

        <xsl:otherwise>
   <TR>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-text" COLSPAN="10" HEIGHT="30"><B><xsl:value-of select="$NO_PROFILES_DEFINED_"/></B><xsl:value-of select="$CLICK_THE_COPY_BUTTON_NEXT_TO_THE_DESIRED_SYSTEM_PROFILE_TO_CREATE_A_CUSTOM_PROFILE"/></TD>
   </TR>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template match="profile" mode="user">
    <xsl:choose>
        <xsl:when test="$profileId=@id and $profileType='user'">
            <xsl:call-template name="selected_userProfile"/>
        </xsl:when>

        <xsl:otherwise>
   <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
   <TR>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?userPreferencesAction=managePreferences&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_LAYOUT_AND_PREFERENCES"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_USER_PREF_GIF"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=edit&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_EDIT_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_PROFILE"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=copy&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_DUPLICATE_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_COPY_PERSONAL_PROFILE"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=delete&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_DELETE_PROFILE"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_DELETE_GIF"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_MAP_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_MAP_CURRENT_BROWSER_TO_THIS_PROFILE"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD CLASS="uportal-text" VALIGN="top">
            <A onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map_adv&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_ADVANCED_MAPPING_PROPERTIES"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_MAP_ADV_GIF"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-text" valign="top">
            <xsl:value-of select="@name"/>
        </TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-text" WIDTH="60%" valign="top">
            <xsl:value-of select="description"/>
        </TD>
   </TR>
   <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
   <TR><TD CLASS="uportal-background-light" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template match="system">
   <TR><TD COLSPAN="11"><IMG HEIGHT="15" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
   <TR><TD CLASS="uportal-head14-bold" COLSPAN="11"><xsl:value-of select="$SYSTEM_PROFILES"/></TD></TR>
   <TR><TD CLASS="uportal-background-med" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
   <TR CLASS="uportal-background-light">
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD ALIGN="CENTER" CLASS="uportal-text-small" COLSPAN="6" WIDTH="20%"><xsl:value-of select="$ACTION"/></TD>
        <TD><IMG HEIGHT="20" HSPACE="5" SRC="/cps/images/misc/divider.gif" WIDTH="2"/></TD>
        <TD CLASS="uportal-text-small" WIDTH="25%"><xsl:value-of select="$PROFILE_NAME"/></TD>
        <TD><IMG HEIGHT="20" HSPACE="5" SRC="/cps/images/misc/divider.gif" WIDTH="2"/></TD>
        <TD CLASS="uportal-text-small" WIDTH="55%"><xsl:value-of select="$PROFILE_DESCRIPTION"/></TD>
   </TR>

    <xsl:apply-templates mode="system"/>
    
</xsl:template>


<xsl:template match="profile" mode="system">
    <xsl:choose>
        <xsl:when test="$profileId=@id and $profileType='system'">
            <xsl:call-template name="selected_systemProfile"/>
        </xsl:when>
        <xsl:otherwise>
   <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
   <tr>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <td class="uportal-text" valign="top">
            <a onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?userPreferencesAction=managePreferences&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
              <img border="0" height="16" width="16">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_USER_PREF_GIF"/></xsl:attribute>
              <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_LAYOUT_AND_PREFERENCES"/></xsl:attribute></img>
            </a>
          </td>
          <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
          <td align="center" class="uportal-text" valign="top">
            <a onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=copy&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
          <img border="0" height="16" width="16">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_DUPLICATE_GIF"/></xsl:attribute>
              <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_COPY_SYSTEM_PROFILE_TO_MY_PROFILES"/></xsl:attribute></img>
            </a>
          </td>
          <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
          <td class="uportal-text" valign="top">
            <a onMouseover="window.status=''; return true;">
              <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
              <img border="0" height="16" width="16">
                <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_MAP_GIF"/></xsl:attribute>
              <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_MAP_CURRENT_BROWSER_TO_THIS_PROFILE"/></xsl:attribute></img>
            </a>
          </td>
          <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
          <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
          <td class="uportal-text" valign="top">
            <xsl:value-of select="@name"/>
          </td>
          <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
          <td class="uportal-text" valign="top">
            <xsl:value-of select="description"/>
          </td>
        </tr>
        <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
        <TR><TD CLASS="uportal-background-light" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>



  <xsl:template name="selected_userProfile">
    <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
    <tr>
      <td class="uportal-text" valign="top">
        <img border="0" height="16" width="16">
          <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$SELECTED_ARROW_GIF"/></xsl:attribute>
        <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_SELECTED_PROFILE"/></xsl:attribute></img>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?userPreferencesAction=managePreferences&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_USER_PREF_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_LAYOUT_AND_PREFERENCES"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=edit&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_EDIT_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_PROFILE"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=copyUserProfile&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_DUPLICATE_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_COPY_PERSONAL_PROFILE"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=delete&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_DELETE_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_DELETE_PROFILE"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_MAP_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_MAP_CURRENT_BROWSER_TO_THIS_PROFILE"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top">
        <a onMouseover="window.status=''; return true;">
          <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map_adv&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=user</xsl:attribute>
              <img border="0" height="16" width="16">
            <xsl:attribute name="src"><xsl:value-of select="string($baseMediaURL)"/><xsl:value-of select="$PROFILE_MAP_ADV_GIF"/></xsl:attribute>
          <xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_ADVANCED_MAPPING_PROPERTIES"/></xsl:attribute></img>
        </a>
      </td>
      <td class="uportal-text" valign="top"/>
      <td class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="@name"/>
      </td>
      <td class="uportal-text" valign="top"/>
      <td class="uportal-channel-emphasis" valign="top">
        <xsl:value-of select="description"/>
      </td>
    </tr>
    <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
    <TR><TD CLASS="uportal-background-light" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
  </xsl:template>


<xsl:template name="selected_systemProfile">
   <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
   <TR>
        <TD VALIGN="top"><IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLSELECTED_ARROW_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_SELECTED_PROFILE"/></xsl:attribute></IMG></TD>
        <TD VALIGN="top">
            <A onMouseover="window.status=''; return true;">
                <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?userPreferencesAction=managePreferences&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
                <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_EDIT_LAYOUT_AND_PREFERENCES"/></xsl:attribute><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_USER_PREF_GIF"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD VALIGN="top">
            <A onMouseover="window.status=''; return true;">
                <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=copy&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
                <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_DUPLICATE_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_COPY_SYSTEM_PROFILE_TO_MY_PROFILES"/></xsl:attribute></IMG>
            </A>
      </TD>
      <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
      <TD VALIGN="top">
            <A onMouseover="window.status=''; return true;">
                <xsl:attribute name="href"><xsl:value-of select="string($baseActionURL)"/>?action=map&amp;profileId=<xsl:value-of select="@id"/>&amp;profileType=system</xsl:attribute>
                <IMG BORDER="0" HEIGHT="16" WIDTH="16"><xsl:attribute name="SRC"><xsl:value-of select="$IMAGE_SRC_BASEMEDIAURLPROFILE_MAP_GIF"/></xsl:attribute><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_MAP_CURRENT_BROWSER_TO_THIS_PROFILE"/></xsl:attribute></IMG>
            </A>
        </TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-channel-emphasis" VALIGN="top">
    <xsl:value-of select="@name"/>
        </TD>
        <TD><IMG HEIGHT="1" SRC="{$baseMediaURL}transparent.gif" WIDTH="1"/></TD>
        <TD CLASS="uportal-channel-emphasis" VALIGN="top">
    <xsl:value-of select="description"/>
        </TD>
   </TR>
   <TR><TD COLSPAN="11"><IMG HEIGHT="3" SRC="/cps/images/misc/dot-blank.gif" WIDTH="1"/></TD></TR>
   <TR><TD CLASS="uportal-background-light" COLSPAN="11"><IMG HEIGHT="2" SRC="/cps/images/misc/dot-blank.gif" WIDTH="2"/></TD></TR>
</xsl:template>

</xsl:stylesheet>