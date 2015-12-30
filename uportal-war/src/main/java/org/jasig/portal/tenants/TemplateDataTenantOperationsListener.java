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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.jasig.portal.spring.spel.IPortalSpELService;
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
                 * Special Handling:  GroupMembershipPortalDataType.IMPORT_32_DATA_KEY
                 * 
                 * Need to prevent the GroupMembershipPortalDataType.IMPORT_32_DATA_KEY
                 * from entering our sorted list of keys because it attempts to import
                 * both the group part and the membership part of a group_membership
                 * file in one go.  We import several entities at once, so it's important
                 * to do these in 2 phases.
                 */
                if (!portalDataKey.equals(GroupMembershipPortalDataType.IMPORT_32_DATA_KEY)) {
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
    public void onCreate(final ITenant tenant) {

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
            throw new RuntimeException("Failed to process the specified template:  " 
                                    + (rsc != null ? rsc.getFilename() : ""), e);
        }

        log.trace("Ready to import data entity templates for new tenant '{}';  importQueue={}", 
                                                            tenant.getName(), importQueue);

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
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process the specified template document:  " 
                                    + (doc != null ? doc.asXML() : ""), e);
        }

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
