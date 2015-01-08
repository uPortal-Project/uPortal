/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.api.permissions;

import org.jasig.portal.api.Principal;
import org.springframework.util.Assert;

/* package-private */ class AssignmentImpl implements Assignment {
	
	private static final long serialVersionUID = 1L;

	private final Owner owner;
	private final Activity activity;
	private final Principal principal;
	private final Target target;
	private final boolean inherited;
	
	public AssignmentImpl(Owner owner, Activity activity, Principal principal, Target target, boolean inherited) {
		
		Assert.notNull(owner, "Argument 'owner' cannot be null");
		Assert.notNull(activity, "Argument 'activity' cannot be null");
		Assert.notNull(principal, "Argument 'principal' cannot be null");
		// NB:  Target may be null
		
		this.owner = owner;
		this.activity = activity;
		this.principal = principal;
		this.target = target;
		this.inherited = inherited;

	}

	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public Principal getPrincipal() {
		return principal;
	}

	@Override
	public Target getTarget() {
		return target;
	}

	@Override
	public boolean isInherited() {
		return inherited;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activity == null) ? 0 : activity.hashCode());
		result = prime * result + (inherited ? 1231 : 1237);
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result
				+ ((principal == null) ? 0 : principal.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentImpl other = (AssignmentImpl) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (inherited != other.inherited)
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (principal == null) {
			if (other.principal != null)
				return false;
		} else if (!principal.equals(other.principal))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AssignmentImpl [owner=" + owner + ", activity=" + activity
				+ ", principal=" + principal + ", target=" + target
				+ ", inherited=" + inherited + "]";
	}

}
