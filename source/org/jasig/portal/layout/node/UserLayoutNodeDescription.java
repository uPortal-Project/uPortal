/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayoutNodeDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An class describing common features of user layout nodes,
 * that is channels and folders
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public abstract class UserLayoutNodeDescription implements IUserLayoutNodeDescription {
    protected String id=null;
    protected String name=null;
    protected boolean immutable=false;
    protected boolean unremovable=false;
    protected boolean hidden=false;


    public UserLayoutNodeDescription() {};
    public UserLayoutNodeDescription(IUserLayoutNodeDescription d) {
        this.id=d.getId();
        this.name=d.getName();
        this.immutable=d.isImmutable();
        this.unremovable=d.isUnremovable();
        this.hidden=d.isHidden();
    }

    /**
     * Returns a node Id.
     * The Id has to be unique in the entire user layout document.
     *
     * @return a <code>String</code> value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set a new node Id.
     * The Id has to be unique in the entire user layout document.
     *
     */
    public void setId(String id) {
        this.id=id;
    }

    /**
     * Determine a name associated with this node.
     *
     * @return a folder/channel name.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public boolean isUnremovable() {
        return this.unremovable;
    }
    public void setUnremovable(boolean setting) {
        this.unremovable=setting;
    }

    public boolean isImmutable() {
        return this.immutable;
    }
    public void setImmutable(boolean setting) {
        this.immutable=setting;
    }

    public boolean isHidden() {
        return this.hidden;
    }
    public void setHidden(boolean setting) {
        this.hidden=setting;
    }

    /**
     * Returns a type of the node, could be FOLDER or CHANNEL integer constant.
     *
     * @return a type
     */
    public abstract int getType();


    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current node.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Element</code> value
     */
    public abstract Element getXML(Document root);

    /**
     * Add all of common node attributes to the <code>Element</code>.
     *
     * @param node an <code>Element</code> value
     */
    public void addNodeAttributes(Element node) {
        node.setAttribute("ID",this.getId());
        node.setAttribute("name",this.getName());
        node.setAttribute("unremovable",(new Boolean(this.isUnremovable())).toString());
        node.setAttribute("immutable",(new Boolean(this.isImmutable())).toString());
        node.setAttribute("hidden",(new Boolean(this.isHidden())).toString());
    }

    /**
     * A factory method to create a <code>UserLayoutNodeDescription</code> instance,
     * based on the information provided in the user layout <code>Element</code>.
     *
     * @param xmlNode a user layout DTD folder/channel <code>Element</code> value
     * @return an <code>UserLayoutNodeDescription</code> value
     * @exception PortalException if the xml passed is somehow invalid.
     */
    public static UserLayoutNodeDescription createUserLayoutNodeDescription(Element xmlNode) throws PortalException {
        // is this a folder or a channel ?
        String nodeName=xmlNode.getNodeName();
        if(nodeName.equals("channel")) {
            return new UserLayoutChannelDescription(xmlNode);
        } else if(nodeName.equals("folder")) {
            return new UserLayoutFolderDescription(xmlNode);
        } else {
            throw new PortalException("Given XML element is neither folder nor channel");
        }
    }

}
