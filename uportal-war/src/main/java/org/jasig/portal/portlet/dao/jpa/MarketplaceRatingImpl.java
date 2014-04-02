package org.jasig.portal.portlet.dao.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.EmbeddedId;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.jasig.portal.portlets.marketplace.IMarketplaceRating;

@Entity
@Table(name = "UP_PORTLET_RATING")
class MarketplaceRatingImpl implements IMarketplaceRating{


	@EmbeddedId
	private MarketplaceRatingPK marketplaceRatingPK;

	@Column(name="RATING")
	private int rating;
	
	@Column(name="REVIEW")
	private String review;

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
        this.review = review;
    }


	/**
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * @param must be within range of MAX_RATING and MIN_RATING
	 *        
	 */
	public void setRating(int rating) {
	    if(rating>MAX_RATING || rating < MIN_RATING){
	        throw new IllegalArgumentException();
	    }
		this.rating = rating;
	}

    @Override
    public String toString(){
        return new ToStringBuilder(this).
            append("RatingPK: ", this.marketplaceRatingPK).
            append("Rating: ", this.rating).
            append("Review: ", this.review).
            toString();
    }

}
