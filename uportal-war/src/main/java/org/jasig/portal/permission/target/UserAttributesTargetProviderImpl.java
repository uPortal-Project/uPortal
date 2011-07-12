package org.jasig.portal.permission.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;

public class UserAttributesTargetProviderImpl implements
        IPermissionTargetProvider {

    private IPersonAttributeDao personAttributeDao;
    
    /**
     * The {@link IPersonAttributeDao} used to perform lookups.
     */
    public void setPersonAttributeDao(IPersonAttributeDao personLookupDao) {
        this.personAttributeDao = personLookupDao;
    }


    @Override
    public IPermissionTarget getTarget(String key) {
        final Set<String> attributes = personAttributeDao.getAvailableQueryAttributes();
        if (attributes.contains(key)) {
            return new PermissionTargetImpl(key, key);
        } else {
            return null;
        }
    }

    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {
        term = term.toLowerCase();
        final Set<String> attributes = personAttributeDao.getAvailableQueryAttributes();
        final List<IPermissionTarget> matches = new ArrayList<IPermissionTarget>();
        for (String attribute : attributes) {
            if (attribute.toLowerCase().contains(term)) {
                matches.add(new PermissionTargetImpl(attribute, attribute));
            }
        }
        return matches;
    }

}
