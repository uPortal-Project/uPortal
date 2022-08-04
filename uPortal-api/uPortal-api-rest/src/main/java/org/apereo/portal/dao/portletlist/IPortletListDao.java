package org.apereo.portal.dao.portletlist;

import java.util.List;
import java.util.Set;

public interface IPortletListDao {

//    public IPersonAttributesGroupDefinition updatePersonAttributesGroupDefinition(
//        IPersonAttributesGroupDefinition personAttributesGroupDefinition);
//
//    public void deletePersonAttributesGroupDefinition(IPersonAttributesGroupDefinition definition);

    public List<IPortletList> getPortletLists(String userId);

    public IPortletList getPortletList(String userId, String portletListUuid);

    public IPortletList createPortletList(IPortletList toCreate);
}
