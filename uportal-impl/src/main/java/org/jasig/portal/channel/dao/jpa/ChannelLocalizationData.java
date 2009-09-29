/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

/**
 * ChannelLocalizationData represents locale-specific ChannelDefinition metadata.
 * This class is intended to be used in a JPA/Hibernate-managed map and does
 * not include the locale itself as part of its data model.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
@Embeddable
@Table(name = "UP_CHANNEL_MDATA")
public class ChannelLocalizationData implements Serializable {
	
	@Column(name = "CHAN_NAME", length = 128)
	private String name;
	
	@Column(name = "CHAN_TITLE", length = 128)
	private String title;
	
	@Column(name = "CHAN_DESC", length = 255)
	private String description;

	
	/**
	 * Default constructor
	 */
	public ChannelLocalizationData() { }
	
	
	// Public getters
	
	/**
	 * Get the name for this locale.
	 * 
	 * @return localized name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the title for this locale.
	 * 
	 * @return localized title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Get the description for this locale.
	 * 
	 * @return localized description
	 */
	public String getDescription() {
		return this.description;
	}
	
	
	// Public setters
	
	/**
	 * Set the name for this locale
	 * 
	 * @param name localized name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Set the title for this locale
	 * 
	 * @param title localized title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Set the description for this localized
	 * 
	 * @param description localized description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
