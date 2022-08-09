package org.apereo.portal.dao.portletlist;

import java.util.List;

public interface IPortletListDao {

//    public IPersonAttributesGroupDefinition updatePersonAttributesGroupDefinition(
//        IPersonAttributesGroupDefinition personAttributesGroupDefinition);
//
//    public void deletePersonAttributesGroupDefinition(IPersonAttributesGroupDefinition definition);

    public List<IPortletList> getPortletLists(String userId);

    public List<IPortletList> getPortletLists();

    public IPortletList getPortletList(String portletListUuid);

    public IPortletList createPortletList(IPortletList toCreate);

    public IPortletList updatePortletList(IPortletList toUpdate, String portletListUuid);
}
