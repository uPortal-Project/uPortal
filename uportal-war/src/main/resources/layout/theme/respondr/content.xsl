<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<!--
 | This file determines the presentation of rows and portlet containers.
 | Portlet content is rendered outside of the theme, handled entirely by the portlet itself.
 | The file is imported by the base stylesheet respondr.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to respondr.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<!-- ============================================= -->
<!-- ========== STYLESHEET DELCARATION =========== -->
<!-- ============================================= -->
<!--
 | RED
 | This statement defines this document as XSL and declares the Xalan extension
 | elements used for URL generation and permissions checks.
 |
 | If a change is made to this section it MUST be copied to all other XSL files
 | used by the theme
-->
<xsl:stylesheet
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg dlm xsi"
    version="1.0">

  <!-- ========== TEMPLATE: CONTENT ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the main content of the page (stuff organized into rows).
  -->
  <xsl:template match="content">
    <!-- Handles dashboard mode -->
    <xsl:if test="column">
      <xsl:call-template name="columns">
        <xsl:with-param name="COLUMNS"><xsl:value-of select="count(column)"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    <!-- Handles focused mode -->
    <xsl:apply-templates select="focused/channel">
      <xsl:with-param name="WIDTH_CSS_CLASS">col-md-12</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ========== TEMPLATE: BODY COLUMNS ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders the columns of the page body.
  -->
  <xsl:template name="columns">
    <xsl:param name="COLUMNS" />
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
    <div id="portalPageBodyColumns" class="row">
      <xsl:for-each select="column">
        <xsl:variable name="NUMBER">
            <xsl:value-of select="position()" />
        </xsl:variable>
        <!-- Determine column place in the layout and add appropriate class. -->
        <xsl:variable name="POSITION_CSS_CLASS">column-<xsl:value-of select="$NUMBER" /></xsl:variable>
        <!-- 
         | Per up-layout-selector.js, current valid width selections are 25%,
         | 33%, 34%, 40%, 50%, 60%, and 100%.  The following approach works
         | with all of those.  (8.3333 == percentage of total width occupied 
         | by 1 column in a 12-column grid.)
         +-->
        <xsl:variable name="WIDTH_CSS_CLASS">col-md-<xsl:value-of select="round(number(substring-before(@width,'%')) div 8.3333)" /></xsl:variable>
        <xsl:variable name="MOVABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:moveAllowed='false')">movable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="DELETABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:deleteAllowed='false')">deletable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="EDITABLE">
          <xsl:choose>
            <xsl:when test="not(@dlm:editAllowed='false')">editable</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="CAN_ADD_CHILDREN">
          <xsl:choose>
            <xsl:when test="not(@dlm:addChildAllowed='false')">canAddChildren</xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <div id="column_{@ID}" class="portal-page-column {$POSITION_CSS_CLASS} {$WIDTH_CSS_CLASS} {$MOVABLE} {$DELETABLE} {$EDITABLE} {$CAN_ADD_CHILDREN}"> <!-- Unique column_ID needed for drag and drop. -->
          <div id="inner-column_{@ID}" class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
            <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
                <div class="column-permissions"><a class="button portal-column-permissions-link" href="javascript:;"><span class="icon permissions"></span><xsl:value-of select="upMsg:getMessage('edit.column.x.permissions', $USER_LANG, $NUMBER)"/></a></div>
            </xsl:if>
            <xsl:apply-templates select="channel|blocked-channel"/> <!-- Render the column's portlets.  -->
          </div>
        </div>
      </xsl:for-each>
    </div>
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
  </xsl:template>
  <!-- ============================================ -->

  <!-- ========== TEMPLATE: PORTLET ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders the portlet containers: chrome and controls.
  -->
  <xsl:template match="channel|blocked-channel">

    <xsl:variable name="PORTLET_LOCKED"> <!-- Test to determine if the portlet is locked in the layout. -->
      <xsl:choose> 
        <xsl:when test="@dlm:moveAllowed='false'">locked</xsl:when> 
        <xsl:otherwise>movable</xsl:otherwise> 
      </xsl:choose> 
    </xsl:variable>

    <xsl:variable name="DELETABLE">
      <xsl:choose>
        <xsl:when test="not(@dlm:deleteAllowed='false')">deletable</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_CHROME"> <!-- Test to determine if the portlet has been given the highlight flag. -->
      <xsl:choose>
        <xsl:when test="./parameter[@name='chromeStyle']/@value='no-chrome'">no-chrome</xsl:when>
        <xsl:when test="./parameter[@name='showChrome']/@value='false'">no-chrome</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_HIGHLIGHT"> <!-- Test to determine if the portlet has been given the highlight flag. -->
      <xsl:choose>
          <xsl:when test="./parameter[@name='chromeStyle']/@value='highlighted'">highlight</xsl:when>
          <xsl:when test="./parameter[@name='highlight']/@value='true'">highlight</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="PORTLET_ALTERNATE"> <!-- Test to determine if the portlet has been given the alternate flag. -->
      <xsl:choose>
        <xsl:when test="./parameter[@name='chromeStyle']/@value='alternate'">alternate</xsl:when>
        <xsl:when test="./parameter[@name='alternate']/@value='true'">alternate</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- ****** PORTLET CONTAINER ****** -->
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->
    <section id="portlet_{@ID}" class="up-portlet-wrapper {@fname} {$PORTLET_LOCKED} {$DELETABLE} {$PORTLET_CHROME} {$PORTLET_ALTERNATE} {$PORTLET_HIGHLIGHT}"> <!-- Main portlet container.  The unique ID is needed for drag and drop.  The portlet fname is also written into the class attribute to allow for unique rendering of the portlet presentation. -->

    <!-- Start of the Marketplace Modal Section -->
        <xsl:variable name="saveRatingPortletUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                    <url:portal-url type='RESOURCE'>
                        <url:fname>portletmarketplace</url:fname>
                        <url:portlet-url>
                            <url:fname>portletmarketplace</url:fname>
                            <url:resourceId>saveRating</url:resourceId>
                            <url:param name="portletFName" value="{@fname}"/>
                        </url:portlet-url>
                    </url:portal-url>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        
      <xsl:variable name="getRatingPortletUrl">
            <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                    <url:portal-url type='RESOURCE'>
                        <url:fname>portletmarketplace</url:fname>
                        <url:portlet-url>
                            <url:fname>portletmarketplace</url:fname>
                            <url:resourceId>getRating</url:resourceId>
                            <url:param name="portletFName" value="{@fname}"/>
                        </url:portlet-url>
                    </url:portal-url>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="portletName" select="@name" />

        <div class="modal fade" id="ratePortletModal{@ID}" tabindex="-1" role="dialog"
             aria-labelledby="RatingModal" aria-hidden="true"
             data-title="{upMsg:getMessage('rate.portlet.by.name', $USER_LANG, $portletName)}"
             data-close.button.label="{upMsg:getMessage('close', $USER_LANG)}"
             data-save.button.label="{upMsg:getMessage('save.and.close', $USER_LANG)}"
             data-geturl="{$getRatingPortletUrl}"
             data-saveurl="{$saveRatingPortletUrl}"
             data-get.rating.unsucessful="{upMsg:getMessage('rating.retrieved.unsuccessfully', $USER_LANG)}"
             data-rating.save.successful = "{upMsg:getMessage('rating.saved.successfully', $USER_LANG)}"
             data-rating.save.unsuccessful = "{upMsg:getMessage('rating.saved.unsuccessfully', $USER_LANG)}"
             data-rating.instructions.unrated =
                     "{upMsg:getMessage('rating.instructions.unrated', $USER_LANG, $portletName)}"
             data-rating.instructions.rated =
                     "{upMsg:getMessage('rating.instructions.rated', $USER_LANG, $portletName)}"
                />
        
        <script type="text/javascript">
            (function($) {
                $( document ).ready(function() {
                    $('#ratePortletModal<xsl:value-of select="@ID"/>').createRatingModal();
                    $('#ratePortletModal<xsl:value-of select="@ID"/>').find('input').rating();
                });
            })(up.jQuery);
        </script>
