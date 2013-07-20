<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${not empty images}">

<portlet:actionURL var="savePreferencesUrl">
    <portlet:param name="action" value="savePreferences"/>
</portlet:actionURL>

<style>

    /* Styles to set the background to the previously-selected image */
    <c:if test="${backgroundImage ne null}">
    ${backgroundContainerSelector} {
        background-image: url("${backgroundImage}");
        background-size: cover;
    }
    ${applyOpacityTo} {
        opacity: ${opacityCssValue};
    }
    </c:if>

    /* Syles for the background selector widget itself  */
    #${n}background-edit-control {
        min-width:150px;
        float:right;
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
        text-align:center;
        max-height:90%;
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
        width:80%;
        margin: auto 10%;
    }
    #${n}background-edit-control .background-edit-menu .caption {
        display:inline-block;
        position:relative;
    }
    #${n}background-edit-control .background-edit-menu a.active, #${n}background-edit-control .background-edit-menu a:hover {
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
        text-shadow: 0px 1px 1px rgba(255, 255, 255, 0.75);
        background: rgb(245, 245, 245) linear-gradient(to bottom, rgb(255, 255, 255), rgb(230, 230, 230)) repeat-x;
        border: 1px solid rgb(204, 204, 204);
        border-bottom-color: rgb(179, 179, 179);
        border-image: none;
        border-radius: 4px 4px 4px 4px;
        box-shadow: 0px 1px 0px rgba(255, 255, 255, 0.2) inset, 0 1px 2px rgba(0, 0, 0, 0.05);
    }
    #${n}background-edit-control .background-edit-button .edit-button-image {
        background: url("/ResourceServingWebapp/rs/famfamfam/silk/1.3/image_edit.png") no-repeat;
        height: 16px;
        width: 16px;
        display: inline-block;
        padding-right: 3px;
    }
    #${n}background-edit-control .background-edit-button .edit-text {
        position:relative;
        bottom:3px;
    }
    #${n}background-edit-control .background-edit-button.active {
        background:rgb(230, 230, 230);
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
        <c:forEach var="image" items="${images}" varStatus="status">
            <a href="#">
                <img src="${image}" />
                <span class="caption">Background ${status.index + 1}</span>
            </a>
        </c:forEach>
    </div>
    <form class="background-edit-form" action="${savePreferencesUrl}" method="post">
        <input class="background-value" type="hidden" name="backgroundImage" value="${backgroundImage}" />
        <input class="redirect-location" type="hidden" name="redirectLocation" value="" />
   </form>
</div>
<script>
    up.jQuery(function() {
        var $ = up.jQuery,
        $editButton = $('#${n}background-edit-control .background-edit-button'),
        $menu = $('#${n}background-edit-control .background-edit-menu'),
        $backgroundEditForm = $("#${n}background-edit-control .background-edit-form"),
        $backgroundValue = $('#${n}background-edit-control .background-value');
        
        $('#${n}background-edit-control .redirect-location').attr('value', window.location.pathname);

        $editButton.click(function () {
            $menu.width($editButton.outerWidth());
            $editButton.toggleClass('active');
            $menu.toggle('slide', {direction: 'up'});
        });
        $menu.find('a').click(function(e) {
            e.preventDefault();
            var selectedValue = $(this).find('img').attr('src') || '';
            $backgroundValue.attr('value', selectedValue);
            $backgroundEditForm.submit();
        });
    });
</script>

</c:if>
