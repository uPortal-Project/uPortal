package org.jasig.portal.jpa;

import javax.persistence.EntityManager;

/**
 * Event fired immediately after the {@link EntityManager} is created 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EntityManagerCreatedEvent extends AbstractEntityManagerEvent {
    private static final long serialVersionUID = 1L;

    public EntityManagerCreatedEvent(Object source, long entityManagerId, String persistenceUnitName,
            EntityManager entityManager) {
        super(source, entityManagerId, persistenceUnitName, entityManager);
    }

    @Override
    public String toString() {
        return "EntityManagerCreatedEvent [entityManagerId=" + getEntityManagerId() + "]";
    }
}
