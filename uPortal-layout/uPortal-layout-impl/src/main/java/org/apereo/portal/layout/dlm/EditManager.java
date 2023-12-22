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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.spring.locator.UserLayoutStoreLocator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles ILF node edit directives recorded in the PLF.
 *
 * @since 2.5
 */
public class EditManager {
    private static IUserLayoutStore dls = null;
    private static final Log LOG = LogFactory.getLog(EditManager.class);

    /**
     * Hands back the single instance of RDBMDistributedLayoutStore. There is already a method for
     * acquiring a single instance of the configured layout store so we delegate over there so that
     * all references refer to the same instance. This method is solely for convenience so that we
     * don't have to keep calling UserLayoutStoreFactory and casting the resulting class.
     */
    private static IUserLayoutStore getDLS() {
        if (dls == null) {
            dls = UserLayoutStoreLocator.getUserLayoutStore();
        }
        return dls;
    }

    /**
     * Get the edit set if any stored in the passed in node. If not found and if the create flag is
     * true then create a new edit set and add it as a child to the passed in node. Then return it.
     */
    private static Element getEditSet(Element node, Document plf, IPerson person, boolean create)
            throws PortalException {
        Node child = node.getFirstChild();

        while (child != null) {
            if (child.getNodeName().equals(Constants.ELM_EDIT_SET)) return (Element) child;
            child = child.getNextSibling();
        }

        if (create == false) return null;

        String ID = null;

        try {
            ID = getDLS().getNextStructDirectiveId(person);
        } catch (Exception e) {
            throw new PortalException(
                    "Exception encountered while "
                            + "generating new edit set node "
                            + "Id for userId="
                            + person.getID(),
                    e);
        }
        Element editSet = plf.createElement(Constants.ELM_EDIT_SET);
        editSet.setAttribute(Constants.ATT_TYPE, Constants.ELM_EDIT_SET);
        editSet.setAttribute(Constants.ATT_ID, ID);
        node.appendChild(editSet);
        return editSet;
    }

    /**
     * Create and append an edit directive to the edit set if not there. This only records that the
     * attribute was changed and the value in the plf copy node should be used, if allowed, during
     * the merge at login time.
     */
    static void addEditDirective(Element plfNode, String attributeName, IPerson person)
            throws PortalException {
        addDirective(plfNode, attributeName, Constants.ELM_EDIT, person);
    }

    /**
     * Create and append a user preferences edit directive to the edit set if not there. This only
     * records that the attribute was changed. The value will be in the user preferences object for
     * the user.
     */
    public static void addPrefsDirective(Element plfNode, String attributeName, IPerson person)
            throws PortalException {
        addDirective(plfNode, attributeName, Constants.ELM_PREF, person);
    }

    /** Create and append an edit directive to the edit set if not there. */
    private static void addDirective(
            Element plfNode, String attributeName, String type, IPerson person)
            throws PortalException {
        Document plf = (Document) person.getAttribute(Constants.PLF);
        Element editSet = getEditSet(plfNode, plf, person, true);

        // see if attributes has already been marked as being edited
        Element child = (Element) editSet.getFirstChild();
        Element edit = null;

        while (child != null && edit == null) {
            if (child.getNodeName().equals(type)
                    && child.getAttribute(Constants.ATT_NAME).equals(attributeName)) edit = child;
            child = (Element) child.getNextSibling();
        }
        if (edit == null) // if not found then newly mark as edited
        {
            String ID = null;

            try {
                ID = getDLS().getNextStructDirectiveId(person);
            } catch (Exception e) {
                throw new PortalException(
                        "Exception encountered while "
                                + "generating new edit node "
                                + "Id for userId="
                                + person.getID(),
                        e);
            }
            edit = plf.createElement(type);
            edit.setAttribute(Constants.ATT_TYPE, type);
            edit.setAttribute(Constants.ATT_ID, ID);
            edit.setAttribute(Constants.ATT_NAME, attributeName);
            editSet.appendChild(edit);
        }
    }

