package org.jasig.portal.rest.layout;

public class MarketplaceEntryRating {
    
    public MarketplaceEntryRating(int rating, String review) {
        this.rating = rating;
        this.review = review;
    }
    
    private int rating;
    private String review;
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
}
