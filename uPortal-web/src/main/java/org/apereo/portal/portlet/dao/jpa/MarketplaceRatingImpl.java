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

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;

@Entity
@Table(name = "UP_PORTLET_RATING")
class MarketplaceRatingImpl implements IMarketplaceRating {

    @EmbeddedId private MarketplaceRatingPK marketplaceRatingPK;

    @Column(name = "RATING")
    private int rating;

    @Column(name = "REVIEW", length = REVIEW_MAX_LENGTH)
    private String review;

    @Column(name = "RATINGDATE")
    private Date ratingDate;

    @Override
    public MarketplaceRatingPK getMarketplaceRatingPK() {
        return marketplaceRatingPK;
    }

    @Override
    public void setMarketplaceRatingPK(MarketplaceRatingPK marketplaceRatingPK) {
        this.marketplaceRatingPK = marketplaceRatingPK;
    }

    @Override
    public String getReview() {
        return review;
    }

    @Override
    public void setReview(String review) {
        if (review != null) {
            review = review.trim().substring(0, Math.min(review.length(), REVIEW_MAX_LENGTH));
        }
        this.review = review;
    }

    /** @return the rating */
    @Override
    public int getRating() {
        return rating;
    }

    /** @param rating must be within range of MAX_RATING and MIN_RATING */
    @Override
    public void setRating(int rating) {
        if (rating > MAX_RATING || rating < MIN_RATING) {
            throw new IllegalArgumentException();
        }
        this.rating = rating;
    }

    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("RatingPK: ", this.marketplaceRatingPK)
                .append("Rating: ", this.rating)
                .append("Review: ", this.review)
                .append("Date: ", this.ratingDate)
                .toString();
    }
}
