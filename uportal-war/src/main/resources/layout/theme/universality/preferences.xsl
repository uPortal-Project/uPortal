<?xml version="1.0" encoding="utf-8"?>

<!--
 | Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 | See license distributed with this file and
 | available online at http://www.uportal.org/license.html
-->

<!--
 | This file determines the presentation of the portal preferences interface.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dlm="http://www.uportal.org/layout/dlm" >
  
 <xsl:template name="preferences">
  <div id="dojoMenus" style="display:none;">
   <!-- Add Channel Menu -->
   <div id="contentAddingDialog" class="jqueryui" title="Add Content">
      <div id="channelAddingTabs" class="jqueryui">
       <ul>
        <li><a href="#channel-tab-1"><span>Browse Channels</span></a></li>
        <li><a href="#channel-tab-2"><span>Search</span></a></li>
       </ul>
       <div id="channel-tab-1">
       <h4 id="channelLoading">Loading portlet list . . . </h4>
       <table cellspacing="0" cellpadding="0" border="0">
        <tr>
         <td class="portlet-section-subheader">Category</td>
         <td class="portlet-section-subheader">Portlet</td>
        </tr>
        <tr>
         <td><select id="categorySelectMenu" onchange="browseChannelCategory()" size="14" style="width: 150px; background: url({$SKIN_PATH}/skin/loading.gif) no-repeat center center"></select></td>
         <td><select id="channelSelectMenu" onchange="selectChannel(this.value)" size="14" style="width: 300px; background: url({$SKIN_PATH}/skin/loading.gif) no-repeat center center"></select></td>
        </tr>
       </table>
       </div>
       <div id="channel-tab-2">
       <p>
        <label class="portlet-form-field-label" for="addChannelSearchTerm">Search for:</label>&#160;
        <input id="addChannelSearchTerm" type="text"/>
       </p>
       <h3>Matching portlets</h3>
       <ul id="addChannelSearchResults" style="list-style-type: none; list-style-image: none; padding: 0px; margin-left: 5px; max-height: 160px; overflow: auto"></ul>
       </div>
      </div>
    <h3 class="portal-section-header">Portlet Details</h3>
    <h4 id="channelTitle" class="portal-section-subheader"></h4>
    <p id="channelDescription"></p>
    <p style="padding-top: 10px;">
     <input id="addChannelId" type="hidden"/>
     <button id="addChannelLink" onclick="addPortlet()" class="portlet-form-button">Add to my page</button>&#160;
     <button id="previewChannelLink" class="portlet-form-button">Use it now</button>&#160;
    </p>
   </div>
   
   <div id="pageLayoutDialog" class="jqueryui" title="Edit Page">
    <form onsubmit="return updatePageName(this.pageName.value);">
    <p>
     <label class="portlet-form-field-label">Page name:</label>
     <input name="pageName" type="text" size="20" value="{/layout/navigation/tab[@activeTab='true']/@name}"/>
     <input type="submit" value="update"/>
    </p>
    </form>
    <p id="changeColumns"><label class="portlet-form-field-label">Number of columns:</label>
     <input type="radio" name="columnNum"/>
     <label class="portlet-form-field-label">1</label>
     <input type="radio" name="columnNum"/>
     <label class="portlet-form-field-label">2</label>
     <input type="radio" name="columnNum"/>
     <label class="portlet-form-field-label">3</label>
    </p>
   </div>	

   <div id="skinChoosingDialog" class="jqueryui" title="Choose Skin">
    <h4 id="skinLoading">Loading portlet list . . . </h4>
    <form onsubmit="return chooseSkin(this);">
     <p class="portlet-form-label">
      Choose a skin for your portal view:
     </p>
     <p id="skinList"></p>
     <p>
      <input type="submit" value="Choose" class="portlet-form-button"/>&#160;
     </p>
    </form>
   </div>
  </div>
  <script type="text/javascript">
   
   var portalUrl = '<xsl:value-of select="$BASE_ACTION_URL"/>';
   var preferencesUrl = 'ajax/preferences';
   var channelListUrl = 'ajax/channelList';
   var mediaPath = '<xsl:value-of select="$MEDIA_PATH"/>';
   var currentSkin = '<xsl:value-of select="$SKIN"/>';
   var columnCount = <xsl:value-of select="count(/layout/content/column)"/>;
   var skinPath = '<xsl:value-of select="$SKIN_PATH"/>';
   var tabId = '<xsl:value-of select="/layout/navigation/tab[@activeTab='true']/@ID"/>';
   
   $(document).ready(function(){

    <xsl:for-each select="/layout/content/column">
      <xsl:variable name="currentColumnId"><xsl:value-of select="@ID"/></xsl:variable>
      <xsl:for-each select="channel">
       <xsl:if test="not(@dlm:moveAllowed='false')">
       		$('#portlet_<xsl:value-of select="@ID"/>').addClass('movable').children('.portlet-toolbar').css('cursor', 'move');
       </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
   initportal();
   
   });

  </script>
 </xsl:template>
  
</xsl:stylesheet>
