package org.apereo.portal.dao.portletlist;

import org.apereo.portal.dao.portletlist.jpa.PortletListItem;
import org.dom4j.Element;

import java.util.List;

public interface IPortletList {

    String getId();

    String getUserId();

    void setUserId(String userId);

    String getName();

    void setName(String name);

    List<PortletListItem> getItems();

    void setItems(List<PortletListItem> items);

    //Set<IPersonPortletListItem> getListItems();

    //void setListItems(Set<IPersonPortletListItem> items);

    /** Supports exporting. */
    void toElement(Element parent);

    String toString();

    void overrideItems(List<PortletListItem> items);
}
