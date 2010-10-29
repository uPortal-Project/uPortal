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

<div class="fl-widget portlet" role="section">
    <div class="fl-widget-titlebar portlet-title" role="sectionhead">
        <h2 role="heading">Import Portlet Entities</h2>
        <h3>Upload an entity to be imported</h3>
    </div>
    
    <div class="fl-col-flex2 portlet-toolbar" role="toolbar">
        <div class="fl-col">
            <ul>
                <li><a href="<portlet:renderURL><portlet:param name="view" value="export"/></portlet:renderURL>">Export</a></li>
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
				
				<form method="POST" enctype="multipart/form-data" action="<portlet:actionURL><portlet:param name="action" value="doImport"/><portlet:param name="view" value="status"/></portlet:actionURL>">
					<p>
	                    <label class="portlet-form-label" for="entityFile">File:</label>
	                    <input type="file" id="entityFile" name="entityFile"/>
					</p>
					
	                <div class="portlet-button-group">
	    				<input class="portlet-button portlet-button-primary" type="submit" value="Import"/>
	    		    </div>
				</form>

            </div>
        </div>
    </div>
</div>