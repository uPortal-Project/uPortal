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

(function($){
    up.notify = function(messageText, messageLayout, messageType){
        $('#up-notification').noty({
            text: messageText,
            layout: messageLayout,
            type: messageType
        });
    };

    $.fn.createRatingModal = function(){
        var that=this;
        var modalTitle = ['<h4 class="modal-title" style="white-space: nowrap"><strong>', $(this).data('title'), '</strong></h4>'].join('');
            var modalHeader = ['<div class="modal-header">', modalTitle, '</div>'].join('');

        // Instructions defaults to no text; text injected by JavaScript on ratings load and save.
        var instructions = '<div class="help-block ratingModalInstruct"/>';
        var starRating = [
            '<fieldset class="rating">',
              '<input',
                'name="ratingmodal"',
                  'type="radio"',
                  'id="ratingmodal5"',
                  'value="5"',
                  'aria-label="5 stars">',
              '<label',
                  'for="ratingmodal5"',
                  'title="5 stars">',
                  '☆',
              '</label>',

              '<input',
                  'name="ratingmodal"',
                  'type="radio"',
                  'id="ratingmodal4"',
                  'value="4"',
                  'aria-label="4 stars">',
              '<label',
                  'for="ratingmodal4"',
                  'title="4 stars">',
                  '☆',
              '</label>',

              '<input',
                  'name="ratingmodal"',
                  'type="radio"',
                  'id="ratingmodal3"',
                  'value="3"',
                  'aria-label="3 stars">',
              '<label',
                  'for="ratingmodal3"',
                  'title="3 stars">',
                  '☆',
              '</label>',

              '<input',
                  'name="ratingmodal"',
                  'type="radio"',
                  'id="ratingmodal2"',
                  'value="2"',
                  'aria-label="2 stars">',
              '<label',
                  'for="ratingmodal2"',
                  'title="2 stars">',
                  '☆',
              '</label>',

              '<input',
                  'name="ratingmodal"',
                  'type="radio"',
                  'id="ratingmodal1"',
                  'value="1"',
                  'aria-label="1 star">',
              '<label',
                  'for="ratingmodal1"',
                  'title="1 star">',
                  '☆',
              '</label>',
            '</fieldset>'
          ].join(' ');
        var modalBody = [
            '<div class="modal-body style="font-size:2em; white-space:nowrap;">',
              instructions,
              starRating,
            '</div>'
          ].join('');

        var closeButton = [' <button type="button" class="btn btn-default" data-dismiss="modal">', $(this).data('close.button.label') ,'</button>'].join('');
        var saveButton = ['<button type="button" class="btn btn-primary ratingModalSaveButton disabled">', $(this).data('save.button.label') , '</button>'].join('');
            var modalFooter = ['<div class="modal-footer">', closeButton, saveButton, '</div>'].join('');
        var modalContent = ['<div class="modal-content" style="display:inline-block">', modalHeader, modalBody, modalFooter, '</div>'].join('');
        var modalDialog = ['<div class="modal-dialog ratePortletModal-dialog{@ID}" style="text-align:center; position:static">', modalContent, '</div>'].join('');
        $(this).append(modalDialog);
        $(this).modal('hide');
        $(this).find('.ratingModalSaveButton').click(
            function(e) {
                var portletRating = $(that).find('input[type=radio][name=ratingmodal]:checked').val();
                var saveUrl = $(that).data('saveurl');
                $.ajax({
                    url: saveUrl,
                    data: {rating: portletRating},
                    type: 'POST',
                    success: function(){
                        // hide the modal first to prevent flicker on updating the instructions in the modal
                        $(that).modal('hide');
                        up.notify($(that).data('rating.save.successful'), 'TopCenter', 'success');
                        // delay adjusting instructions to afford time for the modal to have actually hidden first
                        setTimeout(function() {
                            $(that).find('.ratingModalInstruct').text($(that).data('rating.instructions.rated'));
                        }, 1000);
                    },
                    error: function(){
                        // Dismiss the modal even on error.
                        $(that).modal('hide');
                        up.notify($(that).data('rating.save.unsuccessful'), 'TopCenter', 'error');
                    }
                });

            }
        );
        $(this).on('show.bs.modal', function(e) {
            $(that).find('.ratingModalSaveButton').addClass("disabled");
            var getUrl = $(that).data('geturl');
            $.ajax({
                url: getUrl,
                type: 'GET',
                cache: false,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                success: function(data){
                    if(data.rating === null){
                        $(that).find('.ratingModalInstruct').text($(that).data('rating.instructions.unrated'));
                    } else {
                        $(that).find('.ratingModalSaveButton').removeClass("disabled");
                        $(that).find('.ratingModalInstruct').text($(that).data('rating.instructions.rated'));
                    }
                },
                error: function(data){
                    up.notify($(that).data('get.rating.unsucessful'), 'TopCenter', 'error');
                }
            });
            var initModalHeight = $(that).find('.modal-dialog').outerHeight();
            var userScreenHeight = $(window).outerHeight();
            $(that).find('.modal-dialog').css('transform', 'translate(0, 50%)');
            $(that).find('input').change(function() {
                $(that).find('.ratingModalSaveButton').removeClass("disabled");
            });
        });
    };
})(jQuery);