    /**
     * Evaluate whether attribute changes exist in the ilfChild and if so apply them. Returns true
     * if some changes existed. If changes existed but matched those in the original node then they
     * are not applicable, are removed from the editSet, and false is returned.
     */
    public static boolean applyEditSet(Element plfChild, Element original) {
        // first get edit set if it exists
        Element editSet = null;
        try {
            editSet = getEditSet(plfChild, null, null, false);
        } catch (Exception e) {
            // should never occur unless problem during create in getEditSet
            // and we are telling it not to create.
            return false;
        }

        if (editSet == null || editSet.getChildNodes().getLength() == 0) return false;

        if (original.getAttribute(Constants.ATT_EDIT_ALLOWED).equals("false")) {
            // can't change anymore so discard changes
            plfChild.removeChild(editSet);
            return false;
        }

        Document ilf = original.getOwnerDocument();
        boolean attributeChanged = false;
        Element edit = (Element) editSet.getFirstChild();

        while (edit != null) {
            String attribName = edit.getAttribute(Constants.ATT_NAME);
            Attr attr = plfChild.getAttributeNode(attribName);

            // preferences are only updated at preference storage time so
            // if a preference change exists in the edit set assume it is
            // still valid so that the node being edited will persist in
            // the PLF.
            if (edit.getNodeName().equals(Constants.ELM_PREF)) attributeChanged = true;
            else if (attr == null) {
                // attribute removed. See if needs removing in original.
                attr = original.getAttributeNode(attribName);
                if (attr == null) // edit irrelevant,
                editSet.removeChild(edit);
                else {
                    // edit differs, apply to original
                    original.removeAttribute(attribName);
                    attributeChanged = true;
                }
            } else {
                // attribute there, see if original is also there
                Attr origAttr = original.getAttributeNode(attribName);
                if (origAttr == null) {
                    // original attribute isn't defined so need to add
                    origAttr = (Attr) ilf.importNode(attr, true);
                    original.setAttributeNode(origAttr);
                    attributeChanged = true;
                } else {
                    // original attrib found, see if different
                    if (attr.getValue().equals(origAttr.getValue())) {
                        // they are the same, edit irrelevant
                        editSet.removeChild(edit);
                    } else {
                        // edit differs, apply to original
                        origAttr.setValue(attr.getValue());
                        attributeChanged = true;
                    }
                }
            }
            edit = (Element) edit.getNextSibling();
        }
        return attributeChanged;
    }

    /**
     * Searches for a dlm:pref command which indicates that a user preference was change and if
     * found removes it from the user's PLF.
     */
    public static void removePreferenceDirective(
            IPerson person, String elementId, String attributeName) {
        removeDirective(elementId, attributeName, Constants.ELM_PREF, person);
    }

    /**
     * Searches for a dlm:edit command which indicates that a node attribute was reset to the value
     * in the fragment and if found removes it from the user's PLF.
     */
    public static void removeEditDirective(String elementId, String attributeName, IPerson person) {
        removeDirective(elementId, attributeName, Constants.ELM_EDIT, person);
    }

    /** Searches for a command of the passed-in type and if found removes it from the user's PLF. */
    private static void removeDirective(
            String elementId, String attributeName, String type, IPerson person) {
        Document plf = (Document) person.getAttribute(Constants.PLF);
        Element node = plf.getElementById(elementId);
        if (node == null) return;

        Element editSet = null;

        try {
            editSet = getEditSet(node, plf, person, false);
        } catch (Exception e) {
            /*
             * we should never get here since we are calling getEditSet passing
             * create=false meaning that the only portion of that method that
             * tosses an exception will not be reached with this call. But if a
             * runtime exception somehow occurs we will log it so that we don't
             * lose the information.
             */
            LOG.error(e, e);
            return;
        }

        // if no edit set then the edit can't be there either
        if (editSet == null) return;

        Node child = editSet.getFirstChild();

        while (child != null) {
            if (child.getNodeName().equals(type)) {
                Attr attr = ((Element) child).getAttributeNode(Constants.ATT_NAME);
                if (attr != null && attr.getValue().equals(attributeName)) {
                    // we found it, remove it
                    editSet.removeChild(child);
                    break;
                }
            }
            child = child.getNextSibling();
        }
        // if that was the last on in the edit set then delete it
        if (editSet.getFirstChild() == null) node.removeChild(editSet);
    }
}
