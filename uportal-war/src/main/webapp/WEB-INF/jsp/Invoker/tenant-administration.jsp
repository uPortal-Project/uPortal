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

<div id="${n}tenantAdmin" class="up-portlet-content-wrapper-inner">
    <h2>Portal Administration</h2>
    <ul>
        <li><a href="<c:url value="/p/portlet-admin"/>">Manage portlets</a></li>
        <li class="respondr-admin-link" style="display: none;">
            <a href="" data-lightbox-url="" data-lightbox-title="Manage This Skin">
                Manage This Skin
            </a>
        </li>
    </ul>
</div>
<script type="text/javascript">
up.jQuery(function() {
    if (up.dynamicSkinManagement) {
        up.jQuery('#${n}tenantAdmin .respondr-admin-link a')
                .attr('href', up.dynamicSkinManagement.configUrl)
                .attr('data-lightbox-url', up.dynamicSkinManagement.lightboxConfigUrl);
        up.jQuery('#${n}tenantAdmin .respondr-admin-link').show();
    }
});
</script>
