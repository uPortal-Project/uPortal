package org.apereo.portal.dao.portletlist;

import org.apereo.portal.dao.portletlist.jpa.PortletList;

public interface IPortletListItem {

    PortletList getPortletList();

    void setPortletList(PortletList portletList);

    int getListOrder();

    void setListOrder(int listOrder);

    String getEntityId();

    void setEntityId(String id);

    String toString();
}
