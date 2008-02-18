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
   <div id="contentAddingDialog">
    <div class="dojoTabContainer portal-dojo-container" style="width: 500px; height: 250px;">
     <div class="dojoTabLabels-top dojoTabNoLayout">
      <div id="contentAddingBrowseButton" onclick="chooseContentAddingMethod('browse')" class="dojoTab current">
       <div>
        <span>Browse Portlets</span>
       </div>
      </div>
      <div id="contentAddingSearchButton" onclick="chooseContentAddingMethod('search')" class="dojoTab">
       <div>
        <span>Search</span>
       </div>
      </div>
     </div>
     <div class="dojoTabPaneWrapper">
      <div id="contentAddingBrowseTab" class="portal-dojo-pane dojoTabPane" style="padding: 10px; position: relative; width:460px; height: 208px;">
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
      <div id="contentAddingSearchTab" class="portal-dojo-pane dojoTabPane" style="overflow:auto; padding: 10px; display: none; position: relative; width:460px; height: 208px;">
       <p>
        <label class="portlet-form-field-label" for="addChannelSearchTerm">Search for:</label>&#160;
        <input id="addChannelSearchTerm" type="text" onkeydown="searchChannels()"/>
       </p>
       <br/>
       <h3>Matching portlets</h3>
       <ul id="addChannelSearchResults" style="list-style-type: none; padding: 0px; margin-left: 5px;"></ul>
      </div>
     </div>
    </div>
    <br/>
    <h3 class="portal-section-header">Portlet Details</h3>
    <h4 id="channelTitle" class="portal-section-subheader"></h4>
    <p id="channelDescription"></p>
    <p style="padding-top: 10px;">
     <input id="addChannelId" type="hidden"/>
     <button id="addChannelLink" onclick="addChannel()" class="portlet-form-button">Add to my page</button>&#160;
     <button id="previewChannelLink" class="portlet-form-button">Use it now</button>&#160;
    </p>
   </div>
   <div id="pageLayoutDialog" class="preferences-edit-window" bgColor="#e6eefb" bgOpacity="0.7" toggle="fade" toggleDuration="250">
    <p><label class="portlet-form-field-label">Number of columns:</label>
     <xsl:element name="input">
      <xsl:attribute name="onclick">changeColumns(1);</xsl:attribute>
      <xsl:attribute name="name">columnNum</xsl:attribute>
      <xsl:attribute name="type">radio</xsl:attribute>
      <xsl:if test="count(/layout/content/column)=1">
       <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
     </xsl:element> <label class="portlet-form-field-label">1</label>
     <xsl:element name="input">
      <xsl:attribute name="onclick">changeColumns(2);</xsl:attribute>
      <xsl:attribute name="name">columnNum</xsl:attribute>
      <xsl:attribute name="type">radio</xsl:attribute>
      <xsl:if test="count(/layout/content/column)=2">
       <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
     </xsl:element> <label class="portlet-form-field-label">2</label>
     <xsl:element name="input">
      <xsl:attribute name="onclick">changeColumns(3);</xsl:attribute>
      <xsl:attribute name="name">columnNum</xsl:attribute>
      <xsl:attribute name="type">radio</xsl:attribute>
      <xsl:if test="count(/layout/content/column)=3">
       <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
     </xsl:element> <label class="portlet-form-field-label">3</label>
    </p>
    
    <p><label class="portlet-form-field-label">Column widths:</label></p>
    <br/>
    <div id="columnWidthsAdjuster"></div>
   </div>	
   <div id="skinChoosingDialog">
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
   
   var contentAddingMenu, pageLayoutMenu, skinChoosingMenu;
   dojo.addOnLoad(initAjaxPortalPreferences);
   
   
   <xsl:for-each select="/layout/content/column">
    new dojo.dnd.Source('inner_column_<xsl:value-of select="@ID"/>', { withHandles: true });
    <xsl:for-each select="channel">
     <xsl:if test="not(@dlm:moveAllowed='false')">
     </xsl:if>
     <xsl:if test="not(@dlm:deleteAllowed='false')">
      a = dojo.byId("removePortlet_" + '<xsl:value-of select="@ID"/>');
      a.href = "javascript:;";
      a.onclick = function(){deleteChannel('<xsl:value-of select="@ID"/>')};
     </xsl:if>
    </xsl:for-each>
   </xsl:for-each>
   
   
  </script>
 </xsl:template>
  
</xsl:stylesheet>
