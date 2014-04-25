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

package org.jasig.portal.tenants;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.SAXReader;
import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.io.xml.group.GroupMembershipPortalDataType;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.spring.spel.PortalSpELServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.w3c.dom.DOMImplementation;

/**
 * Uses Import/Export to create some basic portal data for a new tenant.  You
 * can add to, adjust, or pare-down tenant template data in
 * src/main/resources/org/jasig/portal/tenants/data.
 * 
 * @author awills
 */
public final class TemplateDataTenantOperationsListener extends AbstractTenantOperationsListener implements ApplicationContextAware {

    private static final String TEMPLATE_LOCATION = "classpath:/org/jasig/portal/tenants/data/**/*.xml";
    private static final String ATTRIBUTE_XPATH = "//@*";
    private static final String TEXT_XPATH = "//text()";
    private static final String[] XPATH_EXPRESSIONS = new String[] { ATTRIBUTE_XPATH, TEXT_XPATH };

    private ApplicationContext applicationContext;
    private Resource[] templateResources;
    private final SAXReader reader = new SAXReader();
    private final DOMWriter writer = new DOMWriter();
    private DOMImplementation domImpl;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private IPortalDataHandlerService dataHandlerService;

    @Autowired
    private IPortalSpELService portalSpELService;

    private List<PortalDataKey> dataKeyImportOrder = Collections.emptyList();

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
        templateResources = applicationContext.getResources(TEMPLATE_LOCATION);

        Map<String,String> nsPrefixes = new HashMap<String,String>();
        nsPrefixes.put("dlm", FragmentDefinition.NAMESPACE_URI);
        DocumentFactory factory = new DocumentFactory();
        factory.setXPathNamespaceURIs(nsPrefixes);
        reader.setDocumentFactory(factory);

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        domImpl = fac.newDocumentBuilder().getDOMImplementation();
    }

    @Override
    public void onCreate(final ITenant tenant) {

        final StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setRootObject(new RootObjectImpl(tenant));

        /*
         * First load dom4j Documents and sort the entity files into the proper order 
         */
        final Map<PortalDataKey,Set<Document>> importQueue = new HashMap<PortalDataKey,Set<Document>>();
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
                final QName qname = doc.getRootElement().getQName();
                PortalDataKey atLeastOneMatchingDataKey = null;
                for (PortalDataKey pdk : dataKeyImportOrder) {
                    // Matching is tougher because it's dom4j <> w3c...
                    boolean matches = qname.getName().equals(pdk.getName().getLocalPart())
                            && qname.getNamespaceURI().equals(pdk.getName().getNamespaceURI());
                    if (matches) {
                        // Found the right bucket...
                        atLeastOneMatchingDataKey = pdk;
                        Set<Document> bucket = importQueue.get(atLeastOneMatchingDataKey);
                        if (bucket == null) {
                            // First of these we've seen;  create the bucket;
                            bucket = new HashSet<Document>();
                            importQueue.put(atLeastOneMatchingDataKey, bucket);
                        }
                        bucket.add(doc);
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
        Document doc = null;
        org.w3c.dom.Document w3c = null;
        try {
            for (PortalDataKey pdk : dataKeyImportOrder) {
                Set<Document> bucket = importQueue.get(pdk);
                if (bucket != null) {
                    log.debug("Importing the specified PortalDataKey tenant '{}':  {}",
                            tenant.getName(), pdk.getName());
                    for (Document d : bucket) {
                        doc = d;
                        log.trace("Importing document XML={}", doc.asXML());
                        for (String xpath : XPATH_EXPRESSIONS) {
                            @SuppressWarnings("unchecked")
                            List<Node> nodes = doc.selectNodes(xpath);
                            for (Node n : nodes) {
                                String inpt, otpt;
                                switch (n.getNodeType()) {
                                    case org.w3c.dom.Node.ATTRIBUTE_NODE:
                                        Attribute a = (Attribute) n;
                                        inpt = a.getValue();
                                        otpt = processText(inpt, ctx);
                                        if (!otpt.equals(inpt)) {
                                            a.setValue(otpt);
                                        }
                                        break;
                                    case org.w3c.dom.Node.TEXT_NODE:
                                        Text t = (Text) n;
                                        inpt = t.getText();
                                        otpt = processText(inpt, ctx);
                                        if (!otpt.equals(inpt)) {
                                            t.setText(otpt);
                                        }
                                        break;
                                    default:
                                        String msg = "Unsupported node type:  " + n.getNodeTypeName();
                                        throw new RuntimeException(msg);
                                }
                            }
                        }
                        w3c = writer.write(doc, domImpl);
                        Source source = new DOMSource(w3c);
                        source.setSystemId(rsc.getFilename());  // must be set, else import chokes
                        dataHandlerService.importData(source, pdk);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("w3c DOM="+this.nodeToString(w3c));
            throw new RuntimeException("Failed to process the specified template document:  " 
                                    + (doc != null ? doc.asXML() : ""), e);
        }

    }

    /*
     * Implementation
     */

    private String processText(String text, EvaluationContext ctx) {

        String rslt = text;  // default

        Expression x = portalSpELService.parseExpression(text, PortalSpELServiceImpl.TemplateParserContext.INSTANCE);
        rslt = x.getValue(ctx, String.class);

        return rslt;

    }

    private String nodeToString(org.w3c.dom.Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);

            String xmlString = result.getWriter().toString();
            return xmlString;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /*
     * Nested Types
     */

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
