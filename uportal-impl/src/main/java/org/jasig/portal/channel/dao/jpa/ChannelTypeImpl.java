/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.channel.IChannelType;

/**
 * JPA implementation of the IChannelType interface.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Entity
@Table(name = "UP_CHAN_TYPE")
@GenericGenerator(name = "UP_CHANNEL_TYPE_DEF_GEN", strategy = "native", parameters = {
		@Parameter(name = "sequence", value = "UP_CHANNEL_TYPE_DEF_SEQ"),
		@Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
		@Parameter(name = "column", value = "NEXT_UP_CHANNEL_TYPE_DEF_HI") })
public class ChannelTypeImpl implements Serializable, IChannelType {

	@Id
	@GeneratedValue(generator = "UP_CHANNEL_TYPE_DEF_GEN")
	@Column(name = "TYPE_ID")
	private Long internalId;

	@Column(name = "TYPE", length = 128, nullable = false)
	private String javaClass;

	@Column(name = "TYPE_NAME", length = 70, unique = true, nullable = false)
	private String name;

	@Column(name = "TYPE_DESCR", length = 2000)
	private String descr;

	@Column(name = "TYPE_DEF_URI", length = 255, nullable = false)
	private String cpdUri;
	
	/**
	 * Default constructor
	 */
	public ChannelTypeImpl() { }

	/**
	 * Constructs a channel type.
	 * 
	 * @param id
	 *            the channel type ID
	 */
	public ChannelTypeImpl(int id) {
		if (id != 0) {
			this.internalId = new Long(id);
		}
	}

	// Getter methods
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#getId()
	 */
	public int getId() {
		if (internalId == null) {
			return 0;
		} else {
			return internalId.intValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#getJavaClass()
	 */
	public String getJavaClass() {
		return javaClass;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#getDescription()
	 */
	public String getDescription() {
		return descr;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#getCpdUri()
	 */
	public String getCpdUri() {
		return cpdUri;
	}

	// Setter methods
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jasig.portal.IChannelType#setJavaClass(java.lang.String)
	 */
	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#setDescription(java.lang.String)
	 */
	public void setDescription(String descr) {
		this.descr = descr;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelType#setCpdUri(java.lang.String)
	 */
	public void setCpdUri(String cpdUri) {
		this.cpdUri = cpdUri;
	}

}
