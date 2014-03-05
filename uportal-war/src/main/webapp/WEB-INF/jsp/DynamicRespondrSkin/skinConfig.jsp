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

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<portlet:actionURL var="formUrl"/>

<!-- Portlet -->
<div class="skin-config-portlet" role="section">

    <!-- Portlet Body -->
  <div class="portlet-body" role="main">

        <!-- Portlet Section -->
    <div id="${n}skinManagerConfig" class="portlet-section" role="region">

            <div class="portlet-section-body">

                <form role="form" class="form-horizontal" action="${ formUrl }" method="POST">
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><spring:message code="respondr.dynamic.skin.color1"/></label>
                        <input type="color" class="colorPicker" name="color1" value="${color1}"/>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><spring:message code="respondr.dynamic.skin.color2"/></label>
                        <input type="color" class="colorPicker" name="color2" value="${color2}"/>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><spring:message code="respondr.dynamic.skin.color3"/></label>
                        <input type="color" class="colorPicker" name="color3" value="${color3}"/>
                    </div>
                    <div class="buttons">
                        <input type="submit" class="button primary" name="save" value='<spring:message code="save"/>'/>
                        <input type="submit" class="button secondary" name="cancel" value='<spring:message code="cancel"/>'/>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
