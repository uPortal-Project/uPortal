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

<script type="text/javascript">
var up = up || {};

(function($, _) {

    up.analytics = up.analytics || {};
    up.analytics.model = ${up:json(data)};

    // copied directly from up-ga.js, need in order to include
    // tag information in script parameter
    var findPropertyConfig = function() {
        if (up.analytics.model == null) {
            return null;
        }

        if (_.isArray(up.analytics.model.hosts)) {
            var propertyConfig = _.find(up.analytics.model.hosts, function(
                propertyConfig
            ) {
                if (propertyConfig.name == up.analytics.host) {
                    return propertyConfig;
                }
            });

            if (propertyConfig != null) {
                return propertyConfig;
            }
        }
        return up.analytics.model.defaultConfig;
    };

    var props = findPropertyConfig();
    var tagId = props.propertyId;

    var s = document.createElement( 'script' );
    s.setAttribute( 'src', "https://www.googletagmanager.com/gtag/js?id=" + tagId );
    s.async = true;
    document.body.appendChild( s );

    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    up.gtag = gtag;

})(up.jQuery, up._);
</script>
