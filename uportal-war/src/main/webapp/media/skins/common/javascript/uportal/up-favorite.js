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
    var insertNode = function(sourceID, destinationID, method){
        var saveOrderURL = context + "/api/layout?action=move" + tabOrPortlet
        + "&sourceID=" + sourceID
        + "&elementID=" + destinationID
        + "&method=" + method;
        console.log(saveOrderURL);
        $.ajax({
            url: saveOrderURL,
            type: "POST",
            data: null,
            dataType: "json",
            async: false,
            success: function (){
              console.log("layout move successful. URL: " + saveOrderURL);
            },
            error: function(request, text, error) {
              $('#up-notification').noty({text: request.response, type: 'error'});
            }
        });
    };
    var sourceID = $(item).attr('sourceid');
    //We need to insert item both before and after
    
    //Insert item before the next if next exists
    if($(item).next().length!=0){
        var siblingID = $(item).next().attr('sourceid');
        insertNode(sourceID, siblingID, 'insertBefore');
    }else{
        //if next does not exist append item to the end
        var siblingID = $(item).prev().attr('sourceid');
        insertNode(sourceID, siblingID, 'appendAfter');
    }
    //Insert prev before item, if there is a previous
    if($(item).prev().length !=0 ){
        var siblingID = $(item).prev().attr('sourceid');
        insertNode(siblingID, sourceID, 'insertBefore');
    }
  };
})(jQuery);
