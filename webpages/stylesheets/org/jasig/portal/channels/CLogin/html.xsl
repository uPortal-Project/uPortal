<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="guest">false</xsl:param>
<xsl:variable name="mediaPath" select="'media/org/jasig/portal/channels/CLogin'"/>
  <xsl:template match="login-status">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <xsl:choose>
        <xsl:when test="$guest='true'">
          <form action="authentication.jsp" method="post">
            <input type="hidden" name="action" value="login"/>
            <input type="hidden" name="baseActionURL" value="{$baseActionURL}"/>
            <xsl:call-template name="buildTable"/>
          </form>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="buildTable"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$guest='true'">
      </xsl:if>
    </table>
  </xsl:template>
  <xsl:template match="failure">
    <tr class="uportal-background-light">
      <td width="100%" class="uportal-channel-warning" colspan="1" rowspan="1">The user name/password combination entered is not recognized. Please try again.<span class="uportal-channel-warning"></span>
      </td>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="error">
    <tr class="uportal-background-light">
      <td width="100%" class="uportal-channel-warning" colspan="1" rowspan="1">An error occured during authentication. The portal is unable to log you on at this time. Try again later.<span class="uportal-channel-warning"></span>
      </td>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="full-name">
  </xsl:template>
  <xsl:template name="buildTable">
    <tr>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
    <tr>
      <td colspan="1" rowspan="1">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td colspan="4" class="uportal-background-shadow" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td rowspan="4" colspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
            </td>
            <td class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
          <tr>
            <td colspan="3" nowrap="nowrap" class="uportal-background-light" rowspan="1">
              <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <tr class="uportal-background-light">
                  <td class="uportal-channel-text" nowrap="nowrap" colspan="1" rowspan="1">
                    <strong>
                      <xsl:choose>
                        <xsl:when test="$guest='true'">Welcome Guest - Please Login: </xsl:when>
                        <xsl:otherwise>Welcome <xsl:value-of select="full-name"/> </xsl:otherwise>
                      </xsl:choose>
                    </strong>
                  </td>
                </tr>
              </table>
            </td>
            <td nowrap="nowrap" class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td nowrap="nowrap" class="uportal-background-med" rowspan="2" colspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td nowrap="nowrap" class="uportal-background-shadow" colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td width="100%" class="uportal-channel-text" colspan="1" rowspan="1">
              <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <tr class="uportal-background-light">
                  <td width="100%" class="uportal-channel-text" nowrap="nowrap" colspan="1" rowspan="1">
                    <!--Right Content Cell [1]-->
                    <xsl:choose>
                      <xsl:when test="$guest='true'">
                        <span class="uportal-label">Name:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input class="uportal-input-text" type="text" name="userName" size="15">
                            <xsl:attribute name="value">
                              <xsl:value-of select="/login-status/failure/@attemptedUserName"/>
                            </xsl:attribute></input>Password:<img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input class="uportal-input-text" type="password" name="password" size="15"/><img alt="interface image" src="{$mediaPath}/transparent.gif" width="4" height="1"/><input type="submit" value="Login" name="Login" class="uportal-button"/></span>
                      </xsl:when>
                      <xsl:otherwise>You are currently logged in.</xsl:otherwise>
                    </xsl:choose>
                  </td>
                  <td class="uportal-text-small" nowrap="nowrap" colspan="1" rowspan="1">
                    <xsl:choose>
                      <xsl:when test="$guest='true'">
                        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                      </xsl:when>
                      <xsl:otherwise>

                        <SCRIPT LANGUAGE="JavaScript1.2">


var months=new Array(13);
months[1]='January';
months[2]='February';
months[3]='March';
months[4]='April';
months[5]='May';
months[6]='June';
months[7]='July';
months[8]='August';
months[9]='September';
months[10]='October';
months[11]='November';
months[12]='December';
var time=new Date();
var lmonth=months[time.getMonth() + 1];
var date=time.getDate();
var year=time.getYear();
document.write(lmonth + ' ');
document.write(date + ', ' + year);
              </SCRIPT>
                      </xsl:otherwise>
                    </xsl:choose>
                  </td>
                </tr>
                <!--Right Message Row -->
                <xsl:if test="$guest='true'">
                  <xsl:apply-templates/>
                </xsl:if>
              </table>
            </td>
          </tr>
          <tr class="uportal-background-shadow">
            <td colspan="4" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
          <tr class="uportal-background-med">
            <td colspan="5" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
            <td colspan="1" rowspan="1">
              <img alt="interface image" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
            </td>
          </tr>
        </table>
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
    <tr>
      <td colspan="1" rowspan="1">
        <img alt="interface image" src="{$mediaPath}/transparent.gif" width="5" height="5"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c)1998-2001 eXcelon Corp.
<metaInformation>
<scenarios/>
</metaInformation>
-->
