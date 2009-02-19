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
     <div id="focusedContentAddingDialog" title="Add Content">
      <form>
       <fieldset>
       <legend>Add to page:</legend>
        <xsl:for-each select="/layout/navigation/tab">
         <input name="targetTab" id="targetTab{@ID}" value="{@ID}" type="radio"/> <label for="targetTab{@ID}" class="portlet-form-field-label"><xsl:value-of select="@name"/></label><br/>
        </xsl:for-each>
       </fieldset>
       <p>
        <input name="channelId" type="hidden" value="{//focused/channel/@chanID}"/>
        <input type="submit" value="Add" class="portlet-form-button"/>&#160;
       </p>
      </form>
     </div>
    </div>
    <script type="text/javascript">
     
     up.jQuery(document).ready(function(){
       up.jQuery.uportal.UportalLayoutManager(
       {
          portalUrl: '<xsl:value-of select="$BASE_ACTION_URL"/>',
          mediaPath: '<xsl:value-of select="$MEDIA_PATH"/>',
          currentSkin: '<xsl:value-of select="$SKIN"/>',
          isFocusMode: true
       });
     });
     
    </script>
   </xsl:when>
   
   <xsl:otherwise>
  
    <div id="dojoMenus" style="display:none;">
     <!-- Add Channel Menu -->
     <div id="contentAddingDialog" title="Add Content">
        <div id="channelAddingTabs">
         <ul style="height: 30px;">
          <li><a href="#channel-tab-1"><span>Browse Channels</span></a></li>
          <li><a href="#channel-tab-2"><span>Search</span></a></li>
         </ul>
         <div id="channel-tab-1">
         <h4 id="channelLoading">Loading portlet list . . . </h4>
         <table cellspacing="0" cellpadding="0" border="0">
          <tr>
           <td class="portlet-section-subheader"><label for="categorySelectMenu">Category</label></td>
           <td class="portlet-section-subheader"><label for="channelSelectMenu">Portlet</label></td>
          </tr>
          <tr>
           <td><select id="categorySelectMenu" size="14" style="width: 150px; background: url({$SKIN_PATH}/images/loading.gif) no-repeat center center"></select></td>
           <td><select id="channelSelectMenu" size="14" style="width: 300px; background: url({$SKIN_PATH}/images/loading.gif) no-repeat center center"></select></td>
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
       <button id="addChannelLink" class="portlet-form-button">Add to my page</button>&#160;
       <button id="previewChannelLink" class="portlet-form-button">Use it now</button>&#160;
      </p>
     </div>
     
     <div id="pageLayoutDialog" title="Change Layout">
      <form>
      <p>
       <label for="pageName" class="portlet-form-label">Page name:</label><br/>
       <input id="pageName" name="pageName" type="text" size="20" value="{/layout/navigation/tab[@activeTab='true']/@name}"/>
      </p>
      <hr/>
      <fieldset>
      	<legend>Page layout:</legend>
       <table id="changeColumns" style="width: 100%">
        <tr>
         <td>1 Column</td>
         <td>2 Columns</td>
         <td>3 Columns</td>
         <td>4 Columns</td>
        </tr>
        <tr>
         <td>
          <input type="radio" name="layoutChoice" value="100" title="One big column"/>
          <img src="{$SKIN_PATH}/images/layout_100.gif" alt="One big column" title="One big column"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="50-50" title="Two equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_50-50.gif" alt="Two equal columns" title="Two equal columns"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="33-34-33" title="Three equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_33-34-33.gif" alt="Three equal columns" title="Three equal columns"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-25-25-25" title="Four equal columns"/>
          <img src="{$SKIN_PATH}/images/layout_25-25-25-25.gif" alt="Four equal columns" title="Four equal columns"/>
         </td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="40-60" title="One narrow and one wide column"/>
          <img src="{$SKIN_PATH}/images/layout_40-60.gif" alt="One narrow and one wide column" title="One narrow and one wide column"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-50-25" title="Narrow, wide, narrow"/>
          <img src="{$SKIN_PATH}/images/layout_25-50-25.gif" alt="Narrow, wide, narrow" title="Narrow, wide, narrow"/>
         </td>
         <td></td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="60-40" title="One wide and one narrow column"/>
          <img src="{$SKIN_PATH}/images/layout_60-40.gif" alt="One wide and one narrow column" title="One wide and one narrow column"/>
         </td>
         <td></td>
         <td></td>
        </tr>
       </table>
       </fieldset>
       <input type="submit" value="update" class="portlet-form-button"/>
      </form>
     </div>	
  
     <div id="skinChoosingDialog" title="Choose Skin">
      <form>
       <h4 id="skinLoading">Loading skin list . . . </h4>
       <p class="portlet-form-label">
        Choose a skin for your portal view:
       </p>
       <div id="skinList"></div>
       <p>
        <input type="submit" value="Choose" class="portlet-form-button"/>&#160;
       </p>
      </form>
     </div>
     
     <div id="portalDropWarning" style="display:none;">
      <p>The box cannot be moved here. The box underneath is locked.</p>
     </div>
     
    </div>
    <script type="text/javascript">
       up.jQuery(document).ready(function(){
          up.jQuery.uportal.UportalLayoutManager(
            {
              portalUrl: '<xsl:value-of select="$BASE_ACTION_URL"/>',
              mediaPath: '<xsl:value-of select="$MEDIA_PATH"/>',
              currentSkin: '<xsl:value-of select="$SKIN"/>'
            }
          );
       });
    </script>
      
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 
</xsl:stylesheet>
