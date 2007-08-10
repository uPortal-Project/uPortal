<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="no"/>
  <xsl:param name="baseActionURL">render.userLayoutRootNode.uP</xsl:param>
  <xsl:param name="stepID">1</xsl:param>
  <xsl:param name="errorMessage">no parameter passed</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:variable name="defaultLength">10</xsl:variable>
  <xsl:variable name="defaultMaxLength">20</xsl:variable>
  <xsl:variable name="defaultTextCols">40</xsl:variable>
  <xsl:variable name="defaultTextRows">10</xsl:variable>
  <xsl:variable name="mediaPath">media/org/jasig/portal/channels/CUserPreferences/tab-column</xsl:variable>

  <xsl:template match="/">
    <!-- form begin -->
    <form name="parameters" method="post" action="{$baseActionURL}">
      <input type="hidden" name="uPTCUP_action" value="changeMe"/>
      <table width="100%" border="0" cellspacing="0" cellpadding="10" class="uportal-background-light">
        <tr class="uportal-channel-text">
          <td>
            <strong>Channel Settings:</strong> The channel you have selected has settings which may be modified.</td>
        </tr>
        <tr>
          <td>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="uportal-background-content">
              <xsl:choose>
                <xsl:when test="$errorMessage = 'no parameter passed'">
                  <xsl:choose>
                    <xsl:when test="not(/userPrefParams/channelDef)">
                      <tr class="uportal-channel-table-header" valign="bottom">
                        <td width="100%">Custom Channel Settings</td>
                      </tr>
                      <tr class="uportal-channel-table-header">
                        <td align="center">
                          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                            <tr>
                              <td>
                                <img alt="" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <xsl:for-each select="/userPrefParams/channel/parameter[@override = 'yes']">
                        <tr class="uportal-channel-table-header" valign="bottom">
                          <td width="100%">
                            <span class="uportal-label">
                              <xsl:value-of select="@name"/>:</span>
                            <br/>
                            <input type="text" name="{@name}" size="40" class="uportal-input-text">
                              <xsl:attribute name="value">
                                <xsl:value-of select="@value"/>
                              </xsl:attribute>
                            </input>
                          </td>
                        </tr>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>

                      <tr class="uportal-channel-table-header" valign="bottom">
                        <td align="center" nowrap="nowrap">
                          <img alt="" src="{$mediaPath}/transparent.gif" width="16" height="8"/>Help<img alt="" src="{$mediaPath}/transparent.gif" width="16" height="8"/></td>


                        <td width="100%">Channel Settings</td>
                      </tr>
                      <tr class="uportal-channel-table-header">
                        <td align="center" colspan="4">
                          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                            <tr>
                              <td>
                                <img alt="" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <xsl:apply-templates select="/userPrefParams/channelDef//parameter[child::name = /userPrefParams/channel/parameter[@override='yes']/@name]"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                  <tr class="uportal-channel-table-header" valign="bottom">
                    <td width="100%">Error Message</td>
                  </tr>
                  <tr class="uportal-channel-table-header">
                    <td align="center">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
                        <tr>
                          <td>
                            <img alt="" src="{$mediaPath}/transparent.gif" width="2" height="2"/>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  <tr class="uportal-channel-table-header" valign="bottom">
                    <td width="100%">
                      <xsl:value-of select="$errorMessage"/>
                    </td>
                  </tr>
                </xsl:otherwise> </xsl:choose>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" name="uPTCUP_submit" value="Back" onclick="document.parameters.uPTCUP_action.value='back'" class="uportal-button"/>
            <input type="submit" name="uPTCUP_submit" value="Finished" onclick="document.parameters.uPTCUP_action.value='finished'" class="uportal-button"/>
            <input type="submit" name="uPTCUP_submit" value="Cancel" onclick="document.parameters.uPTCUP_action.value='cancel'" class="uportal-button"/>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>


  <xsl:template match="parameter">
    <xsl:choose>
      <xsl:when test="type/@display != 'hidden'">
        <tr>
          <td align="center" valign="top">
            <xsl:call-template name="help"/>
          </td>
          <xsl:choose>
            <xsl:when test="type/@input='text'">
              <xsl:call-template name="text"/>
            </xsl:when>
            <xsl:when test="type/@input='single-choice'">
              <xsl:call-template name="single-choice"/>
            </xsl:when>
            <xsl:when test="type/@input='multi-choice'">
              <xsl:call-template name="multi-choice"/>
            </xsl:when>
          </xsl:choose>
        </tr>
        <tr class="uportal-channel-table-header">
          <td align="center" colspan="4">
            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="uportal-background-light">
              <tr>
                <td>
                  <img alt="" src="{$mediaPath}/transparent.gif" width="1" height="1"/>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="type/@input='text'">
            <xsl:call-template name="text"/>
          </xsl:when>
          <xsl:when test="type/@input='single-choice'">
            <xsl:call-template name="single-choice"/>
          </xsl:when>
          <xsl:when test="type/@input='multi-choice'">
            <xsl:call-template name="multi-choice"/>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- displays checkbox for publisher to allow subscribe time modification-->
  <xsl:template name="subscribe">
  </xsl:template>
  <!-- display all the input fields with a base type of 'single-choice'-->
  <xsl:template name="single-choice">
    <xsl:choose>
      <xsl:when test="type/@display='drop-down'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>


              <option value="{.}">

                <xsl:if test="/userPrefParams/channel/parameter[@name=$paramName]/@value=$paramValue">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>

                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='radio'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <xsl:for-each select="type/restriction/value">
                      
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
            <input type="radio" name="{name}" value="{.}" class="uportal-input-text">
              <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:choose>
              <xsl:when test="@display">
                <xsl:value-of select="@display"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
                        
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <xsl:call-template name="subscribe"/>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- display all the input fields with a base type of 'multi-choice'-->
  <xsl:template name="multi-choice">
    <xsl:choose>
      <xsl:when test="type/@display='select-list'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" size="6" multiple="multiple" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='checkbox'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <xsl:for-each select="type/restriction/value">
                      
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
            <input type="checkbox" name="{name}" value="{.}">
              <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:choose>
              <xsl:when test="@display">
                <xsl:value-of select="@display"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <select name="{name}" size="6" multiple="multiple" class="uportal-input-text">
            <xsl:for-each select="type/restriction/value">
                        
              <xsl:variable name="paramName">
                <xsl:value-of select="../../../name"/>
              </xsl:variable>
              <xsl:variable name="paramValue">
                <xsl:value-of select="."/>
              </xsl:variable>
              <option value="{.}">
                <xsl:if test=". = /userPrefParams/channel/parameter[@name=$paramName]/@value">
                  <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@display">
                    <xsl:value-of select="@display"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="."/>
                  </xsl:otherwise>
                </xsl:choose>
              </option>
            </xsl:for-each>
          </select>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- display all the input fields with a base type of 'text'-->
  <xsl:template name="text">
    <!-- since length and maxlength are not required test existence and use defaults if needed -->
    <xsl:variable name="length">
      <xsl:choose>
        <xsl:when test="type/length"> <xsl:value-of select="type/length"/> </xsl:when>
        <xsl:otherwise> <xsl:value-of select="$defaultLength"/> </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="maxlength">
      <xsl:choose>
        <xsl:when test="type/maxlength"> <xsl:value-of select="type/maxlength"/> </xsl:when>
        <xsl:otherwise> <xsl:value-of select="$defaultMaxLength"/> </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="paramName">
      <xsl:value-of select="name"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="type/@display='text'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input type="text" name="{name}" maxlength="{$maxlength}" size="{$length}" class="uportal-input-text">
            <xsl:attribute name="value">
              <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
            </xsl:attribute>
          </input>
          <xsl:apply-templates select="units"/>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='textarea'">
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <textarea name="{name}" rows="{$defaultTextRows}" cols="{$defaultTextCols}" class="uportal-input-text">
            <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
          </textarea>
        </td>
      </xsl:when>
      <xsl:when test="type/@display='hidden'">
        <input type="hidden" name="{name}" value="{/userPrefParams/channel/parameter[@name = $paramName]/@value}"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="subscribe"/>
        <td class="uportal-text-small">
          <xsl:apply-templates select="label"/>
          <xsl:apply-templates select="example"/>
          <br/>
          <input type="text" name="{name}" maxlength="{$maxlength}" size="{$length}" class="uportal-input-text">
            <xsl:attribute name="value">
              <xsl:value-of select="/userPrefParams/channel/parameter[@name = $paramName]/@value"/>
            </xsl:attribute>
          </input>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="label">
    <span class="uportal-label">
      <xsl:value-of select="."/>:</span>
  </xsl:template>

  <xsl:template match="example">
    <img alt="" src="{$mediaPath}/transparent.gif" width="8" height="8"/>
    <span class="uportal-text-small">[example - <xsl:value-of select="."/>]</span>
  </xsl:template>

  <xsl:template match="units">
    <img alt="" src="{$mediaPath}/transparent.gif" width="8" height="8"/>
    <span class="uportal-text-small">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template name="help">
    <a>
      <xsl:attribute name="href">javascript:alert('Name: <xsl:value-of select="label"/>\nExample: <xsl:value-of select="example"/>\n\nDescription: <xsl:value-of select="description"/>')</xsl:attribute>
      <img src="{$mediaPath}/help.gif" width="16" height="16" border="0" alt="Display help information"/>
    </a>
  </xsl:template>
</xsl:stylesheet><!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. -->
