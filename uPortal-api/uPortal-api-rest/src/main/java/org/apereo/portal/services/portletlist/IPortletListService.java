package org.apereo.portal.services.portletlist;

import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;

import java.util.List;
import java.util.Set;

public interface IPortletListService {
    public List<IPortletList> getPortletLists();

    public List<IPortletList> getPortletLists(IPerson requester);

    public boolean removePortletList(IPerson requester, String portletListUuid);

    /**
     * Returns null if not found
     * @param portletListUuid
     * @return
     */
    public IPortletList getPortletList(String portletListUuid);

    public IPortletList createPortletList(IPerson requester, IPortletList toCreate);

    public IPortletList updatePortletList(IPerson requester, IPortletList toUpdate);

    public boolean isPortletListAdmin(IPerson person);

}
