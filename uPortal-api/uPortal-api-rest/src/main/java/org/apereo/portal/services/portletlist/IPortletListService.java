package org.apereo.portal.services.portletlist;

import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.security.IPerson;

import java.util.List;
import java.util.Set;

public interface IPortletListService {
    public List<IPortletList> getPortletLists(IPerson owner);

    /**
     * Returns null if not found
     * @param owner
     * @param portletListUuid
     * @return
     */
    public IPortletList getPortletList(IPerson owner, String portletListUuid);

    public IPortletList createPortletList(IPerson owner, IPortletList toCreate);
}
