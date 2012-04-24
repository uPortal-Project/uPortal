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

package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProfileEvaluatorFactory implements EvaluatorFactory {

    private static final int OR = 0;
    private static final int AND = 1;
    private static final int NOT = 2;

    @Override
    public Evaluator getEvaluator(Node audience) {
        return getGroupEvaluator( OR, audience );
    }

    private Evaluator getGroupEvaluator(int type, Node node) {
        NodeList nodes = node.getChildNodes();
        Evaluator container = null;

        if (nodes == null || nodes.getLength() == 0
                || (container = createGroupEvaluator(type, nodes)) == null) {
            throw new RuntimeException("Invalid content. Expected one to many "
                    + "<paren>, <NOT>, or <attribute> in '"
                    + XmlUtilitiesImpl.toString(node) + "'");
        }
        return container;
    }

    private Evaluator createGroupEvaluator(int type, NodeList nodes) {
        // if only one child skip wrapping in container for AND and OR
        if (nodes.getLength() == 1 && (type == OR || type == AND))
            return createEvaluator(nodes.item(0));

        Paren container = null;

        if (type == NOT)
            container = new Paren(Paren.Type.NOT);
        else if (type == OR)
            container = new Paren(Paren.Type.OR);
        else if (type == AND)
            container = new Paren(Paren.Type.AND);

        boolean validContentAdded = false;

        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Evaluator e = createEvaluator(nodes.item(i));

                if (e != null) {
                    validContentAdded = true;
                    container.addEvaluator(e);
                }
            }
        }
        if (validContentAdded)
            return container;
        return null;
    }

    private Evaluator createEvaluator(Node node) {
        String nodeName = node.getNodeName();

        if (nodeName.equals("paren"))
            return createParen(node);
        else if (nodeName.equals("profile"))
            return createProfileEvaluator(node);
        throw new RuntimeException("Unrecognized element '" + nodeName
                + "' in '" + XmlUtilitiesImpl.toString(node) + "'");
    }

    private Evaluator createParen(Node n) {
        NamedNodeMap attribs = n.getAttributes();
        Node opNode = attribs.getNamedItem("mode");

        if (opNode == null)
            throw new RuntimeException(
                    "Invalid mode. Expected 'AND','OR', or 'NOT'" + " in '"
                            + XmlUtilitiesImpl.toString(n) + "'");
        else if (opNode.getNodeValue().equals("OR"))
            return getGroupEvaluator(OR, n);
        else if (opNode.getNodeValue().equals("NOT"))
            return getGroupEvaluator(NOT, n);
        else if (opNode.getNodeValue().equals("AND"))
            return getGroupEvaluator(AND, n);
        else
            throw new RuntimeException(
                    "Invalid mode. Expected 'AND','OR', or 'NOT'" + " in '"
                            + XmlUtilitiesImpl.toString(n) + "'");
    }

    private Evaluator createProfileEvaluator(Node n) {
        NamedNodeMap attribs = n.getAttributes();
        Node attribNode = attribs.getNamedItem("value");

        if (attribNode == null || attribNode.getNodeValue().equals(""))
            throw new RuntimeException("Missing or empty value attribute in '"
                    + XmlUtilitiesImpl.toString(n) + "'");
        String value = attribNode.getNodeValue();
        Evaluator eval = null;

        try {
            eval = getProfileEvaluator(value);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() + " in '"
                    + XmlUtilitiesImpl.toString(n), e);
        }
        return eval;
    }

    public Evaluator getProfileEvaluator(String value) throws Exception {
        return new ProfileEvaluator(value);
    }

}
