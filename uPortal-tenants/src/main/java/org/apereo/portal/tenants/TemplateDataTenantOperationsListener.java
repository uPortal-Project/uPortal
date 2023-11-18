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
package org.apereo.portal.tenants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import org.apereo.portal.io.xml.IDataTemplatingStrategy;
import org.apereo.portal.io.xml.IPortalDataHandlerService;
import org.apereo.portal.io.xml.IPortalDataType;
import org.apereo.portal.io.xml.PortalDataKey;
import org.apereo.portal.io.xml.SpELDataTemplatingStrategy;
import org.apereo.portal.io.xml.group.GroupMembershipPortalDataType;
import org.apereo.portal.io.xml.pags.PersonAttributesGroupStorePortalDataType;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.apereo.portal.spring.spel.PortalSpELServiceImpl;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Uses Import/Export to create some basic portal data for a new tenant. You can add to, adjust, or
 * pare-down tenant template data in src/main/resources/org/apereo/portal/tenants/data.
 *
 * @since 4.1
 */
public final class TemplateDataTenantOperationsListener extends AbstractTenantOperationsListener
        implements ApplicationContextAware {

    private static final String TENANT_ENTITIES_IMPORTED = "tenant.entities.imported";
    private static final String IMPORTED_THE_FOLLOWING_ENTITIES = "imported.the.following.entities";
    private static final String DELETED_THE_FOLLOWING_ENTITIES = "deleted.the.following.entities";
    private static final String UNABLE_TO_DELETE_THE_FOLLOWING_ENTITIES =
            "unable.to.delete.the.following.entities";

    private static final String FAILED_TO_LOAD_TENANT_TEMPLATE = "failed.to.load.tenant.template";
    private static final String FAILED_TO_IMPORT_TENANT_TEMPLATE_DATA =
            "failed.to.import.tenant.template.data";

    private static final Set<PortalDataKey> KEYS_TO_IGNORE =
            new HashSet<>(
                    Arrays.asList(
                            GroupMembershipPortalDataType.IMPORT_32_DATA_KEY,
                            PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_41_DATA_KEY));

    private ApplicationContext applicationContext;
    private SAXReader reader;
    private static final Logger log =
            LoggerFactory.getLogger(TemplateDataTenantOperationsListener.class);

    @Autowired private IPortalDataHandlerService dataHandlerService;

    @Autowired private IPortalSpELService portalSpELService;

    private List<PortalDataKey> dataKeyImportOrder = Collections.emptyList();

    @Value(
            "${org.apereo.portal.tenants.TemplateDataTenantOperationsListener.templateLocation:classpath:/org/apereo/portal/tenants/data/**/*.xml}")
    private String templateLocation;

    // Evaluated by scanning a package (see 'templateLocation' above)
    private Set<Resource> entityResourcesToImportOnCreate;
    // Optionally specified in the <bean> definition
    private Set<String> entityResourcePathsToImportOnUpdate = Collections.emptySet(); // default
    private Set<Resource> entityResourcesToImportOnUpdate = Collections.emptySet(); // default
    private List<DeleteTuple> entitiesToRemoveOnDelete = Collections.emptyList(); // default

    public TemplateDataTenantOperationsListener() {
        super("template-data");
        this.reader = new SAXReader();
        this.reader.setMergeAdjacentText(true);
    }

    /**
     * The <code>@Autowired</code> list will reflect the order in which data types should be
     * imported.
     */
    @Autowired
    public void setDataTypeImportOrder(List<IPortalDataType> dataTypeImportOrder) {
        final ArrayList<PortalDataKey> dataKeyImportOrder =
                new ArrayList<>(dataTypeImportOrder.size() * 2);

        for (final IPortalDataType portalDataType : dataTypeImportOrder) {
            final List<PortalDataKey> supportedDataKeys = portalDataType.getDataKeyImportOrder();
            for (final PortalDataKey portalDataKey : supportedDataKeys) {
                /*
                 * Special Handling:  Need to prevent some keys from entering our
                 * sorted collection because they attempt to import both a group
                 * part and the membership parts of a group (either local or PAGS)
                 * in one go.  We import several entities at once, so it's important
                 * to do these in 2 phases.
                 */
                if (!KEYS_TO_IGNORE.contains(portalDataKey)) {
                    dataKeyImportOrder.add(portalDataKey);
                }
            }
        }
        dataKeyImportOrder.trimToSize();
        this.dataKeyImportOrder = Collections.unmodifiableList(dataKeyImportOrder);
    }

    public void setEntityResourcesToImportOnUpdate(Set<String> entityResourcesToImportOnUpdate) {
        // applicationContext not available to create resources immediately
        // simply capturing and resolving full paths to resources for setup()
        entityResourcePathsToImportOnUpdate =
                determineImportOnUpdatePaths(templateLocation, entityResourcesToImportOnUpdate);
    }

    /**
     * Determine resources based on defined template location and context resource values in
     * servicesContext.xml. If the values are relative, prepend template location path. This is
     * determined by checking that the value starts with "\[a-zA-Z]+:".
     *
     * @param templateLoc tenant template location defined in portal.properties
     * @param relResourcePathSet importOnUpdate values defined in servicesContext.xml
     * @return resource paths as absolute paths.
     */
    /*package*/ static Set<String> determineImportOnUpdatePaths(
            String templateLoc, Set<String> relResourcePathSet) {
        String templateLocPath = templateLoc.split("\\*")[0]; // up to wildcard pattern
        if (!templateLocPath.endsWith("/")) {
            templateLocPath = templateLocPath + "/";
        }
        final Set<String> fullResourcePathSet = new HashSet<>();
        for (String resourcePath : relResourcePathSet) {
            final String fullPath;
            try {
                fullPath =
                        resourcePath.matches("^[a-zA-Z]+:.*")
                                ? resourcePath
                                : (new URI(templateLocPath).resolve(resourcePath)).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                        "Unable to construct a URI by resolving '"
                                + resourcePath
                                + "'from '"
                                + templateLocPath
                                + "'");
            }
            log.debug("Calculated full path: {} -> {}", resourcePath, fullPath);
            fullResourcePathSet.add(fullPath);
        }
        return fullResourcePathSet;
    }

    /**
     * Given the list of resource strings, acquire the set of resource objects from a {@code
     * org.springframework.core.io.ResourceLoader}.
     *
     * @param resourceLoader a resource loader, usually an application context
     * @param resourcePaths set of resource paths as strings
     * @return set of resources derived from paths via resource loader
     */
    /*package*/ static Set<Resource> buildResourcesFromPaths(
            ResourceLoader resourceLoader, Set<String> resourcePaths) {
        final Set<Resource> resourceSet = new HashSet<>();
        for (String resourcePath : resourcePaths) {
            Resource resource = resourceLoader.getResource(resourcePath);
            log.debug("Resource {} exists?: {}", resource, resource.exists());
            resourceSet.add(resource);
        }
        return resourceSet;
    }

    public void setEntitiesToRemoveOnDelete(List<DeleteTuple> entitiesToRemoveOnDelete) {
        this.entitiesToRemoveOnDelete = entitiesToRemoveOnDelete;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void setup() throws Exception {
        entityResourcesToImportOnCreate =
                new HashSet<>(Arrays.asList(applicationContext.getResources(templateLocation)));
        this.entityResourcesToImportOnUpdate =
                buildResourcesFromPaths(applicationContext, entityResourcePathsToImportOnUpdate);
    }

    @Override
    public TenantOperationResponse onCreate(final ITenant tenant) {
        return importWithResources(tenant, entityResourcesToImportOnCreate);
    }

    @Override
    public TenantOperationResponse onUpdate(final ITenant tenant) {
        return importWithResources(tenant, entityResourcesToImportOnUpdate);
    }

    @Override
    public TenantOperationResponse onDelete(final ITenant tenant) {

        // Deleting is optional;  we should IGNORE this whole
        // business if no items are specified for delete
        if (entitiesToRemoveOnDelete.isEmpty()) {
            return super.onDelete(tenant);
        }

        TenantOperationResponse.Result result = TenantOperationResponse.Result.SUCCESS; // default

        // We will prepare a list of items that succeeded...
        final StringBuilder successfulEntitiesMessage = new StringBuilder();
        successfulEntitiesMessage
                .append(createLocalizedMessage(DELETED_THE_FOLLOWING_ENTITIES, null))
                .append("\n<ul>");

        // And one for items that failed, if any.
        final StringBuilder failedEntitiesMessage = new StringBuilder();
        failedEntitiesMessage
                .append(createLocalizedMessage(UNABLE_TO_DELETE_THE_FOLLOWING_ENTITIES, null))
                .append("\n<ul>");

        // We support SpEL expressions in the sysid parameter (we don't have a choice)
        final StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setRootObject(new RootObjectImpl(tenant));

        boolean didAtLeastOneCommandSucceed = false; // until we know different
        for (DeleteTuple tuple : entitiesToRemoveOnDelete) {
            final Expression x =
                    portalSpELService.parseExpression(
                            tuple.getSysid(), PortalSpELServiceImpl.TemplateParserContext.INSTANCE);
            final String sysid = x.getValue(ctx, String.class);
            try {
                dataHandlerService.deleteData(tuple.getType(), sysid);
                successfulEntitiesMessage
                        .append("\n  <li><span class=\"label label-info\">")
                        .append(tuple.getType())
                        .append("</span>")
                        .append(" ")
                        .append(sysid)
                        .append("</li>");
                didAtLeastOneCommandSucceed = true;
            } catch (Exception e) {
                log.error(
                        "Failed to process the specified delete command:  type={}, sysid={}",
                        tuple.getType(),
                        tuple.getSysid(),
                        e);
                failedEntitiesMessage
                        .append("\n  <li><span class=\"label label-info\">")
                        .append(tuple.getType())
                        .append("</span>")
                        .append(" ")
                        .append(sysid)
                        .append("</li>");
                result =
                        TenantOperationResponse.Result
                                .FAIL; // We will allow subsequent listeners to follow through
            }
        }

        // Finish our HTML lists...
        successfulEntitiesMessage.append("\n</ul>");
        failedEntitiesMessage.append("\n</ul>");

        final TenantOperationResponse result = new TenantOperationResponse(this, result);

        // Did we succeed at all?
        if (didAtLeastOneCommandSucceed) {
            result.addMessage(successfulEntitiesMessage.toString());
        }

        switch (result) {
                // Did we fail at all?
            case FAIL:
                result.addMessage(failedEntitiesMessage.toString());
                break;
                // Or succeed completely?
            default:
                // Might add another message here in future...
                break;
        }

        return result;
    }

    /*
     * Implementation
     */

    /**
     * High-level implementation method that brokers the queuing, importing, and reporting that is
     * common to Create and Update.
     */
    public TenantOperationResponse importWithResources(
            final ITenant tenant, final Set<Resource> resources) {
        /*
         * First load dom4j Documents and sort the entity files into the proper order
         */
        final Map<PortalDataKey, Set<BucketTuple>> importQueue;
        try {
            importQueue = prepareImportQueue(tenant, resources);
        } catch (Exception e) {
            final TenantOperationResponse error =
                    new TenantOperationResponse(this, TenantOperationResponse.Result.ABORT);
            error.addMessage(
                    createLocalizedMessage(
                            FAILED_TO_LOAD_TENANT_TEMPLATE, new String[] {tenant.getName()}));
            return error;
        }

        log.trace(
                "Ready to import data entity templates for new tenant '{}';  importQueue={}",
                tenant.getName(),
                importQueue);

        // We're going to report on every item imported;  TODO it would be better
        // if we could display human-friendly entity type name + sysid (fname, etc.)
        final StringBuilder importReport = new StringBuilder();

        /*
         * Now import the identified entities each bucket in turn
         */
        try {
            importQueue(tenant, importQueue, importReport);
        } catch (Exception e) {
            final TenantOperationResponse error =
                    new TenantOperationResponse(this, TenantOperationResponse.Result.ABORT);
            error.addMessage(finalizeImportReport(importReport));
            error.addMessage(
                    createLocalizedMessage(
                            FAILED_TO_IMPORT_TENANT_TEMPLATE_DATA,
                            new String[] {tenant.getName()}));
            return error;
        }

        TenantOperationResponse result =
                new TenantOperationResponse(this, TenantOperationResponse.Result.SUCCESS);
        result.addMessage(finalizeImportReport(importReport));
        result.addMessage(
                createLocalizedMessage(TENANT_ENTITIES_IMPORTED, new String[] {tenant.getName()}));
        return result;
    }

    /** Loads dom4j Documents and sorts the entity files into the proper order for Import. */
    private Map<PortalDataKey, Set<BucketTuple>> prepareImportQueue(
            final ITenant tenant, final Set<Resource> templates) throws Exception {
        final Map<PortalDataKey, Set<BucketTuple>> result = new HashMap<>();
        Resource rsc = null;
        try {
            for (Resource r : templates) {
                rsc = r;
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Loading template resource file for tenant "
                                    + "'"
                                    + tenant.getFname()
                                    + "':  "
                                    + rsc.getFilename());
                }
                final Document doc = reader.read(rsc.getInputStream());
                PortalDataKey atLeastOneMatchingDataKey = null;
                for (PortalDataKey pdk : dataKeyImportOrder) {
                    boolean matches = evaluatePortalDataKeyMatch(doc, pdk);
                    if (matches) {
                        // Found the right bucket...
                        log.debug("Found PortalDataKey '{}' for data document {}", pdk, r.getURI());
                        atLeastOneMatchingDataKey = pdk;
                        Set<BucketTuple> bucket = result.get(atLeastOneMatchingDataKey);
                        if (bucket == null) {
                            // First of these we've seen;  create the bucket;
                            bucket = new HashSet<>();
                            result.put(atLeastOneMatchingDataKey, bucket);
                        }
                        BucketTuple tuple = new BucketTuple(rsc, doc);
                        bucket.add(tuple);
                        /*
                         * At this point, we would normally add a break;
                         * statement, but group_membership.xml files need to
                         * match more than one PortalDataKey.
                         */
                    }
                }
                if (atLeastOneMatchingDataKey == null) {
                    // We can't proceed
                    throw new RuntimeException(
                            "No PortalDataKey found for QName:  "
                                    + doc.getRootElement().getQName());
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to process the specified template:  {}",
                    (rsc != null ? rsc.getFilename() : "null"),
                    e);
            throw e;
        }
        return result;
    }

    /** Imports the specified entities in the proper order. */
    private void importQueue(
            final ITenant tenant,
            final Map<PortalDataKey, Set<BucketTuple>> queue,
            final StringBuilder importReport)
            throws Exception {

        final StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setRootObject(new RootObjectImpl(tenant));
        IDataTemplatingStrategy templating = new SpELDataTemplatingStrategy(portalSpELService, ctx);
        Document doc = null;
        try {
            for (PortalDataKey pdk : dataKeyImportOrder) {
                Set<BucketTuple> bucket = queue.get(pdk);
                if (bucket != null) {
                    log.debug(
                            "Importing the specified PortalDataKey tenant '{}':  {}",
                            tenant.getName(),
                            pdk.getName());
                    for (BucketTuple tuple : bucket) {
                        doc = tuple.getDocument();
                        Source data =
                                templating.processTemplates(
                                        doc, tuple.getResource().getURL().toString());
                        dataHandlerService.importData(data, pdk);
                        importReport.append(createImportReportLineItem(pdk, tuple));
                    }
                }
            }

        } catch (Exception e) {
            log.error(
                    "Failed to process the specified template document:\n{}",
                    (doc != null ? doc.asXML() : "null"),
                    e);
            throw e;
        }
    }

    private boolean evaluatePortalDataKeyMatch(Document doc, PortalDataKey pdk) {
        // Matching is tougher because it's dom4j <> w3c...
        final QName qname = doc.getRootElement().getQName();
        if (!qname.getName().equals(pdk.getName().getLocalPart())
                || !qname.getNamespaceURI().equals(pdk.getName().getNamespaceURI())) {
            // Rule these out straight off...
            return false;
        }

        // If the PortalDataKey declares a script
        // (old method), the document must match it
        final Attribute s =
                doc.getRootElement().attribute(PortalDataKey.SCRIPT_ATTRIBUTE_NAME.getLocalPart());
        final String script = s != null ? s.getValue() : null;
        if (pdk.getScript() != null) {
            // If the pdk declares a script, the data document MUST match it...
            if (pdk.getScript().equals(script)) {
                /*
                 * A data document that matches on script need not match on version
                 * as well.  It appears that the pdk.version member is overloaded
                 * with two purposes...
                 *
                 *   - A numeric version (e.g. 4.0) indicates a match IN THE ABSENCE
                 *     OF a script attribute (newer method, below)
                 *   - A word (e.g. 'GROUP' or 'MEMBERS') indicates the type of data
                 *     the pdk handles, where more than one pdk applies to the data
                 *     document
                 */
                return true;
            } else {
                return false;
            }
        }

        // If the PortalDataKey declares a version BUT NOT a script (new
        // method), the document must match it
        final Attribute v =
                doc.getRootElement().attribute(PortalDataKey.VERSION_ATTRIBUTE_NAME.getLocalPart());
        final String version = v != null ? v.getValue() : null;
        if (pdk.getVersion() != null && pdk.getVersion().equals(version)) {
            return true;
        }

        // This pdk is not a match
        return false;
    }

    private String createImportReportLineItem(PortalDataKey pdk, BucketTuple tuple) {
        final String versionPart = pdk.getVersion() != null ? " (" + pdk.getVersion() + ")" : "";
        StringBuilder result = new StringBuilder();
        result.append("\n  <li><span class=\"label label-info\">")
                .append(pdk.getName().getLocalPart())
                .append(versionPart)
                .append("</span>")
                .append(" ")
                .append(tuple.getResource().getFilename())
                .append("</li>");
        return result.toString();
    }

    private String finalizeImportReport(StringBuilder message) {
        final String preamble = createLocalizedMessage(IMPORTED_THE_FOLLOWING_ENTITIES, null);
        message.insert(0, "\n<ul>").insert(0, preamble); // Reverse order
        message.append("\n</ul>");
        return message.toString();
    }

    /*
     * Nested Types
     */

    /** Used in the implementation of onDelete. */
    public static final class DeleteTuple {
        private String type;
        private String sysid;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSysid() {
            return sysid;
        }

        public void setSysid(String sysid) {
            this.sysid = sysid;
        }
    }

    private static final class BucketTuple {
        private final Resource resource;
        private final Document document;

        public BucketTuple(Resource resource, Document document) {
            this.resource = resource;
            this.document = document;
        }

        public Resource getResource() {
            return resource;
        }

        public Document getDocument() {
            return document;
        }
    }

    private static final class RootObjectImpl {
        private final ITenant tenant;

        public RootObjectImpl(ITenant tenant) {
            this.tenant = tenant;
        }

        @SuppressWarnings("unused")
        public ITenant getTenant() {
            return tenant;
        }
    }
}
