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

package org.jasig.portal.concurrency.locking;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.PessimisticLockException;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

/**
 * DB based locking DAO using JPA2 locking APIs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaClusterLockDao extends BasePortalJpaDao implements IClusterLockDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ParameterExpression<String> nameParameter;
    private CriteriaQuery<ClusterMutex> clusterLockByNameQuery;

    /* (non-Javadoc)
     * @see org.jasig.portal.jpa.BaseJpaDao#buildCriteriaQueries(javax.persistence.criteria.CriteriaBuilder)
     */
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder criteriaBuilder) {
        this.nameParameter = criteriaBuilder.parameter(String.class, "name");
        
        this.clusterLockByNameQuery = buildGetClusterLockByNameQuery(criteriaBuilder);
    }

    protected CriteriaQuery<ClusterMutex> buildGetClusterLockByNameQuery(CriteriaBuilder criteriaBuilder) {
        final CriteriaQuery<ClusterMutex> criteriaQuery = criteriaBuilder.createQuery(ClusterMutex.class);
        final Root<ClusterMutex> definitionRoot = criteriaQuery.from(ClusterMutex.class);
        criteriaQuery.select(definitionRoot);
        criteriaQuery.where(
            criteriaBuilder.equal(definitionRoot.get(ClusterMutex_.name), this.nameParameter)
        );
        return criteriaQuery;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.ClusterLockDao#getClusterMutex(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public ClusterMutex getClusterMutex(final String mutexName) {
        final TypedQuery<ClusterMutex> query = this.entityManager.createQuery(this.clusterLockByNameQuery);
        query.setParameter(this.nameParameter, mutexName);
        final List<ClusterMutex> results = query.getResultList();
        return DataAccessUtils.singleResult(results);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.ClusterLockDao#createClusterMutex(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void createClusterMutex(final String lockName) throws DataIntegrityViolationException {
        this.entityManager.persist(new ClusterMutex(lockName));
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.ClusterLockDao#lock(org.jasig.portal.concurrency.locking.ClusterMutex)
     */
    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public void lock(ClusterMutex mutex) {
        if (this.isLocked(mutex)) {
            return;
        }
        
        this.entityManager.lock(mutex, LockModeType.PESSIMISTIC_READ);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.locking.ClusterLockDao#tryLock(org.jasig.portal.concurrency.locking.ClusterMutex, long, java.util.concurrent.TimeUnit)
     */
    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public boolean tryLock(ClusterMutex mutex, long time, TimeUnit unit) {
        if (this.isLocked(mutex)) {
            return true;
        }
        
        /*
         * Note that lock timeouts are only supported on Oracle and PostGres, on other platforms
         * transaction level timeouts are needed to avoid blocking for too long 
         */
        
        try {
            final long lockTime = unit.toMillis(time);
            this.entityManager.lock(mutex, 
                    LockModeType.PESSIMISTIC_READ,
                    ImmutableMap.<String, Object>of("javax.persistence.lock.timeout", lockTime));
            
            final LockModeType lockMode = this.entityManager.getLockMode(mutex);
            
            this.logger.debug("locked {} as {}", mutex, lockMode);
            
            return true;
        }
        catch (LockTimeoutException e) {
            this.logger.debug("{} is already locked", mutex);
            return false;
        }
        catch (PessimisticLockException e) {
            this.logger.debug("{} is already locked", mutex);
            return false;
        }
    }

    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public boolean isLocked(ClusterMutex mutex) {
        final LockModeType lockMode = this.entityManager.getLockMode(mutex);
        if (lockMode == LockModeType.PESSIMISTIC_READ) {
            //Reentrance is ok
            this.logger.debug("{} is already locked {}", mutex, lockMode);
            return true;
        }
        
        return false;
    }
}