<!-- End of the Marketplace Modal Section -->

        <!-- PORTLET CHROME CHOICE -->
        <xsl:choose>
          <!-- ***** REMOVE CHROME ***** -->
          <xsl:when test="$PORTLET_CHROME = 'no-chrome'">
            <div class="up-portlet-wrapper-inner no-chrome">
              <!-- ****** PORTLET TOOLBAR ****** -->
              <!-- If not movable, default to hidden so you have no grab region.
                   jQuery may unhide though if there are menu options to display.  -->
              <xsl:variable name="hideIfNotMovable">
                  <xsl:choose>
                      <!-- UP-4354 For some reason the test below doesn't work when in focus mode and I can't figure out why,
                           so using the 2nd form since it works for both desktop view and focus view.  It appears the
                           dlm namespace is not valid when in focus mode even though the namespace is defined in the
                           input xml so all @dlm: references fail. Below works whether dlm namespace defined or not.
                      <xsl:when test="@dlm:moveAllowed='false'">hidden</xsl:when> -->
                      <xsl:when test="@*[local-name() = 'moveAllowed']='false'">hidden</xsl:when>
                      <xsl:otherwise></xsl:otherwise>
                  </xsl:choose>
              </xsl:variable>
              <div class="hover-toolbar {$hideIfNotMovable}">
                <xsl:call-template name="portlet-toolbar"/>
              </div>
              <!-- ****** START: PORTLET CONTENT ****** -->
              <div id="portletContent_{@ID}" class="up-portlet-content-wrapper"> <!-- Portlet content container. -->
                <div class="up-portlet-content-wrapper-inner"> <!-- Inner div for additional presentation/formatting options. -->
                  <xsl:call-template name="portlet-content"/>
                </div>
              </div>
            </div>
          </xsl:when>

          <!-- ***** RENDER CHROME ***** -->
          <xsl:otherwise>
            <div class="up-portlet-wrapper-inner">
              <!-- ****** PORTLET TOOLBAR ****** -->
              <xsl:call-template name="portlet-toolbar"/>
              <!-- ****** PORTLET CONTENT ****** -->
              <div id="portletContent_{@ID}" class="fl-widget-content fl-fix up-portlet-content-wrapper round-bottom"> <!-- Portlet content container. -->
                <div class="up-portlet-content-wrapper-inner">  <!-- Inner div for additional presentation/formatting options. -->
                  <xsl:call-template name="portlet-content"/>
                </div>
              </div>
            </div>
          </xsl:otherwise>
        </xsl:choose>
    </section>
    <chunk-point/> <!-- Performance Optimization, see ChunkPointPlaceholderEventSource -->

  </xsl:template>
  <!-- ======================================= -->

  <!-- ========== TEMPLATE: PORTLET CONTENT ========== -->
  <!-- ============================================== -->
  <!-- Renders the portlet toolbar -->
  <xsl:template name="portlet-toolbar">
    <div id="toolbar_{@ID}" class="fl-widget-titlebar up-portlet-titlebar round-top"> <!-- Portlet toolbar. -->
      <!-- Portlet Title -->
      <h2 class="portlet-title round-top">
        <xsl:variable name="portletMaxUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
              <url:portal-url>
                <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
              </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <!-- Reference anchor for page focus on refresh and link to focused view of channel. -->
        <a id="{@ID}" href="{$portletMaxUrl}"><xsl:value-of select="@title"/></a>
        <xsl:call-template name="controls"/>
      </h2>
    </div>
  </xsl:template>

  <!-- Renders the portlet content -->
  <xsl:template name="portlet-content">
    <xsl:choose>
        <xsl:when test="name() = 'blocked-channel'">
            <xsl:choose>
                <xsl:when test="parameter[@name='blockImpersonation']/@value = 'true'">
                    <div><p><em><xsl:value-of select="upMsg:getMessage('hidden.in.impersonation.view', $USER_LANG)"/></em></p></div>
                </xsl:when>
                <xsl:otherwise>
                    <div><p><em><xsl:value-of select="upMsg:getMessage('channel.blocked', $USER_LANG)"/></em></p></div>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
        </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ========== TEMPLATE: PORTLET FOCUSED ========== -->
  <!-- ============================================== -->
  <!--
   | These two templates render the focused portlet content.
  -->
  <xsl:template match="focused">
    <div class="fluid-row">
        <div class="col-md-12">
            <div id="portalPageBodyColumns" class="columns-1">
                <div class="portal-page-column column-1">
                    <div class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
                    <xsl:apply-templates select="channel|blocked-channel"/>
                  </div>
                </div>
            </div>
        </div>
    </div>
  </xsl:template>
  <!-- ============================================== -->
  
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->

  <!-- This template renders portlet controls.  Each control has a unique class for assigning icons or other specific presentation. -->
  <xsl:template name="controls">
      <xsl:variable name="PORTLET_LOCKED"> <!-- Test to determine if the portlet is locked in the layout. -->
          <xsl:choose>
              <xsl:when test="@dlm:moveAllowed='false'">locked</xsl:when>
              <xsl:otherwise>movable</xsl:otherwise>
          </xsl:choose>
      </xsl:variable>

      <div class="portlet-controls">
          <!-- Test to determine if the portlet is locked in the layout. If not provide a grab handle the user could
               see.  Otherwise, just provide an empty div for the grab-handle.  The 'grab-handle' class must be
               present on every portlet else fluid will error when it encounters a portlet without the class. -->
          <div class="grab-handle hidden">
              <xsl:if test="$PORTLET_LOCKED='movable'">
                  <i class="fa fa-arrows"></i>
              </xsl:if>
          </div>

    <div class="portlet-options-menu btn-group hidden">  <!-- Start out hidden.  jQuery will unhide if there are menu options -->
      <a class="btn btn-link dropdown-toggle" data-toggle="dropdown" href="#"><xsl:value-of select="upMsg:getMessage('portlet.menu.option', $USER_LANG)"/> <span class="{upMsg:getMessage('portlet.menu.option.caretclass', $USER_LANG)}"></span></a>
      <ul class="dropdown-menu" style="right: 0; left: auto;">
    <!--
      Porlet Controls Display Order:
      help, remove, maximize, minimize, info, print, settings, ...
    -->
      <xsl:variable name="hasHelp">
          <xsl:if test="parameter[@name='hasHelp']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="hasAbout">
          <xsl:if test="parameter[@name='hasAbout']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="editable">
          <xsl:if test="parameter[@name='editable']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="permissionChannelId">PORTLET_ID.<xsl:value-of select="@chanID"/></xsl:variable>
      <xsl:variable name="canConfigure">
          <!-- This option is special in that it evaluates both whether (1) the portlet supports CONFIG mode and (2) this user is allowed to access it. -->
          <xsl:if test="parameter[@name='configurable']/@value = 'true' and upAuth:hasPermission('UP_PORTLET_PUBLISH', 'PORTLET_MODE_CONFIG', $permissionChannelId)">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="printable">
          <xsl:if test="parameter[@name='printable']/@value = 'true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="hasFavorites">
        <xsl:if test="//content/@hasFavorites = 'true' and $AUTHENTICATED='true'">true</xsl:if>
      </xsl:variable>
      <xsl:variable name="isInFavorites">
        <xsl:variable name="curFname" select="@fname" />
        <xsl:if test="/layout/favorites/favorite[@fname = $curFname]">true</xsl:if>
      </xsl:variable>

      <xsl:if test="$AUTHENTICATED='true'">
          <li>
              <a href="javascript:;" title="{upMsg:getMessage('rate.this.portlet', $USER_LANG)}" class="rateThisPortlet{@ID}" data-toggle="modal" data-target="#ratePortletModal{@ID}">
                  <span><xsl:value-of select="upMsg:getMessage('rate.this.portlet', $USER_LANG)"/></span>
              </a>
          </li>
      </xsl:if>

          <!-- Favorites -->
          <xsl:if test="$hasFavorites='true'">
              <xsl:choose>
                  <xsl:when test="$isInFavorites!='true'"><!-- Add to favorite. -->
                      <li>
                          <a href="javascript:;" title="{upMsg:getMessage('add.this.portlet.to.my.favorite', $USER_LANG)}"
                             class="addToFavoriteLink{@chanID}">
                              <span><xsl:value-of select="upMsg:getMessage('add.to.my.favorites', $USER_LANG)"/></span>
                          </a>
                          <!-- used for the ajax call to add to favorites in up-favorite.js-->
                          <script type="text/javascript">
                              (function($) {
                              $( document ).ready(function() {
                              $('.addToFavoriteLink<xsl:value-of
                              select="@chanID"/>').click({
                              portletId : '<xsl:value-of select="@chanID"/>',
                              context : '<xsl:value-of select="$CONTEXT_PATH"/>'}, up.addToFavorite);
                              });
                              })(up.jQuery);
                          </script>
                      </li>
                  </xsl:when>
                  <xsl:otherwise><!-- Remove From favorites. -->
                      <li>
                          <a href="javascript:;"
                             title="{upMsg:getMessage('remove.this.portlet.from.my.favorite', $USER_LANG)}"
                             class="removeFromFavoriteLink{@chanID}">
                              <span><xsl:value-of select="upMsg:getMessage('remove.from.my.favorites', $USER_LANG)"/></span>
                          </a>
                          <!-- used for the ajax call to remove from favorites in up-favorite.js-->
                          <script type="text/javascript">
                              (function($) {
                              $( document ).ready(function() {
                              $('.removeFromFavoriteLink<xsl:value-of select="@chanID"/>').click({portletId : '<xsl:value-of select="@chanID"/>', context : '<xsl:value-of select="$CONTEXT_PATH"/>'}, up.removeFromFavorite);
                              });
                              })(up.jQuery);
                          </script>
                      </li>
                  </xsl:otherwise>
              </xsl:choose>
          </xsl:if>

          <xsl:if test="$PORTLET_LOCKED='movable'">
              <xsl:variable name="moveText"><xsl:value-of select="upMsg:getMessage('move.this.portlet', $USER_LANG)"/></xsl:variable>
              <li>
                  <a id="movePortlet_{@ID}" title="{$moveText}" href="#" class="up-portlet-control move" data-move-text="{$moveText}" data-cancel-move-text="{upMsg:getMessage('cancel.portlet.move', $USER_LANG)}"><xsl:value-of select="$moveText"/></a>
              </li>
          </xsl:if>

          <!-- Add to Layout Icon -->
          <xsl:if test="//focused[@in-user-layout='no'] and upGroup:isChannelDeepMemberOf(//focused/channel/@fname, 'local.1')"> <!-- Add to layout. -->
              <li>
                  <a id="focusedContentDialogLink" href="javascript:;"
                     title="{upMsg:getMessage('add.this.portlet.to.my.layout', $USER_LANG)}" class="up-portlet-control add">
                      <span><xsl:value-of select="upMsg:getMessage('add.to.my.layout', $USER_LANG)"/></span>
                  </a>
              </li>
          </xsl:if>

          <!-- Remove Icon -->
          <!-- note: deleteAllowed will either be false or not present if set from
           the admin ui;  not certain the last (3rd) criteria is needed or
           appropriate -->
          <xsl:if test="not(@dlm:deleteAllowed='false') and not(//focused) and not(/layout/navigation/tab[@activeTab='true']/@immutable='true')">
            <!-- calls a layout api on click that removes the current node from the layout -->
            <li>
              <a id="removePortlet_{@ID}" title="{upMsg:getMessage('are.you.sure.remove.portlet', $USER_LANG)}" href="#" class="up-portlet-control remove"><xsl:value-of select="upMsg:getMessage('remove', $USER_LANG)"/></a>
            </li>
          </xsl:if>

      <!-- Focus Icon -->
      <xsl:if test="not(//focused) and not(//layout_fragment) and @windowState!='minimized'">
        <xsl:variable name="portletMaxUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url state="MAXIMIZED" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletMaxUrl}" title="{upMsg:getMessage('enter.maximized.mode.for.this.portlet', $USER_LANG)}" class="up-portlet-control focus"><xsl:value-of select="upMsg:getMessage('maximize', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Return from Focused Icon. Don't display for transient portlets. -->
      <xsl:if test="//focused and not(@transient='true')">
        <xsl:variable name="portletReturnUrl">
              <xsl:call-template name="portalUrl">
                <xsl:with-param name="url">
                  <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url state="NORMAL" copyCurrentRenderParameters="true" />
                  </url:portal-url>
                </xsl:with-param>
              </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletReturnUrl}" title="{upMsg:getMessage('return.to.dashboard.view', $USER_LANG)}" class="up-portlet-control return"><xsl:value-of select="upMsg:getMessage('return.to.dashboard', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <xsl:if test="$USE_PORTLET_MINIMIZE_CONTENT='true'">
          <xsl:if test="not(//focused) and not(//layout_fragment)">
            <xsl:choose>
              <!-- Return from Minimized. -->
              <xsl:when test="@windowState='minimized'">
                <xsl:variable name="portletReturnUrl">
                  <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                            <url:portlet-url state="NORMAL" copyCurrentRenderParameters="true" />
                        </url:portal-url>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:variable>
                <li>
                  <a href="{$portletReturnUrl}" title="{upMsg:getMessage('return.to.dashboard.view', $USER_LANG)}" class="up-portlet-control show-content"><xsl:value-of select="upMsg:getMessage('return.to.dashboard', $USER_LANG)"/></a>
                </li>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="portletMinUrl">
                  <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url>
                            <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                            <url:portlet-url state="MINIMIZED" copyCurrentRenderParameters="true" />
                        </url:portal-url>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:variable>
                <li>
                  <a href="{$portletMinUrl}" title="{upMsg:getMessage('enter.minimized.mode.for.this.portlet', $USER_LANG)}" class="up-portlet-control hide-content"><xsl:value-of select="upMsg:getMessage('minimize', $USER_LANG)"/></a>
                </li>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
      </xsl:if>

      <!-- Edit Icon -->
      <xsl:if test="$editable='true'">
        <xsl:variable name="portletEditUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="EDIT" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletEditUrl}#{@ID}" title="{upMsg:getMessage('edit.portlet', $USER_LANG)}" class="up-portlet-control edit"><xsl:value-of select="upMsg:getMessage('edit', $USER_LANG)"/></a>
        </li>
      </xsl:if>

      <!-- Configure Icon -->
      <xsl:if test="@portletMode!='config' and @windowState!='minimized'">
        <xsl:if test="$canConfigure='true'">
          <xsl:variable name="portletConfigureUrl">
            <xsl:call-template name="portalUrl">
              <xsl:with-param name="url">
                  <url:portal-url>
                      <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                      <url:portlet-url mode="CONFIG" copyCurrentRenderParameters="true" />
                  </url:portal-url>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:variable>
          <li>
            <a href="{$portletConfigureUrl}" title="{upMsg:getMessage('configure.portlet', $USER_LANG)}" class="up-portlet-control configure"><xsl:value-of select="upMsg:getMessage('configure', $USER_LANG)"/></a>
          </li>
        </xsl:if>
      </xsl:if>

      <!-- Print Icon -->
      <xsl:if test="$printable='true'">
        <xsl:variable name="portletPrintUrl">
          <xsl:call-template name="portalUrl">
            <xsl:with-param name="url">
                <url:portal-url>
                    <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                    <url:portlet-url mode="PRINT" copyCurrentRenderParameters="true" />
                </url:portal-url>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <li>
          <a href="{$portletPrintUrl}#{@ID}" title="{upMsg:getMessage('print.portlet', $USER_LANG)}" class="up-portlet-control print"><xsl:value-of select="upMsg:getMessage('print', $USER_LANG)"/></a>
        </li>
      </xsl:if>

          <!-- About Icon -->
          <xsl:if test="$hasAbout='true'">
              <xsl:variable name="portletAboutUrl">
                  <xsl:call-template name="portalUrl">
                      <xsl:with-param name="url">
                          <url:portal-url>
                              <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                              <url:portlet-url mode="ABOUT" copyCurrentRenderParameters="true" />
                          </url:portal-url>
                      </xsl:with-param>
                  </xsl:call-template>
              </xsl:variable>
              <li>
                  <a href="{$portletAboutUrl}#{@ID}" title="{upMsg:getMessage('view.information.about.portlet', $USER_LANG)}" class="up-portlet-control about"><xsl:value-of select="upMsg:getMessage('view.information.about.portlet', $USER_LANG)"/></a>
              </li>
          </xsl:if>

          <!-- Help Icon -->
          <xsl:if test="$hasHelp='true'">
              <xsl:variable name="portletHelpUrl">
                  <xsl:call-template name="portalUrl">
                      <xsl:with-param name="url">
                          <url:portal-url>
                              <url:layoutId><xsl:value-of select="@ID"/></url:layoutId>
                              <url:portlet-url mode="HELP" copyCurrentRenderParameters="true" />
                          </url:portal-url>
                      </xsl:with-param>
                  </xsl:call-template>
              </xsl:variable>
              <li>
                  <a href="{$portletHelpUrl}#{@ID}" title="{upMsg:getMessage('view.help.for.portlet', $USER_LANG)}" class="up-portlet-control help"><xsl:value-of select="upMsg:getMessage('help', $USER_LANG)"/></a>
              </li>
          </xsl:if>

          <xsl:if test="$IS_FRAGMENT_ADMIN_MODE='true'">
          <li>
            <a class="up-portlet-control permissions portlet-permissions-link" href="javascript:;"
               title="{upMsg:getMessage('edit.permissions.for.this.portlet', $USER_LANG)}">
                <xsl:value-of select="upMsg:getMessage('edit.permissions', $USER_LANG)"/></a>
          </li>
        </xsl:if>
        </ul>
    </div>
  </div>
  </xsl:template>
  
  <xsl:template name="focused-fragment-header">
    <xsl:if test="//tab[@focusedFragment='true']">
        <div id="focused-fragment-header" class="container">
            <div class="row">
                <h3>
                    <xsl:value-of select="//tab[@focusedFragment='true']/@name"></xsl:value-of>
                    <xsl:variable select="//tab[@focusedFragment='true']/@ID" name="FOCUSED_FRAGMENT_ID"></xsl:variable>
                    <!--
                    Collection favoriting and link access not yet implemented.
                    <div class="dropdown pull-right">
                    <button class="btn dropdown-toggle" type="button" id='{$FOCUSED_FRAGMENT_ID}dropdownMenu'
                            data-toggle="dropdown" style="background: inherit;">
                        <span class="glyphicon glyphicon-cog"></span>
                    </button>
                        <ul class="dropdown-menu" role="menu" aria-labelledby='{$FOCUSED_FRAGMENT_ID}dropdownMenu'>
                            <li role="presentation">
                                <a role="menuitem" tabindex="-1" href="#">Favorite This Collection</a></li>
                            <li role="presentation">
                                <a role="menuitem" tabindex="-1" href="#">Link to ...</a></li>
                        </ul>
                    </div>
                    -->
                </h3>
            </div>
            <div class="row">
                <hr style='border-style: dotted; border-color: grey; border-width: 1px;' />
            </div>
        </div>
      </xsl:if>
  </xsl:template>
  <!-- ========== TEMPLATE: PORTLET CONTROLS ========== -->

</xsl:stylesheet>
