<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<portlet:actionURL var="queryUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>
    
<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Title -->
  <div class="fl-widget-titlebar portlet-title" role="sectionhead">
    <h2 role="heading">Set Language Preference</h2>
  </div> <!-- end: portlet-title -->
  
  <!-- Portlet Body -->
  <div class="fl-widget-content portlet-body" role="main">
  
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="portlet-section-body">
        <form action="${queryUrl}" method="POST">
			<ul style="margin:0">
				<c:forEach items="${ locales }" var="locale">
				    <li style="list-style:none;padding:0.2em 0 0.2em 0">
					    <input type="radio" name="locale" value="${ locale.locale }" ${ locale.locale == currentLocale ? "checked" : '' }/>
					    <img src="/ResourceServingWebapp/rs/famfamfam/flags/${ locale.countryCode }.png"/>
					    ${ locale.displayValue }
				    </li>
				</c:forEach>
			</ul>
            <div class="portlet-button-group">
		  	   <input class="portlet-button portlet-button-primary" type="submit" value="Update" name="_eventId_updateLocale"/>
		  	</div>
		</form>
      </div>  
    </div>
    
  </div>

</div>
