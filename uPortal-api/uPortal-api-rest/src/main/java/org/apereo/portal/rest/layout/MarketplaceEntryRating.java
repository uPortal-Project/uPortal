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
package org.apereo.portal.rest.layout;

import java.util.Date;

public class MarketplaceEntryRating {

    public MarketplaceEntryRating() {}

    public MarketplaceEntryRating(int rating, String review) {
        this.rating = rating;
        this.review = review;
    }

    private int rating;
    private String review;
    private String user;
    private String portletName;
    private Date ratingDate;
    private String portletFName;

    public MarketplaceEntryRating(
            String user,
            String portletName,
            String portletFName,
            int rating,
            String review,
            Date ratingDate) {
        this.user = user;
        this.portletName = portletName;
        this.portletFName = portletFName;
        this.rating = rating;
        this.review = review;
        this.ratingDate = ratingDate;
    }

    @Override
    public String toString() {
        return "MarketplaceEntryRating{"
                + "rating="
                + rating
                + ", review="
                + review
                + ", user="
                + user
                + ", portletName="
                + portletName
                + ", ratingDate="
                + ratingDate
                + ", portletFName="
                + portletFName
                + '}';
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }

    public String getPortletFName() {
        return portletFName;
    }

    public void setPortletFName(String portletFName) {
        this.portletFName = portletFName;
    }
}
