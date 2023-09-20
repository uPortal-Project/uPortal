package org.apereo.portal.portlet.registry;

import org.apereo.portal.portlet.om.IPortletDefinitionId;

// TODO this matches PortletDefinitionIdImpl
// May want to make PortletDefinitionIdImpl publicly available and delete the
// InternalPortletDefinitionId
// to DRY out the code
public class InternalPortletDefinitionId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;
    private long id;

    public InternalPortletDefinitionId(long id) {
        this.id = id;
    }

    @Override
    public String getStringId() {
        return Long.toString(id);
    }

    @Override
    public long getLongId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        IPortletDefinitionId other = (IPortletDefinitionId) obj;
        return getStringId().equals(other.getStringId());
    }
}
