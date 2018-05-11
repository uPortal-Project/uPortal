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
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${not empty images}">

<portlet:actionURL var="savePreferencesUrl">
    <portlet:param name="action" value="savePreferences"/>
</portlet:actionURL>

<style id="#${n}background-preference-stylesheet">

    /* Styles to set the background to the previously-selected image */
    <c:if test="${backgroundImage ne null}">
    ${backgroundContainerSelector} {
        background-image: url("${backgroundImage}");
    }
    </c:if>

    /* Syles for the background selector widget itself  */
    #${n}background-edit-control {
        min-width: 150px;
        float: right;
        margin-right: 12px;
    }

    #${n}background-edit-control .background-edit-menu {
        background: rgb(250, 250, 250);
        box-shadow: 0px 5px 10px rgba(0, 0, 0, 0.2);
        border: 1px solid rgb(204, 204, 204);
        font-family: Arial, Helvetica, sans-serif;
        display:none;
        position:absolute;
        overflow-x: hidden;
        overflow-y: scroll;
        z-index: 10000;
        text-align: center;
        max-height: 515px;
    }

    #${n}background-edit-control .background-edit-menu a {
        color: #3c3933;
        display: block;
        font-size: 16px;
        font-weight: bold;
        text-decoration: none;
        border-top: 1px solid #eff0e2;
        border-bottom: 1px solid #d4d2c7;
        padding: 14px;
    }

    #${n}background-edit-control .background-edit-menu img {
        width: 80%;
        margin: auto 10%;
    }

    #${n}background-edit-control .background-edit-menu .caption {
        display: inline-block;
        position: relative;
    }

    #${n}background-edit-control .background-edit-menu a.active,
    #${n}background-edit-control .background-edit-menu a:hover {
        background: rgb(0, 129, 194);
        color: #fff;
    }

    #${n}background-edit-control .background-edit-button {
        cursor: pointer;
        width: 100%;
        padding: 5px 5px 2px 5px;
        position:relative;
        right:3px;
        font-size: 14px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        cursor: pointer;
        color: rgb(51, 51, 51);
        text-shadow: 0 1px 1px rgba(255, 255, 255, 0.75);
        background-color: #DEDEDE;
        background: rgb(245, 245, 245) linear-gradient(to bottom, rgb(255, 255, 255), rgb(230, 230, 230)) repeat-x;
        border: 1px solid rgb(204, 204, 204);
        border-bottom-color: rgb(179, 179, 179);
        border-image: none;
        border-radius: 4px 4px 4px 4px;
        box-shadow: 0 1px 0 rgba(255, 255, 255, 0.2) inset, 0 1px 2px rgba(0, 0, 0, 0.05);
    }

    #${n}background-edit-control .background-edit-button .edit-button-image {
        background: url("/ResourceServingWebapp/rs/famfamfam/silk/1.3/image_edit.png") no-repeat;
        height: 16px;
        width: 16px;
        display: inline-block;
        padding-right: 3px;
    }

    #${n}background-edit-control .background-edit-button .edit-text {
        position: relative;
        bottom: 3px;
    }

    #${n}background-edit-control .background-edit-button.active {
        background: rgb(230, 230, 230);
    }
</style>

<div id="${n}background-edit-control">
    <div class="background-edit-button fl-container-flex">
        <div class="edit-button-image"></div>
        <span class="edit-text">Change Background Image</span>
    </div>
    <div class="background-edit-menu">
        <a href="#">
            <span class="caption">None</span>
        </a>
        <c:forEach var="image" items="${thumbnailImages}" varStatus="status">
            <a href="#">
                <img src="${image}" alt="" role="presentation"/>
                <span class="caption">${imageCaptions[status.index]}</span>
            </a>
        </c:forEach>
    </div>
    <form class="background-edit-form" action="${savePreferencesUrl}" method="POST">
        <input class="background-value" type="hidden" name="backgroundImage" value="${backgroundImage}" />
   </form>
</div>

<script id="backgroundScript">
(function($){
    var BackgroundChanger = function() {
        var elements = {
            button: '',
            menu: '',
            form: '',
            background: ''
        };

        var changeBackground = function() {
            /*
             * NOTE:  We've recently started specifying CSS pseudo-elements as
             * the backgroundContainerSelector (e.g. #portalPageBody::before).
             * Unfortunately, pseudo-elements aren't technically a part of the
             * DOM, and (therefore) you can't obtain a jQuery object that wraps
             * one.  Instead of manipulating CSS with jQuery, we will write a
             * new <style> element to the bottom of the page.
             */
            if (elements.background) {
                $('body').css('background-color','transparent');
                var styleElement = '<style>${backgroundContainerSelector} { background-image: url(' + elements.background + '); }</style>';
                $(styleElement).appendTo('body');
            } else {
                var styleElement = '<style>${backgroundContainerSelector} { background-image: none; }</style>';
                $(styleElement).appendTo('body');
            }
        };

        var setBackground = function(el) {
            elements.background = $(el).find('img').attr('src') || '';
            elements.background = elements.background.replace('/thumbnails', '');
            elements.form.find('.background-value').val(elements.background);

            changeBackground();

            elements.form.submit();
        };

        var toggleMenu = function() {
            elements.button.toggleClass('active');
            elements.menu.slideToggle();
        };

        var setWidth = function() {
            elements.menu.width(elements.button.outerWidth());
        }

        var privateInit = function() {
            elements.button = $('#${n}background-edit-control .background-edit-button');
            elements.menu = $('#${n}background-edit-control .background-edit-menu');
            elements.form = $("#${n}background-edit-control .background-edit-form");

            elements.form.submit(function() {
                $.post(this.action, elements.form.serialize());
                return false;
            });
            elements.button.click(function () {
                setWidth();
                toggleMenu();
            });

            elements.menu.find('a').click(function(e) {
                e.preventDefault();
                setBackground(this);

                toggleMenu();
            });
        };

        (function init() {
            $(function() {
                privateInit();
            });
        })();

        return {
            init: function() {
                 privateInit();
            }
        };

    }();
})(up.jQuery);

</script>
</c:if>
