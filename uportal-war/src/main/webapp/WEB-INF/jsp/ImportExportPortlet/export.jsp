<%--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>

<portlet:defineObjects/>

<div class="fl-widget portlet" role="section">
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 role="heading">Export Portlet Entities</h2>
        <h3>Select an entity to export</h3>
    </div>
    
    <div class="fl-col-flex2 portlet-toolbar" role="toolbar">
        <div class="fl-col">
            <ul>
                <li><a href="<portlet:renderURL><portlet:param name="view" value="import"/></portlet:renderURL>">Import</a></li>
                <li><a href="<portlet:renderURL><portlet:param name="view" value="delete"/></portlet:renderURL>">Delete</a></li>
            </ul>
        </div>
    </div>
    
    <div class="fl-widget-content portlet-body" role="main">
    
        <div class="portlet-section" role="region">
        
            <div class="portlet-section-body">

                <div class="portlet-note" role="note">
                    <p>You can allow/disallow entity types using Portlet 
                    Preferences.  See uPortal's portlet.xml file for details.</p>
                </div>
                
                <form method="POST" action="<portlet:actionURL><portlet:param name="action" value="doExport"/><portlet:param name="view" value="status"/></portlet:actionURL>">
                    
                    <p>
	                    <label class="portlet-form-label" for="entityType">Type:</label>
						<select id="entityType" name="entityType">
						    <option>[Select Type]</option>
						    <c:forEach items="${supportedTypes}" var="type">
						        <option value="${type}"><c:out value="${type}"/></option>
						    </c:forEach>
						</select>
						
						<label class="portlet-form-label" for="sysid">Id:</label>
						<input type="text" id="sysid" name="sysid"/>
					</p>
                    
                    <div class="portlet-button-group">
                        <input class="portlet-button portlet-button-primary" type="submit" value="Export"/>
                    </div>
                </form>

            </div>
        </div>
    </div>
</div>
