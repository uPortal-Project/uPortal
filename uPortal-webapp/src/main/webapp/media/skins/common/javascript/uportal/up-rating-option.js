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
        var instructions =
            ['<div class="help-block ratingModalInstruct"/>'];
        var starRating =
            ['<input type="number" data-max="5" data-min="1" value="0" data-readonly="false" ' +
                'data-empty-value="0" class="ratingModalInput"/>'];
        var modalBody =
            ['<div class="modal-body style="font-size:2em; white-space:nowrap;">',
                instructions, starRating,
                '</div>'].join('');

        var closeButton = [' <button type="button" class="btn btn-default" data-dismiss="modal">', $(this).data('close.button.label') ,'</button>'].join('');
        var saveButton = ['<button type="button" class="btn btn-primary ratingModalSaveButton disabled">', $(this).data('save.button.label') , '</button>'].join('');
            var modalFooter = ['<div class="modal-footer">', closeButton, saveButton, '</div>'].join('');
        var modalContent = ['<div class="modal-content" style="display:inline-block">', modalHeader, modalBody, modalFooter, '</div>'].join('');
        var modalDialog = ['<div class="modal-dialog ratePortletModal-dialog{@ID}" style="text-align:center; position:static">', modalContent, '</div>'].join('');
        $(this).append(modalDialog);
        $(this).modal('hide');
        $(this).find('.ratingModalSaveButton').click(
            function(e) {
                var portletRating = $(that).find('input').val();
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
                    if(data.rating===null){
                        $(that).find('input').val(0);
                        $(that).find('input').redraw(0);
                        $(that).find('.ratingModalInstruct').text($(that).data('rating.instructions.unrated'));
                    }else{
                        $(that).find('.ratingModalSaveButton').removeClass("disabled");
                        $(that).find('input').val(data.rating);
                        $(that).find('input').redraw(data.rating);
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
            $(that).find('.modal-dialog').css('-ms-transform', 'translate(0, 50%)'); //IE 9
            $(that).find('.modal-dialog').css('-webkit-transform', 'translate(0, 50%)'); //Safari and Chrome    
            $(that).find('input').change(function() {
                $(that).find('.ratingModalSaveButton').removeClass("disabled");
            });
        });
    };
})(jQuery);
