/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.persondir;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

public interface ILocalAccountDao extends IPersonAttributeDao {
    
    public ILocalAccountPerson updateAccount(ILocalAccountPerson account);

    public ILocalAccountPerson getPerson(long id);

    public ILocalAccountPerson getPerson(String username);
    
    public List<ILocalAccountPerson> getAllAccounts();
    
    public void deleteAccount(ILocalAccountPerson account);

    public Set<IPersonAttributes> getPeople(Map<String,Object> query);
}
