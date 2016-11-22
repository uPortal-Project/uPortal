<%--

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

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<c:set var="rootContext">${pageContext.request.contextPath}</c:set>

<script type="text/template" id="${n}options-menu">
    <li>
        <a href="javascript:;" title='<spring:message code="link.to" text="Link to ..." />' data-toggle="modal" data-target="#${n}copy-modal" id="${n}linkto">
            <spring:message code="link.to" text="Link to ..."/>
        </a>
    </li>
    <li class="divider"></li>
    <li>
        <spring:message code="add.this.portlet.to.my.favorite" text="Add this Portlet to My Favorites" var="atptmfTitle"/>
        <a href="javascript:;" title="${atptmfTitle}"
                class="{% if (isFavorite) { print('marketplace_remove_favorite'); } else { print('marketplace_add_favorite'); } %}">
            {% if (isFavorite) { %}
                <i class="fa fa-star"></i>
            {% } else { %}
                <i class="fa fa-star-o"></i>
            {% } %}
            <span>
                <spring:message code="my.favorites" text="My Favorites"/>
            </span>
        </a>
    </li>
    <li>
        <a href="javascript://" disabled>
                <span>
                    <spring:message code="marketplace.add.to.tab" text="Add portlet to tab:"/>
                </span>
        </a>
    </li>
    {% _.each(tabs, function(tab) { %}
        <li>
            <a href="javascript:;" class="marketplace_add_to_tab_link" data-tab-id="{%= tab.id %}">
                <span>
                    {%- tab.name %}
                </span>
            </a>
        </li>
    {% }); %}
</script>

<script type="text/template" id="${n}screen-shots">
    {% if (screenShots.length > 0) { %}
        <div class="row">
            <div class="col-md-12">
                <h2>
                    <spring:message code="screenshots.cap" text="Screenshots/Videos"/>
                </h2>
            </div>
        </div>
        <div class="row">
            <div class="col-md-offset-3 col-xs-6">
                <div class="carousel-container">
                    <div id="${n}marketplace_screenshots_and_videos" class="carousel slide" data-ride="carousel" data-interval="9000" data-wrap="true">
                        <div class="carousel-inner marketplace_carousel_inner" role="listbox">
                            {% _(screenShots).each(function(screen, idx) { %}
                                <div class="item marketplace_screen_shots {% if (idx === 0) { %} active {% } %}">
                                    <img src="{%= screen.url %}" alt="screenshot for portlet">
                                        {% _(screen.captions).each(function(caption) { %}
                                            <div class="carousel-caption">
                                                <h3>{%- caption %}</h3>
                                            </div>
                                        {% }); %}
                                    </img>
                                </div>
                            {% }); %}
                        </div>
                        {% if (screenShots.length > 1) { %}
                            <ol class="carousel-indicators marketplace_carousel_indicators">
                                {% _(screenShots).each(function(screen, idx) { %}
                                    <li data-target="#${n}marketplace_screenshots_and_videos" data-slide-to="{%= idx %}"></li>
                                {% }); %}
                            </ol>
                            <a class="left carousel-control carousel-marketplace-control" href="#${n}marketplace_screenshots_and_videos" role="button" data-slide="prev">
                                <span class="glyphicon glyphicon-chevron-left"></span>
                            </a>
                            <a class="right carousel-control carousel-marketplace-control" href="#${n}marketplace_screenshots_and_videos" role="button" data-slide="next">
                                <span class="glyphicon glyphicon-chevron-right"></span>
                            </a>
                        {% } %}
                    </div>
                </div>
            </div>
        </div>
    {% } %}
</script>

