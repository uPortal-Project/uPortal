package org.apereo.portal.services.portletlist;

import java.util.List;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.security.IPerson;

public interface IPortletListService {
    public List<IPortletList> getPortletLists();

    public List<IPortletList> getPortletLists(IPerson requester);

    public boolean removePortletList(IPerson requester, String portletListUuid);

    /**
     * Returns null if not found
     *
     * @param portletListUuid
     * @return
     */
    public IPortletList getPortletList(String portletListUuid);

    public IPortletList createPortletList(IPerson requester, IPortletList toCreate);

    public IPortletList updatePortletList(IPerson requester, IPortletList toUpdate);

    public boolean isPortletListAdmin(IPerson person);
}
