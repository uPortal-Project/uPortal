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

package org.jasig.portal.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.XMLWriter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * ImportExportController provides AJAX/REST targets for import/export operations.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Controller
public class ImportExportController {

	private static final String OWNER = "UP_SYSTEM";
	private static final String EXPORT_PERMISSION = "EXPORT_ENTITY";
	private static final String DELETE_PERMISSION = "DELETE_ENTITY";

    final Log log = LogFactory.getLog(getClass());
    
    private DataSource portalDb;
    private ApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;
    private Map<String,String> attributeNames;
    private Map<String, Task> exportTasks;
    private Map<String, Task> deleteTasks;
    private IPersonManager personManager;
    private IPortalDataHandlerService portalDataHandlerService;
    
    @Resource(name="PortalDb")
    public void setPortalDb(DataSource portalDb) {
        this.portalDb = portalDb;
    }
    
    
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    
    @Autowired
    public void setTransactionManager(@Qualifier("PortalDb") PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    
    @Resource(name="identifierAttributeNames")
    public void setIdentifierAttributeNames(Map<String,String> attributeNames) {
    	this.attributeNames = attributeNames;
    }
    
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
    	this.personManager = personManager;
    }
    
    @Autowired
	public void setPortalDataHandlerService(IPortalDataHandlerService portalDataHandlerService) {
        this.portalDataHandlerService = portalDataHandlerService;
    }