<div id="${n}">
    <div>
        <div class="row">
            <div class="col-xs-1">
                <portlet:renderURL var="initialViewURL" windowState="MAXIMIZED"/>
                <a class="btn btn-default" href="${initialViewURL}"><i class="fa fa-arrow-left"></i> <spring:message code="back.to.list" text="Back to List"/></a>
            </div>
        </div>
        <div class="row header-info-wrapper margin-top">
            <div class="col-sm-1 header-img">
                <c:url value="/media/skins/icons/mobile/default.png" var="defaultIcon"/>
                    <c:choose>
                        <c:when test="${empty marketplaceEntry.getParameter('mobileIconUrl')}">
                            <img src="${defaultIcon}">
                        </c:when>
                        <c:otherwise>
                            <img src="${marketplaceEntry.getParameter('mobileIconUrl').value}">
                        </c:otherwise>
                </c:choose>
            </div>
            <div class="col-sm-11">
                <div class="marketplace_description_title">
                    <h1 class = "fivepx-margin-top">${marketplaceEntry.title}</h1>
                </div>
                <div class="marketplace_description_body">
                    <p>${marketplaceEntry.description}</p>
                </div>
            </div>
			<div class="${n}go_button margin-top col-xs-12">
				<div class="btn-group marketplace_button_group">
					<a href="${marketplaceEntry.renderUrl}" id="marketplace_go_button" class="btn btn-default btn-sm marketplace_dropdown_button" role="button">
						<spring:message code="go" text="Go"/>
					</a>
					<button type="button" class="btn btn-default btn-sm dropdown-toggle marketplace_dropdown_button" data-toggle="dropdown">
						<spring:message code="options" text="Options"/>
						<span class="caret"></span>
					</button>
					<ul class="dropdown-menu marketplace_dropdown_menu" role="menu">
					</ul>
					<button type="button" class="btn btn-default btn-sm" data-toggle="modal" data-target="#${n}-feedback-modal"><span class = "fa fa-pencil"></span><span class = "padding-left">Rate this application</span></button>
				</div>
  				<!-- Modal -->
				<div class="modal fade" id="${n}-feedback-modal" role="dialog">
					<div class="modal-dialog">
    
						<!-- Modal content-->
						<div class="modal-content">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal">&times;
								</button>
									<h4 class="modal-title">Rate the ${marketplaceEntry.title}
									</h4>
 							</div>
								<div class="modal-body">
          					<!-- 	<div class="row">
				            <div class="col-xs-12"> -->
									<div class="marketplace_user_rating">
										<h3><spring:message code="rate.this.portlet" text="Rate this portlet"/>
										</h3>
										<div id="${n}marketplace_rating_instructions" class="help-block">
										</div>
										<form id="${n}save_rating_form">
											<div class="form-group">
												<input id="${n}marketplace_user_rating" placeholder="Write a review.." type="number" data-max="5" data-min="1" value="${marketplaceRating.rating}" name="rating" class="rating"/>
											</div>
											<c:if test="${enableReviews}">
												<div class="form-group">
													<textarea id="${n}marketplace_user_review_input" name="review" class="form-control col-xs-12 col-md-6" rows="3"></textarea>
														<div id="${n}input_chars_remaining" class = "character-limit"></div>
												</div>
											</c:if>				                      
										</form>
									</div>
				
				          <!--   </div>
				        </div> -->
								</div>
								<div class="modal-footer">
									<a id="${n}marketplace_user_rating_submit_button" class="btn btn-primary disabled" href = "#">Submit</a>
									<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
								</div>
							</div>
      
						</div>
					</div>
				
				</div>
			</div>
        <div class="row">
            <div class="col-sm-12 screen-shot-section"></div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <h2><spring:message code="rating.and.review.cap" text="Ratings & Reviews"/></h2>
                <div class="col-xs-3 marketplace_average_rating">
                    <div>
                        <input type="number" data-max="5" data-min="1" value="${marketplaceEntry.rating}" data-readonly="true" name="My Rating System" id="Demo" class="rating"/>
                    </div>
                </div>
                <div id="col-xs-9 marketplace_users_rated">
                    <span id="marketplace_average_rating_description">(${marketplaceEntry.userRated} reviews)</span>
                </div>
            </div>
        </div>
        <c:if test="${not empty  marketplaceEntry.portletReleaseNotes.releaseNotes}">
            <div class="row">
                <div class = "col-xs-12 col-md-4">
                    <br>
                    <p>
                        <span class="marketplace_section_header"><spring:message code="whats.new" text="What's New"/></span>
                        <c:if test="${not empty marketplaceEntry.portletReleaseNotes.releaseDate}">
                            <span class="marketplace_release_date"> (Released <joda:format value="${marketplaceEntry.portletReleaseNotes.releaseDate}" pattern="dd-MM-yyyy" />)</span>
                        </c:if>
                        
                    </p>
                    <p>
                        <c:if test="${not empty marketplaceEntry.portletReleaseNotes.releaseNotes}">
                            <ul class="marketplace_release_notes">
                                <c:forEach var="releaseNote"
                                           items="${marketplaceEntry.portletReleaseNotes.releaseNotes}">
                                    <li class="marketplace_release_note">- ${releaseNote}</li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </p>
                    <c:if test="${fn:length(marketplaceEntry.portletReleaseNotes.releaseNotes) gt
                    3}">
                        <span><a id="marketplace_show_more_less_link"><spring:message code="more" text="More"/></a></span>
                    </c:if>
                </div>
            </div>
        </c:if>
        
        <c:set var="relatedPortlets" value="${marketplaceEntry.relatedPortlets}"/>
        <c:if test="${not empty relatedPortlets}">
            <h2><spring:message code="related.portlets" text="Related Portlets"/></h2>
            <div class="row releated-portlets">
                <c:url value="/media/skins/icons/mobile/default.png" var="defaultIcon"/>
                <c:forEach var="relatedPortlet" items="${relatedPortlets}" varStatus="status">
                    <portlet:renderURL var="entryURL" windowState="MAXIMIZED" >
                        <portlet:param name="action" value="view"/>
                        <portlet:param name="fName" value="${relatedPortlet.fname}"/>
                    </portlet:renderURL>
                    
                    <div class = "image-panel-container">
	                    <div class="col-xs-12 col-sm-6 col-md-3 col-lg-2 image-panel">
							<a href="${entryURL}">
								<div>
									<div class="embed-responsive embed-responsive-4by3">
										<div class="embed-responsive-item image-background-styles">
											<c:choose>
												<c:when test="${empty relatedPortlet.getParameter('mobileIconUrl')}">
													<img src="${defaultIcon}">
												</c:when>
												<c:otherwise>
													<img src="${relatedPortlet.getParameter('mobileIconUrl').value}">
												</c:otherwise>
											</c:choose>
											<h3 class = "header">
												<c:out value="${relatedPortlet.title}"/>
											</h3>
										</div>
									</div>
									<div class="embed-responsive embed-responsive-16by9 description-background">
										<div class="embed-responsive-item description-text">
											<c:out value="${relatedPortlet.description}"/>
										</div>
									</div>
									<div class = "call-to-action">
										<a href="${entryURL}" class = "goto">
											Read more
										</a>
									</div>
								</div>
							</a>
	                    </div>
	                  </div>
                    <c:if test="${(status.index + 1) mod 6 == 0}">
                        <div class="clearfix"></div>
                    </c:if>
                </c:forEach>
            </div>
        </c:if>
        <c:set var="portletCategories" value="${marketplaceEntry.categories}"/>
        <c:if test="${not empty portletCategories}">
            <div class="row">
	            <div class = "col-xs-12 col-md-4">
	                <h3>
	                	<spring:message code="categories" text="CATEGORIES" />
	                </h3>
	                <ul class = "list-unstyled">
						<c:forEach var="portletCategory" items="${portletCategories}">
							<portlet:renderURL var="initialViewWithFilterURL" windowState="MAXIMIZED">
								<portlet:param name="initialFilter" value="${portletCategory}"/>
							</portlet:renderURL>
							 <li>
							 	<a href="${initialViewWithFilterURL}">${portletCategory}</a>
							 </li>
						</c:forEach>
					</ul>
				</div>
            </div>
        </c:if>
    </div>                      
