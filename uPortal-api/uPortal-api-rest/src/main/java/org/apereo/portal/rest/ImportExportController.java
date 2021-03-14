/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.io.xml.IPortalDataHandlerService;
import org.apereo.portal.io.xml.PortalDataKey;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.xml.StaxUtils;
import org.apereo.portal.xml.XmlUtilities;
import org.apereo.portal.xml.stream.BufferedXMLEventReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** ImportExportController provides AJAX/REST targets for import/export operations. */
@Controller
public class ImportExportController {

    final Log log = LogFactory.getLog(getClass());

    private IPersonManager personManager;
    private IPortalDataHandlerService portalDataHandlerService;
    private XmlUtilities xmlUtilities;

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortalDataHandlerService(IPortalDataHandlerService portalDataHandlerService) {
        this.portalDataHandlerService = portalDataHandlerService;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public void importEntity(
            @RequestParam("file") MultipartFile entityFile,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, XMLStreamException {

        // Get a StAX reader for the source to determine info about the data to import
        final BufferedXMLEventReader bufferedXmlEventReader =
                createSourceXmlEventReader(entityFile);
        final PortalDataKey portalDataKey = getPortalDataKey(bufferedXmlEventReader);
        String target = determineTarget(portalDataKey);

        final IPerson person = personManager.getPerson(request);
        final EntityIdentifier ei = person.getEntityIdentifier();
        final IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        if (!ap.hasPermission("UP_SYSTEM", "IMPORT_ENTITY", target)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        portalDataHandlerService.importData(new StAXSource(bufferedXmlEventReader));

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String determineTarget(PortalDataKey key) {
        String target = key.getName().getLocalPart();
        log.debug("portal data key local part:" + target);
        // Old groups and members datafile were combined into groupmemberships,
        // but the main node is still 'group'.
        // Can be determined by looking for 'membership' in the script text.
        if ("group".equals(target) && key.getScript().contains("membership")) {
            target = "group-membership";
        }
        log.debug("import target:" + target);
        return target;
    }

    protected BufferedXMLEventReader createSourceXmlEventReader(MultipartFile multipartFile)
            throws IOException {
        final InputStream inputStream = multipartFile.getInputStream();
        final String name = multipartFile.getOriginalFilename();

        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        final XMLEventReader xmlEventReader;
        try {
            xmlEventReader = xmlInputFactory.createXMLEventReader(name, inputStream);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XML Event Reader for data Source", e);
        }
        return new BufferedXMLEventReader(xmlEventReader, -1);
    }

    protected PortalDataKey getPortalDataKey(final BufferedXMLEventReader bufferedXmlEventReader) {
        final StartElement rootElement = StaxUtils.getRootElement(bufferedXmlEventReader);
        final PortalDataKey portalDataKey = new PortalDataKey(rootElement);
        bufferedXmlEventReader.reset();
        return portalDataKey;
    }

    /**
     * Delete an uPortal database object. This method provides a REST interface for uPortal database
     * object deletion.
     *
     * <p>The path for this method is /entity/type/identifier. The identifier generally a string
     * that may be used as a unique identifier, but is dependent on the entity type. For example, to
     * delete the "demo" user one might use the path /entity/user/demo.
     */
    @RequestMapping(value = "/entity/{entityType}/{entityId}", method = RequestMethod.DELETE)
    public void deleteEntity(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") String entityId,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        final IPerson person = personManager.getPerson(request);
        final EntityIdentifier ei = person.getEntityIdentifier();
        final IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

        if (!ap.hasPermission(IPermission.PORTAL_SYSTEM, IPermission.DELETE_ACTIVITY, entityType)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get the task associated with exporting this entity type
        portalDataHandlerService.deleteData(entityType, entityId);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @RequestMapping(value = "/entity/{entityType}/{entityId}", method = RequestMethod.GET)
    public void exportEntity(
            @PathVariable("entityId") String entityId,
            @PathVariable("entityType") String entityType,
            @RequestParam(value = "download", required = false) boolean download,
            @RequestParam(value = "format", defaultValue = "XML", required = false)
                    String formatType,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, JSONException {

        final IPerson person = personManager.getPerson(request);
        final EntityIdentifier ei = person.getEntityIdentifier();
        final IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());

        // if the current user does not have permission to delete this database
        // object type, return a 401 error code
        if (!ap.hasPermission(IPermission.PORTAL_SYSTEM, IPermission.EXPORT_ACTIVITY, entityType)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Export the data into a string buffer
        final StringWriter exportBuffer = new StringWriter();
        final String fileName =
                portalDataHandlerService.exportData(
                        entityType, entityId, new StreamResult(exportBuffer));
        final PrintWriter responseWriter = response.getWriter();

        if (download) {
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\""
                            + fileName
                            + "."
                            + entityType
                            + "."
                            + formatType.toLowerCase()
                            + "\"");
        }

        if ("XML".equalsIgnoreCase(formatType)) {
            responseWriter.print(exportBuffer.getBuffer());
        } else if ("JSON".equalsIgnoreCase(formatType)) {
            JSONObject json = XML.toJSONObject(exportBuffer.getBuffer().toString());
            responseWriter.print(json);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }
}
