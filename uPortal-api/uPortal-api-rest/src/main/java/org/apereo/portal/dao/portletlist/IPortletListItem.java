package org.apereo.portal.dao.portletlist;

import org.apereo.portal.dao.portletlist.jpa.PortletListItemPK;
import org.dom4j.Element;

public interface IPortletListItem {

    PortletListItemPK getPortletListItemPK();

    void setPortletListItemPK(PortletListItemPK portletListItemPK);

    String getEntityId();

    void setEntityId(String id);

    String toString();
}
