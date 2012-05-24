package org.jasig.portal.jpa;

import javax.persistence.EntityManager;

/**
 * Event fired immediately before the {@link EntityManager} is closed
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EntityManagerClosingEvent extends AbstractEntityManagerEvent {
    private static final long serialVersionUID = 1L;

    public EntityManagerClosingEvent(Object source, long entityManagerId, String persistenceUnitName,
            EntityManager entityManager) {
        super(source, entityManagerId, persistenceUnitName, entityManager);
    }

    @Override
    public String toString() {
        return "EntityManagerClosingEvent [entityManagerId=" + getEntityManagerId() + "]";
    }
}
