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
  <xsl:key name="can" match="//principal/permission[@type='GRANT']" use="concat(@activity,'|',@target)" />
  <xsl:key name="selectedGroup" match="group[@selected='true']" use="@key"/>
  <xsl:key name="selectedEntity" match="entity[@selected='true']" use="@key"/>

  <xsl:template match="/">
  		<SCRIPT LANGUAGE='JavaScript1.2' TYPE='text/javascript'>
		function grpRemoveMember(path){
			if (window.confirm('Are you sure you want to remove this member?')){
				this.location.href=path;
			}
		}
		function grpDeleteGroup(path,form){
			if (window.confirm('Are you sure you want to permanently delete this group, all its permissions and memberships?')){
				form.action = path;
				form.submit();
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
				<td class="uportal-background-highlight" rowspan="3"><img src="{$spacerIMG}" width="2" height="2"/></td>
				<td class="uportal-background-highlight" height="2"><img src="{$spacerIMG}" width="2" height="2"/></td>
				<td class="uportal-background-highlight" rowspan="3">
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
        <!--
        <tr>
        	<td colspan="4" class="uportal-background-dark">
        		<img src="{$spacerIMG}" width="1" height="1"/>
        	</td>
        </tr>
        -->
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
  
  <xsl:template name="rightPane">
  	<xsl:param name="group"/>
    <xsl:variable name="grpKey" select="@key" />
	<table width="100%" border="0">
    <xsl:choose>
      <xsl:when test="$highlightedGroupID='0'">
        <tr>
          <td colspan="3" class="uportal-channel-strong">
            <xsl:text>
              My Groups:
            </xsl:text>
          </td>
        </tr>
      </xsl:when>
      <xsl:otherwise>
      	<form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="grpCommandArg" value="{$highlightedGroupID}" />
        <tr>
          <td colspan="3" class="uportal-channel-strong">
            <xsl:text>
              Group Name:
            </xsl:text>
          </td>
        </tr>
        
          <tr>
            <td>
              <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5" width="14" />
            </td>
            <td class="uportal-text">
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
          <td class="uportal-text">
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
        <xsl:if test="not($mode='select')">
			<tr>	
				<td></td>
				<td>
					<xsl:choose>
					<xsl:when test="not($mode='edit') or (not($ignorePermissions) and not(key('can',concat('UPDATE','|',$group/@key))))">
					  <input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Lock';" value="Edit" class="uportal-button" />
					</xsl:when>
					<xsl:otherwise>
					  <xsl:if test="$ignorePermissions or key('can',concat('UPDATE','|',$group/@key))">
						<input type="submit" name="grpCommand" value="Update" class="uportal-button" />
						<input type="reset" value="Reset"  class="uportal-button" />
					  </xsl:if>
					  
					</xsl:otherwise>
				  </xsl:choose>
				  <xsl:if test="$ignorePermissions or key('can',concat('DELETE','|',$group/@key))">
					<input type="submit" onClick="javascript:grpDeleteGroup('{$baseActionURL}?grpCommand=Delete',this.form);" value="Delete" class="uportal-button" />
				  </xsl:if>
				  <xsl:if test="not($highlightedGroupID='0') and not($grpKey='null') and ($ignorePermissions or key('can',concat('ASSIGNPERMISSIONS','|',$grpKey)))">
					<input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Permissions';" value="Assign Permissions" class="uportal-button" />
				  </xsl:if>
				</td>
			</tr>
        </xsl:if>
        </form>
        
      </xsl:otherwise>
    </xsl:choose>
    <xsl:variable name="siblingCount" select="count($group/node()[name()='group' or name()='entity'])"/>
    <tr>
	  <td colspan="3" class="uportal-channel-strong">
		<xsl:text>
		  Members 
		</xsl:text>
		
		<!--<xsl:value-of select="concat(((($page - 1)*$pageSize)+1),' through ',(($page * $pageSize)),' of ',$siblingCount,':')"/>-->
	  </td>
	</tr>
	<tr>
    	<td></td>
    	<td>
    		<table width="100%">
    			<tr>
					<td align="left" width="33%">
						<xsl:if test="$page &gt; 1">
						<a href="{$baseActionURL}?grpPageBack=1"><span class="uportal-channel-warning">Back</span></a>
						</xsl:if>
					</td>	
					<td align="middle" class="uportal-channel-warning" width="34%" nowrap="nowrap">
						<xsl:value-of select="concat('page ',$page,' of ',ceiling($siblingCount div $pageSize))"/>
					</td>
					<td align="right" width="33%">
						<xsl:if test="($pageSize*$page) &lt; $siblingCount">
						<a href="{$baseActionURL}?grpPageForward=1"><span class="uportal-channel-warning">Forward</span></a>
						</xsl:if>
					</td>
				</tr>
    		</table>
    	</td>
    </tr>
    <form action="{$baseActionURL}" method="POST">
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
					  <xsl:choose>
						<xsl:when test="not($grpServantMode='true')">
						  <a href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg={@id}"> <span class="uportal-channel-table-row-even">
							  <xsl:value-of select="RDF/Description/title" />
							</span> </a>
						</xsl:when>
						<xsl:otherwise>
						  <xsl:value-of select="RDF/Description/title" />
						</xsl:otherwise>
					  </xsl:choose>
					</td>
					<td align="right" valign="top" class="uportal-channel-table-row-even">
					  <xsl:if test="$mode='edit' and ($ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true'))">
						<a href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}');"> <span class="uportal-channel-table-row-even">
							<xsl:text>
							  Remove
							</xsl:text>
						  </span> </a>
					  </xsl:if>
					</td>
				  </tr>
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
					<xsl:value-of select="@displayName" />
				  </td>
				  <td align="right" valign="top" class="uportal-channel-table-row-odd">
					<xsl:if test="$mode='edit' and ($ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true'))">
					  <a href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}');"> <span class="uportal-channel-table-row-odd">
						  <xsl:text>
							Remove
						  </xsl:text>
						</span> </a>
					</xsl:if>
				  </td>
				</tr>
			</xsl:if>
		</xsl:if>
    </xsl:for-each>
    <xsl:if test="$mode='select'">
            <tr>
              <td colspan="2">
                <input type="submit" name="grpCommand" value="Select" class="uportal-button" />
                
              </td>
            </tr>
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
    <xsl:if test="$mode='edit'">
		<xsl:call-template name="AddAndCreateRows">
		  <xsl:with-param name="grpKey" select="$grpKey" />
		</xsl:call-template>
	</xsl:if>
    </table>
  </xsl:template>

  <xsl:template name="AddAndCreateRows">
    <xsl:param name="grpKey">null </xsl:param>
    <xsl:if test="$ignorePermissions or key('can',concat('ADD/REMOVE','|',$grpKey)) or ($grpServantMode='true')">
      <form action="{$baseActionURL}" method="POST">
         <input type="hidden" name="grpCommand" value="Add" /> <input type="hidden" name="grpCommandArg" value="{$highlightedGroupID}" />
        <tr>
          <td>
            <xsl:text>
            </xsl:text>
          </td>
          <td colspan="2">
            <input type="submit" value="Add Members" class="uportal-button" />
          </td>
        </tr>
      </form>
    </xsl:if>
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
              <input type="submit" value="Create New Group" class="uportal-button" />
              <xsl:text>
              </xsl:text>
              <input type="text" size="20" name="grpName" value="(new group name)" class="uportal-channel-text" />
            </td>
          </tr>
        </form>
      </xsl:if>
    </xsl:if>
    <form action="{$baseActionURL}" method="POST">
      <xsl:choose>
        <xsl:when test="$grpServantMode='true'">
          <input type="hidden" name="grpCommand" value="Cancel" />
        </xsl:when>
        <xsl:otherwise>
        	<input type="hidden" name="grpCommand" value="Unlock" />
        </xsl:otherwise>
      </xsl:choose>
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
  </xsl:template>
  
  <xsl:template name="tree">
  	<table border="0" cellpadding="0" cellspacing="0" width="100%">
        
        <form action="{$baseActionURL}" method="POST">
          <input type="hidden" name="uP_root" value="me"/>
          <xsl:apply-templates select="//group[@id=$rootViewGroupID]" />
          <!--
          <xsl:if test="$mode='select'">
            <tr>
              <td colspan="2">
                <input type="submit" name="grpCommand" value="Select" class="uportal-button" />
                <xsl:if test="not($blockFinishActions)">
                  <xsl:text>
                  </xsl:text>
                  <input type="submit" class="uportal-button" name="grpCommand" value="Done" />
                  <xsl:text>
                  </xsl:text>
                  <input type="submit" class="uportal-button" name="grpCommand" value="Cancel Selection" />
                </xsl:if>
              </td>
            </tr>
          </xsl:if>
          -->
        </form>
       
      </table>
  </xsl:template>
  
  <xsl:template match="group">
    <xsl:param name="depth">
      1
    </xsl:param>
    <xsl:if test="$ignorePermissions or key('can',concat('VIEW','|',@key)) or (@id=0)">
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
        <td width="100%">
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
            	<xsl:if test="$highlightedGroupID and $highlightedGroupID=@id">
            		<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
            	</xsl:if>
              <td>
                <img src="media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif" height="5">
                  <xsl:attribute name="width">
                    <xsl:value-of select="$depth*14" />
                  </xsl:attribute>
                </img>
              </td>
              <td>
              	<xsl:choose>
              		<xsl:when test="(@expanded='true') and (@hasMembers='true') and (count(group) &gt; 0) and not(@id=0)">
					  <a href="{$baseActionURL}?grpCommand=Collapse&amp;grpCommandArg={@id}"> <img border="0" height="14" width="14" src="media/org/jasig/portal/channels/CUserPreferences/tab-column/arrow_down_image.gif" /> </a>
					</xsl:when>
              		<xsl:when test="(@expanded='false') and (@hasMembers='true') and not(@id=0)">
					  <a href="{$baseActionURL}?grpCommand=Expand&amp;grpCommandArg={@id}"> <img border="0" height="14" width="14" src="media/org/jasig/portal/channels/CUserPreferences/tab-column/arrow_right_image.gif" /> </a>
					</xsl:when>
              		<xsl:otherwise>
              			<img border="0" height="14" width="14" src="{$spacerIMG}" />
              		</xsl:otherwise>
              	</xsl:choose>
                
                
              </td>
              <td width="100%" class="uportal-channel-table-row-even">
               
                  <a href="{$baseActionURL}?uP_root=me&amp;grpCommand=Highlight&amp;grpCommandArg={@id}"> <span class="uportal-channel-table-row-even">
                      <xsl:value-of select="RDF/Description/title" />
                    </span> </a>
    
              </td>
              <td>
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
      <xsl:if test="@expanded='true'">
        <xsl:apply-templates select="group">
          <xsl:with-param name="depth">
            <xsl:value-of select="$depth + 1" />
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:if>
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
