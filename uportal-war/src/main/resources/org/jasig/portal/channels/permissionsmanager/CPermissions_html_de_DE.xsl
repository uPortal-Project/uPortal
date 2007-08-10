<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:output method="html" indent="yes" version="4.0"/>
        <xsl:param name="baseActionURL"/>
        <xsl:param name="locale">de_DE</xsl:param>
        <xsl:param name="prmView"/>
        <xsl:param name="isAdminUser" select="false()"/>
        <xsl:param name="commandResponse">null</xsl:param>
        <xsl:key name="perms" match="permission" use="concat(parent::principal/@token,'|',@owner,'|',@activity,'|',@target)"/>

        <xsl:template match="/">
          <xsl:choose>
              <xsl:when test="not($isAdminUser)">
                <xsl:call-template name="notAuthorized"/>
              </xsl:when>
              <xsl:otherwise>
                <table border="0" cellpadding="3" cellspacing="3" width="100%">
                    <xsl:call-template name="header"/>
                    <form action="{$baseActionURL}" method="post" name="permissionsForm">
                    <xsl:choose>
                        <xsl:when test="$prmView='Select Owners'">
                            <xsl:call-template name="selectDeselectAll"/>
                            <xsl:apply-templates select="//owner"/>
                            <xsl:call-template name="selectfooter"/>
                        </xsl:when>
                        <xsl:when test="$prmView='Select Activities'">
                            <xsl:call-template name="selectDeselectAll"/>
                            <xsl:call-template name="activities"/>
                            <xsl:call-template name="selectfooter"/>
                        </xsl:when>
                        <xsl:when test="$prmView='Select Targets'">
                            <xsl:call-template name="selectDeselectAll"/>
                            <xsl:call-template name="targets"/>
                            <xsl:call-template name="selectfooter"/>
                        </xsl:when>
                        <xsl:when test="$prmView='Assign By Principal'">
                            <xsl:call-template name="assignByPrincipal"/>
                            <xsl:call-template name="selectfooter"/>
                        </xsl:when>
                        <xsl:when test="$prmView='Assign By Owner'">
                            <xsl:call-template name="assignByOwner"/>
                            <xsl:call-template name="selectfooter"/>
                        </xsl:when>
                    </xsl:choose>
                    </form>
                </table>
              </xsl:otherwise>
            </xsl:choose>
        </xsl:template>

        <xsl:template name="selectDeselectAll">
          <script LANGUAGE="JavaScript">
            <xsl:text>
                  &lt;!--
                  function change(form,bool) {
                      for (i = 0; i &lt; form.length; i++) {
                        if (form[i].type == "checkbox") {
                          form[i].checked = bool;
                        }
                      }
                  }
                  //  End --&gt;
             </xsl:text>
          </script>
          <tr>
            <td colspan="2">
              <input class="uportal-button" type="button" onClick="javascript:change(this.form,true);" value="Wähle alles aus"/>
              <xsl:text> </xsl:text>
              <input class="uportal-button" type="button" onClick="javascript:change(this.form,false);" value="Wähle alles ab"/>
            </td>
          </tr>
        </xsl:template>

        <xsl:template name="notAuthorized">
          <span class="uportal-channel-warning">
          <xsl:text>Sie sind nicht berechtigt diesen Channel zu benutzen</xsl:text>
          </span>
        </xsl:template>

        <xsl:template name="assignByPrincipal">
          <input type="hidden" name="prmCommand" value="AssignPermissions"/>
            <xsl:for-each select="//principal">
                <xsl:variable name="principalkey" select="@token"/>
                <tr><td colspan="2">
                	<span class="uportal-channel-table-header">
                    <xsl:value-of select="@name"/>
                    </span>
                    <span class="uportal-channel-text">
                    <xsl:call-template name="principalTypeDisplay">
                      <xsl:with-param name="type">
                        <xsl:value-of select="@type"/>
                      </xsl:with-param>
                    </xsl:call-template>
                    <br/>
                    <em>
                    	<xsl:text> Weisen Sie die Erlaubnis für diese Direktion im Kontext dieser Inhaber zu:</xsl:text>
                    </em>
                    </span>
                </td></tr>
                <tr><td><xsl:text> </xsl:text></td><td>
                        <table border="0" cellpadding="2" cellspacing="1">
                <xsl:for-each select="//owner[@selected='true']">
                    <xsl:variable name="ownerkey" select="@token"/>
                    <xsl:variable name="activities" select="activity[@selected='true']"/>


                        	<tr>
                        	<td rowspan="2" align="center" valign="middle"><span class="uportal-channel-strong">
                        		<xsl:text>Besitzer:</xsl:text></span><br/>
                        		<span class="uportal-channel-text">
                        		<xsl:value-of select="@name"/></span></td>
                        	<td colspan="{count($activities)}" class="uportal-background-med" align="center">
                        		<span class="uportal-channel-strong">
                        		<xsl:text>Aktivitäten:</xsl:text>
                        		</span>
                        	</td></tr>
                            <tr>
                            <xsl:for-each select="$activities">
                            	<td class="uportal-background-med">
                                <span class="uportal-channel-text"><xsl:value-of select="@name"/></span>
                                </td>
                            </xsl:for-each>
                            </tr>
                            <tr>
                            <td class="uportal-background-light">
                            	<span class="uportal-channel-strong">
                            	<xsl:text>Ziele:</xsl:text>
                            	</span>
                            </td>
                            <td class="uportal-background-dark">
                                 <xsl:attribute name="colspan">
                                  <xsl:value-of select="count($activities)"/>
                                </xsl:attribute>
                                <xsl:text> </xsl:text>
                              </td>
                            </tr>

                            <xsl:for-each select ="target[@selected='true']">
                                <xsl:variable name="targetkey" select="@token"/>
                                <tr>
                                <td class="uportal-background-light">
                                <span class="uportal-channel-text">
                                <xsl:value-of select="@name"/>
                                </span>
                                </td>
                                    <xsl:for-each select="$activities">
                                        <xsl:call-template name="permissionCell">
                                            <xsl:with-param name="activitykey">
                                                <xsl:value-of select="@token"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="ownerkey">
                                                <xsl:value-of select="$ownerkey"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="targetkey">
                                                <xsl:value-of select="$targetkey"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="principalkey">
                                                <xsl:value-of select="$principalkey"/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </tr>
                            </xsl:for-each>
                        	<xsl:if test="not(position()=last())">
								<xsl:call-template name="spacer"/>
							</xsl:if>
                </xsl:for-each>
                </table>
                    </td></tr>
                <xsl:if test="not(position()=last())">
                	<xsl:call-template name="hr"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:template>

        <xsl:template name="assignByOwner">
            <input type="hidden" name="prmCommand" value="AssignPermissions"/>
            <xsl:for-each select="//owner[@selected='true']">
                <xsl:variable name="ownerkey" select="@token"/>
                <xsl:variable name="activities" select="activity[@selected='true']"/>
                 <tr><td colspan="2">
                	<span class="uportal-channel-table-header">
                    <xsl:value-of select="@name"/>
                    </span>
                    <span class="uportal-channel-text">
                    <br/>
                    <em>
                    	<xsl:text> Weisen Sie Erlaubnisse für die Ziele dieses Inhabers zu: </xsl:text>
                    </em>
                    </span>
                </td></tr>
                <tr><td><xsl:text> </xsl:text></td><td>
                    	<table border="0" cellpadding="2" cellspacing="1">
                <xsl:for-each select="target[@selected='true']">
                    <xsl:variable name="targetkey" select="@token"/>

                        	<tr>
                        	<td rowspan="2" align="center" valign="middle"><span class="uportal-channel-strong">
                        		<xsl:text>Ziel:</xsl:text></span><br/>
                        		<span class="uportal-channel-text">
                        		<xsl:value-of select="@name"/></span></td>
                        	<td colspan="{count($activities)}" class="uportal-background-med" align="center">
                        		<span class="uportal-channel-strong">
                        		<xsl:text>Aktivitäten:</xsl:text>
                        		</span>
                        	</td></tr>
                            <tr>
                            <xsl:for-each select="$activities">
                            	<td class="uportal-background-med">
                                <span class="uportal-channel-text"><xsl:value-of select="@name"/></span>
                                </td>
                            </xsl:for-each>
                            </tr>
                            <tr>
                            <td class="uportal-background-light">
                            	<span class="uportal-channel-strong">
                            	<xsl:text>Direktion:</xsl:text>
                            	</span>
                            </td>
                            	<td class="uportal-background-dark">
                                <xsl:attribute name="colspan">
                                  <xsl:value-of select="count($activities)"/>
                                </xsl:attribute>
                                <xsl:text> </xsl:text>
                                </td>
                            </tr>
                            <xsl:for-each select ="//principal">
                                <xsl:variable name="principalkey" select="@token"/>
                                <tr><td class="uportal-background-light">
                                		<span class="uportal-channel-text">
										  <xsl:value-of select="@name"/>
										   <xsl:call-template name="principalTypeDisplay">
											<xsl:with-param name="type">
											  <xsl:value-of select="@type"/>
											</xsl:with-param>
										  </xsl:call-template>
										 </span>
                                    </td>
                                    <xsl:for-each select="$activities">
                                        <xsl:call-template name="permissionCell">
                                            <xsl:with-param name="activitykey">
                                                <xsl:value-of select="@token"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="ownerkey">
                                                <xsl:value-of select="$ownerkey"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="targetkey">
                                                <xsl:value-of select="$targetkey"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="principalkey">
                                                <xsl:value-of select="$principalkey"/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:for-each>
                                </tr>
                            </xsl:for-each>
                        <xsl:if test="not(position()=last())">
							<xsl:call-template name="spacer"/>
						</xsl:if>
                 </xsl:for-each>
                 </table>
                    </td></tr>
                <xsl:if test="not(position()=last())">
                    <xsl:call-template name="hr"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:template>

        <xsl:template name="principalTypeDisplay">
          <xsl:param name="type"/>
          <xsl:choose>
            <xsl:when test="$type='org.jasig.portal.security.IPerson'">
              <xsl:text> (Person) </xsl:text>
            </xsl:when>
            <xsl:when test="$type='org.jasig.portal.groups.IEntityGroup'">
              <xsl:text> (Gruppe) </xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text> (?) </xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:template>

        <xsl:template name="permissionCell">
            <xsl:param name="activitykey"/>
            <xsl:param name="ownerkey"/>
            <xsl:param name="principalkey"/>
            <xsl:param name="targetkey"/>

			<xsl:variable name="perm" select="key('perms',concat($principalkey,'|',$ownerkey,'|',$activitykey,'|',$targetkey))"/>

            <td class="uportal-background-dark" align="center" valign="middle">
                <select name="permission//{$ownerkey}|{$principalkey}|{$activitykey}|{$targetkey}" class="uportal-button">
                    <option>
                        <xsl:if test="(not($perm) or not($perm/@principal=$principalkey))">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="$perm and ($perm[@principal!=$principalkey]/@type)">
                            <xsl:text>Übernehmer </xsl:text>
                            <xsl:choose>
                            	<xsl:when test="($perm[@principal!=$principalkey]/@type='GRANT') and ($perm[@principal!=$principalkey]/@type='DENY')">
                            		<xsl:text>Beide</xsl:text>
                            	</xsl:when>
                            	<xsl:otherwise>
                            		<xsl:value-of select="$perm[@principal!=$principalkey]/@type"/>
                            	</xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </option>
                    <option>
                        <xsl:if test="(($perm) and ($perm[@principal=$principalkey]/@type='GRANT') and not($perm[@principal=$principalkey]/@type='DENY'))">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:text>BEWILLIGUNG</xsl:text>
                    </option>
                    <option>
                        <xsl:if test="(($perm) and ($perm[@principal=$principalkey]/@type='DENY') and not($perm[@principal=$principalkey]/@type='GRANT'))">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:text>VERWEIGERN</xsl:text>
                    </option>
                </select>
            </td>
        </xsl:template>

        <xsl:template name="header">
            <xsl:if test="$commandResponse != 'null'">
              <tr><td colspan="2" class="uportal-channel-warning">
                <xsl:value-of select="$commandResponse"/>
              </td>
              </tr>
              <xsl:call-template name="hr"/>
            </xsl:if>
            <form action="{$baseActionURL}" method="post">
            <input type="hidden" name="uP_root" value="me"/>
            <tr><td colspan="2">
                <table width="100%"><tr><td width="33%" class="uportal-channel-title">
                <xsl:value-of select="$prmView"/>
                </td>
                <xsl:if test="$prmView='Assign By Principal'">
                    <td width="33%">
                        <input type="submit" name="prmView" value="Assign By Owner" class="uportal-button"/>
                    </td>
                </xsl:if>
                <xsl:if test="$prmView='Assign By Owner'">
                    <td width="33%">
                        <input type="submit" name="prmView" value="Assign By Principal" class="uportal-button"/>
                    </td>
                </xsl:if>
                    <td width="33%" class="uport-table-header">
                        <input type="submit" name="prmCommand" value="Cancel" class="uportal-button"/>
                    </td>
                </tr></table>
            </td>
            </tr>
            </form>
            <xsl:call-template name="hr"/>
        </xsl:template>

        <xsl:template name="selectfooter">
            <xsl:call-template name="hr"/>
            <tr><td colspan="2">
                <input type="hidden" name="uP_root" value="me"/>
                <input type="submit" value="Submit" class="uportal-button"/>
                <xsl:text> </xsl:text>
                <input type="reset" value="Reset Form" class="uportal-button"/>
            </td></tr>
        </xsl:template>

        <xsl:template name="hr">
            <tr><td colspan="2" align="center">
                <table width="90%" border="0" cellspacing="0" cellpadding="0"><tr><td height="1" width="5" class="uportal-background-highlight"><xsl:text> </xsl:text></td></tr></table>
             </td></tr>
        </xsl:template>

        <xsl:template name="spacer">
            <tr><td colspan="2" align="center">
                <table width="90%" border="0" cellspacing="0" cellpadding="0"><tr><td height="5" width="5"><xsl:text> </xsl:text></td></tr></table>
             </td></tr>
        </xsl:template>

        <xsl:template match="owner">
            <input type="hidden" name="prmCommand" value="SelectOwners"/>
            <tr>
                <td><input type="checkbox" name="owner//{@ipermissible}"/></td>
                <td class="uportal-channel-text"><xsl:value-of select="@name"/></td>
            </tr>
        </xsl:template>

        <xsl:template name="activities">
            <input type="hidden" name="prmCommand" value="SelectActivities"/>
            <xsl:for-each select="//owner[@selected='true']">
                <td colspan="2" class="uportal-channel-table-header">
                    <xsl:value-of select="@name"/>
                </td>
                <xsl:for-each select="activity">
                    <tr><td>
                      <input type="checkbox" name="activity//{parent::owner/@ipermissible}|{@token}">
                        <xsl:if test="@selected='true'">
                          <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                      </input>
                    </td>
                    <td class="uportal-channel-text"><xsl:value-of select="@name"/></td>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:template>

        <xsl:template name="targets">
            <input type="hidden" name="prmCommand" value="SelectTargets"/>
            <xsl:for-each select="//owner[@selected='true']">
                <td colspan="2" class="uportal-channel-table-header">
                    <xsl:value-of select="@name"/>
                </td>
                <xsl:for-each select="target">
                    <tr><td>
                      <input type="checkbox" name="target//{parent::owner/@ipermissible}|{@token}">
                        <xsl:if test="@selected='true'">
                          <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                      </input>
                    </td>
                    <td class="uportal-channel-text"><xsl:value-of select="@name"/></td>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:template>
</xsl:stylesheet>
