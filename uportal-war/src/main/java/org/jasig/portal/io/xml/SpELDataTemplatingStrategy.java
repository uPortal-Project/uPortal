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

package org.jasig.portal.io.xml;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.spring.spel.PortalSpELServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * Implements a SpEL-based templating strategy.
 * 
 * @since 4.2
 * @author drewwills
 */
public class SpELDataTemplatingStrategy implements IDataTemplatingStrategy {

    private static final String ATTRIBUTE_XPATH = "//@*";
    private static final String TEXT_XPATH = "//text()";
    private static final String[] XPATH_EXPRESSIONS = new String[] { ATTRIBUTE_XPATH, TEXT_XPATH };

    private final IPortalSpELService portalSpELService;
    private final EvaluationContext ctx;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public SpELDataTemplatingStrategy(IPortalSpELService portalSpELService, EvaluationContext ctx) {
        this.portalSpELService = portalSpELService;
        this.ctx = ctx;
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
    }

    @Override
    public Source processTemplates(Document data, String filename) {

        log.trace("Processing templates for document XML={}", data.asXML());
        for (String xpath : XPATH_EXPRESSIONS) {
            @SuppressWarnings("unchecked")
            List<Node> nodes = data.selectNodes(xpath);
            for (Node n : nodes) {
                String inpt, otpt;
                switch (n.getNodeType()) {
                    case org.w3c.dom.Node.ATTRIBUTE_NODE:
                        Attribute a = (Attribute) n;
                        inpt = a.getValue();
                        otpt = processText(inpt);
                        if (!otpt.equals(inpt)) {
                            a.setValue(otpt);
                        }
                        break;
                    case org.w3c.dom.Node.TEXT_NODE:
                    case org.w3c.dom.Node.CDATA_SECTION_NODE:
                        inpt = n.getText();
                        otpt = processText(inpt);
                        if (!otpt.equals(inpt)) {
                            n.setText(otpt);
                        }
                        break;
                    default:
                        String msg = "Unsupported node type:  " + n.getNodeTypeName();
                        throw new RuntimeException(msg);
                }
            }
        }

        final SAXSource rslt = new DocumentSource(data);
        rslt.setSystemId(filename);  // must be set, else import chokes
        return rslt;

    }

    /*
     * Implementation
     */

    private String processText(String text) {
        String rslt = text;  // default
        Expression x = portalSpELService.parseExpression(text, PortalSpELServiceImpl.TemplateParserContext.INSTANCE);
        rslt = x.getValue(ctx, String.class);
        return rslt;
    }

}