</div>
<div class="modal fade" id="${n}copy-modal" tabindex="-1" role="dialog" aria-labelledby="LinkToModal" aria-hidden="true">
    <div class="modal-dialog" style="text-align:center">
        <div class="modal-content" style="white-space: nowrap">
            <h4 class="modal-title">
                <strong>
                    <spring:message code="link.to.this" text="Link to This"/>
                </strong>
            </h4>
            <div class="modal-body">
                <form class="form-horizontal" role="form">
                <div class="form-group">
                    <label for="inputDeep" class="col-sm-2 control-label"><spring:message code="link" text="Link"/></label>
                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="inputDeep"
                               value="${marketplaceEntry.renderUrl}"></input>
                    </div>
                </div>
                <c:if test="${not empty shortURL }">
                    <div class="form-group">
                        <label for="smallLink" class="col-sm-2 control-label"><spring:message code="shortLink" text="Short Link"/></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="smallLink" value="${shortURL}"></input>
                        </div>
                    </div>
                </c:if>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="close" text="Close"/></button>
            </div>
        </div>
    </div>
</div>
<portlet:resourceURL id="saveRating" var="saveRatingUrl" />
<portlet:resourceURL id="layoutInfo" var="portletInfoUrl">
    <portlet:param name="portletFName" value="${marketplaceEntry.fname}"/>
