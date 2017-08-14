/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.persondir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This component finds beans defined by adopters that implement <code>IPersonAttributeDao</code>
 * and incorporates them into the uPortal Person Directory (a.k.a. User Attribute) subsystem.  Beans
 * defined by uPortal itself will typically be "utility" objects -- caching, merging, etc.  Beans
 * defined by adopters will typically integrate with their data sources -- LDAP, RDBMS, etc.  Beans
 * from both sources must be combined in a logical way.
 */
@Component
public class AdopterDataSourcesIncorporator {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * This is the bean within the Person Directory stack whose childrden we must manipulate.
     */
    @Resource(name = "mergingPersonAttributeDao")
    private MergingPersonAttributeDaoImpl mergingPersonAttributeDao;

    /**
     * These are the children of <code>mergingPersonAttributeDao</code> defined by uPortal itself.
     * We need to find beans that implement <code>IPersonAttributeDao</code> defined by adopters
     * and add them to this list (at the end).  Finally, we need to provide the complete list to
     * the <code>mergingPersonAttributeDao</code>.
     */
    @Resource(name = "innerMergedPersonAttributeDaoList")
    private List<IPersonAttributeDao> innerMergedPersonAttributeDaoList;


    @Autowired
    @Qualifier("personAttributeDao")
    private IPersonAttributeDao rootPersonAttributeDao;

    /**
     * Together with the <code>rootPersonAttributeDao</code>, this collection is how we
     * differentiate internal from external beans.
     */
    @Autowired
    @Qualifier("uPortalInternal")
    private Set<IPersonAttributeDao> uPortalInternalPersonAttributeDaos;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {

        /*
         * Identify IPersonAttributeDao beans defined by adopters.
         */
        final Set<IPersonAttributeDao> adopterDaos = new HashSet<>();
        final Map<String,IPersonAttributeDao> allDaos = applicationContext.getBeansOfType(IPersonAttributeDao.class);
        for (Map.Entry<String,IPersonAttributeDao> y : allDaos.entrySet()) {
            logger.debug("Evaluating IPersonAttributeDao bean with id='{}' to see if it's uPortal " +
                    "internal or adopter-defined", y.getKey());
            if (!rootPersonAttributeDao.equals(y.getValue()) && !uPortalInternalPersonAttributeDaos.contains(y.getValue())) {
                logger.info("Identified adopter-defined IPersonAttributeDao bean with id='{}';  this " +
                        "bean will be added to Person Directory configuration", y.getKey());
                adopterDaos.add(y.getValue());
            }
        }

        /*
         * Combine adopterDaos with innerMergedPersonAttributeDaoList and finish the mergingPersonAttributeDao.
         */
        final List<IPersonAttributeDao> finalList = new ArrayList<>(innerMergedPersonAttributeDaoList);
        finalList.addAll(adopterDaos);
        mergingPersonAttributeDao.setPersonAttributeDaos(finalList);

    }

}
