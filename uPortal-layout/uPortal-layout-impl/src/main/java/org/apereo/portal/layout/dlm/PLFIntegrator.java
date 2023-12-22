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
package org.apereo.portal.layout.dlm;

import org.apereo.portal.PortalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Performs merging of PLF into ILF for DLM.
 *
 * @since 2.5
 */
public class PLFIntegrator {

    private static Logger LOG = LoggerFactory.getLogger(PLFIntegrator.class);

    public static void mergePLFintoILF(Document plf, Document ilf, IntegrationResult result)
            throws PortalException {
        // we want to use the root folders located as the single child of
        // the layout elements which are the document elements.
        Element plfLayout = plf.getDocumentElement();
        Element plfRoot = (Element) plfLayout.getFirstChild();
        Element ilfLayout = ilf.getDocumentElement();
        Element ilfRoot = (Element) ilfLayout.getFirstChild();

        DeleteManager.applyAndUpdateDeleteSet(plf, ilf, result);
        ParameterEditManager.applyAndUpdateParmEditSet(plf, ilf, result);

        if (null == plfRoot) {
            throw new RuntimeException(
                    "The PLF layout root element is missing, so it appears "
                            + "that the database has been corrupted.");
        }

        NodeInfoTracker tracker = new NodeInfoTracker();
        applyChildChanges(plfRoot, ilfRoot, result, tracker);
    }

    private static void applyChildChanges(
            Element plfParent, Element ilfParent, IntegrationResult result, NodeInfoTracker tracker)
            throws PortalException {

        Element positions = null;
        Element node = (Element) plfParent.getFirstChild();

        while (node != null) {
            Element nextNode = (Element) node.getNextSibling();

            if (node.getNodeName().equals("folder"))
                mergeFolder(node, plfParent, ilfParent, result, tracker);
            else if (node.getNodeName().equals(Constants.ELM_POSITION_SET)) positions = node;
            else if (node.getNodeName().equals("channel"))
                mergeChannel(node, plfParent, ilfParent, result, tracker);
            node = nextNode;
        }

        if (positions != null) {
            IntegrationResult posResult = new IntegrationResult();
            if (LOG.isInfoEnabled()) LOG.info("applying positions");
            PositionManager.applyPositions(ilfParent, positions, posResult, tracker);
            if (!posResult.isChangedILF()) {
                if (LOG.isInfoEnabled()) LOG.info("removing positionSet");
                plfParent.removeChild(positions);
                result.setChangedPLF(true);
            } else {
                result.setChangedILF(true);
                if (posResult.isChangedPLF()) result.setChangedPLF(true);
            }
        }
    }

    private static void mergeChannel(
            Element plfChild,
            Element plfParent,
            Element ilfParent,
            IntegrationResult result,
            NodeInfoTracker tracker) {

        String id = plfChild.getAttribute(Constants.ATT_ID);

        if (id.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)) {
            // incorporated channel - if a copy of an inc'd channel is in the
            // plf it is because either it has attribute edits. It does not
            // imply movement.
            // That is accomplished by the position set. So see if it still
            // exists in the ilf for applying changes

            Document ilf = ilfParent.getOwnerDocument();
            Element original = ilf.getElementById(id);

            if (original == null) {
                // not there anymore, discard from plf
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
                return;
            }

            // found it, apply changes and see if they had any affect
            boolean attributeChanged = false;
            IntegrationResult childChanges = new IntegrationResult();

            attributeChanged = EditManager.applyEditSet(plfChild, original);
            applyChildChanges(plfChild, original, childChanges, tracker);

            if (attributeChanged == false && !childChanges.isChangedILF()) {
                // no changes were used so remove this guy from plf.
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
            } else result.setChangedILF(true);
            // need to pass on up whether PLF changed in called methods
            if (childChanges.isChangedPLF()) result.setChangedPLF(true);
        } else // plf channel
        {
            if (LOG.isInfoEnabled()) LOG.info("merging into ilf channel " + id);

            if (ilfParent.getAttribute(Constants.ATT_ADD_CHILD_ALLOWED).equals("false")) {
                if (LOG.isInfoEnabled())
                    LOG.info("removing from plf disallowed add of channel " + id);
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
            } else {
                appendChild(plfChild, ilfParent, true);
                result.setChangedILF(true);
            }
        }
    }

    private static void mergeFolder(
            Element plfChild,
            Element plfParent,
            Element ilfParent,
            IntegrationResult result,
            NodeInfoTracker tracker)
            throws PortalException {

        String id = plfChild.getAttribute(Constants.ATT_ID);

        if (id.startsWith(Constants.FRAGMENT_ID_USER_PREFIX)) {
            // incorporated folder - if a copy of an inc'd folder is in the
            // plf it is because either it has attribute edits or there are
            // nested child changes to be applied. It does not imply movement.
            // That is accomplished by the position set. So see if it still
            // exists in the ilf for applying changes

            Document ilf = ilfParent.getOwnerDocument();
            Element original = ilf.getElementById(id);

            if (original == null) {
                // not there anymore, discard from plf
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
                return;
            }

            // found it, apply changes and see if they had any affect
            boolean attributeChanged = false;
            IntegrationResult childChanges = new IntegrationResult();

            attributeChanged = EditManager.applyEditSet(plfChild, original);
            applyChildChanges(plfChild, original, childChanges, tracker);

            if (attributeChanged == false && !childChanges.isChangedILF()) {
                // no changes were used so remove this guy from plf.
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
            } else result.setChangedILF(true);
            // need to pass on up whether PLF changed in called methods
            if (childChanges.isChangedPLF()) result.setChangedPLF(true);
        } else {
            // plf folder - the real node. What is being portrayed in this
            // case is a plf node that is supposed to be added into the ilf
            // parent

            if (ilfParent.getAttribute(Constants.ATT_ADD_CHILD_ALLOWED).equals("false")) {
                // nope, delete directive from plf
                if (LOG.isInfoEnabled()) LOG.info("removing folder from plf " + id);
                plfParent.removeChild(plfChild);
                result.setChangedPLF(true);
                return;
            }
            Element ilfChild = appendChild(plfChild, ilfParent, false);
            result.setChangedILF(true);

            IntegrationResult childChanges = new IntegrationResult();
            applyChildChanges(plfChild, ilfChild, childChanges, tracker);

            if (childChanges.isChangedPLF()) result.setChangedPLF(true);
        }
    }

    /** This method copies a plf node and any of its children into the passed in compViewParent. */
    static Element appendChild(Element plfChild, Element parent, boolean copyChildren) {
        Document document = parent.getOwnerDocument();
        Element copy = (Element) document.importNode(plfChild, false);
        parent.appendChild(copy);

        // set the identifier for the doc if warranted
        String id = copy.getAttribute(Constants.ATT_ID);
        if (id != null && !id.equals("")) copy.setIdAttribute(Constants.ATT_ID, true);

        if (copyChildren) {
            NodeList children = plfChild.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element)
                    appendChild((Element) children.item(i), copy, true);
            }
        }
        return copy;
    }
}
