<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences</xsl:variable>

<xsl:template match="profile">
<form action="{$baseActionURL}" method="post" name="form1">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr><td class="uportal-channel-table-header" colspan="4" valign="top"><xsl:value-of select="$EDIT_PROFILE"/></td></tr>
   <tr><td class="uportal-background-dark" colspan="4"><img height="1" src="{$mediaPath}/transparent.gif" width="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td></tr>
   <tr><td colspan="4"><img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td></tr>
   <tr><td class="uportal-text12-bold" colspan="4"><xsl:value-of select="$PROFILE_NAME"/></td></tr>
   <tr>
        <td class="uportal-channel-text" colspan="4">
        <input class="textform" name="profileName" size="20" type="text" value="{name}"/>
        </td>
   </tr>
   <tr><td colspan="4"><img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td></tr>
   <tr><td class="uportal-text12-bold" colspan="4" valign="top"><xsl:value-of select="$PROFILE_DESCRIPTION"/></td></tr>
   <tr>
        <td class="uportal-channel-text" colspan="4" valign="top">
        <textarea class="textform" cols="50" name="profileDescription" rows="3">
          <xsl:value-of select="description"/>
        </textarea>
        </td>
   </tr>
   <tr><td colspan="4"><img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img></td></tr>

    <xsl:apply-templates select="themestylesheets"/>

   <tr>
        <td class="uportal-text-small" colspan="4">
        <img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
        </td>
   </tr>
</table>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
   <TR><TD><IMG SRC="{$mediaPath}/transparent.gif" height="15" width="1"/></TD></TR>
   <TR><TD CLASS="uportal-background-med"><IMG SRC="{$mediaPath}/transparent.gif" height="1" width="1"/></TD></TR>
   <TR><TD ALIGN="RIGHT" CLASS="uportal-background-light">
        
    <input name="action" type="hidden" value="completeEdit"/>
    <input name="submitSave" type="submit"><xsl:attribute name="value"><xsl:value-of select="$SAVE_CHANGES"/></xsl:attribute></input>
    <img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
    <input name="submitCancel" type="submit"><xsl:attribute name="value"><xsl:value-of select="$CANCEL"/></xsl:attribute></input>

   </TD></TR>
   <TR><TD CLASS="bg1"><IMG SRC="{$mediaPath}/transparent.gif" height="3" width="1"/></TD></TR>
</TABLE>
</form>
</xsl:template>


<xsl:template match="themestylesheets">
    <tr>
      <td class="uportal-label" colspan="4" valign="top"><xsl:value-of select="$THEME"/></td>
    </tr>

    <tr>
      <td class="uportal-background-dark" colspan="4" valign="top">
        <img height="1" src="{$mediaPath}/transparent.gif" width="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
      </td>
    </tr>

    <tr>
      <td colspan="4" valign="top">
        <img height="10" src="{$mediaPath}/transparent.gif" width="10"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
      </td>
    </tr>

    <tr>
      <td colspan="4" valign="top">
        <table border="0" cellpadding="5" cellspacing="0" width="100%">
          <tr valign="top">
            <td align="center" class="uportal-channel-subtitle"><xsl:value-of select="$SELECT"/></td>

            <td align="center" class="uportal-channel-subtitle"><xsl:value-of select="$DEVICE_TYPE"/></td>

            <td class="uportal-channel-subtitle" width="100%">
              <div align="center"><xsl:value-of select="$NAME_DESCRIPTION"/></div>
            </td>

            <td align="center" class="uportal-channel-subtitle">
              <div align="center"><xsl:value-of select="$SAMPLE"/></div>
            </td>
          </tr>

          <xsl:apply-templates select="current"/>

          <xsl:apply-templates select="alternate"/>
        </table>
      </td>
    </tr>

    <tr>
      <td class="uportal-background-med" colspan="4">
        <img height="1" src="{$mediaPath}/transparent.gif" width="1"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="current">
    <tr valign="top">
      <td align="center" class="uportal-text-small" valign="middle">
        <input checked="checked" name="stylesheetID" type="radio" value="{id}"/>
      </td>

      <td align="center" class="uportal-text-small">
        <img height="90" src="{deviceiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>

        <br/>

        <xsl:value-of select="mimetype"/>
      </td>

      <td class="uportal-channel-text" valign="top" width="100%">
        <p>
          <strong>
            <xsl:value-of select="name"/>
          </strong>

          <br/>

          <xsl:value-of select="description"/>
        </p>
      </td>

      <xsl:choose>
        <xsl:when test="sampleiconuri =''">
          <td align="center" class="uportal-text-small" valign="middle"><xsl:value-of select="$NO_SAMPLE"/><br/><xsl:value-of select="$AVAIALBLE"/></td>
        </xsl:when>

        <xsl:when test="sampleuri = ''">
          <td align="center" class="uportal-text-small">
            <img border="0" height="90" src="{sampleiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
          </td>
        </xsl:when>

        <xsl:otherwise>
          <td align="center" class="uportal-text-small">
          <a href="{sampleuri}" target="_blank">
            <img border="0" height="90" src="{sampleiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
          </a>

          <br/><xsl:value-of select="$CLICK_TO_ENLARGE"/></td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>

  <xsl:template match="alternate">
    <tr valign="top">
      <td align="center" class="uportal-text-small" valign="middle">
        <input name="stylesheetID" type="radio" value="{id}"/>
      </td>

      <td align="center" class="uportal-text-small">
        <img height="90" src="{deviceiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>

        <br/>

        <xsl:value-of select="mimetype"/>
      </td>

      <td class="uportal-channel-text" valign="top" width="100%">
        <p>
          <strong>
            <xsl:value-of select="name"/>
          </strong>

          <br/>

          <xsl:value-of select="description"/>
        </p>
      </td>

      <xsl:choose>
        <xsl:when test="sampleiconuri =''">
          <td align="center" class="uportal-text-small" valign="middle"><xsl:value-of select="$NO_SAMPLE"/><br/><xsl:value-of select="$AVAIALBLE"/></td>
        </xsl:when>

        <xsl:when test="sampleuri = ''">
          <td align="center" class="uportal-text-small">
            <img border="0" height="90" src="{sampleiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
          </td>
        </xsl:when>

        <xsl:otherwise>
          <td align="center" class="uportal-text-small">
          <a href="{sampleuri}" target="_blank">
            <img border="0" height="90" src="{sampleiconuri}" width="120"><xsl:attribute name="alt"><xsl:value-of select="$IMAGE_ALT_INTERFACE_IMAGE"/></xsl:attribute></img>
          </a>

          <br/><xsl:value-of select="$CLICK_TO_ENLARGE"/></td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>
  </xsl:template>
</xsl:stylesheet>