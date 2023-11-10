/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var uportal = uportal || {};

(function ($, fluid) {
  var layouts = [
    {nameKey: 'fullWidth', columns: [100]},
    {nameKey: 'narrowWide', columns: [40, 60]},
    {nameKey: 'even', columns: [50, 50]},
    {nameKey: 'wideNarrow', columns: [60, 40]},
    {nameKey: 'even', columns: [33, 34, 33]},
    {nameKey: 'narrowWideNarrow', columns: [25, 50, 25]},
    {nameKey: 'even', columns: [25, 25, 25, 25]},
    {nameKey: 'sixColumn', columns: [17, 17, 16, 16, 17, 17]},
    {nameKey: 'flexTwo', columns: [102]},
    {nameKey: 'flexThree', columns: [103]},
    {nameKey: 'flexFour', columns: [104]},
    {nameKey: 'flexSix', columns: [106]}
  ];

  /*
   * A deletable column must either:
   *   (1) be marked deletable and contain no locked children; or
   *   (2) be marked up-fragment-admin, indicating the user is the fragment owner
   */
  var deletableColumnsSelector =
    '.deletable:not(:has(.locked)),.up-fragment-admin';

  /*
   * GENERAL UTILITY METHODS
   */

  var getActiveTabId = function () {
    return up.defaultNodeIdExtractor($('#portalNavigationList li.active'));
  };

  var typeMessage = {
    ERROR: 'error',
    WARN: 'warn',
    SUCCESS: 'success'
  };

  /*
   * Diplay messages
   * msg : should be text
   * type : should be a css class definition on message div, values are "error", "warn", "success"
   * callback : should be a function or null
   */
  var showMessage = function (message, type, callback) {
    var messageDiv = $('#portalPageBodyMessage');
    if (message && type) {
      var delay = type == typeMessage.ERROR ? 5000 : 2000;
      if (messageDiv.length > 0) {
        messageDiv.html('<p>' + message + '</p>');
        messageDiv
          .removeClass()
          .addClass(type)
          .show()
          .delay(delay)
          .fadeOut(400, callback);
      } else return callback;
    } else {
      return callback;
    }
  };

  /*
   * LAYOUT COLUMN EDITING FUNCTIONS
   */

  /**
   * Return an array representing the currently-chosen layout
   *
   * @return layout columns array
   */
  var getCurrentLayout = function () {
    var columns = [];

    // iterate through the CSS classnames for each column and parse
    // the fl-container-flex classnames to determine the width percentage for
    // each column
    $('#portalPageBodyColumns > [id^=column_]').each(function () {
      var flClass = $(this).get(0).className.match('fl-container-flex[0-9]+');
      if (flClass != null) {
        columns.push(Number(flClass[0].match('[0-9]+')[0]));
      }
    });

    // if no columns were found, indicate that this is a single-column
    // layout
    if (columns.length === 0) columns.push(100);

    return columns;
  };

  /**
   * Return an array of currently-existing columns which may be deleted.
   * Deletable columns are calculated based on the column permissions themselves,
   * as well as the permissions of the columns' contents.
   *
   * @return column array
   */
  var getDeletableColumns = function () {
    var columns = $('#portalPageBodyColumns > [id^=column_]');
    var deletableColumns = columns.filter(deletableColumnsSelector);

    var contentColumns = deletableColumns.filter(':has(.up-portlet-wrapper)');
    if (contentColumns.size() > 0) {
      var acceptorColumns = columns.filter(
        '.canAddChildren,.up-fragment-admin'
      );
      // if there are no acceptor columns, mark any columns that
      // have content as undeletable
      if (acceptorColumns.size() == 0) {
        deletableColumns = deletableColumns.filter(
          ':not(:has(.up-portlet-wrapper))'
        );
      }
    }

    return deletableColumns;
  };

  var getPermittedLayouts = function () {
    var canAddColumns = $('#portalNavigation_' + getActiveTabId()).filter(
      '.canAddChildren,.up-fragment-admin'
    );
    var columns = $('#portalPageBodyColumns > [id^=column_]');
    var deletableColumns = columns.filter(deletableColumnsSelector);

    // set the minimum number of columns according to how
    // many deletable columns the layout currently contains
    var minColumns = columns.length - deletableColumns.length;

    var contentColumns = deletableColumns.filter(':has(.up-portlet-wrapper)');
    if (contentColumns.size() > 0) {
      var acceptorColumns = columns.filter(
        '.canAddChildren,.up-fragment-admin'
      );
      // if there are no acceptor columns, mark any columns that
      // have content as undeletable
      if (acceptorColumns.size() == 0) {
        deletableColumns = deletableColumns.filter(
          ':not(:has(.up-portlet-wrapper))'
        );
        minColumns = columns.length - deletableColumns.length;
      } else {
        var separateAcceptor = false;
        for (const acceptorColumn of acceptorColumns) {
          if ($.inArray(acceptorColumn, deletableColumns) < 0) {
            separateAcceptor = true;
            break;
          }
        }
        if (!separateAcceptor) minColumns++;
      }
    }

    // set disabled to true for layouts not permitted
    $(layouts).each(function (index, layout) {
      var cannotAddColumns =
        !canAddColumns && layout.columns.length > columns.length;
      var cannotRemoveColumns = layout.columns.length < minColumns;

      if (cannotAddColumns || cannotRemoveColumns) {
        layout.disabled = true;
      }
    });

    return layouts;
  };

  var updateColumns = function (layout, that) {
    var newcolumns = layout.columns;
    var columnCount = $('#portalPageBodyColumns [id^=column_]').size();

    var post = {
      action: 'changeColumns',
      tabId: getActiveTabId(),
      widths: newcolumns
    };

    if (newcolumns.length < columnCount) {
      var numberToDelete = columnCount - newcolumns.length;
      var deletables = getDeletableColumns();
      post.deleted = [];
      var deletes = [];
      for (var index = 0; index < numberToDelete; index++) {
        deletes.push(deletables[deletables.length - index - 1]);
        post.deleted.push(
          up.defaultNodeIdExtractor(deletables[deletables.length - index - 1])
        );
      }

      var acceptors = $('#portalPageBodyColumns > [id^=column_]').filter(
        '.canAddChildren,.up-fragment-admin'
      );
      var acceptor = acceptors.filter(':first');
      post.acceptor = up.defaultNodeIdExtractor(acceptor);
    }

    that.persistence.update(post, function (data) {
      // add any new columns to the page
      $(data.newColumnIds).each(function () {
        var id = this;
        var newColumn = $(document.createElement('div'))
          .attr('id', 'column_' + id)
          .addClass(
            'portal-page-column movable deletable editable canAddChildren'
          )
          .html(
            '<div id="inner-column_' +
              id +
              '" class="portal-page-column-inner"></div>'
          );
        if ($('#portalPageBodyColumns').has('.up-fragment-admin')) {
          newColumn.addClass('up-fragment-admin');
        }
        $('#portalPageBodyColumns').append(newColumn);
      });

      // remove any deleted columns from the page
      $(deletes).each(function () {
        $(this)
          .find('[id^=portlet_]')
          .each(function (index, portlet) {
            var innerColumn = acceptor.find('.portal-page-column-inner');
            $(portlet).appendTo(innerColumn);
          });
        $(this).remove();
      });

      // update the widths and CSS classnames for each column
      // on the page
      $('#portalPageBodyColumns > [id^=column_]').each(function (index) {
        // Column Number
        var column = $(this).removeClass(
          'column-1 column-2 column-3 column-4 column-5 column-6'
        );
        var columnNumberClass = 'column-' + (index + 1);
        $(column).addClass(columnNumberClass);

        // Column Width
        $(this.className.split(' ')).each(function (_, className) {
          if (that.options.columnWidthClassPattern.test(className)) {
            $(column).removeClass(className);
          }
        });
        var columnWidthClass = that.options.columnWidthClassFunction(
          newcolumns[index]
        );
        columnWidthClass && $(column).addClass(columnWidthClass);

        // div.inner-column CSS classes
        var innerColumn = $(column).find('.portal-page-column-inner');
        innerColumn.attr('class', 'portal-page-column-inner'); // Reset to minimum
        var innerColumnClasses = that.options.innerColumnClassesFunction(
          newcolumns[index]
        );
        innerColumnClasses && innerColumn.addClass(innerColumnClasses);
      });

      that.components.gallery.refreshPaneLink();

      that.components.portletReorderer.refresh();
    });
  };

  /**
   * Instantiate a LayoutPersistence component
   *
   * @param {Object} component Container the element containing the fragment browser
   * @param {Object} options configuration options for the components
   */
  up.LayoutPreferences = function (container, options) {
    // construct the new component
    var that = fluid.initView('up.LayoutPreferences', container, options);

    that.persistence = up.LayoutPreferencesPersistence(container, {
      saveLayoutUrl: that.options.layoutPersistenceUrl,
      messages: {error: that.options.messages.persistenceError}
    });

    that.urlProvider = up.UrlProvider(container, {
      portalContext: that.options.portalContext
    });

    that.components = {};

    // initialize the gallery component, if present
    if (
      that.options.gallerySelector &&
      $(that.options.gallerySelector).length > 0
    ) {
      that.components.gallery = up.PortalGallery(that.options.gallerySelector, {
        // content browsing pane
        browseContentPane: {
          options: {
            // "add stuff" sub-pane
            portletBrowser: {
              options: {
                portletRegistry: {
                  options: {
                    portletListUrl: that.options.channelRegistryUrl
                  }
                },
                searchView: {
                  options: {
                    searchInvitationMessage:
                      that.options.messages.searchForStuff
                  }
                },
                categoryListView: {
                  options: {
                    rootCategoryName: that.options.messages.allCategories
                  }
                },
                listeners: {
                  onPortletSelect: function (componentThat, portlet) {
                    var options;
                    var firstChannel;

                    // set the main options for this persistence
                    // request
                    options = {
                      action: 'addPortlet',
                      channelID: portlet.id
                    };

                    // get the first channel element that's
                    // unlocked;  fragment owners may bypass
                    // these restrictions
                    firstChannel = $(
                      '[id^=portlet_].movable,[id^=portlet_].up-fragment-admin'
                    ).first();

                    // if the page has no content just add
                    //  the new portlet to the tab
                    if (firstChannel.size() == 0) {
                      options['elementID'] = getActiveTabId();
                    } else {
                      // otherwise
                      options['elementID'] =
                        up.defaultNodeIdExtractor(firstChannel);
                      options['position'] = 'insertBefore';
                    }

                    that.persistence.update(options, function (data) {
                      if (data.error) {
                        showMessage(data.error, typeMessage.ERROR);
                        /* } else if (data.response) {
                                                    showMessage(data.response, typeMsg.SUCCESS, function(){window.location = that.urlProvider.getTabUrl(getActiveTabId());});
                                                */
                      } else {
                        window.location = that.urlProvider.getTabUrl(
                          getActiveTabId()
                        );
                      }
                    });
                  },
                  onPortletDrag: function (portlet, method, targetID) {
                    // Persist the portlet addition.
                    that.persistence.update(
                      {
                        action: 'addPortlet',
                        channelID: portlet.id,
                        position: method,
                        elementID: targetID
                      },
                      function (xml) {
                        if (xml.error) {
                          showMessage(xml.error, typeMessage.ERROR);
                          /* } else if (data.response) {
                                                    showMessage(xml.response, typeMsg.SUCCESS, function(){window.location = that.urlProvider.getTabUrl(getActiveTabId());});
                                                */
                        } else {
                          window.location = that.urlProvider.getTabUrl(
                            getActiveTabId()
                          );
                        }
                      }
                    );
                  }
                }
              }
            }
          }
        },

        // use stuff pane
        useContentPane: {
          options: {
            listeners: {
              // add a PortletBrowser to the use content pane
              onInitialize: function (overallThat) {
                up.PortletBrowser('.use-content', overallThat, {
                  portletRegistry: {
                    options: {
                      portletListUrl: that.options.channelRegistryUrl
                    }
                  },
                  categoryListView: {
                    type: 'up.AjaxLayoutCategoryListView',
                    options: {
                      rootCategoryName: that.options.messages.allCategories
                    }
                  },
                  portletListView: {
                    type: 'up.AjaxLayoutPortletListView'
                  },
                  searchView: {
                    options: {
                      searchInvitationMessage:
                        that.options.messages.searchForStuff
                    }
                  },
                  listeners: {
                    // on portlet selection, redirect the
                    // browser to the selected portlet's
                    // focus URL
                    onPortletSelect: function (componentThat, portlet) {
                      window.location = that.urlProvider.getPortletUrl(
                        portlet.fname
                      );
                    }
                  }
                });
              }
            }
          }
        },

        // colors pane
        skinPane: {
          options: {
            listeners: {
              onInitialize: function () {
                // add a SkinSelector component to the skin pane
                up.SkinSelector('.skins', {
                  listeners: {
                    // when a skin is selected, update the
                    // persisted skin choice and reload
                    // the page with the new skin
                    onSelectSkin: function (skin) {
                      that.persistence.update(
                        {
                          action: 'chooseSkin',
                          skinName: skin.key
                        },
                        function () {
                          window.location = that.urlProvider.getTabUrl(
                            getActiveTabId()
                          );
                        }
                      );
                    }
                  },
                  currentSkin: that.options.currentSkin,
                  skinListURL: that.options.mediaPath + '/skinList.xml',
                  mediaPath: that.options.mediaPath
                });
              }
            }
          }
        },

        // layouts pane
        layoutPane: {
          options: {
            listeners: {
              onInitialize: function () {
                // add a LayoutSelector component to the
                // layouts pane
                up.LayoutSelector('.layouts-list', {
                  currentLayout: getCurrentLayout(),
                  layouts: getPermittedLayouts(),
                  imagePath: that.options.mediaPath + '/common/images/',
                  listeners: {
                    // when a new layout is selected, call
                    // the locally-defined column update
                    // method
                    onLayoutSelect: function (layout) {
                      updateColumns(layout, that);
                    }
                  },
                  strings: that.options.messages
                });
              }
            }
          }
        }
      });
    }

    that.components.tabManager = up.TabManager('#portalNavigation', {
      listeners: {
        onTabEdit: function (newValue) {
          that.persistence.update({
            action: 'renameTab',
            tabId: getActiveTabId(),
            tabName: newValue
          });
        },
        onTabRemove: function (anchor) {
          if (!confirm(that.options.messages.confirmRemoveTab)) {
            return false;
          }

          var li;
          var id;
          li = anchor.parentNode;
          id = up.defaultNodeIdExtractor(li);
          that.persistence.update(
            {
              action: 'removeElement',
              elementID: id
            },
            function () {
              window.location = that.urlProvider.getPortalHomeUrl();
            }
          );
        },
        onTabAdd: function (tabLabel, columns, tabGroup) {
          that.persistence.update(
            {
              action: 'addTab',
              tabName: tabLabel,
              widths: columns,
              tabGroup: tabGroup
            },
            function (data) {
              window.location = that.urlProvider.getTabUrl(data.tabId);
            }
          );
        },
        onTabMove: function (sourceId, method, elementId, tabPosition) {
          that.persistence.update({
            action: 'moveTab',
            sourceID: sourceId,
            method: method,
            elementID: elementId,
            tabPosition: tabPosition
          });
        }
      },
      tabContext: that.options.tabContext,
      numberOfPortlets: that.options.numberOfPortlets,
      addTabLabel: that.options.messages.addTabLabel
    });

    // initialize the portlet reorderer
    // checks to see if chrome toolbars exist on the layout
    // if so, initialize portlet reorderer
    if ($('[id*=toolbar_]').length > 0) {
      that.components.portletReorderer = up.fluid.reorderLayout(
        '#portalPageBodyColumns',
        {
          selectors: {
            columns: '.portal-page-column-inner',
            modules: '.up-portlet-wrapper',
            lockedModules: '.locked:not(.up-fragment-admin)',
            dropWarning: $('#portalDropWarning'),
            grabHandle: '[id*=toolbar_] .grab-handle'
          },
          listeners: {
            afterMove: function (movedNode) {
              var method = 'insertBefore';
              var target = null;
              if ($(movedNode).nextAll('[id^=portlet_]').size() > 0) {
                target = $(movedNode).nextAll('[id^=portlet_]').get(0);
              } else if ($(movedNode).prevAll('[id^=portlet_]').size() > 0) {
                target = $(movedNode).prevAll('[id^=portlet_]').get(0);
                method = 'appendAfter';
              } else {
                target = $(movedNode).parent();
              }

              var options = {
                action: 'movePortlet',
                method: method,
                elementID: up.defaultNodeIdExtractor(target),
                sourceID: up.defaultNodeIdExtractor(movedNode)
              };
              var succeeded = false; // default... until we hear otherwise
              that.persistence.update(options, function (data) {
                if (!data.error) {
                  succeeded = true;
                }
              });

              if (
                !succeeded &&
                confirm(that.options.messages.movePortletError)
              ) {
                location.reload();
              }

              // Now revert the Move Portlet menu item and hide the grab handle
              var moveOptionsItem = $(movedNode).find(
                '.up-portlet-control.move'
              );
              moveOptionsItem.text(moveOptionsItem.attr('data-move-text'));
              $(movedNode)
                .find('.up-portlet-titlebar .grab-handle')
                .addClass('hidden');
            }
          },
          styles: {
            mouseDrag: 'fl-reorderer-movable-dragging-mouse'
          }
        }
      );
    }

    // Portlet deletion
    $('a[id*=removePortlet_]').click(function () {
      var id = up.defaultNodeIdExtractor(this);
      if (!confirm(that.options.messages.confirmRemovePortlet)) {
        return false;
      }
      $('#portlet_' + id).remove();
      $('#portalSubnavLink_' + id).remove();
      that.persistence.update({action: 'removeElement', elementID: id});
      return false;
    });

    return that;
  };

  // defaults
  fluid.defaults('up.LayoutPreferences', {
    tabContext: 'header',
    numberOfPortlets: 0,
    portalContext: '/uPortal',
    layoutPersistenceUrl: '/uPortal/api/layout',
    channelRegistryUrl: '/uPortal/api/portletList',
    subscribableTabUrl: '/uPortal/api/subscribableTabs.json',
    mediaPath: null,
    currentSkin: null,
    isFragmentMode: false,
    gallerySelector: '.up-gallery', // Pass null/false to disable
    columnWidthClassPattern: 'col-md-',
    columnWidthClassFunction: function () {
      console.error('The columnWidthClassFunction option must be specified.');
    },
    innerColumnClassesFunction: function () {
      console.error('The innerColumnClassesFunction option must be specified.');
    },
    messages: {
      persistenceError: 'Error persisting layout change'
    }
  });

  /**
   * Instantiate a FocusedLayoutPersistence component
   *
   * @param {Object} component Container the element containing the fragment browser
   * @param {Object} options configuration options for the components
   */
  up.FocusedLayoutPreferences = function (container, options) {
    // construct the new component
    var that = fluid.initView(
      'up.FocusedLayoutPreferences',
      container,
      options
    );

    that.persistence = up.LayoutPreferencesPersistence(container, {
      saveLayoutUrl: that.options.layoutPersistenceUrl,
      messages: {error: that.options.messages.persistenceError}
    });

    that.urlProvider = up.UrlProvider(container, {
      portalContext: that.options.portalContext
    });

    that.components = {};

    // initialize the focused content adding dialog link
    $('#focusedContentDialogLink').click(function (event) {
      event.preventDefault();

      // initialize the dialog
      $('.focused-content-dialog').dialog({width: 500, modal: true});

      // wire the form to persist portlet addition
      $('.focused-content-dialog form').submit(function () {
        var portletId;
        var tabId;
        var form;

        // collect form data
        form = this;
        portletId = form.portletId.value;
        tabId = $(form).find('[name=targetTab]:checked').val();

        // persist the portlet addition
        that.persistence.update(
          {
            action: 'addPortlet',
            channelID: portletId,
            position: 'insertBefore',
            elementID: tabId
          },
          function () {
            window.location = that.urlProvider.getTabUrl(tabId);
          }
        );
        return false;
      });

      // re-wire the form to open the initialized dialog
      $(this)
        .unbind('click')
        .click(function () {
          $('.focused-content-dialog').dialog('open');
        });
    });

    return that;
  };

  // defaults
  fluid.defaults('up.FocusedLayoutPreferences', {
    portalContext: '/uPortal',
    layoutPersistenceUrl: '/uPortal/api/layout',
    messages: {
      persistenceError: 'Error persisting layout change'
    }
  });
})(jQuery, fluid);
