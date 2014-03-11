package org.jasig.portal.portlet.dao;

import java.util.Set;

import org.jasig.portal.persondir.ILocalAccountPerson;
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
    public IMarketplaceRating createOrUpdateRating(int rating, ILocalAccountPerson person,
            IPortletDefinition portletDefinition);

    public IMarketplaceRating getRating(ILocalAccountPerson person,
            IPortletDefinition portletDefinition);
}
