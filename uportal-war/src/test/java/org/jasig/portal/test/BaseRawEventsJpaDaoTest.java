package org.jasig.portal.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.jpa.BaseRawEventsJpaDao;

/**
 * Base class for RawEventsDb unit tests that want TX and entity manager support.
 * 
 * @author Eric Dalquist
 */
public abstract class BaseRawEventsJpaDaoTest extends BaseJpaDaoTest {
    private EntityManager entityManager;

    @PersistenceContext(unitName = BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    protected final EntityManager getEntityManager() {
        return this.entityManager;
    }
}
