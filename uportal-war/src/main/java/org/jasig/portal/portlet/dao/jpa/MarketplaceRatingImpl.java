package org.jasig.portal.portlet.dao.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.EmbeddedId;

import org.jasig.portal.portlets.marketplace.IMarketplaceRating;

@Entity
@Table(name = "MKTPLACE_RATINGS")
class MarketplaceRatingImpl implements IMarketplaceRating{


	@EmbeddedId
	private MarketplaceRatingPK marketplaceRatingPK;

	@Column(name="RATING")
	private int rating;

	@Override
	public MarketplaceRatingPK getMarketplaceRatingPK() {
		return marketplaceRatingPK;
	}

	@Override
	public void setMarketplaceRatingPK(MarketplaceRatingPK marketplaceRatingPK) {
		this.marketplaceRatingPK = marketplaceRatingPK;
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
		StringBuilder builder = new StringBuilder();
		builder.append("RatingPK: \n");
		builder.append(this.marketplaceRatingPK);
		builder.append("Rating: ");
		builder.append(this.rating);
		return builder.toString();
	}

}
