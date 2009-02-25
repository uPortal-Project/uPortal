/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.smartldap;

import java.util.Collections;
import java.util.List;

import org.jasig.portal.groups.IEntityGroup;

public final class LdapRecord {

	// Instance Members.
	private final IEntityGroup group;
	private final List<String> keysOfChildren;

	/*
	 * Public API.
	 */
		
	public LdapRecord(IEntityGroup group, List<String> keysOfChildren) {

		// Assertions.
		if (group == null) {
			String msg = "Argument 'group' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (keysOfChildren == null) {
			String msg = "Argument 'keysOfChildren' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		// Instance Members.
		this.group = group;
		this.keysOfChildren = Collections.unmodifiableList(keysOfChildren);
		
	}
	
	public IEntityGroup getGroup() {
		return group;
	}
	
	public List<String> getKeysOfChildren() {
		return keysOfChildren;
	}
	
}
