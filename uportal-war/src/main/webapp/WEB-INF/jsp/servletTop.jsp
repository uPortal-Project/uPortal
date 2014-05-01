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

<!DOCTYPE html>
<html lang="en-US" class="respondr">
<head>
    <title>uPortal by Jasig</title>

    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black"/>
    <meta name="description" content="uPortal by Jasig: The academic open source portal built for higher education by higher education"/>
    <meta name="keywords" content="portal, uPortal, academic, higher education, open source, enterprise, JA-SIG, JASIG, Jasig"/>

    <%-- TODO: use rs tags --%>

    <link href="https://fonts.googleapis.com/css?family=Oxygen" rel="stylesheet" type="text/css"/>
    <link href="/ResourceServingWebapp/rs/normalize/2.1.2/normalize-2.1.2.min.css" rel="stylesheet" type="text/css"/>
    <link href="/ResourceServingWebapp/rs/fontawesome/4.0.3/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
    <link href="/ResourceServingWebapp/rs/jqueryui/1.10.3/theme/smoothness/jquery-ui-1.10.3-smoothness.min.css" rel="stylesheet" type="text/css"/>

    <script src="/ResourceServingWebapp/rs/modernizr/2.6.2/modernizr-2.6.2.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/jquery/1.10.2/jquery-1.10.2.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/jquery-migrate/jquery-migrate-1.2.1.min.js" type="text/javascript"></script>
    <script src="/ResourceServingWebapp/rs/jqueryui/1.10.3/jquery-ui-1.10.3.min.js" type="text/javascript"></script>
    <script src="/ResourceServingWebapp/rs/bootstrap/3.1.1/js/bootstrap.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/fluid/pre1.5.0-6cb52a5083/js/fluid-custom.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/underscore/1.5.2/underscore-1.5.2.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/jquery-plugins/noty/2.2.0/jquery.noty.packaged.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/datatables/1.9.4/media/js/jquery.dataTables.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/jquery-plugins/rating/1.0/bootstrap-rating-input.min.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/datatables/1.9.4/plugins/integration/bootstrap/3/dataTables.bootstrap.js" type="text/javascript"> </script>
    <script src="/ResourceServingWebapp/rs/datatables/1.9.4/extras/column-filter-widgets/js/ColumnFilterWidgets.js" type="text/javascript"> </script>
    <script src="/uPortal/rs/conflict-resolution/js/resolve-conflicts.min.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-util.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-portlet-registry.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-entity-registry.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-portlet-browser.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-layout-draggable-manager.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-layout-gallery.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-fragment-permissions-manager.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-subscription-browser.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-layout-selector.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-skin-selector.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-tab-manager.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-layout-persistence.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-url-provider.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-layout-preferences.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-autocomplete.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-showHideToggle.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/entity-selector.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-parameter-editor.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/flyout-nav.js" type="text/javascript"> </script>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-translator.js" type="text/javascript"> </script>
    <%-- No sidebar yet
    <script src="/uPortal/media/skins/common/javascript/uportal/up-sidebar-improved.js" type="text/javascript"> </script>
    --%>
    <%-- no printing
    <script src="/uPortal/media/skins/common/javascript/uportal/up-print-plugin.js" type="text/javascript"> </script>
    --%>
    <%--
    Not ready to deal with Google Analytics.
    <script src="/uPortal/media/skins/common/javascript/uportal/up-ga.js" type="text/javascript"> </script>
    --%>
    <script src="/uPortal/media/skins/common/javascript/uportal/up-rating-option.js" type="text/javascript"> </script>
    <%-- Not ready to give users ability to favorite from portlet
    <script src="/uPortal/media/skins/common/javascript/uportal/up-favorite.js" type="text/javascript"> </script>
    --%>

    <%-- Simulates the effect of the dynamic skin portlet --%>
    <link rel="stylesheet" type="text/css" href="/uPortal/media/skins/respondr/defaultSkin.css"/>

    <link rel="shortcut icon" href="/uPortal/favicon.ico" type="image/x-icon"/>

    <script type="text/javascript">
        var up = up || {};
        up.jQuery = jQuery.noConflict(true);
        up.fluid = fluid;
        fluid = null;
        fluid_1_4 = null;
        up._ = _.noConflict();
        up._.templateSettings = {
            interpolate : /{{=(.+?)}}/g,
            evaluate    : /{{(.+?)}}/g
        };
    </script>
</head>

<body class="up dashboard portal fl-theme-mist">

<div id="portletContent" class="fl-widget-content fl-fix up-portlet-content-wrapper round-bottom">

    <div class="up-portlet-content-wrapper-inner">
