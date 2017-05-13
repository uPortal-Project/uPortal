/*
 * Copyright 2014 Jasig.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jasig.portal.groups.db;

import java.net.URL;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author mfgsscw2
 */
public class ExternalDBGroupStoreFactory implements IEntityGroupStoreFactory {

    private static IEntityGroupStore groupStore;
    
    public static synchronized IEntityGroupStore getGroupStore() {
        if (groupStore == null) {
            
            URL u = ExternalDBGroupStoreFactory.class.getResource("/properties/groups/DBGroupStore.xml");
            FileSystemXmlApplicationContext spring_context = new FileSystemXmlApplicationContext(u.toExternalForm());
//            groupStore = spring_context.getBean(ExternalDBGroupStore.class);
            spring_context.registerShutdownHook();
            
            groupStore = new ExternalDBGroupStore(spring_context);
            
            ((ExternalDBGroupStore)groupStore).updateGroups();
        }
        return groupStore;
    }
    
    @Override
    public IEntityGroupStore newGroupStore() throws GroupsException {
        return getGroupStore();
    }

    @Override
    public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
        return getGroupStore();
    }
    
}
