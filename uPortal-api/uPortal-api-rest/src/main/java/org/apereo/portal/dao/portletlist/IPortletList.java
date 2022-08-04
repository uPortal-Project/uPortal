package org.apereo.portal.dao.portletlist;

import org.dom4j.Element;

public interface IPortletList {

    String getId();

    String getUserId();

    void setUserId(String userId);

    String getName();

    void setName(String name);

    //Set<IPersonPortletListItem> getListItems();

    //void setListItems(Set<IPersonPortletListItem> items);

    /** Supports exporting. */
    void toElement(Element parent);

    String toString();
}
