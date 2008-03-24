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
  
  <xsl:choose>
   
   <xsl:when test="//focused">
    <div id="dojoMenus" style="display:none;">
     <!-- Add Channel Menu -->
     <div id="focusedContentAddingDialog" class="jqueryui" title="Add Portlet to My Layout">
      <form onsubmit="return addFocusedChannel(this);">
       <p class="portlet-form-label">
        Choose a page in which to add this portlet:
       </p>
       <p>
        <xsl:for-each select="/layout/navigation/tab">
         <input name="targetTab" value="{@ID}" type="radio"/> <label class="portlet-form-field-label"><xsl:value-of select="@name"/></label><br/>
        </xsl:for-each>
       </p>
       <p>
        <input name="channelId" type="hidden" value="{//focused/channel/@chanID}"/>
        <input type="submit" value="Add" class="portlet-form-button"/>&#160;
       </p>
      </form>
     </div>
    </div>
    <script type="text/javascript">
     
     var portalUrl = '<xsl:value-of select="$BASE_ACTION_URL"/>';
     var preferencesUrl = 'ajax/preferences';
     var skinPath = '<xsl:value-of select="$SKIN_PATH"/>';
     
     $(document).ready(function(){
     initfocusedportal();
     });
     
    </script>
   </xsl:when>
   
   <xsl:otherwise>
  
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
           <td><select id="categorySelectMenu" size="14" style="width: 150px; background: url({$SKIN_PATH}/skin/loading.gif) no-repeat center center"></select></td>
           <td><select id="channelSelectMenu" size="14" style="width: 300px; background: url({$SKIN_PATH}/skin/loading.gif) no-repeat center center"></select></td>
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
     
     <div id="pageLayoutDialog" class="jqueryui" title="Change Layout">
      <form onsubmit="return updatePage(this);">
      <p>
       <label class="portlet-form-field-label">Page name:</label>
       <input name="pageName" type="text" size="20" value="{/layout/navigation/tab[@activeTab='true']/@name}"/>
      </p>
      <p><label class="portlet-form-field-label">Number of columns:</label></p>
       <table id="changeColumns">
        <tr>
         <td>
          <input type="radio" name="layoutChoice" value="100" title="One big column"/>
          <img src="{$SKIN_PATH}/images/layout_100.gif" title="One big column"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="50-50" title="Two equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_50-50.gif" title="Two equal columns"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="33-34-33" title="Three equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_33-34-33.gif" title="Three equal columns"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-25-25-25" title="Four equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_25-25-25-25.gif" title="Four equal columns"/>
         </td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="40-60" title="One narrow and one wide column"/>
          <img src="{$SKIN_PATH}/images/layout_40-60.gif" title="One narrow and one wide column"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-50-25" title="Narrow, wide, narrow"/>
          <img src="{$SKIN_PATH}/images/layout_25-50-25.gif" title="Narrow, wide, narrow"/>
         </td>
         <td></td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="60-40" title="One wide and one narrow column"/>
          <img src="{$SKIN_PATH}/images/layout_60-40.gif" title="One wide and one narrow column"/>
         </td>
         <td></td>
         <td></td>
        </tr>
       </table>
       <input type="submit" value="update" class="portlet-form-button"/>
      </form>
     </div>	
  
     <div id="skinChoosingDialog" class="jqueryui" title="Choose Skin">
      <h4 id="skinLoading">Loading skin list . . . </h4>
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
      
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 
</xsl:stylesheet>
