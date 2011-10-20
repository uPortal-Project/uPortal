/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.importexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

/**
 * ImportExportPortletController controls the display of the import/export
 * portlet interface views.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class ImportExportPortletController {
	
	private static final String OWNER = "UP_SYSTEM";
	private static final String EXPORT_PERMISSION = "EXPORT_ENTITY";
	private static final String DELETE_PERMISSION = "DELETE_ENTITY";

    protected final Log log = LogFactory.getLog(getClass());

    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;
    private IPortalDataHandlerService portalDataHandlerService;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
    	this.personManager = personManager;
    }
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
	public void setPortalDataHandlerService(IPortalDataHandlerService portalDataHandlerService) {
        this.portalDataHandlerService = portalDataHandlerService;
    }

    /**
     * Display the entity import form view.
     * 
     * @param request
     * @return
     */
    @RequestMapping
    public String getImportView(PortletRequest request) {
    	return "/jsp/ImportExportPortlet/import";
    }
    
    /**
     * Display the entity export form view.
     * 
     * @param request
     * @return
     */
    @RequestMapping(params="action=export")
    public ModelAndView getExportView(PortletRequest request) {
    	Map<String,Object> model = new HashMap<String,Object>();
    
        // add a list of all permitted export types
    	final Iterable<IPortalDataType> exportPortalDataTypes = this.portalDataHandlerService.getExportPortalDataTypes();
    	final List<IPortalDataType> types = getAllowedTypes(request, EXPORT_PERMISSION, exportPortalDataTypes);
    	model.put("supportedTypes", types);
    	
        return new ModelAndView("/jsp/ImportExportPortlet/export", model);
    }

    /**
     * Display the entity deletion form view.
     * 
     * @param request
     * @return
     */
    @RequestMapping(params="action=delete")
    public ModelAndView getDeleteView(PortletRequest request) {
    	Map<String,Object> model = new HashMap<String,Object>();
    	
    	// add a list of all permitted deletion types
    	final Iterable<IPortalDataType> deletePortalDataTypes = this.portalDataHandlerService.getDeletePortalDataTypes();
    	final List<IPortalDataType> types = getAllowedTypes(request, DELETE_PERMISSION, deletePortalDataTypes);
    	model.put("supportedTypes", types);
    	
        return new ModelAndView("/jsp/ImportExportPortlet/delete", model);
    }
    
    /**
     * Return a list of all permitted import/export types for the given permission
     * and the current user.
     * 
     * @param request
     * @param activityName
     * @return
     */
    protected List<IPortalDataType> getAllowedTypes(PortletRequest request, String activityName, Iterable<IPortalDataType> dataTypes) {

    	// get the authorization principal representing the current user
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
		final IPerson person = personManager.getPerson(httpServletRequest);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    // filter the list of configured import/export types by user permission
    	final List<IPortalDataType> results = new ArrayList<IPortalDataType>();
    	for (IPortalDataType type : dataTypes) {
    		final String typeId = type.getTypeId();
    	    if (ap.hasPermission(OWNER, activityName, typeId)) {
    	    	results.add(type);
    	    }    		
    	}

    	return results;
    }

}
