package org.jasig.portal.portlet.dao.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.portlet.dao.IMarketplaceRatingDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlets.marketplace.IMarketplaceRating;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

@Repository("marketplaceRatingDAO")
@Qualifier("persistence")
public class JpaMarketplaceRatingDao extends BasePortalJpaDao implements IMarketplaceRatingDao{

    private CriteriaQuery<MarketplaceRatingImpl> findAllMarketPlaceRating;

    @Override
    public void afterPropertiesSet() throws Exception{
        this.findAllMarketPlaceRating = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<MarketplaceRatingImpl>>(){
            @Override
            public CriteriaQuery<MarketplaceRatingImpl> apply(CriteriaBuilder input) {
                final CriteriaQuery<MarketplaceRatingImpl> criteriaQuery = input.createQuery(MarketplaceRatingImpl.class);
                final Root<MarketplaceRatingImpl> definitionRoot = criteriaQuery.from(MarketplaceRatingImpl.class);
                criteriaQuery.select(definitionRoot);
                return criteriaQuery;
            }
        });
    }

    /**
     * This method will either create a new rating or update an
     * existing rating
     * 
     * @author vertein
     * @param  Must not be null
     * @return the attached entity
     * 
     */
    @Override
    @PortalTransactional
    public IMarketplaceRating createOrUpdateRating(IMarketplaceRating marketplaceRatingImplementation){
        Validate.notNull(marketplaceRatingImplementation, "MarketplaceRatingImpl must not be null");
        final EntityManager entityManager = this.getEntityManager();
        IMarketplaceRating temp = this.getRating(marketplaceRatingImplementation.getMarketplaceRatingPK());
        if(!entityManager.contains(marketplaceRatingImplementation) && temp!=null){
            //Entity is not managed and there is a rating for this portlet/user - update needed
            temp = entityManager.merge(marketplaceRatingImplementation);
        }else{
            //Entity is either already managed or doesn't exist - create needed
            temp = marketplaceRatingImplementation;
        }
        entityManager.persist(temp);
        return temp;  
    }
    
    @Override
    @PortalTransactional
    public IMarketplaceRating createOrUpdateRating(int rating, ILocalAccountPerson person,
            IPortletDefinition portletDefinition) {
        MarketplaceRatingImpl temp = new MarketplaceRatingImpl();
        PortletDefinitionImpl tempPortlet = new PortletDefinitionImpl(portletDefinition.getType(), portletDefinition.getFName(), portletDefinition.getName(), 
                portletDefinition.getTitle(), portletDefinition.getPortletDescriptorKey().getWebAppName(), portletDefinition.getPortletDescriptorKey().getPortletName(), 
                portletDefinition.getPortletDescriptorKey().isFrameworkPortlet(), portletDefinition.getPortletDefinitionId());
        MarketplaceRatingPK tempPK = new MarketplaceRatingPK(person.getName(), tempPortlet);
        temp.setMarketplaceRatingPK(tempPK);
        temp.setRating(rating);
        return this.createOrUpdateRating(temp);
    }

    /**
     * @author vertein
     * @return List of all ratings
     */
    @Override
    @PortalTransactionalReadOnly
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public Set<IMarketplaceRating> getAllRatings(){
        final TypedQuery<MarketplaceRatingImpl> query = this.createCachedQuery(this.findAllMarketPlaceRating);
        return new HashSet<IMarketplaceRating>(query.getResultList());
    }
    
    @Override
    @PortalTransactional
    public IMarketplaceRating getRating(ILocalAccountPerson person, IPortletDefinition portletDefinition) {
        PortletDefinitionImpl tempPortlet = new PortletDefinitionImpl(portletDefinition.getType(), portletDefinition.getFName(), portletDefinition.getName(), 
                portletDefinition.getTitle(), portletDefinition.getPortletDescriptorKey().getWebAppName(), portletDefinition.getPortletDescriptorKey().getPortletName(), 
                portletDefinition.getPortletDescriptorKey().isFrameworkPortlet(), portletDefinition.getPortletDefinitionId());
        MarketplaceRatingPK tempPK = new MarketplaceRatingPK(person.getName(), tempPortlet);
        return this.getRating(tempPK);
    }

    /**
     * @author vertein
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
        if(entityManager.contains(temp)){
            temp = entityManager.merge(temp);
            return temp;
        }else{
            final TypedQuery<MarketplaceRatingImpl> query = this.createQuery(
            this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<MarketplaceRatingImpl>>(){
                @Override
                public CriteriaQuery<MarketplaceRatingImpl> apply(CriteriaBuilder input) {
                    final CriteriaQuery<MarketplaceRatingImpl> criteriaQuery = input.createQuery(MarketplaceRatingImpl.class);
                    final Root<MarketplaceRatingImpl> definitionRoot = criteriaQuery.from(MarketplaceRatingImpl.class);
                    Predicate conditionUser = input.equal(definitionRoot.get("marketplaceRatingPK").get("userName"), tempRatingPK.getUserName());
                    Predicate conditionPortlet = input.equal(definitionRoot.get("marketplaceRatingPK").get("portletDefinition"), tempRatingPK.getPortletDefinition());
                    Predicate allConditions = input.and(conditionPortlet, conditionUser);
                    criteriaQuery.where(allConditions);
                    return criteriaQuery;
                }
            }));
            List<MarketplaceRatingImpl> results = query.getResultList();
            if(!results.isEmpty()){
                return results.get(0);
            }else{
                return null;
            }
        }
    }

    /**
     * @author vertein
     * @param entity to delete - can not be null
     */
    @Override
    @PortalTransactional
    public void deleteRating(IMarketplaceRating marketplaceRatingImplementation){
        Validate.notNull(marketplaceRatingImplementation, "ratingPK can not be null");
        final IMarketplaceRating persistantMarketplaceRatingImpl;
        final EntityManager entityManager = this.getEntityManager();
        if(entityManager.contains(marketplaceRatingImplementation)){
            persistantMarketplaceRatingImpl=marketplaceRatingImplementation;
        }else{
            persistantMarketplaceRatingImpl=entityManager.merge(marketplaceRatingImplementation);
        }
        entityManager.remove(persistantMarketplaceRatingImpl);
    }
}
