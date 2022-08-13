package org.apereo.portal.dao.portletlist;

import org.apereo.portal.security.IPerson;

import java.util.List;

public interface IPortletListDao {

    public List<IPortletList> getPortletLists(String ownerUsername);

    public List<IPortletList> getPortletLists();

    public IPortletList getPortletList(String portletListUuid);

    public IPortletList createPortletList(IPortletList toCreate, IPerson requester);

    public IPortletList updatePortletList(IPortletList toUpdate, IPerson requester);

    public boolean removePortletListAsAdmin(String portletListUuid, IPerson adminRequester);

    public boolean removePortletListAsOwner(String portletListUuid, IPerson ownerRequester);
}
