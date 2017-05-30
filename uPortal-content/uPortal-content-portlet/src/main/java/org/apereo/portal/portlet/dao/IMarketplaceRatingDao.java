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
package org.apereo.portal.portlet.dao;

import java.util.Set;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;
import org.apereo.portal.portlet.om.IPortletDefinition;

public interface IMarketplaceRatingDao {

    /** @return all ratings. Not null */
    Set<IMarketplaceRating> getAllRatings();

    /**
     * @return all ratings for a particular portlet
     * @since 4.4
     */
    public Set<IMarketplaceRating> getRatingsByFname(String fname);

    /** @param IMarketplaceRating. Can not be null */
    void deleteRating(IMarketplaceRating marketplaceRatingImplementation);

    /** Useful for when you're about to delete a portlet. */
    void clearRatingsForPortlet(IPortletDefinition portletDefinition);

    /**
     * @param IMarketplaceRating. Can not be null
     * @return attached entity
     */
    IMarketplaceRating createOrUpdateRating(IMarketplaceRating marketplaceRatingImplementation);

    /**
     * Creates or updates a given a ratings, person, and user
     *
     * @param parseInt
     * @param person
     * @param portletDefinitionByFname
     */
    IMarketplaceRating createOrUpdateRating(
            int rating, String userName, String review, IPortletDefinition portletDefinition);

    /**
     * Returns a rating if found. Can return null if no rating exists.
     *
     * @param userName
     * @param portletDefinition
     * @return - can be null
     */
    IMarketplaceRating getRating(String userName, IPortletDefinition portletDefinition);

    /** Aggregates the IMarketplaceRating into IPortletDefinition */
    void aggregateMarketplaceRating();
}
