/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var up = up || {};

(function($) {
  up.addToFavorite = function addToFavoritesFunction(event) {
    var portletId = event.data.portletId;
    var context = event.data.context;
    $.ajax({
            url: context + "/api/layout?action=addFavorite&channelId=" + portletId,
            type: "POST",
            data: null,
            dataType: "json",
            async: true,
            success: function (request, text){
              $('#up-notification').noty({text: request.response, type: 'success'});
            },
            error: function(request, text, error) {
              $('#up-notification').noty({text: request.response, type: 'error'});
            }
        });
  };

  up.removeFromFavorite = function removeFromFavoritesFunction(event) {
    var portletId = event.data.portletId;
    var context = event.data.context;
    $.ajax({
            url: context + "/api/layout?action=removeFavorite&channelId=" + portletId,
            type: "POST",
            data: null,
            dataType: "json",
            async: true,
            success: function (request, text){
              $('#up-notification').noty({text: request.response, type: 'success'});
            },
            error: function(request, text, error) {
              $('#up-notification').noty({text: request.response, type: 'error'});
            }
        });
    };

  up.moveStuff = function moveStuffFunction(tabOrPortlet, item, context) {
    var sourceID = item.getAttribute('sourceid');
    var destinationID =
            item.previousSibling.getAttribute != undefined ?
                item.previousSibling.getAttribute('sourceid') :
                item.nextSibling.getAttribute('sourceid');
    var method = item.previousSibling.getAttribute == undefined ? 'insertBefore' : 'appendAfter';
        
    var theURL = context + "/api/layout?action=move" + tabOrPortlet
                         + "&sourceID=" + sourceID
                         + "&elementID=" + destinationID
                         + "&method=" + method;

    $.ajax({
            url: theURL,
            type: "POST",
            data: null,
            dataType: "json",
            async: true,
            success: function (){
              console.debug("layout move successful. URL: " + theURL);
            },
            error: function(request, text, error) {
              $('#up-notification').noty({text: request.response, type: 'error'});
            }
        });
  };
})(jQuery);
