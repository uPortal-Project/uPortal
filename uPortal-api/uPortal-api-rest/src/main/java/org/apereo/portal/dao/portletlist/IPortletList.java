package org.apereo.portal.dao.portletlist;

import java.util.List;
import org.apereo.portal.dao.portletlist.jpa.PortletListItem;
import org.apereo.portal.security.IPerson;
import org.dom4j.Element;

public interface IPortletList {

    String getId();

    String getOwnerUsername();

    void setOwnerUsername(String username);

    String getName();

    void setName(String name);

    List<PortletListItem> getItems();

    // Don't use this setter directly - instead use clearAndSetItems(...). Hibernate uses this
    // method to handle its own
    // List management
    void setItems(List<PortletListItem> items);

    /** Supports exporting. */
    void toElement(Element parent);

    String toString();

    void clearAndSetItems(List<PortletListItem> items);

    void prepareForPersistence(IPerson requester);
}
