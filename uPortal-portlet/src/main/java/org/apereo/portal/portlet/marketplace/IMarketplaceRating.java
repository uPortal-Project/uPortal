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
package org.apereo.portal.portlet.marketplace;

import org.apereo.portal.portlet.dao.jpa.MarketplaceRatingPK;

public interface IMarketplaceRating {
    public static final int MAX_RATING = 5;
    public static final int MIN_RATING = 0;
    public static final int REVIEW_MAX_LENGTH = 160;

    public int getRating();

    public void setRating(int rating);

    public MarketplaceRatingPK getMarketplaceRatingPK();

    public void setMarketplaceRatingPK(MarketplaceRatingPK marketplaceRatingPK);

    /** @return a text Review of portlet */
    public String getReview();

    /**
     * Will truncate all review inputs to {@value #REVIEW_MAX_LENGTH} will trim all leading and
     * following whitespaces
     *
     * @param review a text review of the portlet
     */
    public void setReview(String review);
}
