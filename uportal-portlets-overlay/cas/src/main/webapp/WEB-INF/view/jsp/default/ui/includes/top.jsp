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

<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<spring:theme var="isMobile" code="isMobile" text="false"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
	    <title>CAS &#8211; Central Authentication Service</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	    
	    <c:choose>
	       <c:when test="${ isMobile }">
		        <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;" />
		        <meta name="apple-mobile-web-app-capable" content="yes" />
		        <meta name="apple-mobile-web-app-status-bar-style" content="black" />
		    
		        <link type="text/css" rel="stylesheet" media="screen" href="/ResourceServingWebapp/rs/fluid/1.1.2/css/fss-framework-1.1.2.min.css"/>
		        <link type="text/css" rel="stylesheet" href="/uPortal/media/skins/muniversality/common/fss-mobile-iphone-layout.min.css" />    
		        <link type="text/css" rel="stylesheet" href="/uPortal/media/skins/muniversality/uportal3/portal.min.css" />
	       </c:when>
	       <c:otherwise>
		        <style type="text/css" media="screen">@import 'css/cas.css'/**/;</style>
		        <!--[if gte IE 6]><style type="text/css" media="screen">@import 'css/ie_cas.css';</style><![endif]-->
		        <script type="text/javascript" src="js/common_rosters.js"></script>
	       </c:otherwise>
	    </c:choose>
	    <link rel="icon" href="<%= request.getContextPath() %>/favicon.ico" type="image/x-icon" />
	</head>

	<body id="cas" onload="init();" class="fl-theme-iphone">
        <div class="flc-screenNavigator-view-container">
            <div class="fl-screenNavigator-view">
			    <div id="header" class="flc-screenNavigator-navbar fl-navbar fl-table">
			        <h1 id="app-name" class="fl-table-cell">Central Authentication Service (CAS)</h1>
			    </div>
		
			    <div id="content" class="fl-screenNavigator-scroll-container">
