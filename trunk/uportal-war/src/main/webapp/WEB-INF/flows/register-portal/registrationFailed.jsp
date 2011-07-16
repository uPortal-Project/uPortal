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

<!-- Portlet -->
<div class="fl-widget portlet portal-reg view-failed" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
    	<h2 class="title" role="heading">Portal Registration</h2>
    </div>
    
    <!-- Portlet Content -->
	<div class="fl-widget-content content portlet-content" role="main">   
		
        <!-- Messages -->
        <div class="portlet-msg-error portlet-msg error" role="alert">
            <div class="titlebar">
            	<h3 class="title">Registration Error</h3>
            </div>
            <div class="content">
                <p>
                    An unexpected error occured while submitting your registration. Please
                    either try submitting your registration again or check the portal log
                    file for related exceptions. 
                </p>
            </div>
        </div>
        
        <!-- Buttons -->
        <div class="buttons">
            <portlet:actionURL var="backToStartUrl">
                <portlet:param name="execution" value="${flowExecutionKey}" />
                <portlet:param name="_eventId" value="registrationForm" />
            </portlet:actionURL>
            <a class="button" href="${backToStartUrl}">Back</a>
        </div>
    
    </div> <!-- end: portlet-content -->
</div> <!-- end:portlet -->