<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xmsg="urn:x-lexica:xmsg:message:1.0">
  <xsl:output method="html" indent="yes" />
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="highlightedGroupID" select="false()"/>
  <xsl:param name="rootViewGroupID">0</xsl:param>
  <xsl:param name="mode" />
  <xsl:param name="customMessage" select="false()"/>
  <xsl:param name="feedback" select="false()"/>
  <xsl:param name="ignorePermissions" select="false()"/>
  <xsl:param name="blockFinishActions" select="false()"/>
  <xsl:param name="blockEntitySelect" select="false()"/>  
  <xsl:param name="grpServantMode">false </xsl:param>
  <xsl:param name="spacerIMG">media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif</xsl:param>
  <xsl:param name="pageSize" select="number(3)"/>
  <xsl:param name="page"/>
  <xsl:param name="mediaBase">media/org/jasig/portal/channels/groupsmanager</xsl:param>
  <xsl:key name="can" match="//principal/permission[@type='GRANT']" use="concat(@activity,'|',@target)" />
  <xsl:key name="selectedGroup" match="group[@selected='true']" use="@key"/>
  <xsl:key name="selectedEntity" match="entity[@selected='true']" use="@key"/>

  <xsl:template match="/">
  		<SCRIPT LANGUAGE='JavaScript1.2' TYPE='text/javascript'>
		function grpRemoveMember(path,member,group){
			if (window.confirm('Are you sure you want to remove \''+member+'\' from \''+group+'\'?')){
				this.location.href=path;
			}
		}
		function grpDeleteGroup(path){
			if (window.confirm('Are you sure you want to permanently delete this group, all its permissions and memberships?')){
				this.location.href=path;
			}
		}
		</SCRIPT>
      <table cellspacing="0" cellpadding="0">
      	<xsl:if test="$feedback">
          <tr>
            <td colspan="3" class="uportal-channel-warning">
              <xsl:value-of select="$feedback" />
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="$mode='select'">
          <tr>
            <td colspan="5">
            	<span class="uportal-channel-table-header">
              <xsl:choose>
                <xsl:when test="$customMessage">
                    <xsl:value-of select="$customMessage"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>
                    Select Groups and Entities:
                  </xsl:text>
                </xsl:otherwise>
              </xsl:choose>
              </span>
            </td>
          </tr>
        </xsl:if>
        
    
        <!--
        <tr>
        	<td colspan="6" class="uportal-background-dark">
        		<img src="{$spacerIMG}" width="1" height="1"/>
        	</td>
        </tr>
        -->
        <tr>
        	<!--
        	<td rowspan="4" class="uportal-background-dark">
        		<img src="{$spacerIMG}" width="1" height="1"/>
        	</td>
        	-->
        	<td rowspan="3" valign="top">
        		<xsl:call-template name="tree"/>
        	</td>
        	<xsl:if test="//group[@id=$highlightedGroupID]">
				<td class="uportal-background-highlight" rowspan="3" width="2"><img src="{$spacerIMG}" width="2" height="2"/></td>
				<td class="uportal-background-highlight" height="2"><img src="{$spacerIMG}" width="2" height="2"/></td>
				<td class="uportal-background-highlight" rowspan="3" width="2">
					<img src="{$spacerIMG}" width="2" height="2"/>
				</td>
        	</xsl:if>
        	<!--
        	<td rowspan="4" class="uportal-background-dark">
        		<img src="{$spacerIMG}" width="1" height="1"/>
        	</td>
        	-->
        </tr>
        <tr>
        	<td valign="top">
        		<xsl:if test="//group[@id=$highlightedGroupID]">
        		<xsl:call-template name="rightPane">
        			<xsl:with-param name="group" select="//group[@id=$highlightedGroupID]"/>
        		</xsl:call-template>
        		</xsl:if>
        	</td>
        </tr>
        <tr>
        	<td height="2">
        		<xsl:if test="//group[@id=$highlightedGroupID]">
        			<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
        			<img src="{$spacerIMG}" width="2" height="2"/>
        		</xsl:if>
        	</td>
        </tr>
        <tr>
        	<td height="10">
        			<img src="{$spacerIMG}" width="2" height="10"/>
        	</td>
        </tr>
        
        
        <!--
        <tr>
        	<td colspan="4" class="uportal-background-dark">
        		<img src="{$spacerIMG}" width="1" height="1"/>
        	</td>
        </tr>
        -->
        
        <xsl:if test="not($mode='edit')">
        	<xsl:call-template name="hrow">
				<xsl:with-param name="width" select="5"/>
			</xsl:call-template>
			<tr><td height="5"><img src="{$spacerIMG}" width="2" height="5"/></td></tr>
			<form action="{$baseActionURL}">
				<input type="hidden" name="grpCommand" value="Search"/>
				<tr>
				<td colspan="5" nowrap="nowrap" class="uportal-channel-text">
				Search for a 
				<select class="uportal-button" name="grpType">
					<xsl:variable name="stype" select="//group[@id=$rootViewGroupID]/@entityType"/>
					<xsl:for-each select="/CGroupsManager/entityTypes/entityType">
						<xsl:if test="not($stype) or @type=$stype"> 
						<option value="{@type}">
						<xsl:value-of select="@name"/>
						</option>
						<option value="IEntityGroup::{@type}">
							<xsl:value-of select="concat('Group of ',@name,'s')"/>
						</option>
						</xsl:if>
					</xsl:for-each>
				</select>
				that 
				<select class="uportal-button" name="grpMethod">
					<option value="1">is</option>
					<option value="2">starts with</option>
					<option value="3">ends with</option>
					<option value="4">contains</option>
				</select>
				<input type="text" size="10" name="grpQuery"/>
				<input type="submit" class="uportal-button" value="Go"/>
				</td>
				</tr>
				<xsl:if test="//group[(@id=$highlightedGroupID) and not(@id=0) and not(@searchResults='true')]"> 
					<tr>
					<td colspan="5" class="uportal-channel-text">
					<input type="checkbox" name="grpCommandArg" value="{//group[@id=$highlightedGroupID]/@key}"/>
					<em>search only descendants of the selected group</em>
					</td>
					</tr>
				</xsl:if>
			</form>
			<tr><td height="5"><img src="{$spacerIMG}" width="2" height="5"/></td></tr>
			<xsl:call-template name="hrow">
				<xsl:with-param name="width" select="5"/>
			</xsl:call-template>
			<tr><td height="10"><img src="{$spacerIMG}" width="2" height="10"/></td></tr>
		</xsl:if>
        <form action="{$baseActionURL}" method="POST">
        	<xsl:if test="$mode='select'">
				 <xsl:if test="count(descendant::*[@selected='true'])">
				  
					<xsl:if test="count(descendant::group[@selected='true'])">
					  <tr>
						<td colspan="5" class="uportal-channel-table-header">
						  Selected Groups:
						</td>
					  </tr>
					  <tr><td colspan="5">
					  <table>
					  <xsl:for-each select="descendant::group[@selected='true']">
						<xsl:variable name="id">
						  <xsl:value-of select="@id" />
						</xsl:variable>
						<xsl:if test="count(preceding::group[@selected='true' and @id=$id])=0">
						  <tr>
							<td align="center" valign="top">
							  <input type="checkbox" name="grpDeselect//{@id}|group" value="true" />
							</td>
							<td width="100%" class="uportal-channel-table-row-even">
							  <xsl:value-of select="RDF/Description/title" />
							</td>
						  </tr>
						</xsl:if>
					  </xsl:for-each>
					  </table></td></tr>
					</xsl:if>
					<xsl:if test="count(descendant::entity[@selected='true'])">
					  <tr>
						<td colspan="5" class="uportal-channel-table-header">
						  Selected Entities:
						</td>
					  </tr>
					  <tr><td colspan="5">
					  <table>
					  <xsl:for-each select="descendant::entity[@selected='true']">
						<xsl:variable name="id">
						  <xsl:value-of select="@id" />
						</xsl:variable>
						<xsl:if test="count(preceding::entity[@selected='true'][@id=$id])=0">
						  <tr>
							<td align="center" valign="top">
							  <input type="checkbox" name="grpDeselect//{@id}|entity" value="true" />
							</td>
							<td width="100%" class="uportal-channel-table-row-odd">
							  <xsl:value-of select="@displayName" />
							</td>
						  </tr>
						</xsl:if>
					  </xsl:for-each>
					  </table></td></tr>
					</xsl:if>
					
				</xsl:if>
				<tr>
				  <td colspan="5">
					<xsl:if test="count(descendant::*[@selected='true'])">
						<input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Deselect';" value="Deselect" class="uportal-button" />
					</xsl:if>
					<xsl:if test="not($blockFinishActions)">
						<xsl:if test="count(descendant::*[@selected='true'])">
						  <xsl:text>
						  </xsl:text>
						  <input type="submit" class="uportal-button"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Done';" value="Done with Selection" />
						</xsl:if>
					  <xsl:text>
					  </xsl:text>
					  <input type="submit" class="uportal-button"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Cancel';" value="Cancel Selection" />
					</xsl:if>
				  </td>
				</tr>
            </xsl:if>
          </form>
        </table>
 
  </xsl:template>
  
  <xsl:template name="rightPaneButtons">
  	<xsl:param name="group"/>
  	<td align="right">
          	<xsl:choose>
          		<xsl:when test="$mode='select'"/>
          		<xsl:when test="$group/@searchResults='true'">
          			<a href="javascript:this.location.href='{$baseActionURL}?grpCommand=Delete&amp;grpCommandArg={$group/@id}';"><img width="16" height="16" border="0" hspace="1" src="{$mediaBase}/delete.gif" alt="Delete Group"/></a>
          		</xsl:when>
          		<xsl:when test="$mode='edit'">
          			<a href="{$baseActionURL}?grpCommand=Unlock&amp;grpCommandArg={$group/@id}"><img width="16" height="16" border="0" hspace="1" src="{$mediaBase}/unlock.gif" alt="Unlock Group"/></a>
          			<xsl:if test="$ignorePermissions or key('can',concat('DELETE','|',$group/@key))">
						<a href="javascript:grpDeleteGroup('{$baseActionURL}?grpCommand=Delete&amp;grpCommandArg={$group/@id}');"><img width="16" height="16" border="0" hspace="1" src="{$mediaBase}/delete.gif" alt="Delete Group"/></a>
					</xsl:if>
          		</xsl:when>
          		<xsl:when test="$ignorePermissions or key('can',concat('UPDATE','|',$group/@key)) or key('can',concat('ASSIGNPERMISSIONS','|',$group/@key)) or key('can',concat('ADD/REMOVE','|',$group/@key)) or key('can',concat('CREATE','|',$group/@key))">
          			<a href="{$baseActionURL}?grpCommand=Lock&amp;grpCommandArg={$group/@id}"><img width="16" height="16" border="0" hspace="1" src="{$mediaBase}/lock.gif" alt="Lock Group"/></a>
          		</xsl:when>
          	</xsl:choose>
          	
          	<a href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg="><img width="16" height="16" border="0" hspace="1" src="{$mediaBase}/close.gif" alt="Close Group"/></a>
          </td>
  </xsl:template>
  
  <xsl:template name="rightPane">
  	<xsl:param name="group"/>
    <xsl:variable name="grpKey" select="@key" />
	<table width="100%" border="0">
	<form action="{$baseActionURL}" method="POST">
    <xsl:choose>
      <xsl:when test="$highlightedGroupID='0'">
        <tr>
          <td colspan="2" class="uportal-channel-strong">
            <xsl:text>
              My Groups
            </xsl:text>
          </td>
          <!--
          <xsl:call-template name="rightPaneButtons">
          	<xsl:with-param name="group" select="$group"/>
          </xsl:call-template>
          -->
        </tr>
      </xsl:when>
      <xsl:otherwise>
      	
          <input type="hidden" name="grpCommandArg" value="{$highlightedGroupID}" />
        <tr>
          <td colspan="2" class="uportal-channel-strong" width="100%">
            <xsl:text>
              Group Name:
            </xsl:text>
          </td>
          <xsl:call-template name="rightPaneButtons">
          	<xsl:with-param name="group" select="$group"/>
          </xsl:call-template>
        </tr>
        
          <tr>
            <td>
            		<xsl:choose>
							<xsl:when test="($mode='select') and not($group/@searchResults='true')">
							  
								<xsl:if test="not($group/@id=0) and ($ignorePermissions or key('can',concat('SELECT','|',$group/@key)))">
								  <xsl:choose>
									<xsl:when test="($group/@selected='true') or (key('selectedGroup',$group/@key))">
									  <span class="uportal-channel-warning">
										<xsl:text>
										  X
										</xsl:text>
									  </span>
									</xsl:when>
									<xsl:otherwise>
									  <input type="checkbox" name="grpSelect//{$group/@id}|group" value="true" />
									</xsl:otherwise>
								  </xsl:choose>
								</xsl:if>
							  
							</xsl:when>
							<xsl:otherwise>
							  <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
							</xsl:otherwise>
						</xsl:choose>
            
             <!-- <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" /> -->
            </td>
            <td class="uportal-text" colspan="2">
              <xsl:choose>
                <xsl:when test="not($mode='edit') or (not($ignorePermissions) and not(key('can',concat('UPDATE','|',$group/@key))))">
                  <xsl:value-of select="$group/RDF/Description/title" />
                </xsl:when>
                <xsl:otherwise>
                  <input type="text" size="40" maxsize="255" name="grpName" class="uportal-channel-text">
                    <xsl:attribute name="value">
                      <xsl:value-of select="$group/RDF/Description/title" />
                    </xsl:attribute>
                  </input>
                </xsl:otherwise>
              </xsl:choose>
            </td>

          </tr>
        
        <tr>
          <td colspan="3" class="uportal-channel-strong">
            <xsl:text>
              Group Description:
            </xsl:text>
          </td>
        </tr>
        <tr>
          <td>
          </td>
          <td class="uportal-text" colspan="2">
          	<xsl:choose>
                <xsl:when test="not($mode='edit') or (not($ignorePermissions) and not(key('can',concat('UPDATE','|',$group/@key))))">
                  <xsl:value-of select="$group/RDF/Description/description" />
                </xsl:when>
                <xsl:otherwise>
                  <textarea  cols="60" rows="5" name="grpDescription" class="uportal-channel-text">
                      <xsl:value-of select="$group/RDF/Description/description" />
                  </textarea>
                </xsl:otherwise>
              </xsl:choose>
          </td>
        </tr>
        <xsl:if test="$mode='edit'">
			<tr>	
				<td></td>
				<td nowrap="nowrap" colspan="2">
					<xsl:if test="$ignorePermissions or key('can',concat('UPDATE','|',$group/@key))">
						<input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Update';" value="Update" class="uportal-button" />
						<input type="reset" value="Reset Form"  class="uportal-button" />
					  </xsl:if>
					 <img src="$spacerIMG" width="15" height="1"/>
					<xsl:if test="not($highlightedGroupID='0') and not($grpKey='null') and ($ignorePermissions or key('can',concat('ASSIGNPERMISSIONS','|',$grpKey)))">
						<input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Permissions';" value="Assign Permissions" class="uportal-button" />
					  </xsl:if>
					<xsl:if test="$ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true')">
						<input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Add';" value="Add Members" class="uportal-button" />
					</xsl:if>
				</td>
			</tr>
			<xsl:if test="not($grpServantMode='true')">
			  <xsl:if test="$ignorePermissions or key('can',concat('CREATE','|',$grpKey))">
				<form action="{$baseActionURL}" method="POST">
				  <input type="hidden" name="grpCommand" value="Create" /> <input type="hidden" name="grpCommandArg" value="{$highlightedGroupID}" />
				  <tr>
					<td>
					  <xsl:text>
					  </xsl:text>
					</td>
					<td colspan="2" nowrap="true">
					  <input type="submit" value="Create New Member Group" class="uportal-button" />
					  <xsl:text>
					  </xsl:text>
					  <input type="text" size="25" name="grpName" value="(new group name)" class="uportal-channel-text" />
					</td>
				  </tr>
				</form>
			  </xsl:if>
			</xsl:if>
        </xsl:if>
       <!-- </form>-->
        
      </xsl:otherwise>
    </xsl:choose>
    
    <tr><td><img src="{$spacerIMG}" height="10" width="1"/>
    </td></tr>
    <xsl:call-template name="hrow"/>
    <xsl:variable name="siblingCount" select="count($group/node()[name()='group' or name()='entity'])"/>
    <tr>
	  <td colspan="3">
	  	<table width="100%" border="0">
    			<tr>
    				<td class="uportal-channel-strong">
    					<xsl:text>Members</xsl:text>
    				</td>
    				<td width="50%">
					<img src="{$spacerIMG}" width="16" height="16" border="0"/>
					</td>
    				<td align="left">
    					<xsl:choose>
							<xsl:when test="$page &gt; 1">
							<a href="{$baseActionURL}?grpPageBack={$page - 1}"><img width="16" height="16" src="{$mediaBase}/first.gif" border="0" hspace="1" vspace="0"/></a>
							</xsl:when>
							<xsl:otherwise>
								<img src="{$spacerIMG}" width="16" height="16" border="0" hspace="1" vspace="0"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>	
					<td align="left">
						<xsl:choose>
							<xsl:when test="$page &gt; 1">
							<a href="{$baseActionURL}?grpPageBack=1"><img width="16" height="16" src="{$mediaBase}/back.gif" border="0" hspace="1" vspace="0"/></a>
							</xsl:when>
							<xsl:otherwise>
								<img src="{$spacerIMG}" width="16" height="16" border="0" hspace="1" vspace="0"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>	
					<td>
					<img src="{$spacerIMG}" width="16" height="16" border="0"/>
					</td>
					<td align="middle" class="uportal-channel-text" nowrap="nowrap">
						<xsl:value-of select="concat($page,' / ',ceiling($siblingCount div $pageSize))"/>
					</td>
					<td>
					<img src="{$spacerIMG}" width="16" height="16" border="0"/>
					</td>
					<td align="right">
						<xsl:choose>
							<xsl:when test="($pageSize*$page) &lt; $siblingCount">
							<a href="{$baseActionURL}?grpPageForward=1"><img width="16" height="16" src="{$mediaBase}/forward.gif" border="0" hspace="1" vspace="0"/></a>
							</xsl:when>
							<xsl:otherwise>
								<img src="{$spacerIMG}" width="16" height="16" border="0" hspace="1" vspace="0"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td align="right">
						<xsl:choose>
							<xsl:when test="($pageSize*$page) &lt; $siblingCount">
							<a href="{$baseActionURL}?grpPageForward={ceiling($siblingCount div $pageSize) - $page}"><img width="16" height="16" src="{$mediaBase}/last.gif" border="0" hspace="1" vspace="0"/></a>
							</xsl:when>
							<xsl:otherwise>
								<img src="{$spacerIMG}" width="16" height="16" border="0" hspace="1" vspace="0"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td width="50%">
					<img src="{$spacerIMG}" width="16" height="16" border="0"/>
					</td>
				</tr>
    		</table>
		
		
		<!--<xsl:value-of select="concat(((($page - 1)*$pageSize)+1),' through ',(($page * $pageSize)),' of ',$siblingCount,':')"/>-->
	  </td>
	</tr>

    <xsl:call-template name="hrow"/>
    <!--<form action="{$baseActionURL}" method="POST">-->
    <xsl:for-each select="$group/node()[name()='group' or name()='entity']">
    	<xsl:sort data-type="text" order="descending" select="name()"/>
    	<xsl:sort data-type="text" order="ascending" select="RDF/Description/title"/>
    	<xsl:sort data-type="text" order="ascending" select="@displayName"/>
    	<xsl:if test="(position() &gt; (($page - 1)*$pageSize)) and (position() &lt; (($page * $pageSize)+1))">
			<xsl:if test="name()='group'">
				<xsl:if test="$ignorePermissions or key('can',concat('VIEW','|',@key))">
				  <tr>
					<td align="center" valign="top">
						<xsl:choose>
							<xsl:when test="$mode='select'">
							  
								<xsl:if test="not(@id=0) and ($ignorePermissions or key('can',concat('SELECT','|',@key)))">
								  <xsl:choose>
									<xsl:when test="(@selected='true') or (key('selectedGroup',@key))">
									  <span class="uportal-channel-warning">
										<xsl:text>
										  X
										</xsl:text>
									  </span>
									</xsl:when>
									<xsl:otherwise>
									  <input type="checkbox" name="grpSelect//{@id}|group" value="true" />
									</xsl:otherwise>
								  </xsl:choose>
								</xsl:if>
							  
							</xsl:when>
							<xsl:otherwise>
							  <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td width="100%" class="uportal-channel-table-row-even">
						<a href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg={@id}"> <span class="uportal-channel-table-row-even"><strong>
							  <xsl:value-of select="RDF/Description/title" /></strong>
							</span> </a>
					  <!--<xsl:choose>
						<xsl:when test="not($grpServantMode='true')">
						  <a href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg={@id}"> <span class="uportal-channel-table-row-even"><strong>
							  <xsl:value-of select="RDF/Description/title" /></strong>
							</span> </a>
						</xsl:when>
						<xsl:otherwise>
						  <xsl:value-of select="RDF/Description/title" />
						</xsl:otherwise>
					  </xsl:choose>-->
					</td>
					<td align="right" valign="top" class="uportal-channel-table-row-even" nowrap="nowrap">
					  <xsl:if test="$mode='edit' and ($ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true'))">
						<a href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}','{RDF/Description/title}','{parent::group/RDF/Description/title}');">
						<img src="{$mediaBase}/remove.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top" alt="Remove Member" />
						</a>
					  </xsl:if>
					  <xsl:choose>
						<xsl:when test="properties">
							<a href="{$baseActionURL}?grpCommand=HideProperties&amp;grpCommandArg={@id}"><img src="{$mediaBase}/hideinfo.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?grpCommand=ShowProperties&amp;grpCommandArg={@id}"><img src="{$mediaBase}/showinfo.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top"/></a>
				  		</xsl:otherwise>
				  	  </xsl:choose>
				  	</td>
				  </tr>
				  <xsl:if test="properties">
				  <tr><td></td>
				  	<td colspan="2" class="uportal-channel-table-row-even">
				  	<xsl:choose>
				  		<xsl:when test="properties/property">
							<table border="1">
								<xsl:for-each select="properties/property">
									<tr><td class="uportal-channel-table-row-even"><xsl:value-of select="@name"/></td>
									<td class="uportal-channel-table-row-even"><xsl:value-of select="@value"/></td></tr>
								</xsl:for-each>
							</table>
						</xsl:when>
						<xsl:otherwise>
							<em>No additional information available</em>
						</xsl:otherwise>
					</xsl:choose>
				  	</td>
				  	</tr>
				  </xsl:if>
				  <xsl:call-template name="hrow"/>
				</xsl:if>
			</xsl:if>
			<xsl:if test="name()='entity'">
				<tr>
				  <td>
				  	<xsl:choose>
				  		<xsl:when test="$mode='select'">
						   <xsl:if test="($ignorePermissions or key('can',concat('SELECT','|',parent::group/@key))) and not($blockEntitySelect)">
							<xsl:choose>
							  <xsl:when test="(@selected='true') or key('selectedEntity',@key)">
								<span class="uportal-channel-warning">
								  <xsl:text>
									X
								  </xsl:text>
								</span>
							  </xsl:when>
							  <xsl:otherwise>
								<input type="checkbox" name="grpSelect//{@id}|entity" value="true" />
							  </xsl:otherwise>
							</xsl:choose>
						  </xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
						</xsl:otherwise>
					</xsl:choose>
				  </td>
				  <td width="100%" class="uportal-channel-table-row-odd">
				  	<strong>
					<xsl:value-of select="@displayName" />
					</strong>
				  </td>
				  <td align="right" valign="top" class="uportal-channel-table-row-odd">
					<xsl:if test="$mode='edit' and ($ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true'))">
					  <a href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}','{@displayName}','{parent::group/RDF/Description/title}');">
					  	<img src="{$mediaBase}/remove.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top" alt="Remove Member" />
					  </a>
					</xsl:if>
					<xsl:choose>
						<xsl:when test="properties">
							<a href="{$baseActionURL}?grpCommand=HideProperties&amp;grpCommandArg={@id}"><img src="{$mediaBase}/hideinfo.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?grpCommand=ShowProperties&amp;grpCommandArg={@id}"><img src="{$mediaBase}/showinfo.gif" height="16" width="16" hspace="1" vspace="1" border="0" align="top"/></a>
				  		</xsl:otherwise>
				  	</xsl:choose>
				  </td>
				</tr>
				 <xsl:if test="properties">
				  <tr><td></td>
				  	<td colspan="2" class="uportal-channel-table-row-odd">
				  	<xsl:choose>
				  		<xsl:when test="properties/property">
							<table border="0" cellspacing="2" cellpadding="0">
								<xsl:for-each select="properties/property">
									<tr>
									<xsl:if test="position()=1">
										<td rowspan="{count(parent::properties/property)}">
											<img src="{$spacerIMG}" height="5" width="14" />
										</td>
									</xsl:if>
									<td class="uportal-channel-table-row-odd" nowrap="nowrap" valign="top"><xsl:value-of select="@name"/></td>
									<td class="uportal-channel-table-row-odd" valign="top">=</td>
									<td width="100%" class="uportal-channel-table-row-odd" valign="top"><xsl:value-of select="@value"/></td></tr>
								</xsl:for-each>
							</table>
						</xsl:when>
						<xsl:otherwise>
							<em>No additional information available</em>
						</xsl:otherwise>
					</xsl:choose>
				  	</td>
				  	</tr>
				  </xsl:if>
				<xsl:call-template name="hrow"/>
			</xsl:if>
		</xsl:if>
    </xsl:for-each>
    <xsl:if test="$mode='select'">
            <tr>
              <td colspan="2">
                <input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Select';"  value="Select Marked" class="uportal-button" />
                
              </td>
            </tr>
            <xsl:call-template name="hrow"/>
          </xsl:if>
     </form>
    <!--
    <xsl:for-each select="$group/entity">
    	<xsl:sort data-type="text" order="ascending" select="@displayName"/>
		<tr>
		  <td>
			<img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
			<xsl:value-of select="$posCount"/>
		  </td>
		  <td width="100%" class="uportal-channel-table-row-odd">
			<xsl:value-of select="@displayName" />
		  </td>
		  <td align="right" valign="top" class="uportal-channel-table-row-odd">
			<xsl:if test="$mode='edit' and ($ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true'))">
			  <a href="{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}"> <span class="uportal-channel-table-row-odd">
				  <xsl:text>
					Remove
				  </xsl:text>
				</span> </a>
			</xsl:if>
		  </td>
		</tr>
		<xsl:variable name="posCount" select="$posCount+1"/>
    </xsl:for-each>
    -->
    
    <xsl:if test="($mode='edit') and ($grpServantMode='true')">
		<form action="{$baseActionURL}" method="POST">
		<input type="hidden" name="grpCommand" value="Cancel" />
		  <tr>
			<td>
			  <xsl:text>
			  </xsl:text>
			</td>
			<td colspan="2">
			  <input type="submit" value="Finished" class="uportal-button" />
			</td>
		  </tr>
		</form>
	</xsl:if>
    </table>
  </xsl:template>
  
  <xsl:template name="hrow">
  	<xsl:param name="width">3</xsl:param>
  	<tr><td colspan="3" align="center" height="1">
  		<table cellpadding="0" cellspacing="0" border="0" class="uportal-background-shadow" width="100%">
  			<tr><td height="1"><img src="$spacerIMG" height="1" width="1"/></td></tr>
  		</table>	
  	</td>
  	</tr>
  </xsl:template>

  
  <xsl:template name="tree">
        <form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="uP_root" value="me"/>
          <xsl:apply-templates select="//group[@id=$rootViewGroupID]" />
        </form>
  </xsl:template>
  
  <xsl:template match="group">
    <xsl:if test="$ignorePermissions or key('can',concat('VIEW','|',@key)) or (@id=0)">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">	
      <tr>
      	<!--
        <xsl:if test="$mode='select'">
          <td align="center" valign="top">
            <xsl:if test="not(@id=0) and ($ignorePermissions or key('can',concat('SELECT','|',@key)))">
              <xsl:choose>
                <xsl:when test="(@selected='true') or (key('selectedGroup',@key))">
                  <span class="uportal-channel-warning">
                    <xsl:text>
                      X
                    </xsl:text>
                  </span>
                </xsl:when>
                <xsl:otherwise>
                  <input type="checkbox" name="grpSelect//{@id}|group" value="true" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </td>
        </xsl:if>
        -->
        <td width="100%" colspan="2">
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
            	
              
              <td>
              	<xsl:choose>
              		<xsl:when test="(@expanded='true') and (@hasMembers='true') and (count(group) &gt; 0) and not(@id=0)">
					  <a href="{$baseActionURL}?grpCommand=Collapse&amp;grpCommandArg={@id}"> <img border="0" height="16" width="16" src="{$mediaBase}/expanded.gif" align="bottom" vspace="1" hspace="0" alt="Collapse Group"/> </a>
					</xsl:when>
              		<xsl:when test="(@expanded='false') and (@hasMembers='true') and not(@id=0)">
					  <a href="{$baseActionURL}?grpCommand=Expand&amp;grpCommandArg={@id}"> <img border="0" height="16" width="16" src="{$mediaBase}/collapsed.gif" align="bottom" vspace="1" hspace="0" alt="Expand Group"/> </a>
					</xsl:when>
              		<xsl:otherwise>
              			<img border="0" height="16" width="16" src="{$mediaBase}/empty.gif" align="bottom" vspace="1" hspace="0"/>
              		</xsl:otherwise>
              	</xsl:choose>
                
                
              </td>
              <td>
               	<xsl:if test="$highlightedGroupID and $highlightedGroupID=@id">
            		<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
            	</xsl:if>
            	<img border="0" height="2" width="2" src="{$spacerIMG}" vspace="0" hspace="0"/>
            	</td>
              <td width="100%" class="uportal-channel-table-row-even">
               	<xsl:if test="$highlightedGroupID and $highlightedGroupID=@id">
            		<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
            	</xsl:if>
                  <a href="{$baseActionURL}?uP_root=me&amp;grpCommand=Highlight&amp;grpCommandArg={@id}"> <span class="uportal-channel-table-row-even">
                      <xsl:value-of select="RDF/Description/title" />
                    </span> </a>
    
              </td>
              <td>
              	<xsl:if test="$highlightedGroupID and $highlightedGroupID=@id">
            		<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
            	</xsl:if>
                <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="10"/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      
      <!--
      <xsl:if test="@expanded='true'">
        <xsl:apply-templates select="entity">
          <xsl:with-param name="emptyWidth">
            <xsl:value-of select="($depth*14)+14" />
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:if>
      -->
      <xsl:if test="(@expanded='true') and (count(group) &gt; 0)">
      	<tr>
      		<td background="{$mediaBase}/dot.gif">
                <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="16"/>
              </td>
      		<td width="100%">
        <xsl:apply-templates select="group[not(@searchResults='true')]">
        	<xsl:sort data-type="text" order="ascending" select="RDF/Description/title"/>
        </xsl:apply-templates>
        <xsl:if test="@id=$rootViewGroupID">
			<xsl:apply-templates select="//group[@searchResults='true']">
				<xsl:sort data-type="number" order="ascending" select="@id"/>
			</xsl:apply-templates>
        </xsl:if>
        	</td>
        </tr>
      </xsl:if>
      	
      </table>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="entity">
    <xsl:param name="emptyWidth">
      1
    </xsl:param>
    <tr>
      <xsl:if test="$mode='select'">
        <td align="center" valign="top">
        	<!--
          <xsl:if test="($ignorePermissions or key('can',concat('SELECT','|',parent::group/@key))) and not($blockEntitySelect)">
            <xsl:choose>
              <xsl:when test="(@selected='true') or key('selectedEntity',@key)">
                <span class="uportal-channel-warning">
                  <xsl:text>
                    X
                  </xsl:text>
                </span>
              </xsl:when>
              <xsl:otherwise>
                <input type="checkbox" name="grpSelect//{@id}|entity" value="true" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
          -->
        </td>
      </xsl:if>
      <td width="100%">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr>
            <td>
              <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="{$emptyWidth}" />
            </td>
            <td class="uportal-channel-table-row-odd" width="100%">
              <xsl:value-of select="@displayName" />
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
