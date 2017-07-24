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

<style type="text/css">
#${n}skinManagerConfig .loadingMessage {
    position: relative;
    top: -150px;
    left: 250px;
    font-size: 50px;
}
</style>

<portlet:actionURL var="saveUrl"><portlet:param name="action" value="update"/></portlet:actionURL>
<portlet:actionURL var="cancelUrl"><portlet:param name="action" value="cancel"/></portlet:actionURL>

<!-- Portlet -->
<div class="skin-config-portlet" role="section">

    <!-- Portlet Body -->
  <div class="portlet-body" role="main">

        <!-- Portlet Section -->
    <div id="${n}skinManagerConfig" class="portlet-section" role="region">

            <div class="portlet-section-body">

                <form id="${n}dynSkinForm" role="form" class="form-horizontal" action="${ saveUrl }" method="POST">

                    <div class="form-group">
                        <label for="{n}PREFdynamicSkinEnabled" class="col-sm-4 control-label"><spring:message code="respondr.dynamic.skin.enabled"/></label>
                        <input type="checkbox" class="dynamicSelection" id="{n}PREFdynamicSkinEnabled" name="PREFdynamicSkinEnabled"
                               value="true" ${empty PREFdynamicSkinEnabled ? '' : 'checked'}/>
                    </div>
                    <div class="form-group">
                        <label for="{n}PREFdynamicSkinName" class="col-sm-4 control-label"><spring:message code="respondr.dynamic.skin.skinName"/></label>
                        <select id="{n}PREFdynamicSkinName" name="PREFdynamicSkinName">
                            <option value="${PREFdynamicSkinName}" selected>${PREFdynamicSkinName}</option>
                            <c:forEach items="${skinNames}" var="skinName">
                            <c:if test="${skinName != PREFdynamicSkinName}">
                            <option value="${skinName}">${skinName}</option>
                            </c:if>
                            </c:forEach>
                        </select>
                    </div>
                    <!-- WARNING:  If you remove any of the following HTML inputs, you must also remove them from portlet.xml. -->
                    <div class="dynamicItems hidden">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label for="{n}PREFcolor1" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color1"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor1" name="PREFcolor1" value="${PREFcolor1}"/>
                                </div>
                                <div class="form-group">
                                    <label for="{n}PREFcolor2" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color2"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor2" name="PREFcolor2" value="${PREFcolor2}"/>
                                </div>
                                <div class="form-group">
                                    <label for="{n}PREFcolor3" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color3"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor3" name="PREFcolor3" value="${PREFcolor3}"/>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label for="{n}PREFcolor4" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color4"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor4" name="PREFcolor4" value="${PREFcolor4}"/>
                                </div>
                                <div class="form-group">
                                    <label for="{n}PREFcolor5" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color5"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor5" name="PREFcolor5" value="${PREFcolor5}"/>
                                </div>
                                <div class="form-group">
                                    <label for="{n}PREFcolor6" class="col-sm-8 control-label"><spring:message code="respondr.dynamic.skin.color6"/></label>
                                    <input type="color" class="colorPicker dynamicItem" id="{n}PREFcolor6" name="PREFcolor6" value="${PREFcolor6}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="buttons">
                        <button type="submit" class="saveButton btn btn-default"><spring:message code="save"/></button>
                        <button type="button" class="cancelButton btn btn-default" onclick="window.location.href='${cancelUrl}'"><spring:message code="cancel"/></button>
                    </div>
                </form>
                <div class="loadingMessage hidden"><i class="fa fa-spinner fa-spin"></i></div>
            </div>
        </div>
    </div>
</div>

<script language="javascript" type="text/javascript">
<rs:compressJs>
(function() {

    // De-alias jQuery safely within this self-invoking function
    var $ = up.jQuery;

    var initDynSkin = function($, settings, portletSelector, formSelector) {
        var formUrl = $(settings.formSelector).attr('action');
        // The url contains &amp; which messes up spring webflow. Change them to & so parameters get passed through properly.
        var cancelUrl = "${cancelUrl}".replace(/&amp;/g,"&");

        var showLoading = function() {
            $(settings.formSelector).find(".cancelButton").prop("disabled", true);
            $(settings.formSelector).find(".saveButton").prop("disabled", true);
            $(settings.portletSelector).find(".loadingMessage").removeClass("hidden");
        };

        $(settings.formSelector).submit(function (event) {
            showLoading();
            $.ajax({
                url: formUrl,
                type: "POST",
                data: $(settings.formSelector).serialize()
                })
                // We don't capture error since there is no way the portal will return a different status code on an
                // action url. If there is an error we'd get a web page with content that displayed an error message.
                .success(function(data, textStatus, jqXHR) {
                    // Since it saved successfully, invoke cancelUrl to gracefully exit config mode and return to
                    // Portlet configuration without spring webflow errors. I'd have thought we could do a portletUrl
                    // that sets portletMode=View but it does not seem to work.
                    window.location.href=cancelUrl;
                });
            event.preventDefault();
        });

        // Enable or disable the dynamic skins based on whether dynamic is checked. Also setup change event to handle
        // changes.
        var enableOrDisableDynamicFields = function() {
            if ($(settings.formSelector).find(".dynamicSelection").is(':checked')) {
                $(settings.formSelector).find(".dynamicItems").removeClass("hidden");
            } else {
                $(settings.formSelector).find(".dynamicItems").addClass("hidden");
            }
        };
        enableOrDisableDynamicFields();
        $(settings.formSelector).find(".dynamicSelection").change(enableOrDisableDynamicFields);
    };

    $(function() {
        initDynSkin($, {
            portletSelector: "#${n}skinManagerConfig",
            formSelector: "#${n}dynSkinForm"
        });
    });

})();
</rs:compressJs>
</script>
