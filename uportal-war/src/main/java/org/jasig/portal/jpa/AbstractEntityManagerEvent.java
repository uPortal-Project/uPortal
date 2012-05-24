package org.jasig.portal.jpa;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationEvent;

/**
 * Base {@link EntityManager} related event.
 * 
 * @author Eric Dalquist
 */
public abstract class AbstractEntityManagerEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final String entityManagerId;
    private final String persistenceUnitName;
    private final transient EntityManager entityManager;
    
    AbstractEntityManagerEvent(Object source, long entityManagerId, String persistenceUnitName, EntityManager entityManager) {
        super(source);
        this.entityManagerId = persistenceUnitName + "." + entityManagerId;
        this.persistenceUnitName = persistenceUnitName;
        this.entityManager = entityManager;
    }
    
    /**
     * Unique id (within this JVM) for the entity manager
     */
    public String getEntityManagerId() {
        return entityManagerId;
    }
    
    /**
     * Name of the JPA Persistent Unit
     */
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * The EntityManager, transient so may be null if event has been serialized
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
