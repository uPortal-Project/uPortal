/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.tenants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.jasig.portal.io.xml.IDataTemplatingStrategy;
import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.io.xml.SpELDataTemplatingStrategy;
import org.jasig.portal.io.xml.group.GroupMembershipPortalDataType;
import org.jasig.portal.io.xml.pags.PersonAttributesGroupStorePortalDataType;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.tenants.TenantOperationResponse.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Uses Import/Export to create some basic portal data for a new tenant.  You
 * can add to, adjust, or pare-down tenant template data in
 * src/main/resources/org/jasig/portal/tenants/data.
 * 
 * @since 4.1
 * @author awills
 */
public final class TemplateDataTenantOperationsListener extends AbstractTenantOperationsListener implements ApplicationContextAware {

    private static final String TENANT_ENTITIES_IMPORTED = "tenant.entities.imported";
    private static final String IMPORTED_THE_FOLLOWING_ENTITIES = "imported.the.following.entities";

    private static final String FAILED_TO_LOAD_TENANT_TEMPLATE = "failed.to.load.tenant.template";
    private static final String FAILED_TO_IMPORT_TENANT_TEMPLATE_DATA = "failed.to.import.tenant.template.data";

    private static final Set<PortalDataKey> KEYS_TO_IGNORE = new HashSet<>(
            Arrays.asList(new PortalDataKey[] {
                    GroupMembershipPortalDataType.IMPORT_32_DATA_KEY,
                    PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_41_DATA_KEY
            }));

    private ApplicationContext applicationContext;
    private Resource[] templateResources;
    private SAXReader reader;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${org.jasig.portal.tenants.TemplateDataTenantOperationsListener.templateLocation:classpath:/org/jasig/portal/tenants/data/**/*.xml}")
    private String templateLocation;

    @Autowired
    private IPortalDataHandlerService dataHandlerService;

    @Autowired
    private IPortalSpELService portalSpELService;

    private List<PortalDataKey> dataKeyImportOrder = Collections.emptyList();

    public TemplateDataTenantOperationsListener() {
        this.reader = new SAXReader();
        this.reader.setMergeAdjacentText(true);
    }

