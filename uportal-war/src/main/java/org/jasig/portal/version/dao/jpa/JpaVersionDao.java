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
package org.jasig.portal.version.dao.jpa;

import org.hibernate.exception.SQLGrammarException;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.springframework.stereotype.Repository;

@Repository("versionDao")
public class JpaVersionDao extends BasePortalJpaDao implements VersionDao {

    @Override
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public VersionImpl getVersion(String product) {
        NaturalIdQuery<VersionImpl> query = this.createNaturalIdQuery(VersionImpl.class);
        query.using(VersionImpl_.product, product);
        try { 
            return query.load();
        }
        catch (SQLGrammarException e) {
            logger.warn("UP_VERSION table doesn't exist, returning null for version of " + product);
            return null;
        }
    }

    @Override
    @PortalTransactional
    public Version setVersion(String product, int major, int minor, int patch) {
        VersionImpl version = getVersion(product);
        if (version == null) {
            version = new VersionImpl(product, major, minor, patch);
        }
        else {
            version.setMajor(major);
            version.setMinor(minor);
            version.setPatch(patch);
        }
        
        this.getEntityManager().persist(version);
        
        return version;
    }

    @Override
    @PortalTransactional
    public Version setVersion(String product, Version version) {
        return this.setVersion(product, version.getMajor(), version.getMinor(), version.getPatch());
    }
    
    
}
