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

<div id="${n}" class="fl-widget portlet error view-detailed" role="section">

    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <p><spring:message code="errorportlet.main"/></p>

        <div class="breadcrumb">
            <portlet:actionURL var="userResetUrl">
                <portlet:param name="failedPortletWindowId" value="${ portletWindowId.stringId}"/>
            </portlet:actionURL>
            <span class="breadcrumb-1"><a data-href="${ userResetUrl }" href="javascript:void(0)"><spring:message code="errorportlet.reset"/></a></span>
        </div> <!-- end breadcrumbs -->

    </div> <!-- end sectionhead -->

</div> <!--  end portlet -->

<script type="text/javascript">
(function($) {
    // Reset requests must be an actionURL and a POST...
    $('#${n} .breadcrumb a').click(function() {
        var url = $(this).attr('data-href');
        var form = $('<form />', {
            action: url,
            method: 'POST',
            style: 'display: none;'
        });
        form.appendTo('body').submit();
    });
})(up.jQuery);
</script>

