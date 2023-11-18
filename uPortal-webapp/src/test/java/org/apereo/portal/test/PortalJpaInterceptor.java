package org.apereo.portal.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.orm.jpa.EntityManagerFactoryAccessor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Replacement for a class that was deprecated and later removed from Spring. There's a good
 * replacement (<code>OpenEntityManagerInViewInterceptor</code>) in Spring for the non-test code,
 * but it's not a match for what {@link BaseJpaDaoTest} does because it doesn't implement <code>
 * MethodInterceptor</code> from AOP Alliance.
 *
 * @since 5.0
 */
public class PortalJpaInterceptor extends EntityManagerFactoryAccessor
        implements MethodInterceptor {

    private boolean flushEager = false;
    private boolean exceptionConversionEnabled = true;
    private JpaDialect jpaDialect = new DefaultJpaDialect();

    /** Return if this accessor should flush changes to the database eagerly. */
    public boolean isFlushEager() {
        return this.flushEager;
    }

    /**
     * Return the JPA dialect to use for this accessor.
     *
     * <p>Creates a default one for the specified EntityManagerFactory if none set.
     */
    public JpaDialect getJpaDialect() {
        return this.jpaDialect;
    }

    /**
     * Set the JPA dialect to use for this accessor.
     *
     * <p>The dialect object can be used to retrieve the underlying JDBC connection, for example.
     */
    public void setJpaDialect(JpaDialect jpaDialect) {
        this.jpaDialect = (jpaDialect != null ? jpaDialect : new DefaultJpaDialect());
    }

    /**
     * Set whether to convert any PersistenceException raised to a Spring DataAccessException,
     * compatible with the {@code org.springframework.dao} exception hierarchy.
     *
     * <p>Default is "true". Turn this flag off to let the caller receive raw exceptions as-is,
     * without any wrapping.
     *
     * @see org.springframework.dao.DataAccessException
     */
    public void setExceptionConversionEnabled(boolean exceptionConversionEnabled) {
        this.exceptionConversionEnabled = exceptionConversionEnabled;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // Determine current EntityManager: either the transactional one
        // managed by the factory or a temporary one for the given invocation.
        EntityManager em = getTransactionalEntityManager();
        boolean isNewEm = false;
        if (em == null) {
            logger.debug("Creating new EntityManager for JpaInterceptor invocation");
            em = createEntityManager();
            isNewEm = true;
            TransactionSynchronizationManager.bindResource(
                    getEntityManagerFactory(), new EntityManagerHolder(em));
        }

        try {
            Object retVal = methodInvocation.proceed();
            flushIfNecessary(em, !isNewEm);
            return retVal;
        } catch (RuntimeException rawException) {
            if (this.exceptionConversionEnabled) {
                // Translation enabled. Translate if we understand the exception.
                throw translateIfNecessary(rawException);
            } else {
                // Translation not enabled. Don't try to translate.
                throw rawException;
            }
        } finally {
            if (isNewEm) {
                TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
                EntityManagerFactoryUtils.closeEntityManager(em);
            }
        }
    }

    /**
     * Convert the given runtime exception to an appropriate exception from the {@code
     * org.springframework.dao} hierarchy if necessary, or return the exception itself if it is not
     * persistence related
     *
     * <p>Default implementation delegates to the JpaDialect. May be overridden in subclasses.
     *
     * @param ex runtime exception that occurred, which may or may not be JPA-related
     * @return the corresponding DataAccessException instance if wrapping should occur, otherwise
     *     the raw exception
     * @see org.springframework.dao.support.DataAccessUtils#translateIfNecessary
     */
    public RuntimeException translateIfNecessary(RuntimeException ex) {
        return DataAccessUtils.translateIfNecessary(ex, getJpaDialect());
    }

    /**
     * Flush the given JPA entity manager if necessary.
     *
     * @param em the current JPA PersistenceManage
     * @param existingTransaction if executing within an existing transaction
     * @throws javax.persistence.PersistenceException in case of JPA flushing errors
     */
    protected void flushIfNecessary(EntityManager em, boolean existingTransaction)
            throws PersistenceException {
        if (isFlushEager()) {
            logger.debug("Eagerly flushing JPA entity manager");
            em.flush();
        }
    }
}
