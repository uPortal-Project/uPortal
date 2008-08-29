/**
 * Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.groups.smartldap;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;
import org.springframework.ldap.core.AttributesMapper;

public final class SimpleAttributesMapper implements AttributesMapper {
	
	private static final String GROUP_DESCRIPTION = 
					"This group was pulled from the directory server.";
	
	/**
	 * Name of the LDAP attribute on a group that tells you 
	 * its key (normally 'dn').
	 */
	private String keyAttributeName = null; 
	
	/**
	 * Name of the LDAP attribute on a group that tells you 
	 * the name of the group.
	 */
	private String groupNameAttributeName = null; 
	
	/**
	 * Name of the LDAP attribute on a group that tells you 
	 * who its children are.
	 */
	private String membershipAttributeName = null; 
	
    private final Log log = LogFactory.getLog(getClass());

    /*
	 * Public API.
	 */	
	
	public Object mapFromAttributes(Attributes attr) {
		
		// Assertions.
		if (keyAttributeName == null) {
			String msg = "The property 'keyAttributeName' must be set.";
			throw new IllegalStateException(msg);
		}
		if (groupNameAttributeName == null) {
			String msg = "The property 'groupNameAttributeName' must be set.";
			throw new IllegalStateException(msg);
		}
		if (membershipAttributeName == null) {
			String msg = "The property 'membershipAttributeName' must be set.";
			throw new IllegalStateException(msg);
		}
		
		if (log.isInfoEnabled()) {
			String msg = "SimpleAttributesMapper.mapFromAttributes() :: settings:  keyAttributeName='" 
							+ keyAttributeName + "', groupNameAttributeName='" 
							+ groupNameAttributeName + "', groupNameAttributeName='" 
							+ groupNameAttributeName + "'";
			log.info(msg);
		}
		
		LdapRecord rslt;
		
		try {
			
			String key = (String) attr.get(keyAttributeName).get();
			String groupName = (String) attr.get(groupNameAttributeName).get();
						
			IEntityGroup g = new EntityTestingGroupImpl(key, IPerson.class);
	        g.setCreatorID("System");
	        g.setName(groupName);
	        g.setDescription(GROUP_DESCRIPTION);
			List<String> membership = new LinkedList<String>();
			Attribute m = attr.get(membershipAttributeName);
			if (m != null) {
				for (Enumeration<?> en=m.getAll(); en.hasMoreElements();) {
					membership.add((String) en.nextElement());
				}
			}
			rslt = new LdapRecord(g, membership);
			
			if (log.isDebugEnabled()) {
				StringBuilder msg = new StringBuilder();
				msg.append("Record Details:")
							.append("\n\tkey=").append(key)
							.append("\n\tgroupName=").append(groupName)
							.append("\n\tmembers:");
				for (String s : membership) {
					msg.append("\n\t\t").append(s);
				}
				log.debug(msg.toString());
			}

		} catch (Throwable t) {
			log.error("Error in SimpleAttributesMapper", t);
			String msg = "SimpleAttributesMapper failed to create a LdapRecord "
									+ "from the specified Attributes:  " + attr;
			throw new RuntimeException(msg, t);
		}
		
		return rslt;		
		
	}
	
	public void setKeyAttributeName(String keyAttributeName) {
		this.keyAttributeName = keyAttributeName;
	}

	public void setGroupNameAttributeName(String groupNameAttributeName) {
		this.groupNameAttributeName = groupNameAttributeName;
	}

	public void setMembershipAttributeName(String membershipAttributeName) {
		this.membershipAttributeName = membershipAttributeName;
	}

}
