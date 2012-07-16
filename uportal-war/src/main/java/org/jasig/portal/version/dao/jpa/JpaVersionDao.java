package org.jasig.portal.version.dao.jpa;

import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.springframework.stereotype.Repository;

@Repository("versionDao")
public class JpaVersionDao extends BasePortalJpaDao implements VersionDao {

    @Override
    @OpenEntityManager(unitName=BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public VersionImpl getVersion(String product) {
        NaturalIdQuery<VersionImpl> query = this.createNaturalIdQuery(VersionImpl.class);
        query.using(VersionImpl_.product, product);
        return query.load();
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
}