</portlet:resourceURL>

<script type="text/javascript">
(function($, Backbone, _) {
    $(document).ready( function () {
        // IMPORTANT: The tabs object is not a true model so it won't do
        // notifications on change.  If tabs needs to change, replace the
        // entire object, don't modify the one in this model.
        var MenuModel = Backbone.Model.extend({
            defaults: {
                loading: true,
                isFavorite: false,
                fname: null,
                tabs: []
            },
            update: function(data) {
                this.set({
                    isFavorite: data.favorite,
                    tabs: data.tabs
                });
            }
        });

        var templateSettings = {
            evaluate: /{%([\s\S]+?)%}/g,
            interpolate: /{%=([\s\S]+?)%}/g,
            escape: /{%-([\s\S]+?)%}/g
        };

        var MenuController = Backbone.View.extend({
            events: {
                'click .marketplace_add_to_tab_link': 'addPortlet',
                'click .marketplace_add_favorite': 'addFavorite',
                'click .marketplace_remove_favorite': 'removeFavorite'
            },
            template: _.template($('#${n}options-menu').text(), null, templateSettings),
            options: {
                portletChannelId: '${marketplaceEntry.id}'
            },

            initialize: function(options) {
                this.listenTo(this.model, 'change', this.render);
            },

            render: function() {
                var html;

                html = this.template(this.model.toJSON());
                this.$el.html(html);

                return this.$el;
            },

            addPortlet: function(evt) {
                var tabId, url, promise, tabName;

                evt.preventDefault();

                tabId = $(evt.currentTarget).data('tabId');
                tabName = this._getTabNameById(tabId);

                url = '<c:url value="/api/layout"/>';
                promise = $.ajax({
                    url: url,
                    data: {
                        action: 'addPortlet',
                        channelID: this.options.portletChannelId,
                        elementID: tabId,
                        position: ''
                    },
                    type: 'POST',
                    dataType: 'json'
                });

                promise.success(function() {
                    <spring:message code="success" text="Success" var="success"/>
                    <spring:message code="marketplace.add.portlet.success"
                            text="{0} Portlet has been added to tab {1}"
                            htmlEscape="false"
                            arguments="<strong>${success}!</strong>, <span class=\"tab-name\"></span>"
                            var="addPortletSuccessMsg"/>
                    var content = $('<div>${addPortletSuccessMsg}</div>');
                    content.find('.tab-name').text(tabName);

                    $('#up-notification').noty({
                        text: content.html(),
                        type: 'success'
                    });
                });

                promise.error(function() {
                    <spring:message code="marketplace.add.portlet.error"
                            text="Portlet could not be added to tab {0}"
                            htmlEscape="false"
                            arguments="<span class=\"tab-name\"></span>"
                            var="addPortletErrorMsg"/>
                    var content = $('<div>${addPortletErrorMsg}</div>');
                    content.find('.tab-name').text(tabName);
                    $('#up-notification').noty({
                        text: content.html(),
                        type: 'error'
                    });
                });

                promise.done(function() {
                    updateOptionsMenu();
                });
            },


            addFavorite: function(evt) {
                var promise;

                evt.preventDefault();

                evt.data = {
                    context: '${rootContext}',
                    portletId: this.options.portletChannelId
                };

                promise = up.addToFavorite(evt);
                promise.done(function() {
                    updateOptionsMenu();
                });
            },

            removeFavorite: function(evt) {
                var promise;

                evt.data = {
                    context: '${rootContext}',
                    portletId: this.options.portletChannelId
                };

                promise = up.removeFromFavorite(evt);
                promise.done(function() {
                    updateOptionsMenu();
                });
            },

            _getTabNameById: function(id) {
                var tabsArray, first;

                tabsArray = _.where(this.model.get('tabs'), {id: id});
                if (!tabsArray) {
                    return '';
                }

                first = _.first(tabsArray);
                if (!first) {
                    return '';
                }

                return first.name;
            }
        });

        var defaults = {
            textReviewCharLimit : 160,
            visibleReleaseNoteCount: 3
        };
        var remainingCharsAvailable = defaults.textReviewCharLimit;
        var optionsMenuModel = new MenuModel();
        var optionsMenuController = new MenuController({
            el: $('#${n} ul.marketplace_dropdown_menu'),
            model: optionsMenuModel
        });

        var updateOptionsMenu = function() {
            var promise;

            promise = $.ajax({
                url: '${portletInfoUrl}'
            });

            promise.then(function(data) {
                optionsMenuModel.update(data);
            }, function() {
                <spring:message code="marketplace.tablist.error"
                        text="An error occurred while loading the tab list"
                        htmlEscape="false"
                        var="tabListErrorMsg"/>
                $('#up-notification').noty({
                    text: '${tabListErrorMsg}',
                    type: 'error'
                });
            });
        };

        updateOptionsMenu();

        var screenShots = [
            <c:forEach var="screenShot" items="${marketplaceEntry.marketplaceScreenshots}">
            {
                url: '${screenShot.url}',
                captions: [
                    <c:forEach var="caption" items="${screenShot.captions}">
                        '${caption}',
                    </c:forEach>
                ]
            },
            </c:forEach>
        ];

        var ScreenShotModel = Backbone.Model.extend({
            defaults: {
                url: null,
                captions: []
            }
        });
        var screenShotCollection = new Backbone.Collection(screenShots, { model: ScreenShotModel });

        var ScreenShotController = Backbone.View.extend({
            template: _.template($('#${n}screen-shots').text(), null, templateSettings),
            selectors: {
                firstIndicator: '.marketplace_carousel_indicators > li:first-child'
            },
            collection: undefined,

            initialize: function(options) {
                var self, promises, allResolved;

                self = this;

                // kick off validation of each screen shot...
                promises = self.collection.map(function(screenShot) {
                    return self._validateScreenShot(screenShot);
                });

                // after all image URL have been checked, update the collection
                // with just the valid screenshots...
                allResolved = $.when.apply($, promises);
                allResolved.then(function() {
                    var valid;

                    valid = _.filter(arguments, function(screenShot) {
                        return screenShot != null;
                    });

                    // update the collection...
                    self.collection.reset(valid);
                });

                // after the model updates, re-render...
                self.listenTo(self.collection, 'reset', self.render);
            },

            render: function() {
                var self = this;

                this.$el.html(this.template({
                    screenShots: self.collection.toJSON()
                }));
                this.$(self.selectors.firstIndicator).addClass("active");

                return this.$el;
            },

            _validateScreenShot: function(screenShot) {
                var image, defer;

                defer = $.Deferred();
                image = new Image();
                image.onload = function() {
                    defer.resolve(screenShot);
                };
                image.onerror = function() {
                    defer.resolve(null);
                };
                image.src = screenShot.get('url');

                // return a deferred that always resolves successfully and
                // resolves to a screenshot object or null
                return defer.promise();
            }
        });

        // instantiate the actual screenshot controller.
        var screenShotController = new ScreenShotController({
            el: $('.screen-shot-section'),
            collection: screenShotCollection
        });

        var updateRatingInstructions = function(messageText) {
            $("#${n}marketplace_rating_instructions").text(messageText);
        };
        $(".marketplace_screen_shots:first").addClass("active");
        $(".marketplace_release_notes>li:nth-child(-n+"+
                defaults.visibleReleaseNoteCount+")").addClass("marketplace_show");
        $('#${n}copy-modal').modal('hide');
        var lengthLink = $('#marketplace_show_more_less_link');
        if(lengthLink.length>0) {
            lengthLink = lengthLink[0];
            lengthLink.onclick = toggleNotesDisplayLength;
            function toggleNotesDisplayLength() {
                var currentText = lengthLink.innerHTML;
                if($.trim(currentText) == 'More') {
                    $(".marketplace_release_notes>li").addClass("marketplace_show");
                    lengthLink.innerHTML="Less";
                }else{
                    $(".marketplace_release_notes>li:not(:nth-child(-n+"+
                            defaults.visibleReleaseNoteCount+"))").removeClass("marketplace_show");
                    lengthLink.innerHTML="More";
                }
            }
        }
        var htmlDecode = function(input) {
            var e = document.createElement('div');
            e.innerHTML = input;
            return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
        };
        if($("#${n}marketplace_user_rating").val().length>0) {
            $("#${n}marketplace_user_rating_submit_button").removeClass("disabled");
            updateRatingInstructions('<spring:message code="rating.instructions.rated"
            text='You have already rated "{0}"; adjust your rating if you wish.'
            arguments="${marketplaceEntry.title}"
            htmlEscape="true" />');
            $("#${n}marketplace_user_review_input").val(htmlDecode("<c:out value="${marketplaceRating.review}"/>"));
        }else{
            updateRatingInstructions('<spring:message code="rating.instructions.unrated"
            text='You have not yet rated "{0}".'
            arguments="${marketplaceEntry.title}"
            htmlEscape="true" />');
        }

        <c:if test="${enableReviews}">
	        // Optional Reviews feature...
	        var updateCharactersRemaining = function() {
	            if($("#${n}marketplace_user_review_input").val().length > defaults.textReviewCharLimit) {
	                $("#${n}marketplace_user_review_input").val($("#${n}marketplace_user_review_input").val().substr(0, defaults.textReviewCharLimit));
	            }
	            remainingCharsAvailable = defaults.textReviewCharLimit - $("#${n}marketplace_user_review_input").val().length;
	            $("#${n}input_chars_remaining").html('<spring:message code="characters.remaining" text="Characters Remaining: "/> &nbsp' + remainingCharsAvailable);
	            if(remainingCharsAvailable <= 10) {
	                $("#${n}input_chars_remaining").css("color","red");
	            }
	            else{
	                $("#${n}input_chars_remaining").css("color","black");
	            }
	        };
	        updateCharactersRemaining();
	        $("#${n}marketplace_user_review_input").keyup(function() {
	            updateCharactersRemaining();
	        });
        </c:if>

        
        // Submit function
        $("#${n}marketplace_user_rating_submit_button").click(function (e) {
        	
            var reviewText = $("#${n}marketplace_user_review_input").val();
            $.ajax({
                url: '${saveRatingUrl}',
                data: {
                	rating: $("#${n}marketplace_user_rating").val(),
                    portletFName: "${marketplaceEntry.fname}",
                    review: reviewText ? reviewText.trim() : ''},
                type: 'POST',
                success: function() {
                    $('#up-notification').noty({
                        text: '<spring:message code="rating.saved.successfully" text="Success"/>',
                        layout: 'TopCenter',
                        type: 'success'
                    });
                    updateRatingInstructions('<spring:message code="rating.instructions.rated.now"
                     text='You have now rated "{0}"; update your rating if you wish.'
                     arguments="${marketplaceEntry.title}"
                     htmlEscape="false"
                 />');
                     $('#${n}-feedback-modal').modal('toggle');
                     reviewText.val("");
                },
                error: function() {
                    $('#up-notification').noty({
                        text: '<spring:message code="rating.saved.unsuccessfully" text="Failure"/>',
                        layout: 'TopCenter',
                        type: 'error'
                    });
                    $('#${n}-feedback-modal').modal('toggle');
                    reviewText.val("");
                }
            });
            e.preventDefault();
        });
        $("#${n}marketplace_user_rating").change(function() {
            $("#${n}marketplace_user_rating_submit_button").removeClass("disabled");
        });
    });

} (up.jQuery, up.Backbone, up._));

</script>
