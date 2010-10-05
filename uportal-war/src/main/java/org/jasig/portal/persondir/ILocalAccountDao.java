package org.jasig.portal.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

public interface ILocalAccountDao extends IPersonAttributeDao {
    
    public ILocalAccountPerson updateAccount(ILocalAccountPerson account);
    
    public ILocalAccountPerson getPerson(String username);
    
    public List<ILocalAccountPerson> getAllAccounts();
    
    public void deleteAccount(ILocalAccountPerson account);

    public Set<IPersonAttributes> getPeople(Map<String,Object> query);
}
