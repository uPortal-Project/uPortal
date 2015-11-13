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
<%-- escapeXml=false is required to allow config mode to work within the Spring Webflow of Manage Portlets. Otherwise
     the executionKey is lost because &pP_execution=e2s3 becomes &amp;amp;pP_execution=e2s3 --%>
<portlet:actionURL var="formUrl" escapeXml="false">
    <portlet:param name="action" value="updateConfiguration"/>
</portlet:actionURL>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">

    <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">SQL Query Portlet Configuration</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">

    <form:form class="form-horizontal" role="form" modelAttribute="form" action="${formUrl}" method="POST">
    
    <!-- Portlet Messages -->
    <spring:hasBindErrors name="form">
        <div class="portlet-msg-error" role="alert">
            <form:errors path="*" element="div"/>
        </div> <!-- end: portlet-msg -->
    </spring:hasBindErrors>
        
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
    
        <p>
    	    <label class="portlet-form-field-label">SQL query:</label>
	       <form:textarea path="sqlQuery" rows="4" cols="120"/>
            <div>Allows Spring Expression Language (SpEL) expressions such as \${request.contextPath}, \${userInfo['user.login.id']}, or @MyBeanName<br/>
              You typically define a default using the elvis operator ?: in case an attribute has no value to avoid sql grammar errors; e.g. something like<br/>
              select * from EB_CONTACT_TABLE where pidm = \${userInfo['pidm']?:0} and standard_priority<>0 order by standard_priority<br>
              select * from UP_USER where  user_name='\${userInfo['user.login.id']?:''}'<br/>
              select '\${@PortalDb.class.toString()?:unknown}' as className from up_user where user_name='admin';
            </div>
	    </p>

	    <p>
    	    <label class="portlet-form-field-label">Spring data source bean name:</label>
	       <form:input path="dataSource" size="50"/>
	    </p>
	
        <p>
	       <label class="portlet-form-field-label">Spring view:</label>
	       <form:input path="viewName" size="50"/>
	    </p>

        <p>
            <label class="portlet-form-field-label">Cache name:</label>
            <form:input path="cacheName" size="50"/>
            <div><em>Enter 'org.jasig.portal.portlets.sqlquery.SqlQueryPortletController.queryResults' for preconfigured cache.</em> If other than the preconfigured cache name, you must create a cache in ehcache.xml with the name you enter here.
                 <br/>Leave empty to disable caching. <em>WARNING!</em> This would impact performance and scalability!
             </div>

        </p>

      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Portlet Buttons -->
    <div class="portlet-button-group">
      <input class="portlet-button portlet-button-primary" type="submit" name="Save" value="Save"/>
      <input class="portlet-button" type="submit" name="Cancel" value="Cancel"/>
    </div>
    
    </form:form> <!-- End Form -->
            
    </div> <!-- end: portlet-body -->
        
</div> <!-- end: portlet -->