package org.jasig.portal.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Base for DAOs that interact with the "RawEventsDB" JPA Persistent Unit 
 * 
 * @author Eric Dalquist
 */
public class BaseRawEventsJpaDao extends BaseJpaDao {
    public static final String PERSISTENCE_UNIT_NAME = "RawEventsDB";
    
    private EntityManager entityManager;
    private TransactionOperations transactionOperations;

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Autowired
    @Qualifier(PERSISTENCE_UNIT_NAME)
    public final void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }
    
    @Override
    protected final EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    protected final TransactionOperations getTransactionOperations() {
        return this.transactionOperations;
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Transactional(PERSISTENCE_UNIT_NAME)
    public @interface RawEventsTransactional {
    }
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Transactional(value = PERSISTENCE_UNIT_NAME, propagation = Propagation.REQUIRES_NEW)
    public @interface RawEventsTransactionalRequiresNew {
    }
}
