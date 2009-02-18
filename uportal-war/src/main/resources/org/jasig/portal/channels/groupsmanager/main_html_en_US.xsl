<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xmsg="urn:x-lexica:xmsg:message:1.0">
  <xsl:output method="html" indent="yes" />
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="highlightedGroupID" select="false()"/>
  <xsl:param name="rootViewGroupID">0</xsl:param>
  <xsl:param name="mode" />
  <xsl:param name="customMessage" select="false()"/>
  <xsl:param name="feedback" select="false()"/>
  <xsl:param name="blockFinishActions" select="false()"/>
  <xsl:param name="grpServantMode">false </xsl:param>
  <xsl:param name="spacerIMG">media/org/jasig/portal/channels/CUserPreferences/tab-column/transparent.gif</xsl:param>
  <xsl:param name="pageSize" select="number(12)"/>
  <xsl:param name="page"/>
  <xsl:param name="mediaBase">media</xsl:param>
  <xsl:param name="iconBase"><xsl:value-of select="$mediaBase" />/skins/icons</xsl:param>
  <xsl:key name="selectedGroup" match="group[@selected='true']" use="@key"/>
  <xsl:key name="selectedEntity" match="entity[@selected='true']" use="@key"/>
  <xsl:key name="groupByID" match="group" use="@id"/>
  <xsl:key name="members" match="node()[((name()='group') and (@canView='true')) or name()='entity']" use="parent::group/@id"/>
  <xsl:variable name="rootGroup" select="key('groupByID',$rootViewGroupID)"/>
  <xsl:variable name="highlightedGroup" select="key('groupByID',$highlightedGroupID)"/>

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
    
    <!-- Feedback Message -->
    <xsl:if test="$feedback">
    	<p class="uportal-channel-warning"><xsl:value-of select="$feedback" /></p>
    </xsl:if>
    
    <!-- Search -->
    <xsl:if test="not($mode='edit') and not($mode='members')">
    	<div>
      <h3><label for="grpQuery">Search</label></h3>
      <form action="{$baseActionURL}" method="POST">
        <input type="hidden" name="grpCommand" value="Search"/>
        <input type="hidden" name="uP_root" value="me"/>
        <label for="grpType">For</label>
        <select class="uportal-button" name="grpType" id="grpType">
          <xsl:variable name="stype" select="$rootGroup/@entityType"/>
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
        <label for="grpMethod">whose name</label>
        <select class="uportal-button" name="grpMethod" id="grpMethod">
          <option value="1">is</option>
          <option value="2">starts with</option>
          <option value="3">ends with</option>
          <option value="4" selected="selected">contains</option>
        </select>
        <input type="text" size="25" name="grpQuery" id="grpQuery" class="uportal-input-text"/>
        <input type="submit" class="uportal-button" value="Go"/>
        <xsl:if test="$highlightedGroup[not(@id=0) and not(@searchResults='true')]">
        	<br/>
          <input type="checkbox" name="grpCommandArg" id="grpCommandArg" value="{//group[@id=$highlightedGroupID]/@key}"/>
          <label for="grpCommandArg"><em>search only descendants of the selected group</em></label>
        </xsl:if>
      </form>
      </div>
    </xsl:if>
    
    <h3>
    <!--<xsl:if test="$mode='select'">-->
      <xsl:choose>
        <xsl:when test="$customMessage">
          <xsl:value-of select="$customMessage"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>
            Browse
          </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    <!--</xsl:if>-->
    </h3>
    <!-- Layout Table -->
    <table cellspacing="0" cellpadding="0">
      <tr>
        <!-- Left Pane - Tree Navigation -->
        <td valign="top" id="groupmgrLeftPane">
          <xsl:call-template name="tree"/>
        </td>
        <!-- Right Pane - Group Details -->
        <td valign="top" id="groupmgrRightPane">
          <xsl:if test="key('groupByID',$highlightedGroupID)">
            <xsl:call-template name="rightPane">
              <xsl:with-param name="group" select="$highlightedGroup"/>
            </xsl:call-template>
          </xsl:if>
        </td>
      </tr>
		</table>
    
    <form action="{$baseActionURL}" method="POST">
    
      <xsl:if test="$mode='select'">
        <xsl:if test="count(descendant::*[@selected='true'])">
        	<!-- Selected Groups -->
          <xsl:if test="count(descendant::group[@selected='true'])">
          Selected Groups:
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
          </table>
          </xsl:if>
          <!-- Selected Entities -->
          <xsl:if test="count(descendant::entity[@selected='true'])">
            Selected Entities:
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
            </table>
          </xsl:if>
        </xsl:if>
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
      </xsl:if>
      
    </form>
 
  </xsl:template>
  
  <xsl:template name="rightPaneButtons">
  	<xsl:param name="group"/>
    <xsl:if test="not($mode='members')">
      <xsl:choose>
        <xsl:when test="$mode='select'"/>
        <xsl:when test="$group/@searchResults='true'">
          <a class="groupmgr-delete-group" href="javascript:this.location.href='{$baseActionURL}?grpCommand=Delete&amp;grpCommandArg={$group/@id}';"><img border="0" src="{$iconBase}/cross.png" alt="Delete Group" title="Delete Group"/>Delete Group</a>
        </xsl:when>
        <xsl:when test="$mode='edit'">
          <a class="groupmgr-done-editing" href="{$baseActionURL}?grpCommand=Unlock&amp;grpCommandArg={$group/@id}"><img border="0" src="{$iconBase}/tick.png" alt="Done Editing" title="Done Editing"/>Done Editing</a>
          <xsl:if test="$group/@canDelete='true'">
            <a class="groupmgr-delete-group" href="javascript:grpDeleteGroup('{$baseActionURL}?grpCommand=Delete&amp;grpCommandArg={$group/@id}');"><img border="0" src="{$iconBase}/cross.png" alt="Delete Group" title="Delete Group"/>Delete Group</a>
          </xsl:if>
        </xsl:when>
        <xsl:when test="($group/@editable='true') and ($group/@canUpdate='true' or $group/@canAssignPermissions='true' or $group/@canManageMembers='true' or $group/@canCreateGroup='true')">
          <a class="groupmgr-edit-group" href="{$baseActionURL}?grpCommand=Lock&amp;grpCommandArg={$group/@id}"><img border="0" src="{$iconBase}/pencil.png" alt="Edit Group" title="Edit Group"/>Edit Group</a>
        </xsl:when>
      </xsl:choose>
      <a class="groupmgr-close-group" href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg="><img border="0" src="{$iconBase}/cancel.png" alt="Close Group" title="Close Group"/>Close Group</a>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="rightPane">
  	<xsl:param name="group"/>
    <xsl:variable name="grpKey" select="@key" />
    
    <h3>
      <xsl:choose>
        <xsl:when test="$highlightedGroupID='0'">
          <xsl:text>
            My Groups
          </xsl:text>
        </xsl:when>
        <xsl:otherwise>
        	<xsl:text>
            Group Details
          </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </h3>
    
		<form action="{$baseActionURL}" method="POST">
    <xsl:choose>
      <xsl:when test="$highlightedGroupID='0'">
				<!-- Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
      	<input type="hidden" name="grpCommandArg" value="{$highlightedGroupID}" />
        <xsl:call-template name="rightPaneButtons">
          <xsl:with-param name="group" select="$group"/>
        </xsl:call-template>
        
        <!-- Select checkbox or selected tick mark -->
        <xsl:if test="($mode='select') and not($group/@searchResults='true')">
          <xsl:if test="not($group/@id=0) and ($group/@canSelect='true')">
            <xsl:choose>
              <xsl:when test="($group/@selected='true') or (key('selectedGroup',$group/@key))">
                <img border="0" src="{$iconBase}/tick.png" alt="Selected" title="Selected"/>
              </xsl:when>
              <xsl:otherwise>
                <input type="checkbox" name="grpSelect//{$group/@id}|group" value="true" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:if>
        <xsl:choose>
        
        	<!-- View Mode -->
          <xsl:when test="not($mode='edit') or not($group/@canUpdate='true')">
            <h4><xsl:value-of select="$group/RDF/Description/title" /></h4>
            <p><xsl:value-of select="string($group/RDF/Description/description)" /></p>
          </xsl:when>
          
          <!-- Edit Mode -->
          <xsl:otherwise>
          
          	<table width="100%" border="0">
              <tr>
                <td>
                  <label for="grpName">Name:</label>
                </td>
                <td>
                	<input type="text" size="40" maxsize="255" name="grpName" id="grpName">
                    <xsl:attribute name="value">
                      <xsl:value-of select="$group/RDF/Description/title" />
                    </xsl:attribute>
                  </input>
                </td>
              </tr>
              <tr>
              	<td>
                	<label for="">Description:</label>
                </td>
                <td>
                	<textarea  cols="60" rows="5" name="grpDescription" class="uportal-input-text">
                  	<xsl:value-of select="string($group/RDF/Description/description)" />
                  </textarea>
                </td>
              </tr>
            </table>
            
            <xsl:if test="($mode='edit') or ($mode='members')">
              <xsl:if test="$mode='edit'">
                <xsl:if test="$group/@canUpdate='true'">
                <input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Update';" value="Update" class="uportal-button" />
                <input type="reset" value="Reset Form"  class="uportal-button" />
                </xsl:if>
                <xsl:if test="not($highlightedGroupID='0') and not($grpKey='null') and $group/@canAssignPermissions='true'">
                <input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Permissions';" value="Assign Permissions" class="uportal-button" />
                </xsl:if>
              </xsl:if>
              <xsl:if test="$group/@canManageMembers='true' or ($grpServantMode='true')">
                <input type="submit"  onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Add';" value="Add Members" class="uportal-button" />
              </xsl:if>
              <xsl:if test="$mode='edit'">
                <xsl:if test="not($grpServantMode='true')">
                  <xsl:if test="$group/@canCreateGroup='true'">
                    <input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Create';" value="Create New Member Group" class="uportal-button" />
                    <input type="text" size="25" name="grpNewName" value="(new group name)" class="uportal-input-text" />
                  </xsl:if>
                </xsl:if>
              </xsl:if>
            </xsl:if>

          </xsl:otherwise>
        </xsl:choose>
  			
        <!-- Members -->
    		<xsl:variable name="siblingCount" select="count(key('members',$group/@id))"/>
				<h5>Members</h5>
        
        <!-- Pagination controls -->
        <xsl:if test="$page &gt; 1">
          <div id="groupsmgrPagination">
            <a class="groupmgr-paginate-first" href="{$baseActionURL}?grpPageBack={$page - 1}"><img src="{$iconBase}/resultset_first.png" border="0" title="First Page" alt="First Page"/></a>
            <a class="groupmgr-paginate-previous" href="{$baseActionURL}?grpPageBack=1"><img src="{$iconBase}/resultset_previous.png" border="0" hspace="1" vspace="0" title="Previous Page" alt="Previous Page"/></a>
            <xsl:value-of select="concat($page,' / ',ceiling($siblingCount div $pageSize))"/>
            <xsl:if test="($pageSize*$page) &lt; $siblingCount">
              <a class="groupmgr-paginate-next" href="{$baseActionURL}?grpPageForward=1"><img src="{$iconBase}/resultset_next.png" border="0" title="Next Page" alt="Next Page"/></a>
              <a class="groupmgr-paginate-last" href="{$baseActionURL}?grpPageForward={ceiling($siblingCount div $pageSize) - $page}"><img src="{$iconBase}/resultset_last.png" border="0" title="Last Page" alt="Last Page"/></a>
            </xsl:if>
          </div>
        </xsl:if>
        
        <!-- Member List -->
        <xsl:for-each select="key('members',$group/@id)">
          <xsl:sort data-type="text" order="descending" select="name()"/>
          <xsl:sort data-type="text" order="ascending" select="RDF/Description/title"/>
          <xsl:sort data-type="text" order="ascending" select="@displayName"/>
          
          <xsl:if test="(position() &gt; (($page - 1)*$pageSize)) and (position() &lt; (($page * $pageSize)+1))">
          
          	<!-- Group -->
            <xsl:if test="name()='group'">
              <xsl:if test="@canView='true'">
              	<div class="groupmgr-group">
                <xsl:if test="$mode='select'">
                  <xsl:if test="not(@id=0) and @canSelect='true'">
                    <xsl:choose>
                      <xsl:when test="(@selected='true') or (key('selectedGroup',@key))">
                        <img border="0" src="{$iconBase}/tick.png" alt="Selected" title="Selected"/>
                      </xsl:when>
                      <xsl:otherwise>
                      	<input type="checkbox" name="grpSelect//{@id}|group" value="true" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:if>
                
                <span class="uportal-channel-table-row-even">
                <strong>
                <xsl:choose>
                  <xsl:when test="$mode='members'">
                  	<xsl:value-of select="RDF/Description/title" />
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{$baseActionURL}?grpCommand=Highlight&amp;grpCommandArg={@id}"><xsl:value-of select="RDF/Description/title" /></a>
                  </xsl:otherwise>
                </xsl:choose>
                </strong>
                </span>
                
                <xsl:if test="(($mode='edit') or ($mode='members')) and (../@canManageMembers='true' or ($grpServantMode='true'))">
                  <a class="groupmgr-remove-member" href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}','{RDF/Description/title}','{parent::group/RDF/Description/title}');">
                  	<img src="{$iconBase}/cross.png" border="0" alt="Remove Member" title="Remove Member"/>Remove Member
                  </a>
                </xsl:if>
                
                <xsl:choose>
                  <xsl:when test="not($group/@canViewProperties='true')"/>
                  <xsl:when test="properties">
                  	<a class="groupmgr-hide-info" href="{$baseActionURL}?grpCommand=HideProperties&amp;grpCommandArg={@id}"><img src="{$iconBase}/cancel.png" border="0" alt="Hide Info" title="Hide Info"/>Hide Info</a>
                  </xsl:when>
                  <xsl:otherwise>
                  	<a class="groupmgr-show-info" href="{$baseActionURL}?grpCommand=ShowProperties&amp;grpCommandArg={@id}"><img src="{$iconBase}/information.png" border="0" alt="Show Info" title="Show Info"/>Show Info</a>
                  </xsl:otherwise>
                </xsl:choose>
                
                <xsl:if test="properties">
                  <xsl:call-template name="propertiesDisplay">
                    <xsl:with-param name="properties" select="properties"/>
                  </xsl:call-template> 
                </xsl:if>
                </div>
              </xsl:if>
            </xsl:if> <!-- End Group -->
            
            <!-- Entity -->
            <xsl:if test="name()='entity'">
            	<div class="groupmgr-entity">
              <xsl:if test="$mode='select'">
                <xsl:if test="(($group/@searchResults='true') or @canSelect='true')">
                  <xsl:choose>
                    <xsl:when test="(@selected='true') or key('selectedEntity',@key)">
                      <img border="0" src="{$iconBase}/tick.png" alt="Selected" title="Selected"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <input type="checkbox" name="grpSelect//{@id}|entity" value="true" />
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
              </xsl:if>
                
              <strong>
                <xsl:value-of select="@displayName" />
              </strong>
              
              <xsl:if test="(($mode='edit') or ($mode='members')) and ((../@canManageMembers='true') or ($grpServantMode='true'))">
              	<a class="groupmgr-remove-member" href="javascript:grpRemoveMember('{$baseActionURL}?grpCommand=Remove&amp;grpCommandArg=parent.{parent::group/@id}|child.{@id}','{@displayName}','{parent::group/RDF/Description/title}');">
              		<img src="{$iconBase}/cross.gif" border="0" alt="Remove Member" title="Remove Member"/>Remove Member
              	</a>
              </xsl:if>
              
              <xsl:choose>
                <xsl:when test="properties">
                	<a class="groupmgr-hide-info" href="{$baseActionURL}?grpCommand=HideProperties&amp;grpCommandArg={@id}"><img src="{$iconBase}/cancel.png" border="0" alt="Hide Info" title="Hide Info"/>Hide Info</a>
                </xsl:when>
                <xsl:otherwise>
                	<a class="groupmgr-show-info" href="{$baseActionURL}?grpCommand=ShowProperties&amp;grpCommandArg={@id}"><img src="{$iconBase}/information.png" border="0" alt="Show Info" title="Show Info"/>Show Info</a>
                </xsl:otherwise>
              </xsl:choose>
              
              <xsl:if test="properties">
                <xsl:call-template name="propertiesDisplay">
                	<xsl:with-param name="properties" select="properties"/>
                </xsl:call-template>  
              </xsl:if>
              </div>
            </xsl:if>
          </xsl:if> <!-- End Entity -->
          
        </xsl:for-each>
    
        <xsl:if test="$mode='select'">
          <input type="submit" onClick="javascript:this.form.action='{$baseActionURL}?grpCommand=Select';"  value="Select Marked" class="uportal-button" />
        </xsl:if>
      
      </xsl:otherwise>
    </xsl:choose>
		</form>
    
    <xsl:if test="(($mode='edit') or ($mode='members')) and ($grpServantMode='true')">
      <form action="{$baseActionURL}" method="POST">
        <input type="hidden" name="grpCommand" value="Cancel" />
        <input type="submit" value="Finished" class="uportal-button" />
      </form>
		</xsl:if>
    
  </xsl:template>
  
  <xsl:template name="propertiesDisplay">
    <xsl:param name="properties"/>

    <xsl:choose>
      <xsl:when test="$properties/property">
        <table border="0" cellspacing="0" cellpadding="0">
          <xsl:for-each select="$properties/property">
            <xsl:sort data-type="text" order="ascending" select="@name"/>
            <tr>
            	<th>Name</th>
              <th>Value</th>
            </tr>
            <tr>
              <td><xsl:value-of select="@name"/></td>
              <td><xsl:value-of select="@value"/></td>
            </tr>
          </xsl:for-each>
        </table>
      </xsl:when>
      <xsl:otherwise>
      	<em>No additional information available</em>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="tree">
    <form action="{$baseActionURL}" method="POST">
      <input type="hidden" name="uP_root" value="me"/>
      <xsl:apply-templates select="$rootGroup" />
      <xsl:variable name="stype" select="$rootGroup/@entityType"/>
      <xsl:if test="not($mode='members')">
        <xsl:for-each select="/CGroupsManager/group[@searchResults='true']">
          <xsl:sort data-type="number" order="ascending" select="@id"/>
          <xsl:if test="not($stype) or (@entityType=$stype)"> 
          	<xsl:apply-templates select="." />
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
    </form>
  </xsl:template>
  
  <xsl:template match="group">
    <xsl:if test="@canView='true' or (@id=0) or (@searchResults='true')">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">	
      <tr>
        <td width="100%" colspan="2">
        
          <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
              <td>
              	<xsl:choose>
              		<xsl:when test="(@expanded='true') and (@hasMembers='true') and (count(key('members',@id)[name()='group']) &gt; 0) and not(@id=0)">
                  	<a class="groupmgr-tree-open" href="{$baseActionURL}?grpCommand=Collapse&amp;grpCommandArg={@id}">
                  		<img border="0" src="{$iconBase}/tree_open.png" alt="Collapse Group" title="Collapse Group"/>
                  	</a>
                  </xsl:when>
                  <xsl:when test="(@expanded='false') and (@hasMembers='true') and not(@id=0)">
                    <a class="groupmgr-tree-closed" href="{$baseActionURL}?grpCommand=Expand&amp;grpCommandArg={@id}">
                    	<img border="0" src="{$iconBase}/tree_closed.png" alt="Expand Group" title="Expand Group"/>
                    </a>
                  </xsl:when>
              		<xsl:otherwise>
              			<img class="groupmgr-tree-leaf" border="0" src="{$iconBase}/bullet_black.png" alt="Group" title="Group" />
              		</xsl:otherwise>
              	</xsl:choose>                
              </td>
              <td width="100%" class="uportal-channel-table-row-even">
               	<xsl:if test="$highlightedGroupID and $highlightedGroupID=@id">
            			<xsl:attribute name="class">uportal-background-highlight</xsl:attribute>
            		</xsl:if>
                <xsl:choose>
                  <xsl:when test="($mode='members')">
                    <span class="uportal-channel-table-row-even">
                      <xsl:value-of select="RDF/Description/title" />
                    </span>
                  </xsl:when>
                  <xsl:otherwise>
                		<a href="{$baseActionURL}?uP_root=me&amp;grpCommand=Highlight&amp;grpCommandArg={@id}">
                    	<span class="uportal-channel-table-row-even">
                    		<xsl:value-of select="RDF/Description/title" />
                  		</span>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </table>
        </td>
      </tr>

      <xsl:if test="(@expanded='true') and (count(key('members',@id)[name()='group']) &gt; 0)">
      	<tr>
      		<td background="{$iconBase}/tree_bullet.png">
            <img src="{$spacerIMG}" height="5" width="16"/>
          </td>
      		<td width="100%">
            <xsl:apply-templates select="group[not(@searchResults='true')]">
              <xsl:sort data-type="text" order="ascending" select="RDF/Description/title"/>
            </xsl:apply-templates>
        	</td>
        </tr>
      </xsl:if>
      	
      </table>
    </xsl:if>
    
  </xsl:template>
 
</xsl:stylesheet>
