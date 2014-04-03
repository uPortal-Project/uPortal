package org.jasig.portal.portlet.dao;

import java.util.Set;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlets.marketplace.IMarketplaceRating;

public interface IMarketplaceRatingDao{
    
    /**
     * @author vertein
     * @return all ratings. Not null
     */
	public Set<IMarketplaceRating> getAllRatings();
	
	/**
	 * @author vertein
	 * @param IMarketplaceRating. Can not be null
	 */
	public void deleteRating(IMarketplaceRating marketplaceRatingImplementation);
	
	/**
	 * @author vertein
	 * @param IMarketplaceRating. Can not be null
	 * @return attached entity
	 */
	public IMarketplaceRating createOrUpdateRating(IMarketplaceRating marketplaceRatingImplementation);


	/**
	 * Creates or updates a given a ratings, person, and user
	 * @author vertein
	 * @param parseInt
	 * @param person
	 * @param portletDefinitionByFname
	 */
    public IMarketplaceRating createOrUpdateRating(int rating, String userName,
            IPortletDefinition portletDefinition);

    /**
     * Returns a rating if found.  Can return null if no rating exists.
     * @param userName
     * @param portletDefinition
     * @return - can be null
     */
    public IMarketplaceRating getRating(String userName,
            IPortletDefinition portletDefinition);
    
    /**
     * Aggregates the IMarketplaceRating into IPortletDefinition
     */
    public void aggregateMarketplaceRating();
}