    /**
     * 
     * @param entityFile
     * @param request
     * @param response
     * @throws DocumentException
     * @throws IOException
     */
    @RequestMapping(value="/import", method = RequestMethod.POST)
    public void importEntity(@RequestParam("fileData") MultipartFile entityFile, 
    		HttpServletRequest request, HttpServletResponse response) throws DocumentException, IOException {
        Document doc = new org.dom4j.io.SAXReader().read(entityFile.getInputStream());
        final Source source = new DocumentSource(doc);
        final String entityType = doc.getRootElement().getName();
        final String documentNamespace = doc.getRootElement().getNamespaceURI();
		final IPerson person = personManager.getPerson(request);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    if (!ap.hasPermission("UP_SYSTEM", "IMPORT_ENTITY", entityType)) {
	    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	return;
	    }
	    
	    // check the document root namespace+elementName
	    if(requiresLegacyCernunnosSupport(documentNamespace, entityType)) {
	    	// this is a uPortal 3.x (or earlier) document 
	    	Map<String, Object> attrs = new HashMap<String, Object>();
	         attrs.put("PORTAL_CONTEXT", applicationContext);
	         attrs.put("SqlAttributes.DATA_SOURCE", portalDb);
	         attrs.put("SqlAttributes.TRANSACTION_MANAGER", transactionManager);
	         attrs.put("Attributes.NODE", doc.getRootElement());        
	         TaskRequest taskRequest = new RuntimeRequestResponse(attrs);

	         // determine the full path to the relevant crn import script
	         String scriptName = doc.getRootElement().attributeValue("script");
	         String scriptLocation = this.getClass().getClassLoader().getResource(scriptName.substring("classpath:/".length())).toExternalForm();

	         // execute the script
	         ScriptRunner runner = new ScriptRunner();
	         runner.evaluate(scriptLocation, taskRequest);
	    } else {
	    	// this is a document supported by import service
//	    	this.importExportService.importData(source);
	    }
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Test if the namespace+elementName indicates the document to import will require the legacy
     * Cernunnos mechanism.
     * 
     * @param namespace
     * @param elementName
     * @return true if the namespace and/or element name indicates Cernunnos import will be required
     */
    protected boolean requiresLegacyCernunnosSupport(final String namespace, final String elementName) {
    	// TODO rudimentary test, if namespace is empty, it's a up3.x document - correct?
    	if(StringUtils.isBlank(namespace)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    /**
     * Delete an uPortal database object.  This method provides a REST interface
     * for uPortal database object deletion.
     * 
     * The path for this method is /entity/type/identifier.  The identifier generally
     * a string that may be used as a unique identifier, but is dependent on the 
     * entity type.  For example, to delete the "demo" user one might use the 
     * path /entity/user/demo.
     * 
     * @param entityType
     * @param entityId
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/entity/{entityType}/{entityId}", method = RequestMethod.DELETE)
	public void deleteEntity(@PathVariable("entityType") String entityType,
			@PathVariable("entityId") String entityId, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
    	
		final IPerson person = personManager.getPerson(request);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    if (!ap.hasPermission(OWNER, DELETE_PERMISSION, entityType)) {
	    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	return;
	    }
               
	    // get the task associated with exporting this entity type 
        Task task = deleteTasks.get(entityType);
	    if(null == task) {
	    	this.portalDataHandlerService.deleteData(entityType, entityId);
	    } else {
	    	 // set the system identifier under the require attribute name
	        String attributeName = attributeNames.get(entityType);
	        Map<String, Object> attributes = Collections
	                .<String, Object> singletonMap(attributeName, entityId);
	        
	        deleteEntity(task, attributes, request, response);
	    }
    	    	
    	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * 
     * @param entityId
     * @param entityType
     * @param download
     * @param request
     * @param response
     * @throws DocumentException
     * @throws IOException
     */
    @RequestMapping(value="/entity/{entityType}/{entityId}", method = RequestMethod.GET)
    public void exportEntity(@PathVariable("entityId") String entityId,
    		@PathVariable("entityType") String entityType,
    		@RequestParam(value="download", required=false) boolean download,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {
    	
		final IPerson person = personManager.getPerson(request);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    // if the current user does not have permission to delete this database
	    // object type, return a 401 error code
	    if (!ap.hasPermission(OWNER, EXPORT_PERMISSION, entityType)) {
	    	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	return;
	    }
        
	    // if the download boolean is set to true, set the response to be
	    // downloaded as an attachment
	    if (download) {
	    	String fileName = entityId.concat(".").concat(entityType);
	    	response.setHeader( "Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
	    }
	    
	    // get the task associated with exporting this entity type 
        Task task = exportTasks.get(entityType);
        if(null == task) {
        	OutputStream out = response.getOutputStream();
        	StreamResult result = new StreamResult(out);
        	this.portalDataHandlerService.exportData(entityType, entityId, result);
        	out.flush();
        } else {
        	// set the system identifier under the require attribute name
            String attributeName = attributeNames.get(entityType);
            Map<String, Object> attributes = Collections
                    .<String, Object> singletonMap(attributeName, entityId);
            
            exportEntityAsXml(task, attributes, request, response);
        }
    }

    protected TaskRequest getTaskRequest(Map<String, Object> additionalAttributes) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("PORTAL_CONTEXT", applicationContext);
        attrs.put("SqlAttributes.DATA_SOURCE", portalDb);
        attrs.put("SqlAttributes.TRANSACTION_MANAGER", transactionManager);
        
        if (additionalAttributes != null) {
            attrs.putAll(additionalAttributes);
        }
        
        RuntimeRequestResponse taskRequest = new RuntimeRequestResponse(attrs);
        return taskRequest;
    }
    
    protected void deleteEntity(Task task, Map<String, Object> attributes,
    		HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
        TaskRequest taskRequest = getTaskRequest(attributes);

        ReturnValueImpl rslt = new ReturnValueImpl();
        TaskResponse taskResponse = new RuntimeRequestResponse(Collections
                .<String, Object> singletonMap("Attributes.RETURN_VALUE", rslt));
        
        task.perform(taskRequest, taskResponse);
        
        rslt.getValue();
        
    }
    
    protected void exportEntityAsXml(Task task, Map<String, Object> attributes,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        TaskRequest taskRequest = getTaskRequest(attributes);

        ReturnValueImpl rslt = new ReturnValueImpl();
        TaskResponse taskResponse = new RuntimeRequestResponse(Collections
                .<String, Object> singletonMap("Attributes.RETURN_VALUE", rslt));
        
        task.perform(taskRequest, taskResponse);
        
        Node node = (Node) rslt.getValue();
        if(node != null) {
        	org.dom4j.io.OutputFormat fmt = new org.dom4j.io.OutputFormat("    ", true);
        	PrintWriter writer = response.getWriter();
        	XMLWriter xmlWriter = new XMLWriter(writer, fmt);;
        	xmlWriter.write(node);
        	writer.flush();
        	xmlWriter.close();
        }

    }
}
