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

<portlet:actionURL var="doAddTenantUrl">
    <portlet:param name="action" value="doAddTenant"/>
</portlet:actionURL>

<style>
#${n}tenantManager .form-fields li {
    list-style: none;
}

/* Down Caret*/
#${n}tenantManager #addInfoToggle.collapsed i:after {
    content: "\f0d7";
}
/* Up Caret*/
#${n}tenantManager #addInfoToggle i:after {
    content: "\f0d8";
}

#${n}tenantManager .field-error {
    display: none;
    padding: 3px;
    margin: 0 5px;
}
#${n}tenantManager .has-error div.field-error {
    display: block;
}
</style>

<div id="${n}tenantManager">
    <form id="addTenantForm" role="form" class="form-horizontal" action="${doAddTenantUrl}" method="post">
        <div class="form-group">
            <label for="tenantName" class="col-sm-2 control-label"><spring:message code="tenant.manager.name" /></label>
            <div class="col-sm-10">
                <input type="text" class="form-control" name="name" id="tenantName" placeholder="Enter tenant name">
                <div class="field-error bg-danger">Field error message</div>
            </div>
        </div>

        <c:forEach items="${tenantManagerAttributes}" var="attribute">
            <div class="form-group">
                <label for="${attribute.key}" class="col-sm-2 control-label"><spring:message code="${attribute.value}" /></label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" name="${attribute.key}" id="${attribute.key}">
                    <div class="field-error">Field error message</div>
                </div>
            </div>
        </c:forEach>

        <div class="text-right">
            <input type="hidden" name="fname" id="fname" value="">
            <button id="tenantFormSubmit" type="submit" class="btn btn-primary disabled">Submit</button>
            <a href="<portlet:renderURL />" class="btn btn-link">Cancel</a>
        </div>
    </form>

</div>
<script>
    up.jQuery(function() {
        var $ = up.jQuery;
        $('#tenantName').blur(function(evt) {
            var val = this.value;
            var $this = $(this);
            var $formGroup = $this.closest('div.form-group')
            var fname = document.getElementById('fname')

            if (!val.match(/^[\w\-\_\'\s]+$/)) {
                $formGroup.addClass('has-error');
                $('#tenantFormSubmit').addClass('disabled');
                fname.value = '';
                return;
            }

            val = val.replace(/[\s']/g, '_').toLowerCase();

            fname.value = val;

            $formGroup.removeClass('has-error');

            $('#tenantFormSubmit').removeClass('disabled');
        });
    });
</script>