    /**
     * Order in which data types should be imported.
     */
    @javax.annotation.Resource(name="dataTypeImportOrder")
    public void setDataTypeImportOrder(List<IPortalDataType> dataTypeImportOrder) {
        final ArrayList<PortalDataKey> dataKeyImportOrder = new ArrayList<PortalDataKey>(dataTypeImportOrder.size() * 2);

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void setup() throws Exception {
        templateResources = applicationContext.getResources(templateLocation);
    }

    @Override
    public TenantOperationResponse onCreate(final ITenant tenant) {

        final Locale locale = getCurrentUserLocale();

        /*
         * First load dom4j Documents and sort the entity files into the proper order 
         */
        final Map<PortalDataKey,Set<BucketTuple>> importQueue = new HashMap<PortalDataKey,Set<BucketTuple>>();
        Resource rsc = null;
        try {
            for (Resource r : templateResources) {
                rsc = r;
                if (log.isDebugEnabled()) {
                    log.debug("Loading template resource file for tenant " 
                            + "'" + tenant.getFname() + "':  " 
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
                        Set<BucketTuple> bucket = importQueue.get(atLeastOneMatchingDataKey);
                        if (bucket == null) {
                            // First of these we've seen;  create the bucket;
                            bucket = new HashSet<BucketTuple>();
                            importQueue.put(atLeastOneMatchingDataKey, bucket);
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
                    throw new RuntimeException("No PortalDataKey found for QName:  " 
                                                + doc.getRootElement().getQName());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process the specified template:  {}",
                    (rsc != null ? rsc.getFilename() : "null"), e);
            final TenantOperationResponse error = new TenantOperationResponse(this, Result.ABORT);
            error.addMessage(getMessageSource().getMessage(FAILED_TO_LOAD_TENANT_TEMPLATE, new String[] { tenant.getName() }, locale));
            return error;
        }

        log.trace("Ready to import data entity templates for new tenant '{}';  importQueue={}", 
                                                            tenant.getName(), importQueue);

        // We're going to report on every item imported;  TODO it would be better
        // if we could display human-friendly entity type name + sysid (fname, etc.)
        final StringBuilder importReport = new StringBuilder();

        /*
         * Now import the identified entities each bucket in turn 
         */
        final StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setRootObject(new RootObjectImpl(tenant));
        IDataTemplatingStrategy templating = new SpELDataTemplatingStrategy(portalSpELService, ctx);
        Document doc = null;
        try {
            for (PortalDataKey pdk : dataKeyImportOrder) {
                Set<BucketTuple> bucket = importQueue.get(pdk);
                if (bucket != null) {
                    log.debug("Importing the specified PortalDataKey tenant '{}':  {}",
                            tenant.getName(), pdk.getName());
                    for (BucketTuple tuple : bucket) {
                        doc = tuple.getDocument();
                        Source data = templating.processTemplates(doc, tuple.getResource().getURL().toString());
                        dataHandlerService.importData(data, pdk);
                        importReport.append(createImportReportLineItem(pdk, tuple));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to process the specified template document:\n{}",
                                    (doc != null ? doc.asXML() : "null"), e);
            final TenantOperationResponse error = new TenantOperationResponse(this, Result.ABORT);
            error.addMessage(this.finalizeImportReport(importReport, locale));
            error.addMessage(getMessageSource().getMessage(FAILED_TO_IMPORT_TENANT_TEMPLATE_DATA, new String[] { tenant.getName() }, locale));
            return error;
        }

        TenantOperationResponse rslt = new TenantOperationResponse(this, Result.SUCCESS);
        rslt.addMessage(this.finalizeImportReport(importReport, locale));
        rslt.addMessage(getMessageSource().getMessage(TENANT_ENTITIES_IMPORTED, new String[] { tenant.getName() }, locale));
        return rslt;

    }

    /*
     * Implementation
     */

    private boolean evaluatePortalDataKeyMatch(Document doc, PortalDataKey pdk) {
        // Matching is tougher because it's dom4j <> w3c...
        final QName qname = doc.getRootElement().getQName();
        if (!qname.getName().equals(pdk.getName().getLocalPart()) ||
                !qname.getNamespaceURI().equals(pdk.getName().getNamespaceURI())) {
            // Rule these out straight off...
            return false;
        }

        // If the PortalDataKey declares a script
        // (old method), the document must match it
        final Attribute s = doc.getRootElement().attribute(PortalDataKey.SCRIPT_ATTRIBUTE_NAME.getLocalPart());
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
        final Attribute v = doc.getRootElement().attribute(PortalDataKey.VERSION_ATTRIBUTE_NAME.getLocalPart());
        final String version = v != null ? v.getValue() : null;
        if (pdk.getVersion() != null && pdk.getVersion().equals(version)) {
            return true;
        }

        // This pdk is not a match
        return false;

    }

    private String createImportReportLineItem(PortalDataKey pdk, BucketTuple tuple) {
        final String versionPart = pdk.getVersion() != null
                ? " (" + pdk.getVersion() + ")"
                : "";
        StringBuilder rslt = new StringBuilder();
        rslt.append("\n  <li><span class=\"label label-info\">")
                .append(pdk.getName().getLocalPart()).append(versionPart).append("</span>")
                .append(" ").append(tuple.getResource().getFilename()).append("</li>");
        return rslt.toString();
    }

    private String finalizeImportReport(StringBuilder message, Locale locale) {
        final String preamble = getMessageSource().getMessage(IMPORTED_THE_FOLLOWING_ENTITIES, null, locale);
        message.insert(0, "\n<ul>").insert(0, preamble);  // Reverse order
        message.append("\n</ul>");
        return message.toString();
    }

    /*
     * Nested Types
     */

    private static final class BucketTuple {
        private final Resource resource;
        private final Document document;
        public BucketTuple(Resource resource, Document document) {
            this.resource = resource;
            this.document = document;
        }
        public Resource getResource() { return resource; }
        public Document getDocument() { return document; }
    }

    private static final class RootObjectImpl {
        private final ITenant tenant;
        public RootObjectImpl(ITenant tenant) {
            this.tenant = tenant;
        }
        @SuppressWarnings("unused")
        public ITenant getTenant() {
            return this.tenant;
        }
    }

}
