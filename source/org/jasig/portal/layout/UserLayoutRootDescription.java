package org.jasig.portal.layout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.jasig.portal.PortalException;

/**
 * This is a temp solution for top-level <layout> node element.
 * Eventually, we would like the top-level element to become a full-blown folder,
 * but this requires many changes all over the code and XSLT :(
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class UserLayoutRootDescription extends UserLayoutFolderDescription {
    public UserLayoutRootDescription(Element xmlNode) {
        this.setImmutable(false);
        this.setUnremovable(false);
        /*
        // nothign to do really .. we can try reading immutable/unremovable since they might be added at some point
        Boolean unValue=new Boolean(xmlNode.getAttribute("unremovable"));
        Boolean imValue=new Boolean(xmlNode.getAttribute("immutable"));
        if(unValue!=null){
            this.setUnremovable(unValue.booleanValue());
        }
        if(imValue!=null){
            this.setImmutable(imValue.booleanValue());
        } else {

        }
        */
    }

    public String getId() {
        return UserLayoutNodeDescription.ROOT_FOLDER_ID;
    }
    public void setId(String id) {};
}
