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
package org.apereo.portal.portlet.dao.jpa;

import com.google.common.base.Function;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.Validate;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("marketplaceRatingDAO")
@Qualifier("persistence")
public class JpaMarketplaceRatingDao extends BasePortalJpaDao implements IMarketplaceRatingDao {

    private static final String CLEAR_RATINGS_JPQL =
            "DELETE FROM MarketplaceRatingImpl m "
                    + "WHERE m.marketplaceRatingPK.portletDefinition = :portletDefinition";

    private IPortletDefinitionDao portletDefinitionDao;

    @Autowired
    public void getPortletDefinitionDao(IPortletDefinitionDao dao) {
        this.portletDefinitionDao = dao;
    }

    private CriteriaQuery<MarketplaceRatingImpl> findAllMarketPlaceRating;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllMarketPlaceRating =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<MarketplaceRatingImpl>>() {
                            @Override
                            public CriteriaQuery<MarketplaceRatingImpl> apply(
                                    CriteriaBuilder input) {
                                final CriteriaQuery<MarketplaceRatingImpl> criteriaQuery =
                                        input.createQuery(MarketplaceRatingImpl.class);
                                final Root<MarketplaceRatingImpl> definitionRoot =
                                        criteriaQuery.from(MarketplaceRatingImpl.class);
                                criteriaQuery.select(definitionRoot);
                                return criteriaQuery;
                            }
                        });
    }

    /**
     * This method will either create a new rating or update an existing rating
     *
     * @param Must not be null
     * @return the attached entity
     */
    @Override
    @PortalTransactional
    public IMarketplaceRating createOrUpdateRating(
            IMarketplaceRating marketplaceRatingImplementation) {
        Validate.notNull(marketplaceRatingImplementation, "MarketplaceRatingImpl must not be null");
        final EntityManager entityManager = this.getEntityManager();
        IMarketplaceRating temp =
                this.getRating(marketplaceRatingImplementation.getMarketplaceRatingPK());
        if (!entityManager.contains(marketplaceRatingImplementation) && temp != null) {
            // Entity is not managed and there is a rating for this portlet/user - update needed
            temp = entityManager.merge(marketplaceRatingImplementation);
        } else {
            // Entity is either already managed or doesn't exist - create needed
            temp = marketplaceRatingImplementation;
        }
        entityManager.persist(temp);
        return temp;
    }

    @Override
    @PortalTransactional
    public IMarketplaceRating createOrUpdateRating(
            int rating, String userName, String review, IPortletDefinition portletDefinition) {
        MarketplaceRatingImpl temp = new MarketplaceRatingImpl();
        PortletDefinitionImpl tempPortlet =
                new PortletDefinitionImpl(
                        portletDefinition.getType(),
                        portletDefinition.getFName(),
                        portletDefinition.getName(),
                        portletDefinition.getTitle(),
                        portletDefinition.getPortletDescriptorKey().getWebAppName(),
                        portletDefinition.getPortletDescriptorKey().getPortletName(),
                        portletDefinition.getPortletDescriptorKey().isFrameworkPortlet(),
                        portletDefinition.getPortletDefinitionId());
        MarketplaceRatingPK tempPK = new MarketplaceRatingPK(userName, tempPortlet);
        temp.setMarketplaceRatingPK(tempPK);
        temp.setRating(rating);
        temp.setReview(review);
        temp.setRatingDate(new Date());
        return this.createOrUpdateRating(temp);
    }

    /** @return List of all ratings */
    @Override
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public Set<IMarketplaceRating> getAllRatings() {
        final TypedQuery<MarketplaceRatingImpl> query =
                this.createCachedQuery(this.findAllMarketPlaceRating);
        return new HashSet<IMarketplaceRating>(query.getResultList());
    }

    /**
     * @since 5.0
     * @param marketplaceRatingPK the primary key of the entity you want
     * @return Set of ratings per portlet definition
     */
    @Override
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public Set<IMarketplaceRating> getRatingsByFname(String fname) {

        // Build criteria to fetch MarketplaceRatingImpl based on the incoming portlet name.
        final EntityManager entityManager = this.getEntityManager();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<IMarketplaceRating> getByPortlet =
                cb.createQuery(IMarketplaceRating.class);
        final Root<MarketplaceRatingImpl> imr = getByPortlet.from(MarketplaceRatingImpl.class);
        getByPortlet.select(imr);

        // Define the path to the portlet fName
        final Path<MarketplaceRatingPK> mrPK = imr.get("marketplaceRatingPK");
        final Path<PortletDefinitionImpl> mrIPD = mrPK.get("portletDefinition");

        final ParameterExpression<String> portletFName = cb.parameter(String.class, "portletFName");

        getByPortlet.where(cb.equal(mrIPD.get("fname"), portletFName));
        TypedQuery<IMarketplaceRating> tq = entityManager.createQuery(getByPortlet);
        tq.setParameter("portletFName", fname);
        List<IMarketplaceRating> resultList = tq.getResultList();
        Set<IMarketplaceRating> resultSet = new HashSet<IMarketplaceRating>(resultList);
        return resultSet;
    }

    @Override
    @PortalTransactional
    public IMarketplaceRating getRating(String userName, IPortletDefinition portletDefinition) {
        PortletDefinitionImpl tempPortlet =
                new PortletDefinitionImpl(
                        portletDefinition.getType(),
                        portletDefinition.getFName(),
                        portletDefinition.getName(),
                        portletDefinition.getTitle(),
                        portletDefinition.getPortletDescriptorKey().getWebAppName(),
                        portletDefinition.getPortletDescriptorKey().getPortletName(),
                        portletDefinition.getPortletDescriptorKey().isFrameworkPortlet(),
                        portletDefinition.getPortletDefinitionId());
        MarketplaceRatingPK tempPK = new MarketplaceRatingPK(userName, tempPortlet);
        return this.getRating(tempPK);
    }

    /**
     * @param marketplaceRatingPK the primary key of the entity you want
     * @return an attached entity if found, null otherwise
     */
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IMarketplaceRating getRating(MarketplaceRatingPK marketplaceRatingPK) {
        final MarketplaceRatingPK tempRatingPK = marketplaceRatingPK;
        MarketplaceRatingImpl temp = new MarketplaceRatingImpl();
        temp.setMarketplaceRatingPK(marketplaceRatingPK);
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(temp)) {
            temp = entityManager.merge(temp);
            return temp;
        } else {
            final TypedQuery<MarketplaceRatingImpl> query =
                    this.createQuery(
                            this.createCriteriaQuery(
                                    new Function<
                                            CriteriaBuilder,
                                            CriteriaQuery<MarketplaceRatingImpl>>() {
                                        @Override
                                        public CriteriaQuery<MarketplaceRatingImpl> apply(
                                                CriteriaBuilder input) {
                                            final CriteriaQuery<MarketplaceRatingImpl>
                                                    criteriaQuery =
                                                            input.createQuery(
                                                                    MarketplaceRatingImpl.class);
                                            final Root<MarketplaceRatingImpl> definitionRoot =
                                                    criteriaQuery.from(MarketplaceRatingImpl.class);
                                            Predicate conditionUser =
                                                    input.equal(
                                                            definitionRoot
                                                                    .get("marketplaceRatingPK")
                                                                    .get("userName"),
                                                            tempRatingPK.getUserName());
                                            Predicate conditionPortlet =
                                                    input.equal(
                                                            definitionRoot
                                                                    .get("marketplaceRatingPK")
                                                                    .get("portletDefinition"),
                                                            tempRatingPK.getPortletDefinition());
                                            Predicate allConditions =
                                                    input.and(conditionPortlet, conditionUser);
                                            criteriaQuery.where(allConditions);
                                            return criteriaQuery;
                                        }
                                    }));
            List<MarketplaceRatingImpl> results = query.getResultList();
            if (!results.isEmpty()) {
                return results.get(0);
            } else {
                return null;
            }
        }
    }

    /** @param entity to delete - can not be null */
    @Override
    @PortalTransactional
    public void deleteRating(IMarketplaceRating marketplaceRatingImplementation) {
        Validate.notNull(
                marketplaceRatingImplementation, "marketplaceRatingImplementation can not be null");
        final IMarketplaceRating persistantMarketplaceRatingImpl;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(marketplaceRatingImplementation)) {
            persistantMarketplaceRatingImpl = marketplaceRatingImplementation;
        } else {
            persistantMarketplaceRatingImpl = entityManager.merge(marketplaceRatingImplementation);
        }
        entityManager.remove(persistantMarketplaceRatingImpl);
    }

    @Override
    @PortalTransactional
    public void clearRatingsForPortlet(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        final EntityManager em = getEntityManager();
        final Query query = em.createQuery(CLEAR_RATINGS_JPQL);
        query.setParameter("portletDefinition", portletDefinition);
        final int deleteCount = query.executeUpdate();
        logger.info(
                "Cleared {} ratings from portlet {}", deleteCount, portletDefinition.getFName());
    }

    @Override
    @PortalTransactional
    public void aggregateMarketplaceRating() {
        // setup
        EntityManager em = this.getEntityManager();

        // get list of average ratings
        Query aggregatedQuery =
                em.createQuery(
                        "SELECT AVG(m.rating) as rating, "
                                + "       count(m.marketplaceRatingPK.portletDefinition.internalPortletDefinitionId) as theCount, "
                                + "       m.marketplaceRatingPK.portletDefinition.internalPortletDefinitionId as portletId "
                                + "  FROM MarketplaceRatingImpl m "
                                + " GROUP BY m.marketplaceRatingPK.portletDefinition.internalPortletDefinitionId");

        @SuppressWarnings("unchecked")
        List<Object[]> aggregatedResults = aggregatedQuery.getResultList();

        // update the portlet definition with the average rating
        for (Object[] result : aggregatedResults) {
            if (result != null && result.length == 3) {
                try {
                    Double averageRating = (Double) result[0];
                    Long usersRated = (Long) result[1];
                    String portletId = ((Long) result[2]).toString();

                    IPortletDefinition portletDefinition =
                            portletDefinitionDao.getPortletDefinition(portletId);
                    if (portletDefinition != null) {
                        portletDefinition.setRating(averageRating);
                        portletDefinition.setUsersRated(usersRated);
                        em.persist(portletDefinition);
                    }
                } catch (Exception ex) {
                    logger.warn("Issue aggregating portlet ratings, recoverable", ex);
                }
            }
        }
    }

    @Override
    public Set<IMarketplaceRating> getAllRatingsForPortletInLastXDays(
            int numberOfDaysBack, final String fname) {

        LocalDateTime todayMinusNumberOfDaysBack = LocalDateTime.now().minusDays(numberOfDaysBack);

        final TypedQuery<MarketplaceRatingImpl> query =
                this.createQuery(
                        this.createCriteriaQuery(
                                new Function<
                                        CriteriaBuilder, CriteriaQuery<MarketplaceRatingImpl>>() {
                                    @Override
                                    public CriteriaQuery<MarketplaceRatingImpl> apply(
                                            CriteriaBuilder input) {
                                        final CriteriaQuery<MarketplaceRatingImpl> criteriaQuery =
                                                input.createQuery(MarketplaceRatingImpl.class);
                                        final Root<MarketplaceRatingImpl> definitionRoot =
                                                criteriaQuery.from(MarketplaceRatingImpl.class);
                                        Predicate ratingDatePredicate =
                                                input.greaterThanOrEqualTo(
                                                        definitionRoot.<java.util.Date>get(
                                                                "ratingDate"),
                                                        todayMinusNumberOfDaysBack.toDate());
                                        Predicate conditionPortlet =
                                                input.equal(
                                                        definitionRoot
                                                                .get("marketplaceRatingPK")
                                                                .get("portletDefinition"),
                                                        portletDefinitionDao
                                                                .getPortletDefinitionByFname(
                                                                        fname));
                                        Predicate allConditions =
                                                input.and(conditionPortlet, ratingDatePredicate);
                                        criteriaQuery.select(definitionRoot).where(allConditions);

                                        return criteriaQuery;
                                    }
                                }));

        return new HashSet<IMarketplaceRating>(query.getResultList());
    }

    @Override
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public Set<IMarketplaceRating> getAllRatingsInLastXDays(int numberOfDaysBack) {

        LocalDateTime todayMinusNumberOfDaysBack = LocalDateTime.now().minusDays(numberOfDaysBack);

        final TypedQuery<MarketplaceRatingImpl> query =
                this.createQuery(
                        this.createCriteriaQuery(
                                new Function<
                                        CriteriaBuilder, CriteriaQuery<MarketplaceRatingImpl>>() {
                                    @Override
                                    public CriteriaQuery<MarketplaceRatingImpl> apply(
                                            CriteriaBuilder input) {
                                        final CriteriaQuery<MarketplaceRatingImpl> criteriaQuery =
                                                input.createQuery(MarketplaceRatingImpl.class);
                                        final Root<MarketplaceRatingImpl> definitionRoot =
                                                criteriaQuery.from(MarketplaceRatingImpl.class);
                                        Predicate ratingDatePredicate =
                                                input.greaterThanOrEqualTo(
                                                        definitionRoot.<java.util.Date>get(
                                                                "ratingDate"),
                                                        todayMinusNumberOfDaysBack.toDate());

                                        criteriaQuery
                                                .select(definitionRoot)
                                                .where(ratingDatePredicate);

                                        return criteriaQuery;
                                    }
                                }));

        return new HashSet<IMarketplaceRating>(query.getResultList());
    }
}
