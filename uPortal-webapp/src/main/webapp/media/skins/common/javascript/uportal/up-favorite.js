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
var up = up || {};

(function ($) {
  up.addToFavorite = function addToFavoritesFunction(event) {
    var portletId = event.data.portletId;
    var context = event.data.context;
    return $.ajax({
      url: context + '/api/layout?action=addFavorite&channelId=' + portletId,
      type: 'POST',
      data: null,
      dataType: 'json',
      async: true,
      success: function (request) {
        $('#up-notification').noty({
          text: request.response,
          type: 'success'
        });
      },
      error: function (request) {
        $('#up-notification').noty({
          text: request.responseJSON.response,
          type: 'error'
        });
      }
    });
  };

  up.removeFromFavorite = function removeFromFavoritesFunction(event) {
    var portletId = event.data.portletId;
    var context = event.data.context;
    return $.ajax({
      url: context + '/api/layout?action=removeFavorite&channelId=' + portletId,
      type: 'POST',
      data: null,
      dataType: 'json',
      async: true,
      success: function (request) {
        $('#up-notification').noty({
          text: request.response,
          type: 'success'
        });
      },
      error: function (request) {
        $('#up-notification').noty({
          text: request.response,
          type: 'error'
        });
      }
    });
  };

  up.moveStuff = function moveStuffFunction(tabOrPortlet, item, context) {
    var insertNode = function (sourceId, previousNodeId, nextNodeId) {
      var saveOrderURL =
        context +
        '/api/layout?action=movePortletAjax' +
        '&sourceId=' +
        sourceId +
        '&previousNodeId=' +
        previousNodeId +
        '&nextNodeId=' +
        nextNodeId;
      console.log(saveOrderURL);
      $.ajax({
        url: saveOrderURL,
        type: 'POST',
        data: null,
        dataType: 'json',
        async: true,
        success: function () {
          console.log('layout move successful.');
        },
        error: function () {
          console.error('Error persisting move ' + saveOrderURL);
        }
      });
    };

    var moveFavoriteGroup = function (sourceId, previousNodeId, nextNodeId) {
      var method = '' === nextNodeId ? 'appendAfter' : 'insertBefore';
      var elementId = '' === nextNodeId ? previousNodeId : nextNodeId;
      var saveOrderURL =
        context +
        '/api/layout?action=moveTab' +
        '&sourceID=' +
        sourceId +
        '&method=' +
        method +
        '&elementID=' +
        elementId;
      console.log(saveOrderURL);
      $.ajax({
        url: saveOrderURL,
        type: 'POST',
        data: null,
        dataType: 'json',
        async: true,
        success: function () {
          console.log('favorite group move successful.');
        },
        error: function () {
          console.error(
            'Error persisting favorite group reorder ' + saveOrderURL
          );
        }
      });
    };
    var sourceID = $(item).attr('sourceid');
    var nextId =
      $(item).next().length > 0 ? $(item).next().attr('sourceid') : '';
    var previousId =
      $(item).prev().length > 0 ? $(item).prev().attr('sourceid') : '';

    if ('Tab' === tabOrPortlet) {
      moveFavoriteGroup(sourceID, previousId, nextId);
    } else {
      // We need to insert item both before and after
      insertNode(sourceID, previousId, nextId);
    }
  };
})(jQuery);
